package com.apollo.apollo_card_reader

import android.os.Handler
import android.os.Looper
import android.util.Log
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodChannel
import com.spectratech.controllers.BaseCardController
import com.spectratech.controllers.ControllerError
import com.spectratech.controllers.ControllerMessage
import com.spectratech.controllers.TransactionFlowController
import com.spectratech.lib.level1.Ab
import com.spectratech.lib.level1.HStr
import java.util.Hashtable

class MainActivity : FlutterActivity() {
    companion object {
        private const val TAG = "ApolloCardReader"
        private const val EVENT_CHANNEL = "com.apollo.cardreader/events"
        private const val CONFIG_CHANNEL = "com.apollo.cardreader/config"
        private const val TRANSACTION_CHANNEL = "com.apollo.cardreader/transaction"
    }

    private var transactionFlowController: TransactionFlowController? = null
    private var emvConfigManager: EmvConfigManager? = null
    private var eventSink: EventChannel.EventSink? = null
    private var connectionTimestamp: Long = 0
    private var transactionAmount: String = "10.00"

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        // Config Channel - separado para ConfigurationController
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CONFIG_CHANNEL).setMethodCallHandler { call, result ->
            when (call.method) {
                "configureEmv" -> {
                    Log.d(TAG, "=== RECIBIDO: configureEmv ===")
                    configureEmv()
                    result.success(null)
                }
                else -> result.notImplemented()
            }
        }

        // Transaction Channel - separado para TransactionFlowController
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, TRANSACTION_CHANNEL).setMethodCallHandler { call, result ->
            when (call.method) {
                "startTransaction" -> {
                    val amount = call.argument<String>("amount") ?: "10.00"
                    Log.d(TAG, "=== RECIBIDO: startTransaction con monto $amount ===")
                    startTransaction(amount)
                    result.success(null)
                }
                "stopTransaction" -> {
                    stopTransaction()
                    result.success(null)
                }
                "sendConfirmation" -> {
                    val confirmed = call.argument<Boolean>("confirmed") ?: true
                    sendConfirmation(confirmed)
                    result.success(null)
                }
                else -> result.notImplemented()
            }
        }

        // Event Channel - compartido por ambos controladores
        EventChannel(flutterEngine.dartExecutor.binaryMessenger, EVENT_CHANNEL).setStreamHandler(
            object : EventChannel.StreamHandler {
                override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
                    eventSink = events
                }

                override fun onCancel(arguments: Any?) {
                    eventSink = null
                }
            }
        )
    }

    // ==================== CONFIGURATION CONTROLLER ====================

    private fun configureEmv() {
        Log.d(TAG, "=== INICIANDO CONFIGURACIÓN EMV ===")
        emvConfigManager = EmvConfigManager(this, object : EmvConfigManager.EmvConfigCallback {
            override fun onConfigProgress(phase: String, message: String) {
                Log.d(TAG, "=== PROGRESO CONFIGURACIÓN: $phase - $message ===")
                sendEvent("configProgress", mapOf(
                    "phase" to phase,
                    "message" to message
                ))
            }

            override fun onConfigSuccess() {
                Log.d(TAG, "=== CONFIGURACIÓN EMV COMPLETADA ===")
                sendEvent("configSuccess", emptyMap())

                // Dar tiempo al SDK para procesar y guardar la configuración EMV
                Handler(Looper.getMainLooper()).postDelayed({
                    Log.d(TAG, "=== Liberando ConfigurationController después de delay ===")
                    // Importante: detener y liberar el ConfigurationController ANTES de null
                    emvConfigManager?.stopConfiguration()
                    emvConfigManager = null
                }, 2000) // 2 segundos para que la configuración se asiente
            }

            override fun onConfigError(error: String, message: String) {
                Log.e(TAG, "=== ERROR CONFIGURACIÓN EMV: $error - $message ===")
                sendEvent("configError", mapOf(
                    "error" to error,
                    "message" to message
                ))
                emvConfigManager?.stopConfiguration()
                emvConfigManager = null
            }
        })
        emvConfigManager?.startConfiguration()
    }

    private fun stopEmvConfiguration() {
        emvConfigManager?.stopConfiguration()
        emvConfigManager = null
    }

    // ==================== TRANSACTION FLOW CONTROLLER ====================

    private fun startTransaction(amount: String) {
        Log.d(TAG, "=== INICIANDO LECTURA DE TARJETA ===")
        transactionAmount = amount
        transactionFlowController = TransactionFlowController.getControllerInstance(this, CardReaderDelegate())
        transactionFlowController?.connectController()
    }

    private fun stopTransaction() {
        Log.d(TAG, "=== DETENIENDO LECTURA ===")
        transactionFlowController?.apply {
            abortDetection()
            disconnectController()
            releaseControllerInstance()
        }
        transactionFlowController = null
    }

    private fun sendConfirmation(confirmed: Boolean) {
        transactionFlowController?.sendConfirmation(confirmed)
    }

    // ==================== EVENTOS ====================

    private fun sendEvent(eventName: String, data: Map<String, Any?>) {
        runOnUiThread {
            val eventData = mapOf("event" to eventName, "data" to data)
            eventSink?.success(eventData)
        }
    }

    // ==================== LIFECYCLE ====================

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "=== onDestroy - deteniendo todos los controladores ===")
        stopTransaction()
        stopEmvConfiguration()
    }

    // ==================== TRANSACTION FLOW DELEGATE ====================

    inner class CardReaderDelegate : TransactionFlowController.TransactionFlowDelegate {

        private fun elapsed(): Long = System.currentTimeMillis() - connectionTimestamp

        override fun onError(paramError: ControllerError.Error, paramString: String) {
            Log.e(TAG, "=== ERROR: $paramError - $paramString === (+${elapsed()}ms)")
            sendEvent("error", mapOf(
                "error" to paramError.toString(),
                "message" to paramString
            ))
        }

        override fun onControllerConnected() {
            connectionTimestamp = System.currentTimeMillis()
            Log.d(TAG, "=== CONTROLADOR CONECTADO === (t=0ms)")
            sendEvent("connected", emptyMap<String, Any?>())

            // Esperar estabilización del hardware y luego iniciar flujo EMV directamente
            // Siguiendo el patrón de SpectraTech demo: llamar startTransactionFlow() directamente
            // con EMV_OPTION.START en lugar de detectCardInteraction() primero
            Handler(Looper.getMainLooper()).postDelayed({
                if (transactionFlowController != null) {
                    Log.d(TAG, "=== Iniciando flujo de transacción EMV === (+${elapsed()}ms)")
                    Log.d(TAG, "   Monto: $transactionAmount")

                    val data = Hashtable<String, Any>().apply {
                        put(TransactionFlowController.EMV_OPTION, TransactionFlowController.EmvOption.START)
                        put(TransactionFlowController.CHKCRD_MODE, BaseCardController.CheckCardMode.SWIPE_OR_INSERT)
                        put(TransactionFlowController.AMOUNT, transactionAmount)
                        put(TransactionFlowController.CASHBACKAMOUNT, "0")
                        put(TransactionFlowController.TRANSACTIONTYPE, TransactionFlowController.TransactionType.GOODS)
                        put(TransactionFlowController.CURRENCYCODE, "0840")
                        put(TransactionFlowController.EMV_TXNNO, "000001")
                        put(TransactionFlowController.EMV_ISCLFINALCONFIRMATIONENABLE, false)
                    }

                    Log.d(TAG, "=== Parámetros EMV ===")
                    Log.d(TAG, "   EMV_OPTION: ${data[TransactionFlowController.EMV_OPTION]}")
                    Log.d(TAG, "   CHKCRD_MODE: ${data[TransactionFlowController.CHKCRD_MODE]}")
                    Log.d(TAG, "   TRANSACTIONTYPE: ${data[TransactionFlowController.TRANSACTIONTYPE]}")
                    Log.d(TAG, "   CURRENCYCODE: ${data[TransactionFlowController.CURRENCYCODE]}")
                    Log.d(TAG, "   EMV_TXNNO: ${data[TransactionFlowController.EMV_TXNNO]}")

                    transactionFlowController?.startTransactionFlow(data)
                }
            }, 3000)
        }

        override fun onControllerDisconnected() {
            Log.d(TAG, "=== CONTROLADOR DESCONECTADO === (+${elapsed()}ms)")
            sendEvent("disconnected", emptyMap())
        }

        override fun onDeviceInfoReceived(hashtable: Hashtable<String, String>) {
            Log.d(TAG, "=== DEVICE INFO === (+${elapsed()}ms)")
            Log.d(TAG, "   $hashtable")
            sendEvent("deviceInfo", mapOf("info" to hashtable.toString()))
        }

        override fun onMessageReceived(messageText: ControllerMessage.MessageText) {
            Log.d(TAG, "=== MENSAJE: $messageText === (+${elapsed()}ms)")
            sendEvent("message", mapOf("text" to messageText.toString()))
        }

        override fun onCardInteractionDetecting(checkCardMode: BaseCardController.CheckCardMode) {
            Log.d(TAG, "=== DETECTANDO TARJETA: $checkCardMode === (+${elapsed()}ms)")
            sendEvent("detecting", mapOf("mode" to checkCardMode.toString()))
        }

        override fun onDetectCardInteractionAborted(b: Boolean) {
            Log.d(TAG, "=== DETECCIÓN ABORTADA: $b === (+${elapsed()}ms)")
            sendEvent("detectionAborted", mapOf("aborted" to b))
        }

        override fun onCardInteractionDetected(
            checkCardResult: BaseCardController.CheckCardResult,
            hashtable: Hashtable<String, String>?
        ) {
            Log.d(TAG, "=== TARJETA DETECTADA: $checkCardResult === (+${elapsed()}ms)")
            Log.d(TAG, "   Track data: $hashtable")
            sendEvent("cardDetected", mapOf(
                "result" to checkCardResult.toString(),
                "data" to (hashtable?.toString() ?: "")
            ))

            // Siguiendo el patrón de SpectraTech:
            // - Para MSR (swipe), liberamos el controlador
            // - Para chip (INSERTED_CARD), el flujo EMV ya fue iniciado por startTransactionFlow()
            when (checkCardResult) {
                BaseCardController.CheckCardResult.MSR -> {
                    Log.d(TAG, "=== Tarjeta banda magnética detectada - liberando ===")
                    stopTransaction()
                }
                BaseCardController.CheckCardResult.INSERTED_CARD -> {
                    Log.d(TAG, "=== Tarjeta chip detectada - flujo EMV en progreso ===")
                    // El flujo EMV continúa automáticamente
                }
                else -> {
                    Log.d(TAG, "=== Otro tipo de tarjeta: $checkCardResult ===")
                }
            }
        }

        override fun onCTLAudioToneReceived(contactlessStatusTone: BaseCardController.ContactlessStatusTone) {
            Log.d(TAG, "=== TONO AUDIO: $contactlessStatusTone === (+${elapsed()}ms)")
        }

        override fun onCTLLightReceived(contactlessStatusLed: BaseCardController.ContactlessStatusLed) {
            Log.d(TAG, "=== LED: $contactlessStatusLed === (+${elapsed()}ms)")
        }

        override fun onPpSignalOutReceived(s: String) {
            Log.d(TAG, "=== SEÑAL PP: $s === (+${elapsed()}ms)")
        }

        override fun onSelectAIDRequested(arrayList: ArrayList<ArrayList<String>>) {
            Log.d(TAG, "=== AID SOLICITADO === (+${elapsed()}ms)")
            Log.d(TAG, "   AIDs disponibles: $arrayList")
            transactionFlowController?.selectAID(0)
        }

        override fun onConfirmationRequested(s: String) {
            Log.d(TAG, "=== CONFIRMACIÓN SOLICITADA === (+${elapsed()}ms)")
            val data = BaseCardController.decodeTlv(s)
            val pan = data["5A"] ?: "Tag 5A no encontrado"
            Log.d(TAG, "   PAN: $pan")
            sendEvent("confirmationRequested", mapOf(
                "pan" to pan,
                "tlv" to s
            ))
        }

        override fun onOnlineProcessRequested(s: String) {
            Log.d(TAG, "=== PROCESO ONLINE SOLICITADO === (+${elapsed()}ms)")
            Log.d(TAG, "   Data: $s")
            sendEvent("onlineProcessRequested", mapOf("data" to s))
        }

        override fun onBatchDataReceived(s: String) {
            Log.d(TAG, "=== BATCH DATA === (+${elapsed()}ms)")
            Log.d(TAG, "   Raw: $s")
            val data = BaseCardController.decodeTlv(s)
            Log.d(TAG, "   Decoded: $data")
            sendEvent("batchData", mapOf("data" to data.toString()))
        }

        override fun onReversalDataReceived(s: String) {
            Log.d(TAG, "=== REVERSAL DATA === (+${elapsed()}ms)")
            sendEvent("reversalData", mapOf("data" to s))
        }

        override fun onTransactionStatusReceived(transactionResult: TransactionFlowController.TransactionResult) {
            Log.d(TAG, "=== ESTADO TRANSACCIÓN: $transactionResult === (+${elapsed()}ms)")
            sendEvent("transactionStatus", mapOf("result" to transactionResult.toString()))
            stopTransaction()
        }

        override fun onPinEntryRequested(
            pinEntrySource: TransactionFlowController.PinEntrySource,
            s: String
        ) {
            Log.d(TAG, "=== PIN SOLICITADO === fuente: $pinEntrySource (+${elapsed()}ms)")
            sendEvent("pinEntryRequested", mapOf(
                "source" to pinEntrySource.toString()
            ))
        }

        override fun onEmvCardDataReceived(b: Boolean, s: String) {
            Log.d(TAG, "=== DATOS EMV RECIBIDOS === success=$b (+${elapsed()}ms)")
            val data = BaseCardController.decodeTlv(s)
            val pan = data["5A"] ?: "Tag 5A no encontrado"
            Log.d(TAG, "   PAN: $pan")
            Log.d(TAG, "   All tags: $data")
            sendEvent("emvCardData", mapOf(
                "pan" to pan,
                "data" to data.toString()
            ))
        }

        override fun onEmvCardNumberReceived(b: Boolean, s: String) {
            Log.d(TAG, "=== NÚMERO TARJETA EMV === success=$b (+${elapsed()}ms)")
            Log.d(TAG, "   Number: $s")
            sendEvent("emvCardNumber", mapOf("number" to s))
        }

        override fun onSetAmountRequest(s: String) {
            Log.d(TAG, "=== SET AMOUNT REQUEST === $s (+${elapsed()}ms)")
            sendEvent("setAmountRequest", mapOf("amount" to s))
        }
    }
}
