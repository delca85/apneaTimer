package com.bibi.android.apnea.utils

import java.text.SimpleDateFormat
import java.util.*


fun getPercentLeft(timeLeftInMillis: Long, totalMillisTime: Long): Float {
    return timeLeftInMillis.toFloat() / totalMillisTime.toFloat() * 100
}

fun toReadableTime(millis: Long): String {
    val seconds = millis / 1000

    return String.format("%02d:%02d", seconds / 60,
            seconds % 60)
}

fun getMillis(time: String): Long{
    return getMillis(time.split(":")[0], time.split(":")[1])
}

fun getMillis(timeInMinutes: String, timeInSeconds: String) : Long{
    val timeInMinutesInt = if (timeInMinutes.equals("")) 0 else timeInMinutes.toInt()
    val timeInSecondsInt = if (timeInSeconds.equals("")) 0 else timeInSeconds.toInt()
    return timeInMinutesInt * 60 * 1000L + timeInSecondsInt * 1000L
}

fun getStringFromMillis(millis: Long): String{
    var minutes = "%02d".format(millis/60000)
    var seconds = "%02d".format((millis / 1000) % 60)
    return minutes + ":" + seconds
}

fun getBestApneaTime(apneaTime: String, breathTime: String,
                     todayTimes: List<Triple<String, Long, Long>>) : String {
    var bestApneaTime = apneaTime.toLong()
    var bestBreathTime = breathTime
    for (apneaTime in todayTimes)
        if (bestApneaTime < apneaTime.second) {
            bestApneaTime = apneaTime.second
            bestBreathTime = apneaTime.third.toString()
        }
    return bestApneaTime.toString() + " " + bestBreathTime
}