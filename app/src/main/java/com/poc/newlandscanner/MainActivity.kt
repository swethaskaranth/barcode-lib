package com.poc.newlandscanner

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), View.OnClickListener {

    val TAG = MainActivity::class.java.simpleName

    val newlandBroadcast = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(TAG,"OnReceive:intent:${intent?.getStringExtra("SCAN_BARCODE1")}")
            if(intent!=null){
                val barcode = intent.getStringExtra("SCAN_BARCODE1")
                val barcodeType = intent.getIntExtra("SCAN_BARCODE_TYPE",-1)
                val status = intent.getStringExtra("EXTRA_SCAN_STATE")
                    if(barcode!=null){
                        Log.d(TAG,"barcodeData:${barcode}")
                      // barcodeTv.text=barcodeData
                        barcodeTypeTv.text=barcode

                    } else{
                        Toast.makeText(context,"Unable to fetch barcodeData",Toast.LENGTH_SHORT).show()
                    }


            }else{
                Toast.makeText(context,"Scan failed",Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        disable.setOnClickListener(this)
        enable.setOnClickListener(this)

        //1: to get the result directly on edittext 3: to get the result via broadcast.
        val intent = Intent("ACTION_BAR_SCANCFG")
        intent.putExtra("EXTRA_SCAN_MODE",3)
        this.sendBroadcast(intent)

        this
            .registerReceiver(newlandBroadcast, IntentFilter("nlscan.action.SCANNER_RESULT"))


    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when(keyCode){

            KeyEvent.KEYCODE_F1->{
                Log.d(TAG,"KEYCODE_F1")
                val intent = Intent("ACTION_BAR_SCANCFG")
                intent.putExtra("EXTRA_SCAN_POWER",0)
                this.sendBroadcast(intent)

            }
            KeyEvent.KEYCODE_F2->{
                Log.d(TAG,"KEYCODE_F2")
                val intent = Intent("ACTION_BAR_SCANCFG")
                intent.putExtra("EXTRA_SCAN_POWER",1)
                this.sendBroadcast(intent)
            }



        }
        return super.onKeyDown(keyCode, event)

    }

    override fun onDestroy() {
        super.onDestroy()
        this.unregisterReceiver(newlandBroadcast)
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.disable->{
                Log.d(TAG,"Disable scanner")
                val intent = Intent("ACTION_BAR_SCANCFG")

                intent.putExtra("EXTRA_SCAN_POWER",0)
                this.sendBroadcast(intent)
            }
            R.id.enable->{
                Log.d(TAG,"enable scanner")
                val intent = Intent("ACTION_BAR_SCANCFG")
                intent.putExtra("EXTRA_SCAN_POWER",1)
                this.sendBroadcast(intent)
            }
        }
    }
}
