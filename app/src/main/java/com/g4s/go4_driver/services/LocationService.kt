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
import androidx.navigation.NavDeepLinkBuilder
import com.g4s.go4_driver.ui.activity.MainActivity
import com.g4s.go4_driver.R
import com.google.android.gms.location.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

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
                        updateLocationToDatabase(location.latitude, location.longitude)

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
                cekData(idUser, type.toString(), fcmUser.toString()) // Ganti nilai status sesuai kebutuhan
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

    private fun updateLocationToDatabase(latitude: Double, longitude: Double) {
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
                "foreground",
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // Membangun notifikasi yang akan digunakan untuk foreground service
    private fun buildNotification(latitude: Double, longitude: Double): Notification {
        // Menggunakan NavDeepLinkBuilder untuk mengarahkan ke HomeFragment dengan data
        val pendingIntent = NavDeepLinkBuilder(this)
            .setGraph(R.navigation.driver_navigation)
            .setDestination(R.id.homeFragment)
            .createPendingIntent()

        val notificationText = "Latitude: $latitude, Longitude: $longitude"
        val notification = NotificationCompat.Builder(this, "foreground")
            .setContentTitle("Foreground Service")
            .setContentText(notificationText)
            .setSmallIcon(R.drawable.ic_home) // Ganti dengan ikon notifikasi yang sesuai
            .setContentIntent(pendingIntent)
            .build()

        return notification
    }

    private fun cekData(idUser: String?, type: String, fcm: String) {
        val database = FirebaseDatabase.getInstance()
        val userRef = database.getReference("driver_active").child(idUser!!)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Data sudah ada di Firebase, tidak perlu melakukan apa pun
                    startLocationUpdates()
                } else {
                    // Data belum ada di Firebase, simpan data baru
                    saveLocationInFirebase(idUser, type, fcm)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Penanganan kesalahan saat mengambil data dari Firebase
            }
        })
    }

    private fun saveLocationInFirebase(idUser: String?, type: String, fcm: String) {
        val database = FirebaseDatabase.getInstance()
        val userRef = database.getReference("driver_active").child(idUser!!)
        val userData = HashMap<String, Any>()
        userData["latitude"] = 0.0 // Nilai awal latitude
        userData["longitude"] = 0.0 // Nilai awal longitude
        userData["status"] = "active"
        userData["type"] = type
        userData["fcm"] = fcm

        // Simpan data lokasi baru di Firebase
        userRef.setValue(userData).addOnSuccessListener {
            val notification: Notification = buildNotification(0.0, 0.0)
            startLocationUpdates()
        }.addOnFailureListener {
            // Penanganan kesalahan jika data tidak dapat disimpan
        }
    }
}
