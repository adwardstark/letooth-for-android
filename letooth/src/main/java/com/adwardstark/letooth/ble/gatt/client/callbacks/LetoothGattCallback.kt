package com.adwardstark.letooth.ble.gatt.client.callbacks

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService

interface LetoothGattCallback {

    fun onServicesDiscovered(gatt: BluetoothGatt, service: List<BluetoothGattService>?)

    fun onCharacteristicChanged(hexString: String?, byteBuffer: ByteArray, characteristic: BluetoothGattCharacteristic)

    fun onGattConnect()

    fun onGattDisconnect()
}