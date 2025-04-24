/**
 * DisconnectType 열거형은 UWB 장치의 연결이 끊어지는 원인을 정의하는 클래스입니다.
 * 이 클래스는 다양한 연결 해제 시나리오를 구분하여 처리할 수 있도록 합니다.
 */
package com.growspace.sdk.model

enum class DisconnectType {
    /**
     * 장치와의 거리가 설정된 임계값을 초과하여 연결이 끊어진 경우
     * 이는 일반적으로 장치가 통신 범위를 벗어났을 때 발생합니다.
     */
    DISCONNECTED_DUE_TO_DISTANCE,

    /**
     * 시스템적인 문제로 인해 연결이 끊어진 경우
     * 이는 네트워크 오류, 시스템 충돌, 또는 기타 예기치 않은 상황에서 발생할 수 있습니다.
     */
    DISCONNECTED_DUE_TO_SYSTEM,
}
