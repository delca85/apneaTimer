package com.bibi.android.apnea

import android.app.AlertDialog
import android.graphics.Color
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.ToneGenerator
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Vibrator
import android.support.v7.app.AppCompatActivity
import android.view.*
import android.widget.*
import com.mikhaellopez.circularprogressbar.CircularProgressBar
import android.support.v7.widget.Toolbar
import com.bibi.android.apnea.utils.*
import org.jetbrains.anko.db.*
import java.io.File
import java.io.FileNotFoundException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(), View.OnClickListener {
    var mTimer: CountDownTimer? = null

    var toolbar: Toolbar? = null

    var buttonStartTime: Button? = null
    var buttonStopTime: Button? = null
    var buttonViewLogs: Button? = null
    var buttonReset: Button? = null

    var time_in_minutes: EditText? = null
    var time_in_seconds: EditText? = null
    var time_text: TextView? = null
    var series_in: EditText? = null
    var separator: TextView? = null
    var remaining_time: TextView? = null            //shows the time
    var remaining_series: TextView? = null            //shows the series
    var remaining_series_text: TextView? = null
    var series_number_text: TextView? = null

    var go_home_progress: CircularProgressBar? = null

    var series_stored: ArrayList<Triple<String, Long, Long>> =
            ArrayList<Triple<String, Long, Long>>()     // in order to memoize info about all executed series

    var totalTimeCountMilliseconds: Long? = null    //total countdown time
    var numberOfSeries: Int = 1                 //number of total series

    var mTimerRunning: Boolean = false
    var vibe: Vibrator? = null

    val const_fileName = "apneaTimerRecord.txt"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        bindViews()

        setSupportActionBar(toolbar)

        vibe = getSystemService(android.content.Context.VIBRATOR_SERVICE) as android.os.Vibrator
        buttonStartTime!!.setOnClickListener(this)
        buttonStopTime!!.setOnClickListener(this)
        buttonViewLogs!!.setOnClickListener(this)
        buttonReset!!.setOnClickListener(this)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onClick(v: View?) {
        if (v!!.id == R.id.btnStartTime){
            if (setTimer()) {
                buttonStopTime!!.visibility = View.VISIBLE
                buttonStartTime!!.visibility = View.GONE
                buttonViewLogs!!.visibility = View.GONE
                time_in_minutes!!.isEnabled = false
                time_in_seconds!!.isEnabled = false
                series_in!!.isEnabled = false
                series_stored.clear()
                startTimer()
            }
        } else if (v.id == R.id.btnStopTime){
            vibe!!.vibrate(500)
            trackApneaTime()
        } else if (v.id == R.id.btnLog){
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            startActivity(ViewLogsIntent(series_stored))
        } else if (v.id == R.id.btnReset){
            go_home_progress?.setProgressWithAnimation(0F, 100)
            if (mTimer != null)
                mTimer!!.cancel()
            remaining_time?.text = "00:00"
            remaining_series!!.text = "1"
            time_in_minutes!!.setText("00")
            time_in_seconds!!.setText("00")
            time_in_minutes!!.isEnabled = true
            time_in_seconds!!.isEnabled = true
            series_in!!.setText("1")
            series_in!!.isEnabled = true
            buttonStartTime!!.visibility = View.VISIBLE
            buttonStopTime!!.visibility = View.GONE
            buttonViewLogs!!.visibility = View.GONE

        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP && mTimer != null && mTimerRunning) {
            vibe!!.vibrate(500)
            trackApneaTime()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun trackApneaTime() {
        var dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
        var date = dateFormat.format(Date())
        go_home_progress!!.color = Color.parseColor("#00e1fe")
        var seriesExecuted: Int
        if (series_in!!.text.toString().equals(""))
            seriesExecuted = 1 - remaining_series!!.text.toString().toInt()
        else
            seriesExecuted = series_in!!.text.toString().toInt() - remaining_series!!.text.toString().toInt()
        if (seriesExecuted + 1 > series_stored.size) {
            var breathMillis = getMillis(remaining_time!!.text.toString().split(":")[0],
                    remaining_time!!.text.toString().split(":")[1])
            var tripleToBeAdded : Triple<String, Long?, Long> = Triple(date,
                    totalTimeCountMilliseconds?.minus(breathMillis), breathMillis)
            series_stored.add(tripleToBeAdded as Triple<String, Long, Long>)
            Toast.makeText(applicationContext, "Apnea time stored!",
                    Toast.LENGTH_LONG).show()
            if (numberOfSeries == 1)
                finishSeries()
        }
    }

    private fun finishSeries() {
        writeRecord()
        mTimerRunning = false
        if (mTimer != null)
            mTimer!!.cancel()
        buttonStopTime!!.visibility = View.GONE
        buttonStartTime!!.visibility = View.VISIBLE
        buttonViewLogs!!.visibility = View.VISIBLE
        time_in_minutes!!.isEnabled = true
        time_in_seconds!!.isEnabled = true
        series_in!!.isEnabled = true
        go_home_progress?.progress = 0F
        val toneG = ToneGenerator(AudioManager.STREAM_ALARM, 100)
        toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 800)
        writeToDatabase()
        showBravoDialog()
    }

    private fun writeToDatabase() {
        val parser = rowParser { id: String, apnea: String, breath: String ->
            Triple(id, apnea, breath)
        }
        val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
        val date = dateFormat.format(Date())
        var todaySeriesDB: List<Triple<String, String, String>>? = null

        database.use {
            select("ApneaLog", columns = *arrayOf("id", "apnea", "breath"))
                    .whereArgs("id LIKE '" + date!!.split(" ")[0] + "%'").exec {
                todaySeriesDB = parseList(parser)
            }

        }
        var bestApneaTimeDB: String? = null
        if (todaySeriesDB!!.size > 0) 
            bestApneaTimeDB = getBestApneaTime("0", "0", todaySeriesDB!!.map{(date, apnea, breath) ->
                Triple(date, apnea.toLong(), breath.toLong())})
        val bestApneaTimeNow = getBestApneaTime("0", "0", series_stored)
        if (bestApneaTimeDB == null || bestApneaTimeNow.split(" ")[0] > bestApneaTimeDB.split(" ")[0])
            database.use {
                delete("ApneaLog", "id LIKE '" + date!!.split(" ")[0] + "%'")
                for ((date, apnea, breath) in series_stored)
                    insert("ApneaLog", "id" to date, "apnea" to apnea, "breath" to breath)
            }
        
    }

    private fun bindViews() {
        toolbar = findViewById(R.id.toolbar) as Toolbar
        time_in_minutes = findViewById(R.id.time_in_minutes) as EditText
        time_in_seconds = findViewById(R.id.time_in_seconds) as EditText
        time_text = findViewById(R.id.textViewTime) as TextView
        series_in = findViewById(R.id.series_in_number) as EditText
        separator = findViewById(R.id.separator) as TextView
        buttonStartTime = findViewById(R.id.btnStartTime) as Button?
        buttonStopTime = findViewById(R.id.btnStopTime) as Button?
        buttonViewLogs = findViewById(R.id.btnLog) as Button?
        buttonReset = findViewById(R.id.btnReset) as Button?
        remaining_time = findViewById(R.id.remaining_time) as TextView
        remaining_series = findViewById(R.id.series_out) as TextView
        remaining_series_text = findViewById(R.id.textViewRemainingSeries) as TextView
        series_number_text = findViewById(R.id.textViewSeries) as TextView
        go_home_progress = findViewById(R.id.go_home_progress) as CircularProgressBar
        time_in_minutes!!.onFocusChangeListener = generateTwoDigitsWatcher()
        time_in_seconds!!.onFocusChangeListener = generateTwoDigitsWatcher()
    }

    // function that forces input with two digits in minutes and seconds
    private fun generateTwoDigitsWatcher(): View.OnFocusChangeListener? {
        val focusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus){
                when (v!!.id) {
                    R.id.time_in_minutes ->
                        if (time_in_minutes!!.text.length == 1) {
                            val text = "%02d".format(time_in_minutes!!.text.toString().toInt())
                            time_in_minutes!!.setText(text)
                        }
                    R.id.time_in_seconds ->
                        if (time_in_seconds!!.text.length == 1) {
                            val text = "%02d".format(time_in_seconds!!.text.toString().toInt())
                            time_in_seconds!!.setText(text)
                        }
                }
            }
        }
        return focusChangeListener
    }

    private fun setTimer(): Boolean {
        if (!(time_in_minutes!!.text.toString().equals("") || time_in_minutes!!.text.toString().equals("00")) ||
                !(time_in_seconds!!.text.toString().equals("") || time_in_seconds!!.text.toString().equals("00"))) {
            totalTimeCountMilliseconds = getMillis(time_in_minutes!!.text.toString(),
                    time_in_seconds!!.text.toString())
        }
        else {
            Toast.makeText(applicationContext, "Please enter time...",
                    Toast.LENGTH_LONG).show()
            return false
        }

        if (series_in!!.text.toString().equals("")) {
            numberOfSeries = 1
            remaining_series!!.text = "1"
        }
        else {
            numberOfSeries = series_in!!.text.toString().toInt()
            remaining_series!!.text = series_in!!.text.toString()
        }
        return true
    }

    private fun startTimer() {
        go_home_progress!!.color = Color.parseColor("#e80404")
        var toBePlayedTen = true
        var toBePlayedThree = true
        var series_executed = 0
        mTimerRunning = true

        mTimer = object : CountDownTimer(totalTimeCountMilliseconds!!, 250) {

            override fun onTick(millisUntilFinished: Long) {
                if (millisUntilFinished <= 11000  && totalTimeCountMilliseconds!!.compareTo(10000) > 0
                        && toBePlayedTen && !remaining_series!!.text.equals("1")){
                    val mp = MediaPlayer.create(applicationContext, R.raw.ten)
                    mp.start()
                    Handler().postDelayed({
                        mp.stop()
                    }, 2000)
                    toBePlayedTen = false
                }
                if (millisUntilFinished <= 3000  && totalTimeCountMilliseconds!!.compareTo(3000) > 0
                        && toBePlayedThree && !remaining_series!!.text.equals("1")){
                    val mp = MediaPlayer.create(applicationContext, R.raw.countdown_three_seconds)
                    mp.start()
                    toBePlayedThree = false
                }

                remaining_time?.text = toReadableTime(millisUntilFinished)
                remaining_series?.text = numberOfSeries.toString()
                go_home_progress?.setProgressWithAnimation(getPercentLeft(millisUntilFinished-125,
                        totalTimeCountMilliseconds!!), 250)
            }

            override fun onFinish() {
                val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
                val date = dateFormat.format(Date())
                go_home_progress!!.color = Color.parseColor("#e80404")
                numberOfSeries--
                remaining_series!!.text = numberOfSeries.toString()
                go_home_progress?.progress = 0F
                toBePlayedTen = true
                toBePlayedThree = true

                series_executed++

                if (series_stored.size < series_executed)
                    series_stored.add(Triple(date, totalTimeCountMilliseconds!!, 0L))

                if (numberOfSeries > 0)
                    this.start()
                else
                    finishSeries()
            }
        }.start()

    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    override fun onResume() {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        super.onResume()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId){
            R.id.credits -> showCredits()
            R.id.about -> showVersion()
            R.id.record -> showRecord()
            else -> return true

        }
        return true

    }

    private fun showRecord() {
        val layoutInflater = LayoutInflater.from(this@MainActivity)
        var view = layoutInflater.inflate(R.layout.dialog_show_record, null)
        var alertDialogBuilder = AlertDialog.Builder(this@MainActivity)
        alertDialogBuilder.setView(view)
        var tableLayout = view.findViewById(R.id.recordTable) as TableLayout
        var record: String? = null
        try {
            record = File(filesDir.toString() + "/" + const_fileName).readText().
                    replace("\n", "")
            val tr = TableRow(this)
            val date = TextView(this)
            val apneaTime = TextView(this)
            val breathTime = TextView(this)
            val recordElements = record.split(" ")
            date.text = recordElements[0] + " " + recordElements[1]
            apneaTime.text = getStringFromMillis(recordElements[2].toLong())
            breathTime.text = getStringFromMillis(recordElements[3].toLong())

            date.gravity = Gravity.CENTER
            apneaTime.gravity = Gravity.CENTER
            breathTime.gravity = Gravity.CENTER

            date.setTextColor(Color.parseColor("#00e1fe"))
            apneaTime.setTextColor(Color.parseColor("#00e1fe"))
            breathTime.setTextColor(Color.parseColor("#00e1fe"))

            tr.addView(date)
            tr.addView(apneaTime)
            tr.addView(breathTime)
            tableLayout.addView(tr, TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT))
        } catch (e : FileNotFoundException) {
            Toast.makeText(applicationContext, "No record stored yet!",
                    Toast.LENGTH_LONG).show()
        }
        alertDialogBuilder.setPositiveButton("Reset", {
            _, _ -> confirmationDialog(tableLayout, record)
            })
        val alert = alertDialogBuilder.create()
        alert.show()
    }

    private fun  confirmationDialog(tableLayout: TableLayout, record: String?) {
        val layoutInflater = layoutInflater
        var view = layoutInflater.inflate(R.layout.dialog_confirmation_reset, null)
        var alertDialogBuilder = AlertDialog.Builder(this@MainActivity)
        alertDialogBuilder.setView(view)

        alertDialogBuilder.setPositiveButton("Yes", {
            _, _ ->
            if (record != null) {
                removeFileAndLine(tableLayout)
            Toast.makeText(applicationContext, "Record deleted!",
                    Toast.LENGTH_LONG).show()
            }
            else
                Toast.makeText(applicationContext, "Nothing to be deleted!",
                        Toast.LENGTH_LONG).show()})
        alertDialogBuilder.setNegativeButton("No", {
            _, _ ->
                Toast.makeText(applicationContext, "Nothing deleted!",
                    Toast.LENGTH_LONG).show()
        })
        val alert = alertDialogBuilder.create()
        alert.show()
    }

    private fun removeFileAndLine(table: TableLayout) {
        File(filesDir.toString() + "/" + const_fileName).deleteRecursively()
        table.removeViewAt(table.childCount -1)
    }

    private fun showVersion() {
        var versionName = BuildConfig.VERSION_NAME
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        startActivity(AboutIntent(versionName))

    }

    private fun showCredits() {
        val layoutInflater = LayoutInflater.from(this@MainActivity)
        var view = layoutInflater.inflate(R.layout.credits_layout, null)
        var alertDialogBuilder = AlertDialog.Builder(this@MainActivity)
        alertDialogBuilder.setView(view)
        val alert = alertDialogBuilder.create()
        alert.show()
    }


    private fun showBravoDialog() {
        val layoutInflater = LayoutInflater.from(this@MainActivity)
        var view = layoutInflater.inflate(R.layout.dialog_series_ended, null)
        var alertDialogBuilder = AlertDialog.Builder(this@MainActivity)
        alertDialogBuilder.setView(view)
        val alert = alertDialogBuilder.create()
        alert.show()
    }


    private fun writeRecord() {
        var fileName = filesDir.toString() + "/" + const_fileName
        var dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm")
        var date = dateFormat.format(Date())
        try {
            var currentRecord = File(fileName).readText()
            var apneaTime = currentRecord.split(" ")[2]
            var breathTime = currentRecord.split(" ")[3]
            var apneaBreath = getBestApneaTime(apneaTime, breathTime, series_stored)
            if (!apneaBreath.split(" ")[0].equals(apneaTime))
                File(fileName).printWriter().use{out -> out.println(
                        date + " " + apneaBreath)}
        } catch (fileNotFound: FileNotFoundException){
            File(fileName).printWriter().use{out -> out.println(
                    date + " " + getBestApneaTime("0", "0", series_stored))}
        }
    }

}