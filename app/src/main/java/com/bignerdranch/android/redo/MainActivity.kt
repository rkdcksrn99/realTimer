package com.bignerdranch.android.redo

import android.os.Bundle
import android.os.CountDownTimer
import android.view.Menu
import android.view.MenuItem
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bignerdranch.android.redo.util.PrefUtil
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.content_main.*
import java.util.*


class MainActivity : AppCompatActivity() {

    enum class TimerState{
        Stopped, Paused, Running
    }

    private lateinit var timer: CountDownTimer
    private var timerLengthSeconds: Long = 0L
    private var timerState = TimerState.Stopped

    private var secondsRemaining = 0L


    private lateinit var progressCountdown: ProgressBar
    private lateinit var textViewCountdown: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setIcon(R.drawable.ic_timer)
        supportActionBar?.title = "      Timer"

        findViewById<FloatingActionButton>(R.id.fab_start).setOnClickListener { view ->
            startTimer()
            timerState = TimerState.Running
            updateButtons()
        }

        findViewById<FloatingActionButton>(R.id.fab_pause).setOnClickListener{ view ->
            timer.cancel()
            timerState = TimerState.Paused
            updateButtons()
        }

        findViewById<FloatingActionButton>(R.id.fab_stop).setOnClickListener{ view ->
            timer.cancel()
            onTimerFinished()
        }
    }

    override fun onResume(){
        super.onResume()

        initTimer()
    }

    override fun onPause(){
        super.onPause()

        if(timerState == TimerState.Running) {
            timer.cancel()
        }
        else if(timerState == TimerState.Paused){
            //notification
        }

        PrefUtil.setPreviousTimerLengthSeconds(timerLengthSeconds,this)
        PrefUtil.setSecondsRemaining(secondsRemaining, this)
        PrefUtil.setTimerState(timerState, this)
    }

    private fun initTimer(){
        timerState = PrefUtil.getTimerState(this)

        if(timerState == TimerState.Stopped)
            setNewTimerLength()
        else
            setPreviousTimerLength()

        secondsRemaining = if(timerState == TimerState.Running || timerState == TimerState.Paused)
            PrefUtil.getSecondsRemaining(this)
        else
            timerLengthSeconds

        if(timerState == TimerState.Running)
            startTimer()

        updateButtons()
        updateCountdownUI()
    }

    private fun onTimerFinished(){
        timerState = TimerState.Stopped

        setNewTimerLength()

        progress_countdown.progress = 0

        PrefUtil.setSecondsRemaining(timerLengthSeconds, this)
        secondsRemaining = timerLengthSeconds

        updateButtons()
        updateCountdownUI()
    }

    private fun startTimer(){
        timerState = TimerState.Running

        timer = object: CountDownTimer(secondsRemaining * 1000, 1000) {
            override fun onFinish() = onTimerFinished()

            override fun onTick(millisUntilFinished: Long){
                secondsRemaining = millisUntilFinished / 1000
                updateCountdownUI()
            }
        }.start()
    }

    private fun setNewTimerLength(){
        val lengthInMintutes = PrefUtil.getTimerLength(this)
        timerLengthSeconds = (lengthInMintutes * 60L)
        progress_countdown.max = timerLengthSeconds.toInt()
    }

    private fun setPreviousTimerLength(){
        timerLengthSeconds = PrefUtil. getPreviousTimerLengthSeconds(this)
        progress_countdown.max = timerLengthSeconds.toInt()
    }

    private fun updateCountdownUI() {
        val minutesUntilFinished = secondsRemaining / 60
        val secondsInMinuteUntilFinished = secondsRemaining - minutesUntilFinished * 60
        val secondsStr = secondsInMinuteUntilFinished.toString()
        textView_countdown.text = "$minutesUntilFinished:${if (secondsStr.length == 2) secondsStr else "0" + secondsStr}"
        progress_countdown.progress = (timerLengthSeconds - secondsRemaining).toInt()
    }

    private fun updateButtons(){
        when (timerState) {
            TimerState.Running ->{
                findViewById<FloatingActionButton>(R.id.fab_start).isEnabled = false
                findViewById<FloatingActionButton>(R.id.fab_pause).isEnabled = true
                findViewById<FloatingActionButton>(R.id.fab_stop).isEnabled = true
            }
            TimerState.Stopped -> {
                findViewById<FloatingActionButton>(R.id.fab_start).isEnabled = true
                findViewById<FloatingActionButton>(R.id.fab_pause).isEnabled = false
                findViewById<FloatingActionButton>(R.id.fab_stop).isEnabled = false
            }
            TimerState.Paused -> {
                findViewById<FloatingActionButton>(R.id.fab_start).isEnabled = true
                findViewById<FloatingActionButton>(R.id.fab_pause).isEnabled = false
                findViewById<FloatingActionButton>(R.id.fab_stop).isEnabled = true
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}