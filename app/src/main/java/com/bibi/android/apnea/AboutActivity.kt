package com.bibi.android.apnea

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import com.bibi.android.apnea.utils.getStringFromMillis

/**
 * Created by bibi on 12/06/17.
 */


fun Context.AboutIntent(version: String): Intent {
    return Intent(this, AboutActivity::class.java).apply {
        putExtra(VERSION_NUMBER, version)
    }
}

private const val VERSION_NUMBER = "version"

class AboutActivity: AppCompatActivity() {
    var versionNumber: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.about_activity)

        versionNumber = findViewById(R.id.versionNumber_textView) as TextView
        versionNumber!!.text = intent.getStringExtra(VERSION_NUMBER)
    }

}