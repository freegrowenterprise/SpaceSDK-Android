// RtlsFilterFactory.kt
package com.growspace.sdk.rtls.filter

import com.growspace.sdk.model.RtlsLocation

object RtlsFilterFactory {
    fun create(type: RtlsFilterType): RtlsFilter {
        return when (type) {
            RtlsFilterType.LOW_PASS -> LowPassFilter()
            RtlsFilterType.MOVING_AVERAGE -> MovingAverageFilter()
            RtlsFilterType.NONE -> object : RtlsFilter {
                override fun filter(location: RtlsLocation) = location
            }
        }
    }
}