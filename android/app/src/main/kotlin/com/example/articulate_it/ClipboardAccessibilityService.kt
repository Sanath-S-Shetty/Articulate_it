// package com.example.articulate_it

// import android.accessibilityservice.AccessibilityService
// import android.content.ClipboardManager
// import android.content.Context
// import android.util.Log
// import android.view.accessibility.AccessibilityEvent
// import okhttp3.*
// import okhttp3.MediaType.Companion.toMediaTypeOrNull
// import org.json.JSONObject
// import java.io.IOException

// class ClipboardAccessibilityService : AccessibilityService() {

//     private lateinit var clipboardManager: ClipboardManager
//     private val client = OkHttpClient()
//     private var lastClipboardText = ""

//     companion object {
//         private const val TAG = "ClipboardAccess"
//     }

//     override fun onServiceConnected() {
//         super.onServiceConnected()
//         Log.d(TAG, "🟢 Accessibility service connected")

//         clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

//         // Set up the clipboard listener
//         clipboardManager.addPrimaryClipChangedListener {
//             Log.d(TAG, "📝 🎉 CLIPBOARD CHANGED DETECTED VIA ACCESSIBILITY! 🎉")
//             handleClipboardChange()
//         }

//         Log.d(TAG, "✅ Clipboard listener setup via accessibility service")
//     }

//     private fun handleClipboardChange() {
//         try {
//             val clip = clipboardManager.primaryClip
//             if (clip != null && clip.itemCount > 0) {
//                 val text = clip.getItemAt(0)?.text?.toString() ?: ""
                
//                 // Avoid duplicate processing
//                 if (text != lastClipboardText && text.isNotEmpty()) {
//                     lastClipboardText = text
//                     Log.d(TAG, "✅ New unique text detected, processing...")
//                     sendToGemini(text)
//                 } else {
//                     Log.d(TAG, "⏭️ Duplicate or empty text, skipping...")
//                 }
//             }
//         } catch (e: Exception) {
//             Log.e(TAG, "❌ Error handling clipboard: ${e.message}", e)
//         }
//     }

//     private fun sendToGemini(text: String) {
//         Log.d(TAG, "🌐 Sending to Gemini: '${text.take(30)}...'")

//         try {
//             val apiKey = "YOUR_GEMINI_API_KEY" // ⚠️ Replace with your key
//             val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=$apiKey"

//             val jsonObject = JSONObject().apply {
//                 put("contents", arrayOf(
//                     JSONObject().apply {
//                         put("parts", arrayOf(
//                             JSONObject().apply { put("text", text) }
//                         ))
//                     }
//                 ))
//             }

//             val body = RequestBody.create("application/json".toMediaTypeOrNull(), jsonObject.toString())
//             val request = Request.Builder().url(url).post(body).build()

//             client.newCall(request).enqueue(object : Callback {
//                 override fun onFailure(call: Call, e: IOException) {
//                     Log.e(TAG, "❌ Gemini API failed: ${e.message}")
//                 }

//                 override fun onResponse(call: Call, response: Response) {
//                     Log.d(TAG, "✅ Gemini API success: ${response.code}")
//                     val responseBody = response.body?.string()
//                     Log.d(TAG, "📄 Response: ${responseBody?.take(100)}...")
//                     response.close()
//                 }
//             })
//         } catch (e: Exception) {
//             Log.e(TAG, "❌ Error sending to Gemini: ${e.message}", e)
//         }
//     }

//     override fun onAccessibilityEvent(event: AccessibilityEvent?) {
//         // Not needed for clipboard monitoring, but this method is required
//     }

//     override fun onInterrupt() {
//         Log.d(TAG, "🔴 Accessibility service interrupted")
//     }

//     override fun onDestroy() {
//         Log.d(TAG, "🔴 Accessibility service destroyed")
//         super.onDestroy()
//     }
// }