package com.growspace.sdk.rtls.filter

import com.growspace.sdk.model.RtlsLocation
import java.util.LinkedList

class MovingAverageFilter(private val windowSize: Int = 10) : RtlsFilter {

    private val xWindow = LinkedList<Double>()
    private val yWindow = LinkedList<Double>()

    override fun filter(location: RtlsLocation): RtlsLocation {
        xWindow.add(location.x)
        yWindow.add(location.y)

        if (xWindow.size > windowSize) xWindow.removeFirst()
        if (yWindow.size > windowSize) yWindow.removeFirst()

        val avgX = xWindow.average()
        val avgY = yWindow.average()

        return RtlsLocation(x = avgX, y = avgY, z = location.z)
    }
}