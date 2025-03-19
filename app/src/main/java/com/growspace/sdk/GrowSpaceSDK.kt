package com.growspace.sdk

import android.content.Context
import com.growspace.privatesdk.BLEScanner
import com.growspace.sdk.model.ScanRate
import com.growspace.sdk.model.SpaceLocation

public class GrowSpaceSDK(
    private val apiKey: String,
    private val context: Context,
) {

    private var bleScanner: BLEScanner? = null

    fun startScanning(scanRate: ScanRate = ScanRate.MEDIUM, onResponse: (Result<SpaceLocation>) -> Unit) {
        bleScanner = BLEScanner(apiKey, context)
        bleScanner?.startScan(scanRate.privateRate) { result ->
            result.fold(
                onSuccess = { privateLocation ->
                    onResponse(Result.success(SpaceLocation(privateLocation)))
                },
                onFailure = { error ->
                    onResponse(Result.failure(error))
                }
            )
        }
    }

    fun stopScanning() {
        bleScanner?.stopScan()
    }
}