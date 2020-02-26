package com.pharmeasy.barcode.scanners

import android.content.Context
import android.content.Intent
import android.util.Log
import com.pharmeasy.barcode.*
import com.pharmeasy.barcode.interfaces.Scanner

class ZebraScanner: Scanner {

    private val TAG = ZebraScanner::class.java.simpleName

    override fun enableScanner(context: Context) {
        Log.d(TAG,">>>>>>>> ENABLING SCANNER <<<<<<<<<<<")
        val intent = Intent()
        intent.action= context.getString(R.string.zebra_scanner_intent_action)
        intent.putExtra(context.getString(R.string.zebra_scanner_extra_param),
            ZEBRA_SCANNER_EXTRA_VALUE_ENABLE_PLUGIN)
        context.sendBroadcast(intent)
    }

    override fun disableScanner(context: Context) {
        Log.d(TAG,">>>>>>>> DISABLING SCANNER <<<<<<<<<<<")
        val intent = Intent()
        intent.action=context.getString(R.string.zebra_scanner_intent_action)
        intent.putExtra(context.getString(R.string.zebra_scanner_extra_param),
            ZEBRA_SCANNER_EXTRA_VALUE_DISABLE_PLUGIN)
        context.sendBroadcast(intent)
    }
}