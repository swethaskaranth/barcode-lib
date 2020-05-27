package com.pharmeasy.barcode.scanners.bluetoothScanner

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.pharmeasy.barcode.BarcodeReader
import com.pharmeasy.barcode.R
import kotlinx.android.synthetic.main.devices_content.*

class DevicesActivity : AppCompatActivity(), ScannerActionListener {

    private val TAG = "DA"
    private val ENABLE_BT = 1000

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.devices_content)

        /*title = getString(R.string.devices)

        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.white))*/

        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(false)

        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        list.layoutManager = layoutManager


        val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        val pairedDevices = mBluetoothAdapter.bondedDevices

        val s = ArrayList<String>()
        for (bt in pairedDevices)
            s.add(bt.name)

        ivBack.setOnClickListener { finish() }

        ivRefresh.setOnClickListener { reload() }

        //setListAdapter(ArrayAdapter(this, R.layout.list, s))
    }

    override fun onBackPressed() {
        super.onBackPressed()
    //    BarcodeReader.clearMode()
    }

  /*  override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.refresh, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.refresh -> reload()
            android.R.id.home -> {
              //  BarcodeReader.clearMode()
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }*/

    public override fun onResume() {
        super.onResume()

        ScannerService.register(this)

        if (!BluetoothAdapter.getDefaultAdapter().isEnabled) {
            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)

            startActivityForResult(intent, ENABLE_BT)
        } else
            reload()
    }

    public override fun onPause() {
        super.onPause()

        ScannerService.deregister(this)
    }

    private fun reload(): Boolean {

        val devices = ScannerService.getPairedDevices()
        list.adapter = DeviceAdapter(this, devices)

        if (devices.isEmpty()) {
            empty_panel.visibility = View.VISIBLE
            list.visibility = View.GONE
        } else {
            empty_panel.visibility = View.GONE
            list.visibility = View.VISIBLE
        }


        return true
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                reload()
            } else {
                message(getString(R.string.bt_not_enabled))
                finish()
            }
        } else
            super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onConnected() {
        // hideProgress()

        //  if(mode != null)
        finish()
    }

    override fun onConnecting() {
        // showProgress()
    }

    override fun onDisconnected() {
        //  hideProgress()

        reload()
    }

    override fun onDestroy() {
        super.onDestroy()
        BarcodeReader.clearMode()
    }

    override fun onData(barcode: String) {
        Log.d("newlander", barcode)
    }

    private fun message(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }


}