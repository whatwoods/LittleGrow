package com.littlegrow.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.littlegrow.app.MainViewModel
import com.littlegrow.app.data.FeedingFormDefaults
import com.littlegrow.app.data.RecordTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickRecordSheet(
    viewModel: MainViewModel,
    feedingFormDefaults: FeedingFormDefaults,
    caregivers: List<String>,
    currentCaregiver: String,
    initialSelectedTab: RecordTab? = null,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedTab by rememberSaveable { mutableStateOf<RecordTab?>(initialSelectedTab) }
    var dismissDraft by remember { mutableStateOf<(() -> Unit)?>(null) }

    LaunchedEffect(selectedTab) {
        if (selectedTab != RecordTab.FEEDING) {
            dismissDraft = null
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            dismissDraft?.invoke()
            onDismiss()
        },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp, start = 16.dp, end = 16.dp, top = 8.dp)
        ) {
            if (selectedTab == null) {
                Text("记录点什么？", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(24.dp))
                // We can use a grid or row for quick actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickActionCard("记喂奶", Modifier.weight(1f)) { selectedTab = RecordTab.FEEDING }
                    QuickActionCard("记睡眠", Modifier.weight(1f)) { selectedTab = RecordTab.SLEEP }
                    QuickActionCard("记尿布", Modifier.weight(1f)) { selectedTab = RecordTab.DIAPER }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickActionCard("健康记录", Modifier.weight(1f)) { selectedTab = RecordTab.MEDICAL }
                    QuickActionCard("活动记录", Modifier.weight(1f)) { selectedTab = RecordTab.ACTIVITY }
                }
            } else {
                val currentTab = requireNotNull(selectedTab)
                Text(
                    text = currentTab.label,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(16.dp))
                when (currentTab) {
                    RecordTab.FEEDING -> AddFeedingForm(
                        initial = null,
                        defaults = feedingFormDefaults,
                        caregivers = caregivers,
                        currentCaregiver = currentCaregiver,
                        onSubmit = { draft ->
                            viewModel.addFeeding(draft)
                            onDismiss()
                        },
                        onCancel = { selectedTab = null },
                        bindDiscard = { dismissDraft = it },
                    )
                    RecordTab.SLEEP -> AddSleepForm(
                        initial = null,
                        caregivers = caregivers,
                        currentCaregiver = currentCaregiver,
                        onSubmit = { draft ->
                            viewModel.addSleep(draft)
                            onDismiss()
                        },
                        onCancel = { selectedTab = null },
                    )
                    RecordTab.DIAPER -> AddDiaperForm(
                        initial = null,
                        caregivers = caregivers,
                        currentCaregiver = currentCaregiver,
                        onSubmit = { draft ->
                            viewModel.addDiaper(draft)
                            onDismiss()
                        },
                        onCancel = { selectedTab = null },
                    )
                    RecordTab.MEDICAL -> AddMedicalForm(
                        initial = null,
                        caregivers = caregivers,
                        currentCaregiver = currentCaregiver,
                        onSubmit = { draft ->
                            viewModel.addMedical(draft)
                            onDismiss()
                        },
                        onCancel = { selectedTab = null },
                    )
                    RecordTab.ACTIVITY -> AddActivityForm(
                        initial = null,
                        caregivers = caregivers,
                        currentCaregiver = currentCaregiver,
                        onSubmit = { draft ->
                            viewModel.addActivity(draft)
                            onDismiss()
                        },
                        onCancel = { selectedTab = null },
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickActionCard(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    androidx.compose.material3.Surface(
        modifier = modifier,
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(vertical = 16.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}
