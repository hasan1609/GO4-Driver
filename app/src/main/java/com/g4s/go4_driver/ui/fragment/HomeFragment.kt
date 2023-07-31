package com.g4s.go4_driver.ui.fragment

import android.Manifest
import android.app.ActivityManager
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.g4s.go4_driver.R
import com.g4s.go4_driver.databinding.FragmentHomeBinding
import com.g4s.go4_driver.services.LocationService
import com.g4s.go4_driver.session.SessionManager
import com.google.firebase.database.*
import org.jetbrains.anko.support.v4.toast

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    lateinit var sessionManager: SessionManager
    private val REQUEST_BACKGROUND_LOCATION_PERMISSION = 1005
    private val REQUEST_LOCATION_SETTINGS = 1004

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
        binding.lifecycleOwner = this
        sessionManager = SessionManager(requireActivity())

        val database = FirebaseDatabase.getInstance()
        val locationRef = database.getReference("locations").child(sessionManager.getId().toString())

        // Tambahkan listener untuk memeriksa keberadaan idUser di database
        locationRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Jika data untuk idUser tersedia, aktifkan Switch
                if (snapshot.exists()) {
                    binding.statusDriver.setImageResource(R.drawable.ic_switch_on)
                    startLocationService()
                } else {
                    binding.statusDriver.setImageResource(R.drawable.ic_switch_off)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error (optional)
            }
        })

        binding.statusDriver.setOnClickListener {
            if (isServiceRunning(LocationService::class.java)) {
                // Layanan sedang berjalan, berarti kita akan mematikan layanan
                stopLocationService()
                binding.statusDriver.setImageResource(R.drawable.ic_switch_off)
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // Untuk Android versi 10 (Q) dan di atasnya, periksa ketersediaan izin background location
                    if (ContextCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        // Izin background location sudah diberikan, maka kita dapat memulai layanan
                        startLocationService()
                        binding.statusDriver.setImageResource(R.drawable.ic_switch_on)
                    } else {
                        // Izin background location belum diberikan, maka kita perlu meminta izin kepada pengguna
                        requestBackgroundLocationPermission()
                    }
                } else {
                    // Untuk Android versi sebelum 10 (Q), tidak perlu izin background location
                    startLocationService()
                    binding.statusDriver.setImageResource(R.drawable.ic_switch_on)
                }
            }
        }
        return binding.root
    }

    private fun startLocationService() {
        val serviceIntent = Intent(requireActivity(), LocationService::class.java)
        serviceIntent.putExtra("ID_USER_EXTRA", sessionManager.getId().toString())
        serviceIntent.action = "START_LOCATION_SERVICE"
        requireActivity().startForegroundService(serviceIntent)
    }

    private fun stopLocationService() {
        val stopServiceIntent = Intent(requireActivity(), LocationService::class.java)
        stopServiceIntent.action = "STOP_LOCATION_SERVICE"
        requireActivity().stopService(stopServiceIntent)
        deleteLocationDataFromDatabase()
    }

    private fun deleteLocationDataFromDatabase() {
        val database = FirebaseDatabase.getInstance()
        val cartReference = database.reference.child("locations").child(sessionManager.getId().toString())
        cartReference.removeValue()
            .addOnSuccessListener {
                toast("Berhasil mematikan")
            }
            .addOnFailureListener { exception ->
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
}
