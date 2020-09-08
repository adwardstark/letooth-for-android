package com.adwardstark.letooth

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import androidx.core.location.LocationManagerCompat
import java.lang.RuntimeException

object LetoothUtils {

    const val DEFAULT_SCAN_PERIOD = 2000
    const val DEFAULT_MAX_SCAN_PERIOD = 600000

    private fun PackageManager.missingSystemFeature(name: String): Boolean = !hasSystemFeature(name)

    fun getBluetoothAdapter(): BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    fun requestToEnableBluetooth(context: Context) {
        context.startActivity(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
    }

    fun isBluetoothEnabled(): Boolean {
        if(isAnEmulator()) throw RuntimeException("Bluetooth is not supported on an emulator!")
        return getBluetoothAdapter().isEnabled
    }

    fun isBleSupported(packageManager: PackageManager): Boolean {
        if(isAnEmulator()) throw RuntimeException("BLE is not supported on an emulator!")
        return !packageManager.missingSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
    }

    fun isLocationServicesEnabled(context: Context): Boolean {
        // Check for location services
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return LocationManagerCompat.isLocationEnabled(locationManager)
    }

    fun launchLocationServiceSettings(context: Context) {
        context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
    }

    fun isAnEmulator(): Boolean {
        return (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.HARDWARE.contains("goldfish")
                || Build.HARDWARE.contains("ranchu")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.PRODUCT.contains("sdk_google")
                || Build.PRODUCT.contains("google_sdk")
                || Build.PRODUCT.contains("sdk")
                || Build.PRODUCT.contains("sdk_x86")
                || Build.PRODUCT.contains("vbox86p")
                || Build.PRODUCT.contains("emulator")
                || Build.PRODUCT.contains("simulator")
    }
}