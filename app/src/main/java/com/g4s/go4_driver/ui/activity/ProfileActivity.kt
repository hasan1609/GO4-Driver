package com.g4s.go4_driver.ui.activity

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.g4s.go4_driver.R
import com.g4s.go4_driver.databinding.ActivityProfileBinding
import com.g4s.go4_driver.model.ResponseCekBooking
import com.g4s.go4_driver.session.SessionManager
import com.g4s.go4_driver.utils.AlertOrderUtils
import com.g4s.go4_driver.webservice.ApiClient
import com.google.gson.Gson
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast

class ProfileActivity : AppCompatActivity() {
    lateinit var binding: ActivityProfileBinding
    lateinit var sessionManager: SessionManager
    var api = ApiClient.instance()
    private lateinit var alertOrderUtils: AlertOrderUtils

    // Register receiver for location updates
    private val orderReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val responseDataJson = intent!!.getStringExtra("response_data")
            val responseData = Gson().fromJson(responseDataJson, ResponseCekBooking::class.java)
            alertOrderUtils.showAlertDialog(responseData)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_profile)
        binding.lifecycleOwner = this
        sessionManager = SessionManager(this)
        val filter2 = IntentFilter("CEK_BOOKING")
        this.registerReceiver(orderReceiver, filter2)
        binding.txtDriver.text = sessionManager.getNamaDriver().toString().uppercase()
        binding.ulasan.setOnClickListener {
            startActivity<UlasanActivity>()
        }
        binding.logout.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setMessage("Logout ? ")
            builder.setPositiveButton("Ok") { dialog, which ->
                sessionManager.clearSession()
                startActivity<LoginActivity>()
                toast("Berhasil Logout")
                this.finish()
            }

            builder.setNegativeButton("Cancel ?") { dialog, which ->
                dialog.dismiss()
            }

            builder.show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        this.unregisterReceiver(orderReceiver)
    }
}