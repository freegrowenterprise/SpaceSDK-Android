package com.growspace.sdk.model

data class UwbRange(
    val deviceName: String,
    val distance: Float,
    val azimuth: Float,
    val elevation: Float?,
)