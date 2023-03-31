package com.arash.altafi.servicesample.audioService

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.arash.altafi.servicesample.utils.Constants

object AudioServiceManager {

    fun changeStatus(context: Context, status: String) {
        Intent(context, AudioService::class.java).also {
            it.putExtra(Constants.ACTION, status)
            context.startService(it)
        }
    }

    fun changeProgress(context: Context, progress: Int) {
        Intent(context, AudioService::class.java).also {
            it.putExtra(Constants.ACTION, Constants.ACTION_CHANGE)
            it.putExtra(Constants.ACTION_CHANGE, progress)
            context.startService(it)
        }
    }

    fun startService(
        context: Context,
        title: String,
        artist: String,
        image: String,
        uri: Uri,
        messageTime: Long,
    ) {
        Intent(context, AudioService::class.java).also {
            it.putExtra(Constants.ACTION, Constants.ACTION_START)
            it.putExtra(Constants.TITLE, title)
            it.putExtra(Constants.ARTIST, artist)
            it.putExtra(Constants.IMAGE, image)
            it.putExtra(Constants.URI, uri.toString())
            it.putExtra(Constants.MESSAGE_TIME, messageTime)
            context.startService(it)
        }
    }

}