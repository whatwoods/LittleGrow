package com.littlegrow.app.ui

import com.littlegrow.app.data.GrowthMetric
import org.junit.Assert.assertEquals
import org.junit.Test

class FormattersTest {
    @Test
    fun formatMinutes_rendersHoursAndMinutes() {
        assertEquals("2 小时 5 分钟", 125L.formatMinutes())
        assertEquals("45 分钟", 45L.formatMinutes())
    }

    @Test
    fun formatMetric_rendersUnitsByMetric() {
        assertEquals("6.9 kg", 6.9f.formatMetric(GrowthMetric.WEIGHT))
        assertEquals("64.0 cm", 64.0f.formatMetric(GrowthMetric.HEIGHT))
        assertEquals("未记录", (null as Float?).formatMetric(GrowthMetric.HEAD))
    }
}
