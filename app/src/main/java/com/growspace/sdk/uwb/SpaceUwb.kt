package com.growspace.sdk.uwb

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
        maxConnectCount: Int,
        maxDistance: Long,
        onResponse: (Result<UwbRange>) -> Unit
    ) {
        /// 1초마다 더미 값 리턴하는 함수 생성
        stopUwbRanging() // 기존 작업 중지

        uwbJob = uwbScope.launch {
            try {
                while (isActive) {
                    delay(1000)
                    val dummyRange = UwbRange(
                        deviceName = "DummyDevice",
                        distance = (0..100).random().coerceAtLeast(0).toLong(),
                        azimuth = (0..360).random().toLong()
                    )
                    onResponse(Result.success(dummyRange))
                }
            } catch (e: Exception) {
                onResponse(Result.failure(e))
            }
        }
    }

    fun stopUwbRanging() {
        uwbJob?.cancel()
        uwbJob = null
    }
}