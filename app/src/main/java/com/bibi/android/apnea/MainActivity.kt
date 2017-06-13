package com.bibi.android.apnea

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.media.ToneGenerator
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.*
import com.bibi.android.apnea.utils.getMillis
import com.bibi.android.apnea.utils.getPercentLeft
import com.bibi.android.apnea.utils.toReadableTime
import com.mikhaellopez.circularprogressbar.CircularProgressBar

class MainActivity : AppCompatActivity(), View.OnClickListener {
    var mTimer: CountDownTimer? = null

    var mainLayout: RelativeLayout? = null

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

    var series_stored: ArrayList<Pair<Long, Long>> = ArrayList<Pair<Long, Long>>()     // in order to memoize info about all executed series

    var totalTimeCountMilliseconds: Long? = null    //total countdown time
    var numberOfSeries: Int = 1                 //number of total series

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bindViews()

        buttonStartTime!!.setOnClickListener(this)
        buttonStopTime!!.setOnClickListener(this)
        buttonViewLogs!!.setOnClickListener(this)
        buttonReset!!.setOnClickListener(this)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
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
                remaining_series!!.visibility = View.VISIBLE
                remaining_series_text!!.visibility = View.VISIBLE
                series_stored.clear()
                startTimer()
            }
        } else if (v.id == R.id.btnStopTime){
            var breathMillis = getMillis(remaining_time!!.text.toString().split(":")[0],
                    remaining_time!!.text.toString().split(":")[1])
            var pairToBeAdded = Pair(totalTimeCountMilliseconds?.minus(breathMillis), breathMillis)
            series_stored.add(pairToBeAdded as Pair<Long, Long>)
            Toast.makeText(this, "No breath time stored!",
                    Toast.LENGTH_LONG).show()
        } else if (v.id == R.id.btnLog){
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            startActivity(ViewLogsIntent(series_stored))
        } else if (v.id == R.id.btnReset){
            if (mTimer != null)
                mTimer!!.cancel()
            remaining_time?.text = "00:00"
            remaining_series!!.visibility = View.VISIBLE
            remaining_series_text!!.visibility = View.VISIBLE
            time_in_minutes!!.visibility = View.VISIBLE
            time_in_seconds!!.visibility = View.VISIBLE
            time_in_minutes!!.setText("00")
            time_in_seconds!!.setText("00")
            time_in_minutes!!.isEnabled = true
            time_in_seconds!!.isEnabled = true
            time_text!!.visibility = View.VISIBLE
            separator!!.visibility = View.VISIBLE
            series_in!!.visibility = View.VISIBLE
            series_in!!.isEnabled = true
            series_number_text!!.visibility = View.VISIBLE
            buttonStartTime!!.visibility = View.VISIBLE
            buttonStopTime!!.visibility = View.GONE
            buttonViewLogs!!.visibility = View.GONE
        }
    }

    private fun bindViews() {
        mainLayout = findViewById(R.id.fragmentLayout) as RelativeLayout
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
        if (!time_in_minutes!!.text.toString().equals("") || !time_in_seconds!!.text.toString().equals("")) {
            totalTimeCountMilliseconds = getMillis(time_in_minutes!!.text.toString(),
                    time_in_seconds!!.text.toString())
        }
        else {
            Toast.makeText(this, "Please enter time...",
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
        var toBePlayed = true

        mTimer = object : CountDownTimer(totalTimeCountMilliseconds!!, 500) {

            override fun onTick(millisUntilFinished: Long) {
                if (millisUntilFinished <= 11000  && totalTimeCountMilliseconds!!.compareTo(10000) > 0
                        && toBePlayed && !remaining_series!!.text.equals("1")){
                    val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    val mp = MediaPlayer.create(applicationContext, notification)
                    mp.start()
                    Handler().postDelayed({
                        mp.stop()
                    }, 2000)
                    toBePlayed = false
                }
                remaining_time?.text = toReadableTime(millisUntilFinished)
                remaining_series?.text = numberOfSeries.toString()
                go_home_progress?.setProgressWithAnimation(getPercentLeft(millisUntilFinished-500,
                        totalTimeCountMilliseconds!!), 500)
            }

            override fun onFinish() {
                numberOfSeries--
                remaining_series!!.text = numberOfSeries.toString()
                go_home_progress?.progress = 0F
                val toneG = ToneGenerator(AudioManager.STREAM_ALARM, 100)
                toBePlayed = true

                if (numberOfSeries > 0) {
                    this.start()
                    toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200)
                }
                else {
                    buttonStopTime!!.visibility = View.GONE
                    buttonStartTime!!.visibility = View.VISIBLE
                    buttonViewLogs!!.visibility = View.VISIBLE
                    time_in_minutes!!.isEnabled = true
                    time_in_seconds!!.isEnabled = true
                    series_in!!.isEnabled = true
                    go_home_progress?.progress = 0F
                    toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 800)
                    showBravoDialog()
                }
            }
        }.start()

    }

    private fun showBravoDialog() {
        val layoutInflater = LayoutInflater.from(this@MainActivity)
        var view = layoutInflater.inflate(R.layout.activity_series_end, null)
        var alertDialogBuilder = AlertDialog.Builder(this@MainActivity)
        alertDialogBuilder.setView(view)
        val alert = alertDialogBuilder.create()
        alert.show()
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

}