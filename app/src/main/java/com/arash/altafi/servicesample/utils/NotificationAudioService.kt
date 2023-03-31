package com.arash.altafi.servicesample.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationAudioService : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        context?.sendBroadcast(
            Intent(Constants.NOTIFICATION_ACTION)
                .putExtra(
                    Constants.NOTIFICATION_ACTION,
                    intent?.action)
        )
    }

}