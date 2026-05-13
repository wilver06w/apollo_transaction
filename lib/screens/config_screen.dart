import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class ConfigScreen extends StatefulWidget {
  const ConfigScreen({super.key});

  @override
  State<ConfigScreen> createState() => _ConfigScreenState();
}

class _ConfigScreenState extends State<ConfigScreen> {
  static const configChannel = MethodChannel('com.apollo.cardreader/config');
  static const eventChannel = EventChannel('com.apollo.cardreader/events');

  StreamSubscription? _eventSubscription;
  List<String> _logs = [];
  bool _isConfiguring = false;
  String _status = 'Esperando inicio...';
  bool _configCompleted = false;

  @override
  void initState() {
    super.initState();
    _listenToEvents();
  }

  @override
  void dispose() {
    _eventSubscription?.cancel();
    super.dispose();
  }

  void _listenToEvents() {
    _eventSubscription = eventChannel.receiveBroadcastStream().listen(
      (dynamic event) {
        _addLog('Evento: $event');
        if (event is Map) {
          final eventName = event['event'];
          _handleEvent(eventName, event['data']);
        }
      },
      onError: (dynamic error) {
        _addLog('Error: $error');
        setState(() => _status = 'Error');
      },
    );
  }

  Future<void> _handleEvent(String? eventName, dynamic data) async {
    setState(() {
      switch (eventName) {
        case 'configProgress':
          _status = 'Configurando: ${data?['phase']}';
          break;
        case 'configSuccess':
          _status = '¡Configuración completada!';
          _configCompleted = true;
          _isConfiguring = false;
          _addLog('✅ Configuración EMV completada exitosamente');
          _showSuccessDialog();
          break;
        case 'configError':
          _status = 'Error en configuración';
          _isConfiguring = false;
          _addLog('❌ Error: ${data?['error']} - ${data?['message']}');
          break;
        default:
          _status = 'Evento: $eventName';
      }
    });
  }

  void _addLog(String message) {
    setState(() {
      _logs.add('${DateTime.now().toLocal().toIso8601String().split('.')[0]} - $message');
    });
  }

  Future<void> _startConfiguration() async {
    try {
      setState(() {
        _isConfiguring = true;
        _status = 'Iniciando configuración...';
        _logs.clear();
        _configCompleted = false;
      });
      _addLog('Iniciando configuración EMV...');
      await configChannel.invokeMethod('configureEmv');
    } catch (e) {
      _addLog('Error al iniciar configuración: $e');
      setState(() {
        _status = 'Error al iniciar';
        _isConfiguring = false;
      });
    }
  }

  void _showSuccessDialog() {
    showDialog(
      context: context,
      barrierDismissible: false,
      builder: (context) => AlertDialog(
        title: const Text('Configuración EMV Exitosa'),
        content: const Text(
          'La configuración EMV se ha completado exitosamente.\n\n'
          'El lector ahora puede procesar tarjetas chip con las configuraciones de:\n'
          '• Visa\n'
          '• Mastercard\n'
          '• American Express\n'
          '• Discover\n'
          '• JCB\n'
          '• UnionPay',
        ),
        actions: [
          ElevatedButton(
            onPressed: () {
              Navigator.pop(context);
              Navigator.pop(context, true); // Volver a main con éxito
            },
            child: const Text('Continuar a Transacciones'),
          ),
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        backgroundColor: Theme.of(context).colorScheme.inversePrimary,
        title: const Text('Configuración EMV'),
      ),
      body: SingleChildScrollView(
        child: Padding(
          padding: const EdgeInsets.all(16.0),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
            // Panel de estado
            Container(
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                color: _configCompleted
                    ? Colors.green.shade100
                    : _isConfiguring
                        ? Colors.orange.shade100
                        : Colors.grey.shade200,
                borderRadius: BorderRadius.circular(8),
                border: Border.all(
                  color: _configCompleted
                      ? Colors.green
                      : _isConfiguring
                          ? Colors.orange
                          : Colors.grey,
                  width: 2,
                ),
              ),
              child: Column(
                children: [
                  Icon(
                    _configCompleted
                        ? Icons.check_circle
                        : _isConfiguring
                            ? Icons.settings
                            : Icons.settings_suggest,
                    size: 48,
                    color: _configCompleted
                        ? Colors.green
                        : _isConfiguring
                            ? Colors.orange
                            : Colors.grey,
                  ),
                  const SizedBox(height: 8),
                  Text(
                    _status,
                    style: Theme.of(context).textTheme.titleLarge?.copyWith(
                          color: _configCompleted
                              ? Colors.green.shade900
                              : _isConfiguring
                                  ? Colors.orange.shade900
                                  : Colors.grey.shade700,
                          fontWeight: FontWeight.bold,
                        ),
                    textAlign: TextAlign.center,
                  ),
                  if (_isConfiguring) ...[
                    const SizedBox(height: 16),
                    const CircularProgressIndicator(),
                  ],
                ],
              ),
            ),
            const SizedBox(height: 16),

            // Botón de configuración
            ElevatedButton.icon(
              onPressed: _isConfiguring ? null : _startConfiguration,
              icon: const Icon(Icons.settings),
              label: Text(_isConfiguring ? 'Configurando...' : 'Iniciar Configuración EMV'),
              style: ElevatedButton.styleFrom(
                padding: const EdgeInsets.symmetric(vertical: 16),
                backgroundColor: _configCompleted ? Colors.green : null,
              ),
            ),
            const SizedBox(height: 16),

            // Instrucciones
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
                      Expanded(
                        child: Text(
                          'Información',
                          style: TextStyle(
                            fontWeight: FontWeight.bold,
                            color: Colors.blue.shade900,
                          ),
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 8),
                  Text(
                    'La configuración EMV debe realizarse solo una vez. '
                    'El lector recordará la configuración incluso si la app se cierra.',
                    style: TextStyle(fontSize: 12, color: Colors.blue.shade900),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 16),

            // Logs
            SizedBox(
              height: 300,
              child: Container(
                padding: const EdgeInsets.all(8),
                decoration: BoxDecoration(
                  color: Colors.grey.shade100,
                  borderRadius: BorderRadius.circular(8),
                  border: Border.all(color: Colors.grey.shade300),
                ),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: [
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        Text(
                          'Logs de Configuración',
                          style: Theme.of(context).textTheme.titleSmall,
                        ),
                        TextButton(
                          onPressed: () => setState(() => _logs.clear()),
                          child: const Text('Limpiar'),
                        ),
                      ],
                    ),
                    const Divider(),
                    Expanded(
                      child: ListView.builder(
                        itemCount: _logs.length,
                        itemBuilder: (context, index) {
                          final log = _logs[index];
                          return Padding(
                            padding: const EdgeInsets.symmetric(vertical: 2),
                            child: Text(
                              log,
                              style: Theme.of(context).textTheme.bodySmall,
                            ),
                          );
                        },
                      ),
                    ),
                  ],
                ),
              ),
            ),
          ],
        ),
        ),
      ),
    );
  }
}
