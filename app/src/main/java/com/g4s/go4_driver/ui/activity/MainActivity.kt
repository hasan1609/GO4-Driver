package com.g4s.go4_driver.ui.activity

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.*
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.g4s.go4_driver.R
import com.g4s.go4_driver.services.LocationService
import com.g4s.go4_driver.ui.fragment.ChatFragment
import com.g4s.go4_driver.ui.fragment.HomeFragment
import com.g4s.go4_driver.ui.fragment.PendapatanFragment
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.jetbrains.anko.toast

class MainActivity : AppCompatActivity() {
    private val locationPermissionCode = 1001
    private val onNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    supportFragmentManager.beginTransaction().replace(
                        R.id.flFragment,
                        HomeFragment()
                    ).commit()
                    return@OnNavigationItemSelectedListener true
                }
                R.id.nav_pendapatan -> {
                    supportFragmentManager.beginTransaction().replace(
                        R.id.flFragment,
                        PendapatanFragment()
                    ).commit()
                    return@OnNavigationItemSelectedListener true
                }
                R.id.nav_chat -> {
                    supportFragmentManager.beginTransaction().replace(
                        R.id.flFragment,
                        ChatFragment()
                    ).commit()
                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)
        if (intent?.action == "OPEN_BOOKING_ACTION_FRAGMENT") {
            val fragmentBundle = intent.getBundleExtra("fragment_bundle")
            if (fragmentBundle != null) {
                moveToFragment(HomeFragment().apply {
                    arguments = fragmentBundle
                })
            }
        } else {
            moveToFragment(HomeFragment())
        }
    }

    private fun moveToFragment(fragment: Fragment) {
        val fragmentTrans = supportFragmentManager.beginTransaction()
        fragmentTrans.replace(R.id.flFragment, fragment)
        fragmentTrans.commit()
    }

}