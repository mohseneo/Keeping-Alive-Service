package ir.scriptestan.keepingaliveservice

import android.annotation.SuppressLint
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.View
import android.widget.Toast
import ir.scriptestan.keepingaliveservice.utils.Utils
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.concurrent.schedule
import lecho.lib.hellocharts.model.SliceValue
import lecho.lib.hellocharts.model.PieChartData
import ir.scriptestan.keepingaliveservice.wakeup.KeepAliveHandler


class MainActivity : AppCompatActivity() {

    private lateinit var timer : Timer
    private lateinit var setting : Setting

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
    }

    @SuppressLint("SetTextI18n")
    private fun init() {
        setting = Setting(this)

        when(setting.isStarted()){

            false -> {
                button.text = "Start"
                chart.pieChartData = null
                tvServiceStatus.text = "Service status..."
                tvTimer.text = "Stopped..!"
            }

            true -> {
                startTimer()
                button.text = "Reset and stop"
            }

        }
    }

    @SuppressLint("SetTextI18n")
    fun action(v: View) {
        when(setting.isStarted()){

            false -> { // Starting service
                toast("Starting service..")

                setting.resetServiceWorkingTime()
                setting.resetStartTime()
                setting.setStartTime(System.currentTimeMillis())
                startService(Intent(this, MyService::class.java))
                button.text = "Reset and stop"
                startTimer()
                KeepAliveHandler.setJob(this)
            }

            true -> { // Stopping service
                toast("Stopping service :/")

                KeepAliveHandler.removeJob(this)
                stopService(Intent(this, MyService::class.java))
                button.text = "Start"
                timer.cancel()
//                setting.resetServiceWorkingTime()
//                setting.resetStartTime()
                setting.resetAll()
                chart.pieChartData = null

                tvServiceLastActiveTime.text = "Service last activity time..."
                tvJobLastActiveTime.text = "Awake job last activity time..."
                tvJobCallCount.text = "Job call count .."
                tvServiceStatus.text = "Service status..."
                tvTimer.text = "Stopped..!"
            }

        }
    }

    private val mHandler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message) {
            updateUI()
            log("Timer working at time: "+System.currentTimeMillis())
        }
    }

    private fun startTimer(){
        timer = Timer()
        timer.schedule(200,1000) {
            mHandler.obtainMessage(1).sendToTarget()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateUI(){
        val timeElapsed = System.currentTimeMillis() - setting.getStartTime()
        tvTimer.text = Utils.formatInterval(timeElapsed)
        updateChartData()
        updateServiceStatusText()
        tvServiceLastActiveTime.text = "Service last activity: " +
                Utils.toDuration((System.currentTimeMillis() - setting.getServiceLastActiveTime()))
        tvJobLastActiveTime.text = "Awake service last activity: " +
                Utils.toDuration((System.currentTimeMillis() - setting.getJobLastActiveTime()))
        tvJobCallCount.text = "Job call count: by Alarm=${setting.getCountCallJobByAlarm()}" +
                ", by JobService=${setting.getCountCallJobByJobService()}"
    }

    @SuppressLint("SetTextI18n")
    private fun updateServiceStatusText(){
        val timeActive = System.currentTimeMillis() - setting.getServiceLastActiveTime()
        if(timeActive<3000){
            tvServiceStatus.text = "Good news. Service is active now! :P"
            tvServiceStatus.setTextColor(ContextCompat.getColor(this, R.color.green))
        }else{
            tvServiceStatus.text = "Bad news. Service is not active! :("
            tvServiceStatus.setTextColor(ContextCompat.getColor(this, R.color.red))
        }
    }

    private fun updateChartData(){
        val allTimeFromStart = (System.currentTimeMillis() - setting.getStartTime()) / 1000
        val allTimeThatServiceWorking = setting.getServiceWorkingTime() / 1000

        val pieData = ArrayList<SliceValue>()
        pieData.add(SliceValue(allTimeThatServiceWorking.toFloat(), ContextCompat.getColor(this, R.color.green))
            .setLabel("Working\n${Utils.formatInterval(allTimeThatServiceWorking*1000)}"))

        var stoppedTime : Float = (allTimeFromStart-allTimeThatServiceWorking).toFloat()
        if(stoppedTime<1) stoppedTime = 1.toFloat()

        pieData.add(SliceValue(stoppedTime, ContextCompat.getColor(this, R.color.grey))
            .setLabel("Stopped\n${Utils.formatInterval((stoppedTime*1000).toLong())}"))

        val pieChartData = PieChartData(pieData)
        pieChartData.setHasLabels(true)
        pieChartData.setHasCenterCircle(true).centerText1 = "Service status"
        pieChartData.setCenterText1FontSize(20).centerText1Color = ContextCompat.getColor(this, R.color.black)

        chart.pieChartData = pieChartData
    }

    @SuppressLint("ShowToast")
    fun toast(s: String) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
    }

    fun log(s: String) {
        Log.wtf("MainActivity", s)
    }

}
