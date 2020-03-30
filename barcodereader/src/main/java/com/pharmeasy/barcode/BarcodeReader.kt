package com.pharmeasy.barcode

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.device.ScanManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.google.zxing.BarcodeFormat
import com.google.zxing.ResultPoint
import com.google.zxing.client.android.BeepManager
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import com.pharmeasy.barcode.broadcastreceivers.NewlandBroadCast
import com.pharmeasy.barcode.broadcastreceivers.ProfileBroadCast
import com.pharmeasy.barcode.broadcastreceivers.UrovoBroadCast
import com.pharmeasy.barcode.broadcastreceivers.ZebraBroadcast
import com.pharmeasy.barcode.interfaces.Scanner
import com.pharmeasy.barcode.scanners.NewlandScanner
import com.pharmeasy.barcode.scanners.UrovoScanner
import com.pharmeasy.barcode.scanners.ZebraScanner

class BarcodeReader private constructor(context: Context){

    private val TAG: String = BarcodeReader::class.java.simpleName
    private val mContext=context

    var scanner: Scanner
    var broadcastReceiver: BroadcastReceiver
    var intentFilter: IntentFilter= IntentFilter()
    var profileBroadCast: ProfileBroadCast?=null
    lateinit var urovoScannerManager: ScanManager

    var barcodeView: DecoratedBarcodeView? = null
    var beepManager: BeepManager? = null

    private var lastText : String = ""

    private val isZebraDevice= Build.MANUFACTURER.contains("Zebra Technologies") || Build.MANUFACTURER.contains("Motorola Solutions")
    private val isUrovo= Build.MANUFACTURER.contains("Urovo")

    var UIView: Boolean? = false

    companion object:SingletonHolder<BarcodeReader,Context>(::BarcodeReader) {
        val barcodeData = MutableLiveData<Event<String>>()
    }

    private val callback = object : BarcodeCallback {
        override fun barcodeResult(result: BarcodeResult) {
            if (result.text == null || result.text == lastText) {
                // Prevent duplicate scans
                return
            }

            lastText = result.text
            barcodeView?.setStatusText(result.text)
            beepManager?.playBeepSoundAndVibrate()
            BarcodeReader.barcodeData.value= Event(result.text)

        }

        override fun possibleResultPoints(resultPoints: List<ResultPoint>) {}
    }

    init {
        Log.d("code", "code here")


        when {
            isZebraDevice -> {
                createProfileForZebraDevice()
                createBarCodeProfileForZebraDevice()
                configureOutputMethodForZebraDevice()
                switchToProfile(VALUE_PROFILE_NAME)
                scanner = ZebraScanner()
                broadcastReceiver=ZebraBroadcast()
                profileBroadCast= ProfileBroadCast()
                intentFilter.addCategory(Intent.CATEGORY_DEFAULT)
                intentFilter.addAction(context.getString(R.string.activity_intent_filter_action))
                UIView = true
            }
            isUrovo -> {
                scanner=UrovoScanner()
                broadcastReceiver=UrovoBroadCast()
                urovoScannerManager = ScanManager()
                initialiseUrovo()
                val action=urovoScannerManager.getParameterString(UROVO_ID_BUF)
                Log.d(TAG,"Action::::${action.get(0)}")
                intentFilter.addAction(action.get(0))
                UIView = true
            }
            else -> {
                Log.d("barcode", "camera")
                scanner = NewlandScanner()
                broadcastReceiver = NewlandBroadCast()
                intentFilter.addAction(context.getString(R.string.newland_scanner_result))

            }
        }

    }

    fun initializeScanner(activity: Activity, intent: Intent){

        val formats = listOf(BarcodeFormat.QR_CODE, BarcodeFormat.CODE_39)
        barcodeView?.barcodeView?.decoderFactory = DefaultDecoderFactory(formats)
        barcodeView?.initializeFromIntent(intent)
        barcodeView?.decodeContinuous(callback)

        beepManager = BeepManager(activity)
    }

    fun registerBroadcast(context: Context){
        context.registerReceiver(broadcastReceiver,intentFilter)
        if(isZebraDevice) {
            context.registerReceiver(
                profileBroadCast,
                IntentFilter(context.getString(R.string.datawedge_intent_key_result_action))
            )
        }
    }

    fun unregisterBroadcast(context: Context){
        context.unregisterReceiver(broadcastReceiver)
        if(isZebraDevice) {
            context.unregisterReceiver(
                profileBroadCast
            )
        }

    }

    fun enableScanner(){
        this.scanner.enableScanner(mContext)
    }

    fun disableScanner(){
        this.scanner.disableScanner(mContext)
    }

    private fun createBarCodeProfileForZebraDevice(){

        // Configuration for barcode reader
        val barcodeBundle = Bundle()
        barcodeBundle.putString(KEY_PLUGIN_NAME, VALUE_PLUGIN_NAME_BARCODE)
        barcodeBundle.putString(KEY_RESET_CONFIG,VALUE_RESET_CONFIG_TRUE)

        val barcodeBundleParams = Bundle()
        barcodeBundle.putBundle(KEY_PARAM_LIST,barcodeBundleParams)

        val appConfig = Bundle()
        appConfig.putString(KEY_PACKAGE_NAME,mContext.packageName)
        appConfig.putStringArray(KEY_ACTIVITY_LIST, arrayOf("*"))

        val mainBundle = Bundle()
        mainBundle.putString(KEY_PROFILE_NAME, VALUE_PROFILE_NAME)
        // will enable this profile.
        mainBundle.putString(KEY_PROFILE_ENABLED, VALUE_PROFILE_ENABLED)
        // this profile will be updated on top of the existing profile.
        mainBundle.putString(KEY_CONFIG_MODE, VALUE_CONFIG_MODE)
        mainBundle.putBundle(KEY_PLUGIN_CONFIG,barcodeBundle)
        mainBundle.putParcelableArray(KEY_APP_LIST, arrayOf(appConfig))

        val intent = Intent()
        intent.setAction(mContext.getString(R.string.datawedge_api_intent_action))
        intent.putExtra(mContext.getString(R.string.datawedge_api_intent_extra_config),mainBundle)
        intent.putExtra(KEY_SEND_RESULT,VALUE_SEND_RESULT_TRUE)
        mContext.sendBroadcast(intent)

    }

    /**
     * Configures the output barcode to be sent via broadcast
     */

    private fun configureOutputMethodForZebraDevice(){

        val outputBundleParams = Bundle()
        // Enable the intent output
        outputBundleParams.putString(KEY_INTENT_OUTPUT_ENABLED, VALUE_INTENT_OUTPUT_ENABLED_TRUE)
        // Set the action string to which the broadcast listener listens to get barcode
        outputBundleParams.putString(KEY_INTENT_ACTION,mContext.getString(R.string.activity_intent_filter_action).toString())
        // Deliver via broadcast the barcode which was read.
        outputBundleParams.putString(KEY_INTENT_DELIVERY, VALUE_INTENT_DELIVERY_BROADCAST)

        val outputBundle = Bundle()
        outputBundle.putString(KEY_PLUGIN_NAME, VALUE_PLUGIN_NAME_INTENT)
        outputBundle.putString(KEY_RESET_CONFIG, VALUE_RESET_CONFIG_TRUE)
        outputBundle.putBundle(KEY_PARAM_LIST,outputBundleParams)


        val mainBundle = Bundle()
        mainBundle.putString(KEY_PROFILE_NAME, VALUE_PROFILE_NAME)
        // will enable this profile.
        mainBundle.putString(KEY_PROFILE_ENABLED, VALUE_PROFILE_ENABLED)
        // this profile will be updated on top of the existing profile.
        mainBundle.putString(KEY_CONFIG_MODE, VALUE_CONFIG_MODE)
        mainBundle.putBundle(KEY_PLUGIN_CONFIG,outputBundle)

        val intent = Intent()
        intent.setAction(mContext.getString(R.string.datawedge_api_intent_action))
        intent.putExtra(mContext.getString(R.string.datawedge_api_intent_extra_config),mainBundle)
        intent.putExtra(KEY_SEND_RESULT,VALUE_SEND_RESULT_TRUE)
        mContext.sendBroadcast(intent)

    }

    /**
     * Create a profile with a profile name VALUE_PROFILE_NAME
     */
    private fun createProfileForZebraDevice(){
       val intent = Intent()
       intent.setAction(mContext.getString(R.string.datawedge_api_intent_action))
       intent.putExtra(mContext.getString(R.string.datawedge_intent_key_create_profile), VALUE_PROFILE_NAME)
       intent.putExtra(KEY_SEND_RESULT, VALUE_SEND_RESULT_TRUE)
       intent.putExtra(KEY_COMMAND_IDENTIFIER, VALUE_COMMAND_IDENTIFIER)
       mContext.sendBroadcast(intent)
   }

    /**
     * Switch to the profile with the name @param profileName
     */

    private fun switchToProfile(profileName:String){
        val intent = Intent()
        intent.setAction(mContext.getString(R.string.datawedge_intent_key_switch_profile))
        intent.putExtra(mContext.getString(R.string.datawedge_intent_key_extra_profilename), profileName)
        intent.putExtra(KEY_SEND_RESULT,VALUE_SEND_RESULT_TRUE)
        intent.putExtra(KEY_COMMAND_IDENTIFIER, VALUE_COMMAND_IDENTIFIER)
        mContext.sendBroadcast(intent)
    }

    private fun initialiseUrovo(){
        val idmode = urovoScannerManager?.getParameterInts(UROVO_IDMODE_BUF)
        UROVO_ACTION_BUF= urovoScannerManager?.getParameterString(UROVO_ID_BUF) as Array<String>
        //ZERO IS SET FOR INTENT MODE OUTPUT
        idmode?.set(0, 0)
        urovoScannerManager?.setParameterInts(UROVO_IDMODE_BUF, idmode)
    }

}