package com.example.exercise_reminder

import android.media.RingtoneManager
import android.media.Ringtone
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import android.app.KeyguardManager

class AlarmActivity : ComponentActivity() {

    //private lateinit var ringtone: Ringtone
    private var ringtone: Ringtone? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Show over lock screen
        setShowWhenLocked(true)
        setTurnScreenOn(true)

        // Dismiss keyguard (unlock screen)
        val keyguardManager = getSystemService(KeyguardManager::class.java)
        keyguardManager?.requestDismissKeyguard(this, null)

        // Get the random message
        val message = intent.getStringExtra("message") ?: "No message"

        val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        ringtone = RingtoneManager.getRingtone(applicationContext, alarmUri)
        ringtone?.play()

        setContent {
            AlarmScreen(
                message = message,   // pass the message to your composable
                onDismiss = {
                    ringtone?.stop()
                    // Schedule next random alarm
                    val triggerTime = AlarmScheduler.getRandomTimeInMillis()
                    val randomMessage = /* You can pick a random message again or reuse previous */
                        intent.getStringExtra("message") ?: "No message"
                    AlarmScheduler.scheduleRandomAlarm(this, triggerTime, randomMessage)
                    finish()
                }
            )
        }
    }
}

@Composable
fun AlarmScreen(message: String, onDismiss: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = message, style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = onDismiss) {
            Text("Dismiss Alarm")
        }
    }
}

