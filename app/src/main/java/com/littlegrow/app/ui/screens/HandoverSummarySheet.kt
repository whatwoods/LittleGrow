package com.littlegrow.app.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.littlegrow.app.data.HandoverSummary
import com.littlegrow.app.ui.NativeDateTimePickerField
import com.littlegrow.app.ui.components.ExpressiveOutlinedButton as OutlinedButton
import com.littlegrow.app.ui.components.ExpressiveTextButton as TextButton
import com.littlegrow.app.ui.dateTimeFormatter
import com.littlegrow.app.ui.toLocalDateTimeOrNull
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HandoverSummarySheet(
    summary: HandoverSummary?,
    onGenerate: (LocalDateTime) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var fromText by rememberSaveable {
        mutableStateOf(LocalDateTime.now().minusHours(8).format(dateTimeFormatter))
    }
    var errorText by rememberSaveable { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = {
            onClear()
            onDismiss()
        },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text("交接摘要", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                "选择你离开的起点时间，系统会把这段时间内的喂养、睡眠、排泄和健康变化汇总出来。",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            NativeDateTimePickerField(
                value = fromText,
                onValueChange = { fromText = it },
                modifier = Modifier.fillMaxWidth(),
                label = "交接起点",
                supportingText = "点击选择日期和时间",
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        val from = fromText.toLocalDateTimeOrNull()
                        if (from == null) {
                            errorText = "请选择交接起点。"
                        } else {
                            errorText = null
                            onGenerate(from)
                        }
                    },
                ) {
                    Text("生成摘要")
                }
                TextButton(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        onClear()
                        onDismiss()
                    },
                ) {
                    Text("关闭")
                }
            }
            errorText?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }
            summary?.let { handover ->
                val summaryText = handover.lines.joinToString(separator = "\n")
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    handover.lines.forEach { line ->
                        Text(line)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedButton(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                clipboardManager.setText(AnnotatedString(summaryText))
                            },
                        ) {
                            Text("复制")
                        }
                        OutlinedButton(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                context.startActivity(
                                    Intent.createChooser(
                                        Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, summaryText)
                                        },
                                        "分享交接摘要",
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
    }
}
