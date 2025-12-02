package com.example.exercise_reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class  AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        Log.d("AlarmReceiver", "Alarm triggered!")

        val message = intent?.getStringExtra("message") ?: "No message"

        //start alarmActivity
        val activityIntent = Intent(context, AlarmActivity::class.java).apply {
            putExtra("message", message)   // Send the message to the activity
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
        }
        context.startActivity(activityIntent)
    }
}
