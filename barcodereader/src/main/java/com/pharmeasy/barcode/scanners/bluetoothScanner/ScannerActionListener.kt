package com.pharmeasy.barcode.scanners.bluetoothScanner

interface ScannerActionListener {

    fun onConnected()

    fun onConnecting()

    fun onDisconnected()

    fun onData(barcode: String)
}