package com.apollo.apollo_card_reader

import com.spectratech.controllers.CAPK
import com.spectratech.controllers.ConfigurationController.*

/**
 * Configuraciones EMV L2 para tarjetas de pago.
 *
 * Incluye configuraciones completas para:
 * - Visa (contacto y contactless/Paywave)
 * - Mastercard (contacto y contactless/Paypass)
 * - Unionpay
 * - American Express
 * - Discover
 * - JCB
 *
 * CAPKs: Portados del SDK original de SpectraTech con datos reales.
 */
class EMVL2Settings : CFGnCAPKBuilder() {

    init {
        addVisaConfig()
        addMastercardConfig()
        addUnionpayConfig()
        addAmexConfig()
        addDiscoverConfig()
        addJcbConfig()
        addPaypassConfig()
        addPaywaveConfig()

        capkTable = arrayOf(
            // ================== Visa keys ==================
            kCapk96Visa(),
            kCapk50Visa(),
            kCapk51Visa(),
            kCapk53Visa(),
            kCapk54Visa(),
            kCapk57Visa(),
            kCapk58Visa(),
            kCapk94Visa(),
            kCapk97Visa(),
            kCapk01Visa(),
            kCapk07Visa(),
            kCapk08Visa(),
            kCapk09Visa(),
            kCapk92Visa(),
            kCapk94Visa(),
            kCapk95Visa(),
            kCapk97Visa(),
            kCapk99Visa(),
            // ================== master keys ==================
            kCapkEfMc(), kCapkF1Mc(), kCapkFeMc(), kCapk00Mc(),
            kCapk01Mc(), kCapk02Mc(), kCapk03Mc(), kCapk04Mc(),
            kCapk05Mc(), kCapk06Mc(), kCapkFcMc(), kCapkFdMc(),
            kCapkFbMc(), kCapkFaMc(), kCapkFfMc(),
            // ================== unionpay keys ==================
            kCapk05Unionpay(), kCapk08Unionpay(), kCapk09Unionpay(), kCapk0BUnionpay(),
            // ================== Amex keys ==================
            kCapk60Amex(), kCapk61Amex(),
            // ================== Discover keys ==================
            kCapkD0Discover(), kCapkD1Discover(),
            // ================== JCB keys ==================
            kCapk02Jcb(), kCapk03Jcb()
        )
    }

    // ================== Visa config ==================
    private fun addVisaConfig() {
        resetData()
        setAction(CFGnCAPKBuilder.Action.UPDATE)
        putData(KEY_AID, "A0000000031010")
        setScheme(CFGnCAPKBuilder.Scheme.CONTACT_VISA)

        val bitFields = arrayListOf(
            CFGnCAPKBuilder.BitFieldOption.FORCE_TRM,
            CFGnCAPKBuilder.BitFieldOption.ENABLE_PIN_BYPASS
        )
        setBitFields(bitFields)

        putData(KEY_ERSBTHRESH, "000000000000")
        putData(KEY_ERSTARGET, "00")
        putData(KEY_ERSBMAX, "00")
        putData(KEY_TERMINALCAPABILITIES, "E0F0C8")
        putData(KEY_ADDITTERMCAPABILITIES, "F000F0A001")
        putData(KEY_TERMINALTYPE, "22")
        putData(KEY_APPVERSION, "0096")
        putData(KEY_TERMINALCOUNTRYCODE, "0566")
        putData(KEY_TRANSCURRENCYCODE, "0566")
        putData(KEY_TRANSCURRENCYEXPONENT, "02")
        putData(KEY_ACQUIRERIDENTIFIER, "000000000001")
        putData(KEY_MERCHANTIDENTIFIER, "4D45524348414E5420494420202020")
        putData(KEY_TID, "3330303030303030")
        putData(KEY_DEFAULTDDOL, "9F3704")
        putData(KEY_DEFAULTTDOL, "9F02069F0306")
        putData(KEY_TERMINALFLOORLIMIT, "000000010000")
        putData(KEY_CONTACTTACDEFAULT, "DC4000A800")
        putData(KEY_CONTACTTACDENIAL, "0010000000")
        putData(KEY_CONTACTTACONLINE, "DC4004F800")

        completeConfig()
    }

    // ================== mastercard config ==================
    private fun addMastercardConfig() {
        resetData()
        setAction(CFGnCAPKBuilder.Action.UPDATE)
        putData(KEY_AID, "A000000004")
        setScheme(CFGnCAPKBuilder.Scheme.CONTACT_MASTER)

        val bitFields = arrayListOf(CFGnCAPKBuilder.BitFieldOption.FORCE_TRM)
        setBitFields(bitFields)

        putData(KEY_ERSBTHRESH, "000000000000")
        putData(KEY_ERSTARGET, "00")
        putData(KEY_ERSBMAX, "00")
        putData(KEY_TERMINALCAPABILITIES, "E0F0C8")
        putData(KEY_ADDITTERMCAPABILITIES, "F000F0A001")
        putData(KEY_TERMINALTYPE, "22")
        putData(KEY_APPVERSION, "0002")
        putData(KEY_TERMINALCOUNTRYCODE, "0566")
        putData(KEY_TRANSCURRENCYCODE, "0566")
        putData(KEY_TRANSCURRENCYEXPONENT, "02")
        putData(KEY_ACQUIRERIDENTIFIER, "000000000001")
        putData(KEY_MERCHANTIDENTIFIER, "4D45524348414E5420494420202020")
        putData(KEY_TID, "3330303030303030")
        putData(KEY_DEFAULTDDOL, "9F3704")
        putData(KEY_DEFAULTTDOL, "9F02069F0306")
        putData(KEY_TERMINALFLOORLIMIT, "000000010000")
        putData(KEY_TERMINALRISKMGMDATA, "0123456789ABCDEF")
        putData(KEY_CONTACTTACDEFAULT, "FC50B8A000")
        putData(KEY_CONTACTTACDENIAL, "0000000000")
        putData(KEY_CONTACTTACONLINE, "FC50B8F800")

        completeConfig()
    }

    // ================== unionpay config ==================
    private fun addUnionpayConfig() {
        resetData()
        setAction(CFGnCAPKBuilder.Action.UPDATE)
        putData(KEY_AID, "A000000333")
        setScheme(CFGnCAPKBuilder.Scheme.CONTACT_PBOC)

        val bitFields = arrayListOf(CFGnCAPKBuilder.BitFieldOption.FORCE_TRM)
        setBitFields(bitFields)

        putData(KEY_ERSBTHRESH, "000000000000")
        putData(KEY_ERSTARGET, "00")
        putData(KEY_ERSBMAX, "00")
        putData(KEY_TERMINALCAPABILITIES, "E0F0C8")
        putData(KEY_ADDITTERMCAPABILITIES, "F000F0A001")
        putData(KEY_TERMINALTYPE, "22")
        putData(KEY_APPVERSION, "0002")
        putData(KEY_TERMINALCOUNTRYCODE, "0566")
        putData(KEY_TRANSCURRENCYCODE, "0566")
        putData(KEY_TRANSCURRENCYEXPONENT, "02")
        putData(KEY_ACQUIRERIDENTIFIER, "000000000001")
        putData(KEY_MERCHANTIDENTIFIER, "4D45524348414E5420494420202020")
        putData(KEY_TID, "3330303030303030")
        putData(KEY_DEFAULTDDOL, "9F3704")
        putData(KEY_DEFAULTTDOL, "9F02069F0306")
        putData(KEY_TERMINALFLOORLIMIT, "000000010000")
        putData(KEY_TERMINALRISKMGMDATA, "0123456789ABCDEF")
        putData(KEY_CONTACTTACDEFAULT, "DC4000A800")
        putData(KEY_CONTACTTACDENIAL, "0010000000")
        putData(KEY_CONTACTTACONLINE, "DC4004F800")

        completeConfig()
    }

    // ================== Amex config ==================
    private fun addAmexConfig() {
        resetData()
        setAction(CFGnCAPKBuilder.Action.UPDATE)
        putData(KEY_AID, "A000000025")
        setScheme(CFGnCAPKBuilder.Scheme.CONTACT_AMEX)

        val bitFields = arrayListOf(CFGnCAPKBuilder.BitFieldOption.FORCE_TRM)
        setBitFields(bitFields)

        putData(KEY_ERSBTHRESH, "000000000000")
        putData(KEY_ERSTARGET, "00")
        putData(KEY_ERSBMAX, "00")
        putData(KEY_TERMINALCAPABILITIES, "E0F0C8")
        putData(KEY_ADDITTERMCAPABILITIES, "F000F0A001")
        putData(KEY_TERMINALTYPE, "22")
        putData(KEY_APPVERSION, "0002")
        putData(KEY_TERMINALCOUNTRYCODE, "0566")
        putData(KEY_TRANSCURRENCYCODE, "0566")
        putData(KEY_TRANSCURRENCYEXPONENT, "02")
        putData(KEY_ACQUIRERIDENTIFIER, "000000000001")
        putData(KEY_MERCHANTIDENTIFIER, "4D45524348414E5420494420202020")
        putData(KEY_TID, "3330303030303030")
        putData(KEY_DEFAULTDDOL, "9F3704")
        putData(KEY_DEFAULTTDOL, "9F02069F0306")
        putData(KEY_TERMINALFLOORLIMIT, "000000010000")
        putData(KEY_TERMINALRISKMGMDATA, "0123456789ABCDEF")
        putData(KEY_CONTACTTACDEFAULT, "DC4000A800")
        putData(KEY_CONTACTTACDENIAL, "0010000000")
        putData(KEY_CONTACTTACONLINE, "DC4004F800")

        completeConfig()
    }

    // ================== discover config ==================
    private fun addDiscoverConfig() {
        resetData()
        setAction(CFGnCAPKBuilder.Action.UPDATE)
        putData(KEY_AID, "A000000152")
        setScheme(CFGnCAPKBuilder.Scheme.CONTACT_DPAS)

        val bitFields = arrayListOf(CFGnCAPKBuilder.BitFieldOption.FORCE_TRM)
        setBitFields(bitFields)

        putData(KEY_ERSBTHRESH, "000000000000")
        putData(KEY_ERSTARGET, "00")
        putData(KEY_ERSBMAX, "00")
        putData(KEY_TERMINALCAPABILITIES, "E0F0C8")
        putData(KEY_ADDITTERMCAPABILITIES, "F000F0A001")
        putData(KEY_TERMINALTYPE, "22")
        putData(KEY_APPVERSION, "0002")
        putData(KEY_TERMINALCOUNTRYCODE, "0566")
        putData(KEY_TRANSCURRENCYCODE, "0566")
        putData(KEY_TRANSCURRENCYEXPONENT, "02")
        putData(KEY_ACQUIRERIDENTIFIER, "000000000001")
        putData(KEY_MERCHANTIDENTIFIER, "4D45524348414E5420494420202020")
        putData(KEY_TID, "3330303030303030")
        putData(KEY_DEFAULTDDOL, "9F3704")
        putData(KEY_DEFAULTTDOL, "9F02069F0306")
        putData(KEY_TERMINALFLOORLIMIT, "000000010000")
        putData(KEY_TERMINALRISKMGMDATA, "0123456789ABCDEF")
        putData(KEY_CONTACTTACDEFAULT, "DC4000A800")
        putData(KEY_CONTACTTACDENIAL, "0010000000")
        putData(KEY_CONTACTTACONLINE, "DC4004F800")

        completeConfig()
    }

    // ================== JCB config ==================
    private fun addJcbConfig() {
        resetData()
        setAction(CFGnCAPKBuilder.Action.UPDATE)
        putData(KEY_AID, "A000000065")
        setScheme(CFGnCAPKBuilder.Scheme.CONTACT_JCB)

        val bitFields = arrayListOf(CFGnCAPKBuilder.BitFieldOption.FORCE_TRM)
        setBitFields(bitFields)

        putData(KEY_ERSBTHRESH, "000000000000")
        putData(KEY_ERSTARGET, "00")
        putData(KEY_ERSBMAX, "00")
        putData(KEY_TERMINALCAPABILITIES, "E0F0C8")
        putData(KEY_ADDITTERMCAPABILITIES, "F000F0A001")
        putData(KEY_TERMINALTYPE, "22")
        putData(KEY_APPVERSION, "0002")
        putData(KEY_TERMINALCOUNTRYCODE, "0566")
        putData(KEY_TRANSCURRENCYCODE, "0566")
        putData(KEY_TRANSCURRENCYEXPONENT, "02")
        putData(KEY_ACQUIRERIDENTIFIER, "000000000001")
        putData(KEY_MERCHANTIDENTIFIER, "4D45524348414E5420494420202020")
        putData(KEY_TID, "3330303030303030")
        putData(KEY_DEFAULTDDOL, "9F3704")
        putData(KEY_DEFAULTTDOL, "9F02069F0306")
        putData(KEY_TERMINALFLOORLIMIT, "000000010000")
        putData(KEY_TERMINALRISKMGMDATA, "0123456789ABCDEF")
        putData(KEY_CONTACTTACDEFAULT, "DC4000A800")
        putData(KEY_CONTACTTACDENIAL, "0010000000")
        putData(KEY_CONTACTTACONLINE, "DC4004F800")

        completeConfig()
    }

    // ================== Paypass config ==================
    private fun addPaypassConfig() {
        resetData()
        setAction(CFGnCAPKBuilder.Action.UPDATE)
        putData(KEY_AID, "A0000000041010")
        setScheme(CFGnCAPKBuilder.Scheme.CONTACTLESS_PAYPASS)

        val bitFields = arrayListOf(CFGnCAPKBuilder.BitFieldOption.FORCE_TRM)
        setBitFields(bitFields)

        putData(KEY_ERSBTHRESH, "000000000000")
        putData(KEY_ERSTARGET, "00")
        putData(KEY_ERSBMAX, "00")
        putData(KEY_TERMINALCAPABILITIES, "E0B0C8")
        putData(KEY_ADDITTERMCAPABILITIES, "F000F0A001")
        putData(KEY_TERMINALTYPE, "22")
        putData(KEY_APPVERSION, "0002")
        putData(KEY_TERMINALCOUNTRYCODE, "0344")
        putData(KEY_TRANSCURRENCYCODE, "0344")
        putData(KEY_TRANSCURRENCYEXPONENT, "02")
        putData(KEY_ACQUIRERIDENTIFIER, "000000000001")
        putData(KEY_MERCHANTIDENTIFIER, "4D45524348414E5420494420202020")
        putData(KEY_TID, "3330303030303030")
        putData(KEY_DEFAULTDDOL, "9F3704")
        putData(KEY_DEFAULTTDOL, "9F02069F0306")
        putData(KEY_TERMINALFLOORLIMIT, "000000000000")
        putData(KEY_CONTACTTACDEFAULT, "FC50B8A000")
        putData(KEY_CONTACTTACDENIAL, "0000000000")
        putData(KEY_CONTACTTACONLINE, "FC50B8F800")
        putData(KEY_CONTACTLESSTACDEFAULT, "F45084800C")
        putData(KEY_CONTACTLESSTACDENIAL, "0000000000")
        putData(KEY_CONTACTLESSTACONLINE, "F45084800C")
        putData(KEY_CONTACTLESSTRANSACTIONLIMIT, "000099999999")
        putData(KEY_CONTACTLESSCVMREQUIREDLIMIT, "000000005000")
        putData(KEY_CONTACTLESSFLOORLIMIT, "000000000000")
        putData(KEY_CONTACTLESSTRANSACTIONLIMITODCV, "000099999999")
        putData(KEY_TRANSACTIONTYPE, "00")
        putData(KEY_TERMINALRISKMGMDATA, "2C00000000000000")
        putData(KEY_MC_CVMCAP_CVMREQ, "40")            // OPin        (2nd byte of Term Cap when CVM required)
        putData(KEY_MC_CVMCAP_NOCVMREQ, "08")          // noCVM       (2nd byte of Term Cap when CVM not required)
        putData(KEY_MC_SECURITY_CAP, "08")

        completeConfig()
    }

    // ================== Paywave config ==================
    private fun addPaywaveConfig() {
        resetData()
        setAction(CFGnCAPKBuilder.Action.UPDATE)
        putData(KEY_AID, "A0000000031010")
        setScheme(CFGnCAPKBuilder.Scheme.CONTACTLESS_PAYWAVE)

        val bitFields = arrayListOf(CFGnCAPKBuilder.BitFieldOption.DISABLE_REFERRAL)
        setBitFields(bitFields)

        putData(KEY_ERSBTHRESH, "000000000000")
        putData(KEY_ERSTARGET, "00")
        putData(KEY_ERSBMAX, "00")
        putData(KEY_TERMINALCAPABILITIES, "0000C8")
        putData(KEY_ADDITTERMCAPABILITIES, "6000F02001")
        putData(KEY_TERMINALTYPE, "22")
        putData(KEY_APPVERSION, "0002")
        putData(KEY_TERMINALCOUNTRYCODE, "0840")
        putData(KEY_TRANSCURRENCYCODE, "0840")
        putData(KEY_TRANSCURRENCYEXPONENT, "02")
        putData(KEY_ACQUIRERIDENTIFIER, "000000000001")
        putData(KEY_MERCHANTIDENTIFIER, "4D45524348414E5420494420202020")
        putData(KEY_TID, "3330303030303030")
        putData(KEY_DEFAULTDDOL, "9F3704")
        putData(KEY_DEFAULTTDOL, "9F02069F0306")
        putData(KEY_TERMINALFLOORLIMIT, "000000000000")
        putData(KEY_CONTACTTACDEFAULT, "0000000000")
        putData(KEY_CONTACTTACDENIAL, "0000000000")
        putData(KEY_CONTACTTACONLINE, "0000000000")
        putData(KEY_CONTACTLESSTRANSACTIONLIMIT, "000099999999")
        putData(KEY_CONTACTLESSCVMREQUIREDLIMIT, "000000050000")
        putData(KEY_CONTACTLESSFLOORLIMIT, "000000000000")
        putData(KEY_TTQORPUNATC, "B4004000")           // OPin
        putData(KEY_TRANSACTIONTYPE, "00")
        putData(KEY_PWAVEENCHANCEDDDAIDR, "00")
        putData(KEY_PWAVECVMREQUIREMENT, "00")
        putData(KEY_PWAVEDISPOFFLFUNDIDR, "01")
        putData(KEY_CLTTERMENTRYCAP, "05")
        putData(KEY_CLTVFLAG, "00")
        putData(KEY_CLTPPINFLAG, "40")

        completeConfig()
    }

    // ================== visa keys ==================
    protected fun kCapk01Visa(): CAPK {
        val capk = CAPK()
        capk.index = "01"
        capk.location = "00"
        capk.rid = "A000000003"
        capk.exponent = "00000003"
        capk.size = "80"
        capk.modulus = (
            "C696034213D7D8546984579D1D0F0EA5" +
            "19CFF8DEFFC429354CF3A871A6F7183F" +
            "1228DA5C7470C055387100CB935A712C" +
            "4E2864DF5D64BA93FE7E63E71F25B1E5" +
            "F5298575EBE1C63AA617706917911DC2" +
            "A75AC28B251C7EF40F2365912490B939" +
            "BCA2124A30A28F54402C34AECA331AB6" +
            "7E1E79B285DD5771B5D9FF79EA630B75"
        )
        capk.checksum = "D34A6A776011C7E7CE3AEC5F03AD2F8CFC5503CC"
        capk.expirydate = "1249"
        capk.effectdate = "0100"
        return capk
    }

    protected fun kCapk07Visa(): CAPK {
        val capk = CAPK()
        capk.index = "07"
        capk.location = "00"
        capk.rid = "A000000003"
        capk.exponent = "00000003"
        capk.size = "90"
        capk.modulus = (
            "A89F25A56FA6DA258C8CA8B40427D927" +
            "B4A1EB4D7EA326BBB12F97DED70AE5E4" +
            "480FC9C5E8A972177110A1CC318D06D2" +
            "F8F5C4844AC5FA79A4DC470BB11ED635" +
            "699C17081B90F1B984F12E92C1C52927" +
            "6D8AF8EC7F28492097D8CD5BECEA16FE" +
            "4088F6CFAB4A1B42328A1B996F9278B0" +
            "B7E3311CA5EF856C2F888474B83612A8" +
            "2E4E00D0CD4069A6783140433D50725F"
        )
        capk.checksum = "B4BC56CC4E88324932CBC643D6898F6FE593B172"
        capk.expirydate = "1249"
        capk.effectdate = "0100"
        return capk
    }

    protected fun kCapk08Visa(): CAPK {
        val capk = CAPK()
        capk.index = "08"
        capk.location = "00"
        capk.rid = "A000000003"
        capk.exponent = "00000003"
        capk.size = "B0"
        capk.modulus = (
            "D9FD6ED75D51D0E30664BD157023EAA1" +
            "FFA871E4DA65672B863D255E81E137A5" +
            "1DE4F72BCC9E44ACE12127F87E263D3A" +
            "F9DD9CF35CA4A7B01E907000BA85D249" +
            "54C2FCA3074825DDD4C0C8F186CB020F" +
            "683E02F2DEAD3969133F06F7845166AC" +
            "EB57CA0FC2603445469811D293BFEFBA" +
            "FAB57631B3DD91E796BF850A25012F1A" +
            "E38F05AA5C4D6D03B1DC2E5686127859" +
            "38BBC9B3CD3A910C1DA55A5A9218ACE0" +
            "F7A21287752682F15832A678D6E1ED0B"
        )
        capk.checksum = "20D213126955DE205ADC2FD2822BD22DE21CF9A8"
        capk.expirydate = "1249"
        capk.effectdate = "0100"
        return capk
    }

    protected fun kCapk09Visa(): CAPK {
        val capk = CAPK()
        capk.index = "09"
        capk.location = "00"
        capk.rid = "A000000003"
        capk.exponent = "00000003"
        capk.size = "F8"
        capk.modulus = (
            "9D912248DE0A4E39C1A7DDE3F6D25889" +
            "92C1A4095AFBD1824D1BA74847F2BC49" +
            "26D2EFD904B4B54954CD189A54C5D117" +
            "9654F8F9B0D2AB5F0357EB642FEDA95D" +
            "3912C6576945FAB897E7062CAA44A4AA" +
            "06B8FE6E3DBA18AF6AE3738E30429EE9" +
            "BE03427C9D64F695FA8CAB4BFE376853" +
            "EA34AD1D76BFCAD15908C077FFE6DC55" +
            "21ECEF5D278A96E26F57359FFAEDA194" +
            "34B937F1AD999DC5C41EB11935B44C18" +
            "100E857F431A4A5A6BB65114F174C2D7" +
            "B59FDF237D6BB1DD0916E644D709DED5" +
            "6481477C75D95CDD68254615F7740EC0" +
            "7F330AC5D67BCD75BF23D28A140826C0" +
            "26DBDE971A37CD3EF9B8DF644AC38501" +
            "0501EFC6509D7A41"
        )
        capk.checksum = "1FF80A40173F52D7D27E0F26A146A1C8CCB29046"
        capk.expirydate = "1249"
        capk.effectdate = "0100"
        return capk
    }

    protected fun kCapk50Visa(): CAPK {
        val capk = CAPK()
        capk.index = "50"
        capk.location = "00"
        capk.rid = "A000000003"
        capk.exponent = "00010001"
        capk.size = "80"
        capk.modulus = (
            "D11197590057B84196C2F4D11A8F3C05408F422A35D702F90106EA5B019BB28A" +
            "E607AA9CDEBCD0D81A38D48C7EBB0062D287369EC0C42124246AC30D80CD602A" +
            "B7238D51084DED4698162C59D25EAC1E66255B4DB2352526EF0982C3B8AD3D1C" +
            "CE85B01DB5788E75E09F44BE7361366DEF9D1E1317B05E5D0FF5290F88A0DB47"
        )
        capk.checksum = "B769775668CACB5D22A647D1D993141EDAB7237B"
        capk.expirydate = "1249"
        capk.effectdate = "0100"
        return capk
    }

    protected fun kCapk51Visa(): CAPK {
        val capk = CAPK()
        capk.index = "51"
        capk.location = "00"
        capk.rid = "A000000003"
        capk.exponent = "00000003"
        capk.size = "90"
        capk.modulus = (
            "DB5FA29D1FDA8C1634B04DCCFF148ABEE63C772035C79851D3512107586E02A9" +
            "17F7C7E885E7C4A7D529710A145334CE67DC412CB1597B77AA2543B98D19CF2C" +
            "B80C522BDBEA0F1B113FA2C86216C8C610A2D58F29CF3355CEB1BD3EF410D1ED" +
            "D1F7AE0F16897979DE28C6EF293E0A19282BD1D793F1331523FC71A228800468" +
            "C01A3653D14C6B4851A5C029478E757F"
        )
        capk.checksum = "969299D792D3CC08AD28F2D544CEE3309DADF1B9"
        capk.expirydate = "1249"
        capk.effectdate = "0100"
        return capk
    }

    protected fun kCapk53Visa(): CAPK {
        val capk = CAPK()
        capk.index = "53"
        capk.location = "00"
        capk.rid = "A000000003"
        capk.exponent = "00000003"
        capk.size = "F8"
        capk.modulus = (
            "BCD83721BE52CCCC4B6457321F22A7DC769F54EB8025913BE804D9EABBFA19B3" +
            "D7C5D3CA658D768CAF57067EEC83C7E6E9F81D0586703ED9DDDADD20675D6342" +
            "4980B10EB364E81EB37DB40ED100344C928886FF4CCC37203EE6106D5B59D1AC" +
            "102E2CD2D7AC17F4D96C398E5FD993ECB4FFDF79B17547FF9FA2AA8EEFD6CBDA" +
            "124CBB17A0F8528146387135E226B005A474B9062FF264D2FF8EFA36814AA295" +
            "0065B1B04C0A1AE9B2F69D4A4AA979D6CE95FEE9485ED0A03AEE9BD953E81CFD" +
            "1EF6E814DFD3C2CE37AEFA38C1F9877371E91D6A5EB59FDEDF75D3325FA3CA66" +
            "CDFBA0E57146CC789818FF06BE5FCC50ABD362AE4B80996D"
        )
        capk.checksum = "A84A53964513A5D9363B4BA13AF5D43B83A83CE7"
        capk.expirydate = "1249"
        capk.effectdate = "0100"
        return capk
    }

    protected fun kCapk54Visa(): CAPK {
        val capk = CAPK()
        capk.index = "54"
        capk.location = "00"
        capk.rid = "A000000003"
        capk.exponent = "00010001"
        capk.size = "F8"
        capk.modulus = (
            "C6DDC0B7645F7F16286AB7E4116655F56DD0C944766040DC68664DD973BD3BFD" +
            "4C525BCBB95272B6B3AD9BA8860303AD08D9E8CC344A4070F4CFB9EEAF29C8A3" +
            "460850C264CDA39BBE3A7E7D08A69C31B5C8DD9F94DDBC9265758C0E7399ADCF" +
            "4362CAEE458D414C52B498274881B196DACCA7273F687F2A65FAEB809D4B2AC1" +
            "D3D1EFB4F6490322318BD296D153B307A3283AB4E5BE6EBD910359A8565EB9C4" +
            "360D24BAACA3DBFE393F3D6C830D603C6FC1E83409DFCD80D3A33BA243813BBB" +
            "4CEAF9CBAB6B74B00116F72AB278A88A011D70071E06CAB140646438D986D482" +
            "81624B85B3B2EBB9A6AB3BF2178FCC3011E7CAF24897AE7D"
        )
        capk.checksum = "00112233445566778899AABBCCDDEEFF00112233"
        capk.expirydate = "1249"
        capk.effectdate = "0100"
        return capk
    }

    protected fun kCapk57Visa(): CAPK {
        val capk = CAPK()
        capk.index = "57"
        capk.location = "00"
        capk.rid = "A000000003"
        capk.exponent = "00010001"
        capk.size = "60"
        capk.modulus = (
            "942B7F2BA5EA307312B63DF77C5243618ACC2002BD7ECB74D821FE7BDC78BF28" +
            "F49F74190AD9B23B9713B140FFEC1FB429D93F56BDC7ADE4AC075D75532C1E59" +
            "0B21874C7952F29B8C0F0C1CE3AEEDC8DA25343123E71DCF86C6998E15F756E3"
        )
        capk.checksum = "00112233445566778899AABBCCDDEEFF00112233"
        capk.expirydate = "1249"
        capk.effectdate = "0100"
        return capk
    }

    protected fun kCapk58Visa(): CAPK {
        val capk = CAPK()
        capk.index = "58"
        capk.location = "00"
        capk.rid = "A000000003"
        capk.exponent = "00010001"
        capk.size = "C8"
        capk.modulus = (
            "99552C4A1ECD68A0260157FC4151B5992837445D3FC57365CA5692C87BE358CD" +
            "CDF2C92FB6837522842A48EB11CDFFE2FD91770C7221E4AF6207C2DE4004C7DE" +
            "E1B6276DC62D52A87D2CD01FBF2DC4065DB52824D2A2167A06D19E6A0F781071" +
            "CDB2DD314CB94441D8DC0E936317B77BF06F5177F6C5ABA3A3BC6AA30209C972" +
            "60B7A1AD3A192C9B8CD1D153570AFCC87C3CD681D13E997FE33B3963A0A1C797" +
            "72ACF991033E1B8397AD0341500E48A24770BC4CBE19D2CCF419504FDBF0389B" +
            "C2F2FDCD4D44E61F"
        )
        capk.checksum = "E6D302EBE7DC6F267E4D00F7D488F0AB6235F105"
        capk.expirydate = "1249"
        capk.effectdate = "0100"
        return capk
    }

    protected fun kCapk92Visa(): CAPK {
        val capk = CAPK()
        capk.index = "92"
        capk.location = "00"
        capk.rid = "A000000003"
        capk.exponent = "00000003"
        capk.size = "B0"
        capk.modulus = (
            "996AF56F569187D09293C14810450ED8" +
            "EE3357397B18A2458EFAA92DA3B6DF65" +
            "14EC060195318FD43BE9B8F0CC669E3F" +
            "844057CBDDF8BDA191BB64473BC8DC9A" +
            "730DB8F6B4EDE3924186FFD9B8C77357" +
            "89C23A36BA0B8AF65372EB57EA5D89E7" +
            "D14E9C7B6B557460F10885DA16AC923F" +
            "15AF3758F0F03EBD3C5C2C949CBA306D" +
            "B44E6A2C076C5F67E281D7EF56785DC4" +
            "D75945E491F01918800A9E2DC66F6008" +
            "0566CE0DAF8D17EAD46AD8E30A247C9F"
        )
        capk.checksum = "429C954A3859CEF91295F663C963E582ED6EB253"
        capk.expirydate = "1249"
        capk.effectdate = "0100"
        return capk
    }

    protected fun kCapk94Visa(): CAPK {
        val capk = CAPK()
        capk.index = "94"
        capk.location = "00"
        capk.rid = "A000000003"
        capk.exponent = "00000003"
        capk.size = "F8"
        capk.modulus = (
            "ACD2B12302EE644F3F835ABD1FC7A6F6" +
            "2CCE48FFEC622AA8EF062BEF6FB8BA8B" +
            "C68BBF6AB5870EED579BC3973E121303" +
            "D34841A796D6DCBC41DBF9E52C460979" +
            "5C0CCF7EE86FA1D5CB041071ED2C51D2" +
            "202F63F1156C58A92D38BC60BDF424E1" +
            "776E2BC9648078A03B36FB554375FC53" +
            "D57C73F5160EA59F3AFC5398EC7B6775" +
            "8D65C9BFF7828B6B82D4BE124A416AB7" +
            "301914311EA462C19F771F31B3B57336" +
            "000DFF732D3B83DE07052D730354D297" +
            "BEC72871DCCF0E193F171ABA27EE464C" +
            "6A97690943D59BDABB2A27EB71CEEBDA" +
            "FA1176046478FD62FEC452D5CA393296" +
            "530AA3F41927ADFE434A2DF2AE3054F8" +
            "840657A26E0FC617"
        )
        capk.checksum = "C4A3C43CCF87327D136B804160E47D43B60E6E0F"
        capk.expirydate = "1249"
        capk.effectdate = "0100"
        return capk
    }

    protected fun kCapk95Visa(): CAPK {
        val capk = CAPK()
        capk.index = "95"
        capk.location = "00"
        capk.rid = "A000000003"
        capk.exponent = "00000003"
        capk.size = "90"
        capk.modulus = (
            "BE9E1FA5E9A803852999C4AB432DB286" +
            "00DCD9DAB76DFAAA47355A0FE37B1508" +
            "AC6BF38860D3C6C2E5B12A3CAAF2A700" +
            "5A7241EBAA7771112C74CF9A0634652F" +
            "BCA0E5980C54A64761EA101A114E0F0B" +
            "5572ADD57D010B7C9C887E104CA4EE12" +
            "72DA66D997B9A90B5A6D624AB6C57E73" +
            "C8F919000EB5F684898EF8C3DBEFB330" +
            "C62660BED88EA78E909AFF05F6DA627B"
        )
        capk.checksum = "EE1511CEC71020A9B90443B37B1D5F6E703030F6"
        capk.expirydate = "1249"
        capk.effectdate = "0100"
        return capk
    }

    protected fun kCapk96Visa(): CAPK {
        val capk = CAPK()
        capk.index = "96"
        capk.location = "00"
        capk.rid = "A000000003"
        capk.exponent = "00000003"
        capk.size = "80"
        capk.modulus = (
            "B74586D19A207BE6627C5B0AAFBC44A2ECF5A2942D3A26CE19C4FFAEEE920521" +
            "868922E893E7838225A3947A2614796FB2C0628CE8C11E3825A56D3B1BBAEF78" +
            "3A5C6A81F36F8625395126FA983C5216D3166D48ACDE8A431212FF763A7F79D9" +
            "EDB7FED76B485DE45BEB829A3D4730848A366D3324C3027032FF8D16A1E44D8D"
        )
        capk.checksum = "7616E9AC8BE014AF88CA11A8FB17967B7394030E"
        capk.expirydate = "1249"
        capk.effectdate = "0100"
        return capk
    }

    protected fun kCapk97Visa(): CAPK {
        val capk = CAPK()
        capk.index = "97"
        capk.location = "00"
        capk.rid = "A000000003"
        capk.exponent = "00000003"
        capk.size = "60"
        capk.modulus = (
            "AF0754EAED977043AB6F41D6312AB1E2" +
            "2A6809175BEB28E70D5F99B2DF18CAE7" +
            "3519341BBBD327D0B8BE9D4D0E15F07D" +
            "36EA3E3A05C892F5B19A3E9D3413B0D9" +
            "7E7AD10A5F5DE8E38860C0AD004B1E06" +
            "F4040C295ACB457A788551B6127C0B29"
        )
        capk.checksum = "8001CA76C1203955E2C62841CD6F201087E564BF"
        capk.expirydate = "1249"
        capk.effectdate = "0100"
        return capk
    }

    protected fun kCapk99Visa(): CAPK {
        val capk = CAPK()
        capk.index = "99"
        capk.location = "00"
        capk.rid = "A000000003"
        capk.exponent = "00000003"
        capk.size = "80"
        capk.modulus = (
            "AB79FCC9520896967E776E64444E5DCD" +
            "D6E13611874F3985722520425295EEA4" +
            "BD0C2781DE7F31CD3D041F565F747306" +
            "EED62954B17EDABA3A6C5B85A1DE1BEB" +
            "9A34141AF38FCF8279C9DEA0D5A6710D" +
            "08DB4124F041945587E20359BAB47B75" +
            "75AD94262D4B25F264AF33DEDCF28E09" +
            "615E937DE32EDC03C54445FE7E382777"
        )
        capk.checksum = "4ABFFD6B1C51212D05552E431C5B17007D2F5E6D"
        capk.expirydate = "1249"
        capk.effectdate = "0100"
        return capk
    }

    // ================== master keys ==================
    protected fun kCapk00Mc(): CAPK {
        val capk = CAPK()
        capk.index = "00"
        capk.location = "00"
        capk.rid = "A000000004"
        capk.exponent = "00000003"
        capk.size = "60"
        capk.modulus = (
            "9E15214212F6308ACA78B80BD986AC28" +
            "7516846C8D548A9ED0A42E7D997C902C" +
            "3E122D1B9DC30995F4E25C75DD7EE0A0" +
            "CE293B8CC02B977278EF256D76119492" +
            "4764942FE714FA02E4D57F282BA3B2B6" +
            "2C9E38EF6517823F2CA831BDDF6D363D"
        )
        capk.checksum = "8BB99ADDF7B560110955014505FB6B5F8308CE27"
        capk.expirydate = "1249"
        capk.effectdate = "0100"
        return capk
    }

    protected fun kCapk01Mc(): CAPK {
        val capk = CAPK()
        capk.index = "01"
        capk.location = "00"
        capk.rid = "A000000004"
        capk.exponent = "00000003"
        capk.size = "60"
        capk.modulus = (
            "D2010716C9FB5264D8C91A14F4F32F89" +
            "81EE954F20087ED77CDC5868431728D3" +
            "637C632CCF2718A4F5D92EA8AB166AB9" +
            "92D2DE24E9FBDC7CAB9729401E91C502" +
            "D72B39F6866F5C098B1243B132AFEE65" +
            "F5036E168323116338F8040834B98725"
        )
        capk.checksum = "EA950DD4234FEB7C900C0BE817F64DE66EEEF7C4"
        capk.expirydate = "1249"
        capk.effectdate = "0100"
        return capk
    }

    protected fun kCapk02Mc(): CAPK {
        val capk = CAPK()
        capk.index = "02"
        capk.location = "00"
        capk.rid = "A000000004"
        capk.exponent = "00000003"
        capk.size = "70"
        capk.modulus = (
            "CF4264E1702D34CA897D1F9B66C5D636" +
            "91EACC612C8F147116BB22D0C463495B" +
            "D5BA70FB153848895220B8ADEEC3E7BA" +
            "B31EA22C1DC9972FA027D54265BEBF0A" +
            "E3A23A8A09187F21C856607B98BDA6FC" +
            "908116816C502B3E58A145254EEFEE2A" +
            "3335110224028B67809DCB8058E24895"
        )
        capk.checksum = "AF1CC1FD1C1BC9BCA07E78DA6CBA2163F169CBB7"
        capk.expirydate = "1249"
        capk.effectdate = "0100"
        return capk
    }

    protected fun kCapk03Mc(): CAPK {
        val capk = CAPK()
        capk.index = "03"
        capk.location = "00"
        capk.rid = "A000000004"
        capk.exponent = "00000003"
        capk.size = "80"
        capk.modulus = (
            "C2490747FE17EB0584C88D47B1602704" +
            "150ADC88C5B998BD59CE043EDEBF0FFE" +
            "E3093AC7956AD3B6AD4554C6DE19A178" +
            "D6DA295BE15D5220645E3C8131666FA4" +
            "BE5B84FE131EA44B039307638B9E74A8" +
            "C42564F892A64DF1CB15712B736E3374" +
            "F1BBB6819371602D8970E97B900793C7" +
            "C2A89A4A1649A59BE680574DD0B60145"
        )
        capk.checksum = "5ADDF21D09278661141179CBEFF272EA384B13BB"
        capk.expirydate = "1249"
        capk.effectdate = "0100"
        return capk
    }

    protected fun kCapk04Mc(): CAPK {
        val capk = CAPK()
        capk.index = "04"
        capk.location = "00"
        capk.rid = "A000000004"
        capk.exponent = "00000003"
        capk.size = "90"
        capk.modulus = (
            "A6DA428387A502D7DDFB7A74D3F412BE" +
            "762627197B25435B7A81716A700157DD" +
            "D06F7CC99D6CA28C2470527E2C03616B" +
            "9C59217357C2674F583B3BA5C7DCF283" +
            "8692D023E3562420B4615C439CA97C44" +
            "DC9A249CFCE7B3BFB22F68228C3AF133" +
            "29AA4A613CF8DD853502373D62E49AB2" +
            "56D2BC17120E54AEDCED6D96A4287ACC" +
            "5C04677D4A5A320DB8BEE2F775E5FEC5"
        )
        capk.checksum = "381A035DA58B482EE2AF75F4C3F2CA469BA4AA6C"
        capk.expirydate = "1249"
        capk.effectdate = "0100"
        return capk
    }

    protected fun kCapk05Mc(): CAPK {
        val capk = CAPK()
        capk.index = "05"
        capk.location = "00"
        capk.rid = "A000000004"
        capk.exponent = "00000003"
        capk.size = "B0"
        capk.modulus = (
            "B8048ABC30C90D976336543E3FD7091C" +
            "8FE4800DF820ED55E7E94813ED00555B" +
            "573FECA3D84AF6131A651D66CFF4284F" +
            "B13B635EDD0EE40176D8BF04B7FD1C7B" +
            "ACF9AC7327DFAA8AA72D10DB3B8E70B2" +
            "DDD811CB4196525EA386ACC33C0D9D45" +
            "75916469C4E4F53E8E1C912CC618CB22" +
            "DDE7C3568E90022E6BBA770202E4522A" +
            "2DD623D180E215BD1D1507FE3DC90CA3" +
            "10D27B3EFCCD8F83DE3052CAD1E48938" +
            "C68D095AAC91B5F37E28BB49EC7ED597"
        )
        capk.checksum = "EBFA0D5D06D8CE702DA3EAE890701D45E274C845"
        capk.expirydate = "1249"
        capk.effectdate = "0100"
        return capk
    }

    protected fun kCapk06Mc(): CAPK {
        val capk = CAPK()
        capk.index = "06"
        capk.location = "00"
        capk.rid = "A000000004"
        capk.exponent = "00000003"
        capk.size = "F8"
        capk.modulus = (
            "CB26FC830B43785B2BCE37C81ED33462" +
            "2F9622F4C89AAE641046B2353433883F" +
            "307FB7C974162DA72F7A4EC75D9D6573" +
            "36865B8D3023D3D645667625C9A07A6B" +
            "7A137CF0C64198AE38FC238006FB2603" +
            "F41F4F3BB9DA1347270F2F5D8C606E42" +
            "0958C5F7D50A71DE30142F70DE468889" +
            "B5E3A08695B938A50FC980393A9CBCE4" +
            "4AD2D64F630BB33AD3F5F5FD495D31F3" +
            "7818C1D94071342E07F1BEC2194F6035" +
            "BA5DED3936500EB82DFDA6E8AFB655B1" +
            "EF3D0D7EBF86B66DD9F29F6B1D324FE8" +
            "B26CE38AB2013DD13F611E7A594D675C" +
            "4432350EA244CC34F3873CBA06592987" +
            "A1D7E852ADC22EF5A2EE28132031E48F" +
            "74037E3B34AB747F"
        )
        capk.checksum = "F910A1504D5FFB793D94F3B500765E1ABCAD72D9"
        capk.expirydate = "1249"
        capk.effectdate = "0100"
        return capk
    }

    protected fun kCapkEfMc(): CAPK {
        val capk = CAPK()
        capk.index = "EF"
        capk.location = "00"
        capk.rid = "A000000004"
        capk.exponent = "00000003"
        capk.size = "F8"
        capk.modulus = (
            "A191CB87473F29349B5D60A88B3EAEE0973AA6F1A082F358D849FDDFF9C091F8" +
            "99EDA9792CAF09EF28F5D22404B88A2293EEBBC1949C43BEA4D60CFD879A1539" +
            "544E09E0F09F60F065B2BF2A13ECC705F3D468B9D33AE77AD9D3F19CA40F23DC" +
            "F5EB7C04DC8F69EBA565B1EBCB4686CD274785530FF6F6E9EE43AA43FDB02CE0" +
            "0DAEC15C7B8FD6A9B394BABA419D3F6DC85E16569BE8E76989688EFEA2DF22FF" +
            "7D35C043338DEAA982A02B866DE5328519EBBCD6F03CDD686673847F84DB651A" +
            "B86C28CF1462562C577B853564A290C8556D818531268D25CC98A4CC6A0BDFFF" +
            "DA2DCCA3A94C998559E307FDDF915006D9A987B07DDAEB3B"
        )
        capk.checksum = "21766EBB0EE122AFB65D7845B73DB46BAB65427A"
        capk.expirydate = "1249"
        capk.effectdate = "0100"
        return capk
    }

    protected fun kCapkF1Mc(): CAPK {
        val capk = CAPK()
        capk.index = "F1"
        capk.location = "00"
        capk.rid = "A000000004"
        capk.exponent = "00000003"
        capk.size = "B0"
        capk.modulus = (
            "A0DCF4BDE19C3546B4B6F0414D174DDE294AABBB828C5A83" +
            "4D73AAE27C99B0B053A90278007239B6459FF0BBCD7B4B9C" +
            "6C50AC02CE91368DA1BD21AAEADBC65347337D89B68F5C99" +
            "A09D05BE02DD1F8C5BA20E2F13FB2A27C41D3F85CAD5CF66" +
            "68E75851EC66EDBF98851FD4E42C44C1D59F5984703B27D5" +
            "B9F21B8FA0D93279FBBF69E090642909C9EA27F898959541" +
            "AA6757F5F624104F6E1D3A9532F2A6E51515AEAD1B43B3D7" +
            "835088A2FAFA7BE7"
        )
        capk.checksum = "D8E68DA167AB5A85D8C3D55ECB9B0517A1A5B4BB"
        capk.expirydate = "1249"
        capk.effectdate = "0100"
        return capk
    }

    protected fun kCapkFaMc(): CAPK {
        val capk = CAPK()
        capk.index = "FA"
        capk.location = "00"
        capk.rid = "A000000004"
        capk.exponent = "00000003"
        capk.size = "90"
        capk.modulus = (
            "A90FCD55AA2D5D9963E35ED0F4401776" +
            "99832F49C6BAB15CDAE5794BE93F934D" +
            "4462D5D12762E48C38BA83D8445DEAA7" +
            "4195A301A102B2F114EADA0D180EE5E7" +
            "A5C73E0C4E11F67A43DDAB5D55683B14" +
            "74CC0627F44B8D3088A492FFAADAD4F4" +
            "2422D0E7013536C3C49AD3D0FAE96459" +
            "B0F6B1B6056538A3D6D44640F94467B1" +
            "08867DEC40FAAECD740C00E2B7A8852D"
        )
        capk.checksum = "5BED4068D96EA16D2D77E03D6036FC7A160EA99C"
        capk.expirydate = "1249"
        capk.effectdate = "0100"
        return capk
    }

    protected fun kCapkFbMc(): CAPK {
        val capk = CAPK()
        capk.index = "FB"
        capk.location = "00"
        capk.rid = "A000000004"
        capk.exponent = "00000002"
        capk.size = "80"
        capk.modulus = (
            "A9548DFB398B48123FAF41E6CFA4AE1E" +
            "2352B518AB4BCEFECDB0B3EDEC090287" +
            "D88B12259F361C1CC088E5F066494417" +
            "E8EE8BBF8991E2B32FF16F994697842B" +
            "3D6CB37A2BB5742A440B6356C62AA33D" +
            "B3C455E59EDDF7864701D03A5B83EE9E" +
            "9BD83AB93302AC2DFE63E66120B051CF" +
            "081F56326A71303D952BB336FF12610D"
        )
        capk.checksum = "6C7289632919ABEE6E1163D7E6BF693FD88EBD35"
        capk.expirydate = "1249"
        capk.effectdate = "0100"
        return capk
    }

    protected fun kCapkFcMc(): CAPK {
        val capk = CAPK()
        capk.index = "FC"
        capk.location = "00"
        capk.rid = "A000000004"
        capk.exponent = "00000002"
        capk.size = "70"
        capk.modulus = (
            "B37BFD2A9674AD6221C1A001081C6265" +
            "3DC280B0A9BD052C677C913CE7A0D902" +
            "E77B12F4D4D79037B1E9B923A8BB3FAC" +
            "3C612045BB3914F8DF41E9A1B61BFA5B" +
            "41705A691D09CE6F530FE48B30240D98" +
            "F4E692FFD6AADB87243BA8597AB23758" +
            "6ECF258F4148751BE5DA5A3BE6CC34BD"
        )
        capk.checksum = "7FB377EEBBCF7E3A6D04015D10E1BDCB15E21B80"
        capk.expirydate = "1249"
        capk.effectdate = "0100"
        return capk
    }

    protected fun kCapkFdMc(): CAPK {
        val capk = CAPK()
        capk.index = "FD"
        capk.location = "00"
        capk.rid = "A000000004"
        capk.exponent = "00000002"
        capk.size = "60"
        capk.modulus = (
            "B3572BA49AE4C7B7A0019E5189E142CF" +
            "CDED9498DDB5F0470567AB0BA713B8DA" +
            "226424622955B54B937ABFEFAAD97919" +
            "E377621E22196ABC1419D5ADC1234842" +
            "09EA7CB7029E66A0D54C5B45C8AD615A" +
            "EDB6AE9E0A2F75310EA8961287241245"
        )
        capk.checksum = "23CF0D702E0AEFE518E4FA6B836D3CD45B8AAA71"
        capk.expirydate = "1249"
        capk.effectdate = "0100"
        return capk
    }

    protected fun kCapkFeMc(): CAPK {
        val capk = CAPK()
        capk.index = "FE"
        capk.location = "00"
        capk.rid = "A000000004"
        capk.exponent = "00000003"
        capk.size = "80"
        capk.modulus = (
            "A653EAC1C0F786C8724F737F172997D6" +
            "3D1C3251C44402049B865BAE877D0F39" +
            "8CBFBE8A6035E24AFA086BEFDE9351E5" +
            "4B95708EE672F0968BCD50DCE40F7833" +
            "22B2ABA04EF137EF18ABF03C7DBC5813" +
            "AEAEF3AA7797BA15DF7D5BA1CBAF7FD5" +
            "20B5A482D8D3FEE105077871113E23A4" +
            "9AF3926554A70FE10ED728CF793B62A1"
        )
        capk.checksum = "9A295B05FB390EF7923F57618A9FDA2941FC34E0"
        capk.expirydate = "1249"
        capk.effectdate = "0100"
        return capk
    }

    protected fun kCapkFfMc(): CAPK {
        val capk = CAPK()
        capk.index = "FF"
        capk.location = "00"
        capk.rid = "A000000004"
        capk.exponent = "00000003"
        capk.size = "70"
        capk.modulus = (
            "B855CC64313AF99C453D181642EE7DD2" +
            "1A67D0FF50C61FE213BCDC18AFBCD077" +
            "22EFDD2594EFDC227DA3DA23ADCC90E3" +
            "FA907453ACC954C47323BEDCF8D4862C" +
            "457D25F47B16D7C3502BE081913E5B04" +
            "82D838484065DA5F6659E00A9E5D570A" +
            "DA1EC6AF8C57960075119581FC81468D"
        )
        capk.checksum = "B4E769CECF7AAC4783F305E0B110602A07A6355B"
        capk.expirydate = "1249"
        capk.effectdate = "0100"
        return capk
    }

    // ================== unionpay keys ==================
    protected fun kCapk05Unionpay(): CAPK {
        val capk = CAPK()
        capk.index = "05"
        capk.location = "00"
        capk.rid = "A000000333"
        capk.exponent = "00000005"
        capk.size = "F8"
        capk.modulus = (
            "97CF8BAD30CAE0F9A89285454DDDE967" +
            "AAFBCD4BC0B78F29ECB1005286F15F6D" +
            "7532A9C476607C73FF7424316DFC7418" +
            "94AA52EDBAF909719C7B53448343B45C" +
            "F2F00A8ABFB78CEEBE848933AAED97DB" +
            "E84F0730F34FB1AA1528D3D6EC75B732" +
            "52A30D0C717518BE36458ADD0FBF854C" +
            "65497F3F54084154B60F51561361EE8E" +
            "85F742A54005524CB00FEBC334276E0E" +
            "63DAD86C079A9A3DF5DD32BECADE1AB2" +
            "B71F5F0A0E95A4000D01F1044A578AAD" +
            "92E9FDE92E3C6AA3DCD4913DFA555253" +
            "7E7DE75E241FAED455D76CB8FCAFEED3" +
            "FD6DAB24D7A9C32852F866C751D7710F" +
            "494A0DF11B67FAECDD87A9A4E2CC44F6" +
            "F27E46E3C0CCCD0F"
        )
        capk.checksum = "CC9585E8E637191C10FCECB32B5AE1B9D410B52D"
        capk.expirydate = "1249"
        capk.effectdate = "0100"
        return capk
    }

    protected fun kCapk08Unionpay(): CAPK {
        val capk = CAPK()
        capk.index = "08"
        capk.location = "00"
        capk.rid = "A000000333"
        capk.exponent = "00000003"
        capk.size = "90"
        capk.modulus = (
            "B61645EDFD5498FB246444037A0FA18C" +
            "0F101EBD8EFA54573CE6E6A7FBF63ED2" +
            "1D66340852B0211CF5EEF6A1CD989F66" +
            "AF21A8EB19DBD8DBC3706D135363A0D6" +
            "83D046304F5A836BC1BC632821AFE7A2" +
            "F75DA3C50AC74C545A75456220413716" +
            "9663CFCC0B06E67E2109EBA41BC67FF2" +
            "0CC8AC80D7B6EE1A95465B3B2657533E" +
            "A56D92D539E5064360EA4850FED2D1BF"
        )
        capk.checksum = "EE23B616C95C02652AD18860E48787C079E8E85A"
        capk.expirydate = "1249"
        capk.effectdate = "0100"
        return capk
    }

    protected fun kCapk09Unionpay(): CAPK {
        val capk = CAPK()
        capk.index = "09"
        capk.location = "00"
        capk.rid = "A000000333"
        capk.exponent = "00000003"
        capk.size = "B0"
        capk.modulus = (
            "EB374DFC5A96B71D2863875EDA2EAFB9" +
            "6B1B439D3ECE0B1826A2672EEEFA7990" +
            "286776F8BD989A15141A75C384DFC14F" +
            "EF9243AAB32707659BE9E4797A247C2F" +
            "0B6D99372F384AF62FE23BC54BCDC57A" +
            "9ACD1D5585C303F201EF4E8B806AFB80" +
            "9DB1A3DB1CD112AC884F164A67B99C7D" +
            "6E5A8A6DF1D3CAE6D7ED3D5BE725B2DE" +
            "4ADE23FA679BF4EB15A93D8A6E29C7FF" +
            "A1A70DE2E54F593D908A3BF9EBBD760B" +
            "BFDC8DB8B54497E6C5BE0E4A4DAC29E5"
        )
        capk.checksum = "A075306EAB0045BAF72CDD33B3B678779DE1F527"
        capk.expirydate = "1249"
        capk.effectdate = "0100"
        return capk
    }

    protected fun kCapk0BUnionpay(): CAPK {
        val capk = CAPK()
        capk.index = "0B"
        capk.location = "00"
        capk.rid = "A000000333"
        capk.exponent = "00000003"
        capk.size = "F8"
        capk.modulus = (
            "CF9FDF46B356378E9AF311B0F981B21A" +
            "1F22F250FB11F55C958709E3C7241918" +
            "293483289EAE688A094C02C344E2999F" +
            "315A72841F489E24B1BA0056CFAB3B47" +
            "9D0E826452375DCDBB67E97EC2AA66F4" +
            "601D774FEAEF775ACCC621BFEB65FB00" +
            "53FC5F392AA5E1D4C41A4DE9FFDFDF13" +
            "27C4BB874F1F63A599EE3902FE95E729" +
            "FD78D4234DC7E6CF1ABABAA3F6DB29B7" +
            "F05D1D901D2E76A606A8CBFFFFECBD91" +
            "8FA2D278BDB43B0434F5D45134BE1C27" +
            "81D157D501FF43E5F1C470967CD57CE5" +
            "3B64D82974C8275937C5D8502A1252A8" +
            "A5D6088A259B694F98648D9AF2CB0EFD" +
            "9D943C69F896D49FA39702162ACB5AF2" +
            "9B90BADE005BC157"
        )
        capk.checksum = "BD331F9996A490B33C13441066A09AD3FEB5F66C"
        capk.expirydate = "1249"
        capk.effectdate = "0100"
        return capk
    }

    // ================== Amex keys ==================
    protected fun kCapk60Amex(): CAPK {
        val capk = CAPK()
        capk.index = "60"
        capk.location = "00"
        capk.rid = "A000000025"
        capk.exponent = "00000003"
        capk.size = "60"
        capk.modulus = (
            "D0F543F03F2517133EF2BA4A11044867" +
            "58630DCFE3A883C77B4E4844E39A9BD6" +
            "360D23E6644E1E071F196DDF2E4A68B4" +
            "A3D93D14268D7240F6A14F0D714C1782" +
            "7D279D192E88931AF7300727AE9DA80A" +
            "3F0E366AEBA61778171737989E1EE309"
        )
        capk.checksum = "C08E256F276ED814021B11CAF6EC3701EC7553A1"
        capk.expirydate = "1249"
        capk.effectdate = "0100"
        return capk
    }

    protected fun kCapk61Amex(): CAPK {
        val capk = CAPK()
        capk.index = "61"
        capk.location = "00"
        capk.rid = "A000000025"
        capk.exponent = "00010001"
        capk.size = "F8"
        capk.modulus = (
            "86C7254665E17CE6934DF7D082569F20" +
            "8D1CC1AD8E9FB2FE23E3D7467BE50B4F" +
            "874F906ADF2280EC9D204F6D10C037A2" +
            "3CE5FD8283C9ED47D1C669ABDD7C1CB3" +
            "56C70BCDC44E5C8AE231555F7B786AC9" +
            "C3155BCD51F28EFBC1B33CC872770492" +
            "19B2C890952736C4713487111678911D" +
            "9F42E08074CF524E65D721D727F054E6" +
            "B5E85EC92B3EB59FFEE926DD6C314DF5" +
            "55C94AD487A99B67CB7C7BA5E46A5B81" +
            "3DDB918B8E3E0423F4302A58686D1263" +
            "C0BACA9E82068C493289E3E6936ECA5F" +
            "9F77E06B0D6FBDA718818B835020098C" +
            "671C5DD7E9B8E8E841D2DF32EE94A7F4" +
            "748484CA44108AB241A5263BA1FF00D5" +
            "1360DDDC749D30A1"
        )
        capk.checksum = "CC9585E8E637191C10FCECB32B5AE1B9D410B52D"
        capk.expirydate = "1249"
        capk.effectdate = "0100"
        return capk
    }

    // ================== Discover keys ==================
    protected fun kCapkD0Discover(): CAPK {
        val capk = CAPK()
        capk.index = "D0"
        capk.location = "00"
        capk.rid = "A000000152"
        capk.exponent = "00010001"
        capk.size = "90"
        capk.modulus = (
            "D05C2A09D09C9031366EC092BCAC67D4" +
            "B1B4F88B10005E1FC45C1B483AE7EB86" +
            "FF0E884A19C0595A6C34F06386D776A2" +
            "1D620FC9F9C498ADCA00E66D129BCDD4" +
            "789837B96DCC7F09DA94CCAC5AC7CFC0" +
            "7F4600DF78E493DC1957DEBA3F4838A4" +
            "B8BD4CEFE4E4C6119085E5BB21077341" +
            "C568A21D65D049D666807C39C401CDFE" +
            "E7F7F99B8F9CB34A8841EA62E83E8D63"
        )
        capk.checksum = "CC9585E8E637191C10FCECB32B5AE1B9D410B52D"
        capk.expirydate = "1249"
        capk.effectdate = "0100"
        return capk
    }

    protected fun kCapkD1Discover(): CAPK {
        val capk = CAPK()
        capk.index = "D1"
        capk.location = "00"
        capk.rid = "A000000152"
        capk.exponent = "00010001"
        capk.size = "F8"
        capk.modulus = (
            "A71AF977C1079304D6DFF3F665AB6DB3" +
            "FBDFA1B170287AC6D7BC0AFCB7A202A4" +
            "C815E1FC2E34F75A052564EE2148A39C" +
            "D6B0F39CFAEF95F0294A86C3198E349F" +
            "F82EECE633D50E5860A15082B4B342A9" +
            "0928024057DD51A2401D781B67AE7598" +
            "D5D1FF26A441970A19A3A58011CA1928" +
            "4279A85567D3119264806CAF761122A7" +
            "1FC0492AC8D8D42B036C394FC494E03B" +
            "43600D7E02CB5267755ACE64437CFA7B" +
            "475AD40DDC93B8C9BCAD63801FC492FD" +
            "251640E41FD13F6E231F56F97283447A" +
            "B44CBE11910DB3C75243784AA9BDF575" +
            "39C31B51C9F35BF8BC24957628812554" +
            "78264B792BBDCA6498777AE9120ED935" +
            "BB3E8BEA3EAB13D9"
        )
        capk.checksum = "CC9585E8E637191C10FCECB32B5AE1B9D410B52D"
        capk.expirydate = "1249"
        capk.effectdate = "0100"
        return capk
    }

    // ================== JCB keys ==================
    protected fun kCapk02Jcb(): CAPK {
        val capk = CAPK()
        capk.index = "02"
        capk.location = "00"
        capk.rid = "A000000065"
        capk.exponent = "00010001"
        capk.size = "80"
        capk.modulus = (
            "BB7F51983FD8707FD6227C23DEF5D537" +
            "7A5A737CEF3C5252E578EFE136DF87B5" +
            "0473F9341F1640C8D258034E14C16993" +
            "FCE6C6B8C3CEEB65FC8FBCD8EB77B3B0" +
            "5AC7C4D09E0FA1BA2EFE87D3184DB671" +
            "8AE41A7CAD89B8DCE0FE80CEB523D5D6" +
            "47F9DB58A31D2E71AC677E67FA6E7582" +
            "0736C9893761EE4ACD11F31DBDC349EF"
        )
        capk.checksum = "CC9585E8E637191C10FCECB32B5AE1B9D410B52D"
        capk.expirydate = "1249"
        capk.effectdate = "0100"
        return capk
    }

    protected fun kCapk03Jcb(): CAPK {
        val capk = CAPK()
        capk.index = "03"
        capk.location = "00"
        capk.rid = "A000000065"
        capk.exponent = "00000003"
        capk.size = "F8"
        capk.modulus = (
            "C9E6C1F3C6949A8A42A91F8D0224132B" +
            "2865E6D953A5B5A54CFFB0412439D54A" +
            "EBA79E9B399A6C104684DF3FB727C7F5" +
            "5984DB7A450E6AA917E110A7F2343A00" +
            "24D2785D9EBE09F601D592362FDB2377" +
            "00B567BA14BBE2A6D3D23CF1270B3DD8" +
            "22B5496549BF884948F55A0D308348C4" +
            "B723BAFB6A7F3975AC397CAD3C5D0FC2" +
            "D178716F5E8E79E75BEB1C84FA202F80" +
            "E68069A984E008706B30C21230545620" +
            "1540787925E86A8B28B129A11AF204B3" +
            "87CB6EE43DB53D15A46E13901BEBD5CE" +
            "CF4854251D9E9875B16E82AD1C5938A9" +
            "72842C8F1A42EBB5AE5336B04FF3DA8B" +
            "8DFBE606FCA8B9084EE05BF67950BA89" +
            "897CD089F924DBCD"
        )
        capk.checksum = "CC9585E8E637191C10FCECB32B5AE1B9D410B52D"
        capk.expirydate = "1249"
        capk.effectdate = "0100"
        return capk
    }
}
