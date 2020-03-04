package com.poc.newlandscanner

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.zxing.integration.android.IntentIntegrator
import com.pharmeasy.barcode.BarcodeReader
import kotlinx.android.synthetic.main.activity_zxing.*

class ZxingActivity : AppCompatActivity() {

    lateinit var barcodeReader: BarcodeReader

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_zxing)

        //barcodeReader = BarcodeReader.getInstance(this)

        button.setOnClickListener {
                IntentIntegrator(this).setOrientationLocked(false)
                    .setCaptureActivity(ZxingScanActivity::class.java).initiateScan()

        }

    }

}
