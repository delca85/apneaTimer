package com.bibi.android.apnea.utils

import android.content.Context
import android.preference.PreferenceManager

fun getString(context: android.content.Context, key: String, defValue: String): String {
    val pref = PreferenceManager.getDefaultSharedPreferences(context)
    return pref.getString(key, defValue)
}

fun putString(context: android.content.Context, key: String, value: String) {
    val pref = PreferenceManager.getDefaultSharedPreferences(context)
    val editor = pref.edit()
    editor.putString(key, value)
    editor.apply()
}

fun getLong(context: android.content.Context, key: String, defValue: Long): Long {
    val pref = PreferenceManager.getDefaultSharedPreferences(context)
    return pref.getLong(key, defValue)
}

fun putLong(context: android.content.Context, key: String, value: Long) {
    val pref = PreferenceManager.getDefaultSharedPreferences(context)
    val editor = pref.edit()
    editor.putLong(key, value)
    editor.apply()
}

fun exists(context: android.content.Context, key: String): Boolean {
    val pref = android.preference.PreferenceManager.getDefaultSharedPreferences(context)
    return pref.contains(key)
}

fun remove(context: android.content.Context, key: String) {
    val pref = PreferenceManager.getDefaultSharedPreferences(context)
    val editor = pref.edit()
    editor.remove(key)
    editor.apply()
}