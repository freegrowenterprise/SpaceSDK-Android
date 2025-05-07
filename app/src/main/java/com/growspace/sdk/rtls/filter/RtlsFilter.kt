package com.growspace.sdk.rtls.filter

import com.growspace.sdk.model.RtlsLocation

interface RtlsFilter {
    fun filter(location: RtlsLocation): RtlsLocation
}