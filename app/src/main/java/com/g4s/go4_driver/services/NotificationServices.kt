package com.g4s.go4_driver.services

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavDeepLinkBuilder
import com.g4s.go4_driver.R
import com.g4s.go4_driver.ui.activity.MainActivity
import com.g4s.go4_driver.ui.fragment.HomeFragment

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class NotificationServices : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Handle the incoming FCM message here
        val notificationTitle = remoteMessage.notification?.title
        val notificationBody = remoteMessage.notification?.body
        val bookingId = remoteMessage.data["booking_id"]

        // Display the notification to the user
        if (notificationTitle != null && notificationBody != null) {
            if (bookingId != null) {
                // Navigasi ke BookingActionFragment dengan data
                val bundle = Bundle()
                bundle.putString("booking_id", bookingId)
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("fragment_bundle", bundle)
                intent.action = "OPEN_BOOKING_ACTION_FRAGMENT" // Tambahkan baris ini
                val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)
                val notificationBuilder = NotificationCompat.Builder(this, "channel_id")
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle("New Booking Request")
                    .setContentText("You have a new booking request. ID: $bookingId")
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)

                val notificationManager = NotificationManagerCompat.from(this)
                notificationManager.notify(0, notificationBuilder.build())
            } else {
                // Notifikasi tanpa tindakan
//                showNotification(notificationTitle, notificationBody)
            }
        }
    }

    private fun showNotification(title: String, message: String) {
        val notificationBuilder = NotificationCompat.Builder(this, "channel_id")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.notify(0, notificationBuilder.build())
    }
}
