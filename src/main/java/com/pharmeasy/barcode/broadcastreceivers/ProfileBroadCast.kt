package com.pharmeasy.barcode.broadcastreceivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast

class ProfileBroadCast : BroadcastReceiver() {
    private val TAG=ProfileBroadCast::class.java.simpleName

    override fun onReceive(context: Context?, intent: Intent?) {

        val command = intent?.getStringExtra("COMMAND")
        val commandIdentifier =intent?.getStringExtra("COMMAND_IDENTIFIER")
        val result = intent?.getStringExtra("RESULT")

        var bundle = Bundle()
        var resultInfo=""

        if(intent!!.hasExtra("RESULT_INFO")){
            bundle = intent.getBundleExtra("RESULT_INFO")
            val keys = bundle.keySet()
            for(key in keys ){
                resultInfo+= "${key}:${bundle.getString(key)}\n"
            }
            Log.d(TAG,"Command:${command}\n Result:{$result}\n ResultInfo:${resultInfo}\n CID:${commandIdentifier}")
        }

    }
}