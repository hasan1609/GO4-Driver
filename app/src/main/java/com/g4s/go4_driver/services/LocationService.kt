package com.g4s.go4_driver.services

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.g4s.go4_driver.ui.activity.MainActivity
import com.g4s.go4_driver.R
import com.google.android.gms.location.*
import com.google.firebase.database.FirebaseDatabase

class LocationService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        buildLocationRequest()
        createNotificationChannel()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                p0.let {
                    val location = it.lastLocation
                    if (location != null) {
                        // Simpan data lokasi ke Firebase Realtime Database
                        saveLocationToDatabase(location.latitude, location.longitude)
                    }
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startLocationUpdates()

        // Memulai layanan sebagai foreground service
        val notification: Notification = buildNotification()
        startForeground(123, notification)

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


    private fun buildLocationRequest() {
        locationRequest = LocationRequest.create().apply {
            interval = 10000 // Interval update lokasi dalam milisekon (contoh: 10 detik)
            fastestInterval = 5000 // Interval tercepat untuk update lokasi dalam milisekon (contoh: 5 detik)
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Lakukan pengecekan izin lokasi di sini jika diperlukan
            return
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun saveLocationToDatabase(latitude: Double, longitude: Double) {
        // Simpan data lokasi ke Firebase Realtime Database
        val database = FirebaseDatabase.getInstance()
        val locationRef = database.getReference("locations").child("id_user")
        locationRef.child("latitude").setValue(latitude)
        locationRef.child("longitude").setValue(longitude)
    }

    // Membuat saluran notifikasi (Notification Channel) jika perangkat menggunakan Android Oreo atau versi yang lebih tinggi
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "channel_id",
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // Membangun notifikasi yang akan digunakan untuk foreground service
    private fun buildNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java) // Ganti YourActivity dengan activity tujuan notifikasi
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)

        val notification = NotificationCompat.Builder(this, "channel_id")
            .setContentTitle("Foreground Service")
            .setContentText("Service berjalan di latar belakang")
            .setSmallIcon(R.drawable.ic_home) // Ganti dengan ikon notifikasi yang sesuai
            .setContentIntent(pendingIntent)
            .build()

        return notification
    }
}
