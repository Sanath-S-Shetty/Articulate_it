package com.example.articulate_it

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity() {
    private val CHANNEL = "clipboard_service"
    private val TAG = "MainActivity"

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        Log.d(TAG, "🔧 Configuring Flutter engine...")

        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler { call, result ->
            Log.d(TAG, "📞 Method call received: ${call.method}")
            Log.d(TAG, "📋 Arguments: ${call.arguments}")
            
            when (call.method) {
                "startService" -> {
                    Log.d(TAG, "🚀 Starting clipboard service...")
                    
                    try {
                        val intent = Intent(this, ClipboardService::class.java)
                        Log.d(TAG, "📄 Intent created: $intent")
                        Log.d(TAG, "🏷️ Intent component: ${intent.component}")
                        
                        // Use correct start method depending on Android version
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            Log.d(TAG, "📱 Android O+, using startForegroundService()")
                            val componentName = startForegroundService(intent)
                            Log.d(TAG, "🎯 startForegroundService returned: $componentName")
                        } else {
                            Log.d(TAG, "📱 Android < O, using startService()")
                            val componentName = startService(intent)
                            Log.d(TAG, "🎯 startService returned: $componentName")
                        }
                        
                        Log.d(TAG, "✅ Service start command completed")
                        result.success("Clipboard Service Started ✅")
                        
                    } catch (e: Exception) {
                        Log.e(TAG, "❌ Error starting service: ${e.message}", e)
                        result.error("SERVICE_ERROR", "Failed to start service: ${e.message}", null)
                    }
                }
                "stopService" -> {
                    Log.d(TAG, "🛑 Stopping clipboard service...")
                    
                    try {
                        val intent = Intent(this, ClipboardService::class.java)
                        Log.d(TAG, "📄 Stop intent created: $intent")
                        
                        val stopped = stopService(intent)
                        Log.d(TAG, "🎯 stopService returned: $stopped")
                        
                        Log.d(TAG, "✅ Service stop command completed")
                        result.success("Clipboard Service Stopped ⛔")
                        
                    } catch (e: Exception) {
                        Log.e(TAG, "❌ Error stopping service: ${e.message}", e)
                        result.error("SERVICE_ERROR", "Failed to stop service: ${e.message}", null)
                    }
                }
                else -> {
                    Log.w(TAG, "⚠️ Unknown method call: ${call.method}")
                    result.notImplemented()
                }
            }
        }
        
        Log.d(TAG, "✅ Flutter engine configuration complete")
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "🟢 MainActivity onCreate() called")
        super.onCreate(savedInstanceState)
        Log.d(TAG, "✅ MainActivity onCreate() complete")
    }
    
    override fun onResume() {
        Log.d(TAG, "▶️ MainActivity onResume() called")
        super.onResume()
    }
    
    override fun onPause() {
        Log.d(TAG, "⏸️ MainActivity onPause() called")
        super.onPause()
    }
    
    override fun onDestroy() {
        Log.d(TAG, "🔴 MainActivity onDestroy() called")
        super.onDestroy()
    }
}