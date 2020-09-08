package com.adwardstark.letooth.commons

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService

object GattUtils {

    fun getServiceByUUID(serviceUUID: String,
                                 services: List<BluetoothGattService>): BluetoothGattService? {
        var fetchedService: BluetoothGattService? = null
        services.forEach { gattService ->
            if(serviceUUID == gattService.uuid.toString())
                fetchedService = gattService
        }
        return fetchedService
    }

    fun getCharacteristicByUUID(serviceUUID: String,
                                characteristicUUID: String,
                                services: List<BluetoothGattService>): BluetoothGattCharacteristic? {
        var fetchedCharacteristic: BluetoothGattCharacteristic? = null
        val gattService = getServiceByUUID(serviceUUID, services)
        gattService?.characteristics?.forEach { gattCharacteristic ->
            if(characteristicUUID == gattCharacteristic.uuid.toString()){
                fetchedCharacteristic = gattCharacteristic
            }
        }
        return fetchedCharacteristic
    }

}