package com.arash.altafi.servicesample.foreground

import android.app.Activity
import android.app.Notification
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.arash.altafi.servicesample.utils.NotificationUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class MyForegroundService : LifecycleService() {

    private var flagInternetConnected = MutableStateFlow(false)
    private var flagScreenOff = MutableStateFlow(false)
    private var flagCloseApp = MutableStateFlow(false)
    private var flagDetached = MutableStateFlow(false)

    private val lbr = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null)
                return

            Log.d(TAG, "onReceive: ${intent.extras}")

            if (intent.hasExtra(KEY_IN_SCREEN_OFF))
                flagScreenOff.tryEmit(intent.getBooleanExtra(KEY_IN_SCREEN_OFF, false))

            if (intent.hasExtra(KEY_IN_CLOSE_APP))
                flagCloseApp.tryEmit(intent.getBooleanExtra(KEY_IN_CLOSE_APP, false))

            if (intent.hasExtra(KEY_IN_FORCE_STOP))
                stopService()

            if (intent.hasExtra(KEY_IN_DETACHED))
                flagDetached.tryEmit(
                    intent.getBooleanExtra(KEY_IN_DETACHED, false)
                )

        }
    }

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            Log.i(TAG, "networkCallback onAvailable")
            flagInternetConnected.tryEmit(true)
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            Log.i(TAG, "networkCallback onLost")
            flagInternetConnected.tryEmit(false)
        }
    }

    private val countDownTimer = object : CountDownTimer(
        30 * 60 * 1000L,
        1000
    ) {
        override fun onFinish() {
            stopService()
        }

        override fun onTick(millisUntilFinished: Long) {
            Log.d(TAG, "onTick remained:$millisUntilFinished")
        }
    }

    private val broadCastReceiver = object : BroadcastReceiver() {
        override fun onReceive(contxt: Context?, intent: Intent?) {
            when (intent?.extras?.getString(KEY_IN_FORCE_STOP)) {
                KEY_IN_FORCE_STOP -> {
                    stopService()
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        // perform any initialization here
        Log.i(TAG, "onCreate")

        registerNetworkConnectivity(this)

        registerReceiver()

        registerReceiver(
            broadCastReceiver,
            IntentFilter(KEY_IN_FORCE_STOP)
        )

        lifecycleScope.launchWhenCreated {
            launch {
                flagInternetConnected.collect {
                    Log.i(TAG, "flagInternetConnected: $it")
                    if (it.not()) {
                        Toast.makeText(
                            this@MyForegroundService,
                            "internet is Not Connect",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this@MyForegroundService,
                            "internet is Connect",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            launch {
                flagCloseApp.collect {
                    Log.i(TAG, "flagCloseApp: $it")
                    if (it) {
                        Toast.makeText(
                            this@MyForegroundService,
                            "Application is Closed",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            launch {
                flagDetached.collect {
                    Log.i(TAG, "flagDetached: $it")
                    if (it)
                        createNotification()
                    else
                        this@MyForegroundService.stopForeground(true)
                }
            }

            launch {
                flagScreenOff.collect {
                    Log.i(TAG, "flagScreenOff: $it")
                    if (it) {
                        Toast.makeText(
                            this@MyForegroundService,
                            "Screen is OFF",
                            Toast.LENGTH_SHORT
                        ).show()
                        countDownTimer.start()
                    } else {
                        Toast.makeText(this@MyForegroundService, "Screen is ON", Toast.LENGTH_SHORT)
                            .show()
                        countDownTimer.cancel()
                    }
                }
            }
        }
    }

    private fun stopService() {
        Log.e(TAG, "stopService")
        destroyService()
    }

    private fun createNotification() {
        Log.i(TAG, "createNotification")
        val channelId: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationUtils.createChannel(
                context = this,
                channelId = NOTIFICATION_CHANNEL_ID,
                name = NOTIFICATION_CHANNEL_NAME,
                importance = NotificationManager.IMPORTANCE_NONE,
                visibility = Notification.VISIBILITY_PRIVATE,
                soundUri = null,
                showBadge = false
            )
            NOTIFICATION_CHANNEL_ID
        } else {
            ""
        }
        val notification = NotificationUtils.sendNotification(
            this,
            channelId,
            "MyForegroundService",
            "Running..."
        )
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun registerNetworkConnectivity(context: Context) {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager.registerDefaultNetworkCallback(networkCallback)
        } else {
            val builder = NetworkRequest.Builder()
            connectivityManager.registerNetworkCallback(builder.build(), networkCallback)
        }
    }

    private fun unregisterNetworkConnectivity(context: Context) {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    private fun registerReceiver() {
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(lbr, IntentFilter(INTENT_FILTER_IN))
    }

    private fun unregisterReceiver() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(lbr)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        // start the foreground service with a notification
        Log.i(TAG, "onStartCommand")


        // perform any long-running operation here

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        // clean up any resources here
        Log.i(TAG, "onDestroy")
        unregisterNetworkConnectivity(this)
        try {
            unregisterReceiver(broadCastReceiver)
        } catch (_: Exception) {
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        // return null as this service does not provide binding
        Log.i(TAG, "onBind")
        return null
    }

    private fun destroyService() {
        Log.e(TAG, "destroyService")
        unregisterReceiver()
        unregisterNetworkConnectivity(this)
        countDownTimer.cancel()
        stopSelf()
    }

    companion object {
        private const val TAG = "MyForegroundService"
        private const val NOTIFICATION_CHANNEL_ID: String = "my_foreground_service"
        private const val NOTIFICATION_CHANNEL_NAME: String = "Foreground Service"
        private const val NOTIFICATION_ID: Int = 1
        private const val INTENT_FILTER_IN = "$TAG.IN"
        private const val KEY_IN_FORCE_STOP = "force_stop"
        private const val KEY_IN_SCREEN_OFF = "screen_off"
        private const val KEY_IN_CLOSE_APP = "close_app"
        private const val KEY_IN_DETACHED = "detached"

        fun startService(activity: Activity) {
            val i = Intent(activity, MyForegroundService::class.java)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            activity.startService(i)
        }

        fun sendMsg(context: Context, bundle: Bundle) {
            LocalBroadcastManager.getInstance(context).sendBroadcast(
                Intent(INTENT_FILTER_IN).putExtras(bundle)
            )
        }

        fun messageBuilder(
            detach: Boolean? = null,
            forceStop: Boolean? = null,
            screenOff: Boolean? = null,
            closeApp: Boolean? = null
        ): Bundle {
            return Bundle().apply {
                if (detach != null)
                    putBoolean(KEY_IN_DETACHED, detach)

                if (forceStop != null)
                    putBoolean(KEY_IN_FORCE_STOP, forceStop)

                if (screenOff != null)
                    putBoolean(KEY_IN_SCREEN_OFF, screenOff)

                if (closeApp != null)
                    putBoolean(KEY_IN_CLOSE_APP, closeApp)
            }
        }
    }

}