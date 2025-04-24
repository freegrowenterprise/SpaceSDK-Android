package com.growspace.sdk

import android.content.Context
import com.growspace.sdk.bluetooth.BluetoothLEManagerHelper
import com.growspace.sdk.controller.UWBController
import com.growspace.sdk.location.LocationManagerHelper
import com.growspace.sdk.logger.LoggerHelper
import com.growspace.sdk.model.UwbDisconnect
import com.growspace.sdk.model.UwbRange
import com.growspace.sdk.permissions.PermissionHelper
import com.growspace.sdk.storage.database.DatabaseStorageHelper
import com.growspace.sdk.storage.preferences.PreferenceStorageHelper
import com.growspace.sdk.uwb.UwbManagerHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class SpaceUwb(
    private val apiKey: String, context: Context, activity: android.app.Activity
) {

    private val uwbScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var uwbJob: Job? = null

    private val uWBController: UWBController = UWBController(
        PermissionHelper(activity),
        PreferenceStorageHelper(context),
        DatabaseStorageHelper(context),
        LoggerHelper(context),
        BluetoothLEManagerHelper(context),
        LocationManagerHelper(context),
        UwbManagerHelper(context)
    )

    init {

        this.uWBController.onCreate()
    }

    fun startUwbRanging(
        ///  장치 최대 연결 개수. 디폴트 값 4.
        maximumConnectionCount: Int = 4,

        /// 최대 연결 거리. 해당 값을 초과할 경우 연결을 끊고, 다른 UWB 장치와 연결 시도. 기본값 8(m)
        replacementDistanceThreshold: Float = 8f,

        /// RSSI 신호가 강한 장치부터 연결 시도. 기본값 true.
        isConnectStrongestSignalFirst: Boolean = true,

        onUpdate: (UwbRange) -> Unit, onDisconnect: (UwbDisconnect) -> Unit
    ) {
        stopUwbRanging(
            onComplete = { result ->
                if (result.isFailure) {
                    println("❌ UWB 작업 중지 실패: ${result.exceptionOrNull()}")
                } else {
                    println("✅ UWB 작업 중지 성공")
                }
            })

        uwbJob = uwbScope.launch {
            delay(1000)
//            val dummyRange = UwbRange(
//                deviceName = "DummyDevice",
//                distance = (0..100).random().coerceAtLeast(0).toLong(),
//                azimuth = (0..360).random().toLong(),
//                elevation = null,
//                direction = null
//            )
//            onUpdate(dummyRange)

            uWBController.onStart(
                maximumConnectionCount,
                replacementDistanceThreshold,
                isConnectStrongestSignalFirst,
                onUpdate,
                onDisconnect
            )
        }

        uwbJob?.start()
    }

    private fun stopUwbRanging(onComplete: ((Result<Unit>) -> Unit)? = null) {
        try {
            uwbJob?.cancel()
            uwbJob = null

            if (uWBController.onStop()) {
                println("✅ UWB 작업 중지 성공")
                onComplete?.invoke(Result.success(Unit))
            } else {
                println("❌ UWB 작업 중지 실패")
                onComplete?.invoke(Result.failure(Exception("UWB 작업 중지 실패")))
            }

        } catch (e: Exception) {
            onComplete?.invoke(Result.failure(e))
        }
    }
}