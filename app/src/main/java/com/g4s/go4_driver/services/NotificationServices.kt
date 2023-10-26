package com.g4s.go4_driver.services

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavDeepLinkBuilder
import com.g4s.go4_driver.R
import com.g4s.go4_driver.model.NotificationEvent
import com.g4s.go4_driver.session.SessionManager
import com.g4s.go4_driver.ui.activity.MainActivity
import com.g4s.go4_driver.ui.activity.ProfileActivity
import com.g4s.go4_driver.ui.activity.TrackingOrderActivity
import com.g4s.go4_driver.ui.fragment.HomeFragment
import org.greenrobot.eventbus.EventBus

const val channel_id="Pesanan"
class NotificationServices : FirebaseMessagingService() {

    private lateinit var sessionManager: SessionManager
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        // Handle the incoming FCM message here
        val notificationTitle = remoteMessage.notification?.title
        val notificationBody = remoteMessage.notification?.body
        val bookingId = remoteMessage.data["booking_id"]

        // Display the notification to the user
        if (notificationTitle != null && notificationBody != null) {
            if (bookingId != null) {
                val bundle = Bundle()
                sessionManager = SessionManager(this)
                sessionManager.isNotificationBooking(true)

                // Membuat Notification Channel (Saluran Notifikasi) jika Android Oreo atau versi yang lebih baru
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val channelId = "booking-channel"
                    val channelName = "Booking Notifications"
                    val importance = NotificationManager.IMPORTANCE_HIGH
                    val channel = NotificationChannel(channelId, channelName, importance)
                    val notificationManager = getSystemService(NotificationManager::class.java)
                    notificationManager.createNotificationChannel(channel)
                }
//
                val intent= Intent(this,MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                val pendingIntent=PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_IMMUTABLE)

                val notificationBuilder = NotificationCompat.Builder(this, channel_id)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle(notificationTitle)
                    .setContentText(notificationBody)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)
                    .build()

                EventBus.getDefault().post(NotificationEvent(notificationTitle, notificationBody, bookingId))
                val notificationManager = NotificationManagerCompat.from(this)
                val notificationId = System.currentTimeMillis().toInt()
                notificationManager.notify(notificationId, notificationBuilder)
            }
        }
    }
}
