package com.g4s.go4_driver.ui.fragment

import android.Manifest
import android.app.Activity
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
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.g4s.go4_driver.R
import com.g4s.go4_driver.databinding.FragmentHomeBinding
import com.g4s.go4_driver.services.LocationService
import com.google.firebase.database.*
import org.jetbrains.anko.support.v4.toast

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var database: DatabaseReference
    private lateinit var locationListener: ValueEventListener
    private val idUser = "id_user"
    private val REQUEST_BACKGROUND_LOCATION_PERMISSION = 1005
    private val REQUEST_LOCATION_SETTINGS = 1004

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
        binding.lifecycleOwner = this

        val database = FirebaseDatabase.getInstance()
        val locationRef = database.getReference("locations").child(idUser)

        // Tambahkan listener untuk memeriksa keberadaan idUser di database
        locationRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Jika data untuk idUser tersedia, aktifkan Switch
                if (snapshot.exists()){
                    binding.statusDriver.isChecked = true
                    startLocationService()
                }else{
                    binding.statusDriver.isChecked = false
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error (optional)
            }
        })

        binding.statusDriver.setOnClickListener {
            if (binding.statusDriver.isChecked) {
                startLocationService()
            } else {
                stopLocationService()
            }
        }
        return binding.root
    }

    private fun startLocationService() {
        val serviceIntent = Intent(requireActivity(), LocationService::class.java)
        serviceIntent.putExtra("ID_USER_EXTRA", idUser)
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
        val cartReference = database.reference.child("locations").child(idUser)
        cartReference.removeValue()
            .addOnSuccessListener {
                toast("Berhasil mematikan")
            }
            .addOnFailureListener { exception ->
                toast("gagal mematikan")
            }
    }
}
