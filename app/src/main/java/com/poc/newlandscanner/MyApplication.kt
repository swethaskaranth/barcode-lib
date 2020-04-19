package com.poc.newlandscanner

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import com.pharmeasy.barcode.BarcodeReader

class MyApplication : Application(){

    private var barcodeReader: BarcodeReader? = null

    override fun onCreate() {
        super.onCreate()

        mContext = applicationContext

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            setApplicationOrientation()
        }

        barcodeReader = BarcodeReader.getInstance(this)
    }


    private fun setApplicationOrientation() {
        registerActivityLifecycleCallbacks(object : ActivityLifecycleAdapter() {
            override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {
                activity!!.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
        })
        registerActivityLifecycleCallbacks(object : ActivityLifecycleAdapter() {
            override fun onActivityResumed(activity: Activity?) {
                super.onActivityResumed(activity)
                barcodeReader?.registerBroadcast(activity?.baseContext!!)
            }

            override fun onActivityPaused(activity: Activity?) {
                super.onActivityPaused(activity)
                barcodeReader?.unregisterBroadcast(activity?.baseContext!!)
            }
        })
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var mContext: Context? = null

        fun getApplicationContex(): Context {
            return mContext!!
        }

        fun getAppClass(): MyApplication {
            return mContext as MyApplication

        }
    }

    fun getBarcodeReader(): BarcodeReader {
        return barcodeReader!!
    }

}