package com.littlegrow.app.data

import android.content.Context
import java.util.concurrent.ConcurrentHashMap

private val whoPercentiles = listOf(3, 15, 50, 85, 97)

data class WhoCurvePoint(
    val ageDays: Int,
    val p3: Float,
    val p15: Float,
    val p50: Float,
    val p85: Float,
    val p97: Float,
) {
    fun valueFor(percentile: Int): Float {
        return when (percentile) {
            3 -> p3
            15 -> p15
            50 -> p50
            85 -> p85
            97 -> p97
            else -> error("Unsupported percentile: $percentile")
        }
    }
}

data class WhoReferenceSet(
    val points: List<WhoCurvePoint>,
) {
    val percentiles: List<Int> = whoPercentiles

    fun series(percentile: Int): List<Pair<Int, Float>> {
        return points.map { it.ageDays to it.valueFor(percentile) }
    }

    fun percentileBand(
        ageDays: Int,
        value: Float,
    ): String? {
        val thresholds = percentiles.mapNotNull { percentile ->
            interpolatedValue(ageDays, percentile)?.let { percentile to it }
        }
        if (thresholds.size != percentiles.size) return null
        if (value < thresholds.first().second) return "<P${thresholds.first().first}"
        thresholds.zipWithNext().forEach { (lower, upper) ->
            if (value < upper.second) {
                return "P${lower.first}-P${upper.first}"
            }
        }
        return ">P${thresholds.last().first}"
    }

    fun interpolatedValue(
        ageDays: Int,
        percentile: Int,
    ): Float? {
        val lower = points.lastOrNull { it.ageDays <= ageDays } ?: points.firstOrNull()
        val upper = points.firstOrNull { it.ageDays >= ageDays } ?: points.lastOrNull()
        if (lower == null || upper == null) return null
        if (lower.ageDays == upper.ageDays) return lower.valueFor(percentile)
        val progress = (ageDays - lower.ageDays).toFloat() / (upper.ageDays - lower.ageDays).toFloat()
        return lower.valueFor(percentile) + (upper.valueFor(percentile) - lower.valueFor(percentile)) * progress
    }
}

object WhoGrowthStandards {
    private val cache = ConcurrentHashMap<String, WhoReferenceSet>()

    fun load(
        context: Context,
        gender: Gender,
        metric: GrowthMetric,
    ): WhoReferenceSet {
        val assetName = assetName(gender, metric)
        return cache.getOrPut(assetName) {
            context.assets.open("who/$assetName").bufferedReader().useLines { lines ->
                val points = lines
                    .drop(1)
                    .map { line ->
                        val parts = line.split(',')
                        WhoCurvePoint(
                            ageDays = parts[0].toInt(),
                            p3 = parts[1].toFloat(),
                            p15 = parts[2].toFloat(),
                            p50 = parts[3].toFloat(),
                            p85 = parts[4].toFloat(),
                            p97 = parts[5].toFloat(),
                        )
                    }
                    .toList()
                WhoReferenceSet(points = points)
            }
        }
    }

    private fun assetName(
        gender: Gender,
        metric: GrowthMetric,
    ): String {
        val genderKey = when (gender) {
            Gender.BOY -> "boys"
            Gender.GIRL -> "girls"
        }
        val metricKey = when (metric) {
            GrowthMetric.WEIGHT -> "weight"
            GrowthMetric.HEIGHT -> "height"
            GrowthMetric.HEAD -> "head"
        }
        return "${genderKey}_${metricKey}.csv"
    }
}
