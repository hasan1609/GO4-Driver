package com.g4s.go4_driver.ui.fragment

import android.Manifest
import android.app.ActivityManager
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.g4s.go4_driver.R
import com.g4s.go4_driver.databinding.FragmentHomeBinding
import com.g4s.go4_driver.model.ResponseCekBooking
import com.g4s.go4_driver.services.LocationService
import com.g4s.go4_driver.session.SessionManager
import com.g4s.go4_driver.ui.activity.ProfileActivity
import com.g4s.go4_driver.utils.AlertOrderUtils
import com.g4s.go4_driver.webservice.ApiClient
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.firebase.database.*
import com.google.gson.Gson
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.support.v4.toast

class HomeFragment : Fragment(), AnkoLogger, OnMapReadyCallback {

    private lateinit var binding: FragmentHomeBinding
    lateinit var sessionManager: SessionManager
    var api = ApiClient.instance()
    private lateinit var progressDialog: ProgressDialog
    private lateinit var alertOrderUtils: AlertOrderUtils
    private val permissionCode = 101
    private val REQUEST_BACKGROUND_LOCATION_PERMISSION = 1005
    private val REQUEST_LOCATION_SETTINGS = 1004
    private lateinit var mMap: GoogleMap // Google Maps objek
    private var locationServiceIntent: Intent? = null // Intent untuk LocationService
    private var currentLocationMarker: Marker? = null

    // Register receiver for location updates
    private val orderReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val responseDataJson = intent!!.getStringExtra("response_data")
            val responseData = Gson().fromJson(responseDataJson, ResponseCekBooking::class.java)
            alertOrderUtils.showAlertDialog(responseData)
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
        binding.lifecycleOwner = this
        sessionManager = SessionManager(requireActivity())
        progressDialog = ProgressDialog(requireActivity())
        alertOrderUtils = AlertOrderUtils(requireContext())
        val database = FirebaseDatabase.getInstance()
        val locationRef = database.getReference("driver_active").child(sessionManager.getId().toString())
        // Tambahkan listener untuk memeriksa keberadaan idUser di database
        loading(true)
        locationRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Jika data untuk idUser tersedia, aktifkan Switch
                if (snapshot.exists()) {
                    loading(false)
                    binding.statusDriver.setImageResource(R.drawable.ic_switch_on)
                    startLocationService()
                    // Dapatkan status dari snapshot
                    val userStatus = snapshot.child("status").getValue(String::class.java)
                    if (userStatus == "active") {
                        binding.txtStatus.text = "On"
                        binding.txtStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
                    } else if (userStatus == "busy") {
                        binding.txtStatus.text = "Busy" // Mengganti teks untuk status "sibuk"
                        binding.txtStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.red)) // Mengubah warna teks untuk status "sibuk"
                    }
                } else {
                    loading(false)
                    binding.statusDriver.setImageResource(R.drawable.ic_switch_off)
                    binding.txtStatus.text = "Off"
                    binding.txtStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                }
            }
            override fun onCancelled(error: DatabaseError) {
                // Handle error (optional)
            }
        })
        // Inisialisasi Google Maps
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapview) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Register receiver for location updates
        val locationUpdateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == "LOCATION_UPDATE") {
                    val latitude = intent.getDoubleExtra("latitude", 0.0)
                    val longitude = intent.getDoubleExtra("longitude", 0.0)

                    // Update peta dengan lokasi terbaru
                    updateMapLocation(latitude, longitude)
                }
            }
        }
        val filter = IntentFilter("LOCATION_UPDATE")
        requireActivity().registerReceiver(locationUpdateReceiver, filter)
        val filter2 = IntentFilter("CEK_BOOKING")
        requireActivity().registerReceiver(orderReceiver, filter2)

        binding.namaDriver.text = sessionManager.getNamaDriver()
        binding.lyProfil.setOnClickListener {
            startActivity<ProfileActivity>()
        }
        binding.statusDriver.setOnClickListener {
            if (isServiceRunning(LocationService::class.java)) {
                loading(true)
                // Layanan sedang berjalan, berarti kita akan mematikan layanan
                stopLocationService()
                binding.statusDriver.setImageResource(R.drawable.ic_switch_off)
                binding.txtStatus.text = "Off"
                binding.txtStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey))
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // Untuk Android versi 10 (Q) dan di atasnya, periksa ketersediaan izin background location
                    if (ContextCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        // Izin background location sudah diberikan, maka kita dapat memulai layanan
                        if (isGPSEnabled()) {
                            // GPS aktif, maka mulai layanan lokasi
                            startLocationService()
                            binding.statusDriver.setImageResource(R.drawable.ic_switch_on)
                            binding.txtStatus.text = "On"
                            binding.txtStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_color))
                        } else {
                            // GPS tidak aktif, tampilkan dialog untuk mengaktifkannya
                            requestGPSEnabling()
                        }
                    } else {
                        // Izin background location belum diberikan, maka kita perlu meminta izin kepada pengguna
                        requestBackgroundLocationPermission()
                    }
                } else {
                    // Untuk Android versi sebelum 10 (Q), tidak perlu izin background location
                    if (isGPSEnabled()) {
                        // GPS aktif, maka mulai layanan lokasi
                        startLocationService()
                        binding.statusDriver.setImageResource(R.drawable.ic_switch_on)
                    } else {
                        // GPS tidak aktif, tampilkan dialog untuk mengaktifkannya
                        requestGPSEnabling()
                    }
                }
                loading(false)
            }
        }
        return binding.root
    }

    private fun startLocationService() {
        locationServiceIntent = Intent(requireActivity(), LocationService::class.java)
        locationServiceIntent?.putExtra("ID_USER_EXTRA", sessionManager.getId().toString())
        locationServiceIntent?.putExtra("FCM_USER_EXTRA", sessionManager.getFcm().toString())
        locationServiceIntent?.putExtra("TYPE_USER_EXTRA", sessionManager.getType().toString())
        locationServiceIntent?.action = "START_LOCATION_SERVICE"
        requireActivity().startForegroundService(locationServiceIntent)
        loading(false)
    }

    private fun stopLocationService() {
        if (locationServiceIntent != null) {
            locationServiceIntent?.action = "STOP_LOCATION_SERVICE"
            requireActivity().stopService(locationServiceIntent)
            deleteLocationDataFromDatabase()
            loading(false)
        }
    }

    private fun deleteLocationDataFromDatabase() {
        val database = FirebaseDatabase.getInstance()
        val cartReference = database.reference.child("driver_active").child(sessionManager.getId().toString())
        cartReference.removeValue()
            .addOnSuccessListener {
                loading(false)
                toast("Berhasil mematikan")
            }
            .addOnFailureListener { exception ->
                loading(false)
                toast("gagal mematikan")
            }
    }

    private fun requestBackgroundLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        ) {
            // Tampilkan penjelasan mengapa kita memerlukan izin background location (opsional)
            val alertDialog = AlertDialog.Builder(requireContext())
            alertDialog.setTitle("Izin Lokasi")
            alertDialog.setMessage("Kami memerlukan izin background location untuk melacak lokasi Anda.")
            alertDialog.setPositiveButton("OK") { _, _ ->
                // Meminta izin background location
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                    REQUEST_BACKGROUND_LOCATION_PERMISSION
                )
            }
            alertDialog.show()
        } else {
            // Meminta izin background location
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                REQUEST_BACKGROUND_LOCATION_PERMISSION
            )
        }
    }

    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = requireActivity().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    // Fungsi untuk memeriksa apakah GPS aktif
    private fun isGPSEnabled(): Boolean {
        val locationManager = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    // Fungsi untuk meminta pengguna mengaktifkan GPS
    private fun requestGPSEnabling() {
        val alertDialog = AlertDialog.Builder(requireContext())
        alertDialog.setTitle("Aktifkan GPS")
        alertDialog.setMessage("Harap aktifkan GPS untuk menggunakan fitur ini.")
        alertDialog.setPositiveButton("OK") { _, _ ->
            // Buka aktivitas pengaturan lokasi agar pengguna dapat mengaktifkan GPS
            val locationSettingsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivityForResult(locationSettingsIntent, REQUEST_LOCATION_SETTINGS)
        }
        alertDialog.show()
    }

    // Implementasi dari OnMapReadyCallback
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Mengatur tampilan peta dan lainnya
        val uiSettings = mMap.uiSettings
        uiSettings.isZoomControlsEnabled = true
        uiSettings.isZoomGesturesEnabled = true
        uiSettings.isScrollGesturesEnabled = true
        uiSettings.isTiltGesturesEnabled = true
        uiSettings.isRotateGesturesEnabled = true
        uiSettings.isCompassEnabled = true

        // Meminta izin lokasi
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
        } else {
            // Request the permission
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                permissionCode
            )
        }
    }

    // Fungsi untuk mengupdate peta dengan lokasi terbaru dan menambahkan marker
    private fun updateMapLocation(latitude: Double, longitude: Double) {
        val location = LatLng(latitude, longitude)

        currentLocationMarker?.remove()
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
    }

    private fun loading(isLoading: Boolean) {
        if (isLoading) {
            progressDialog.setMessage("Tunggu sebentar...")
            progressDialog.setCancelable(false)
            progressDialog.show()
        } else {
            progressDialog.dismiss()
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        requireActivity().unregisterReceiver(orderReceiver)
    }
}
