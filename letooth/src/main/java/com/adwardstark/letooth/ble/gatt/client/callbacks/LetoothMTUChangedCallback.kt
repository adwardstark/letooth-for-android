package com.adwardstark.letooth.ble.gatt.client.callbacks

interface LetoothMTUChangedCallback {

    fun onMtuChanged(mtuSize: Int, status: Int)

}