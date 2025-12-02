package com.example.exercise_reminder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.layout.weight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import android.os.Build
import android.app.AlarmManager
import android.content.Intent
import android.provider.Settings
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import android.app.Activity
import android.widget.Toast
import androidx.activity.viewModels
import androidx.compose.material3.ExposedDropdownMenuBox
//import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.TextField
import androidx.compose.material3.ExposedDropdownMenuBoxScope
//import androidx.compose.material3.menuAnchor
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MenuAnchorType

class MainActivity : ComponentActivity() {
    // Using Jetpack’s ViewModel delegation
    private val messageViewModel: MessageViewModel by viewModels {
        MessageViewModelFactory(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestExactAlarmPermission()
        setContent {
            MainScreen(messageViewModel) //{AlarmScheduler.scheduleRandomAlarm(this) }
        }
    }
    private fun requestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(AlarmManager::class.java)
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)
            }
        }
    }
}

fun fuzzyMatch(query: String, target: String): Int {
    val q = query.lowercase()
    val t = target.lowercase()

    if (t.contains(q)) return 0

    val dp = Array(q.length + 1) { IntArray(t.length + 1) }

    for (i in 0..q.length) dp[i][0] = i
    for (j in 0..t.length) dp[0][j] = j

    for (i in 1..q.length) {
        for (j in 1..t.length) {
            dp[i][j] = minOf(
                dp[i - 1][j] + 1,
                dp[i][j - 1] + 1,
                dp[i - 1][j - 1] + if (q[i - 1] == t[j - 1]) 0 else 1
            )
        }
    }

    return dp[q.length][t.length]
}

@Composable
fun MainScreen(viewModel: MessageViewModel) {
    val context = LocalContext.current
    val activity = context as Activity

    var nextAlarmTime by remember { mutableStateOf<Long?>(null) }
    var timeRemaining by remember { mutableStateOf<Long?>(null) }

    val messages by viewModel.messages.collectAsState()
    var inputText by remember { mutableStateOf("") }

    // Accurate countdown timer (recalculates using system time)
    LaunchedEffect(nextAlarmTime) {
        while (nextAlarmTime != null) {
            val now = System.currentTimeMillis()
            val remaining = nextAlarmTime!! - now

            if (remaining <= 0) {
                timeRemaining = 0
                break
            }

            timeRemaining = remaining
            delay(1000)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // ============= INPUT FIELD =============
        Row(verticalAlignment = Alignment.CenterVertically) {
            // --- AUTOCOMPLETE TEXT FIELD ---
            var expanded by remember { mutableStateOf(false) }

            val allSuggestions = viewModel.history.collectAsState().value

            // fuzzy search based on full history
            val filteredSuggestions =
                if (inputText.isBlank()) emptyList()
                else allSuggestions
                    .distinct()
                    .sortedBy { fuzzyMatch(inputText, it) } // fuzzy ranking
                    .take(5) // max 5 suggestions

            LaunchedEffect(inputText) {
                expanded = false  // hide while user is typing

                if (inputText.isBlank()) return@LaunchedEffect

                // wait 2 seconds after last keystroke
                delay(2000)

                // if there are suggestions after 2 seconds → show dropdown
                expanded = filteredSuggestions.isNotEmpty()
            }

            Box(modifier = Modifier.weight(1f)) {
                TextField(
                    value = inputText,
                    onValueChange = {
                        inputText = it
                        //expanded = filteredSuggestions.isNotEmpty()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Enter message") },
                    singleLine = true
                )

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    filteredSuggestions.forEach { suggestion ->
                        DropdownMenuItem(
                            text = { Text(suggestion) },
                            onClick = {
                                inputText = suggestion
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Button(onClick = {
                if (inputText.isNotBlank()) {
                    viewModel.addMessage(inputText)
                    inputText = ""
                }
            }) {
                Text("Add")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ============= MESSAGE LIST =============
        Text("Messages:")
        messages.forEach { msg ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("• $msg")
                Button(onClick = { viewModel.deleteMessage(msg) }) {
                    Text("Delete")
                }
            }
        }

        Spacer(modifier = Modifier.height(25.dp))

        // ============= START ALARM BUTTON =============
        Button(onClick = {
            if (messages.isEmpty()) {
                Toast.makeText(context, "Add at least one message first!", Toast.LENGTH_SHORT).show()
                return@Button
            }

            val triggerTime = AlarmScheduler.getRandomTimeInMillis()
            nextAlarmTime = triggerTime

            val randomMessage = messages.random()

            AlarmScheduler.scheduleRandomAlarm(context, triggerTime, randomMessage)
        }) {
            Text("Start Random Alarm")
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ============= COUNTDOWN TIMER DISPLAY =============
        timeRemaining?.let { remaining ->
            if (remaining > 0) {
                val minutes = remaining / 60000
                val seconds = (remaining % 60000) / 1000
                Text("Next alarm in: ${minutes}m ${seconds}s")
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // ============= EXIT BUTTON =============
        Button(
            onClick = { activity.finishAffinity() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp)
        ) {
            Text("Exit")
        }
    }
}



fun formatTime(millis: Long): String {
    val sdf = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(millis))
}

