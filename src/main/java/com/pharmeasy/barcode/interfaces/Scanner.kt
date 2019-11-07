package com.pharmeasy.barcode.interfaces

import android.content.Context

interface Scanner {
    fun enableScanner(context: Context)
    fun disableScanner(context: Context)
}