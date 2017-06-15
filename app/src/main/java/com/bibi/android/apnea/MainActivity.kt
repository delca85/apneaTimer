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
import com.bibi.android.apnea.utils.getMillis
import com.bibi.android.apnea.utils.getPercentLeft
import com.bibi.android.apnea.utils.toReadableTime
import com.mikhaellopez.circularprogressbar.CircularProgressBar
import android.support.v7.widget.Toolbar

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

    var series_stored: ArrayList<Pair<Long, Long>> = ArrayList<Pair<Long, Long>>()     // in order to memoize info about all executed series

    var totalTimeCountMilliseconds: Long? = null    //total countdown time
    var numberOfSeries: Int = 1                 //number of total series

    var mTimerRunning: Boolean = false
    var vibe: Vibrator? = null

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
        go_home_progress!!.color = Color.parseColor("#00e1fe")
        var seriesExecuted: Int
        if (series_in!!.text.toString().equals(""))
            seriesExecuted = 1 - remaining_series!!.text.toString().toInt()
        else
            seriesExecuted = series_in!!.text.toString().toInt() - remaining_series!!.text.toString().toInt()
        if (seriesExecuted + 1 > series_stored.size) {
            var breathMillis = getMillis(remaining_time!!.text.toString().split(":")[0],
                    remaining_time!!.text.toString().split(":")[1])
            var pairToBeAdded = Pair(totalTimeCountMilliseconds?.minus(breathMillis), breathMillis)
            series_stored.add(pairToBeAdded as Pair<Long, Long>)
            Toast.makeText(applicationContext, "Apnea time stored!",
                    Toast.LENGTH_LONG).show()
            if (numberOfSeries == 1)
                finishSeries()
        }
    }

    private fun finishSeries() {
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
        showBravoDialog()
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
                go_home_progress!!.color = Color.parseColor("#e80404")
                numberOfSeries--
                remaining_series!!.text = numberOfSeries.toString()
                go_home_progress?.progress = 0F
                toBePlayedTen = true
                toBePlayedThree = true

                series_executed++

                if (series_stored.size < series_executed)
                    series_stored.add(Pair(totalTimeCountMilliseconds!!, 0L))

                if (numberOfSeries > 0)
                    this.start()
                else
                    finishSeries()
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

    override fun onResume() {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        super.onResume()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId){
            R.id.credits -> showCredits()
            R.id.about -> showVersion()
            else -> return true

        }
        return true

    }

    private fun showVersion() {
        var versionCode = BuildConfig.VERSION_CODE
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

}