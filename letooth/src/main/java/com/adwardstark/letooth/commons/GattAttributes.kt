package com.adwardstark.letooth.commons

import java.util.HashMap

object GattAttributes {

    // Unknowns
    private const val UNKNOWN_SERVICE = "Unknown Service"
    private const val UNKNOWN_CHARACTERISTIC = "Unknown Characteristic"

    // Generic Services
    const val GENERIC_ACCESS = "00001800-0000-1000-8000-00805f9b34fb"
    const val GENERIC_ATTRIBUTE = "00001801-0000-1000-8000-00805f9b34fb"
    const val DEVICE_INFORMATION_SERVICE = "0000180a-0000-1000-8000-00805f9b34fb"
    const val HEART_RATE = "0000180d-0000-1000-8000-00805f9b34fb"

    // Generic Characteristics
    const val DEVICE_NAME = "00002a00-0000-1000-8000-00805f9b34fb"
    const val SERIAL_NUMBER = "00002a25-0000-1000-8000-00805f9b34fb"
    const val APPEARANCE = "00002a01-0000-1000-8000-00805f9b34fb"
    const val MANUFACTURER_NAME = "00002a29-0000-1000-8000-00805f9b34fb"
    const val SERVICE_CHANGED = "00002a05-0000-1000-8000-00805f9b34fb"
    const val PERIPHERAL_PREFERRED_CONNECTION_PARAMETERS = "00002a04-0000-1000-8000-00805f9b34fb"
    const val CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb"
    const val HEART_RATE_MEASUREMENT = "00002a37-0000-1000-8000-00805f9b34fb"

    private val serviceAttributes = HashMap<String,String>()
    private val characteristicAttributes = HashMap<String,String>()

    init {
        // Generic Services.
        serviceAttributes[GENERIC_ACCESS] = "Generic Access"
        serviceAttributes[GENERIC_ATTRIBUTE] = "Generic Attribute"
        serviceAttributes[DEVICE_INFORMATION_SERVICE] = "Device Information Service"
        serviceAttributes[HEART_RATE] = "Heart Rate Service"

        // Generic Characteristics.
        characteristicAttributes[DEVICE_NAME] = "Device Name"
        characteristicAttributes[SERIAL_NUMBER] = "Serial Number"
        characteristicAttributes[APPEARANCE] = "Appearance"
        characteristicAttributes[MANUFACTURER_NAME] = "Manufacturer Name String"
        characteristicAttributes[SERVICE_CHANGED] = "Service Changed"
        characteristicAttributes[PERIPHERAL_PREFERRED_CONNECTION_PARAMETERS] = "Peripheral Preferred Connection Parameters"
        characteristicAttributes[HEART_RATE_MEASUREMENT] = "Heart Rate Measurement"
    }

    fun getGattServiceName(uuid: String): String {
        val name = serviceAttributes[uuid]
        return name ?: UNKNOWN_SERVICE
    }

    fun getGattCharacteristicName(uuid: String): String {
        val name = serviceAttributes[uuid]
        return name ?: UNKNOWN_CHARACTERISTIC
    }

}