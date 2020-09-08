package com.adwardstark.letooth.ble.gatt.client

import android.bluetooth.*
import android.content.Context
import android.util.Log
import com.adwardstark.letooth.LetoothUtils
import com.adwardstark.letooth.ble.gatt.client.callbacks.*
import com.adwardstark.letooth.commons.GattAttributes
import com.adwardstark.letooth.commons.GattUtils
import java.util.*

class LetoothGattClient private constructor(private val context: Context) {

    companion object {
        private var instance: LetoothGattClient? = null

        fun with(context: Context): LetoothGattClient {
            if(instance == null) {
                synchronized(lock = LetoothGattClient::class.java) {
                    instance = Builder(context).build()
                }
            }
            return instance!!
        }

        private val TAG = LetoothGattClient::class.java.simpleName
        private const val STATE_DISCONNECTED = 0
        private const val STATE_CONNECTING = 1
        private const val STATE_CONNECTED = 2
    }

    private class Builder(context: Context) {
        private var context: Context = context.applicationContext
        fun build(): LetoothGattClient = LetoothGattClient(context = context)
    }

    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var mBluetoothGatt: BluetoothGatt? = null

    private var mGattConnectionState = STATE_DISCONNECTED
    private var defaultMtuSize: Int = 20
    private var lastKnownDeviceAddress: String? = null

    private var letoothGattCallback: LetoothGattCallback? = null
    private var letoothMTUChangedCallback: LetoothMTUChangedCallback? = null
    private var letoothReadCallback: LetoothReadCallback? = null
    private var letoothWriteCallback: LetoothWriteCallback? = null
    private var letoothNotifyCallback: LetoothNotifyCallback? = null

    val isGattConnected: Boolean
        get() { return mGattConnectionState == STATE_CONNECTED }

    val getMaxSupportedMTU: Int = defaultMtuSize

    val supportedGattServices: List<BluetoothGattService>
        get() {
            requireNotNull(mBluetoothGatt) { "BluetoothGatt not initialised!" }
            return mBluetoothGatt!!.services
        }

    init {
        mBluetoothAdapter = LetoothUtils.getBluetoothAdapter()
    }

    // Extension function for converting byte to hex
    fun ByteArray.toHex(): String {
        return joinToString("") { "%02x".format(it) }
    }

    private val gattCallback = object: BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            if(newState == BluetoothProfile.STATE_CONNECTED) {
                mGattConnectionState = STATE_CONNECTED
                Log.d(TAG, "->onConnectionStateChange() Connected to GATT server.")
                letoothGattCallback?.onGattConnect()
                Log.d(TAG, "->onConnectionStateChange() Attempting to start service discovery:"
                        + mBluetoothGatt?.discoverServices())
            } else if(newState == BluetoothProfile.STATE_DISCONNECTED) {
                mGattConnectionState = STATE_DISCONNECTED
                Log.d(TAG, "->onConnectionStateChange() Disconnected from GATT server.")
                letoothGattCallback?.onGattDisconnect()
                letoothGattCallback = null
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            super.onServicesDiscovered(gatt, status)
            if(status == BluetoothGatt.GATT_SUCCESS) {
                letoothGattCallback?.onServicesDiscovered(gatt, supportedGattServices)
            } else {
                Log.d(TAG, "->onServicesDiscovered() Error statusCode: $status")
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            super.onCharacteristicChanged(gatt, characteristic)
            val hexData = characteristic.value.toHex()
            letoothGattCallback?.onCharacteristicChanged(hexData, characteristic.value, characteristic)
        }

        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            super.onMtuChanged(gatt, mtu, status)
            if(mtu > 20) defaultMtuSize = mtu
            letoothMTUChangedCallback?.onMtuChanged(mtu, status)
        }

        override fun onCharacteristicRead(gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic, status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, status)
            if(status == BluetoothGatt.GATT_SUCCESS) {
                val buffer = characteristic.value
                val hexString = buffer.toHex()
                letoothReadCallback?.onRead(hexString, buffer, characteristic)
            } else {
                Log.d(TAG, "->onCharacteristicRead() FAILED")
            }
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic, status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            if(status == BluetoothGatt.GATT_SUCCESS) {
                val buffer = characteristic.value
                val hexString = buffer.toHex()
                letoothWriteCallback?.onWrite(hexString, buffer, characteristic)
            } else {
                Log.d(TAG, "->onCharacteristicWrite() FAILED")
            }
        }

        override fun onDescriptorWrite(gatt: BluetoothGatt,
            descriptor: BluetoothGattDescriptor, status: Int
        ) {
            super.onDescriptorWrite(gatt, descriptor, status)
            if(status == BluetoothGatt.GATT_SUCCESS) {
                letoothNotifyCallback?.onNotificationSuccess()
            } else {
                letoothNotifyCallback?.onNotificationFailed()
            }
        }
    }

    fun connectToGatt(macAddress: String, callback: BluetoothGattCallback): Boolean {
        requireNotNull(mBluetoothAdapter) { "BluetoothAdapter not initialized" }

        // Previously connected device, Try to reconnect.
        if((lastKnownDeviceAddress != null && macAddress == lastKnownDeviceAddress) && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing bluetoothGatt for connection.")
            return if (mBluetoothGatt!!.connect()) {
                mGattConnectionState = STATE_CONNECTING
                true
            } else {
                false
            }
        }

        val bluetoothDevice = mBluetoothAdapter?.getRemoteDevice(macAddress)
        requireNotNull(bluetoothDevice) {
            "Unable to connect, device: $macAddress not found"
        }

        mBluetoothGatt = bluetoothDevice.connectGatt(context, false, callback)
        Log.d(TAG, "Trying to create a new connection with device: $macAddress")
        lastKnownDeviceAddress = macAddress
        mGattConnectionState = STATE_CONNECTING
        return true
    }

    fun connectToGatt(bluetoothDevice: BluetoothDevice, callback: BluetoothGattCallback): Boolean {
        return connectToGatt(bluetoothDevice.address, callback)
    }

    fun connectToGatt(macAddress: String, callback: LetoothGattCallback): Boolean {
        letoothGattCallback = callback
        return connectToGatt(macAddress, gattCallback)
    }

    fun disconnectFromGatt() {
        if(mBluetoothAdapter== null || mBluetoothGatt == null) return
        mBluetoothGatt?.disconnect()
    }

    fun closeGatt() {
        if(mBluetoothAdapter== null || mBluetoothGatt == null) return
        mBluetoothGatt?.close()
        mBluetoothGatt = null
        lastKnownDeviceAddress = null
        // Flush callbacks
        letoothMTUChangedCallback = null
        letoothReadCallback = null
        letoothWriteCallback = null
        letoothNotifyCallback = null
    }

    fun requestHigherMTU(size: Int) {
        requireNotNull(mBluetoothAdapter) { "BluetoothAdapter not initialized" }
        requireNotNull(mBluetoothGatt) { "BluetoothGatt not initialized" }

        Log.d(TAG, "Requesting for higher MTU of $size bytes")
        mBluetoothGatt?.requestMtu(size)
    }

    fun requestHigherMTU(size: Int, callback: LetoothMTUChangedCallback) {
        requireNotNull(letoothGattCallback) { "LetoothGattCallback not initialised" }
        letoothMTUChangedCallback = callback
        requestHigherMTU(size)
    }

    fun readFromGatt(serviceUUID: String, characteristicUUID: String) {
        requireNotNull(mBluetoothAdapter) { "BluetoothAdapter not initialized" }
        requireNotNull(mBluetoothGatt) { "BluetoothGatt not initialized" }

        val characteristic = GattUtils.getCharacteristicByUUID(
            serviceUUID,
            characteristicUUID,
            supportedGattServices
        )
        characteristic?.let { mBluetoothGatt?.readCharacteristic(it) }
    }

    fun readFromGatt(serviceUUID: String, characteristicUUID: String, callback: LetoothReadCallback) {
        requireNotNull(letoothGattCallback) { "LetoothGattCallback not initialised" }
        letoothReadCallback = callback
        readFromGatt(serviceUUID, characteristicUUID)
    }

    fun readFromGatt(serviceUUID: UUID, characteristicUUID: UUID) {
        readFromGatt(serviceUUID.toString(), characteristicUUID.toString())
    }

    fun readFromGatt(serviceUUID: UUID, characteristicUUID: UUID, callback: LetoothReadCallback) {
        requireNotNull(letoothGattCallback) { "LetoothGattCallback not initialised" }
        letoothReadCallback = callback
        readFromGatt(serviceUUID.toString(), characteristicUUID.toString())
    }

    fun writeToGatt(serviceUUID: String, characteristicUUID: String, data: ByteArray) {
        requireNotNull(mBluetoothAdapter) { "BluetoothAdapter not initialized" }
        requireNotNull(mBluetoothGatt) { "BluetoothGatt not initialized" }

        val characteristic = GattUtils.getCharacteristicByUUID(
            serviceUUID,
            characteristicUUID,
            supportedGattServices
        )
        characteristic?.let {
            it.value = data
            mBluetoothGatt?.writeCharacteristic(it)
        }
    }

    fun writeToGatt(serviceUUID: String, characteristicUUID: String, data: ByteArray, callback: LetoothWriteCallback) {
        requireNotNull(letoothGattCallback) { "LetoothGattCallback not initialised" }
        letoothWriteCallback = callback
        writeToGatt(serviceUUID, characteristicUUID, data)
    }

    fun writeToGatt(serviceUUID: UUID, characteristicUUID: UUID, data: ByteArray) {
        writeToGatt(serviceUUID.toString(), characteristicUUID.toString(), data)
    }

    fun writeToGatt(serviceUUID: UUID, characteristicUUID: UUID, data: ByteArray, callback: LetoothWriteCallback) {
        requireNotNull(letoothGattCallback) { "LetoothGattCallback not initialised" }
        letoothWriteCallback = callback
        writeToGatt(serviceUUID.toString(), characteristicUUID.toString(), data)
    }

    fun startGattNotification(serviceUUID: String, characteristicUUID: String): Boolean {
        return writeToGattDescriptor(serviceUUID, characteristicUUID, true)
    }

    fun startGattNotification(serviceUUID: String,
                              characteristicUUID: String,
                              callback: LetoothNotifyCallback): Boolean {
        requireNotNull(letoothGattCallback) { "LetoothGattCallback not initialised" }
        letoothNotifyCallback = callback
        return writeToGattDescriptor(serviceUUID, characteristicUUID, true)
    }

    fun startGattNotification(serviceUUID: UUID, characteristicUUID: UUID): Boolean {
        return writeToGattDescriptor(
            serviceUUID.toString(),
            characteristicUUID.toString(),
            true
        )
    }

    fun startGattNotification(serviceUUID: UUID,
                              characteristicUUID: UUID,
                              callback: LetoothNotifyCallback): Boolean {
        requireNotNull(letoothGattCallback) { "LetoothGattCallback not initialised" }
        letoothNotifyCallback = callback
        return writeToGattDescriptor(
            serviceUUID.toString(),
            characteristicUUID.toString(),
            true
        )
    }

    fun stopGattNotification(serviceUUID: String, characteristicUUID: String): Boolean {
        return writeToGattDescriptor(serviceUUID, characteristicUUID, false)
    }

    fun stopGattNotification(serviceUUID: UUID, characteristicUUID: UUID): Boolean {
        return writeToGattDescriptor(
            serviceUUID.toString(),
            characteristicUUID.toString(),
            false
        )
    }

    private fun writeToGattDescriptor(serviceUUID: String,
                                      characteristicUUID: String,
                                      isNotifyEnabled: Boolean): Boolean {
        requireNotNull(mBluetoothAdapter) { "BluetoothAdapter not initialized" }
        requireNotNull(mBluetoothGatt) { "BluetoothGatt not initialized" }

        val characteristic = GattUtils.getCharacteristicByUUID(
            serviceUUID,
            characteristicUUID,
            supportedGattServices
        ) ?: return false

        if(!mBluetoothGatt!!.setCharacteristicNotification(characteristic, isNotifyEnabled))
            return false

        val descriptor = characteristic
            .getDescriptor(UUID.fromString(GattAttributes.CLIENT_CHARACTERISTIC_CONFIG))

        if (isNotifyEnabled)
            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
        else
            descriptor.value = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE

        return mBluetoothGatt!!.writeDescriptor(descriptor)
    }

}