import 'package:flutter/material.dart';
import 'theme/app_theme.dart';
import 'screens/splash_page.dart';
import 'screens/login_page.dart';

// Comentario: Rutas simples para Splash y Login
void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      title: 'Ruti',
      theme: AppTheme.light(),
      initialRoute: '/',
      routes: {
        '/': (_) => const SplashPage(),
        '/login': (_) => const LoginPage(),
      },
    );
  }
}
