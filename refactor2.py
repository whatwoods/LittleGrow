import re

with open('app/src/main/java/com/littlegrow/app/ui/screens/TimelineScreen.kt', 'r', encoding='utf-8') as f:
    timeline_content = f.read()

carousel_code = """
            item {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 24.dp)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("精彩瞬间", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text("查看全部", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    val highlightMilestones = milestones.filter { it.photoPath != null }.take(5)
                    if (highlightMilestones.isNotEmpty()) {
                        androidx.compose.foundation.lazy.LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(highlightMilestones, key = { "hl_${it.id}" }) { milestone ->
                                Box(
                                    modifier = Modifier
                                        .width(200.dp)
                                        .aspectRatio(4f / 5f)
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable { editingMilestone = milestone; showDialog = true }
                                ) {
                                    PhotoPreviewCard(
                                        filePath = milestone.photoPath!!,
                                        contentDescription = milestone.title,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Brush.verticalGradient(
                                                colors = listOf(Color.Transparent, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)),
                                                startY = 100f
                                            ))
                                    )
                                    Column(
                                        modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)
                                    ) {
                                        Text(milestone.achievedDate.formatDate(), color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.labelSmall)
                                        Text(milestone.title, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    } else {
                        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(100.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                            Text("还没有带照片的里程碑哦", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            if (milestones.isEmpty()) {"""

timeline_content = timeline_content.replace('            if (milestones.isEmpty()) {', carousel_code, 1)

new_timeline_row = """@Composable
private fun TimelineRow(
    isFirst: Boolean,
    isLast: Boolean,
    category: MilestoneCategory,
    content: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                val nodeY = 28.dp.toPx()
                val lineX = TimelineLineOffset.toPx()
                val strokeW = 4.dp.toPx()
                
                if (!isLast) {
                    drawLine(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF825600).copy(alpha = 0.2f),
                                Color(0xFF825600).copy(alpha = 0.05f),
                                Color.Transparent
                            )
                        ),
                        start = androidx.compose.ui.geometry.Offset(lineX, nodeY),
                        end = androidx.compose.ui.geometry.Offset(lineX, size.height),
                        strokeWidth = strokeW,
                    )
                }
            }
            .padding(bottom = if (isLast) 0.dp else 32.dp)
    ) {
        val nodeBg = if (isFirst) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        val borderColor = MaterialTheme.colorScheme.surface
        Box(
            modifier = Modifier
                .padding(start = TimelineLineOffset - 10.dp, top = 18.dp)
                .size(20.dp)
                .background(nodeBg, CircleShape)
                .border(4.dp, borderColor, CircleShape)
                .shadow(if (isFirst) 8.dp else 0.dp, CircleShape, spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
        )
        Box(modifier = Modifier.padding(start = TimelineContentStart - TimelineLineOffset - 10.dp, end = 16.dp, top = 8.dp)) {
            content()
        }
    }
}"""

timeline_content = re.sub(r'@Composable\s+private fun TimelineRow\(.*?Box\(modifier = Modifier\.padding\(start = TimelineContentStart - TimelineLineOffset - 14\.dp, end = 16\.dp\)\) \{\s+content\(\)\s+\}\s+\}\s+\}', new_timeline_row, timeline_content, flags=re.DOTALL)

new_timeline_card = """                        TimelineRow(
                            isFirst = index == 0,
                            isLast = index == totalMilestones - 1,
                            category = milestone.category,
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(4.dp, RoundedCornerShape(16.dp), spotColor = Color(0xFF825600).copy(alpha = 0.05f))
                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f), RoundedCornerShape(16.dp))
                                    .clip(RoundedCornerShape(16.dp))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = 32.dp, y = (-32).dp)
                                        .size(100.dp)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f), CircleShape)
                                )
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                ) {
                                    val dateStr = "${milestone.achievedDate.formatDate()} · ${milestone.category.label}"
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            dateStr,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                        )
                                        profile?.birthday?.let { birthday ->
                                            val day = ChronoUnit.DAYS.between(birthday, milestone.achievedDate) + 1
                                            Text("第${day}天", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                    
                                    milestone.photoPath?.let {
                                        Box(
                                            modifier = Modifier
                                                .padding(top = 12.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                        ) {
                                            PhotoPreviewCard(
                                                filePath = it,
                                                contentDescription = milestone.title,
                                                modifier = Modifier.fillMaxWidth().aspectRatio(16f/9f),
                                            )
                                        }
                                    }
                                    
                                    Text(
                                        milestone.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(top = 12.dp)
                                    )

                                    milestone.note?.let {
                                        Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                                        horizontalArrangement = Arrangement.End,
                                    ) {
                                        TextButton(onClick = {
                                            editingMilestone = milestone
                                            showDialog = true
                                        }) { Text("编辑") }
                                        TextButton(onClick = { onDeleteMilestone(milestone.id) }) {
                                            Text("删除", color = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }
                        }"""

timeline_content = re.sub(r'TimelineRow\([\s\S]*?\{[\s\S]*?GlassSurface\([\s\S]*?\}\s*\}\s*\}\s*\}', new_timeline_card, timeline_content)

timeline_content = 'import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.border
' + timeline_content

with open('app/src/main/java/com/littlegrow/app/ui/screens/TimelineScreen.kt', 'w', encoding='utf-8') as f:
    f.write(timeline_content)


with open('app/src/main/java/com/littlegrow/app/ui/screens/RecordsScreen.kt', 'r', encoding='utf-8') as f:
    records_content = f.read()

hero_bento_code = """            HeroSection()
            BentoTabGrid(selectedTab, orderedTabs, onSelectTab)"""

records_content = re.sub(r'ScrollableTabRow.*?\}
\s*\}', hero_bento_code, records_content, flags=re.DOTALL)

additional_code = """
@Composable
fun HeroSection() {
    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp)) {
        Box(modifier = Modifier
            .offset(x = (-20).dp, y = (-20).dp)
            .size(120.dp)
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f), androidx.compose.foundation.shape.CircleShape)
        )
        Box(modifier = Modifier
            .align(Alignment.TopEnd)
            .offset(x = 20.dp, y = (-10).dp)
            .size(80.dp)
            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f), androidx.compose.foundation.shape.CircleShape)
        )
        Column {
            Text("今天记录", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold)
            Text("宝宝的成长每一步都值得珍藏", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun BentoTabGrid(selectedTab: RecordTab, orderedTabs: List<RecordTab>, onSelectTab: (RecordTab) -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            BentoCard(
                modifier = Modifier.weight(1f),
                title = "喂奶",
                icon = androidx.compose.material.icons.Icons.Rounded.Restaurant,
                iconColor = Color(0xFFC2410C),
                iconBgColor = Color(0xFFFFEDD5),
                bgColor = Color.White.copy(alpha = 0.7f),
                isSelected = selectedTab == RecordTab.FEEDING,
                onClick = { orderedTabs.find { it == RecordTab.FEEDING }?.let(onSelectTab) }
            )
            BentoCard(
                modifier = Modifier.weight(1f),
                title = "纸尿裤",
                icon = androidx.compose.material.icons.Icons.Rounded.WaterDrop,
                iconColor = MaterialTheme.colorScheme.secondary,
                iconBgColor = MaterialTheme.colorScheme.secondaryContainer,
                bgColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                isSelected = selectedTab == RecordTab.DIAPER,
                onClick = { orderedTabs.find { it == RecordTab.DIAPER }?.let(onSelectTab) }
            )
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            BentoCard(
                modifier = Modifier.weight(1f),
                title = "睡眠",
                icon = androidx.compose.material.icons.Icons.Rounded.Bedtime,
                iconColor = MaterialTheme.colorScheme.tertiary,
                iconBgColor = MaterialTheme.colorScheme.tertiaryContainer,
                bgColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.2f),
                isSelected = selectedTab == RecordTab.SLEEP,
                onClick = { orderedTabs.find { it == RecordTab.SLEEP }?.let(onSelectTab) }
            )
            BentoCard(
                modifier = Modifier.weight(1f),
                title = "成长",
                icon = androidx.compose.material.icons.Icons.Rounded.Straighten,
                iconColor = MaterialTheme.colorScheme.primary,
                iconBgColor = MaterialTheme.colorScheme.primaryContainer,
                bgColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                isSelected = selectedTab == RecordTab.ACTIVITY || selectedTab == RecordTab.MEDICAL,
                onClick = { orderedTabs.find { it == RecordTab.ACTIVITY }?.let(onSelectTab) ?: orderedTabs.find { it == RecordTab.MEDICAL }?.let(onSelectTab) }
            )
        }
    }
}

@Composable
fun BentoCard(
    modifier: Modifier,
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    iconBgColor: Color,
    bgColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.height(110.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        color = bgColor,
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, iconColor.copy(alpha = 0.5f)) else androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
        shadowElevation = 2.dp,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(56.dp).background(iconBgColor, androidx.compose.foundation.shape.CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(32.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun ActivityListItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    iconBgColor: Color,
    borderColor: Color,
    title: String,
    time: String,
    description: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    content: (@Composable () -> Unit)? = null
) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp).shadow(elevation = 4.dp, shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp), spotColor = Color.Black.copy(alpha = 0.02f)),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    drawRect(
                        color = borderColor,
                        topLeft = androidx.compose.ui.geometry.Offset(0f, 0f),
                        size = androidx.compose.ui.geometry.Size(4.dp.toPx(), size.height)
                    )
                }
                .clickable { onEdit() }
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp).background(iconBgColor, androidx.compose.foundation.shape.CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                    Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text(time, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (description.isNotEmpty()) {
                    Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
                }
                if (content != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    content()
                }
            }
        }
    }
}
"""
records_content += additional_code

feeding_item = """            items(items, key = { it.id }) { feeding ->
                val detail = buildList {
                    feeding.durationMinutes?.let { add("${it} 分钟") }
                    feeding.amountMl?.let { add("${it} ml") }
                    feeding.foodName?.let { add(it) }
                    feeding.caregiver?.let { add("记录人 $it") }
                    feeding.note?.let { add(it) }
                }.joinToString(" · ")
                ActivityListItem(
                    icon = androidx.compose.material.icons.Icons.Rounded.Restaurant,
                    iconColor = Color(0xFFEA580C),
                    iconBgColor = Color(0xFFFFF7ED),
                    borderColor = Color(0xFFFB923C),
                    title = feeding.type.label,
                    time = feeding.happenedAt.formatDateTime(),
                    description = detail,
                    onEdit = { onEdit(feeding) },
                    onDelete = { onDelete(feeding.id) }
                ) {
                    if (feeding.allergyObservation != AllergyStatus.NONE) {
                        Surface(color = MaterialTheme.colorScheme.errorContainer, shape = MaterialTheme.shapes.small) {
                            Text(
                                text = buildString {
                                    append("辅食观察：${feeding.allergyObservation.label}")
                                    feeding.observationEndDate?.let { append("，到 ${it.formatDate()}") }
                                },
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                    feeding.photoPath?.let { 
                        Spacer(Modifier.height(8.dp))
                        PhotoPreviewCard(filePath = it, contentDescription = "辅食照片") 
                    }
                }
            }"""
records_content = re.sub(r'items\(items, key = \{ it\.id \}\) \{ feeding ->\s+ElevatedCard \{[\s\S]*?\}\s+\}', feeding_item, records_content)

sleep_item = """            items(items, key = { it.id }) { sleep ->
                val detail = listOfNotNull(
                    "时长 ${java.time.Duration.between(sleep.startTime, sleep.endTime).toMinutes()} 分钟",
                    sleep.caregiver?.let { "记录人 $it" },
                    sleep.note,
                ).joinToString(" · ")
                ActivityListItem(
                    icon = androidx.compose.material.icons.Icons.Rounded.Bedtime,
                    iconColor = MaterialTheme.colorScheme.tertiary,
                    iconBgColor = MaterialTheme.colorScheme.tertiaryContainer,
                    borderColor = MaterialTheme.colorScheme.tertiary,
                    title = "${sleep.sleepType.label} · ${sleep.fallingAsleepMethod?.label ?: "未记录入睡方式"}",
                    time = "${sleep.startTime.formatDateTime()} - ${sleep.endTime.formatDateTime()}",
                    description = detail,
                    onEdit = { onEdit(sleep) },
                    onDelete = { onDelete(sleep.id) }
                )
            }"""
records_content = re.sub(r'items\(items, key = \{ it\.id \}\) \{ sleep ->\s+ElevatedCard \{[\s\S]*?\}\s+\}', sleep_item, records_content)

diaper_item = """            items(items, key = { it.id }) { diaper ->
                val detail = buildList {
                    diaper.poopColor?.let { add(it.label) }
                    diaper.poopTexture?.let { add(it.label) }
                    diaper.caregiver?.let { add("记录人 $it") }
                    diaper.note?.let { add(it) }
                }.joinToString(" · ")
                ActivityListItem(
                    icon = androidx.compose.material.icons.Icons.Rounded.WaterDrop,
                    iconColor = MaterialTheme.colorScheme.secondary,
                    iconBgColor = MaterialTheme.colorScheme.secondaryContainer,
                    borderColor = MaterialTheme.colorScheme.secondary,
                    title = diaper.type.label,
                    time = diaper.happenedAt.formatDateTime(),
                    description = detail,
                    onEdit = { onEdit(diaper) },
                    onDelete = { onDelete(diaper.id) }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        diaper.photoPath?.let { PhotoPreviewCard(filePath = it, contentDescription = "大便照片") }
                        if (diaper.poopColor == PoopColor.RED || diaper.poopColor == PoopColor.WHITE) {
                            Text("异常颜色提醒：建议结合宝宝状态尽快观察或咨询医生。", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }"""
records_content = re.sub(r'items\(items, key = \{ it\.id \}\) \{ diaper ->\s+ElevatedCard \{[\s\S]*?\}\s+\}', diaper_item, records_content)

medical_item = """            items(items, key = { it.id }) { medical ->
                val detail = buildList {
                    medical.temperatureC?.let { add(String.format("%.1f ℃", it)) }
                    medical.dosage?.let { add("剂量 $it") }
                    medical.caregiver?.let { add("记录人 $it") }
                    medical.note?.let { add(it) }
                }.joinToString(" · ")
                ActivityListItem(
                    icon = androidx.compose.material.icons.Icons.Rounded.MedicalServices,
                    iconColor = MaterialTheme.colorScheme.error,
                    iconBgColor = MaterialTheme.colorScheme.errorContainer,
                    borderColor = MaterialTheme.colorScheme.error,
                    title = "${medical.type.label} · ${medical.title}",
                    time = medical.happenedAt.formatDateTime(),
                    description = detail,
                    onEdit = { onEdit(medical) },
                    onDelete = { onDelete(medical.id) }
                ) {
                    if ((medical.temperatureC ?: 0f) >= 38.0f) {
                        Text("体温偏高，请结合精神状态和持续时间继续观察。", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }"""
records_content = re.sub(r'items\(items, key = \{ it\.id \}\) \{ medical ->\s+ElevatedCard \{[\s\S]*?\}\s+\}', medical_item, records_content)

activity_item = """            items(items, key = { it.id }) { activity ->
                val detail = buildList {
                    activity.durationMinutes?.let { add("${it} 分钟") }
                    activity.caregiver?.let { add("记录人 $it") }
                    activity.note?.let { add(it) }
                }.joinToString(" · ")
                ActivityListItem(
                    icon = androidx.compose.material.icons.Icons.Rounded.Straighten,
                    iconColor = MaterialTheme.colorScheme.primary,
                    iconBgColor = MaterialTheme.colorScheme.primaryContainer,
                    borderColor = MaterialTheme.colorScheme.primary,
                    title = activity.type.label,
                    time = activity.happenedAt.formatDateTime(),
                    description = detail,
                    onEdit = { onEdit(activity) },
                    onDelete = { onDelete(activity.id) }
                )
            }"""
records_content = re.sub(r'items\(items, key = \{ it\.id \}\) \{ activity ->\s+ElevatedCard \{[\s\S]*?\}\s+\}', activity_item, records_content)

records_content = 'import androidx.compose.foundation.background
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.rounded.*
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.BorderStroke
' + records_content

with open('app/src/main/java/com/littlegrow/app/ui/screens/RecordsScreen.kt', 'w', encoding='utf-8') as f:
    f.write(records_content)
