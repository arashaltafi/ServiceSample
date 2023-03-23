package com.arash.altafi.servicesample.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.arash.altafi.servicesample.R

object NotificationUtils {

    fun sendNotification(
        context: Context,
        channelId: String,
        title: String,
        body: String
    ): Notification {
        val intentClose = Intent(
            context,
            NotificationActionService::class.java
        ).setAction(Constants.KEY_IN_FORCE_STOP)
        val pendingClose = PendingIntent.getBroadcast(
            context,
            0,
            intentClose,
            (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE
            else 0x0)
                    or
                    PendingIntent.FLAG_UPDATE_CURRENT
        )

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setChannelId(channelId)
            .setSmallIcon(R.drawable.ic_baseline_notifications_active_24)
            .setBadgeIconType(NotificationCompat.BADGE_ICON_LARGE)
            .setContentTitle(Utils.setPersianDigits(title))
            .setContentText(Utils.setPersianDigits(body))
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .setSound(defaultSoundUri)
            .setOngoing(true)
            .addAction(
                R.drawable.ic_close,
                "Cancel",
                pendingClose
            )

        return notificationBuilder.build()
    }

    /**
     * create channel if is not created
     *
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun createChannel(
        context: Context,
        channelId: String,
        name: String,
        descriptionText: String = "",
        importance: Int = NotificationManager.IMPORTANCE_DEFAULT,
        visibility: Int = Notification.VISIBILITY_PUBLIC,
        groupId: String? = null,
        soundUri: Uri? = null,
        showBadge: Boolean = true,
    ) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val isChannelCreated = try {
            val notificationChannel = notificationManager.getNotificationChannel(channelId)
            Log.d(
                "notificationUtils",
                "channel is created= ${notificationChannel.name}"
            ) // don't remove
            true
        } catch (e: Exception) {
            false
        }

        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_ALARM)
            .build()
        val soundURI = soundUri ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        if (isChannelCreated.not()) {
            val mChannel = NotificationChannel(channelId, name, importance).apply {
                groupId?.let {
                    group = it
                }
                description = descriptionText
                lockscreenVisibility = visibility
                when (importance) {
                    NotificationManager.IMPORTANCE_HIGH,
                    NotificationManager.IMPORTANCE_DEFAULT -> {
                        setSound(soundURI, audioAttributes)
                    }
                    NotificationManager.IMPORTANCE_LOW -> {
                        if (soundUri != null)
                            setSound(soundURI, audioAttributes)
                    }
                }
                setShowBadge(showBadge)
            }
            notificationManager.createNotificationChannel(mChannel)
        }

    }

}