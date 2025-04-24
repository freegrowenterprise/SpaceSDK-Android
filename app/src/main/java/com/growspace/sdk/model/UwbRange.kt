package com.growspace.sdk.model

data class UwbRange(
    val deviceName: String,
    val distance: Long,
    val azimuth: Long,
    /// iOS는 elevation 값이 나오는데 android는 안나온다면 삭제 해도 됨.
    val elevation: Long?,
    /// iOS는 direction 값이 나오는데 android는 안나온다면 삭제 해도 됨.
    val direction: List<Float>?,
)