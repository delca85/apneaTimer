package com.bibi.android.apnea

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
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
        var tr = Array(series_list!!.size, {TableRow(this)})
        var nSeries = Array(series_list!!.size, {TextView(this)})
        var apneaTime = Array(series_list!!.size, {TextView(this)})
        var breathTime = Array(series_list!!.size, {TextView(this)})
        for (i in 1..series_list!!.size){
            nSeries[i-1].text = i.toString()
            apneaTime[i-1].text = getStringFromMillis(series_list!!.get(i-1).first)
            breathTime[i-1].text = getStringFromMillis(series_list!!.get(i-1).second)
            nSeries[i-1].gravity = Gravity.CENTER
            apneaTime[i-1].gravity = Gravity.CENTER
            breathTime[i-1].gravity = Gravity.CENTER
            tr[i-1].addView(nSeries[i-1])
            tr[i-1].addView(apneaTime[i-1])
            tr[i-1].addView(breathTime[i-1])
            tableLayout.addView(tr[i-1], TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT))

        }
    }

}