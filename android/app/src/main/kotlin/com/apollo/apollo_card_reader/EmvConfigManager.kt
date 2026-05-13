package com.apollo.apollo_card_reader

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.spectratech.controllers.CAPK
import com.spectratech.controllers.ConfigurationController
import com.spectratech.controllers.ControllerError
import com.spectratech.controllers.ControllerMessage
import java.util.Hashtable

/**
 * Gestiona la configuración EMV del lector de tarjetas.
 *
 * Flujo de configuración:
 * 1. Remover todos los CAPK existentes (contacto y contactless)
 * 2. Agregar CAPKs necesarios
 * 3. Remover todas las configuraciones AID
 * 4. Agregar configuraciones AID
 *
 * Uso:
 * - Primer uso de la app
 * - Después de cada asentamiento (batch settlement)
 */
class EmvConfigManager(
    private val context: Context,
    private val callback: EmvConfigCallback
) {
    companion object {
        private const val TAG = "EmvConfigManager"
    }

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

    /**
     * Inicia el proceso de configuración EMV
     */
    fun startConfiguration() {
        Log.d(TAG, "=== INICIANDO CONFIGURACIÓN EMV ===")
        currentPhase = ConfigPhase.IDLE
        emvSettings = EMVL2Settings()

        configurationController = ConfigurationController.getControllerInstance(
            context,
            ConfigDelegate()
        )

        configurationController?.connectController()
    }

    /**
     * Detiene y libera el controlador de configuración
     */
    fun stopConfiguration() {
        Log.d(TAG, "=== DETENIENDO CONFIGURATIONCONTROLLER ===")
        configurationController?.apply {
            Log.d(TAG, "=== Desconectando ConfigurationController ===")
            disconnectController()
            Log.d(TAG, "=== ConfigurationController desconectado ===")
            // No liberar la instancia inmediatamente - dejar que onControllerDisconnected lo haga
        }
        Log.d(TAG, "=== CONFIGURATIONCONTROLLER DETENIDO ===")
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
            Log.e(TAG, "=== ERROR CONFIGURACIÓN: $error - $message ===")
            currentPhase = ConfigPhase.ERROR
            callback.onConfigError(error.toString(), message)
            stopConfiguration()
        }

        override fun onControllerConnected() {
            Log.d(TAG, "=== CONTROLADOR CONECTADO - Iniciando configuración ===")
            callback.onConfigProgress("CONNECTED", "Controlador conectado")

            // Fase 1: Remover CAPKs de contacto
            currentPhase = ConfigPhase.REMOVING_CONTACT_CAPK
            val removeData = Hashtable<String, String>().apply {
                put("rid", "0000000000")
                put("index", "00")
            }
            processingContactless = false
            configurationController?.removeCapk(false, removeData)
        }

        override fun onControllerDisconnected() {
            Log.d(TAG, "=== CONTROLADOR DESCONECTADO ===")
            // Solo liberar la instancia si no se ha completado exitosamente
            // (si se completó, stopConfiguration() ya fue llamado)
            if (currentPhase == ConfigPhase.COMPLETED) {
                Log.d(TAG, "=== CONFIGURACIÓN COMPLETADA EXITOSAMENTE ===")
                // Solo liberar la instancia, la desconexión ya ocurrió
                configurationController?.releaseControllerInstance()
                configurationController = null
                Log.d(TAG, "=== CONFIGURATIONCONTROLLER LIBERADO ===")
            } else if (currentPhase != ConfigPhase.ERROR) {
                callback.onConfigError("DISCONNECTED", "Controlador desconectado inesperadamente")
                stopConfiguration()
            }
        }

        override fun onDeviceInfoReceived(deviceInfo: Hashtable<String, String>) {
            Log.d(TAG, "=== DEVICE INFO ===")
        }

        override fun onMessageReceived(message: ControllerMessage.MessageText) {
            Log.d(TAG, "=== MENSAJE: $message ===")
        }

        override fun onAllCapkReceived(capkList: List<CAPK>) {
            Log.d(TAG, "=== ALL CAPK RECEIVED: ${capkList.size} CAPKs ===")
        }

        override fun onCapkInfoReceived(capk: CAPK) {
            Log.d(TAG, "=== CAPK INFO RECEIVED ===")
        }

        override fun onCapkUpdated(isSuccess: Boolean) {
            if (!isSuccess) {
                Log.e(TAG, "=== ERROR ACTUALIZANDO CAPK === currentPhase: $currentPhase, processingContactless: $processingContactless")
                currentPhase = ConfigPhase.ERROR
                callback.onConfigError("CAPK_UPDATE", "Error actualizando CAPK")
                stopConfiguration()
                return
            }

            Log.d(TAG, "=== CAPK ACTUALIZADO EXITOSAMENTE === currentPhase: $currentPhase, processingContactless: $processingContactless")

            when (currentPhase) {
                ConfigPhase.ADDING_CONTACT_CAPK -> {
                    // Obtener siguiente CAPK de contacto
                    val nextCapk = emvSettings?.getNextCAPK()
                    Log.d(TAG, "=== getNextCAPK() returned: ${nextCapk?.index} ===")
                    if (nextCapk != null) {
                        configurationController?.updateCapk(false, nextCapk)
                    } else {
                        // Terminaron los CAPKs de contacto, pasar a contactless
                        callback.onConfigProgress("CAPK_CONTACT", "CAPKs contacto completados")
                        currentPhase = ConfigPhase.ADDING_CONTACTLESS_CAPK
                        processingContactless = true
                        val firstContactlessCapk = emvSettings?.get1stCAPK()
                        Log.d(TAG, "=== get1stCAPK() for contactless returned: ${firstContactlessCapk?.index} ===")
                        if (firstContactlessCapk != null) {
                            configurationController?.updateCapk(true, firstContactlessCapk)
                        } else {
                            // No hay CAPKs contactless, pasar a AIDs
                            startRemovingAids()
                        }
                    }
                }
                ConfigPhase.ADDING_CONTACTLESS_CAPK -> {
                    // Obtener siguiente CAPK contactless
                    val nextCapk = emvSettings?.getNextCAPK()
                    Log.d(TAG, "=== getNextCAPK() (contactless) returned: ${nextCapk?.index} ===")
                    if (nextCapk != null) {
                        configurationController?.updateCapk(true, nextCapk)
                    } else {
                        // Terminaron todos los CAPKs, pasar a AIDs
                        callback.onConfigProgress("CAPK_CONTACTLESS", "CAPKs contactless completados")
                        startRemovingAids()
                    }
                }
                else -> {
                    Log.e(TAG, "=== FASE INVÁLIDA EN onCapkUpdated ===")
                }
            }
        }

        override fun onCapkRemoved(isSuccess: Boolean) {
            if (!isSuccess) {
                Log.e(TAG, "=== ERROR REMOVIENDO CAPK ===")
                currentPhase = ConfigPhase.ERROR
                callback.onConfigError("CAPK_REMOVE", "Error removiendo CAPK")
                stopConfiguration()
                return
            }

            Log.d(TAG, "=== CAPK REMOVIDO EXITOSAMENTE ===")

            when (currentPhase) {
                ConfigPhase.REMOVING_CONTACT_CAPK -> {
                    // Remover CAPKs contactless
                    currentPhase = ConfigPhase.REMOVING_CONTACTLESS_CAPK
                    val removeData = Hashtable<String, String>().apply {
                        put("rid", "0000000000")
                        put("index", "00")
                    }
                    processingContactless = true
                    configurationController?.removeCapk(true, removeData)
                }
                ConfigPhase.REMOVING_CONTACTLESS_CAPK -> {
                    // Terminó remoción, empezar a agregar CAPKs
                    callback.onConfigProgress("CAPK_REMOVE", "CAPKs removidos")
                    currentPhase = ConfigPhase.ADDING_CONTACT_CAPK
                    processingContactless = false
                    val firstCapk = emvSettings?.get1stCAPK()
                    if (firstCapk != null) {
                        configurationController?.updateCapk(false, firstCapk)
                    } else {
                        Log.e(TAG, "=== NO HAY CAPKs PARA AGREGAR ===")
                        currentPhase = ConfigPhase.ERROR
                        callback.onConfigError("NO_CAPK", "No hay CAPKs configurados")
                        stopConfiguration()
                    }
                }
                else -> {
                    Log.e(TAG, "=== FASE INVÁLIDA EN onCapkRemoved ===")
                }
            }
        }

        override fun onAllAidReceived(data: Hashtable<String, String>) {
            Log.d(TAG, "=== ALL AID RECEIVED ===")
        }

        override fun onAidInfoReceived(data: Hashtable<String, Any>) {
            Log.d(TAG, "=== AID INFO RECEIVED ===")
        }

        override fun onAidUpdated(data: Hashtable<String, ConfigurationController.ConfigurationStatus>) {
            // Verificar si hay una acción en lugar de un AID específico
            if (data.containsKey("action") && !data.containsKey(ConfigurationController.KEY_AID)) {
                val actionStatus = data["action"]
                if (actionStatus == ConfigurationController.ConfigurationStatus.SUCCESS) {
                    Log.d(TAG, "=== ACCIÓN COMPLETADA === currentPhase: $currentPhase")
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
                                Log.e(TAG, "=== NO HAY AIDs PARA AGREGAR ===")
                                currentPhase = ConfigPhase.ERROR
                                callback.onConfigError("NO_AID", "No hay AIDs configurados")
                                stopConfiguration()
                            }
                        }
                        else -> {
                            Log.e(TAG, "=== FASE INVÁLIDA EN onAidUpdated (action) ===")
                        }
                    }
                    return
                }
                Log.e(TAG, "=== ERROR EN ACCIÓN ===")
                currentPhase = ConfigPhase.ERROR
                callback.onConfigError("ACTION_ERROR", "Error en acción de configuración")
                stopConfiguration()
                return
            }

            // Es un AID específico
            val aid = data[ConfigurationController.KEY_AID]
            if (aid != null) {
                // El SDK usa KEY_AID como clave del Hashtable
                val status = data[ConfigurationController.KEY_AID]
                if (status != ConfigurationController.ConfigurationStatus.SUCCESS) {
                    Log.e(TAG, "=== ERROR ACTUALIZANDO AID: $aid ===")
                    currentPhase = ConfigPhase.ERROR
                    callback.onConfigError("AID_UPDATE", "Error actualizando AID: $aid")
                    stopConfiguration()
                    return
                }

                Log.d(TAG, "=== AID ACTUALIZADO EXITOSAMENTE: $aid === currentPhase: $currentPhase")

                when (currentPhase) {
                    ConfigPhase.ADDING_CONTACT_AID -> {
                        // Buscar siguiente AID de contacto (etype <= 0x80)
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
                            // Terminaron AIDs de contacto, pasar a contactless
                            callback.onConfigProgress("AID_CONTACT", "AIDs contacto completados")
                            currentPhase = ConfigPhase.ADDING_CONTACTLESS_AID
                            processingContactless = true

                            // Buscar primer AID contactless (etype > 0x80)
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
                                // No hay AIDs contactless, terminó
                                callback.onConfigProgress("AID_CONTACTLESS", "AIDs contactless completados")
                                currentPhase = ConfigPhase.COMPLETED
                                callback.onConfigSuccess()
                                stopConfiguration()
                            }
                        }
                    }
                    ConfigPhase.ADDING_CONTACTLESS_AID -> {
                        // Buscar siguiente AID contactless (etype > 0x80)
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
                            // ¡Configuración completada!
                            callback.onConfigProgress("AID_CONTACTLESS", "AIDs contactless completados")
                            currentPhase = ConfigPhase.COMPLETED
                            callback.onConfigSuccess()
                            stopConfiguration()
                        }
                    }
                    else -> {
                        Log.e(TAG, "=== FASE INVÁLIDA EN onAidUpdated (AID) === currentPhase: $currentPhase ===")
                    }
                }
            } else {
                // No hay AID ni acción - esto no debería pasar, pero terminamos por seguridad
                Log.w(TAG, "=== onAidUpdated sin AID ni action - terminando ===")
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
