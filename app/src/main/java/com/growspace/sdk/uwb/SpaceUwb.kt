package com.growspace.sdk.uwb

import com.growspace.sdk.model.UwbDisconnect
import com.growspace.sdk.model.UwbRange
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class SpaceUwb {
    private val uwbScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var uwbJob: Job? = null

    fun startUwbRanging(
        ///  장치 최대 연결 개수. 디폴트 값 4.
        maximumConnectionCount: Int = 4,

        /// 최대 연결 거리. 해당 값을 초과할 경우 연결을 끊고, 다른 UWB 장치와 연결 시도. 기본값 800(cm)
        replacementDistanceThreshold: Float = 800f,

        /// RSSI 신호가 강한 장치부터 연결 시도. 기본값 true.
        isConnectStrongestSignalFirst: Boolean = true,

        onUpdate: (UwbRange) -> Unit,
        onDisconnect: (UwbDisconnect) -> Unit
    ) {
        /// 1초마다 더미 값 리턴하는 함수 생성
        stopUwbRanging(
            onComplete = { result ->
                if (result.isFailure) {
                    println("❌ UWB 작업 중지 실패: ${result.exceptionOrNull()}")
                } else {
                    println("✅ UWB 작업 중지 성공")
                }
            }
        ) // 기존 작업 중지

        uwbJob = uwbScope.launch {
            while (isActive) {
                delay(1000)
                val dummyRange = UwbRange(
                    deviceName = "DummyDevice",
                    distance = (0..100).random().coerceAtLeast(0).toLong(),
                    azimuth = (0..360).random().toLong(),
                    elevation = null,
                    direction = null
                )
                onUpdate(dummyRange)
            }
        }
    }

    fun stopUwbRanging(onComplete: ((Result<Unit>) -> Unit)? = null) {
        try {
            uwbJob?.cancel()
            uwbJob = null
            onComplete?.invoke(Result.success(Unit))
        } catch (e: Exception) {
            onComplete?.invoke(Result.failure(e))
        }
    }
}