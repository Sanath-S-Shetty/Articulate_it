package com.example.articulate_it

import android.content.Intent
import android.os.Build
import androidx.annotation.NonNull
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity: FlutterActivity() {
    private val CHANNEL = "clipboard_service"

    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler {
            call, result ->
            when (call.method) {
                "startService" -> {
                    val apiKey = call.argument<String>("apiKey")
                    if (apiKey != null) {
                        startClipboardService(apiKey)
                        result.success("Service Started")
                    } else {
                        result.error("UNAVAILABLE", "API Key not provided.", null)
                    }
                }
                "stopService" -> {
                    stopClipboardService()
                    result.success("Service Stopped")
                }
                else -> {
                    result.notImplemented()
                }
            }
        }
    }

    private fun startClipboardService(apiKey: String) {
        val serviceIntent = Intent(this, ClipboardService::class.java)
        serviceIntent.putExtra("apiKey", apiKey)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }

    private fun stopClipboardService() {
        val serviceIntent = Intent(this, ClipboardService::class.java)
        stopService(serviceIntent)
    }
}