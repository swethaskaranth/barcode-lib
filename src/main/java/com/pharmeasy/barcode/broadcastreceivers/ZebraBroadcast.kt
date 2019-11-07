package com.pharmeasy.barcode.broadcastreceivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.pharmeasy.barcode.BarcodeReader
import com.pharmeasy.barcode.R
import java.lang.Exception

class ZebraBroadcast : BroadcastReceiver() {

    val TAG = ZebraBroadcast::class.java.simpleName


    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action
        val bundle = intent?.extras


        Log.d(TAG,">>>>>> onReceive:: ZebraBroadcast <<<<<< Action:${action}, bundle:${bundle}")

        if(action?.equals(context?.getString(R.string.activity_intent_filter_action))!!){
            try {
                displayResults(intent!!,"via broadcast",context)
            }catch (e: Exception){


            }
        }

    }

    private fun displayResults(initiatingIntent:Intent,howDataReceived:String,context: Context?){
        var decodedSource = initiatingIntent.getStringExtra(context?.getString(R.string.datawedge_intent_key_source))
        var decodedData = initiatingIntent.getStringExtra(context?.getString(R.string.datawedge_intent_key_data))
        var decodedLabelType = initiatingIntent.getStringExtra(context?.getString(R.string.datawedge_intent_key_label_type))

        if(null==decodedSource){
            decodedSource = initiatingIntent.getStringExtra(context?.getString(R.string.datawedge_intent_key_source_legacy))
            decodedData = initiatingIntent.getStringExtra(context?.getString(R.string.datawedge_intent_key_data_legacy))
            decodedLabelType = initiatingIntent.getStringExtra(context?.getString(R.string.datawedge_intent_key_label_type_legacy))
        }
        Log.d(TAG,">>>>>>>> Barcode <<<<<<<<<<< :::: ${decodedData}")
        BarcodeReader.barcode.value=decodedData


    }
}