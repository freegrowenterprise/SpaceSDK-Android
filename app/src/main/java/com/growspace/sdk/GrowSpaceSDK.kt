package com.growspace.sdk

import android.content.Context
import com.growspace.privatesdk.BLEScanner
import com.growspace.sdk.model.ScanRate
import com.growspace.sdk.model.SpaceLocation
import com.growspace.sdk.model.UwbRange
import com.growspace.sdk.uwb.SpaceUwb

public class GrowSpaceSDK(
    private val apiKey: String,
    private val context: Context,
) {

    private var bleScanner: BLEScanner? = null
    private var spaceUwb: SpaceUwb = SpaceUwb()

    fun startScanning(
        scanRate: ScanRate = ScanRate.MEDIUM,
        onResponse: (Result<SpaceLocation>) -> Unit
    ) {
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

    fun startUwbRanging(
        /// 최대 연결 수
        maxConnectCount: Int = 4,
        /// 최대 거리 (cm)
        maxDistance: Long = 800,
        onResponse: (Result<UwbRange>) -> Unit
    ) {
        spaceUwb.startUwbRanging(maxConnectCount, maxDistance) { result ->
            result.fold(
                onSuccess = { uwbRange ->
                    onResponse(Result.success(uwbRange))
                },
                onFailure = { error ->
                    onResponse(Result.failure(error))
                }
            )
        }
    }

    fun stopUwbRanging() {
        spaceUwb.stopUwbRanging()
    }
}