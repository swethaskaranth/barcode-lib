package com.pharmeasy.barcode.scanners

import android.content.Context
import android.device.ScanManager
import com.pharmeasy.barcode.interfaces.Scanner

class UrovoScanner:Scanner{

    private var mScanManager:ScanManager = ScanManager()

    override fun enableScanner(context: Context) {
        mScanManager.unlockTrigger()
    }

    override fun disableScanner(context: Context) {
        mScanManager.lockTrigger()
    }
}