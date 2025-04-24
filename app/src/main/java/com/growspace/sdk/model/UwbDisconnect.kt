/**
 * UwbDisconnect 데이터 클래스는 UWB 장치의 연결 해제 정보를 나타내는 클래스입니다.
 * 이 클래스는 연결이 끊어진 장치의 이름과 연결 해제 유형을 포함합니다.
 */
package com.growspace.sdk.model

data class UwbDisconnect(
    // 연결 해제의 원인을 나타내는 열거형 값
    val disConnectType: DisconnectType,
    
    // 연결이 끊어진 장치의 이름 또는 식별자
    val deviceName: String,
)
