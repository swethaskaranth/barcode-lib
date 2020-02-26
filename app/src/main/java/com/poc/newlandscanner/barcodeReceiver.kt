package com.poc.newlandscanner

interface barcodeReceiver {

    fun registerBroadcast()
    fun unRegisterBroadcast()
    fun receiveBarcode()
    fun enableScanner()
    fun disableScanner()
}