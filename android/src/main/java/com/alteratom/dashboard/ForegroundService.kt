package com.alteratom.dashboard

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_MIN
import androidx.core.app.NotificationCompat.VISIBILITY_SECRET
import androidx.lifecycle.LifecycleService
import com.alteratom.R
import com.alteratom.dashboard.activity.MainActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull


class ForegroundService : LifecycleService() {

    var finishAndRemoveTask: () -> Unit = {}
    var isStarted = false

    companion object {
        var service: ForegroundService? = null

        //Stop service and activity
        fun shut(activity: MainActivity) {
            Intent(activity, ForegroundService::class.java).also {
                it.action = "SHUT"
                activity.startForegroundService(it)
            }
        }

        //Stop service but not the activity
        fun stop(context: Context) {
            Intent(context, ForegroundService::class.java).also {
                it.action = "STOP"
                context.startForegroundService(it)
            }
        }

        fun start(context: Context) {
            Intent(context, ForegroundService::class.java).also {
                it.action = "START"
                context.startForegroundService(it)
            }

        }

        //Wait for service
        suspend fun haltForService() = withTimeoutOrNull(10000) {
            while (true) {
                if (service?.isStarted == true) break
                else delay(100)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        val intent = Intent(this, ForegroundService::class.java)
        intent.action = "SHUT"

        val pendingIntent = PendingIntent.getService(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val notification =
            NotificationCompat.Builder(this, "foreground_service_id")
                .setContentTitle("Server working in background")
                .setSmallIcon(R.drawable.ic_icon)
                .setPriority(PRIORITY_MIN)
                .addAction(R.drawable.ic_trashcan, "stop working in background", pendingIntent)
                .setVisibility(VISIBILITY_SECRET)
                .setSilent(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(1, notification.build(), FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(1, notification.build())
        }

        service = this
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ""

        if (action == "START") isStarted = true
        else {
            isStarted = false

            if (action == "SHUT") finishAndRemoveTask()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            "foreground_service_id",
            "Server service notification",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "com/alteratom/notification_channel"
        }

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}