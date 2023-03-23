package com.arash.altafi.servicesample

import android.Manifest
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PowerManager
import android.widget.Toast
import com.arash.altafi.servicesample.background.MyBackgroundService
import com.arash.altafi.servicesample.foreground.MyForegroundService
import com.arash.altafi.servicesample.utils.PermissionUtils

class MainActivity : AppCompatActivity() {

    private val registerNotificationResult = PermissionUtils.register(this,
        object : PermissionUtils.PermissionListener {
            override fun observe(permissions: Map<String, Boolean>) {
                if (permissions[Manifest.permission.POST_NOTIFICATIONS] == true) {
                    Toast.makeText(
                        this@MainActivity,
                        "Permission is Granted Successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(this@MainActivity, "Permission Not Granted", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestPermissionNotification()
    }

    private fun requestPermissionNotification() {
        if (!PermissionUtils.isGranted(this, Manifest.permission.POST_NOTIFICATIONS)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                PermissionUtils.requestPermission(
                    this, registerNotificationResult,
                    Manifest.permission.POST_NOTIFICATIONS
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        //start MyBackgroundService
        MyBackgroundService.startService(this)

        //start MyForegroundService
        MyForegroundService.startService(this)

        MyForegroundService.sendMsg(
            this,
            MyForegroundService.messageBuilder(screenOff = false, closeApp = false, detach = false)
        )
    }

    override fun onPause() {
        super.onPause()

        val powerManager = getSystemService(POWER_SERVICE) as PowerManager

        MyForegroundService.sendMsg(
            this,
            MyForegroundService.messageBuilder(
                screenOff = powerManager.isInteractive.not(),
                detach = true
            )
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        MyForegroundService.sendMsg(
            this,
            MyForegroundService.messageBuilder(
                closeApp = true, detach = false
            )
        )
    }
}