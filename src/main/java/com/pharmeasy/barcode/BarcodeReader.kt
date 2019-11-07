package com.pharmeasy.barcode

import android.app.Activity
import android.app.Application
import android.content.BroadcastReceiver
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

class BarcodeReader :Application() {

    companion object {
        val barcode = MutableLiveData<String>()
    }

    lateinit var scanner: Scanner
    lateinit var broadcastReceiver: BroadcastReceiver
    var intentFilter: IntentFilter= IntentFilter()
    lateinit var profileBroadCast:ProfileBroadCast

    override fun onCreate() {
        super.onCreate()


        if(Build.MANUFACTURER.contains("Zebra Technologies") || Build.MANUFACTURER.contains("Motorola Solutions")){
            createProfileForZebraDevice()
            createBarCodeProfileForZebraDevice()
            configureOutputMethodForZebraDevice()
            switchToProfile(VALUE_PROFILE_NAME)
            scanner = ZebraScanner()
            broadcastReceiver=ZebraBroadcast()
            profileBroadCast= ProfileBroadCast()
            intentFilter.addCategory(Intent.CATEGORY_DEFAULT)
            intentFilter.addAction(resources.getString(R.string.activity_intent_filter_action))
       }else{
            scanner = NewlandScanner()
            broadcastReceiver=NewlandBroadCast()
            intentFilter.addAction(resources.getString(R.string.newland_scanner_result))

        }


        registerActivityLifecycleCallbacks(object: ActivityLifecycleCallbacks{

            override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {
            }


            override fun onActivityStarted(activity: Activity?) {
            }

            override fun onActivityResumed(activity: Activity?) {
                registerReceiver(broadcastReceiver,intentFilter)
                registerReceiver(profileBroadCast,
                    IntentFilter(getString(R.string.datawedge_intent_key_result_action))
                )

            }

            override fun onActivityPaused(activity: Activity?) {
            }

            override fun onActivityStopped(activity: Activity?) {
                unregisterReceiver(broadcastReceiver)
                unregisterReceiver(profileBroadCast)

            }

            override fun onActivityDestroyed(activity: Activity?) {
            }



            override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {
            }
        })

    }


     fun enableScanner(){
        this.scanner.enableScanner(this)
     }

    fun disableScanner(){
        this.scanner.disableScanner(this)
    }


    private fun createBarCodeProfileForZebraDevice(){


        // Configuration for how to output the barcode
       /* val outputBundleParams = Bundle()
        outputBundleParams.putString(KEY_INTENT_OUTPUT_ENABLED, VALUE_INTENT_OUTPUT_ENABLED_TRUE) // enable this profile
        outputBundleParams.putString(KEY_INTENT_ACTION,resources.getString(R.string.activity_intent_filter_action)) // set the action to which the broadcastlistener listens to get barcode
        outputBundleParams.putString(KEY_INTENT_DELIVERY, VALUE_INTENT_DELIVERY_BROADCAST) // deliver via broadcast

        val outputBundle = Bundle()
        outputBundle.putString(KEY_PLUGIN_NAME, VALUE_PLUGIN_NAME_INTENT)
        outputBundle.putString(KEY_RESET_CONFIG, VALUE_RESET_CONFIG_TRUE)
        outputBundleParams.putBundle(KEY_PARAM_LIST,outputBundleParams)*/

        // Configuration for barcode reader
        val barcodeBundle = Bundle()
        barcodeBundle.putString(KEY_PLUGIN_NAME, VALUE_PLUGIN_NAME_BARCODE)
        barcodeBundle.putString(KEY_RESET_CONFIG,VALUE_RESET_CONFIG_TRUE)

        val barcodeBundleParams = Bundle()
        barcodeBundle.putBundle(KEY_PARAM_LIST,barcodeBundleParams)

        val appConfig = Bundle()
        appConfig.putString(KEY_PACKAGE_NAME,packageName)
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
        intent.setAction(getString(R.string.datawedge_api_intent_action))
        intent.putExtra(getString(R.string.datawedge_api_intent_extra_config),mainBundle)
        intent.putExtra(KEY_SEND_RESULT,VALUE_SEND_RESULT_TRUE)
        sendBroadcast(intent)

    }

    /**
     * Configures the output barcode to be sent via broadcast
     */

    fun configureOutputMethodForZebraDevice(){



        val outputBundleParams = Bundle()
        // Enable the intent output
        outputBundleParams.putString(KEY_INTENT_OUTPUT_ENABLED, VALUE_INTENT_OUTPUT_ENABLED_TRUE)
        // Set the action string to which the broadcast listener listens to get barcode
        outputBundleParams.putString(KEY_INTENT_ACTION,getString(R.string.activity_intent_filter_action).toString())
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
        intent.setAction(getString(R.string.datawedge_api_intent_action))
        intent.putExtra(getString(R.string.datawedge_api_intent_extra_config),mainBundle)
        intent.putExtra(KEY_SEND_RESULT,VALUE_SEND_RESULT_TRUE)
        sendBroadcast(intent)



    }

    /**
     * Create a profile with a profile name VALUE_PROFILE_NAME
     */
    fun createProfileForZebraDevice(){
       val intent = Intent()
       intent.setAction(getString(R.string.datawedge_api_intent_action))
       intent.putExtra(getString(R.string.datawedge_intent_key_create_profile), VALUE_PROFILE_NAME)
       intent.putExtra(KEY_SEND_RESULT, VALUE_SEND_RESULT_TRUE)
       intent.putExtra(KEY_COMMAND_IDENTIFIER, VALUE_COMMAND_IDENTIFIER)
       this.sendBroadcast(intent)
   }

    /**
     * Switch to the profile with the name @param profileName
     */

    fun switchToProfile(profileName:String){
        val intent = Intent()
        intent.setAction(getString(R.string.datawedge_intent_key_switch_profile))
        intent.putExtra(getString(R.string.datawedge_intent_key_extra_profilename), profileName)
        intent.putExtra(KEY_SEND_RESULT,VALUE_SEND_RESULT_TRUE)
        intent.putExtra(KEY_COMMAND_IDENTIFIER, VALUE_COMMAND_IDENTIFIER)
        this.sendBroadcast(intent)
    }





}