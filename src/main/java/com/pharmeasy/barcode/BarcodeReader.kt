package com.pharmeasy.barcode

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import com.pharmeasy.barcode.broadcastreceivers.NewlandBroadCast
import com.pharmeasy.barcode.broadcastreceivers.ProfileBroadCast
import com.pharmeasy.barcode.broadcastreceivers.ZebraBroadcast
import com.pharmeasy.barcode.interfaces.Scanner
import com.pharmeasy.barcode.scanners.NewlandScanner
import com.pharmeasy.barcode.scanners.ZebraScanner

class BarcodeReader private constructor(context: Context){
    val mContext=context
    companion object:SingletonHolder<BarcodeReader,Context>(::BarcodeReader) {
        val barcode = MutableLiveData<String>()
    }



     var scanner: Scanner
     var broadcastReceiver: BroadcastReceiver
    var intentFilter: IntentFilter= IntentFilter()
    lateinit var profileBroadCast:ProfileBroadCast

    init {


        if(Build.MANUFACTURER.contains("Zebra Technologies") || Build.MANUFACTURER.contains("Motorola Solutions")){
            createProfileForZebraDevice()
            createBarCodeProfileForZebraDevice()
            configureOutputMethodForZebraDevice()
            switchToProfile(VALUE_PROFILE_NAME)
            scanner = ZebraScanner()
            broadcastReceiver=ZebraBroadcast()
            profileBroadCast= ProfileBroadCast()
            intentFilter.addCategory(Intent.CATEGORY_DEFAULT)
            intentFilter.addAction(context.getString(R.string.activity_intent_filter_action))
        }else{
            scanner = NewlandScanner()
            broadcastReceiver=NewlandBroadCast()
            intentFilter.addAction(context.getString(R.string.newland_scanner_result))

        }
    }

    fun registerBroadcast(context: Context){
        context.registerReceiver(broadcastReceiver,intentFilter)
        context.registerReceiver(profileBroadCast,
            IntentFilter(context.getString(R.string.datawedge_intent_key_result_action))
        )
    }

    fun unregisterBroadcast(context: Context){
        context.unregisterReceiver(broadcastReceiver)
        context.unregisterReceiver(profileBroadCast)
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

    fun configureOutputMethodForZebraDevice(){



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
    fun createProfileForZebraDevice(){
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

    fun switchToProfile(profileName:String){
        val intent = Intent()
        intent.setAction(mContext.getString(R.string.datawedge_intent_key_switch_profile))
        intent.putExtra(mContext.getString(R.string.datawedge_intent_key_extra_profilename), profileName)
        intent.putExtra(KEY_SEND_RESULT,VALUE_SEND_RESULT_TRUE)
        intent.putExtra(KEY_COMMAND_IDENTIFIER, VALUE_COMMAND_IDENTIFIER)
        mContext.sendBroadcast(intent)
    }





}