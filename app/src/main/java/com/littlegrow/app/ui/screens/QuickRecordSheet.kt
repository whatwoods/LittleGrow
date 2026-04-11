package com.littlegrow.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BabyChangingStation
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.ChildCare
import androidx.compose.material.icons.rounded.DirectionsRun
import androidx.compose.material.icons.rounded.LocalDining
import androidx.compose.material.icons.rounded.MedicalServices
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.littlegrow.app.MainViewModel
import com.littlegrow.app.data.FeedingFormDefaults
import com.littlegrow.app.data.RecordTab
import com.littlegrow.app.ui.components.GlassSurface
import com.littlegrow.app.ui.theme.Spacing

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
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp, start = 16.dp, end = 16.dp, top = 4.dp)
        ) {
            if (selectedTab == null) {
                Text(
                    "记录点什么？",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "今天的每一刻都值得被记住",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(Spacing.xl))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                ) {
                    QuickActionCard("喂奶", Icons.Rounded.LocalDining, Modifier.weight(1f)) { selectedTab = RecordTab.FEEDING }
                    QuickActionCard("睡眠", Icons.Rounded.Bedtime, Modifier.weight(1f)) { selectedTab = RecordTab.SLEEP }
                    QuickActionCard("尿布", Icons.Rounded.BabyChangingStation, Modifier.weight(1f)) { selectedTab = RecordTab.DIAPER }
                }
                Spacer(modifier = Modifier.height(Spacing.md))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                ) {
                    QuickActionCard("健康", Icons.Rounded.MedicalServices, Modifier.weight(1f)) { selectedTab = RecordTab.MEDICAL }
                    QuickActionCard("活动", Icons.Rounded.DirectionsRun, Modifier.weight(1f)) { selectedTab = RecordTab.ACTIVITY }
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
private fun QuickActionCard(
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    GlassSurface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        alpha = 0.55f,
        accentColor = MaterialTheme.colorScheme.primary,
        shadowElevation = 8.dp,
    ) {
        androidx.compose.material3.Surface(
            onClick = onClick,
            color = Color.Transparent,
            shape = MaterialTheme.shapes.large,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp, horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f),
                            shape = CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(22.dp),
                    )
                }
                Text(
                    text = label,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}
