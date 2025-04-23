package com.growspace.sdk.model

data class UwbRange(
    val deviceName: String,
    val distance: Long,
    val azimuth: Long,
)