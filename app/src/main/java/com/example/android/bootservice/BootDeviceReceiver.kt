package com.example.android.bootservice


import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast

class BootDeviceReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val action = intent.action

        val message = "BootDeviceReceiver onReceive, action is " + action!!

        Toast.makeText(context, message, Toast.LENGTH_LONG).show()

        Log.d(TAG_BOOT_BROADCAST_RECEIVER, action)

        if (Intent.ACTION_BOOT_COMPLETED == action) {

            startServiceByAlarm(context)
        }
    }

    /* Create an repeat Alarm that will invoke the background service for each execution time.
     * The interval time can be specified by your self.  */
    private fun startServiceByAlarm(context: Context) {
        // Get alarm manager.
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Create intent to invoke the background service.
        val intent = Intent(context, OverlayShowingService::class.java)
        val pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val startTime = System.currentTimeMillis()
        val intervalTime = (60 * 1000).toLong()

        val message = "Start service use repeat alarm. "

        Log.d(TAG_BOOT_BROADCAST_RECEIVER, message)

        // Create repeat alarm.
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, startTime, intervalTime, pendingIntent)
    }

    companion object {

        private const val TAG_BOOT_BROADCAST_RECEIVER = "BOOT_BROADCAST_RECEIVER"
    }
}