package com.adwardstark.letooth.ble.scanner

import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanSettings
import android.os.ParcelUuid
import com.adwardstark.letooth.LetoothUtils

abstract class LetoothScanner protected constructor(builder: ScanBuilder) {

    protected var scanPeriod: Int = 0
    protected var scanFilters: List<ScanFilter>? = null
    protected var scanSettings: ScanSettings? = null

    init {
        this.scanPeriod = builder.scanPeriod
        this.scanFilters = builder.scanFilters
        this.scanSettings = builder.scanSettings
    }

    abstract fun startScan(callback: ScanCallback)
    abstract fun startScan(callback: ScanCallback, onScanFinished: () -> Unit)
    abstract fun startContinuousScan(callback: ScanCallback, onScanFinished: () -> Unit)
    abstract fun startContinuousScan(timeOutInMilliseconds: Long,
                                     callback: ScanCallback,
                                     onScanFinished: () -> Unit)
    abstract fun stopScan(callback: ScanCallback)

    class ScanBuilder {
        var scanPeriod: Int = LetoothUtils.DEFAULT_SCAN_PERIOD
        val scanFilters: MutableList<ScanFilter> = arrayListOf()
        var scanSettings: ScanSettings = ScanSettings.Builder().build()

        fun setScanPeriod(interval: Int): ScanBuilder {
            scanPeriod = interval
            return this
        }

        fun setFilterByMAC(vararg macAddress: String): ScanBuilder {
            for (address in macAddress) {
                scanFilters.add(
                    ScanFilter.Builder()
                        .setDeviceAddress(address)
                        .build()
                )
            }
            return this
        }

        fun setFilterByServiceUUID(vararg serviceUuid: String): ScanBuilder {
            for (uuid in serviceUuid) {
                scanFilters.add(
                    ScanFilter.Builder()
                        .setServiceUuid(ParcelUuid.fromString(uuid))
                        .build()
                )
            }
            return this
        }

        fun setFilterByName(vararg deviceName: String): ScanBuilder {
            for (name in deviceName) {
                scanFilters.add(
                    ScanFilter.Builder()
                        .setDeviceName(name)
                        .build()
                )
            }
            return this
        }

        fun setSettingsScan(scanSettings: ScanSettings): ScanBuilder {
            this.scanSettings = scanSettings
            return this
        }

        fun build(): LetoothScanner {
            return LetoothScanManager(this)
        }
    }

}