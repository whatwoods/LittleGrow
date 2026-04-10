package com.littlegrow.app.ui

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.littlegrow.app.ui.components.ExpressiveTextButton as TextButton
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import kotlinx.coroutines.flow.collect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NativeDatePickerField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    maxDate: LocalDate? = null,
) {
    var showPicker by rememberSaveable { mutableStateOf(false) }
    val initialDate = remember(value, maxDate) {
        val parsedDate = runCatching { LocalDate.parse(value.trim(), dateFormatter) }.getOrNull()
        when {
            parsedDate == null && maxDate != null -> maxDate
            parsedDate == null -> LocalDate.now()
            maxDate != null && parsedDate.isAfter(maxDate) -> maxDate
            else -> parsedDate
        }
    }

    if (showPicker) {
        val maxDateMillis = remember(maxDate) { maxDate?.toUtcStartOfDayMillis() }
        val selectableDates = remember(maxDateMillis, maxDate) {
            if (maxDateMillis == null || maxDate == null) {
                null
            } else {
                object : SelectableDates {
                    override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis <= maxDateMillis

                    override fun isSelectableYear(year: Int): Boolean = year <= maxDate.year
                }
            }
        }
        val datePickerState = if (selectableDates == null) {
            rememberDatePickerState(initialSelectedDateMillis = initialDate.toUtcStartOfDayMillis())
        } else {
            rememberDatePickerState(
                initialSelectedDateMillis = initialDate.toUtcStartOfDayMillis(),
                selectableDates = selectableDates,
            )
        }

        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis
                            ?.let(::utcMillisToLocalDate)
                            ?.let { selectedDate ->
                                val boundedDate = if (maxDate != null && selectedDate.isAfter(maxDate)) {
                                    maxDate
                                } else {
                                    selectedDate
                                }
                                onValueChange(boundedDate.format(dateFormatter))
                            }
                        showPicker = false
                    },
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text("取消")
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }

    PickerTextField(
        value = value,
        label = label,
        modifier = modifier,
        supportingText = supportingText,
        trailingIcon = Icons.Rounded.CalendarMonth,
        onClick = { showPicker = true },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NativeDateTimePickerField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
) {
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var showTimePicker by rememberSaveable { mutableStateOf(false) }
    var pendingDate by remember { mutableStateOf<LocalDate?>(null) }
    val initialDateTime = remember(value) {
        runCatching { LocalDateTime.parse(value.trim(), dateTimeFormatter) }.getOrNull() ?: LocalDateTime.now()
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialDateTime.toLocalDate().toUtcStartOfDayMillis(),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingDate = datePickerState.selectedDateMillis
                            ?.let(::utcMillisToLocalDate)
                            ?: initialDateTime.toLocalDate()
                        showDatePicker = false
                        showTimePicker = true
                    },
                ) {
                    Text("下一步")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("取消")
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = initialDateTime.hour,
            initialMinute = initialDateTime.minute,
            is24Hour = true,
        )
        TimePickerDialog(
            onDismissRequest = {
                showTimePicker = false
                pendingDate = null
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedDate = pendingDate ?: initialDateTime.toLocalDate()
                        val selectedTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                        onValueChange(LocalDateTime.of(selectedDate, selectedTime).format(dateTimeFormatter))
                        showTimePicker = false
                        pendingDate = null
                    },
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showTimePicker = false
                        pendingDate = null
                    },
                ) {
                    Text("取消")
                }
            },
            title = { Text(label, style = MaterialTheme.typography.titleLarge) },
        ) {
            TimePicker(state = timePickerState)
        }
    }

    PickerTextField(
        value = value,
        label = label,
        modifier = modifier,
        supportingText = supportingText,
        trailingIcon = Icons.Rounded.Schedule,
        onClick = { showDatePicker = true },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NativeDurationPickerField(
    valueMinutes: Int?,
    onValueChange: (Int?) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    allowClear: Boolean = false,
    initialMinutesWhenEmpty: Int = 15,
) {
    var showPicker by rememberSaveable { mutableStateOf(false) }
    val initialMinutes = (valueMinutes ?: initialMinutesWhenEmpty).coerceAtLeast(0)

    if (showPicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = (initialMinutes / 60).coerceIn(0, 23),
            initialMinute = (initialMinutes % 60).coerceIn(0, 59),
            is24Hour = true,
        )
        TimePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val totalMinutes = timePickerState.hour * 60 + timePickerState.minute
                        onValueChange(
                            when {
                                totalMinutes > 0 -> totalMinutes
                                allowClear -> null
                                else -> 0
                            },
                        )
                        showPicker = false
                    },
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                Row {
                    if (allowClear) {
                        TextButton(
                            onClick = {
                                onValueChange(null)
                                showPicker = false
                            },
                        ) {
                            Text("清空")
                        }
                    }
                    TextButton(onClick = { showPicker = false }) {
                        Text("取消")
                    }
                }
            },
            title = { Text(label, style = MaterialTheme.typography.titleLarge) },
        ) {
            TimePicker(state = timePickerState)
        }
    }

    PickerTextField(
        value = valueMinutes?.let(::formatDurationMinutes).orEmpty(),
        label = label,
        modifier = modifier,
        supportingText = supportingText,
        trailingIcon = Icons.Rounded.Timer,
        onClick = { showPicker = true },
    )
}

@Composable
private fun PickerTextField(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    trailingIcon: ImageVector,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }

    LaunchedEffect(interactionSource, onClick) {
        interactionSource.interactions.collect { interaction ->
            if (interaction is PressInteraction.Release) {
                onClick()
            }
        }
    }

    OutlinedTextField(
        value = value,
        onValueChange = {},
        modifier = modifier,
        label = { Text(label) },
        supportingText = supportingText?.let { text -> { Text(text) } },
        readOnly = true,
        singleLine = true,
        interactionSource = interactionSource,
        trailingIcon = {
            Icon(
                imageVector = trailingIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        },
        shape = MaterialTheme.shapes.large,
    )
}

private fun LocalDate.toUtcStartOfDayMillis(): Long {
    return atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
}

private fun utcMillisToLocalDate(utcTimeMillis: Long): LocalDate {
    return Instant.ofEpochMilli(utcTimeMillis).atZone(ZoneOffset.UTC).toLocalDate()
}

private fun formatDurationMinutes(minutes: Int): String {
    return Duration.ofMinutes(minutes.toLong()).formatDuration()
}
