package com.pharmeasy.barcode.scanners.bluetoothScanner

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView
import com.pharmeasy.barcode.R
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.item_device.view.*
import java.util.concurrent.TimeUnit

class DeviceAdapter(private val context: Context, private val items: List<Device>) : RecyclerView.Adapter<DeviceAdapter.ViewHolder>() {

    private val subject = PublishSubject.create<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceAdapter.ViewHolder {

        val view = LayoutInflater.from(context).inflate(R.layout.item_device, parent, false)
        return DeviceAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: DeviceAdapter.ViewHolder, position: Int) {

        val device = items[position]

        holder.view.tag = device
        holder.name.text = device.name
        holder.connect.isChecked = device.connected
       // holder.connect.isEnabled = enabled

        holder.connect.setOnCheckedChangeListener { _, checked ->

            val connected = items.firstOrNull { it.connected }
            val item = holder.view.tag as Device

            if (checked) {
                if (connected != null && connected.address != item.address) {
                    val builder = AlertDialog.Builder(context)
                            .setTitle("Switch Scanner")
                            .setMessage("Are you sure to switch from ${connected.name} to ${item.name}?")
                            .setPositiveButton(R.string.continu) { _, _ -> onSwitch(item.address) }
                            .setNegativeButton(R.string.cancel) { _, _ -> holder.connect.isChecked = false }

                    builder.create().show()
                } else
                    ScannerService.connect(item.address)
            } else if (item.connected) {

                val builder = AlertDialog.Builder(context)
                        .setTitle("Confirmation")
                        .setMessage("Are you sure to disconnect ${item.name}?")
                        .setPositiveButton(R.string.disconnect, { _, _ -> onDisconnect() })
                        .setNegativeButton(R.string.cancel, { _, _ -> holder.connect.isChecked = true })

                builder.create().show()
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    private fun onSwitch(address: String) {
        ScannerService.disconnect()

        Observable.just("switch").delay(5, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    ScannerService.connect(address)
                }
    }

    private fun onDisconnect() {
        ScannerService.disconnect()
    }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val view: View = v
        val name: TextView = v.name
        val connect: SwitchCompat = v.connect
    }
}