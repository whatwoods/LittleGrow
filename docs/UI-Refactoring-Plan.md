# LittleGrow UI 优化计划



## Context



LittleGrow 是一款 Jetpack Compose + Material3 Expressive 的宝宝成长记录 App。当前 UI 已有良好的磨砂玻璃效果、设计 token 体系和自适应布局，但存在 token 使用不一致、硬编码颜色、主题变体未实现、动效缺失、大文件难以维护等问题。本次优化覆盖一致性、动效、主题、功能增强和代码拆分五个维度。



---



## Sprint 1: 一致性修复 (1-2 天)



### 1.1 统一 Spacing Token 使用



**问题:** GrowthScreen、RecordsScreen、SettingsScreen 完全没有使用 Spacing token，全部是原始 `16.dp`、`8.dp` 等字面量。HomeScreen 也有混用。



**改动:**

- `DesignSystem.kt` — 新增 `Spacing.lg2 = 20.dp` 覆盖 RecordsScreen 中的 `20.dp`

- `GrowthScreen.kt` — 全部 `16.dp` → `Spacing.lg`，`8.dp` → `Spacing.sm`，`12.dp` → `Spacing.md`

- `RecordsScreen.kt` — 同上，`20.dp` → `Spacing.lg2`

- `SettingsScreen.kt` — 同上

- `HomeScreen.kt` — `24.dp` → `Spacing.xl`，补齐剩余原始 dp



### 1.2 提取硬编码颜色到主题语义色



**问题:** 多处使用 `Color(0x...)` 字面量，暗色模式下无法适配。



**改动:**

- 新建 `ui/theme/SemanticColors.kt` — 定义 `LocalSemanticColors` CompositionLocal，包含：

  - `milestoneCategoryColors`: Map<MilestoneCategory, Color> (亮/暗双版本)

  - `feedingAccent` / `sleepAccent` / `diaperAccent` 等记录类型色

  - `referenceNormal` / `referenceBelow` / `referenceAbove` 成长参考色

  - `carouselPlaceholders`: List<Pair<Color, Color>> 适配暗色模式

- `GlassSurface.kt:89` — `Color(0xFFB0B2AF)` → `MaterialTheme.colorScheme.outlineVariant`

- `Shadows.kt` — 硬编码 `Color(0xFF825600)` → 接受 `color` 参数，默认从 `MaterialTheme.colorScheme.primary` 取值

- `TimelineScreen.kt` — 替换 5 组 carousel 颜色和 milestone 颜色

- `RecordsScreen.kt` — 替换 feeding 橙色等

- `HomeScreen.kt` — 替换参考徽章颜色



### 1.3 统一 EmptyRecordCard



**问题:** `EmptyRecordCard` 在 HomeScreen 和 RecordsScreen 中各有一份不同实现。



**改动:**

- `Commons.kt` — 新增统一的 `EmptyRecordCard(message, action?)` 组件，基于 `EmptyState` + `GlassSurface`

- `HomeScreen.kt` — 删除私有 `EmptyRecordCard`，改用 Commons 版本

- `RecordsScreen.kt` — 删除公有 `EmptyRecordCard`，改用 Commons 版本



### 1.4 HomeScreen 卡片使用统一



**问题:** 看护人筛选区使用原始 `ElevatedCard`，`SectionCard` 与 `InfoCard` 功能重复。



**改动:**

- `HomeScreen.kt` — 看护人筛选区改用 `InfoCard(title = "看护人筛选")`

- `HomeScreen.kt` — `SectionCard` 调用全部改为 `InfoCard(tone = CardTone.Default)`，删除私有 `SectionCard`



---



## Sprint 2: 动效与交互 + 主题 (3-4 天)



### 2.1 实现主题变体 (PEACH / MINT / LAVENDER)



**问题:** `appThemeSpec()` 始终返回 NurturingTheme，设置页选择器不生效。



**改动:**

- `Theme.kt` — 新增三组 `AppThemeSpec`：

  - **PEACH**: 珊瑚粉 primary、奶油色 background、暖粉 tertiary

  - **MINT**: 薄荷绿 primary、浅绿 background、海沫 secondary  

  - **LAVENDER**: 淡紫 primary、薰衣草灰 background、玫瑰粉 tertiary

- 每组包含 `lightColors` + `darkColors` + `preview`

- `appThemeSpec()` 改为 `when(theme)` 分发

- `LittleGrowTheme` 暗色模式改用 `appThemeSpec(appTheme).darkColors`



### 2.2 列表入场交错动画



**改动:**

- 新建 `ui/components/StaggeredAnimation.kt` — 提供 `Modifier.staggeredFadeSlideIn(index)` 修饰器

- 使用 M3 Expressive motion tokens: `MaterialTheme.motionScheme.defaultEffectsSpec()`

- 应用到: HomeScreen 各模块 item、RecordsScreen bento 卡片和记录列表、GrowthScreen 记录项、TimelineScreen 时间线行



### 2.3 下拉刷新



**改动:**

- HomeScreen、RecordsScreen、GrowthScreen、TimelineScreen — LazyColumn 外层包裹 M3 `PullToRefreshBox`

- `MainViewModel.kt` — 新增 `refreshing` 状态，触发时执行 300ms 动画表示数据已是最新



### 2.4 暗色模式阴影修复



**改动:**

- `Shadows.kt` — `softShadow()` 改为 `@Composable` 修饰器函数，内部读取 `MaterialTheme.colorScheme.primary` 作为阴影色

- 或改为接受 `color: Color` 参数，调用处传入主题色

- 验证所有主题变体在亮/暗模式下的 GlassSurface 视觉效果



---



## Sprint 3: 功能增强 (5-7 天)



### 3.1 生长曲线图增强



**文件:** `GrowthScreen.kt` (后续拆分到 `GrowthChart.kt`)



**改动:**

- **Phase A** — WHO 百分位带着色: 在 P3-P97 之间绘制渐变填充区域，数据已有 (`WhoGrowthStandards` 的 `p3/p15/p50/p85/p97`)

- **Phase B** — 坐标轴标签: 使用 `drawText` + `TextMeasurer` 绘制 Y 轴数值 (kg/cm) 和 X 轴月份标签

- **Phase C** — 触摸交互: `pointerInput` + `detectTapGestures` 定位最近数据点，显示浮层 tooltip (日期、数值、百分位)

- **Phase D** — 百分位标签: 图表右侧绘制 "P3"、"P50"、"P97" 文字



### 3.2 时间线视觉增强



**文件:** `TimelineScreen.kt`



**改动:**

- 轴线渐变按阶段变色 (新生儿=琥珀, 3-6月=青绿, 6-12月=主色)

- 最新节点添加脉冲环动画 (`InfiniteTransition`)

- 节点颜色按里程碑类别区分

- `GlassSurface.accentColor` 按类别变化

- 有照片的里程碑卡片显示缩略图



### 3.3 引导页视觉升级



**文件:** `OnboardingScreen.kt`



**改动:**

- 替换纯 emoji 插图为组合装饰元素:

  - 欢迎页: GlassSurface + 头像占位符呼吸动画

  - 成长追踪页: Canvas 曲线绘制动画

  - 里程碑页: 时间线节点依次入场

- 页面切换增加微缩放动画 (0.92f → 1f)

- 背景添加渐变色块视差效果



### 3.4 Widget 增强



**文件:** `LittleGrowWidget.kt`



**改动:**

- 2x2 Bento 布局: 今日喂养次数、睡眠时长、换尿布次数、下次疫苗

- "距上次喂养" 倒计时显示

- 底部快捷记录按钮行

- Glance `ColorProvider` 适配选定主题



---



## Sprint 4: 代码拆分 (2-3 天)



### 4.1 RecordsScreen 拆分



**当前:** 1450 行单文件



**拆分为:**

- `ui/screens/records/RecordsScreen.kt` — 协调器 (~200 行)

- `ui/screens/records/RecordsBentoSection.kt` — Hero + Bento 网格

- `ui/screens/records/RecordTabContent.kt` — 各 Tab 的列表内容

- `ui/screens/records/RecentActivitySection.kt` — 最近活动卡片



### 4.2 GrowthScreen 拆分



**当前:** 1446 行单文件



**拆分为:**

- `ui/screens/growth/GrowthScreen.kt` — 协调器

- `ui/screens/growth/GrowthChart.kt` — 图表组件 (含 Canvas 绘制)

- `ui/screens/growth/GrowthSummarySection.kt` — 月度汇总 + Bento 卡片

- `ui/screens/growth/VaccineSection.kt` — 疫苗概览和列表



### 4.3 HomeScreen 拆分



**当前:** 1081 行



**拆分为:**

- `ui/screens/home/HomeScreen.kt` — LazyColumn 协调器

- `ui/screens/home/HomeHero.kt` — PostcardHeroCard + StatsBentoGrid

- `ui/screens/home/HomeModuleSections.kt` — 各模块区段 (TrendCard, RoutineCard, MemoryCard 等)



### 4.4 StageReportSheet 拆分



**当前:** 2904 行



**拆分方向:** 按报告阶段或组件类型拆分为多个文件



---



## 设计原则



1. **Token 优先** — 屏幕文件中零原始 `dp` 或 `Color(0x...)`

2. **Glass 一致性** — 重点内容用 GlassSurface，标准内容用 ElevatedCard/InfoCard，同层级不混用

3. **M3 Expressive Motion** — 动画使用 `MaterialTheme.motionScheme` 规格

4. **暗色模式对等** — 每个视觉元素在暗色模式下必须看起来是有意为之的

5. **组件复用** — 新增内联组件前先检查 Commons.kt / ExpressiveControls.kt