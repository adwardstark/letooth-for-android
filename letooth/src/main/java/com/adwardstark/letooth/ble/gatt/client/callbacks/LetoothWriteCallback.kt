package com.adwardstark.letooth.ble.gatt.client.callbacks

import android.bluetooth.BluetoothGattCharacteristic

interface LetoothWriteCallback {

    fun onWrite(hexString: String, buffer: ByteArray, characteristic: BluetoothGattCharacteristic)

}