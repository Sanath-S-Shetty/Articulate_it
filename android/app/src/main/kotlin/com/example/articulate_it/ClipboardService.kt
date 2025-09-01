package com.example.articulate_it 

import android.app.*
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.*

class ClipboardService : Service() {

    private lateinit var clipboardManager: ClipboardManager
    private var generativeModel: GenerativeModel? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO)

    private val clipboardListener = ClipboardManager.OnPrimaryClipChangedListener {
        val clipData = clipboardManager.primaryClip
        if (clipData != null && clipData.itemCount > 0) {
            val clipboardText = clipData.getItemAt(0).text?.toString()
            if (!clipboardText.isNullOrEmpty()) {
                Log.d("ClipboardService", "Copied text: $clipboardText")
                getAiResponse(clipboardText)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val apiKey = intent?.getStringExtra("apiKey")

        if (apiKey.isNullOrEmpty()) {
            Log.e("ClipboardService", "API Key is missing.")
            stopSelf() // Stop if no API key
            return START_NOT_STICKY
        }

        // Initialize the GenerativeModel here
        generativeModel = GenerativeModel(
            modelName = "gemini-pro",
            apiKey = apiKey
        )

        createNotificationChannel()
        val notification = createServiceNotification()
        startForeground(1, notification)

        clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboardManager.addPrimaryClipChangedListener(clipboardListener)

        Log.d("ClipboardService", "Service has started.")
        return START_STICKY
    }

    private fun getAiResponse(prompt: String) {
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
                    // Optionally, show an error notification
                }
            }
        }
    }

    private fun showResultNotification(question: String, answer: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(this, "CLIPBOARD_AI_RESULTS")
            .setContentTitle("Clipboard AI Answer")
            .setContentText(question)
            .setStyle(NotificationCompat.BigTextStyle().bigText(answer))
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()
        notificationManager.notify(System.currentTimeMillis().toInt(), notification) // Unique ID for each notification
    }

    private fun createServiceNotification(): Notification {
        return NotificationCompat.Builder(this, "CLIPBOARD_AI_SERVICE")
            .setContentTitle("Clipboard AI")
            .setContentText("Listening for copied text...")
            .setSmallIcon(R.mipmap.ic_launcher) // Default Flutter icon
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Channel for the foreground service
            val serviceChannel = NotificationChannel(
                "CLIPBOARD_AI_SERVICE",
                "Clipboard AI Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            // Channel for the AI results
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
        clipboardManager.removePrimaryClipChangedListener(clipboardListener)
        serviceScope.cancel()
        Log.d("ClipboardService", "Service has been stopped.")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}