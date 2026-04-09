package com.littlegrow.app.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.TextView
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import kotlinx.coroutines.flow.collect

@Composable
fun NativeDatePickerField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    maxDate: LocalDate? = null,
) {
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val currentValue = rememberUpdatedState(value)
    val currentOnValueChange = rememberUpdatedState(onValueChange)
    val currentMaxDate = rememberUpdatedState(maxDate)

    fun showDatePicker() {
        val initialDate = runCatching {
            LocalDate.parse(currentValue.value.trim(), dateFormatter)
        }.getOrNull() ?: currentMaxDate.value ?: LocalDate.now()

        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                currentOnValueChange.value(LocalDate.of(year, month + 1, dayOfMonth).format(dateFormatter))
            },
            initialDate.year,
            initialDate.monthValue - 1,
            initialDate.dayOfMonth,
        ).apply {
            currentMaxDate.value?.let { selectedMaxDate ->
                datePicker.maxDate = selectedMaxDate
                    .atStartOfDay()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()
            }
        }.show()
    }

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            if (interaction is PressInteraction.Release) {
                showDatePicker()
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
    )
}

@Composable
fun NativeDateTimePickerField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
) {
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val currentValue = rememberUpdatedState(value)
    val currentOnValueChange = rememberUpdatedState(onValueChange)

    fun showDateTimePicker() {
        val initialDateTime = runCatching {
            LocalDateTime.parse(currentValue.value.trim(), dateTimeFormatter)
        }.getOrNull() ?: LocalDateTime.now()

        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                TimePickerDialog(
                    context,
                    { _, hourOfDay, minute ->
                        currentOnValueChange.value(
                            LocalDateTime.of(year, month + 1, dayOfMonth, hourOfDay, minute).format(dateTimeFormatter),
                        )
                    },
                    initialDateTime.hour,
                    initialDateTime.minute,
                    true,
                ).show()
            },
            initialDateTime.year,
            initialDateTime.monthValue - 1,
            initialDateTime.dayOfMonth,
        ).show()
    }

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            if (interaction is PressInteraction.Release) {
                showDateTimePicker()
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
    )
}

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
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val currentValueMinutes = rememberUpdatedState(valueMinutes)
    val currentOnValueChange = rememberUpdatedState(onValueChange)

    fun showDurationPicker() {
        val initialMinutes = currentValueMinutes.value ?: initialMinutesWhenEmpty
        val hoursPicker = createDurationPicker(context, maxValue = 23, value = initialMinutes / 60)
        val minutesPicker = createDurationPicker(context, maxValue = 59, value = initialMinutes % 60)
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(48, 24, 48, 8)
            addView(hoursPicker)
            addView(createDurationLabel(context, "小时"))
            addView(minutesPicker)
            addView(createDurationLabel(context, "分钟"))
        }

        android.app.AlertDialog.Builder(context)
            .setTitle(label)
            .setView(layout)
            .setPositiveButton("确定") { _, _ ->
                val totalMinutes = hoursPicker.value * 60 + minutesPicker.value
                currentOnValueChange.value(
                    when {
                        totalMinutes > 0 -> totalMinutes
                        allowClear -> null
                        else -> 0
                    },
                )
            }
            .setNegativeButton("取消", null)
            .apply {
                if (allowClear) {
                    setNeutralButton("清空") { _, _ -> currentOnValueChange.value(null) }
                }
            }
            .show()
    }

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            if (interaction is PressInteraction.Release) {
                showDurationPicker()
            }
        }
    }

    OutlinedTextField(
        value = valueMinutes?.let(::formatDurationMinutes) ?: "",
        onValueChange = {},
        modifier = modifier,
        label = { Text(label) },
        supportingText = supportingText?.let { text -> { Text(text) } },
        readOnly = true,
        singleLine = true,
        interactionSource = interactionSource,
    )
}

private fun createDurationPicker(
    context: Context,
    maxValue: Int,
    value: Int,
): NumberPicker {
    return NumberPicker(context).apply {
        minValue = 0
        this.maxValue = maxValue
        this.value = value.coerceIn(minValue, maxValue)
        setFormatter { "%02d".format(it) }
    }
}

private fun createDurationLabel(
    context: Context,
    text: String,
): TextView {
    return TextView(context).apply {
        this.text = text
        textSize = 16f
        setPadding(16, 0, 24, 0)
    }
}

private fun formatDurationMinutes(minutes: Int): String {
    return Duration.ofMinutes(minutes.toLong()).formatDuration()
}
