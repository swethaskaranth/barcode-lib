package com.poc.newlandscanner

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import com.google.zxing.BarcodeFormat
import com.google.zxing.ResultPoint
import com.google.zxing.client.android.BeepManager
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import com.pharmeasy.barcode.BarcodeReader
import com.pharmeasy.barcode.Event
import kotlinx.android.synthetic.main.scan_content.*

class ZxingScanActivity : AppCompatActivity(), DecoratedBarcodeView.TorchListener {

    override fun onTorchOff() {

    }

    override fun onTorchOn() {

    }

    lateinit var barcodeReader: BarcodeReader
    var barcodeView: DecoratedBarcodeView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_zxing_scan)

        barcodeReader = BarcodeReader.getInstance(this)

        //barcodeReader.barcodeView = findViewById(R.id.zxing_scanner_view)
        barcodeReader.initializeScanner(this, intent, R.id.zxing_scanner_view)

        if(barcodeReader.UIView!!)
            scan_lay.visibility = View.VISIBLE
        else
            cameraView.visibility = View.VISIBLE

    }

    override fun onResume() {
        super.onResume()
        if(!barcodeReader.UIView!!)
            barcodeReader.onResume()
        else
            barcodeReader.registerBroadcast(this)

        val barcodeObserver = Observer<Event<String>> { it ->
            it.getContentIfNotHandled()?.let {
                Toast.makeText(baseContext, it, Toast.LENGTH_LONG).show()
            }
        }

        BarcodeReader.barcodeData.observe(this, barcodeObserver)

    }

    override fun onPause() {
        super.onPause()
        if(!barcodeReader.UIView!!)
            barcodeReader.onPause()
        else
            barcodeReader.unregisterBroadcast(this)
    }
}
