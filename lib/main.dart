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
      theme: ThemeData(
        primarySwatch: Colors.deepPurple,
        brightness: Brightness.dark,
        scaffoldBackgroundColor: const Color(0xFF121212),
        elevatedButtonTheme: ElevatedButtonThemeData(
          style: ElevatedButton.styleFrom(
            backgroundColor: Colors.deepPurple,
            foregroundColor: Colors.white,
          ),
        ),
      ),
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
  static const _platform = MethodChannel("clipboard_service");
  final _apiKeyController = TextEditingController();

  void _showSnackBar(String message) {
    if (mounted) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text(message)),
      );
    }
  }

  Future<void> _startService() async {
    if (_apiKeyController.text.isEmpty) {
      _showSnackBar("Please enter your Gemini API Key.");
      return;
    }
    try {
      final String result = await _platform.invokeMethod("startService", {
        "apiKey": _apiKeyController.text,
      });
      _showSnackBar(result);
    } on PlatformException catch (e) {
      _showSnackBar("Failed to start service: ${e.message}");
    }
  }

  Future<void> _stopService() async {
    try {
      final String result = await _platform.invokeMethod("stopService");
      _showSnackBar(result);
    } on PlatformException catch (e) {
      _showSnackBar("Failed to stop service: ${e.message}");
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text("Clipboard AI 🤖")),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            TextField(
              controller: _apiKeyController,
              decoration: const InputDecoration(
                labelText: "Gemini API Key",
                border: OutlineInputBorder(),
              ),
              obscureText: true,
            ),
            const SizedBox(height: 24),
            ElevatedButton(
              onPressed: _startService,
              child: const Text("Start Listening"),
            ),
            const SizedBox(height: 16),
            ElevatedButton(
              onPressed: _stopService,
              child: const Text("Stop Listening"),
            ),
          ],
        ),
      ),
    );
  }
}