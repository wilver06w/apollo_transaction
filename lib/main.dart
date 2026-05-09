import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

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
      home: const CardReaderPage(),
    );
  }
}

class CardReaderPage extends StatefulWidget {
  const CardReaderPage({super.key});

  @override
  State<CardReaderPage> createState() => _CardReaderPageState();
}

class _CardReaderPageState extends State<CardReaderPage> {
  static const platform = MethodChannel('com.apollo.cardreader/payment');
  static const eventChannel = EventChannel('com.apollo.cardreader/events');

  StreamSubscription? _eventSubscription;
  List<String> _logs = [];
  String _status = 'Desconectado';
  bool _isReading = false;
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
    super.dispose();
  }

  void _listenToEvents() {
    _eventSubscription = eventChannel.receiveBroadcastStream().listen(
      (dynamic event) {
        _addLog('Evento recibido: $event');
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

  void _handleEvent(String? eventName, dynamic data) {
    setState(() {
      switch (eventName) {
        case 'connected':
          _status = 'Conectado - Iniciando transacción';
          break;
        case 'disconnected':
          _status = 'Desconectado';
          _isReading = false;
          break;
        case 'detecting':
          _status = 'Detectando tarjeta...';
          break;
        case 'cardDetected':
          _status = '¡Tarjeta detectada!';
          break;
        case 'emvCardData':
          _status = 'Datos EMV recibidos';
          if (data is Map && data.containsKey('pan')) {
            _addLog('PAN: ${data['pan']}');
          }
          break;
        case 'confirmationRequested':
          _status = 'Confirmación requerida';
          _addLog('PAN: ${data?['pan']}');
          _showConfirmationDialog();
          break;
        case 'transactionStatus':
          _status = 'Transacción finalizada: ${data?['result']}';
          _isReading = false;
          break;
        case 'error':
          _status = 'Error: ${data?['error']}';
          _addLog('Error: ${data?['message']}');
          _isReading = false;
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

  Future<void> _startTransaction() async {
    try {
      setState(() {
        _isReading = true;
        _status = 'Iniciando...';
        _logs.clear();
      });
      _addLog('Iniciando transacción con monto: ${_amountController.text}');
      await platform.invokeMethod('startTransaction', {
        'amount': _amountController.text,
      });
    } catch (e) {
      _addLog('Error al iniciar: $e');
      setState(() {
        _status = 'Error al iniciar';
        _isReading = false;
      });
    }
  }

  Future<void> _stopTransaction() async {
    try {
      await platform.invokeMethod('stopTransaction');
      _addLog('Transacción detenida');
    } catch (e) {
      _addLog('Error al detener: $e');
    }
  }

  Future<void> _sendConfirmation() async {
    try {
      await platform.invokeMethod('sendConfirmation');
      _addLog('Confirmación enviada');
    } catch (e) {
      _addLog('Error al enviar confirmación: $e');
    }
  }

  void _showConfirmationDialog() {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Confirmar Transacción'),
        content: const Text('¿Desea confirmar esta transacción?'),
        actions: [
          TextButton(
            onPressed: () {
              Navigator.pop(context);
              _stopTransaction();
            },
            child: const Text('Cancelar'),
          ),
          ElevatedButton(
            onPressed: () {
              Navigator.pop(context);
              _sendConfirmation();
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
        title: const Text('Apollo Card Reader'),
      ),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            // Estado
            Container(
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                color: _isReading ? Colors.green.shade100 : Colors.grey.shade200,
                borderRadius: BorderRadius.circular(8),
              ),
              child: Column(
                children: [
                  Icon(
                    Icons.credit_card,
                    size: 48,
                    color: _isReading ? Colors.green : Colors.grey,
                  ),
                  const SizedBox(height: 8),
                  Text(
                    _status,
                    style: Theme.of(context).textTheme.titleLarge,
                    textAlign: TextAlign.center,
                  ),
                ],
              ),
            ),
            const SizedBox(height: 16),

            // Monto
            TextField(
              controller: _amountController,
              keyboardType: const TextInputType.numberWithOptions(decimal: true),
              decoration: const InputDecoration(
                labelText: 'Monto de la transacción',
                border: OutlineInputBorder(),
                prefixText: '\$ ',
              ),
              enabled: !_isReading,
            ),
            const SizedBox(height: 16),

            // Botones
            Row(
              children: [
                Expanded(
                  child: ElevatedButton(
                    onPressed: _isReading ? null : _startTransaction,
                    style: ElevatedButton.styleFrom(
                      padding: const EdgeInsets.symmetric(vertical: 16),
                    ),
                    child: Text(_isReading ? 'Leyendo...' : 'Iniciar Lectura'),
                  ),
                ),
                const SizedBox(width: 8),
                Expanded(
                  child: ElevatedButton(
                    onPressed: _isReading ? _stopTransaction : null,
                    style: ElevatedButton.styleFrom(
                      padding: const EdgeInsets.symmetric(vertical: 16),
                      backgroundColor: Colors.red,
                      foregroundColor: Colors.white,
                    ),
                    child: const Text('Detener'),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 16),

            // Logs
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
                          'Logs',
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
    );
  }
}
