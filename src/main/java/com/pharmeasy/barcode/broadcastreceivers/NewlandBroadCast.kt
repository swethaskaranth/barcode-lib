package com.pharmeasy.barcode.broadcastreceivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.pharmeasy.barcode.BarcodeReader

class NewlandBroadCast : BroadcastReceiver() {

    val TAG = NewlandBroadCast::class.java.simpleName

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG,"OnReceive:intent:${intent?.getStringExtra("SCAN_BARCODE1")}")
        if(intent!=null){
            val barcode = intent.getStringExtra("SCAN_BARCODE1")
            if(barcode!=null){
                Log.d(TAG,"Newland >>>>> Barcode >>>>>>${barcode}")
                BarcodeReader.barcode.value=barcode
            } else{
                Log.d(TAG,"Newland >>>>> Unable to fetch barcode")
            }
        }else{
            Log.d(TAG,"Newland >>>>> Scan failed")
        }
    }
}