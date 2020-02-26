package com.pharmeasy.barcode.broadcastreceivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.pharmeasy.barcode.BarcodeReader
import com.pharmeasy.barcode.Event
import com.pharmeasy.barcode.UROVO_ACTION_BUF

class UrovoBroadCast: BroadcastReceiver() {

    val TAG=UrovoBroadCast::class.java.simpleName


    override fun onReceive(context: Context?, intent: Intent?) {
            val result = intent?.getStringExtra(UROVO_ACTION_BUF[1])
            Log.d(TAG,">>>>>>>> Barcode <<<<<<<<<<< :::: ${result}")
            BarcodeReader.barcodeData.value=Event(result!!)
    }
}