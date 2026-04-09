package com.littlegrow.app.export

import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.littlegrow.app.data.ExportSnapshot
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.time.format.DateTimeFormatter

private val exportDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
private val exportDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

fun writeCsvExport(
    outputStream: OutputStream,
    snapshot: ExportSnapshot,
) {
    OutputStreamWriter(outputStream, Charsets.UTF_8).use { writer ->
        // BOM keeps Chinese text readable in spreadsheet apps like Excel/WPS.
        writer.write('\uFEFF'.code)
        writer.write(buildCsv(snapshot))
    }
}

fun buildCsv(snapshot: ExportSnapshot): String {
    return buildString {
        appendLine("LittleGrow 数据导出")
        appendLine("生成时间,${snapshot.generatedAt.format(exportDateTimeFormatter).csvCell()}")
        appendLine()

        appendCsvSection(
            title = "宝宝资料",
            headers = listOf("昵称", "生日", "性别"),
            rows = listOf(
                listOf(
                    snapshot.profile?.name.orEmpty(),
                    snapshot.profile?.birthday?.format(exportDateFormatter).orEmpty(),
                    snapshot.profile?.gender?.label.orEmpty(),
                ),
            ),
        )

        appendCsvSection(
            title = "喂养记录",
            headers = listOf("时间", "类型", "时长(分钟)", "奶量(ml)", "食材", "备注"),
            rows = snapshot.feedings.map { feeding ->
                listOf(
                    feeding.happenedAt.format(exportDateTimeFormatter),
                    feeding.type.label,
                    feeding.durationMinutes?.toString().orEmpty(),
                    feeding.amountMl?.toString().orEmpty(),
                    feeding.foodName.orEmpty(),
                    feeding.note.orEmpty(),
                )
            },
        )

        appendCsvSection(
            title = "睡眠记录",
            headers = listOf("开始时间", "结束时间", "总分钟数", "备注"),
            rows = snapshot.sleeps.map { sleep ->
                listOf(
                    sleep.startTime.format(exportDateTimeFormatter),
                    sleep.endTime.format(exportDateTimeFormatter),
                    java.time.Duration.between(sleep.startTime, sleep.endTime).toMinutes().toString(),
                    sleep.note.orEmpty(),
                )
            },
        )

        appendCsvSection(
            title = "排泄记录",
            headers = listOf("时间", "类型", "颜色", "性状", "备注"),
            rows = snapshot.diapers.map { diaper ->
                listOf(
                    diaper.happenedAt.format(exportDateTimeFormatter),
                    diaper.type.label,
                    diaper.poopColor?.label.orEmpty(),
                    diaper.poopTexture?.label.orEmpty(),
                    diaper.note.orEmpty(),
                )
            },
        )

        appendCsvSection(
            title = "成长记录",
            headers = listOf("日期", "体重(kg)", "身高(cm)", "头围(cm)"),
            rows = snapshot.growthRecords.map { growth ->
                listOf(
                    growth.date.format(exportDateFormatter),
                    growth.weightKg?.toString().orEmpty(),
                    growth.heightCm?.toString().orEmpty(),
                    growth.headCircCm?.toString().orEmpty(),
                )
            },
        )

        appendCsvSection(
            title = "里程碑",
            headers = listOf("日期", "分类", "标题", "备注"),
            rows = snapshot.milestones.map { milestone ->
                listOf(
                    milestone.achievedDate.format(exportDateFormatter),
                    milestone.category.label,
                    milestone.title,
                    milestone.note.orEmpty(),
                )
            },
        )

        appendCsvSection(
            title = "疫苗计划",
            headers = listOf("计划键", "疫苗", "剂次", "建议日期", "状态", "实际日期"),
            rows = snapshot.vaccines.map { vaccine ->
                listOf(
                    vaccine.scheduleKey,
                    vaccine.vaccineName,
                    vaccine.doseNumber.toString(),
                    vaccine.scheduledDate.format(exportDateFormatter),
                    if (vaccine.isDone) "已接种" else "待接种",
                    vaccine.actualDate?.format(exportDateFormatter).orEmpty(),
                )
            },
        )
    }
}

fun writePdfExport(
    outputStream: OutputStream,
    snapshot: ExportSnapshot,
) {
    val document = PdfDocument()
    try {
        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 11f
        }
        val sectionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
        }
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
        }

        val pageWidth = 595
        val pageHeight = 842
        val horizontalMargin = 40f
        val topMargin = 52f
        val bottomMargin = 48f
        val maxTextWidth = pageWidth - horizontalMargin * 2
        val lines = buildPdfLines(snapshot)

        var pageNumber = 1
        var page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
        var canvas = page.canvas
        var y = topMargin

        lines.forEach { line ->
            val paint = when (line.style) {
                PdfLineStyle.Title -> titlePaint
                PdfLineStyle.Section -> sectionPaint
                PdfLineStyle.Body -> bodyPaint
            }
            val spacing = when (line.style) {
                PdfLineStyle.Title -> 18f
                PdfLineStyle.Section -> 12f
                PdfLineStyle.Body -> 6f
            }
            val wrappedLines = wrapText(line.text, paint, maxTextWidth)
            val lineHeight = paint.textSize + 6f
            val requiredHeight = wrappedLines.size * lineHeight + spacing
            if (y + requiredHeight > pageHeight - bottomMargin) {
                document.finishPage(page)
                pageNumber += 1
                page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
                canvas = page.canvas
                y = topMargin
            }
            wrappedLines.forEach { segment ->
                canvas.drawText(segment, horizontalMargin, y, paint)
                y += lineHeight
            }
            y += spacing
        }

        document.finishPage(page)
        document.writeTo(outputStream)
    } finally {
        document.close()
    }
}

private enum class PdfLineStyle {
    Title,
    Section,
    Body,
}

private data class PdfLine(
    val text: String,
    val style: PdfLineStyle,
)

private fun buildPdfLines(snapshot: ExportSnapshot): List<PdfLine> {
    val lines = mutableListOf<PdfLine>()
    lines += PdfLine("LittleGrow 数据导出", PdfLineStyle.Title)
    lines += PdfLine("生成时间：${snapshot.generatedAt.format(exportDateTimeFormatter)}", PdfLineStyle.Body)

    lines += PdfLine("宝宝资料", PdfLineStyle.Section)
    if (snapshot.profile == null) {
        lines += PdfLine("尚未填写宝宝资料。", PdfLineStyle.Body)
    } else {
        lines += PdfLine("昵称：${snapshot.profile.name}", PdfLineStyle.Body)
        lines += PdfLine("生日：${snapshot.profile.birthday.format(exportDateFormatter)}", PdfLineStyle.Body)
        lines += PdfLine("性别：${snapshot.profile.gender.label}", PdfLineStyle.Body)
    }

    lines += PdfLine("喂养记录（${snapshot.feedings.size} 条）", PdfLineStyle.Section)
    if (snapshot.feedings.isEmpty()) {
        lines += PdfLine("暂无喂养记录。", PdfLineStyle.Body)
    } else {
        snapshot.feedings.forEachIndexed { index, feeding ->
            lines += PdfLine(
                "${index + 1}. ${feeding.happenedAt.format(exportDateTimeFormatter)} · ${feeding.type.label}" +
                    feeding.durationMinutes?.let { " · ${it} 分钟" }.orEmpty() +
                    feeding.amountMl?.let { " · ${it} ml" }.orEmpty() +
                    feeding.foodName?.takeIf { it.isNotBlank() }?.let { " · $it" }.orEmpty() +
                    feeding.note?.takeIf { it.isNotBlank() }?.let { " · 备注：$it" }.orEmpty(),
                PdfLineStyle.Body,
            )
        }
    }

    lines += PdfLine("睡眠记录（${snapshot.sleeps.size} 条）", PdfLineStyle.Section)
    if (snapshot.sleeps.isEmpty()) {
        lines += PdfLine("暂无睡眠记录。", PdfLineStyle.Body)
    } else {
        snapshot.sleeps.forEachIndexed { index, sleep ->
            val minutes = java.time.Duration.between(sleep.startTime, sleep.endTime).toMinutes()
            lines += PdfLine(
                "${index + 1}. ${sleep.startTime.format(exportDateTimeFormatter)} - " +
                    "${sleep.endTime.format(exportDateTimeFormatter)} · ${minutes} 分钟" +
                    sleep.note?.takeIf { it.isNotBlank() }?.let { " · 备注：$it" }.orEmpty(),
                PdfLineStyle.Body,
            )
        }
    }

    lines += PdfLine("排泄记录（${snapshot.diapers.size} 条）", PdfLineStyle.Section)
    if (snapshot.diapers.isEmpty()) {
        lines += PdfLine("暂无排泄记录。", PdfLineStyle.Body)
    } else {
        snapshot.diapers.forEachIndexed { index, diaper ->
            lines += PdfLine(
                "${index + 1}. ${diaper.happenedAt.format(exportDateTimeFormatter)} · ${diaper.type.label}" +
                    diaper.poopColor?.let { " · 颜色：${it.label}" }.orEmpty() +
                    diaper.poopTexture?.let { " · 性状：${it.label}" }.orEmpty() +
                    diaper.note?.takeIf { it.isNotBlank() }?.let { " · 备注：$it" }.orEmpty(),
                PdfLineStyle.Body,
            )
        }
    }

    lines += PdfLine("成长记录（${snapshot.growthRecords.size} 条）", PdfLineStyle.Section)
    if (snapshot.growthRecords.isEmpty()) {
        lines += PdfLine("暂无成长记录。", PdfLineStyle.Body)
    } else {
        snapshot.growthRecords.forEachIndexed { index, growth ->
            lines += PdfLine(
                "${index + 1}. ${growth.date.format(exportDateFormatter)} · 体重 ${growth.weightKg ?: "-"} kg" +
                    " · 身高 ${growth.heightCm ?: "-"} cm · 头围 ${growth.headCircCm ?: "-"} cm",
                PdfLineStyle.Body,
            )
        }
    }

    lines += PdfLine("里程碑（${snapshot.milestones.size} 条）", PdfLineStyle.Section)
    if (snapshot.milestones.isEmpty()) {
        lines += PdfLine("暂无里程碑记录。", PdfLineStyle.Body)
    } else {
        snapshot.milestones.forEachIndexed { index, milestone ->
            lines += PdfLine(
                "${index + 1}. ${milestone.achievedDate.format(exportDateFormatter)} · ${milestone.category.label} · " +
                    milestone.title +
                    milestone.note?.takeIf { it.isNotBlank() }?.let { " · 备注：$it" }.orEmpty(),
                PdfLineStyle.Body,
            )
        }
    }

    lines += PdfLine("疫苗计划（${snapshot.vaccines.size} 条）", PdfLineStyle.Section)
    if (snapshot.vaccines.isEmpty()) {
        lines += PdfLine("暂无疫苗计划。", PdfLineStyle.Body)
    } else {
        snapshot.vaccines.forEachIndexed { index, vaccine ->
            lines += PdfLine(
                "${index + 1}. ${vaccine.vaccineName} 第 ${vaccine.doseNumber} 针 · 建议 ${vaccine.scheduledDate.format(exportDateFormatter)}" +
                    if (vaccine.isDone) {
                        " · 已接种" + vaccine.actualDate?.let { "（${it.format(exportDateFormatter)}）" }.orEmpty()
                    } else {
                        " · 待接种"
                    },
                PdfLineStyle.Body,
            )
        }
    }

    return lines
}

private fun wrapText(
    text: String,
    paint: Paint,
    maxWidth: Float,
): List<String> {
    if (text.isEmpty()) return listOf("")
    val segments = mutableListOf<String>()
    var remaining = text
    while (remaining.isNotEmpty()) {
        var count = paint.breakText(remaining, true, maxWidth, null)
        if (count <= 0) count = 1
        segments += remaining.take(count)
        remaining = remaining.drop(count)
    }
    return segments
}

private fun StringBuilder.appendCsvSection(
    title: String,
    headers: List<String>,
    rows: List<List<String>>,
) {
    appendLine(title.csvCell())
    appendLine(headers.joinToString(",") { it.csvCell() })
    if (rows.isEmpty()) {
        appendLine(listOf("暂无数据").joinToString(",") { it.csvCell() })
    } else {
        rows.forEach { row ->
            appendLine(row.joinToString(",") { it.csvCell() })
        }
    }
    appendLine()
}

private fun String.csvCell(): String {
    return "\"${replace("\"", "\"\"")}\""
}
