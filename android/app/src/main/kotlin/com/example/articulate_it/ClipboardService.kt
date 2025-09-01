package com.example.articulate_it // Make sure this matches your package name

import android.app.*
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager // Import PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.*

class ClipboardService : Service() {

    private lateinit var clipboardManager: ClipboardManager
    private var generativeModel: GenerativeModel? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private val handler = Handler(Looper.getMainLooper())
    private var lastClipboardText: String? = ""
    
    // --- NEW: WakeLock declaration ---
    private var wakeLock: PowerManager.WakeLock? = null

    private val clipboardChecker = object : Runnable {
        override fun run() {
            try {
                val clipData = clipboardManager.primaryClip
                if (clipData != null && clipData.itemCount > 0) {
                    val currentText = clipData.getItemAt(0).text?.toString()
                    if (currentText != null && currentText != lastClipboardText) {
                        lastClipboardText = currentText
                        Log.d("ClipboardService", "New text detected: $currentText")
                        getAiResponse(currentText)
                    }
                }
            } catch (e: Exception) {
                Log.e("ClipboardService", "Error reading clipboard: ${e.message}")
            } finally {
                handler.postDelayed(this, 1500)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val apiKey = intent?.getStringExtra("apiKey")
        if (apiKey.isNullOrEmpty()) {
            stopSelf()
            return START_NOT_STICKY
        }
        generativeModel = GenerativeModel(modelName = "gemini-1.5-flash", apiKey = apiKey)

        // --- NEW: Initialize PowerManager and WakeLock ---
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "ClipboardAI::TaskWakelockTag")

        createNotificationChannel()
        val notification = createServiceNotification()
        startForeground(1, notification)
        clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        handler.post(clipboardChecker)
        Log.d("ClipboardService", "Service has started with polling.")
        return START_STICKY
    }

    private fun getAiResponse(prompt: String) {
        // --- MODIFIED: Acquire and release the WakeLock around the API call ---
        try {
            // Prevent the CPU from sleeping for up to 1 minute
            wakeLock?.acquire(1 * 60 * 1000L /* 1 minute timeout */)
            Log.d("ClipboardService", "WakeLock acquired.")
            
            generativeModel?.let { model ->
                serviceScope.launch {
                    try {
                        val response = model.generateContent(prompt)
                        response.text?.let { answer ->
                            Log.d("ClipboardService", "AI Answer: $answer")
                            showResultNotification(prompt, answer)
                        }
                    } catch (e: Exception) {
                        Log.e("ClipboardService", "Error calling Gemini API: ${e.message}")
                    }
                }
            }
        } finally {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
                Log.d("ClipboardService", "WakeLock released.")
            }
        }
    }
    
    // ... (The rest of your functions: showResultNotification, createServiceNotification, etc. remain unchanged) ...
    private fun showResultNotification(question: String, answer: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(this, "CLIPBOARD_AI_RESULTS")
            .setContentTitle("Clipboard AI Answer")
            .setContentText(question)
            .setStyle(NotificationCompat.BigTextStyle().bigText(answer))
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun createServiceNotification(): Notification {
        return NotificationCompat.Builder(this, "CLIPBOARD_AI_SERVICE")
            .setContentTitle("Clipboard AI")
            .setContentText("Listening for copied text...")
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                "CLIPBOARD_AI_SERVICE",
                "Clipboard AI Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val resultChannel = NotificationChannel(
                "CLIPBOARD_AI_RESULTS",
                "Clipboard AI Results Channel",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
            manager.createNotificationChannel(resultChannel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(clipboardChecker)
        serviceScope.cancel()
        Log.d("ClipboardService", "Service has been stopped.")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}