package com.adwardstark.letooth.ble.scanner

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.os.Handler
import android.util.Log
import com.adwardstark.letooth.LetoothUtils

class LetoothScanManager(builder: ScanBuilder): LetoothScanner(builder) {

    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var mBluetoothLeScanner: BluetoothLeScanner? = null
    private lateinit var handler: Handler

    companion object {
        private val TAG = LetoothScanManager::class.java.simpleName
    }

    init {
        mBluetoothAdapter = LetoothUtils.getBluetoothAdapter()
    }

    override fun startScan(callback: ScanCallback) {
        mBluetoothLeScanner = mBluetoothAdapter!!.bluetoothLeScanner
        requireNotNull(mBluetoothLeScanner) { "Bluetooth-LE not available" }

        Log.d(TAG, "Starting le-scan")
        mBluetoothLeScanner?.startScan(scanFilters, scanSettings, callback)
        handler = Handler()
        handler.postDelayed({
            stopScan(callback)
        }, scanPeriod.toLong())
    }

    override fun startScan(callback: ScanCallback, onScanFinished: () -> Unit) {
        mBluetoothLeScanner = mBluetoothAdapter!!.bluetoothLeScanner
        requireNotNull(mBluetoothLeScanner) { "Bluetooth-LE not available" }

        Log.d(TAG, "Starting le-scan")
        mBluetoothLeScanner?.startScan(scanFilters, scanSettings, callback)
        handler = Handler()
        handler.postDelayed({
            stopScan(callback)
            onScanFinished()
        }, scanPeriod.toLong())
    }

    override fun startContinuousScan(timeOutInMilliseconds: Long,
                                     callback: ScanCallback,
                                     onScanFinished: () -> Unit) {
        mBluetoothLeScanner = mBluetoothAdapter!!.bluetoothLeScanner
        requireNotNull(mBluetoothLeScanner) { "Bluetooth-LE not available" }

        Log.d(TAG, "Starting continuous le-scan")
        mBluetoothLeScanner?.startScan(scanFilters, scanSettings, callback)
        handler = Handler()
        handler.postDelayed({
            stopScan(callback)
            onScanFinished()
        }, timeOutInMilliseconds)
    }

    override fun startContinuousScan(callback: ScanCallback, onScanFinished: () -> Unit) {
        startContinuousScan(
            LetoothUtils.DEFAULT_MAX_SCAN_PERIOD.toLong(),
            callback,
            onScanFinished
        )
    }

    override fun stopScan(callback: ScanCallback) {
        mBluetoothLeScanner = mBluetoothAdapter!!.bluetoothLeScanner
        requireNotNull(mBluetoothLeScanner) { "Bluetooth-LE not available" }

        Log.d(TAG, "Stopping le-scan")
        mBluetoothLeScanner?.stopScan(callback)
        mBluetoothLeScanner = null
    }

}