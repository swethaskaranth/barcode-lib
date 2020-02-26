package com.pharmeasy.barcode.scanners

import android.content.Context
import android.device.ScanManager
import com.pharmeasy.barcode.interfaces.Scanner

class UrovoScanner:Scanner{

    var mScanManager:ScanManager

    init {
        mScanManager= ScanManager()
    }

    override fun enableScanner(context: Context) {
        mScanManager.openScanner()
    }

    override fun disableScanner(context: Context) {
        mScanManager.closeScanner()
    }
}