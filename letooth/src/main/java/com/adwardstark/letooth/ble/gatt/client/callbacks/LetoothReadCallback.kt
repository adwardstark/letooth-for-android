package com.adwardstark.letooth.ble.gatt.client.callbacks

import android.bluetooth.BluetoothGattCharacteristic

interface LetoothReadCallback {

    fun onRead(hexString: String, byteArray: ByteArray, characteristic: BluetoothGattCharacteristic)

}