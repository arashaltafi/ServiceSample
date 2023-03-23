package com.arash.altafi.servicesample.utils

object Utils {

    fun setPersianDigits(src: String?): String {
        val result = StringBuilder("")
        var unicode = 0
        if (src != null) {
            for (i in src.indices) {
                unicode = src[i].code
                if (unicode in 48..57) {
                    result.append((unicode + 1728).toChar())
                } else {
                    result.append(src[i])
                }
            }
        }
        return result.toString()
    }

}