/**
 * SpaceUwb 클래스는 UWB(Ultra-Wideband) 통신을 관리하는 메인 클래스입니다.
 * 이 클래스는 UWB 장치의 연결, 거리 측정, 방향 측정 등의 기능을 제공합니다.
 */
package com.growspace.sdk

import android.content.Context
import com.growspace.sdk.bluetooth.BluetoothLEManagerHelper
import com.growspace.sdk.controller.UWBController
import com.growspace.sdk.location.LocationManagerHelper
import com.growspace.sdk.logger.LoggerHelper
import com.growspace.sdk.model.RtlsLocation
import com.growspace.sdk.model.UwbAnchor
import com.growspace.sdk.model.UwbDisconnect
import com.growspace.sdk.model.UwbRange
import com.growspace.sdk.permissions.PermissionHelper
import com.growspace.sdk.rtls.RtlsProcessor
import com.growspace.sdk.rtls.filter.RtlsFilterFactory
import com.growspace.sdk.rtls.filter.RtlsFilterType
import com.growspace.sdk.storage.preferences.PreferenceStorageHelper
import com.growspace.sdk.uwb.UwbManagerHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * SpaceUwb 클래스는 UWB 통신의 핵심 기능을 제공합니다.
 * @param apiKey API 인증에 사용되는 키
 * @param context Android 애플리케이션 컨텍스트
 * @param activity 현재 활성화된 Android 액티비티
 */
class SpaceUwb(
    private val apiKey: String, context: Context, activity: android.app.Activity
) {
    // UWB 작업을 위한 코루틴 스코프 설정
    // IO 디스패처를 사용하여 백그라운드 작업을 처리
    private val uwbScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // 현재 실행 중인 UWB 작업을 추적하는 Job 객체
    private var uwbJob: Job? = null

    private val loggerHelper = LoggerHelper(context)

    // UWB 컨트롤러 인스턴스 생성 및 초기화
    // 필요한 모든 헬퍼 클래스들을 주입하여 초기화
    private val uWBController: UWBController = UWBController(
        PermissionHelper(activity),          // 권한 관리
        PreferenceStorageHelper(context),    // 설정 저장
        loggerHelper,               // 로깅
        BluetoothLEManagerHelper(context),   // 블루투스 LE 관리
        LocationManagerHelper(context),      // 위치 관리
        UwbManagerHelper(context)            // UWB 관리
    )

    // 초기화 블록: UWB 컨트롤러 생성 시 필요한 초기화 작업 수행
    init {
        this.uWBController.onCreate()
    }

    /**
     * UWB 거리 측정을 시작하는 메서드
     * @param maximumConnectionCount 동시에 연결할 수 있는 최대 장치 수 (기본값: 4)
     * @param replacementDistanceThreshold 연결을 끊고 다른 장치와 연결을 시도할 거리 임계값 (기본값: 8m)
     * @param isConnectStrongestSignalFirst RSSI 신호가 강한 장치부터 연결할지 여부 (기본값: true)
     * @param onUpdate 거리 측정 결과를 전달하는 콜백 함수
     * @param onDisconnect 장치 연결이 끊어졌을 때 호출되는 콜백 함수
     */
    fun startUwbRanging(
        ///  장치 최대 연결 개수. 디폴트 값 4.
        maximumConnectionCount: Int = 4,

        /// 최대 연결 거리. 해당 값을 초과할 경우 연결을 끊고, 다른 UWB 장치와 연결 시도. 기본값 8(m)
        replacementDistanceThreshold: Float = 8f,

        /// RSSI 신호가 강한 장치부터 연결 시도. 기본값 true.
        isConnectStrongestSignalFirst: Boolean = true,

        onUpdate: (UwbRange) -> Unit, onDisconnect: (UwbDisconnect) -> Unit
    ) {
        // 기존 UWB 작업을 중지하고 새로운 작업 시작
        stopUwbRanging(
            onComplete = { result ->
                if (result.isFailure) {
                    println("❌ UWB 작업 중지 실패: ${result.exceptionOrNull()}")
                } else {
                    println("✅ UWB 작업 중지 성공")
                }
            })

        // 새로운 UWB 작업 시작
        uwbJob = uwbScope.launch {
            // 작업 시작 전 1초 대기
            delay(1000)
//            val dummyRange = UwbRange(
//                deviceName = "DummyDevice",
//                distance = (0..100).random().coerceAtLeast(0).toLong(),
//                azimuth = (0..360).random().toLong(),
//                elevation = null,
//                direction = null
//            )
//            onUpdate(dummyRange)

            // UWB 컨트롤러를 통해 실제 UWB 작업 시작
            uWBController.onStart(
                maximumConnectionCount,
                replacementDistanceThreshold,
                isConnectStrongestSignalFirst,
                onUpdate,
                onDisconnect
            )
        }

        // UWB 작업 시작
        uwbJob?.start()
    }

    /**
     * UWB 거리 측정을 중지하는 메서드
     * @param onComplete 작업 중지 완료 시 호출되는 콜백 함수 (선택적)
     */
    fun stopUwbRanging(onComplete: ((Result<Unit>) -> Unit)? = null) {
        try {
            // 현재 실행 중인 UWB 작업 취소
            uwbJob?.cancel()
            uwbJob = null

            // UWB 컨트롤러를 통해 작업 중지
            if (uWBController.onStop()) {
                println("✅ UWB 작업 중지 성공")
                onComplete?.invoke(Result.success(Unit))
            } else {
                println("❌ UWB 작업 중지 실패")
                onComplete?.invoke(Result.failure(Exception("UWB 작업 중지 실패")))
            }

        } catch (e: Exception) {
            // 예외 발생 시 실패 결과 전달
            onComplete?.invoke(Result.failure(e))
        }
    }

    fun exportLogsTxt() {
        loggerHelper.exportLogsTxt()
    }

    fun startUwbRtls(
        anchorPositionMap: Map<String, Triple<Double, Double, Double>>,
        zCorrection: Float = 1.0f,
        maximumConnectionCount: Int = 4,
        replacementDistanceThreshold: Float = 8f,
        isConnectStrongestSignalFirst: Boolean = true,
        filterType: RtlsFilterType = RtlsFilterType.NONE,
        onResult: (RtlsLocation) -> Unit,
        onFail: (String) -> Unit,
        onDeviceRanging: (Map<String, Float>) -> Unit = {}
    ) {
        // 최근 거리 측정값과 수신 시간을 저장
        val anchorDistances = mutableMapOf<String, Pair<Float, Long>>()

        val filter = RtlsFilterFactory.create(filterType)

        // 먼저 기존 UWB 작업 중지
        stopUwbRanging {
            uwbScope.launch {
                delay(1000)

                uWBController.onStart(
                    maximumConnectionCount,
                    replacementDistanceThreshold,
                    isConnectStrongestSignalFirst,
                    { range ->
                        val id = range.deviceName
                        val distance = range.distance
                        val timestamp = System.currentTimeMillis()

                        anchorDistances[id] = distance to timestamp

                        val now = System.currentTimeMillis()
                        val anchors = anchorDistances
                            .filter { now - it.value.second <= 1000 }
                            .mapNotNull { (id, pair) ->
                                anchorPositionMap[id]?.let { (x, y, z) ->
                                    UwbAnchor(x = x, y = y, z = z, distance = pair.first)
                                }
                            }
                        val distanceMap: Map<String, Float> = anchorDistances.mapValues { it.value.first }
                        onDeviceRanging(distanceMap)

                        val processor = RtlsProcessor()
                        val result = processor.processAnchors(anchors, zCorrection)
                        val filtered = result?.let { filter.filter(it) }

                        if (result != null) {
                            onResult(filtered ?: result)
                        } else {
                            onFail("위치 계산 실패 (anchor 부족 또는 계산 불가)")
                        }
                    },
                    { disconnect ->
                        anchorDistances.remove(disconnect.deviceName)
                    }
                )
            }
        }
    }
}