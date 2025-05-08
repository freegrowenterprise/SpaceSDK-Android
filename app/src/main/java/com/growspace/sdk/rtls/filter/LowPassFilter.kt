package com.growspace.sdk.rtls.filter

import com.growspace.sdk.model.RtlsLocation

class LowPassFilter(private val alpha: Float = 0.2f) : RtlsFilter {

    private var lastX: Double? = null
    private var lastY: Double? = null

    override fun filter(location: RtlsLocation): RtlsLocation {
        val filteredX = lastX?.let { it + alpha * (location.x - it) } ?: location.x
        val filteredY = lastY?.let { it + alpha * (location.y - it) } ?: location.y

        lastX = filteredX
        lastY = filteredY

        return RtlsLocation(x = filteredX, y = filteredY, z = location.z)
    }
}