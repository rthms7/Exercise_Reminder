package com.example.exercise_reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import kotlin.random.Random
import android.widget.Toast


object AlarmScheduler {

    fun getRandomTimeInMillis(): Long {
        val now = System.currentTimeMillis()
        val randomDelay = Random.nextLong(0, 3600_000)   // 0–1 hour
        return now + randomDelay
    }

    fun scheduleRandomAlarm(context: Context, triggerTime: Long, message: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("message", message)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        } catch (e: SecurityException) {
            e.printStackTrace()

            // OPTIONAL: notify the user
            Toast.makeText(context, "Please allow Exact Alarm permission", Toast.LENGTH_LONG).show()
        }
    }
}
