package com.bibi.android.apnea.utils


fun getPercentLeft(timeLeftInMillis: Long, totalMillisTime: Long): Float {
    return timeLeftInMillis.toFloat() / totalMillisTime.toFloat() * 100
}

fun toReadableTime(millis: Long): String {
    val seconds = millis / 1000

    return String.format("%02d:%02d", seconds / 60,
            seconds % 60)
}

fun getMillis(timeInMinutes: String, timeInSeconds: String) : Long{
    val timeInMinutesInt = if (timeInMinutes.equals("")) 0 else timeInMinutes.toInt()
    val timeInSecondsInt = if (timeInSeconds.equals("")) 0 else timeInSeconds.toInt()
    return timeInMinutesInt * 60 * 1000L + timeInSecondsInt * 1000L
}

fun getStringFromMillis(millis: Long): String{
    return "" + millis / 60000 + ":" + millis % 60000
}