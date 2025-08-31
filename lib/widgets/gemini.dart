import 'package:google_generative_ai/google_generative_ai.dart';

class GeminiService {
  static const String _apiKey = 'key';
  
  static GenerativeModel? _model;
  
  static GenerativeModel get model {
    _model ??= GenerativeModel(
      model: 'gemini-1.5-flash',
      apiKey: _apiKey,
    );
    return _model!;
  }
  
  static Future<String> processText(String text) async {
    try {
      // Create a prompt for processing clipboard content
      final prompt = '''
        correct the gramatical mistakes,spelling mistakes in the following sentence $text
      ''';
      
      final content = [Content.text(prompt)];
      final response = await model.generateContent(content);
      
      return response.text ?? 'Unable to process content';
    } catch (e) {
      print('Gemini API error: $e');
      return 'Error processing content: ${e.toString()}';
    }
  }
}