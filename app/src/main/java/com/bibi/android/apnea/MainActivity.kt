package com.bibi.android.apnea

import android.media.AudioManager
import android.media.RingtoneManager
import android.media.ToneGenerator
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.text.Editable
import android.text.TextWatcher
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

    var toolbar: Toolbar? = null
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

    var totalTimeCountMilliseconds: Long? = null    //total countdown time
    var numberOfSeries: Int = 1                 //number of total series

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bindViews()

        buttonStartTime!!.setOnClickListener(this)
        buttonStopTime!!.setOnClickListener(this)

        setSupportActionBar(toolbar)
    }

    override fun onClick(v: View?) {
        if (v!!.id == R.id.btnStartTime){
            setTimer()
            buttonStopTime!!.visibility = View.VISIBLE
            buttonStartTime!!.visibility = View.GONE
            time_in_minutes!!.isEnabled = false
            time_in_seconds!!.isEnabled = false
            series_in!!.isEnabled = false
            remaining_series!!.visibility = View.VISIBLE
            remaining_series_text!!.visibility = View.VISIBLE
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            startTimer()
        } else if (v.id == R.id.btnStopTime){
            mTimer!!.cancel()
            buttonStartTime!!.visibility = View.VISIBLE
            buttonStopTime!!.visibility = View.GONE
            time_in_minutes!!.isEnabled = true
            time_in_seconds!!.isEnabled = true
            series_in!!.isEnabled = true
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    private fun bindViews() {
        mainLayout = findViewById(R.id.fragmentLayout) as RelativeLayout
        toolbar = findViewById(R.id.toolbar) as Toolbar
        time_in_minutes = findViewById(R.id.time_in_minutes) as EditText
        time_in_seconds = findViewById(R.id.time_in_seconds) as EditText
        time_text = findViewById(R.id.textViewTime) as TextView
        series_in = findViewById(R.id.series_in_number) as EditText
        separator = findViewById(R.id.separator) as TextView
        buttonStartTime = findViewById(R.id.btnStartTime) as Button?
        buttonStopTime = findViewById(R.id.btnStopTime) as Button?
        remaining_time = findViewById(R.id.remaining_time) as TextView
        remaining_series = findViewById(R.id.series_out) as TextView
        remaining_series_text = findViewById(R.id.textViewRemainingSeries) as TextView
        series_number_text = findViewById(R.id.textViewSeries) as TextView
        go_home_progress = findViewById(R.id.go_home_progress) as CircularProgressBar
        time_in_minutes!!.onFocusChangeListener = generateTwoDigitsWatcher()
        time_in_seconds!!.onFocusChangeListener = generateTwoDigitsWatcher()
        mainLayout!!.setOnClickListener(generateFragmentLayoutListener())
    }

    private fun  generateFragmentLayoutListener(): View.OnClickListener? {
        val fragmentLayoutListener = View.OnClickListener {
            mTimer!!.cancel()
            remaining_time?.text = "00:00"
            remaining_series!!.visibility = View.VISIBLE
            remaining_series_text!!.visibility = View.VISIBLE
            time_in_minutes!!.visibility = View.VISIBLE
            time_in_seconds!!.visibility = View.VISIBLE
            time_in_minutes!!.isEnabled = true
            time_in_seconds!!.isEnabled = true
            time_text!!.visibility = View.VISIBLE
            separator!!.visibility = View.VISIBLE
            series_in!!.visibility = View.VISIBLE
            series_in!!.isEnabled = true
            series_number_text!!.visibility = View.VISIBLE
            buttonStartTime!!.visibility = View.VISIBLE
            buttonStopTime!!.visibility = View.GONE
        }
        return fragmentLayoutListener
    }

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

    private fun setTimer() {
        var time = 0
        if (!time_in_minutes!!.text.toString().equals("") || !time_in_seconds!!.text.toString().equals("")) {
            if (!time_in_minutes!!.text.toString().equals(""))
                time += time_in_minutes!!.text.toString().toInt() * 60
            if (!time_in_seconds!!.text.toString().equals(""))
                time += time_in_seconds!!.text.toString().toInt()
        }
        else
            Toast.makeText(this, "Please enter time...",
                    Toast.LENGTH_LONG).show()

        totalTimeCountMilliseconds = time * 1000L

        if (series_in!!.text.toString().equals("")) {
            numberOfSeries = 1
            remaining_series!!.text = "1"
        }
        else {
            numberOfSeries = series_in!!.text.toString().toInt()
            remaining_series!!.text = series_in!!.text.toString()
        }
    }

    private fun startTimer() {
        var toBePlayed = getMillis(time_in_minutes!!.text.toString(), time_in_seconds!!.text.toString()) >= 10000

        mTimer = object : CountDownTimer(totalTimeCountMilliseconds!!, 500) {

            override fun onTick(millisUntilFinished: Long) {
                if (millisUntilFinished <= 10000 && toBePlayed){
                    val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    val r = RingtoneManager.getRingtone(applicationContext, notification)
                    r.play()
                    Handler().postDelayed({
                        r.stop()
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

                if (numberOfSeries > 0)
                    this.start()
                else {
                    remaining_time?.text =  "Bravo Bittino!"
                    remaining_series!!.visibility = View.GONE
                    remaining_series_text!!.visibility = View.GONE
                    buttonStopTime!!.visibility = View.GONE
                    time_in_minutes!!.visibility = View.GONE
                    time_in_seconds!!.visibility = View.GONE
                    time_text!!.visibility = View.GONE
                    separator!!.visibility = View.GONE
                    series_in!!.visibility = View.GONE
                    series_number_text!!.visibility = View.GONE
                    go_home_progress?.progress = 0F
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    val toneG = ToneGenerator(AudioManager.STREAM_ALARM, 100)
                    toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200)
                }
            }
        }.start()

    }
}
