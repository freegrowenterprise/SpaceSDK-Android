package com.growspace.sdk.model

enum class DisconnectType {
    /// 장치와 거리가 멀어져서 연결을 끊은 경우.
    DISCONNECTED_DUE_TO_DISTANCE,

    /// 시스템적인 이유로 연결이 끊어진 경우.
    DISCONNECTED_DUE_TO_SYSTEM,
}
