package com.poc.newlandscanner

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class NewlandScannerBroadcast : BroadcastReceiver(){
    override fun onReceive(context: Context?, intent: Intent?) {
        if(intent!=null){
            val barcode = intent.getStringExtra("SCAN_BARCODE1")
            val barcodeType = intent.getIntExtra("SCAN_BARCODE_TYPE",-1)

            if(barcode!=null){

                Toast.makeText(context,"barcodeData:${barcode} barcodeType:${barcodeType}",Toast.LENGTH_SHORT).show()

            } else{
                Toast.makeText(context,"Unable to fetch barcodeData",Toast.LENGTH_SHORT).show()
            }
        }else{
            Toast.makeText(context,"Scan failed",Toast.LENGTH_SHORT).show()
        }
    }
}