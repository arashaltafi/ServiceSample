package com.arash.altafi.servicesample.audioService

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.SeekBar
import com.arash.altafi.servicesample.R
import com.arash.altafi.servicesample.databinding.ActivityAudioServiceBinding
import com.arash.altafi.servicesample.utils.*
import saman.zamani.persiandate.PersianDate

class AudioServiceActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityAudioServiceBinding.inflate(layoutInflater)
    }

    private val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Constants.TIME_ACTION -> {
                    binding.apply {
                        val isPlaying = intent.getBooleanExtra(Constants.IS_PLAYING, true)
                        val currentTime = intent.getLongExtra(Constants.CURRENT_TIME, 0)
                        val time = intent.getLongExtra(Constants.TIME, 0)

                        tvTitle.text = intent.getStringExtra(Constants.TITLE)
                        val messageTime = intent.getLongExtra(Constants.MESSAGE_TIME, 0)
                        tvMessageTime.text =
                            "...".plus(PersianDate(messageTime).getClockString())

                        tvTime.text =
                            "${time.convertDurationToTime()} / " + currentTime.convertDurationToTime()

                        seekBar.progress = currentTime.toInt() / 1000
                        seekBar.max = time.toInt() / 1000

                        if (seekBar.progress >= seekBar.max) {
                            AudioServiceManager.changeStatus(
                                this@AudioServiceActivity,
                                Constants.ACTION_STOP
                            )
                        }

                        ivClose.setOnClickListener {
                            AudioServiceManager.changeStatus(
                                this@AudioServiceActivity,
                                Constants.ACTION_STOP
                            )
                        }

                        ivClose.setOnClickListener {
                            constraintLayout.toGone()
                            AudioServiceManager.changeStatus(
                                this@AudioServiceActivity,
                                Constants.ACTION_STOP
                            )
                        }

                        if (isPlaying)
                            ivPlay.setImageResource(R.drawable.ic_round_pause_24)
                        else
                            ivPlay.setImageResource(R.drawable.ic_round_play_arrow_24)

                        ivPlay.setOnClickListener {
                            if (isPlaying) {
                                onTrackPause()
                            } else {
                                onTrackPlay()
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        createChannel()
        registerReceiver(
            broadcastReceiver, IntentFilter(Constants.TIME_ACTION)
        )
        registerReceiver(
            broadcastReceiver, IntentFilter(Constants.AUDIO_ACTION)
        )
        init()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationUtils.createChannel(
                this,
                Constants.DefaultChannelId,
                "public",
                "public_app_notifications",
                NotificationManager.IMPORTANCE_HIGH,
                Notification.VISIBILITY_PUBLIC,
                soundUri = Uri.parse("android.resource://$packageName/raw/notif")
            )
        }
    }

    private fun init() = binding.apply {
        btnPlayAudio.setOnClickListener {
            constraintLayout.toShow()

            AudioServiceManager.changeStatus(this@AudioServiceActivity, Constants.ACTION_STOP)
            AudioServiceManager.startService(
                this@AudioServiceActivity,
                "title",
                "artist",
                "https://arashaltafi.ir/arash.jpg",
                Uri.parse("android.resource://$packageName/raw/test"),
                System.currentTimeMillis()
            )

            onTrackPlay()
        }

        btnPauseAudio.setOnClickListener {
            onTrackPause()
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar1: SeekBar?, progress: Int, fromUser: Boolean
            ) {
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar1: SeekBar?) {
                seekBar.progress = seekBar1?.progress ?: 0
                AudioServiceManager.changeProgress(
                    this@AudioServiceActivity, seekBar1?.progress?.times(1000) ?: 0
                )
            }
        })
    }

    private fun onTrackPlay() {
        AudioServiceManager.changeStatus(this, Constants.ACTION_PLAY)
    }

    private fun onTrackPause() {
        AudioServiceManager.changeStatus(this, Constants.ACTION_PAUSE)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(broadcastReceiver)
    }

}