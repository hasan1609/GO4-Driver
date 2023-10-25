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
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.g4s.go4_driver.R
import com.g4s.go4_driver.databinding.FragmentHomeBinding
import com.g4s.go4_driver.model.NotificationEvent
import com.g4s.go4_driver.model.ResponseBooking
import com.g4s.go4_driver.services.LocationService
import com.g4s.go4_driver.session.SessionManager
import com.g4s.go4_driver.ui.activity.ProfileActivity
import com.g4s.go4_driver.ui.activity.TrackingOrderActivity
import com.g4s.go4_driver.webservice.ApiClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.custom_alert_recive_booking.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.support.v4.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment(), AnkoLogger, OnMapReadyCallback {

    private lateinit var binding: FragmentHomeBinding
    lateinit var sessionManager: SessionManager
    var api = ApiClient.instance()
    private lateinit var progressDialog: ProgressDialog
    private val permissionCode = 101
    private val REQUEST_BACKGROUND_LOCATION_PERMISSION = 1005
    private val REQUEST_LOCATION_SETTINGS = 1004
    private lateinit var mMap: GoogleMap // Google Maps objek
    private var locationServiceIntent: Intent? = null // Intent untuk LocationService
    private var currentLocationMarker: Marker? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
        binding.lifecycleOwner = this
        sessionManager = SessionManager(requireActivity())
        progressDialog = ProgressDialog(requireActivity())
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
                    binding.txtStatus.text = "On"
                    binding.txtStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_color))
                    startLocationService()
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

    private fun checkBooking(idBooking: String) {
        api.getBookingById(idBooking).enqueue(object : Callback<ResponseBooking> {
            override fun onResponse(
                call: Call<ResponseBooking>,
                response: Response<ResponseBooking>
            ) {
                try {
                    if (response.isSuccessful) {
                        val data = response.body()
                        if (data!!.status == true) {
                            showAlertReciveOrder(
                                data.data!!.idOrder.toString(),
                                data.data.idOrder.toString(),
                                data.data.total.toString(),
                                data.jarak.toString(),
                                data.data.alamatTujuan.toString(),
                            )
                        }
                    } else {
                        toast("gagal mendapatkan response")
                    }
                } catch (e: Exception) {
                    info { "hasan ${e.message}" }
                    toast(e.message.toString())
                }
            }

            override fun onFailure(call: Call<ResponseBooking>, t: Throwable) {
                if (isAdded) {
                    info { "hasan ${t.message}" }
                    toast(t.message.toString())
                }
            }
        })
    }

    // menampilkan alert order masuk
    private fun showAlertReciveOrder(
        kdBooking: String,
        jenis: String,
        total: String,
        jarak: String,
        tujuan: String,
    ) {
        sessionManager.isNotificationBooking(false)
        val builder = AlertDialog.Builder(context)
        val dialogView = LayoutInflater.from(context).inflate(R.layout.custom_alert_recive_booking, null)

        val txtKode = dialogView.txt_kode
        val txtJenisOrder = dialogView.txt_jenis_order
        val txtTotal = dialogView.txt_total
        val txtJarak = dialogView.txt_jarak
        val txtTujuan = dialogView.txt_tujuan
        val btnTerima = dialogView.btn_terima
        val btnTolak = dialogView.btn_tolak

        txtKode.text = kdBooking
        txtJenisOrder.text = jenis
        txtTotal.text = total
        txtJarak.text = jarak + "KM"
        txtTujuan.text = tujuan

        // Menambahkan view yang telah diinisialisasi ke dalam builder
        builder.setView(dialogView)

        // Membuat dan menampilkan alert dialog
        val alertDialog = builder.create()
        alertDialog.show()

        // Mengatur aksi ketika tombol Terima diklik
        btnTerima.setOnClickListener {
            // Lakukan aksi yang diinginkan ketika tombol Terima diklik
            alertDialog.dismiss() // Tutup alert dialog setelah aksi selesai
            startActivity<TrackingOrderActivity>("kdBooking" to kdBooking)
        }

        // Mengatur aksi ketika tombol Tolak diklik
        btnTolak.setOnClickListener {
            // Lakukan aksi yang diinginkan ketika tombol Tolak diklik
            alertDialog.dismiss() // Tutup alert dialog setelah aksi selesai
        }
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Daftarkan diri sebagai penerima EventBus
        EventBus.getDefault().register(this)
    }
    
    override fun onDestroyView() {
        // Unregister penerima siaran saat fragment dihancurkan
        EventBus.getDefault().unregister(this)
        super.onDestroyView()
    }
    // Terima notifikasi yang dikirim dari NotificationServices melalui EventBus
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onNotificationReceived(notificationEvent: NotificationEvent) {
        val title = notificationEvent.title
        val message = notificationEvent.message
        val dataBundle = notificationEvent.data
        info("oekke $dataBundle")
        // Tampilkan alert sesuai dengan notifikasi yang diterima
//        checkBooking(dataBundle.toString())
        // Cek status notifikasi yang diklik
        val notificationClicked = sessionManager.getNotificationBooking()
        if (notificationClicked == true) {
            // Tampilkan alert karena notifikasi telah diklik
            checkBooking(dataBundle.toString())
        }
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
}
