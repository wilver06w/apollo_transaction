package com.apollo.apollo_card_reader

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.spectratech.controllers.CAPK
import com.spectratech.controllers.ConfigurationController
import com.spectratech.controllers.ControllerError
import com.spectratech.controllers.ControllerMessage
import java.util.Hashtable

class EmvConfigManager(
    private val context: Context,
    private val callback: EmvConfigCallback
) {

    private var configurationController: ConfigurationController? = null
    private var emvSettings: EMVL2Settings? = null
    private var processingContactless = false
    private var currentPhase = ConfigPhase.IDLE
    private var currentCfgIndex = 0

    private enum class ConfigPhase {
        IDLE,
        REMOVING_CONTACT_CAPK,
        REMOVING_CONTACTLESS_CAPK,
        ADDING_CONTACT_CAPK,
        ADDING_CONTACTLESS_CAPK,
        REMOVING_CONTACT_AID,
        REMOVING_CONTACTLESS_AID,
        ADDING_CONTACT_AID,
        ADDING_CONTACTLESS_AID,
        COMPLETED,
        ERROR
    }

    fun startConfiguration() {
        currentPhase = ConfigPhase.IDLE
        emvSettings = EMVL2Settings()

        configurationController = ConfigurationController.getControllerInstance(
            context,
            ConfigDelegate()
        )

        configurationController?.connectController()
    }

    fun stopConfiguration() {
        configurationController?.apply {
            disconnectController()
        }
    }

    /**
     * Callback para eventos de configuración
     */
    interface EmvConfigCallback {
        fun onConfigProgress(phase: String, message: String)
        fun onConfigSuccess()
        fun onConfigError(error: String, message: String)
    }

    /**
     * Delegate para ConfigurationController
     */
    private inner class ConfigDelegate : ConfigurationController.ConfigurationDelegate {

        override fun onError(error: ControllerError.Error, message: String) {
            currentPhase = ConfigPhase.ERROR
            callback.onConfigError(error.toString(), message)
            stopConfiguration()
        }

        override fun onControllerConnected() {
            callback.onConfigProgress("CONNECTED", "Controlador conectado")

            currentPhase = ConfigPhase.REMOVING_CONTACT_CAPK
            val removeData = Hashtable<String, String>().apply {
                put("rid", "0000000000")
                put("index", "00")
            }
            processingContactless = false
            configurationController?.removeCapk(false, removeData)
        }

        override fun onControllerDisconnected() {
            if (currentPhase == ConfigPhase.COMPLETED) {
                configurationController?.releaseControllerInstance()
                configurationController = null
            } else if (currentPhase != ConfigPhase.ERROR) {
                callback.onConfigError("DISCONNECTED", "Controlador desconectado inesperadamente")
                stopConfiguration()
            }
        }

        override fun onDeviceInfoReceived(deviceInfo: Hashtable<String, String>) = Unit

        override fun onMessageReceived(message: ControllerMessage.MessageText) = Unit

        override fun onAllCapkReceived(capkList: List<CAPK>) = Unit

        override fun onCapkInfoReceived(capk: CAPK) = Unit

        override fun onCapkUpdated(isSuccess: Boolean) {
            if (!isSuccess) {
                currentPhase = ConfigPhase.ERROR
                callback.onConfigError("CAPK_UPDATE", "Error actualizando CAPK")
                stopConfiguration()
                return
            }

            when (currentPhase) {
                ConfigPhase.ADDING_CONTACT_CAPK -> {
                    val nextCapk = emvSettings?.getNextCAPK()
                    if (nextCapk != null) {
                        configurationController?.updateCapk(false, nextCapk)
                    } else {
                        callback.onConfigProgress("CAPK_CONTACT", "CAPKs contacto completados")
                        currentPhase = ConfigPhase.ADDING_CONTACTLESS_CAPK
                        processingContactless = true
                        val firstContactlessCapk = emvSettings?.get1stCAPK()
                        if (firstContactlessCapk != null) {
                            configurationController?.updateCapk(true, firstContactlessCapk)
                        } else {
                            startRemovingAids()
                        }
                    }
                }
                ConfigPhase.ADDING_CONTACTLESS_CAPK -> {
                    val nextCapk = emvSettings?.getNextCAPK()
                    if (nextCapk != null) {
                        configurationController?.updateCapk(true, nextCapk)
                    } else {
                        callback.onConfigProgress("CAPK_CONTACTLESS", "CAPKs contactless completados")
                        startRemovingAids()
                    }
                }
                else -> { }
            }
        }

        override fun onCapkRemoved(isSuccess: Boolean) {
            if (!isSuccess) {
                currentPhase = ConfigPhase.ERROR
                callback.onConfigError("CAPK_REMOVE", "Error removiendo CAPK")
                stopConfiguration()
                return
            }

            when (currentPhase) {
                ConfigPhase.REMOVING_CONTACT_CAPK -> {
                    currentPhase = ConfigPhase.REMOVING_CONTACTLESS_CAPK
                    val removeData = Hashtable<String, String>().apply {
                        put("rid", "0000000000")
                        put("index", "00")
                    }
                    processingContactless = true
                    configurationController?.removeCapk(true, removeData)
                }
                ConfigPhase.REMOVING_CONTACTLESS_CAPK -> {
                    callback.onConfigProgress("CAPK_REMOVE", "CAPKs removidos")
                    currentPhase = ConfigPhase.ADDING_CONTACT_CAPK
                    processingContactless = false
                    val firstCapk = emvSettings?.get1stCAPK()
                    if (firstCapk != null) {
                        configurationController?.updateCapk(false, firstCapk)
                    } else {
                        currentPhase = ConfigPhase.ERROR
                        callback.onConfigError("NO_CAPK", "No hay CAPKs configurados")
                        stopConfiguration()
                    }
                }
                else -> { }
            }
        }

        override fun onAllAidReceived(data: Hashtable<String, String>) = Unit

        override fun onAidInfoReceived(data: Hashtable<String, Any>) = Unit

        override fun onAidUpdated(data: Hashtable<String, ConfigurationController.ConfigurationStatus>) {
            if (data.containsKey("action") && !data.containsKey(ConfigurationController.KEY_AID)) {
                val actionStatus = data["action"]
                if (actionStatus == ConfigurationController.ConfigurationStatus.SUCCESS) {
                    when (currentPhase) {
                        ConfigPhase.REMOVING_CONTACT_AID -> {
                            callback.onConfigProgress("AID_REMOVE_CONTACT", "AIDs contacto removidos")
                            currentPhase = ConfigPhase.REMOVING_CONTACTLESS_AID
                            processingContactless = true
                            configurationController?.removeAllAid(true)
                        }
                        ConfigPhase.REMOVING_CONTACTLESS_AID -> {
                            callback.onConfigProgress("AID_REMOVE_CONTACTLESS", "AIDs contactless removidos")
                            currentPhase = ConfigPhase.ADDING_CONTACT_AID
                            currentCfgIndex = 0
                            val nextCfg = emvSettings?.getCfg(currentCfgIndex++)
                            if (nextCfg != null) {
                                val etype = nextCfg[ConfigurationController.KEY_ETYPE] ?: "01"
                                configurationController?.updateAid(etype.toInt() > 0x80, nextCfg)
                            } else {
                                currentPhase = ConfigPhase.ERROR
                                callback.onConfigError("NO_AID", "No hay AIDs configurados")
                                stopConfiguration()
                            }
                        }
                        else -> { }
                    }
                    return
                }
                currentPhase = ConfigPhase.ERROR
                callback.onConfigError("ACTION_ERROR", "Error en acción de configuración")
                stopConfiguration()
                return
            }

            val aid = data[ConfigurationController.KEY_AID]
            if (aid != null) {
                val status = data[ConfigurationController.KEY_AID]
                if (status != ConfigurationController.ConfigurationStatus.SUCCESS) {
                    currentPhase = ConfigPhase.ERROR
                    callback.onConfigError("AID_UPDATE", "Error actualizando AID: $aid")
                    stopConfiguration()
                    return
                }

                when (currentPhase) {
                    ConfigPhase.ADDING_CONTACT_AID -> {
                        var nextCfg: Hashtable<String, String>? = null
                        while (currentCfgIndex < (emvSettings?.cfgCount ?: 0)) {
                            val cfg = emvSettings?.getCfg(currentCfgIndex++)
                            val etype = cfg?.get(ConfigurationController.KEY_ETYPE)?.toInt(16) ?: 0
                            if (etype <= 0x80) {
                                nextCfg = cfg
                                break
                            }
                        }

                        if (nextCfg != null) {
                            val etype = nextCfg[ConfigurationController.KEY_ETYPE] ?: "01"
                            configurationController?.updateAid(etype.toInt(16) > 0x80, nextCfg)
                        } else {
                            callback.onConfigProgress("AID_CONTACT", "AIDs contacto completados")
                            currentPhase = ConfigPhase.ADDING_CONTACTLESS_AID
                            processingContactless = true

                            currentCfgIndex = 0
                            var firstContactless: Hashtable<String, String>? = null
                            while (currentCfgIndex < (emvSettings?.cfgCount ?: 0)) {
                                val cfg = emvSettings?.getCfg(currentCfgIndex++)
                                val etype = cfg?.get(ConfigurationController.KEY_ETYPE)?.toInt(16) ?: 0
                                if (etype > 0x80) {
                                    firstContactless = cfg
                                    break
                                }
                            }

                            if (firstContactless != null) {
                                val etype = firstContactless[ConfigurationController.KEY_ETYPE] ?: "01"
                                configurationController?.updateAid(true, firstContactless)
                            } else {
                                callback.onConfigProgress("AID_CONTACTLESS", "AIDs contactless completados")
                                currentPhase = ConfigPhase.COMPLETED
                                callback.onConfigSuccess()
                                stopConfiguration()
                            }
                        }
                    }
                    ConfigPhase.ADDING_CONTACTLESS_AID -> {
                        var nextCfg: Hashtable<String, String>? = null
                        while (currentCfgIndex < (emvSettings?.cfgCount ?: 0)) {
                            val cfg = emvSettings?.getCfg(currentCfgIndex++)
                            val etype = cfg?.get(ConfigurationController.KEY_ETYPE)?.toInt(16) ?: 0
                            if (etype > 0x80) {
                                nextCfg = cfg
                                break
                            }
                        }

                        if (nextCfg != null) {
                            configurationController?.updateAid(true, nextCfg)
                        } else {
                            callback.onConfigProgress("AID_CONTACTLESS", "AIDs contactless completados")
                            currentPhase = ConfigPhase.COMPLETED
                            callback.onConfigSuccess()
                            stopConfiguration()
                        }
                    }
                    else -> { }
                }
            } else {
                currentPhase = ConfigPhase.COMPLETED
                callback.onConfigSuccess()
                stopConfiguration()
            }
        }

        private fun startRemovingAids() {
            currentPhase = ConfigPhase.REMOVING_CONTACT_AID
            configurationController?.removeAllAid(false)
        }
    }
}
