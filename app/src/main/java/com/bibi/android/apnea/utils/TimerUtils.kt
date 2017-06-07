package com.bibi.android.apnea.utils

val PREF_TIME_KEY = "Start_time"

fun didTimeStart(context: android.content.Context): Boolean {
    when {
        exists(context, PREF_TIME_KEY) -> return true
        else -> return getStartTime(context) != 0L
    }
}

fun removeStartTime(context: android.content.Context) {
    remove(context, PREF_TIME_KEY)
}

fun getStartTime(context: android.content.Context): Long {
    return getLong(context, PREF_TIME_KEY, 0)
}

fun setStartTime(context: android.content.Context, time: Long) {
    putLong(context, PREF_TIME_KEY, time)
}
