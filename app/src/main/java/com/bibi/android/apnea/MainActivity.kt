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
    var separator: TextView? = null
    var time_out: TextView? = null
    var remaining_time: TextView? = null            //shows the time
    var go_home_progress: CircularProgressBar? = null

    var totalTimeCountMilliseconds: Long? = null    //total countdown time

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
            time_in_minutes!!.visibility = View.GONE
            time_in_seconds!!.visibility = View.GONE
            separator!!.visibility = View.GONE
            time_in_minutes!!.setText("")
            time_in_seconds!!.setText("")
            startTimer()
        } else if (v!!.id == R.id.btnStopTime){
            mTimer!!.cancel()
            buttonStartTime!!.visibility = View.VISIBLE
            buttonStopTime!!.visibility = View.GONE
            time_in_minutes!!.visibility = View.VISIBLE
            time_in_seconds!!.visibility = View.VISIBLE
            separator!!.visibility = View.VISIBLE
        }
    }

    private fun bindViews() {
        toolbar = findViewById(R.id.toolbar) as Toolbar
        time_in_minutes = findViewById(R.id.time_in_minutes) as EditText
        time_in_seconds = findViewById(R.id.time_in_seconds) as EditText
        separator = findViewById(R.id.separator) as TextView
        time_out = findViewById(R.id.series_out) as TextView
        buttonStartTime = findViewById(R.id.btnStartTime) as Button?
        buttonStopTime = findViewById(R.id.btnStopTime) as Button?
        remaining_time = findViewById(R.id.remaining_time) as TextView
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
    }


    private fun startTimer() {
        mTimer = object : CountDownTimer(totalTimeCountMilliseconds!!, 1000) {

            override fun onTick(millisUntilFinished: Long) {
                remaining_time?.text = toReadableTime(millisUntilFinished)
                go_home_progress?.setProgressWithAnimation(getPercentLeft(millisUntilFinished, totalTimeCountMilliseconds!!))
            }

            override fun onFinish() {
                time_out?.text = getString(R.string.default_time)
                remaining_time?.text =  "Bravo Bittino!"
                go_home_progress?.progress = 0F
                remaining_time!!.visibility = View.VISIBLE
                buttonStartTime!!.visibility = View.VISIBLE
                buttonStopTime!!.visibility = View.GONE
                time_in_minutes!!.visibility = View.VISIBLE
                time_in_seconds!!.visibility = View.VISIBLE
                separator!!.visibility = View.VISIBLE
            }
        }.start()

        /*if (didTimeStart(baseContext)) {
            val remainingTime = getRemainingTime(baseContext)
            if (remainingTime > 0) {
//                time_in_minutes?.text = toHourMinFormat(getStartTime(baseContext))
                time_out?.text = toHourMinFormat(getEndTime(baseContext))
                remaining_time?.text = toReadableTime(remainingTime)
                go_home_progress?.progress = 100F
                mTimer?.start()
            } else {
//                time_in_minutes?.text = getString(R.string.default_time)
                time_out?.text = getString(R.string.default_time)
                remaining_time?.text = getString(R.string.default_remaining_time)
                go_home_progress?.progress = 0F
            }
        }*/
    }

}
