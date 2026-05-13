import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class TransactionScreen extends StatefulWidget {
  const TransactionScreen({super.key});

  @override
  State<TransactionScreen> createState() => _TransactionScreenState();
}

class _TransactionScreenState extends State<TransactionScreen> {
  static const transactionChannel = MethodChannel('com.apollo.cardreader/transaction');
  static const eventChannel = EventChannel('com.apollo.cardreader/events');

  StreamSubscription? _eventSubscription;
  final ValueNotifier<List<String>> _logs = ValueNotifier<List<String>>([]);
  final ValueNotifier<String> _status = ValueNotifier<String>('Desconectado');
  final ValueNotifier<bool> _isReading = ValueNotifier<bool>(false);
  final TextEditingController _amountController = TextEditingController(text: '10.00');

  @override
  void initState() {
    super.initState();
    _listenToEvents();
  }

  @override
  void dispose() {
    _eventSubscription?.cancel();
    _amountController.dispose();
    _logs.dispose();
    _status.dispose();
    _isReading.dispose();
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
      case 'connected':
        _status.value = 'Conectado';
        break;
      case 'disconnected':
        _status.value = 'Desconectado';
        _isReading.value = false;
        break;
      case 'detecting':
        _status.value = 'Detectando tarjeta...';
        break;
      case 'cardDetected':
        _status.value = '¡Tarjeta detectada!';
        _addLog('Resultado: ${data?['result']}');
        if (data?.containsKey('data') == true) {
          _addLog('Track data: ${data?['data']}');
        }
        break;
      case 'confirmationRequested':
        _status.value = 'Confirmación requerida';
        _addLog('PAN: ${data?['pan']}');
        _showConfirmationDialog(data?['pan'] ?? 'No disponible');
        break;
      case 'transactionStatus':
        _status.value = 'Transacción: ${data?['result']}';
        _isReading.value = false;
        _addLog('Estado final: ${data?['result']}');
        break;
      case 'error':
        _status.value = 'Error: ${data?['error']}';
        _addLog('Error: ${data?['message']}');
        _isReading.value = false;
        break;
      case 'emvCardData':
        _status.value = 'Datos EMV recibidos';
        if (data is Map && data.containsKey('pan')) {
          _addLog('PAN: ${data['pan']}');
          _addLog('Todos los datos: ${data['data']}');
        }
        break;
      default:
        _status.value = 'Evento: $eventName';
    }
  }

  void _addLog(String message) {
    final timestamp = DateTime.now().toLocal().toIso8601String().split('.')[0];
    _logs.value = [..._logs.value, '$timestamp - $message'];
  }

  Future<void> _startTransaction() async {
    try {
      _isReading.value = true;
      _status.value = 'Iniciando...';
      _logs.value = [];
      _addLog('Iniciando transacción con monto: ${_amountController.text}');
      await transactionChannel.invokeMethod('startTransaction', {
        'amount': _amountController.text,
      });
    } catch (e) {
      _addLog('Error al iniciar: $e');
      _status.value = 'Error al iniciar';
      _isReading.value = false;
    }
  }

  Future<void> _stopTransaction() async {
    try {
      await transactionChannel.invokeMethod('stopTransaction');
      _addLog('Transacción detenida');
      _isReading.value = false;
    } catch (e) {
      _addLog('Error al detener: $e');
    }
  }

  Future<void> _sendConfirmation(bool confirmed) async {
    try {
      await transactionChannel.invokeMethod('sendConfirmation', {'confirmed': confirmed});
      _addLog(confirmed ? 'Confirmación enviada: ACEPTAR' : 'Confirmación enviada: RECHAZAR');
      if (!confirmed) {
        await _stopTransaction();
      }
    } catch (e) {
      _addLog('Error al enviar confirmación: $e');
    }
  }

  void _showConfirmationDialog(String pan) {
    showDialog(
      context: context,
      barrierDismissible: false,
      builder: (context) => AlertDialog(
        title: const Text('Confirmar Transacción'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text('¿Desea confirmar esta transacción?'),
            const SizedBox(height: 8),
            Text(
              'Tarjeta: $pan',
              style: const TextStyle(fontWeight: FontWeight.bold),
            ),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () {
              Navigator.pop(context);
              _sendConfirmation(false);
            },
            child: const Text('Cancelar'),
          ),
          ElevatedButton(
            onPressed: () {
              Navigator.pop(context);
              _sendConfirmation(true);
            },
            child: const Text('Confirmar'),
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
        title: const Text('Lectura de Tarjetas'),
        actions: [
          IconButton(
            icon: const Icon(Icons.home),
            onPressed: () => Navigator.pop(context),
            tooltip: 'Volver al inicio',
          ),
        ],
      ),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            ValueListenableBuilder<bool>(
              valueListenable: _isReading,
              builder: (context, isReading, _) {
                return Container(
                  padding: const EdgeInsets.all(16),
                  decoration: BoxDecoration(
                    color: isReading ? Colors.green.shade100 : Colors.grey.shade200,
                    borderRadius: BorderRadius.circular(8),
                  ),
                  child: Column(
                    children: [
                      Icon(
                        Icons.credit_card,
                        size: 48,
                        color: isReading ? Colors.green : Colors.grey,
                      ),
                      const SizedBox(height: 8),
                      ValueListenableBuilder<String>(
                        valueListenable: _status,
                        builder: (context, status, _) {
                          return Text(
                            status,
                            style: Theme.of(context).textTheme.titleLarge,
                            textAlign: TextAlign.center,
                          );
                        },
                      ),
                    ],
                  ),
                );
              },
            ),
            const SizedBox(height: 16),

            TextField(
              controller: _amountController,
              keyboardType: const TextInputType.numberWithOptions(decimal: true),
              decoration: const InputDecoration(
                labelText: 'Monto de la transacción',
                border: OutlineInputBorder(),
                prefixText: '\$ ',
              ),
              enabled: !_isReading.value,
            ),
            const SizedBox(height: 16),

            ValueListenableBuilder<bool>(
              valueListenable: _isReading,
              builder: (context, isReading, _) {
                return Row(
                  children: [
                    Expanded(
                      child: ElevatedButton(
                        onPressed: isReading ? null : _startTransaction,
                        style: ElevatedButton.styleFrom(
                          padding: const EdgeInsets.symmetric(vertical: 16),
                        ),
                        child: Text(isReading ? 'Leyendo...' : 'Iniciar Lectura'),
                      ),
                    ),
                    const SizedBox(width: 8),
                    Expanded(
                      child: ElevatedButton(
                        onPressed: isReading ? _stopTransaction : null,
                        style: ElevatedButton.styleFrom(
                          padding: const EdgeInsets.symmetric(vertical: 16),
                          backgroundColor: Colors.red,
                          foregroundColor: Colors.white,
                        ),
                        child: const Text('Detener'),
                      ),
                    ),
                  ],
                );
              },
            ),
            const SizedBox(height: 16),

            Expanded(
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
                          'Logs de Transacción',
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
    );
  }
}
