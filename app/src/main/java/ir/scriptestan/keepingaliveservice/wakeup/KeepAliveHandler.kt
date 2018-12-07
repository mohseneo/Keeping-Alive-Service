package ir.scriptestan.keepingaliveservice.wakeup

import android.content.Context
import android.content.Intent
import ir.scriptestan.keepingaliveservice.MyService
import ir.scriptestan.keepingaliveservice.Setting
import ir.scriptestan.keepingaliveservice.utils.Utils

/**
 * Created by Neo on 2018-12-06.
 * http://m3n.ir/
 */
class KeepAliveHandler {

    companion object {

        /**
         * WAKEUP THE SERVICE
         */
        fun wakeUpTheService(context: Context){
            if(!Utils.isMyServiceRunning(context, MyService::class.java)){
                context.startService(Intent(context, MyService::class.java))
            }

            Setting(context).updateJobLastActiveTime()
//            toast(context,"KeepAliveHandler WORKING!!!")
//            Utils.playSound(context, R.raw.relentless)
        }

        fun setJob(context: Context){
            WakeupAlarm.setJob(context)
            WakeupJobService.setJob(context)
        }

        fun removeJob(context: Context){
            WakeupAlarm.removeJob(context)
            WakeupJobService.removeJob(context)
        }
    }

}