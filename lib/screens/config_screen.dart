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
  final ValueNotifier<List<String>> _logs = ValueNotifier<List<String>>([]);
  final ValueNotifier<bool> _isConfiguring = ValueNotifier<bool>(false);
  final ValueNotifier<String> _status = ValueNotifier<String>('Esperando inicio...');
  final ValueNotifier<bool> _configCompleted = ValueNotifier<bool>(false);

  @override
  void initState() {
    super.initState();
    _listenToEvents();
  }

  @override
  void dispose() {
    _eventSubscription?.cancel();
    _logs.dispose();
    _isConfiguring.dispose();
    _status.dispose();
    _configCompleted.dispose();
    super.dispose();
  }

  void _listenToEvents() {
    _eventSubscription = eventChannel.receiveBroadcastStream().listen(
      (dynamic event) {
        if (event is Map) {
          final eventName = event['event'];
          _handleEvent(eventName, event['data']);
        }
      },
      onError: (dynamic error) {
        _status.value = 'Error';
      },
    );
  }

  Future<void> _handleEvent(String? eventName, dynamic data) async {
    switch (eventName) {
      case 'configProgress':
        _status.value = 'Configurando: ${data?['phase']}';
        break;
      case 'configSuccess':
        _status.value = '¡Configuración completada!';
        _configCompleted.value = true;
        _isConfiguring.value = false;
        _addLog('✅ Configuración EMV completada exitosamente');
        _showSuccessDialog();
        break;
      case 'configError':
        _status.value = 'Error en configuración';
        _isConfiguring.value = false;
        _addLog('❌ Error: ${data?['error']} - ${data?['message']}');
        break;
      default:
        _status.value = 'Evento: $eventName';
    }
  }

  void _addLog(String message) {
    final timestamp = DateTime.now().toLocal().toIso8601String().split('.')[0];
    _logs.value = [..._logs.value, '$timestamp - $message'];
  }

  Future<void> _startConfiguration() async {
    try {
      _isConfiguring.value = true;
      _status.value = 'Iniciando configuración...';
      _logs.value = [];
      _configCompleted.value = false;
      _addLog('Iniciando configuración EMV...');
      await configChannel.invokeMethod('configureEmv');
    } catch (e) {
      _addLog('Error al iniciar configuración: $e');
      _status.value = 'Error al iniciar';
      _isConfiguring.value = false;
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
              Navigator.pop(context, true);
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
              ValueListenableBuilder<bool>(
                valueListenable: _configCompleted,
                builder: (context, configCompleted, _) {
                  return ValueListenableBuilder<bool>(
                    valueListenable: _isConfiguring,
                    builder: (context, isConfiguring, _) {
                      final bgColor = configCompleted
                          ? Colors.green.shade100
                          : isConfiguring
                              ? Colors.orange.shade100
                              : Colors.grey.shade200;

                      final borderColor = configCompleted
                          ? Colors.green
                          : isConfiguring
                              ? Colors.orange
                              : Colors.grey;

                      return Container(
                        padding: const EdgeInsets.all(16),
                        decoration: BoxDecoration(
                          color: bgColor,
                          borderRadius: BorderRadius.circular(8),
                          border: Border.all(
                            color: borderColor,
                            width: 2,
                          ),
                        ),
                        child: Column(
                          children: [
                            Icon(
                              configCompleted
                                  ? Icons.check_circle
                                  : isConfiguring
                                      ? Icons.settings
                                      : Icons.settings_suggest,
                              size: 48,
                              color: borderColor,
                            ),
                            const SizedBox(height: 8),
                            ValueListenableBuilder<String>(
                              valueListenable: _status,
                              builder: (context, status, _) {
                                return Text(
                                  status,
                                  style: Theme.of(context).textTheme.titleLarge?.copyWith(
                                        color: configCompleted
                                            ? Colors.green.shade900
                                            : isConfiguring
                                                ? Colors.orange.shade900
                                                : Colors.grey.shade700,
                                        fontWeight: FontWeight.bold,
                                      ),
                                  textAlign: TextAlign.center,
                                );
                              },
                            ),
                            if (isConfiguring) ...[
                              const SizedBox(height: 16),
                              const CircularProgressIndicator(),
                            ],
                          ],
                        ),
                      );
                    },
                  );
                },
              ),
              const SizedBox(height: 16),

              ValueListenableBuilder<bool>(
                valueListenable: _isConfiguring,
                builder: (context, isConfiguring, _) {
                  return ValueListenableBuilder<bool>(
                    valueListenable: _configCompleted,
                    builder: (context, configCompleted, _) {
                      return ElevatedButton.icon(
                        onPressed: isConfiguring ? null : _startConfiguration,
                        icon: const Icon(Icons.settings),
                        label: Text(isConfiguring ? 'Configurando...' : 'Iniciar Configuración EMV'),
                        style: ElevatedButton.styleFrom(
                          padding: const EdgeInsets.symmetric(vertical: 16),
                          backgroundColor: configCompleted ? Colors.green : null,
                        ),
                      );
                    },
                  );
                },
              ),
              const SizedBox(height: 16),

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
                            onPressed: () => _logs.value = [],
                            child: const Text('Limpiar'),
                          ),
                        ],
                      ),
                      const Divider(),
                      ValueListenableBuilder<List<String>>(
                        valueListenable: _logs,
                        builder: (context, logs, _) {
                          return Expanded(
                            child: ListView.builder(
                              itemCount: logs.length,
                              itemBuilder: (context, index) {
                                final log = logs[index];
                                return Padding(
                                  padding: const EdgeInsets.symmetric(vertical: 2),
                                  child: Text(
                                    log,
                                    style: Theme.of(context).textTheme.bodySmall,
                                  ),
                                );
                              },
                            ),
                          );
                        },
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
