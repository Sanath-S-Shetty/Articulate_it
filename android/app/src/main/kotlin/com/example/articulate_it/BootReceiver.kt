package com.example.articulate_it

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

class BootReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "🔄 BootReceiver triggered")
        Log.d(TAG, "📱 Intent action: ${intent.action}")
        
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                Log.d(TAG, "🚀 Device boot completed")
                startClipboardService(context, "Boot completed")
            }
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                Log.d(TAG, "📦 App package replaced/updated")
                startClipboardService(context, "App updated")
            }
            Intent.ACTION_PACKAGE_REPLACED -> {
                Log.d(TAG, "📦 Package replaced")
                // Only start if it's our package
                val packageName = intent.dataString
                Log.d(TAG, "📋 Package name: $packageName")
                if (packageName?.contains(context.packageName) == true) {
                    startClipboardService(context, "Our app updated")
                }
            }
            else -> {
                Log.w(TAG, "⚠️ Unknown intent action: ${intent.action}")
            }
        }
    }
    
    private fun startClipboardService(context: Context, reason: String) {
        Log.d(TAG, "🎯 Starting clipboard service - Reason: $reason")
        
        try {
            val serviceIntent = Intent(context, ClipboardService::class.java)
            Log.d(TAG, "📄 Service intent created: $serviceIntent")
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Log.d(TAG, "📱 Android O+, using startForegroundService()")
                context.startForegroundService(serviceIntent)
            } else {
                Log.d(TAG, "📱 Android < O, using startService()")
                context.startService(serviceIntent)
            }
            
            Log.d(TAG, "✅ Clipboard service start command sent successfully")
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error starting clipboard service: ${e.message}", e)
        }
    }
}