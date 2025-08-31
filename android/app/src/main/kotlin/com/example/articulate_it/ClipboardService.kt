package com.example.articulate_it

import android.app.*
import android.content.*
import android.os.*
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException

class ClipboardService : Service() {

    private lateinit var clipboardManager: ClipboardManager
    private val client = OkHttpClient()
    
    companion object {
        private const val TAG = "ClipboardService"
        private const val NOTIFICATION_ID = 1
        private const val CLIPBOARD_NOTIFICATION_ID = 2
        private const val CHANNEL_ID = "clipboard_channel"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "🟢 Service onCreate() called")
        
        try {
            // 📢 Create notification channel first
            Log.d(TAG, "📢 Creating notification channel...")
            createNotificationChannel()
            Log.d(TAG, "✅ Notification channel created successfully")
            
            Log.d(TAG, "📋 Getting clipboard manager...")
            clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            Log.d(TAG, "✅ Clipboard manager obtained")

            // 🔎 Listen for clipboard changes
            Log.d(TAG, "🔗 Setting up clipboard listener...")
            clipboardManager.addPrimaryClipChangedListener {
                Log.d(TAG, "📝 Clipboard changed detected!")
                
                try {
                    val clip = clipboardManager.primaryClip
                    Log.d(TAG, "📋 Primary clip: $clip")
                    
                    if (clip != null && clip.itemCount > 0) {
                        val item = clip.getItemAt(0)
                        Log.d(TAG, "📄 Clip item: $item")
                        
                        val text = item?.text?.toString() ?: ""
                        Log.d(TAG, "📝 Extracted text: '${text.take(100)}${if (text.length > 100) "..." else ""}'")
                        
                        if (text.isNotEmpty()) {
                            Log.d(TAG, "✅ Valid text found, processing...")
                            sendToGemini(text)
                            showCopiedNotification(text)
                        } else {
                            Log.w(TAG, "⚠️ Empty text, skipping...")
                        }
                    } else {
                        Log.w(TAG, "⚠️ No clip data available")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error in clipboard listener: ${e.message}", e)
                }
            }
            Log.d(TAG, "✅ Clipboard listener setup complete")
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error in onCreate(): ${e.message}", e)
            Toast.makeText(this, "Error initializing clipboard service: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "🚀 onStartCommand() called")
        Log.d(TAG, "📱 Intent: $intent")
        Log.d(TAG, "🏁 Flags: $flags, StartId: $startId")
        
        // 🧪 Manual clipboard test
        testClipboardAccess()
        
        try {
            Log.d(TAG, "🔔 Creating foreground notification...")
            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Clipboard Listener Active")
                .setContentText("Tap to test clipboard access")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setOngoing(true)
                .setContentIntent(createTestPendingIntent())
                .build()

            Log.d(TAG, "🎯 Starting foreground service...")
            startForeground(NOTIFICATION_ID, notification)
            Log.d(TAG, "✅ Foreground service started successfully")
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error starting foreground service: ${e.message}", e)
            return START_NOT_STICKY
        }

        Log.d(TAG, "🔄 Returning START_STICKY")
        return START_STICKY
    }
    
    private fun createTestPendingIntent(): PendingIntent {
        val intent = Intent(this, ClipboardService::class.java).apply {
            action = "TEST_CLIPBOARD"
        }
        return PendingIntent.getService(
            this, 
            0, 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    private fun testClipboardAccess() {
        Log.d(TAG, "🧪 Testing manual clipboard access...")
        
        try {
            val clip = clipboardManager.primaryClip
            Log.d(TAG, "🧪 Current clipboard: $clip")
            
            if (clip != null && clip.itemCount > 0) {
                val text = clip.getItemAt(0)?.text?.toString()
                Log.d(TAG, "🧪 Current clipboard text: '$text'")
            } else {
                Log.d(TAG, "🧪 No clipboard content available")
            }
            
            // Test if we can detect clipboard changes by manually checking
            Handler(Looper.getMainLooper()).postDelayed({
                Log.d(TAG, "🧪 Manual clipboard check after 5 seconds...")
                val newClip = clipboardManager.primaryClip
                val newText = newClip?.getItemAt(0)?.text?.toString()
                Log.d(TAG, "🧪 Clipboard after delay: '$newText'")
            }, 5000)
            
        } catch (e: Exception) {
            Log.e(TAG, "🧪 Error testing clipboard: ${e.message}", e)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.d(TAG, "🔗 onBind() called with intent: $intent")
        return null
    }

    override fun onDestroy() {
        Log.d(TAG, "🔴 onDestroy() called")
        super.onDestroy()
        
        try {
            Log.d(TAG, "🧹 Cleaning up resources...")
            // Remove clipboard listener if possible
            Log.d(TAG, "✅ Cleanup complete")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error during cleanup: ${e.message}", e)
        }
        
        Toast.makeText(this, "Clipboard listener stopped", Toast.LENGTH_SHORT).show()
        Log.d(TAG, "🏁 Service destroyed")
    }

    // 🔔 Small popup when text is copied
    private fun showCopiedNotification(text: String) {
        Log.d(TAG, "🔔 Showing copied notification for text: '${text.take(20)}...'")
        
        try {
            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Copied Text")
                .setContentText(text.take(50)) // shorten long text
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true)
                .build()

            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(CLIPBOARD_NOTIFICATION_ID, notification)
            Log.d(TAG, "✅ Copied notification posted successfully")
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error showing notification: ${e.message}", e)
        }
    }

    // 🌐 Send copied text to Gemini API
    private fun sendToGemini(text: String) {
        Log.d(TAG, "🌐 Starting Gemini API call...")
        Log.d(TAG, "📤 Text to send (first 100 chars): '${text.take(100)}${if (text.length > 100) "..." else ""}'")
        
        try {
            val apiKey = "YOUR_GEMINI_API_KEY" // ⚠️ replace with your key
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=$apiKey"
            Log.d(TAG, "🔗 API URL: $url")

            val json = """
                {
                  "contents": [
                    {
                      "parts": [
                        {"text": "$text"}
                      ]
                    }
                  ]
                }
            """.trimIndent()
            
            Log.d(TAG, "📋 JSON payload: $json")

            val body = RequestBody.create("application/json".toMediaTypeOrNull(), json)
            Log.d(TAG, "📦 Request body created")

            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()
            
            Log.d(TAG, "🚀 Making HTTP request...")

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e(TAG, "❌ API call failed: ${e.message}", e)
                    Log.e(TAG, "🔍 Call: $call")
                }

                override fun onResponse(call: Call, response: Response) {
                    Log.d(TAG, "📥 API response received")
                    Log.d(TAG, "📊 Response code: ${response.code}")
                    Log.d(TAG, "📋 Response headers: ${response.headers}")
                    
                    try {
                        val responseBody = response.body?.string()
                        Log.d(TAG, "📄 Response body length: ${responseBody?.length ?: 0}")
                        Log.d(TAG, "📝 Gemini Response: $responseBody")
                    } catch (e: Exception) {
                        Log.e(TAG, "❌ Error reading response body: ${e.message}", e)
                    } finally {
                        response.close()
                        Log.d(TAG, "🔒 Response closed")
                    }
                }
            })
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error in sendToGemini(): ${e.message}", e)
        }
    }

    // 📢 Channel for foreground service
    private fun createNotificationChannel() {
        Log.d(TAG, "📢 createNotificationChannel() called")
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d(TAG, "📱 Android O+ detected, creating channel...")
            
            try {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Clipboard Service",
                    NotificationManager.IMPORTANCE_LOW
                )
                Log.d(TAG, "📋 Channel object created: $channel")
                
                val manager = getSystemService(NotificationManager::class.java)
                Log.d(TAG, "📱 NotificationManager obtained: $manager")
                
                manager.createNotificationChannel(channel)
                Log.d(TAG, "✅ Notification channel created successfully")
                
                // Verify channel was created
                val createdChannel = manager.getNotificationChannel(CHANNEL_ID)
                Log.d(TAG, "🔍 Verification - Created channel: $createdChannel")
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error creating notification channel: ${e.message}", e)
            }
        } else {
            Log.d(TAG, "📱 Android version < O, no channel needed")
        }
    }
}