package com.bibi.android.apnea.utils

import java.text.SimpleDateFormat
import java.util.*

fun toHourMinFormat(millis: Long): String {
    return parseDate("HH:mm", millis)
}

fun parseDate(pattern: String, millis: Long): String {
    return SimpleDateFormat(pattern, Locale.getDefault()).format(Date(millis))
}

fun convertToMillis(min: Int, sec: Int): Long {
    return ((min * 60000) + (sec * 1000)).toLong()
}

fun getPercentLeft(timeLeftInMillis: Long, totalMillisTime: Long): Float {
    return timeLeftInMillis.toFloat() / totalMillisTime.toFloat() * 100
}

fun toReadableTime(millis: Long): String {
    var seconds = millis / 1000

    return String.format("%02d:%02d", seconds / 60,
            seconds % 60)
}