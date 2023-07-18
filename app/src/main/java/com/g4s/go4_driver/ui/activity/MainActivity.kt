package com.g4s.go4_driver.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.g4s.go4_driver.R
import com.g4s.go4_driver.ui.fragment.HomeFragment
import com.g4s.go4_driver.ui.fragment.ProfileFragment
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
                R.id.nav_profil -> {
                    supportFragmentManager.beginTransaction().replace(
                        R.id.flFragment,
                        ProfileFragment()
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
        moveToFragment(HomeFragment())
//        navController = Navigation.findNavController(this, R.id.restoNavHostFragment)
//        NavigationUI.setupWithNavController(bottomNavigationView, navController)
    }

    private fun moveToFragment(fragment: Fragment) {
        val fragmentTrans = supportFragmentManager.beginTransaction()
        fragmentTrans.replace(R.id.flFragment, fragment)
        fragmentTrans.commit()
    }
}