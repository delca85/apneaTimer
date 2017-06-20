package com.bibi.android.apnea

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.bibi.android.apnea.utils.database
import com.bibi.android.apnea.utils.fromStringToDate
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import org.jetbrains.anko.db.parseList
import org.jetbrains.anko.db.rowParser
import org.jetbrains.anko.db.select




fun Context.ViewDBIntent(): Intent {
    return Intent(this, GraphActivity::class.java)
}

class GraphActivity : AppCompatActivity() {

    var graph: GraphView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_show_db)
        graph = findViewById(R.id.graph) as GraphView
        populateGraph()
    }

    private fun populateGraph() {
        val parser = rowParser{ id: String, apnea: String, breath: String ->
            Triple(id, apnea, breath)
        }

        var todaySeriesDB: List<Triple<String, String, String>>? = null

        database.use{
            select("ApneaLog")
                    .whereArgs("id LIKE '%'").exec {
                todaySeriesDB = parseList(parser)
            }
        }

        if (!todaySeriesDB!!.isEmpty()) {
            var dp = arrayOfNulls<DataPoint>(todaySeriesDB!!.size)
            var i = 0

            for ((date, apnea, _) in todaySeriesDB!!) {
                dp[i] = DataPoint(fromStringToDate(date), apnea.toDouble())
                i++
            }

            var series = LineGraphSeries<DataPoint>(dp)
            graph!!.addSeries(series)

            graph!!.gridLabelRenderer.labelFormatter = DateAsXAxisLabelFormatter(this)
            graph!!.gridLabelRenderer.isHumanRounding = false

        }

    }
}
