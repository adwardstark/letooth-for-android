package com.adwardstark.letooth

import android.content.Context
import com.adwardstark.letooth.ble.gatt.client.LetoothGattClient
import com.adwardstark.letooth.ble.scanner.LetoothScanner
import com.adwardstark.letooth.commons.GattAttributes
import com.adwardstark.letooth.commons.GattUtils
import com.adwardstark.letooth.commons.HexConverter
import java.lang.RuntimeException

open class Letooth private constructor(private val context: Context) {

    companion object {
        private var singleton: Letooth? = null
        fun with(context: Context): Letooth {
            if(singleton == null) {
                synchronized(Letooth::class.java) {
                    singleton = Builder(context).build()
                }
            }
            return singleton!!
        }
    }

    private class Builder(context: Context) {
        private val context: Context = context.applicationContext
        fun build(): Letooth = Letooth(context)
    }

    // Utility functions
    fun hexConverter() = HexConverter
    fun gattAttributes() = GattAttributes
    fun gattUtils() = GattUtils
    fun utils() = LetoothUtils

    // Safety functions
    fun isBluetoothEnabled() = utils().isBluetoothEnabled()
    fun isBleSupported() = utils().isBleSupported(context.packageManager)
    fun isLocationServiceEnabled() = utils().isLocationServicesEnabled(context)
    fun isRunningOnEmulator() = utils().isAnEmulator()
    fun requestToEnableBluetooth() = utils().requestToEnableBluetooth(context)
    fun launchLocationServiceSettings() = utils().launchLocationServiceSettings(context)

    fun getLeScanBuilder(): LetoothScanner.ScanBuilder {
        if(isBluetoothEnabled()) {
            if(isBleSupported()) {
                if(isLocationServiceEnabled()) {
                    return LetoothScanner.ScanBuilder()
                } else {
                    throw RuntimeException("Location service is not enabled on this device!")
                }
            } else {
                throw RuntimeException("BLE not supported on this device!")
            }
        } else {
            throw RuntimeException("Bluetooth not enabled on this device!")
        }
    }

    fun getLeGattClient(): LetoothGattClient {
        if(isBluetoothEnabled()) {
            if(isBleSupported()) {
                return LetoothGattClient.with(context)
            } else {
                throw RuntimeException("BLE not supported on this device!")
            }
        } else {
            throw RuntimeException("Bluetooth not enabled on this device!")
        }
    }

}