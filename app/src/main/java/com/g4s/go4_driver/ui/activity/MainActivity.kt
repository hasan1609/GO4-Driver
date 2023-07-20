package com.g4s.go4_driver.ui.activity

import android.app.AlertDialog
import android.content.*
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.g4s.go4_driver.R
import com.g4s.go4_driver.services.LocationService
import com.g4s.go4_driver.ui.fragment.HomeFragment
import com.g4s.go4_driver.ui.fragment.TransaksiFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
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
                R.id.nav_transaksi -> {

                    supportFragmentManager.beginTransaction().replace(
                        R.id.flFragment,
                        TransaksiFragment()
                    ).commit()
                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (!isGPSEnabled()) {
            showGPSDisabledAlert()
        }
        val navView: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)
        moveToFragment(HomeFragment())
        startLocationService()
        if (intent.hasExtra("FRAGMENT_TAG")) {
            val fragmentTag = intent.getStringExtra("FRAGMENT_TAG")
            when (fragmentTag) {
                "HomeFragment" -> {
                    moveToFragment(HomeFragment())
                }
            }
        }
    }

    private fun moveToFragment(fragment: Fragment) {
        val fragmentTrans = supportFragmentManager.beginTransaction()
        fragmentTrans.replace(R.id.flFragment, fragment)
        fragmentTrans.commit()
    }

    private fun isGPSEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun showGPSDisabledAlert() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("GPS Tidak Aktif")
        builder.setMessage("Aktifkan sekarang?")
        builder.setPositiveButton("Aktifkan",
            DialogInterface.OnClickListener { _, _ ->
                startActivity(Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            })
        builder.setNegativeButton("Batal",
            DialogInterface.OnClickListener { dialogInterface, _ ->
                dialogInterface.cancel()
            })
        builder.show()
    }

    private fun startLocationService() {
        val serviceIntent = Intent(this, LocationService::class.java)
        this.startForegroundService(serviceIntent)
    }
}