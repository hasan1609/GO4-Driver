package com.g4s.go4_driver.ui.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.g4s.go4_driver.R
import com.g4s.go4_driver.databinding.FragmentHomeBinding
import com.g4s.go4_driver.services.LocationService
import com.google.firebase.database.*
import org.jetbrains.anko.support.v4.stopService
import org.jetbrains.anko.support.v4.toast

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var database: DatabaseReference
    private lateinit var locationListener: ValueEventListener

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
        binding.lifecycleOwner = this

        // Dapatkan data latitude dan longitude dari database Firebase
        database = FirebaseDatabase.getInstance().reference.child("locations").child("id_user")
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (isAdded) { // Check if the Fragment is still attached to the Activity
                    if (dataSnapshot.exists()) {
                        binding.statusDriver.isChecked = true
//                        startLocationService()
                        val latitude = dataSnapshot.child("latitude").getValue(Double::class.java)
                        val longitude = dataSnapshot.child("longitude").getValue(Double::class.java)

                        binding.lat.text = latitude?.toString()
                        binding.longi.text = longitude?.toString()
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error if needed
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
        val serviceIntent = Intent(requireContext(), LocationService::class.java)
        requireContext().startForegroundService(serviceIntent)
    }

    private fun stopLocationService() {
        val serviceIntent = Intent(requireContext(), LocationService::class.java)
        requireContext().stopService(serviceIntent)
        deleteLocationDataFromDatabase()
    }

    private fun deleteLocationDataFromDatabase() {
        val database = FirebaseDatabase.getInstance()
        val userId = "id_user" // Ganti dengan ID pengguna yang sesuai

        val cartReference = database.reference.child("locations").child(userId)
        cartReference.removeValue()
            .addOnSuccessListener {
                toast("Berhasil mematikan")
            }
            .addOnFailureListener { exception ->
                toast("gagal mematikan")
            }
    }
}
