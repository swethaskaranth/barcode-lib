package com.poc.newlandscanner

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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


    lateinit var barcodeReader: BarcodeReader
    var barcodeView: DecoratedBarcodeView? = null

    val CAMERA_REQ = 100


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_zxing_scan)

        initScanner()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermissions()
        }

    }

     /* //  barcodeReader = BarcodeReader.getInstance(this)

        //barcodeReader.barcodeView = findViewById(R.id.zxing_scanner_view)
        barcodeReader.initializeScanner(this, intent, R.id.zxing_scanner_view,null)

        if(barcodeReader.UIView!!)
            scan_lay.visibility = View.VISIBLE
        else
            cameraView.visibility = View.VISIBLE

    }*/

    private fun initScanner(){



        if(MyApplication.getAppClass().getBarcodeReader().UIView!!){
            cameraView.visibility = View.GONE
            scan_lay.visibility = View.VISIBLE

        } else {
            //
            MyApplication.getAppClass().getBarcodeReader().initializeScanner(this, intent, R.id.zxing_scanner_view,editText)
            scan_lay.visibility = View.GONE
            cameraView.visibility = View.VISIBLE
            barcodeView = findViewById(R.id.zxing_scanner_view)
            barcodeView?.setTorchListener(this)
        }

        /* editText?.requestFocus()
         editText?.addTextChangedListener(object : TextWatcher {
             override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

             }

             override fun afterTextChanged(s: Editable?) {
                 // if(s!= null)
                 Toast.makeText(this@ZxingActivity,s?.toString()?:"No Text",Toast.LENGTH_SHORT).show()
             }

             override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // editText.text.clear()
             }
         })


         editText.setOnFocusChangeListener(View.OnFocusChangeListener { v, hasFocus ->
             editText.requestFocus()
             val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
             imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
         })*/

        val barcodeObserver = Observer<Event<String>> {
            it.getContentIfNotHandled()?.let { i ->
                Toast.makeText(this,i,Toast.LENGTH_SHORT).show()
            }
        }

        BarcodeReader.barcodeData.observe(this, barcodeObserver)
    }

    override fun onResume() {
        super.onResume()

        if (!MyApplication.getAppClass().getBarcodeReader().UIView!!) {
            MyApplication.getAppClass().getBarcodeReader().onResume()
            barcodeView?.resume()
        }
        else
            MyApplication.getAppClass().getBarcodeReader().registerBroadcast(
                    this
            )

    }

    override fun onPause() {

        if (!MyApplication.getAppClass().getBarcodeReader().UIView!!) {
            MyApplication.getAppClass().getBarcodeReader().onPause()
            barcodeView?.pause()
        }
        else
            MyApplication.getAppClass().getBarcodeReader().unregisterBroadcast(
                    this
            )
        super.onPause()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkPermissions(): Boolean {
        if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                    this, arrayOf(
                    Manifest.permission.CAMERA
            ),
                    CAMERA_REQ
            )
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
    ) {
        when (requestCode) {
            CAMERA_REQ -> {
                // If request is CANCELLED, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    if (!MyApplication.getAppClass().getBarcodeReader().UIView!!) {
                        MyApplication.getAppClass().getBarcodeReader().onResume()
                        barcodeView?.resume()
                    }
                    else
                        MyApplication.getAppClass().getBarcodeReader().registerBroadcast(
                                this
                        )
                }
                return
            }
        }
    }

    override fun onTorchOn() {
    }

    override fun onTorchOff() {
    }
}
