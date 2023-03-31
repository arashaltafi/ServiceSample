package com.arash.altafi.servicesample.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.View
import androidx.annotation.DrawableRes
import com.arash.altafi.servicesample.R
import com.arash.altafi.servicesample.utils.glide.GlideUtils
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import saman.zamani.persiandate.PersianDate
import java.util.*
import java.util.concurrent.TimeUnit

fun Long.convertDurationToTime(): String {
    val convertHours = java.lang.String.format(
        "%02d", TimeUnit.MILLISECONDS.toHours(this)
    )
    val convertMinutes = java.lang.String.format(
        "%02d", TimeUnit.MILLISECONDS.toMinutes(this) - TimeUnit.HOURS.toMinutes(
            TimeUnit.MILLISECONDS.toHours(this)
        )
    )
    val convertSeconds = java.lang.String.format(
        "%02d", TimeUnit.MILLISECONDS.toSeconds(this) - TimeUnit.MINUTES.toSeconds(
            TimeUnit.MILLISECONDS.toMinutes(this)
        )
    )
    return if (this > 3600000) "$convertHours:$convertMinutes:$convertSeconds" else "$convertMinutes:$convertSeconds"
}

fun String.applyValue(vararg args: Any?): String {
    return String.format(Locale.US, this, *args)
}

fun PersianDate.getClockString(withSecond: Boolean = false): String {
    val secondString: String = if (withSecond) "::$second" else ""
    return if (withSecond) "%02d:%02d:%02d".applyValue(hour, minute, secondString.toInt())
    else "%02d:%02d".applyValue(hour, minute)
}

fun PersianDate.getDateString(): String {
    val year = shYear
    val month = if (shMonth < 10) "0${shMonth}" else shMonth
    val day = if (shDay < 10) "0${shDay}" else shDay

    return ("$year/$month/$day")
}

fun PersianDate.getDateStringWithClock(withSecond: Boolean = false): String {
    val year = shYear
    val month = if (shMonth < 10) "0${shMonth}" else shMonth
    val day = if (shDay < 10) "0${shDay}" else shDay

    val secondString: String = if (withSecond) ":$second" else ""
    return ("$year/$month/$day $hour:$minute$secondString")
}

fun PersianDate.getTimeString(withSecond: Boolean = false): String {
    val secondString: String = if (withSecond) ":$second" else ""
    return ("$hour:$minute$secondString")
}

private fun getPersianWeekDayName(index: Int): String = when (index) {
    0 -> "شنبه"
    1 -> "یک شنبه"
    2 -> "دو شنبه"
    3 -> "سه شنبه"
    4 -> "چهار شنبه"
    5 -> "پنج شنبه"
    else -> "جمعه"
}

private fun getPersianMonthName(index: Int): String = when (index) {
    1 -> "فروردین"
    2 -> "اردیبهشت"
    3 -> "خرداد"
    4 -> "تیر"
    5 -> "مرداد"
    6 -> "شهریور"
    7 -> "مهر"
    8 -> "آبان"
    9 -> "آذر"
    10 -> "دی"
    11 -> "بهمن"
    else -> "اسفند"
}

fun Context.getBitmap(
    url: Any,
    result: ((Bitmap) -> Unit),
    @DrawableRes placeholderRes: Int? = R.drawable.bit_placeholder_image,
    @DrawableRes errorRes: Int? = R.drawable.bit_error_image,
    requestOptions: RequestOptions? = null
) {
    GlideUtils(this).getBitmapRequestBuilder(requestOptions)
        .load(url)
        .apply {
            placeholderRes?.let { placeholder(it) }
            error(errorRes)
        }
        .into(object : CustomTarget<Bitmap>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                result.invoke(resource)
            }

            override fun onLoadCleared(placeholder: Drawable?) {
            }

        })

}

fun View.toShow() {
    this.visibility = View.VISIBLE
}

fun View.isShow(): Boolean {
    return this.visibility == View.VISIBLE
}

fun View.toHide() {
    this.visibility = View.INVISIBLE
}

fun View.isHide(): Boolean {
    return this.visibility == View.INVISIBLE
}

fun View.toGone() {
    this.visibility = View.GONE
}

fun View.isGone(): Boolean {
    return this.visibility == View.GONE
}
