package com.bibi.android.apnea

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity

/**
 * Created by bibi on 12/06/17.
 */


fun Context.ViewLogsIntent(): Intent {
    return Intent(this, DisplayLogActivity::class.java)
}

class DisplayLogActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

}