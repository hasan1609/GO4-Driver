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
    private var idUser: String? = null
    private var fcmUser: String? = null
    private var type: String? = null

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

                        // Update the notification with the new location data
                        val notification: Notification = buildNotification(location.latitude, location.longitude)
                        // Kirim data lokasi ke HomeFragment
                        val intent = Intent("LOCATION_UPDATE")
                        intent.putExtra("latitude", location.latitude)
                        intent.putExtra("longitude", location.longitude)
                        sendBroadcast(intent)
                        startForeground(123, notification)
                    }
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "START_LOCATION_SERVICE" -> {
                // Retrieve the user ID and FCM code from the intent extra
                idUser = intent.getStringExtra("ID_USER_EXTRA")
                fcmUser = intent.getStringExtra("FCM_USER_EXTRA")
                type = intent.getStringExtra("TYPE_USER_EXTRA")

                // Memulai layanan setelah izin lokasi diberikan dan GPS diaktifkan
                startLocationUpdates()
                val notification: Notification = buildNotification(0.0, 0.0)
                startForeground(123, notification)

                // Update user data with status and kodeFcm
                updateUserData(idUser, type.toString(), fcmUser.toString()) // Ganti nilai status sesuai kebutuhan
            }
            "STOP_LOCATION_SERVICE" -> {
                // Menghentikan layanan jika tindakan "STOP_LOCATION_SERVICE" diterima
                stopLocationUpdates()
                stopForeground(true)
                stopSelf()
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun buildLocationRequest() {
        locationRequest = LocationRequest.create().apply {
            interval = 10000 // Interval update lokasi dalam milidetik (contoh: 10 detik)
            fastestInterval = 5000 // Interval tercepat untuk update lokasi dalam milidetik (contoh: 5 detik)
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

    override fun onDestroy() {
        stopLocationUpdates()
        super.onDestroy()
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun saveLocationToDatabase(latitude: Double, longitude: Double) {
        // Simpan data lokasi ke Firebase Realtime Database
        val database = FirebaseDatabase.getInstance()
        val locationRef = database.getReference("driver_active").child(idUser!!)
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
    private fun buildNotification(latitude: Double, longitude: Double): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java) // Ganti YourActivity dengan activity tujuan notifikasi
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)
        val notificationText = "Latitude: $latitude, Longitude: $longitude"
        val notification = NotificationCompat.Builder(this, "channel_id")
            .setContentTitle("Foreground Service")
            .setContentText(notificationText)
            .setSmallIcon(R.drawable.ic_home) // Ganti dengan ikon notifikasi yang sesuai
            .setContentIntent(pendingIntent)
            .build()

        return notification
    }

    private fun updateUserData(idUser: String?, type: String, fcm: String) {
        val database = FirebaseDatabase.getInstance()
        val userRef = database.getReference("driver_active").child(idUser!!)

        val userData = HashMap<String, Any>()
        userData["latitude"] = 0.0 // Nilai awal latitude
        userData["longitude"] = 0.0 // Nilai awal longitude
        userData["status"] = "active"
        userData["type"] = type
        userData["fcm"] = fcm

        userRef.updateChildren(userData)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Pembaharuan data pengguna berhasil
                } else {
                    // Pembaharuan data pengguna gagal
                }
            }
    }
}
