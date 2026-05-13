package com.apollo.apollo_card_reader

import android.os.Handler
import android.os.Looper
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

        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CONFIG_CHANNEL).setMethodCallHandler { call, result ->
            when (call.method) {
                "configureEmv" -> {
                    configureEmv()
                    result.success(null)
                }
                else -> result.notImplemented()
            }
        }

        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, TRANSACTION_CHANNEL).setMethodCallHandler { call, result ->
            when (call.method) {
                "startTransaction" -> {
                    val amount = call.argument<String>("amount") ?: "10.00"
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

    private fun configureEmv() {
        emvConfigManager = EmvConfigManager(this, object : EmvConfigManager.EmvConfigCallback {
            override fun onConfigProgress(phase: String, message: String) {
                sendEvent("configProgress", mapOf(
                    "phase" to phase,
                    "message" to message
                ))
            }

            override fun onConfigSuccess() {
                sendEvent("configSuccess", emptyMap())

                Handler(Looper.getMainLooper()).postDelayed({
                    emvConfigManager?.stopConfiguration()
                    emvConfigManager = null
                }, 2000)
            }

            override fun onConfigError(error: String, message: String) {
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

    private fun startTransaction(amount: String) {
        transactionAmount = amount
        transactionFlowController = TransactionFlowController.getControllerInstance(this, CardReaderDelegate())
        transactionFlowController?.connectController()
    }

    private fun stopTransaction() {
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

    override fun onDestroy() {
        super.onDestroy()
        stopTransaction()
        stopEmvConfiguration()
    }

    inner class CardReaderDelegate : TransactionFlowController.TransactionFlowDelegate {

        override fun onError(paramError: ControllerError.Error, paramString: String) {
            sendEvent("error", mapOf(
                "error" to paramError.toString(),
                "message" to paramString
            ))
        }

        override fun onControllerConnected() {
            sendEvent("connected", emptyMap<String, Any?>())

            Handler(Looper.getMainLooper()).postDelayed({
                transactionFlowController?.let {
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
                    it.startTransactionFlow(data)
                }
            }, 3000)
        }

        override fun onControllerDisconnected() {
            sendEvent("disconnected", emptyMap())
        }

        override fun onDeviceInfoReceived(hashtable: Hashtable<String, String>) {
            sendEvent("deviceInfo", mapOf("info" to hashtable.toString()))
        }

        override fun onMessageReceived(messageText: ControllerMessage.MessageText) {
            sendEvent("message", mapOf("text" to messageText.toString()))
        }

        override fun onCardInteractionDetecting(checkCardMode: BaseCardController.CheckCardMode) {
            sendEvent("detecting", mapOf("mode" to checkCardMode.toString()))
        }

        override fun onDetectCardInteractionAborted(b: Boolean) {
            sendEvent("detectionAborted", mapOf("aborted" to b))
        }

        override fun onCardInteractionDetected(
            checkCardResult: BaseCardController.CheckCardResult,
            hashtable: Hashtable<String, String>?
        ) {
            sendEvent("cardDetected", mapOf(
                "result" to checkCardResult.toString(),
                "data" to (hashtable?.toString() ?: "")
            ))

            when (checkCardResult) {
                BaseCardController.CheckCardResult.MSR -> stopTransaction()
                else -> { }
            }
        }

        override fun onCTLAudioToneReceived(contactlessStatusTone: BaseCardController.ContactlessStatusTone) = Unit

        override fun onCTLLightReceived(contactlessStatusLed: BaseCardController.ContactlessStatusLed) = Unit

        override fun onPpSignalOutReceived(s: String) = Unit

        override fun onSelectAIDRequested(arrayList: ArrayList<ArrayList<String>>) {
            transactionFlowController?.selectAID(0)
        }

        override fun onConfirmationRequested(s: String) {
            val data = BaseCardController.decodeTlv(s)
            val pan = data["5A"] ?: ""
            sendEvent("confirmationRequested", mapOf(
                "pan" to pan,
                "tlv" to s
            ))
        }

        override fun onOnlineProcessRequested(s: String) {
            sendEvent("onlineProcessRequested", mapOf("data" to s))
        }

        override fun onBatchDataReceived(s: String) {
            val data = BaseCardController.decodeTlv(s)
            sendEvent("batchData", mapOf("data" to data.toString()))
        }

        override fun onReversalDataReceived(s: String) {
            sendEvent("reversalData", mapOf("data" to s))
        }

        override fun onTransactionStatusReceived(transactionResult: TransactionFlowController.TransactionResult) {
            sendEvent("transactionStatus", mapOf("result" to transactionResult.toString()))
            stopTransaction()
        }

        override fun onPinEntryRequested(
            pinEntrySource: TransactionFlowController.PinEntrySource,
            s: String
        ) {
            sendEvent("pinEntryRequested", mapOf("source" to pinEntrySource.toString()))
        }

        override fun onEmvCardDataReceived(b: Boolean, s: String) {
            val data = BaseCardController.decodeTlv(s)
            val pan = data["5A"] ?: ""
            sendEvent("emvCardData", mapOf("pan" to pan, "data" to data.toString()))
        }

        override fun onEmvCardNumberReceived(b: Boolean, s: String) {
            sendEvent("emvCardNumber", mapOf("number" to s))
        }

        override fun onSetAmountRequest(s: String) {
            sendEvent("setAmountRequest", mapOf("amount" to s))
        }
    }
}
