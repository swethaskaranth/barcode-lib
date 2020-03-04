package com.pharmeasy.barcode.scanners

import android.content.Context
import android.content.Intent
import android.util.Log
import com.pharmeasy.barcode.NEWLAND_SCANNER_ACTION
import com.pharmeasy.barcode.NEWLAND_SCANNER_DATA
import com.pharmeasy.barcode.interfaces.Scanner

class NewlandScanner : Scanner {

    private val TAG = NewlandScanner::class.java.simpleName

    override fun enableScanner(context: Context) {
        Log.d(TAG,">>>>>>>> ENABLING SCANNER <<<<<<<<<<<")
        val intent = Intent(NEWLAND_SCANNER_ACTION)
        intent.putExtra(NEWLAND_SCANNER_DATA,1)
        context.sendBroadcast(intent)
    }

    override fun disableScanner(context: Context) {
        Log.d(TAG,">>>>>>>> DISABLING SCANNER <<<<<<<<<<<")
        val intent = Intent(NEWLAND_SCANNER_ACTION)
        intent.putExtra(NEWLAND_SCANNER_DATA,0)
        context.sendBroadcast(intent)
    }
}