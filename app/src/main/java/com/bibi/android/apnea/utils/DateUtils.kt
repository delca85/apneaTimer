package com.bibi.android.apnea.utils


fun getPercentLeft(timeLeftInMillis: Long, totalMillisTime: Long): Float {
    return timeLeftInMillis.toFloat() / totalMillisTime.toFloat() * 100
}

fun toReadableTime(millis: Long): String {
    val seconds = millis / 1000

    return String.format("%02d:%02d", seconds / 60,
            seconds % 60)
}