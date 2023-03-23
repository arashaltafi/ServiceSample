package com.arash.altafi.servicesample.background

import android.app.Activity
import android.app.Service
import android.content.Intent
import android.os.CountDownTimer
import android.os.IBinder
import android.util.Log

class MyBackgroundService : Service() {

    private val countDownTimer = object : CountDownTimer(
        30 * 60 * 1000L,
        1000
    ) {
        override fun onFinish() {
            Log.d(TAG, "onFinish")
        }

        override fun onTick(millisUntilFinished: Long) {
            Log.d(TAG, "onTick remained:$millisUntilFinished")
        }
    }

    override fun onCreate() {
        super.onCreate()
        // perform any initialization here
        Log.i(TAG, "onCreate")

        countDownTimer.start()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // perform any long-running operation here
        Log.i(TAG, "onStartCommand")

        // stop the service when it's done
        stopSelf()

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        // return null as this service does not provide binding
        Log.i(TAG, "onBind")
        return null
    }

    companion object {
        private const val TAG = "MyBackgroundService"

        fun startService(activity: Activity) {
            val i = Intent(activity, MyBackgroundService::class.java)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            activity.startService(i)
        }
    }

}