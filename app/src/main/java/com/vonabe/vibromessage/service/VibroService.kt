package com.vonabe.vibromessage.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.vonabe.vibromessage.R
import com.vonabe.vibromessage.utils.Ext.commandToLong
import com.vonabe.vibromessage.utils.Ext.commandToPattern
import com.vonabe.vibromessage.utils.Ext.getDelay

class VibroService : Service() {

    private val notificationId = 100

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val messageExtra = intent?.getStringExtra("message")

        // Здесь обрабатываем входящие данные
        if (messageExtra != null) {
            val notification = createNotification()
            startForeground(notificationId, notification)

            // Обработка уведомлений
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(messageExtra.commandToPattern())
                Handler(Looper.getMainLooper()).postDelayed({
                    closeService()
                }, messageExtra.getDelay() + 1000)
            } else {
                val commandToPattern = messageExtra.commandToLong()
                vibrator.vibrate(commandToPattern, -1)
                Handler(Looper.getMainLooper()).postDelayed({
                    closeService()
                }, messageExtra.getDelay() + 1000)
            }
        } else error("Message Notification null")

        return START_NOT_STICKY
    }

    private fun closeService() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(notificationId)  // Удаляет уведомление по его ID
    }

    private fun createNotification(): Notification {
        val channelId = getString(R.string.default_notification_channel_id)
        val channelName = getString(R.string.default_notification_channel_name)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("App is running")
            .setSmallIcon(R.drawable.sticker)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = getString(R.string.default_notification_channel_id)
            val channelName = getString(R.string.default_notification_channel_name)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, channelName, importance)
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(title: String, message: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }

        val notificationBuilder = NotificationCompat.Builder(this, getString(R.string.default_notification_channel_id))
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setAutoCancel(true)

        notificationManager.notify(0, notificationBuilder.build())
    }

    // Get Vibrator service
    private val vibrator: Vibrator by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION") getSystemService(VIBRATOR_SERVICE) as Vibrator
        }
    }

    private fun String.toast() {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(this@VibroService, this, Toast.LENGTH_LONG).show()
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}