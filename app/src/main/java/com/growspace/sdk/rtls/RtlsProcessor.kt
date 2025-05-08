package com.growspace.sdk.rtls

import com.growspace.sdk.model.UwbAnchor
import com.growspace.sdk.model.RtlsLocation
import kotlin.math.pow
import kotlin.math.sqrt

class RtlsProcessor {

    fun processAnchors(anchors: List<UwbAnchor>, zCorrection: Float): RtlsLocation? {
        val filtered = anchors.filter { it.distance > 0 && it.z != null }

        return when {
            filtered.size < 3 -> null
            filtered.size == 3 -> calculate2DWithZCorrection(filtered, zCorrection)
            else -> leastSquaresEstimateWithZ(filtered, zCorrection)
        }
    }

    private fun calculate2DWithZCorrection(anchors: List<UwbAnchor>, zRef: Float): RtlsLocation? {
        val corrected = anchors.mapNotNull {
            val z = it.z ?: return@mapNotNull null
            val dz2 = (z - zRef).pow(2)
            val r2 = it.distance.pow(2)
            if (r2 <= dz2) return@mapNotNull null  // 방어 코드
            val correctedR = sqrt(r2 - dz2).toFloat()
            it.copy(distance = correctedR)
        }
        return calculate2DLocation(corrected)
    }

    private fun calculate2DLocation(anchors: List<UwbAnchor>): RtlsLocation? {
        val (a1, a2, a3) = anchors.take(3)
        val (x1, y1, r1) = Triple(a1.x, a1.y, a1.distance)
        val (x2, y2, r2) = Triple(a2.x, a2.y, a2.distance)
        val (x3, y3, r3) = Triple(a3.x, a3.y, a3.distance)

        val A = 2 * (x2 - x1)
        val B = 2 * (y2 - y1)
        val C = r1.pow(2) - r2.pow(2) - x1.pow(2) + x2.pow(2) - y1.pow(2) + y2.pow(2)
        val D = 2 * (x3 - x2)
        val E = 2 * (y3 - y2)
        val F = r2.pow(2) - r3.pow(2) - x2.pow(2) + x3.pow(2) - y2.pow(2) + y3.pow(2)

        val denominator = A * E - B * D
        if (denominator == 0.0) return null

        val x = (C * E - B * F) / denominator
        val y = (A * F - C * D) / denominator

        return RtlsLocation(x, y, null)
    }

    private fun leastSquaresEstimateWithZ(anchors: List<UwbAnchor>, zRef: Float): RtlsLocation? {
        val corrected = anchors.mapNotNull {
            val z = it.z ?: return@mapNotNull null
            val dz2 = (z - zRef).pow(2)
            val r2 = it.distance.pow(2)
            if (r2 <= dz2) return@mapNotNull null
            val correctedR = sqrt(r2 - dz2).toFloat()
            it.copy(distance = correctedR)
        }
        return leastSquaresEstimate(corrected)
    }

    private fun leastSquaresEstimate(anchors: List<UwbAnchor>): RtlsLocation? {
        val A = mutableListOf<DoubleArray>()
        val b = mutableListOf<Double>()

        val ref = anchors.first()
        val (x0, y0, r0) = Triple(ref.x, ref.y, ref.distance)

        for (i in 1 until anchors.size) {
            val (xi, yi, ri) = Triple(anchors[i].x, anchors[i].y, anchors[i].distance)
            A.add(doubleArrayOf(2 * (xi - x0), 2 * (yi - y0)))
            b.add(r0.pow(2) - ri.pow(2) - x0.pow(2) + xi.pow(2) - y0.pow(2) + yi.pow(2))
        }

        val ata = Array(2) { DoubleArray(2) }
        val atb = DoubleArray(2)

        for (i in A.indices) {
            for (j in 0..1) {
                for (k in 0..1) ata[j][k] += A[i][j] * A[i][k]
                atb[j] += A[i][j] * b[i]
            }
        }

        val det = ata[0][0] * ata[1][1] - ata[0][1] * ata[1][0]
        if (det == 0.0) return null

        val inv = arrayOf(
            doubleArrayOf(ata[1][1] / det, -ata[0][1] / det),
            doubleArrayOf(-ata[1][0] / det, ata[0][0] / det)
        )

        val x = inv[0][0] * atb[0] + inv[0][1] * atb[1]
        val y = inv[1][0] * atb[0] + inv[1][1] * atb[1]

        return RtlsLocation(x, y, null)
    }
}