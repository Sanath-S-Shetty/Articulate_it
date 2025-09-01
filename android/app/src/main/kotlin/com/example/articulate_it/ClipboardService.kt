package com.example.articulate_it // Make sure this matches your package name

import android.app.*
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.*

class ClipboardService : Service() {

    private lateinit var clipboardManager: ClipboardManager
    private var generativeModel: GenerativeModel? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO)

    // --- NEW: For polling ---
    private val handler = Handler(Looper.getMainLooper())
    private var lastClipboardText: String? = ""
    // --- END NEW ---

    // --- NEW: The runnable that will check the clipboard every second ---
    private val clipboardChecker = object : Runnable {
        override fun run() {
            try {
                val clipData = clipboardManager.primaryClip
                if (clipData != null && clipData.itemCount > 0) {
                    val currentText = clipData.getItemAt(0).text?.toString()
                    // Check if the text is new and not empty
                    if (currentText != null && currentText != lastClipboardText) {
                        lastClipboardText = currentText
                        Log.d("ClipboardService", "New text detected: $currentText")
                        getAiResponse(currentText)
                    }
                }
            } catch (e: Exception) {
                Log.e("ClipboardService", "Error reading clipboard: ${e.message}")
            } finally {
                // Schedule the next check in 1.5 seconds
                handler.postDelayed(this, 1500)
            }
        }
    }
    // --- END NEW ---


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val apiKey = intent?.getStringExtra("apiKey")

        if (apiKey.isNullOrEmpty()) {
            Log.e("ClipboardService", "API Key is missing.")
            stopSelf()
            return START_NOT_STICKY
        }


        generativeModel = GenerativeModel(modelName = "gemini-1.5-flash", apiKey = apiKey)

        createNotificationChannel()
        val notification = createServiceNotification()
        startForeground(1, notification)

        clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        
        // --- MODIFIED: Start the polling instead of adding a listener ---
        handler.post(clipboardChecker)
        // --- END MODIFIED ---

        Log.d("ClipboardService", "Service has started with polling.")
        return START_STICKY
    }

    private fun getAiResponse(prompt: String) {
        // This function remains the same
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
    }

    private fun showResultNotification(question: String, answer: String) {
        // This function remains the same
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
        // This function remains the same
        return NotificationCompat.Builder(this, "CLIPBOARD_AI_SERVICE")
            .setContentTitle("Clipboard AI")
            .setContentText("Listening for copied text...")
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()
    }

    private fun createNotificationChannel() {
        // This function remains the same
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
        // --- MODIFIED: Stop the polling when the service is destroyed ---
        handler.removeCallbacks(clipboardChecker)
        // --- END MODIFIED ---
        serviceScope.cancel()
        Log.d("ClipboardService", "Service has been stopped.")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}