package ir.scriptestan.keepingaliveservice

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.widget.Toast
import java.util.*
import kotlin.concurrent.schedule


class MyService : Service() {

    private lateinit var setting : Setting
    private val timer : Timer = Timer()
    private var isTimerActive : Boolean = false

//    val appSetting : AppSettings = AppSettings()

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        toast("Service started :P")

        setting = Setting(this)

        if(!isTimerActive){
            isTimerActive = true
            timer.schedule(100, 1000) {
                setting.incrementServiceWorkingTime(1000) // increment 1 second
                setting.updateServiceLastActiveTime() // service is active
            }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        timer.cancel()

        toast("Service destroyed :((")
//        if(setting.isStarted()){
//            val broadcastIntent = Intent(this, WakeupAlarm::class.java)
//            sendBroadcast(broadcastIntent)
//        }
    }


    @SuppressLint("ShowToast")
    fun toast(s: String) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
    }
}
