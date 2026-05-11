package com.apollo.apollo_card_reader

import android.content.Intent
import android.os.Bundle
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
        private const val CHANNEL = "com.apollo.cardreader/payment"
        private const val EVENT_CHANNEL = "com.apollo.cardreader/events"
    }

    private var transactionFlowController: TransactionFlowController? = null
    private var eventSink: EventChannel.EventSink? = null
    private var pendingAmount: String = "0.00"
    private var transactionStarted = false
    private var connectionTimestamp: Long = 0
    private var retryCount = 0
    private val MAX_RETRIES = 3

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        // Method Channel
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler { call, result ->
            when (call.method) {
                "startTransaction" -> {
                    val amount = call.argument<String>("amount") ?: "0.00"
                    startTransaction(amount)
                    result.success(null)
                }
                "stopTransaction" -> {
                    stopTransaction()
                    result.success(null)
                }
                "sendPin" -> {
                    val pin = call.argument<String>("pin")
                    sendPin(pin)
                    result.success(null)
                }
                "sendConfirmation" -> {
                    sendConfirmation(true)
                    result.success(null)
                }
                else -> result.notImplemented()
            }
        }

        // Event Channel
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

    private fun startTransaction(amount: String) {
        Log.d(TAG, "=== INICIANDO TRANSACCIÓN === Monto: $amount")
        pendingAmount = amount
        transactionStarted = false
        retryCount = 0

        transactionFlowController = TransactionFlowController.getControllerInstance(this, TransactionDelegate())
        transactionFlowController?.connectController()
    }

    private fun stopTransaction() {
        Log.d(TAG, "Deteniendo transacción")
        transactionStarted = false
        transactionFlowController?.apply {
            abortDetection()
            disconnectController()
            releaseControllerInstance()
        }
        transactionFlowController = null
    }

    private fun doStartTransactionFlow() {
        transactionStarted = true
        Log.d(TAG, "=== INICIANDO TRANSACTION FLOW === (intento ${retryCount + 1}) monto: $pendingAmount")

        val data = Hashtable<String, Any>().apply {
            put(TransactionFlowController.EMV_OPTION, TransactionFlowController.EmvOption.START)
            put(TransactionFlowController.CHKCRD_MODE, BaseCardController.CheckCardMode.SWIPE_OR_INSERT_OR_TAP)
            put(TransactionFlowController.AMOUNT, pendingAmount)
            put(TransactionFlowController.CASHBACKAMOUNT, "0")
            put(TransactionFlowController.TRANSACTIONTYPE, TransactionFlowController.TransactionType.GOODS)
            put(TransactionFlowController.CURRENCYCODE, "0840")
            put(TransactionFlowController.EMV_TXNNO, "000001")
            put(TransactionFlowController.EMV_ISCLFINALCONFIRMATIONENABLE, TransactionFlowController.GenericStatus.FALSE)
        }
        transactionFlowController?.startTransactionFlow(data)
    }

    private fun sendPin(pin: String?) {
        // Implementar lógica PIN
        transactionFlowController?.sendPinEntry(null)
    }

    private fun sendConfirmation(confirmed: Boolean) {
        transactionFlowController?.sendConfirmation(confirmed)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTransaction()
    }

    inner class TransactionDelegate : TransactionFlowController.TransactionFlowDelegate {

        private fun sendEvent(eventName: String, data: Map<String, Any?>) {
            runOnUiThread {
                val eventData = mapOf("event" to eventName, "data" to data)
                eventSink?.success(eventData)
            }
        }

        override fun onError(paramError: ControllerError.Error, paramString: String) {
            Log.e(TAG, "Error: $paramError - $paramString")
            sendEvent("error", mapOf(
                "error" to paramError.toString(),
                "message" to paramString
            ))
        }

        override fun onControllerConnected() {
            connectionTimestamp = System.currentTimeMillis()
            Log.d(TAG, "=== CONTROLADOR CONECTADO === (t=0ms)")
            sendEvent("connected", emptyMap<String, Any?>())

            // Esperar 3 segundos para que el hardware se estabilice
            Handler(Looper.getMainLooper()).postDelayed({
                if (transactionFlowController != null && !transactionStarted) {
                    val elapsed = System.currentTimeMillis() - connectionTimestamp
                    Log.d(TAG, "=== DELAY COMPLETADO === (${elapsed}ms tras conexión) - iniciando transaction flow")
                    doStartTransactionFlow()
                }
            }, 3000)
        }

        override fun onControllerDisconnected() {
            Log.d(TAG, "Controlador desconectado")
            sendEvent("disconnected", emptyMap())
        }

        override fun onDeviceInfoReceived(hashtable: Hashtable<String, String>) {
            Log.d(TAG, "Info del dispositivo: $hashtable")
            sendEvent("deviceInfo", mapOf("info" to hashtable.toString()))
        }

        override fun onMessageReceived(messageText: ControllerMessage.MessageText) {
            val elapsed = System.currentTimeMillis() - connectionTimestamp
            Log.d(TAG, "=== MENSAJE: $messageText === (+${elapsed}ms)")
            sendEvent("message", mapOf("text" to messageText.toString()))
        }

        override fun onCardInteractionDetecting(checkCardMode: BaseCardController.CheckCardMode) {
            val elapsed = System.currentTimeMillis() - connectionTimestamp
            Log.d(TAG, "=== DETECTANDO TARJETA: $checkCardMode === (+${elapsed}ms)")
            sendEvent("detecting", mapOf("mode" to checkCardMode.toString()))
        }

        override fun onDetectCardInteractionAborted(b: Boolean) {
            Log.d(TAG, "Detección de tarjeta abortada: $b")
            sendEvent("detectionAborted", mapOf("aborted" to b))
        }

        override fun onCardInteractionDetected(
            checkCardResult: BaseCardController.CheckCardResult,
            hashtable: Hashtable<String, String>?
        ) {
            Log.d(TAG, "Tarjeta detectada: $checkCardResult")
            sendEvent("cardDetected", mapOf(
                "result" to checkCardResult.toString(),
                "data" to (hashtable?.toString() ?: "")
            ))
        }

        override fun onCTLAudioToneReceived(contactlessStatusTone: BaseCardController.ContactlessStatusTone) {
            val elapsed = System.currentTimeMillis() - connectionTimestamp
            Log.d(TAG, "=== TONO AUDIO: $contactlessStatusTone === (+${elapsed}ms)")
        }

        override fun onCTLLightReceived(contactlessStatusLed: BaseCardController.ContactlessStatusLed) {
            val elapsed = System.currentTimeMillis() - connectionTimestamp
            Log.d(TAG, "=== LED: $contactlessStatusLed === (+${elapsed}ms)")

            // Si el dispositivo ahora está listo y la transacción fue terminada por NOT_READY, reintentar
            if (contactlessStatusLed != BaseCardController.ContactlessStatusLed.NOT_READY
                && !transactionStarted
                && transactionFlowController != null
                && retryCount < MAX_RETRIES
            ) {
                retryCount++
                Log.d(TAG, "=== DISPOSITIVO LISTO - Reintentando transaction flow (intento $retryCount) ===")
                doStartTransactionFlow()
            }
        }

        override fun onPpSignalOutReceived(s: String) {
            Log.d(TAG, "Señal PP: $s")
        }

        override fun onSelectAIDRequested(arrayList: ArrayList<ArrayList<String>>) {
            Log.d(TAG, "AID seleccionado")
            transactionFlowController?.selectAID(0)
        }

        override fun onConfirmationRequested(s: String) {
            Log.d(TAG, "Confirmación solicitada: $s")
            val data = BaseCardController.decodeTlv(s)
            val pan = data["5A"] ?: "Tag 5A no encontrado"
            Log.d(TAG, "PAN: $pan")
            sendEvent("confirmationRequested", mapOf(
                "pan" to pan,
                "tlv" to s
            ))
        }

        override fun onOnlineProcessRequested(s: String) {
            Log.d(TAG, "Proceso online solicitado: $s")
            sendEvent("onlineProcessRequested", mapOf("data" to s))
            // Simular respuesta del host
            val hostResp = "8A023030"
            transactionFlowController?.sendOnlineProcessingData(hostResp)
        }

        override fun onBatchDataReceived(s: String) {
            Log.d(TAG, "Datos batch recibidos: $s")
            val data = BaseCardController.decodeTlv(s)
            sendEvent("batchData", mapOf("data" to data.toString()))
        }

        override fun onReversalDataReceived(s: String) {
            Log.d(TAG, "Datos de reversa: $s")
            sendEvent("reversalData", mapOf("data" to s))
        }

        override fun onTransactionStatusReceived(transactionResult: TransactionFlowController.TransactionResult) {
            val elapsed = System.currentTimeMillis() - connectionTimestamp
            Log.d(TAG, "=== ESTADO TRANSACCIÓN: $transactionResult === (+${elapsed}ms)")
            sendEvent("transactionStatus", mapOf("result" to transactionResult.toString()))

            if (transactionResult == TransactionFlowController.TransactionResult.TERMINATED && retryCount < MAX_RETRIES) {
                // No desconectar - esperar a onCTLLightReceived para reintentar
                transactionStarted = false
                Log.d(TAG, "=== TERMINATED - Esperando dispositivo listo para reintento ===")
            } else {
                // Transacción completada o máximo de reintentos alcanzado
                Log.d(TAG, "=== Transacción finalizada - deteniendo ===")
                stopTransaction()
            }
        }

        override fun onPinEntryRequested(
            pinEntrySource: TransactionFlowController.PinEntrySource,
            s: String
        ) {
            Log.d(TAG, "PIN solicitado: $pinEntrySource")
            val isOffline = pinEntrySource == TransactionFlowController.PinEntrySource.PEDDLL_OFFLINE
            sendEvent("pinEntryRequested", mapOf(
                "isOffline" to isOffline,
                "source" to pinEntrySource.toString()
            ))
        }

        override fun onEmvCardDataReceived(b: Boolean, s: String) {
            Log.d(TAG, "Datos EMV recibidos: $s")
            val data = BaseCardController.decodeTlv(s)
            val pan = data["5A"] ?: "Tag 5A no encontrado"
            Log.d(TAG, "PAN: $pan")
            sendEvent("emvCardData", mapOf(
                "pan" to pan,
                "data" to data.toString()
            ))
        }

        override fun onEmvCardNumberReceived(b: Boolean, s: String) {
            Log.d(TAG, "Número de tarjeta EMV: $s")
            sendEvent("emvCardNumber", mapOf("number" to s))
        }

        override fun onSetAmountRequest(s: String) {
            Log.d(TAG, "Monto solicitado: $s")
            sendEvent("setAmountRequest", mapOf("amount" to s))
        }
    }
}
