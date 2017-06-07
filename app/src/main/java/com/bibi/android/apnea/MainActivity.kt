package com.bibi.android.apnea

import android.os.Bundle
import android.os.CountDownTimer
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.bibi.android.apnea.utils.*
import com.mikhaellopez.circularprogressbar.CircularProgressBar

class MainActivity : AppCompatActivity(), View.OnClickListener {
    var mTimer: CountDownTimer? = null

    var buttonStartTime: Button? = null
    var buttonStopTime: Button? = null

    var toolbar: Toolbar? = null
    var time_in_minutes: EditText? = null
    var time_in_seconds: EditText? = null
    var series_in: EditText? = null
    var separator: TextView? = null
    var remaining_time: TextView? = null            //shows the time
    var remaining_series: TextView? = null            //shows the series
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
            startTimer()
        } else if (v!!.id == R.id.btnStopTime){
            mTimer!!.cancel()
            buttonStartTime!!.visibility = View.VISIBLE
            buttonStopTime!!.visibility = View.GONE
            time_in_minutes!!.isEnabled = true
            time_in_seconds!!.isEnabled = true
            series_in!!.isEnabled = true
        }
    }

    private fun bindViews() {
        toolbar = findViewById(R.id.toolbar) as Toolbar
        time_in_minutes = findViewById(R.id.time_in_minutes) as EditText
        time_in_seconds = findViewById(R.id.time_in_seconds) as EditText
        series_in = findViewById(R.id.series_in_number) as EditText
        separator = findViewById(R.id.separator) as TextView
        buttonStartTime = findViewById(R.id.btnStartTime) as Button?
        buttonStopTime = findViewById(R.id.btnStopTime) as Button?
        remaining_time = findViewById(R.id.remaining_time) as TextView
        remaining_series = findViewById(R.id.series_out) as TextView
        go_home_progress = findViewById(R.id.go_home_progress) as CircularProgressBar
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

        if (!series_in!!.text.toString().equals(""))
            numberOfSeries = series_in!!.text.toString().toInt()
    }


    private fun startTimer() {
        mTimer = object : CountDownTimer(totalTimeCountMilliseconds!!, 500) {

            override fun onTick(millisUntilFinished: Long) {
                remaining_time?.text = toReadableTime(millisUntilFinished)
                remaining_series?.text = numberOfSeries.toString()
                go_home_progress?.setProgressWithAnimation(getPercentLeft(millisUntilFinished-500,
                        totalTimeCountMilliseconds!!), 500)
            }

            override fun onFinish() {
                numberOfSeries--;
                remaining_series!!.text = numberOfSeries.toString();
                go_home_progress?.progress = 0F

                if (numberOfSeries > 0)
                    this.start()
                else {
                    remaining_time?.text =  "Bravo Bittino!"
                    remaining_time!!.visibility = View.VISIBLE
                    remaining_series!!.visibility = View.VISIBLE
                    buttonStartTime!!.visibility = View.VISIBLE
                    buttonStopTime!!.visibility = View.GONE
                    time_in_minutes!!.visibility = View.VISIBLE
                    time_in_seconds!!.visibility = View.VISIBLE
                    time_in_minutes!!.isEnabled = true
                    time_in_seconds!!.isEnabled = true
                    series_in!!.visibility = View.VISIBLE
                    series_in!!.isEnabled = true
                    separator!!.visibility = View.VISIBLE
                    go_home_progress?.progress = 0F
                }
            }
        }.start()



    }

}
