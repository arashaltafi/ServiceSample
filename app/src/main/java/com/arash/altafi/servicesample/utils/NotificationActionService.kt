package com.arash.altafi.servicesample.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationActionService : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        context?.sendBroadcast(
            Intent(Constants.KEY_IN_FORCE_STOP)
                .putExtra(
                    Constants.KEY_IN_FORCE_STOP,
                    intent?.action)
        )
    }

}