package com.g4s.go4_driver.services

import android.app.AlertDialog
import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.util.Log
import com.g4s.go4_driver.model.ResponseCekBooking
import com.g4s.go4_driver.webservice.ApiClient
import com.google.gson.Gson
import kotlinx.android.synthetic.main.custom_alert_recive_booking.view.*

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CekBookingService : Service() {
    private val apiService = ApiClient.instance()
    private val handler = Handler()
    private var userId: String? = null

    private val apiCallback = object : Callback<ResponseCekBooking> {
        override fun onResponse(call: Call<ResponseCekBooking>, response: Response<ResponseCekBooking>) {
            if (response.isSuccessful) {
                val responseData = response.body()
                if (responseData != null) {
                    // Respon tidak null, tampilkan alert
                    Log.e("kenek", responseData.toString())

                    // Kirim siaran lokal dengan data respon
                    val intent = Intent("CEK_BOOKING")
                    intent.putExtra("response_data", Gson().toJson(responseData))
                    sendBroadcast(intent)
                } else {
                    Log.e("ApiServiceManager", "API response is null")
                }
            } else {
                Log.e("ApiServiceManager", userId.toString())
            }
        }

        override fun onFailure(call: Call<ResponseCekBooking>, t: Throwable) {
            Log.e("ApiServiceManager", "API Call failed with exception: ${t.message}")
        }
    }

    private val runnable = object : Runnable {
        override fun run() {
            apiService.cekBooking(userId.toString()).enqueue(apiCallback)
            handler.postDelayed(this, 10000) // Pemanggilan setiap 10 detik
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("ApiServiceManager", "Service onCreate()") // Log saat layanan diciptakan
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            userId = intent.getStringExtra("user_id") // Mengambil user_id dari Intent
        }
        handler.post(runnable)
        return START_STICKY
    }

    override fun onDestroy() {
        handler.removeCallbacks(runnable)
        super.onDestroy()
        Log.d("ApiServiceManager", "Service onDestroy()") // Log saat layanan dihentikan
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}