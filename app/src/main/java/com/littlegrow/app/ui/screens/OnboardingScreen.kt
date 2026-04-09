package com.littlegrow.app.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.littlegrow.app.data.BabyProfile
import com.littlegrow.app.data.Gender
import com.littlegrow.app.ui.dateFormatter
import com.littlegrow.app.ui.NativeDatePickerField
import kotlinx.coroutines.launch
import java.time.LocalDate

private data class OnboardingPage(
    val emoji: String,
    val title: String,
    val description: String,
)

private val introPages = listOf(
    OnboardingPage(
        emoji = "\uD83C\uDF31",
        title = "欢迎来到长呀长",
        description = "记录宝宝成长的每一个瞬间\n喂养、睡眠、换尿布，一切尽在掌握",
    ),
    OnboardingPage(
        emoji = "\uD83D\uDCC8",
        title = "科学追踪成长",
        description = "WHO 标准生长曲线\n直观了解宝宝的身高、体重发育情况",
    ),
    OnboardingPage(
        emoji = "\uD83D\uDC76",
        title = "里程碑与时光轴",
        description = "记录第一次翻身、第一声叫妈妈\n珍藏每一个成长里程碑",
    ),
)

private const val TOTAL_PAGES = 4 // 3 intro + 1 profile setup
private const val PROFILE_PAGE_INDEX = 3

@Composable
fun OnboardingScreen(
    onComplete: (BabyProfile) -> Unit,
) {
    val pagerState = rememberPagerState(pageCount = { TOTAL_PAGES })
    val scope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == TOTAL_PAGES - 1

    var name by rememberSaveable { mutableStateOf("") }
    var birthday by rememberSaveable { mutableStateOf(LocalDate.now().format(dateFormatter)) }
    var gender by rememberSaveable { mutableStateOf(Gender.BOY) }
    var errorText by rememberSaveable { mutableStateOf<String?>(null) }

    fun tryComplete() {
        val trimmedName = name.trim()
        val parsedBirthday = runCatching {
            LocalDate.parse(birthday.trim(), dateFormatter)
        }.getOrNull()

        if (trimmedName.isBlank()) {
            errorText = "请输入宝宝昵称"
            return
        }
        if (birthday.isBlank() || parsedBirthday == null) {
            errorText = "请选择生日"
            return
        }
        errorText = null
        onComplete(
            BabyProfile(
                name = trimmedName,
                birthday = parsedBirthday,
                gender = gender,
            ),
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        // Skip button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.End,
        ) {
            if (!isLastPage) {
                TextButton(onClick = {
                    scope.launch { pagerState.animateScrollToPage(PROFILE_PAGE_INDEX) }
                }) {
                    Text("跳过")
                }
            }
        }

        // Pager content
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
        ) { page ->
            if (page < introPages.size) {
                IntroPageContent(introPages[page])
            } else {
                ProfileSetupPage(
                    name = name,
                    birthday = birthday,
                    gender = gender,
                    errorText = errorText,
                    onNameChange = { name = it; errorText = null },
                    onBirthdayChange = { birthday = it; errorText = null },
                    onGenderChange = { gender = it },
                )
            }
        }

        // Bottom section: indicators + button
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Page indicators
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(TOTAL_PAGES) { index ->
                    val isSelected = pagerState.currentPage == index
                    val width by animateDpAsState(
                        targetValue = if (isSelected) 24.dp else 8.dp,
                        animationSpec = tween(300),
                        label = "indicator_width",
                    )
                    val color by animateColorAsState(
                        targetValue = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        animationSpec = tween(300),
                        label = "indicator_color",
                    )
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .width(width)
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(color),
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Action button
            Button(
                onClick = {
                    if (isLastPage) {
                        tryComplete()
                    } else {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Text(
                    text = if (isLastPage) "开始使用" else "下一步",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
private fun IntroPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = page.emoji,
            fontSize = 80.sp,
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 26.sp,
        )
    }
}

@Composable
private fun ProfileSetupPage(
    name: String,
    birthday: String,
    gender: Gender,
    errorText: String?,
    onNameChange: (String) -> Unit,
    onBirthdayChange: (String) -> Unit,
    onGenderChange: (Gender) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "\uD83D\uDC76",
            fontSize = 64.sp,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "填写宝宝信息",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "这些信息用于生成成长曲线和疫苗计划",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("宝宝昵称") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))

        NativeDatePickerField(
            value = birthday,
            onValueChange = onBirthdayChange,
            modifier = Modifier.fillMaxWidth(),
            label = "出生日期",
            supportingText = "点击选择日期",
            maxDate = LocalDate.now(),
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "性别",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Gender.entries.forEach { entry ->
                FilterChip(
                    selected = entry == gender,
                    onClick = { onGenderChange(entry) },
                    label = { Text(entry.label) },
                )
            }
        }

        errorText?.let {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
