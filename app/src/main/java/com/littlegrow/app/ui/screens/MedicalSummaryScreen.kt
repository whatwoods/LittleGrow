package com.littlegrow.app.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.littlegrow.app.data.MedicalSummary
import com.littlegrow.app.ui.components.ExpressiveAssistChip as AssistChip
import com.littlegrow.app.ui.components.ExpressiveOutlinedButton as OutlinedButton

@Composable
fun MedicalSummaryScreen(
    summary: MedicalSummary?,
    contentPadding: PaddingValues,
    onGenerate: (Long) -> Unit,
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        if (summary == null) {
            onGenerate(30)
        }
    }

    LazyColumn(
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = contentPadding.calculateTopPadding() + 16.dp,
            bottom = contentPadding.calculateBottomPadding() + 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            ElevatedCard {
                androidx.compose.foundation.layout.Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text("就医摘要", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(
                        "按最近 7 / 14 / 30 天汇总关键照护信息，方便问诊时快速补充背景。",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(7L, 14L, 30L).forEach { days ->
                            AssistChip(
                                onClick = { onGenerate(days) },
                                label = { Text("最近 $days 天") },
                            )
                        }
                    }
                }
            }
        }

        summary?.let { medicalSummary ->
            item {
                ElevatedCard {
                    androidx.compose.foundation.layout.Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(medicalSummary.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            OutlinedButton(
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(medicalSummary.lines.joinToString("\n")))
                                },
                            ) {
                                Text("复制文本")
                            }
                            OutlinedButton(
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    context.startActivity(
                                        Intent.createChooser(
                                            Intent(Intent.ACTION_SEND).apply {
                                                type = "text/plain"
                                                putExtra(Intent.EXTRA_TEXT, medicalSummary.lines.joinToString("\n"))
                                            },
                                            "分享就医摘要",
                                        ),
                                    )
                                },
                            ) {
                                Text("分享")
                            }
                        }
                    }
                }
            }
            items(medicalSummary.lines) { line ->
                ElevatedCard {
                    Text(
                        text = line,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
        } ?: item {
            EmptyRecordCard("暂无就医摘要。")
        }
    }
}
