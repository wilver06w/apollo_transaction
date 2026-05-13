package com.apollo.apollo_card_reader

import com.spectratech.controllers.CAPK
import com.spectratech.controllers.ConfigurationController
import java.util.Hashtable

/**
 * Clase base para construir configuraciones EMV (AIDs) y CAPKs.
 *
 * Proporciona métodos auxiliares para construir configuraciones de AID
 * y gestionar listas de CAPKs.
 */
abstract class CFGnCAPKBuilder {
    protected var mData: Hashtable<String, String> = Hashtable()
    protected var mCfgTbl: MutableList<Hashtable<String, String>> = mutableListOf()
    protected var capkTable: Array<CAPK>? = null
    private var mNextCfgIndex = 0
    private var mNextCapk = 0

    enum class Action {
        UPDATE
    }

    enum class Scheme {
        CONTACT_EMV,
        CONTACT_VISA,
        CONTACT_MASTER,
        CONTACT_JCB,
        CONTACT_PBOC,
        CONTACT_AMEX,
        CONTACT_DPAS,
        CONTACT_DZIP,
        CONTACT_PURE,
        CONTACTLESS_PAYWAVE,
        CONTACTLESS_PAYPASS,
        CONTACTLESS_JSPEEDY,
        CONTACTLESS_QUICKPASS,
        CONTACTLESS_EXPRESSPAY,
        CONTACTLESS_DDPAS,
        CONTACTLESS_DDZIP,
        CONTACTLESS_GPURE
    }

    enum class BitFieldOption {
        DISABLE_PARTIAL_AID,
        DISABLE_REFERRAL,
        ENABLE_PIN_BYPASS,
        FORCE_TRM,
        MULTIPLE_ACQUIRER
    }

    /**
     * Reinicia los datos de configuración para una nueva AID
     */
    protected fun resetData() {
        mData = Hashtable()
    }

    /**
     * Agrega un par clave-valor a la configuración actual
     */
    protected fun putData(key: String, value: String) {
        mData[key] = value
    }

    /**
     * Establece la acción de la configuración (UPDATE, DELETE, etc.)
     */
    protected fun setAction(action: Action) {
        when (action) {
            Action.UPDATE -> mData[ConfigurationController.KEY_ACTION] = "update"
        }
    }

    /**
     * Establece el esquema de tarjeta (VISA, MC, etc.)
     * Esto configura el campo ETYPE automáticamente
     */
    protected fun setScheme(scheme: Scheme) {
        val etype = when (scheme) {
            Scheme.CONTACT_EMV -> "01"
            Scheme.CONTACT_VISA -> "02"
            Scheme.CONTACT_MASTER -> "03"
            Scheme.CONTACT_JCB -> "04"
            Scheme.CONTACT_PBOC -> "05"
            Scheme.CONTACT_AMEX -> "06"
            Scheme.CONTACT_DPAS -> "07"
            Scheme.CONTACT_DZIP -> "08"
            Scheme.CONTACT_PURE -> "0E"
            Scheme.CONTACTLESS_PAYWAVE -> "82"
            Scheme.CONTACTLESS_PAYPASS -> "83"
            Scheme.CONTACTLESS_JSPEEDY -> "84"
            Scheme.CONTACTLESS_QUICKPASS -> "85"
            Scheme.CONTACTLESS_EXPRESSPAY -> "86"
            Scheme.CONTACTLESS_DDPAS -> "87"
            Scheme.CONTACTLESS_DDZIP -> "88"
            Scheme.CONTACTLESS_GPURE -> "8E"
        }
        mData[ConfigurationController.KEY_ETYPE] = etype
    }

    /**
     * Establece los campos de bitfield de la configuración
     */
    protected fun setBitFields(bitFields: List<BitFieldOption>) {
        var value = 0
        for (option in bitFields) {
            value = value or when (option) {
                BitFieldOption.DISABLE_PARTIAL_AID -> 0x01
                BitFieldOption.DISABLE_REFERRAL -> 0x02
                BitFieldOption.ENABLE_PIN_BYPASS -> 0x04
                BitFieldOption.FORCE_TRM -> 0x08
                BitFieldOption.MULTIPLE_ACQUIRER -> 0x80
            }
        }
        mData[ConfigurationController.KEY_EBITFIELD] = String.format("%02X", value)
    }

    /**
     * Completa la configuración actual y la agrega a la tabla de configuraciones
     */
    protected fun completeConfig() {
        mCfgTbl.add(mData)
    }

    // ================== Métodos para obtener CAPKs ==================

    /**
     * Obtiene el primer CAPK de la tabla
     */
    fun get1stCAPK(): CAPK? {
        capkTable?.let {
            if (it.isNotEmpty()) {
                mNextCapk = 1
                return it[0]
            }
        }
        return null
    }

    /**
     * Obtiene el siguiente CAPK de la tabla
     */
    fun getNextCAPK(): CAPK? {
        capkTable?.let {
            if (it.size > mNextCapk) {
                return it[mNextCapk++]
            }
        }
        return null
    }

    /**
     * Obtiene un CAPK específico por índice
     */
    fun getCAPK(index: Int): CAPK? {
        capkTable?.let {
            if (it.size > index) {
                return it[index]
            }
        }
        return null
    }

    // ================== Métodos para obtener configuraciones AID ==================

    /**
     * Obtiene la primera configuración AID
     */
    fun get1stCfg(): Hashtable<String, String>? {
        if (mCfgTbl.isNotEmpty()) {
            mNextCfgIndex = 1
            return mCfgTbl[0]
        }
        return null
    }

    /**
     * Obtiene la siguiente configuración AID
     */
    fun getNextCfg(): Hashtable<String, String>? {
        if (mCfgTbl.size > mNextCfgIndex) {
            return mCfgTbl[mNextCfgIndex++]
        }
        return null
    }

    /**
     * Obtiene una configuración AID específica por índice
     */
    fun getCfg(index: Int): Hashtable<String, String>? {
        if (mCfgTbl.size > index) {
            return mCfgTbl[index]
        }
        return null
    }

    /**
     * Obtiene el número total de configuraciones AID
     */
    val cfgCount: Int
        get() = mCfgTbl.size
}
