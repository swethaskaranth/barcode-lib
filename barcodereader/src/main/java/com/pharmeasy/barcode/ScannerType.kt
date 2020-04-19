package com.pharmeasy.barcode

enum class ScannerType(var displayName : String) {
    BLUETOOTH_SCANNER("Bluetooth Scanner"), OTG_SCANNER("OTG Scanner"), CAMERA_SCANNER("Camera Scanner");

    companion object{
        fun toStringList(): List<String> {
            val list = ArrayList<String>()
            for(type in values())
                list.add(type.displayName)
            return list
        }
    }
}