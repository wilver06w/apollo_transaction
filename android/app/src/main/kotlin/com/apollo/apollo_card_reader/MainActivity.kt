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
        private const val CHANNEL = "com.apollo.cardreader/payment"
        private const val EVENT_CHANNEL = "com.apollo.cardreader/events"
    }

    private var transactionFlowController: TransactionFlowController? = null
    private var eventSink: EventChannel.EventSink? = null
    private var connectionTimestamp: Long = 0

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        // Method Channel
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler { call, result ->
            when (call.method) {
                "startTransaction" -> {
                    startTransaction()
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

    private fun startTransaction() {
        Log.d(TAG, "=== INICIANDO LECTURA DE TARJETA ===")
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

    private fun sendPin(pin: String?) {
        transactionFlowController?.sendPinEntry(pin)
    }

    private fun sendConfirmation(confirmed: Boolean) {
        transactionFlowController?.sendConfirmation(confirmed)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTransaction()
    }

    inner class CardReaderDelegate : TransactionFlowController.TransactionFlowDelegate {

        private fun sendEvent(eventName: String, data: Map<String, Any?>) {
            runOnUiThread {
                val eventData = mapOf("event" to eventName, "data" to data)
                eventSink?.success(eventData)
            }
        }

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

            // Esperar estabilización del hardware y luego iniciar detección de tarjeta
            Handler(Looper.getMainLooper()).postDelayed({
                if (transactionFlowController != null) {
                    Log.d(TAG, "=== Iniciando detección de tarjeta (solo chip/banda) === (+${elapsed()}ms)")
                    val data = Hashtable<String, Any>().apply {
                        put(BaseCardController.CHKCRD_MODE, BaseCardController.CheckCardMode.SWIPE_OR_INSERT)
                        put(BaseCardController.CHKCRD_TIMEOUT, 60)
                    }
                    transactionFlowController?.detectCardInteraction(data)
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

            // Si se insertó chip, solicitar datos EMV
            if (checkCardResult == BaseCardController.CheckCardResult.INSERTED_CARD) {
                Log.d(TAG, "=== Tarjeta insertada - solicitando datos EMV ===")
                transactionFlowController?.getEmvCardData()
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
