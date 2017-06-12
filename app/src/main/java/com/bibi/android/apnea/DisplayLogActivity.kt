package com.bibi.android.apnea

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity

/**
 * Created by bibi on 12/06/17.
 */


fun Context.ViewLogsIntent(series_stored: ArrayList<Pair<Long, Long>>): Intent {
    return Intent(this, DisplayLogActivity::class.java).apply {
        putExtra(INTENT_LOGS_LIST, series_stored)
    }
}

private const val INTENT_LOGS_LIST = "logs_list"

class DisplayLogActivity: AppCompatActivity() {
    var series_list: ArrayList<Pair<Long, Long>>?  = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        series_list = intent.getStringArrayListExtra(INTENT_LOGS_LIST) as ArrayList<Pair<Long, Long>>
    }

}