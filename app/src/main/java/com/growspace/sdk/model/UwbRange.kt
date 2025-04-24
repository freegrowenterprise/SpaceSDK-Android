/**
 * UwbRange 데이터 클래스는 UWB 장치로부터 측정된 거리 및 방향 정보를 나타내는 클래스입니다.
 * 이 클래스는 장치 이름, 거리, 방위각, 고도각 등의 정보를 포함합니다.
 */
package com.growspace.sdk.model

data class UwbRange(
    // UWB 장치의 이름 또는 식별자
    val deviceName: String,
    
    // UWB 장치까지의 거리 (미터 단위)
    val distance: Float,
    
    // UWB 장치의 방위각 (0-360도)
    val azimuth: Float,
    
    // UWB 장치의 고도각 (null 가능, 0-90도)
    val elevation: Float?,
)