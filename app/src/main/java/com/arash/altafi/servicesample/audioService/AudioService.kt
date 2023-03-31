package com.arash.altafi.servicesample.audioService

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.net.Uri
import android.os.CountDownTimer
import android.os.IBinder
import androidx.lifecycle.LifecycleService
import com.arash.altafi.servicesample.utils.Constants
import com.arash.altafi.servicesample.utils.Constants.DLONG
import com.arash.altafi.servicesample.utils.getBitmap
import com.arash.altafi.servicesample.utils.NotificationUtils

class AudioService : LifecycleService() {

    private var musicPlayer: MediaPlayer = MediaPlayer()
    private var isPlaying = false
    private var title = ""
    private var image = ""
    private var artist = ""
    private var messageTime = 0L
    private lateinit var timer: CountDownTimer
    private var isCounterRunning = false

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        if (::timer.isInitialized) timer.onFinish()
        handleIntent(intent)
        return START_NOT_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        timer.onFinish()
        stopSelf()
    }

    override fun onCreate() {
        super.onCreate()
        registerReceiver(
            broadCastReceiver,
            IntentFilter(Constants.NOTIFICATION_ACTION)
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        musicPlayer.stop()
        musicPlayer.reset()
        timer.onFinish()
        NotificationUtils.cancelNotif(this, NotificationUtils.MEDIA_NOTIFICATION_ID)
        try {
            unregisterReceiver(broadCastReceiver)
        } catch (_: Exception) { }
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return
        when (intent.getStringExtra(Constants.ACTION)) {
            Constants.ACTION_START -> {
                title = intent.getStringExtra(Constants.TITLE) ?: ""
                artist = intent.getStringExtra(Constants.ARTIST) ?: ""
                image = intent.getStringExtra(Constants.IMAGE) ?: ""
                messageTime = intent.getLongExtra(Constants.MESSAGE_TIME, DLONG)
                val uri = Uri.parse(intent.getStringExtra(Constants.URI))
                initMusic(uri)
            }
            Constants.ACTION_PLAY -> {
                isPlaying = false
                musicPlayer.start()
                showNotification(true)
                sendBroadcast()
            }
            Constants.ACTION_PAUSE -> {
                isPlaying = true
                musicPlayer.pause()
                showNotification(false)
                if (::timer.isInitialized) timer.cancel()
                sendBroadcast()
            }
            Constants.ACTION_STOP -> {
                musicPlayer.stop()
                musicPlayer.reset()
                NotificationUtils.cancelNotif(this, NotificationUtils.MEDIA_NOTIFICATION_ID)
                if (::timer.isInitialized) timer.cancel()
            }
            Constants.ACTION_CHANGE -> {
                musicPlayer.seekTo(intent.getIntExtra(Constants.ACTION_CHANGE, musicPlayer.duration))
            }
        }

        musicPlayer.setOnCompletionListener{
            sendBroadcast(false)
        }

        sendTimeBroadcast()
    }

    private fun showNotification(playButton: Boolean) {
        getBitmap(url = image, result = { bitmap ->
            NotificationUtils.sendMediaNotification(
                this,
                bitmap,
                title,
                artist,
                playButton
            )
        })
    }

    private fun sendBroadcast(isPlaying: Boolean? = null) {
        sendBroadcast(Intent(Constants.AUDIO_ACTION).apply {
            isPlaying?.let {
                putExtra(Constants.IS_PLAYING, false)
            } ?: putExtra(Constants.IS_PLAYING, musicPlayer.isPlaying)
            putExtra(Constants.IS_COMPLETE, musicPlayer.currentPosition >= musicPlayer.duration)
        })
    }

    private fun sendTimeBroadcast() {
        val millisInFuture = (musicPlayer.duration.toLong() - musicPlayer.currentPosition.toLong()) + 1000L
        timer = object : CountDownTimer(millisInFuture, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                isCounterRunning = true
                sendBroadcast(Intent(Constants.TIME_ACTION).apply {
                    putExtra(Constants.IS_PLAYING, musicPlayer.isPlaying)
                    putExtra(Constants.TITLE, title)
                    putExtra(Constants.TIME, musicPlayer.duration.toLong())
                    putExtra(Constants.CURRENT_TIME, musicPlayer.currentPosition.toLong())
                    putExtra(Constants.MESSAGE_TIME, messageTime)
                })
            }
            override fun onFinish() {
                isCounterRunning = false
                timer.cancel()
            }
        }
        if (isCounterRunning.not())
            timer.start()
    }

    private fun initMusic(uri: Uri) {
        musicPlayer = MediaPlayer.create(this, uri)
        musicPlayer.isLooping = false
        musicPlayer.setVolume(100F, 100F)
    }

    private val broadCastReceiver = object : BroadcastReceiver() {
        override fun onReceive(contxt: Context?, intent: Intent?) {
            when (intent?.extras?.getString(Constants.NOTIFICATION_ACTION)) {
                Constants.NOTIFICATION_ACTION_PLAY_PAUSE -> {
                    if (isPlaying) {
                        isPlaying = false
                        AudioServiceManager.changeStatus(this@AudioService, Constants.ACTION_PLAY)
                    } else {
                        isPlaying = true
                        AudioServiceManager.changeStatus(this@AudioService, Constants.ACTION_PAUSE)
                    }
                }
                //add next, previous, repeat, ...
            }
        }
    }

}