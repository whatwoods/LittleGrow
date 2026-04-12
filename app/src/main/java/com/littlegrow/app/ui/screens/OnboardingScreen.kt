package com.littlegrow.app.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.littlegrow.app.data.BabyProfile
import com.littlegrow.app.data.Gender
import com.littlegrow.app.ui.BabyAvatar
import com.littlegrow.app.ui.NativeDatePickerField
import com.littlegrow.app.ui.PhotoActionRow
import com.littlegrow.app.ui.components.GlassSurface
import com.littlegrow.app.ui.dateFormatter
import com.littlegrow.app.ui.rememberManagedPhotoAttachment
import java.time.LocalDate
import com.littlegrow.app.ui.components.ExpressiveButton as Button
import com.littlegrow.app.ui.components.ExpressiveFilterChip as FilterChip
import com.littlegrow.app.ui.components.ExpressiveTextButton as TextButton

private enum class OnboardingVisualStyle {
    WELCOME,
    GROWTH,
    MILESTONE,
}

private data class OnboardingPage(
    val title: String,
    val description: String,
    val style: OnboardingVisualStyle,
)

private val introPages = listOf(
    OnboardingPage(
        title = "欢迎来到长呀长",
        description = "把喂养、睡眠、排泄和成长时刻\n整理成一条清晰、温柔的育儿时间线。",
        style = OnboardingVisualStyle.WELCOME,
    ),
    OnboardingPage(
        title = "科学追踪成长",
        description = "用 WHO 标准曲线观察体重和身高趋势，\n不只看到一个点，也看到整体节奏。",
        style = OnboardingVisualStyle.GROWTH,
    ),
    OnboardingPage(
        title = "里程碑与时光轴",
        description = "第一次翻身、第一次发声、第一次挥手，\n每个节点都能留下照片和注释。",
        style = OnboardingVisualStyle.MILESTONE,
    ),
)

private const val TOTAL_PAGES = 4
private const val PROFILE_PAGE_INDEX = 3

@Composable
fun OnboardingScreen(
    onComplete: (BabyProfile) -> Unit,
) {
    var currentPage by rememberSaveable { mutableStateOf(0) }
    val isLastPage = currentPage == TOTAL_PAGES - 1

    var name by rememberSaveable { mutableStateOf("") }
    var birthday by rememberSaveable { mutableStateOf(LocalDate.now().format(dateFormatter)) }
    var gender by rememberSaveable { mutableStateOf(Gender.BOY) }
    var errorText by rememberSaveable { mutableStateOf<String?>(null) }
    var isCompleting by rememberSaveable { mutableStateOf(false) }
    var retainedAvatarPath by rememberSaveable { mutableStateOf<String?>(null) }
    val avatarAttachment = rememberManagedPhotoAttachment(
        initialPhotoPath = null,
        photoTag = "avatar",
        onError = { errorText = it },
    )
    val latestAvatarPath by rememberUpdatedState(avatarAttachment.photoPath)
    val latestRetainedAvatarPath by rememberUpdatedState(retainedAvatarPath)
    val latestDiscardAvatarChanges by rememberUpdatedState(avatarAttachment.discardChanges)

    DisposableEffect(Unit) {
        onDispose {
            if (latestAvatarPath != latestRetainedAvatarPath) {
                latestDiscardAvatarChanges()
            }
        }
    }

    fun navigateToPage(page: Int) {
        currentPage = page.coerceIn(0, TOTAL_PAGES - 1)
    }

    fun tryComplete() {
        if (isCompleting) {
            return
        }
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
        isCompleting = true
        avatarAttachment.commitChanges()
        retainedAvatarPath = avatarAttachment.photoPath
        onComplete(
            BabyProfile(
                name = trimmedName,
                birthday = parsedBirthday,
                gender = gender,
                avatarPath = avatarAttachment.photoPath,
            ),
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.94f))
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        OnboardingBackdrop(page = currentPage)

        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                if (!isLastPage) {
                    TextButton(
                        enabled = !isCompleting,
                        onClick = {
                            if (!isCompleting) {
                                navigateToPage(PROFILE_PAGE_INDEX)
                            }
                        },
                        modifier = Modifier.semantics { contentDescription = "跳过引导，直接进入应用" },
                    ) {
                        Text("跳过")
                    }
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                AnimatedContent(
                    targetState = currentPage,
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(380)) + scaleIn(initialScale = 0.92f)).togetherWith(
                            fadeOut(animationSpec = tween(220)) + scaleOut(targetScale = 0.96f)
                        )
                    },
                    label = "page_transition",
                ) { page ->
                    if (page < introPages.size) {
                        IntroPageContent(page = introPages[page])
                    } else {
                        ProfileSetupPage(
                            name = name,
                            birthday = birthday,
                            gender = gender,
                            errorText = errorText,
                            avatarPath = avatarAttachment.photoPath,
                            onNameChange = { name = it; errorText = null },
                            onBirthdayChange = { birthday = it; errorText = null },
                            onGenderChange = { gender = it },
                            onTakeAvatar = avatarAttachment.onTakePhoto,
                            onPickAvatar = avatarAttachment.onPickPhoto,
                            onRemoveAvatar = avatarAttachment.onRemovePhoto,
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                GlassSurface(
                    modifier = Modifier.fillMaxWidth(),
                    alpha = 0.84f,
                    shape = RoundedCornerShape(32.dp),
                    accentColor = MaterialTheme.colorScheme.primary,
                    shadowElevation = 22.dp,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            repeat(TOTAL_PAGES) { index ->
                                val isSelected = currentPage == index
                                val width by animateDpAsState(
                                    targetValue = if (isSelected) 24.dp else 8.dp,
                                    animationSpec = spring(dampingRatio = 0.7f),
                                    label = "indicator_width",
                                )
                                val color by animateColorAsState(
                                    targetValue = if (isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    },
                                    animationSpec = spring(dampingRatio = 0.7f),
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
                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            enabled = !isCompleting,
                            onClick = {
                                if (isLastPage) {
                                    tryComplete()
                                } else if (!isCompleting) {
                                    navigateToPage(currentPage + 1)
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
        }
    }
}

@Composable
private fun OnboardingBackdrop(page: Int) {
    val transition = rememberInfiniteTransition(label = "onboarding_backdrop")
    val drift by transition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "backdrop_drift",
    )

    val primary = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
    val tertiary = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f)
    val secondary = MaterialTheme.colorScheme.secondary.copy(alpha = 0.10f)

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .offset(x = (40 + page * 8).dp, y = (-24).dp)
                .size(220.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(listOf(primary, Color.Transparent))
                ),
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = (24 + drift * 18).dp, y = ((page * 10) - 36).dp)
                .size(260.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(listOf(tertiary, Color.Transparent))
                ),
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-32 + drift * 12).dp, y = 12.dp)
                .size(240.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(listOf(secondary, Color.Transparent))
                ),
        )
    }
}

@Composable
private fun IntroPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        GlassSurface(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 520.dp),
            alpha = 0.74f,
            shape = RoundedCornerShape(36.dp),
            accentColor = MaterialTheme.colorScheme.primary,
            shadowElevation = 22.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                when (page.style) {
                    OnboardingVisualStyle.WELCOME -> WelcomeIllustration()
                    OnboardingVisualStyle.GROWTH -> GrowthIllustration()
                    OnboardingVisualStyle.MILESTONE -> MilestoneIllustration()
                }
                Text(
                    text = page.title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = page.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 26.sp,
                )
            }
        }
    }
}

@Composable
private fun WelcomeIllustration() {
    val transition = rememberInfiniteTransition(label = "welcome")
    val haloScale by transition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "halo_scale",
    )

    Box(
        modifier = Modifier.size(220.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size((170 * haloScale).dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                            Color.Transparent,
                        ),
                    )
                ),
        )
        GlassSurface(
            modifier = Modifier.size(148.dp),
            alpha = 0.76f,
            shape = CircleShape,
            accentColor = MaterialTheme.colorScheme.tertiary,
            shadowElevation = 18.dp,
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                BabyAvatar(
                    avatarPath = null,
                    contentDescription = "欢迎插图",
                    modifier = Modifier.size(92.dp),
                    containerColor = MaterialTheme.colorScheme.surface,
                    borderColor = Color.White.copy(alpha = 0.18f),
                )
            }
        }
    }
}

@Composable
private fun GrowthIllustration() {
    val transition = rememberInfiniteTransition(label = "growth")
    val progress by transition.animateFloat(
        initialValue = 0.18f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "curve_progress",
    )
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.36f)
    val outlineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)
    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryGlow = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.22f)
    val tertiaryColor = MaterialTheme.colorScheme.tertiary

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
    ) {
        val width = size.width
        val height = size.height
        val left = 24f
        val bottom = height - 24f
        val top = 18f
        val right = width - 24f
        val path = Path().apply {
            moveTo(left, bottom - height * 0.12f)
            cubicTo(
                width * 0.28f,
                bottom - height * 0.42f,
                width * 0.54f,
                bottom - height * 0.20f,
                right,
                top + height * 0.18f,
            )
        }

        drawRoundRect(
            color = surfaceVariant,
            topLeft = Offset(left, top),
            size = Size(right - left, bottom - top),
            cornerRadius = CornerRadius(32f, 32f),
        )
        repeat(4) { index ->
            val y = top + (bottom - top) * index / 3f
            drawLine(
                color = outlineColor,
                start = Offset(left + 12f, y),
                end = Offset(right - 12f, y),
                strokeWidth = 2f,
            )
        }
        drawPath(
            path = path,
            color = primaryColor,
            style = Stroke(width = 10f, cap = StrokeCap.Round),
            alpha = progress,
        )
        val pointX = left + (right - left) * progress
        val pointY = bottom - (bottom - top) * (0.15f + 0.55f * progress)
        drawCircle(
            color = tertiaryGlow,
            radius = 26f,
            center = Offset(pointX, pointY),
        )
        drawCircle(
            color = tertiaryColor,
            radius = 11f,
            center = Offset(pointX, pointY),
        )
    }
}

@Composable
private fun MilestoneIllustration() {
    val transition = rememberInfiniteTransition(label = "timeline")
    val activeIndex by transition.animateFloat(
        initialValue = 0f,
        targetValue = 2.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "timeline_steps",
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        repeat(3) { index ->
            val active = activeIndex >= index.toFloat()
            val nodeColor = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(if (index == 1) 28.dp else 22.dp)
                        .clip(CircleShape)
                        .background(nodeColor),
                )
                GlassSurface(
                    modifier = Modifier.weight(1f),
                    alpha = if (active) 0.74f else 0.58f,
                    shape = RoundedCornerShape(20.dp),
                    accentColor = if (index == 2) MaterialTheme.colorScheme.tertiary else nodeColor,
                    shadowElevation = 12.dp,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = listOf("第一次抬头", "第一次翻身", "第一次挥手")[index],
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = listOf("第 42 天", "第 118 天", "第 206 天")[index],
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileSetupPage(
    name: String,
    birthday: String,
    gender: Gender,
    errorText: String?,
    avatarPath: String?,
    onNameChange: (String) -> Unit,
    onBirthdayChange: (String) -> Unit,
    onGenderChange: (Gender) -> Unit,
    onTakeAvatar: () -> Unit,
    onPickAvatar: () -> Unit,
    onRemoveAvatar: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        GlassSurface(
            modifier = Modifier
                .widthIn(max = 480.dp)
                .fillMaxWidth(),
            alpha = 0.72f,
            shape = RoundedCornerShape(28.dp),
            accentColor = MaterialTheme.colorScheme.primary,
            shadowElevation = 18.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                BabyAvatar(
                    avatarPath = avatarPath,
                    contentDescription = "宝宝头像",
                    modifier = Modifier
                        .size(108.dp)
                        .clip(CircleShape),
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.84f),
                    borderColor = Color.White.copy(alpha = 0.22f),
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
                Spacer(modifier = Modifier.height(20.dp))
                PhotoActionRow(
                    title = "头像",
                    removeLabel = "移除头像",
                    hasPhoto = avatarPath != null,
                    onTakePhoto = onTakeAvatar,
                    onPickPhoto = onPickAvatar,
                    onRemovePhoto = onRemoveAvatar,
                )
                Spacer(modifier = Modifier.height(24.dp))

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
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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
    }
}
