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

data class GrowthVelocityRange(
    val min: Float,
    val max: Float,
    val unit: String,
)

object WhoGrowthStandards {
    private val cache = ConcurrentHashMap<String, WhoReferenceSet>()

    fun load(
        context: Context,
        gender: Gender,
        metric: GrowthMetric,
    ): WhoReferenceSet {
        if (metric == GrowthMetric.BMI) {
            val cacheKey = "${gender.name}_BMI"
            return cache.getOrPut(cacheKey) {
                val weightReference = load(context, gender, GrowthMetric.WEIGHT)
                val heightReference = load(context, gender, GrowthMetric.HEIGHT)
                val points = weightReference.points.mapNotNull { weightPoint ->
                    val heightPoint = heightReference.points.find { it.ageDays == weightPoint.ageDays } ?: return@mapNotNull null
                    WhoCurvePoint(
                        ageDays = weightPoint.ageDays,
                        p3 = bmi(weightPoint.p3, heightPoint.p3),
                        p15 = bmi(weightPoint.p15, heightPoint.p15),
                        p50 = bmi(weightPoint.p50, heightPoint.p50),
                        p85 = bmi(weightPoint.p85, heightPoint.p85),
                        p97 = bmi(weightPoint.p97, heightPoint.p97),
                    )
                }
                WhoReferenceSet(points = points)
            }
        }
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
            GrowthMetric.BMI -> "bmi"
        }
        return "${genderKey}_${metricKey}.csv"
    }

    fun monthlyVelocityRange(
        metric: GrowthMetric,
        ageMonths: Int,
    ): GrowthVelocityRange? {
        return when (metric) {
            GrowthMetric.WEIGHT -> when {
                ageMonths < 3 -> GrowthVelocityRange(500f, 1000f, "g/月")
                ageMonths < 6 -> GrowthVelocityRange(350f, 700f, "g/月")
                ageMonths < 12 -> GrowthVelocityRange(150f, 400f, "g/月")
                ageMonths < 24 -> GrowthVelocityRange(80f, 250f, "g/月")
                else -> GrowthVelocityRange(50f, 180f, "g/月")
            }

            GrowthMetric.HEIGHT -> when {
                ageMonths < 3 -> GrowthVelocityRange(2.5f, 4.5f, "cm/月")
                ageMonths < 6 -> GrowthVelocityRange(1.5f, 3.0f, "cm/月")
                ageMonths < 12 -> GrowthVelocityRange(1.0f, 2.0f, "cm/月")
                ageMonths < 24 -> GrowthVelocityRange(0.6f, 1.4f, "cm/月")
                else -> GrowthVelocityRange(0.4f, 1.0f, "cm/月")
            }

            GrowthMetric.HEAD -> when {
                ageMonths < 3 -> GrowthVelocityRange(0.8f, 1.5f, "cm/月")
                ageMonths < 6 -> GrowthVelocityRange(0.4f, 1.0f, "cm/月")
                ageMonths < 12 -> GrowthVelocityRange(0.2f, 0.6f, "cm/月")
                else -> GrowthVelocityRange(0.1f, 0.3f, "cm/月")
            }

            GrowthMetric.BMI -> null
        }
    }

    private fun bmi(
        weightKg: Float,
        heightCm: Float,
    ): Float {
        val meter = heightCm / 100f
        return if (meter <= 0f) 0f else weightKg / (meter * meter)
    }
}
