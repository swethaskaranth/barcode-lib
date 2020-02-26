package com.poc.newlandscanner

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.Observer
import com.pharmeasy.barcode.BarcodeReader
import com.pharmeasy.barcode.BarcodeReader.Companion.barcodeData
import com.pharmeasy.barcode.Event
import kotlinx.android.synthetic.main.activity_zebra.*

class ZebraActivity : AppCompatActivity(), View.OnClickListener {

    val TAG = ZebraActivity::class.java.simpleName

    val softScanTrigger = "com.symbol.datawedge.api.ACTION_SCANNERINPUTPLUGIN"
    val extraData ="com.symbol.datawedge.api.EXTRA_PARAMETER"
    var barcodeReader:BarcodeReader?=null

    /*val dataWedgeBroadcast = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            val bundle = intent?.extras

            Log.d(TAG,"Action:${action}, bundle:${bundle}")

            if(action?.equals(resources.getString(R.string.activity_intent_filter_action))!!){
                try {
                    displayResults(intent!!,"via broadcast")
                }catch (e:Exception){


                }
            }
        }
    }*/

    private fun displayResults(initiatingIntent:Intent,howDataReceived:String){
        var decodedSource = initiatingIntent.getStringExtra(resources.getString(R.string.datawedge_intent_key_source))
        var decodedData = initiatingIntent.getStringExtra(resources.getString(R.string.datawedge_intent_key_data))
        var decodedLabelType = initiatingIntent.getStringExtra(resources.getString(R.string.datawedge_intent_key_label_type))

        if(null==decodedSource){
            decodedSource = initiatingIntent.getStringExtra(resources.getString(R.string.datawedge_intent_key_source_legacy))
            decodedData = initiatingIntent.getStringExtra(resources.getString(R.string.datawedge_intent_key_data_legacy))
            decodedLabelType = initiatingIntent.getStringExtra(resources.getString(R.string.datawedge_intent_key_label_type_legacy))
        }

        sourceTv.text=decodedSource
       // scanDataTv.text= BarcodeReader.barcodeObservable.get()
        scanLabelTv.text=decodedLabelType


    }

    override fun onResume() {
        super.onResume()
        barcodeReader?.registerBroadcast(this)

        val barcodeObserver = Observer<Event<String>> { barcode ->
            barcode.getContentIfNotHandled()?.let {
                scanDataTv.text=it
            }
        }

        barcodeData.observe(this,barcodeObserver)

        Log.d("DEVICE_NAME","The device name is: ${Build.MANUFACTURER}")

    }

    override fun onPause() {
        super.onPause()
        barcodeReader?.unregisterBroadcast(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_zebra)

       /* val intentFilter = IntentFilter()
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT)
        intentFilter.addAction(resources.getString(R.string.activity_intent_filter_action))
        registerReceiver(dataWedgeBroadcast,intentFilter)


*/
        barcodeReader=BarcodeReader.getInstance(this)



        disableBtn.setOnClickListener(this)
        enableBtn.setOnClickListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
       // unregisterReceiver(dataWedgeBroadcast)
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.disableBtn->disableScanner()
            R.id.enableBtn->enableScanner()
        }
    }

    fun disableScanner(){
        barcodeReader?.disableScanner()
        /*val intent = Intent()
        intent.action=softScanTrigger
        intent.putExtra(extraData,"DISABLE_PLUGIN")
        sendBroadcast(intent)*/
    }

    fun enableScanner(){
       barcodeReader?.enableScanner()
        /*val intent = Intent()
        intent.action=softScanTrigger
        intent.putExtra(extraData,"ENABLE_PLUGIN")
        sendBroadcast(intent)*/
    }
}
