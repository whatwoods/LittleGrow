package com.littlegrow.app.data

object MonthlyGuide {
    private val guides = (0..24).associateWith { month ->
        MonthlyGuideEntry(
            month = month,
            title = "${month} 月龄指南",
            developmentHighlights = when {
                month < 3 -> listOf("开始建立昼夜节律", "对声音和面孔更敏感")
                month < 6 -> listOf("翻身和抓握更活跃", "开始关注辅食准备信号")
                month < 12 -> listOf("辅食占比逐步提升", "爬行、扶站更常见")
                else -> listOf("动作与表达持续增长", "饮食和睡眠习惯逐步稳定")
            },
            feedingTips = when {
                month < 6 -> listOf("以奶为主，按需喂养。")
                month < 12 -> listOf("循序添加辅食，单一食材先观察 3 天。")
                else -> listOf("向家庭饮食过渡，注意铁和蛋白质。")
            },
            sleepTips = when {
                month < 6 -> listOf("夜间尽量维持低刺激环境。")
                month < 12 -> listOf("固定睡前流程，帮助形成入睡联想。")
                else -> listOf("午睡和夜睡时间尽量固定。")
            },
            careTips = listOf("留意疫苗计划。", "适量户外和互动。", "异常情况及时就医。"),
        )
    }

    fun guideFor(month: Int): MonthlyGuideEntry? = guides[month.coerceIn(0, 24)]
}
