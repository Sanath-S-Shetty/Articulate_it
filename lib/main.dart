import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Clipboard AI',
      home: const HomeScreen(),
    );
  }
}

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  static const platform = MethodChannel("clipboard_service");

  Future<void> startService() async {
    print("🚀 Dart: About to call startService");
    
    try {
      print("📞 Dart: Invoking platform method...");
      final result = await platform.invokeMethod("startService");
      print("✅ Dart: Platform method result: $result");
      
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text(result.toString())),
        );
      }
    } on PlatformException catch (e) {
      print("❌ Dart: Platform exception: ${e.message}");
      print("🔍 Dart: Error code: ${e.code}");
      print("🔍 Dart: Error details: ${e.details}");
      
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text("Error: ${e.message}")),
        );
      }
    } catch (e) {
      print("❌ Dart: Unexpected error: $e");
      
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text("Unexpected error: $e")),
        );
      }
    }
    
    print("🏁 Dart: startService method complete");
  }

  Future<void> stopService() async {
    print("🛑 Dart: About to call stopService");
    
    try {
      print("📞 Dart: Invoking platform method...");
      final result = await platform.invokeMethod("stopService");
      print("✅ Dart: Platform method result: $result");
      
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text(result.toString())),
        );
      }
    } on PlatformException catch (e) {
      print("❌ Dart: Platform exception: ${e.message}");
      print("🔍 Dart: Error code: ${e.code}");
      print("🔍 Dart: Error details: ${e.details}");
      
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text("Error: ${e.message}")),
        );
      }
    } catch (e) {
      print("❌ Dart: Unexpected error: $e");
      
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text("Unexpected error: $e")),
        );
      }
    }
    
    print("🏁 Dart: stopService method complete");
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text("Clipboard AI")),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
   
            ElevatedButton(
              onPressed: startService,
              child: const Text("Start Listening"),
            ),
            const SizedBox(height: 16),
            ElevatedButton(
              onPressed: stopService,
              child: const Text("Stop Listening"),
            ),
            const SizedBox(height: 32),
            const Text(
              "Check console logs for detailed debugging info",
              style: TextStyle(fontSize: 12, color: Colors.grey),
            ),
          ],
        ),
      ),
    );
  }
}