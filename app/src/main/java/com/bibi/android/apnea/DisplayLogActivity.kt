package com.bibi.android.apnea

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import com.bibi.android.apnea.utils.getStringFromMillis

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
        setContentView(R.layout.activity_display_logs)
        series_list = intent.getStringArrayListExtra(INTENT_LOGS_LIST) as ArrayList<Pair<Long, Long>>

        populateTable()
    }

    private fun populateTable() {
        var tableLayout = findViewById(R.id.logsTable) as TableLayout
        for (i in 1..series_list!!.size){
            var tr = TableRow(this)
            var nSeries = TextView(this)
            var apneaTime = TextView(this)
            var breathTime = TextView(this)
            nSeries.setText(i.toString())
            apneaTime.setText(getStringFromMillis(series_list!!.get(i-1).first))
            breathTime.setText(getStringFromMillis(series_list!!.get(i-1).second))
            tr.addView(nSeries)
            tr.addView(apneaTime)
            tr.addView(breathTime)
            tableLayout.addView(tr, TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT))

        }
    }

}