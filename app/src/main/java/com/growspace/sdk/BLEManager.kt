package com.growspace.sdk

import android.content.Context
import com.growspace.privatesdk.BLEScanner
import com.growspace.privatesdk.ZoneResponse

public class BLEManager(private val context: Context) {

    private var bleScanner: BLEScanner? = null

    fun startScanning(onDeviceFound: (String, Int) -> Unit) {
        bleScanner = BLEScanner(context) { macAddress, rssi ->
            onDeviceFound(macAddress, rssi)
        }
        bleScanner?.startScan()
    }

    fun stopScanning() {
        bleScanner?.stopScan()
    }

    fun sendBeaconData(macAddress: String, onResponse: (ZoneResponse?) -> Unit) {
        bleScanner?.sendBeaconData(macAddress, onResponse)
    }
}