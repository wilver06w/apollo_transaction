import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'screens/config_screen.dart';
import 'screens/transaction_screen.dart';

void main() {
  runApp(const ApolloCardReaderApp());
}

class ApolloCardReaderApp extends StatelessWidget {
  const ApolloCardReaderApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Apollo Card Reader',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.deepPurple),
        useMaterial3: true,
      ),
      home: const MainScreen(),
    );
  }
}

class MainScreen extends StatefulWidget {
  const MainScreen({super.key});

  @override
  State<MainScreen> createState() => _MainScreenState();
}

class _MainScreenState extends State<MainScreen> {
  final ValueNotifier<bool> _emvConfigured = ValueNotifier<bool>(false);

  @override
  void initState() {
    super.initState();
    _checkEmvConfiguration();
  }

  @override
  void dispose() {
    _emvConfigured.dispose();
    super.dispose();
  }

  Future<void> _checkEmvConfiguration() async {
    final prefs = await SharedPreferences.getInstance();
    _emvConfigured.value = prefs.getBool('emv_configured') ?? false;
  }

  Future<void> _navigateToConfig() async {
    final result = await Navigator.push<bool>(
      context,
      MaterialPageRoute(builder: (context) => const ConfigScreen()),
    );

    if (result == true) {
      final prefs = await SharedPreferences.getInstance();
      await prefs.setBool('emv_configured', true);
      _emvConfigured.value = true;

      if (mounted) {
        showDialog(
          context: context,
          barrierDismissible: false,
          builder: (context) => const AlertDialog(
            content: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                CircularProgressIndicator(),
                SizedBox(height: 16),
                Text('Procesando configuración EMV...'),
                SizedBox(height: 8),
                Text('Por favor espere', style: TextStyle(fontSize: 12)),
              ],
            ),
          ),
        );

        await Future.delayed(const Duration(seconds: 3));

        if (mounted) {
          Navigator.pop(context);
          if (mounted) {
            showDialog(
              context: context,
              builder: (context) => AlertDialog(
                title: const Text('Configuración Lista'),
                content: const Text('El lector está listo para procesar tarjetas chip.'),
                actions: [
                  TextButton(
                    onPressed: () => Navigator.pop(context),
                    child: const Text('OK'),
                  ),
                ],
              ),
            );
          }
        }
      }
    }
  }

  void _navigateToTransactions() {
    Navigator.push(
      context,
      MaterialPageRoute(builder: (context) => const TransactionScreen()),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        backgroundColor: Theme.of(context).colorScheme.inversePrimary,
        title: const Text('Apollo Card Reader'),
      ),
      body: SingleChildScrollView(
        child: Padding(
          padding: const EdgeInsets.all(16.0),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              Icon(
                Icons.credit_card,
                size: 100,
                color: Theme.of(context).colorScheme.primary,
              ),
              const SizedBox(height: 32),

              Text(
                'Apollo Card Reader',
                style: Theme.of(context).textTheme.headlineMedium?.copyWith(
                      fontWeight: FontWeight.bold,
                    ),
                textAlign: TextAlign.center,
              ),
              const SizedBox(height: 8),

              Text(
                'Sistema de lectura de tarjetas EMV',
                style: Theme.of(context).textTheme.bodyLarge?.copyWith(
                      color: Colors.grey.shade600,
                    ),
                textAlign: TextAlign.center,
              ),
              const SizedBox(height: 48),

              ValueListenableBuilder<bool>(
                valueListenable: _emvConfigured,
                builder: (context, emvConfigured, _) {
                  return Container(
                    padding: const EdgeInsets.all(16),
                    decoration: BoxDecoration(
                      color: emvConfigured ? Colors.green.shade50 : Colors.orange.shade50,
                      borderRadius: BorderRadius.circular(8),
                      border: Border.all(
                        color: emvConfigured ? Colors.green : Colors.orange,
                        width: 2,
                      ),
                    ),
                    child: Column(
                      children: [
                        Row(
                          children: [
                            Icon(
                              emvConfigured ? Icons.check_circle : Icons.warning,
                              color: emvConfigured ? Colors.green : Colors.orange,
                            ),
                            const SizedBox(width: 12),
                            Expanded(
                              child: Text(
                                emvConfigured
                                    ? 'Configuración EMV: Completada'
                                    : 'Configuración EMV: Requerida',
                                style: TextStyle(
                                  fontWeight: FontWeight.bold,
                                  color: emvConfigured
                                      ? Colors.green.shade900
                                      : Colors.orange.shade900,
                                ),
                              ),
                            ),
                          ],
                        ),
                        if (!emvConfigured) ...[
                          const SizedBox(height: 8),
                          const Text(
                            'Debe configurar EMV antes de procesar tarjetas chip',
                            style: TextStyle(fontSize: 12),
                          ),
                        ],
                      ],
                    ),
                  );
                },
              ),
              const SizedBox(height: 32),

              ElevatedButton.icon(
                onPressed: _navigateToConfig,
                icon: const Icon(Icons.settings),
                label: const Text('Configurar EMV'),
                style: ElevatedButton.styleFrom(
                  padding: const EdgeInsets.symmetric(vertical: 16),
                  backgroundColor: Colors.amber,
                ),
              ),
              const SizedBox(height: 16),

              ElevatedButton.icon(
                onPressed: _navigateToTransactions,
                icon: const Icon(Icons.payment),
                label: const Text('Ir a Transacciones'),
                style: ElevatedButton.styleFrom(
                  padding: const EdgeInsets.symmetric(vertical: 16),
                ),
              ),
              const SizedBox(height: 32),

              Container(
                padding: const EdgeInsets.all(12),
                decoration: BoxDecoration(
                  color: Colors.blue.shade50,
                  borderRadius: BorderRadius.circular(8),
                  border: Border.all(color: Colors.blue.shade200),
                ),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        Icon(Icons.info_outline, color: Colors.blue.shade700, size: 20),
                        const SizedBox(width: 8),
                        Text(
                          'Instrucciones',
                          style: TextStyle(
                            fontWeight: FontWeight.bold,
                            color: Colors.blue.shade900,
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 8),
                    Text(
                      '1. Presione "Configurar EMV" para configurar el lector la primera vez\n'
                      '2. Presione "Ir a Transacciones" para procesar tarjetas\n'
                      '3. La configuración se guarda en el lector, no es necesario repetirla',
                      style: TextStyle(fontSize: 12, color: Colors.blue.shade900),
                    ),
                  ],
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
