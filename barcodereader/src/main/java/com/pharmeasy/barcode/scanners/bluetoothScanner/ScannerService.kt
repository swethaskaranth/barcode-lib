package com.pharmeasy.barcode.scanners.bluetoothScanner

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import java.io.Closeable
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*

object ScannerService {

    private val TAG = "SS"
    //private val SECURE_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66")
    private val INSECURE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    private val adapter = BluetoothAdapter.getDefaultAdapter()
    private var currentDeviceAddress: String? = null

    private var connector: Connector? = null
    private var reader: Reader? = null

    private var currentState: ScannerState = ScannerState.TERMINATED

    private var receivers = hashSetOf<ScannerActionListener>()
    private val dataPublisher = PublishSubject.create<String>()
    private val statePublisher = PublishSubject.create<ScannerState>()

    var currentDevice: Device? = null
        private set

    init {
        dataPublisher.observeOn(AndroidSchedulers.mainThread())
                .subscribe { barcode ->
                    receivers.forEach { it.onData(barcode) }
                }

        statePublisher.observeOn(AndroidSchedulers.mainThread())
                .subscribe { state ->
                    when (state) {
                        ScannerState.CONNECTED -> receivers.forEach { it.onConnected() }
                        ScannerState.CONNECTING -> receivers.forEach { it.onConnecting() }
                        else -> receivers.forEach { it.onDisconnected() }
                    }
                }
    }

    @Synchronized
    private fun setState(state: ScannerState) {
        currentState = state
    }

    fun getPairedDevices(): List<Device> {

        val devices = adapter.bondedDevices

        return devices.map { Device(it.name, it.address, it.address == currentDevice?.address) }
    }

    fun register(actionListener: ScannerActionListener) {
        receivers.add(actionListener)
        statePublisher.onNext(currentState)
    }

    fun deregister(actionListener: ScannerActionListener) {
        receivers.remove(actionListener)
    }

    @Synchronized
    fun connect(address: String?) {

        if (address == null)
            return

        if (currentState == ScannerState.CONNECTED && currentDeviceAddress == address)
            return

        reader?.cancel()
        reader = null

        connector?.cancel()

        currentDeviceAddress = address
        setState(ScannerState.CONNECTING)
        statePublisher.onNext(currentState)

        val device = adapter.getRemoteDevice(address)
        connector = Connector(device)
        connector!!.start()
    }

    @Synchronized
    fun disconnect() {

        setState(ScannerState.TERMINATED)

        currentDeviceAddress = null
        currentDevice = null

        connector?.cancel()
        connector = null

        reader?.cancel()
        reader = null
    }

    @Synchronized
    private fun onConnected(socket: BluetoothSocket) {

        val device = socket.remoteDevice
        currentDevice = Device(device.name, device.address, true)

        connector = null

        reader?.cancel()
        reader = Reader(socket)
        reader!!.start()

        setState(ScannerState.CONNECTED)
        statePublisher.onNext(ScannerState.CONNECTED)
    }

    private fun onConnectionFailed() {

        currentDeviceAddress = null
        currentDevice = null

        reader?.cancel()
        reader = null

        connector?.cancel()
        connector = null

        setState(ScannerState.DISCONNECTED)
        statePublisher.onNext(ScannerState.DISCONNECTED)
    }

    private fun reconnect() {

        if(currentState == ScannerState.TERMINATED)
            return

        setState(ScannerState.CONNECTING)
        statePublisher.onNext(ScannerState.CONNECTING)

        connect(currentDeviceAddress)
    }

    private fun closeSocket(socket: Closeable?) {
        try {
            socket?.close()
        } catch (ex: IOException) {
        }
    }

    private class Connector(device: BluetoothDevice) : Thread() {

        private var socket: BluetoothSocket? = null

        init {
            try {
                socket = device.createInsecureRfcommSocketToServiceRecord(INSECURE_UUID)
            } catch (e: IOException) {
                Log.e(TAG, "Error while connecting to device", e)
            }
        }

        override fun run() {

            adapter.cancelDiscovery()

            if (socket == null)
                return

            try {
                socket?.connect()
            } catch (e: IOException) {

                closeSocket(socket)
                onConnectionFailed()
                return
            }

            onConnected(socket!!)
        }

        fun cancel() {
            closeSocket(socket)
        }
    }

    private class Reader(private val socket: BluetoothSocket) : Thread() {

        private val marker = '*'.toInt()
        private var input: InputStream? = null
        private var running = true
        var out: OutputStream? = null

        init {
            try {
                input = socket.inputStream
                out = socket.outputStream;
            } catch (e: IOException) {
                Log.e(TAG, "Unable to get input-stream")
            }
        }

        override fun run() {

            /* if (input == null)
                 return

             var buffer = mutableListOf<Byte>()
             while (running) {
                 try {
                     val data = input!!.read()
                     if (data == marker && buffer.isNotEmpty()) {
                         val barcode = String(buffer.toByteArray())
                         dataPublisher.onNext(barcode)

                         buffer = mutableListOf()
                     } else if (valid(data))
                         buffer.add(data.toByte())

                 } catch (e: IOException) {
                     Log.e(TAG, "${System.currentTimeMillis()} Unable to read")
                     running = false
                     reconnect()
                     break
                 }
             }*/


            val buffer2 = ByteArray(1024)
            var bytes: Int

            while (true) {
                try {
                    bytes = input!!.read(buffer2)
                    val strReceived = String(buffer2, 0, bytes)
                    val msgReceived = (strReceived.trim())
                    dataPublisher.onNext(msgReceived)
                    Log.d("newlandbarcode", msgReceived)

                } catch (e: IOException) { // TODO Auto-generated catch block
                    e.printStackTrace()
                    val msgConnectionLost = ("Connection lost:\n"
                            + e.message)

                }
            }
        }

        fun cancel() {
            running = false
            closeSocket(socket)
        }

        fun valid(data: Int): Boolean {

            if (data == 45)
                return true

            if (data in 97..122)
                return true

            if (data in 65..90)
                return true

            if (data in 48..57)
                return true

            return false
        }
    }


}