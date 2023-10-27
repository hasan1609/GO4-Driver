package com.g4s.go4_driver.ui.activity

import android.Manifest
import android.app.Activity
import android.app.ActivityManager
import android.app.AlertDialog
import android.content.*
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.g4s.go4_driver.R
import com.g4s.go4_driver.services.CekBookingService
import com.g4s.go4_driver.services.LocationService
import com.g4s.go4_driver.session.SessionManager
import com.g4s.go4_driver.ui.fragment.ChatFragment
import com.g4s.go4_driver.ui.fragment.HomeFragment
import com.g4s.go4_driver.ui.fragment.PendapatanFragment
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.jetbrains.anko.toast

class MainActivity : AppCompatActivity() {
    private lateinit var sessionManager: SessionManager
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
            }
            false
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sessionManager = SessionManager(this)
        val navView: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)
        // Memulai service cek booking dengan parameter user_id
        val serviceIntent = Intent(this, CekBookingService::class.java)
        if (!isMyServiceRunning(CekBookingService::class.java)) {
            startService(serviceIntent)
        }
        serviceIntent.putExtra("user_id", sessionManager.getId())
        startService(serviceIntent)
        moveToFragment(HomeFragment())
    }

    private fun moveToFragment(fragment: Fragment) {
        val fragmentTrans = supportFragmentManager.beginTransaction()
        fragmentTrans.replace(R.id.flFragment, fragment)
        fragmentTrans.commit()
    }

    // Fungsi untuk memeriksa apakah layanan berjalan atau tidak
    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val services = manager.getRunningServices(Integer.MAX_VALUE)
        for (service in services) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }
}