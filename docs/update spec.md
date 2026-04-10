# LittleGrow 更新迭代 — 技术规格文档

> 基于 `docs/update plan.md`，结合当前代码库状态编写。
> 当前版本: 单模块 MVVM + Jetpack Compose + Room (v3) + SharedPreferences + Glance Widget + 纯离线架构。
> 更新日期: 2026-04-09

---

## 目录

- [文档范围](#文档范围)
- [当前实现基线](#当前实现基线)
- [横切设计约束](#横切设计约束)
- [Phase 1: 核心体验强化](#phase-1-核心体验强化)
- [Phase 2: 数据价值释放](#phase-2-数据价值释放)
- [Phase 3: 数据安全与备份](#phase-3-数据安全与备份)
- [Phase 4: 情感设计与阶段适配](#phase-4-情感设计与阶段适配)
- [Phase 5: 多看护人协作](#phase-5-多看护人协作)
- [数据库迁移汇总](#数据库迁移汇总)
- [实施顺序建议](#实施顺序建议)
- [测试与验收要求](#测试与验收要求)
- [风险与回滚策略](#风险与回滚策略)
- [非目标 / 明确不做](#非目标--明确不做)

---

## 文档范围

本文档不是 PRD 的重复抄写，而是把 `docs/update plan.md` 中的更新方向压缩成当前仓库可执行的技术规格。目标有三点：

1. 明确每个功能在当前代码结构下应落在哪一层。
2. 明确哪些改动必须合并实施，尤其是数据库迁移、通知恢复和备份兼容。
3. 明确本轮迭代的边界，避免实现阶段重新引入架构争论。

默认前提：

- 以当前单模块工程继续演进，不先做模块拆分。
- 以纯离线为前提，不新增账号系统和自建服务端。
- 以 Android 8.0+ 为最低兼容版本，不为了少量新 API 抬高 `minSdk`。

---

## 当前实现基线

为避免 spec 与代码脱节，以下事实视为本轮实现基线：

- 数据层：Room `AppDatabase` 当前版本为 `v3`，已有 `MIGRATION_1_2` 与 `MIGRATION_2_3`，并有迁移测试。
- 偏好层：`PreferencesRepository` 当前基于 `SharedPreferences` 封装为 `Flow`，尚未迁移到 DataStore。
- 视图层：`MainViewModel` 直接组合 Repository 与导出/通知/Widget 刷新逻辑，没有 UseCase/Hilt 层。
- UI：Navigation Compose 单 Activity 架构，主要页面由 `ui/MainApp.kt` 统一路由。
- 快捷记录：已有 `QuickRecordSheet`、母乳计时器、Widget 跳转记录页，以及 `AppLaunchTarget`/`RecordQuickAction` 机制。
- 媒体能力：图片使用应用私有目录与 `FileProvider`；记录删除时会清理附件。
- 通知能力：当前已有疫苗提醒 Receiver 与开机恢复链路，可作为后续提醒类功能的统一模板。

这意味着后续新增功能默认优先复用现有入口，而不是为了“结构更优雅”先重构一轮。

---

## 横切设计约束

### 1. 架构约束

- 本轮不引入 Hilt、不拆分多模块、不补 UseCase 层，除非某项功能无法在现有结构中安全实现。
- 新增业务逻辑优先放在 `data/*Provider`、`*Generator`、`*Analyzer` 等纯 Kotlin 组件中，由 `MainViewModel` 编排调用。
- 新增设置项继续先接入 `PreferencesRepository`；除非偏好字段已明显失控，否则不单独发起 SharedPreferences → DataStore 迁移。

### 2. 数据约束

- 所有新增实体字段必须提供明确默认值或兼容迁移路径，不能依赖“新安装用户不会遇到”。
- 只要改动 Room schema，就必须同时提交：
  - `AppDatabase.kt` 版本号与 Migration
  - `app/schemas/...` 导出 schema
  - `AppDatabaseMigrationTest.kt` 回归测试
- 需要跨设备或备份恢复的标识，优先使用稳定字段而不是列表位置或 UI 顺序推导。

### 3. 通知与后台任务约束

- 新增通知能力必须考虑 Android 13+ 通知权限、设备重启、应用升级、时区切换后的恢复。
- 周期性任务优先使用 WorkManager；只有需要精确到分钟级的本地提醒时才保留 AlarmManager。
- 所有提醒默认语气柔和，不制造焦虑，不使用高优先级打断式通知。

### 4. UI/交互约束

- 所有新增入口应优先复用现有页面：`HomeScreen`、`RecordsScreen`、`GrowthScreen`、`TimelineScreen`、`SettingsScreen`。
- 夜间/单手操作场景优先级高于视觉装饰；任何新增交互不能让高频记录路径变长。
- 大多数图表和状态卡片使用 Compose 原生能力实现，避免为了单一图表引入重型三方库。

### 5. 质量约束

- 每个 Phase 的“无 schema 改动功能”应尽量先落地，以便把数据库升级集中到一次版本迁移。
- 涉及导入、恢复、批量写入的功能必须具备幂等或去重策略，避免重复导入造成脏数据。
- 所有文件导出与恢复流程必须能在无网络、无外部存储权限前提下完成。

---

## Phase 1: 核心体验强化

> 对应 update plan 第一节「记录效率」+ 第七节「具体功能改进」+ 第八节「UI/交互改进」。
> 目标：减少每次记录的操作步数，让宝妈在抱娃、夜奶等场景下也能轻松记录。

### 1.1 智能默认值

**需求**：打开记录表单时，根据上下文预填合理默认值，减少手动选择。

**规则引擎**：

| 场景 | 默认行为 |
|------|---------|
| 凌晨 0:00-6:00 打开喂奶记录 | 自动选择「亲喂」类型 |
| 上次亲喂选了「左侧」 | 本次默认「右侧」，并在 UI 上显示提示文案 |
| 上次瓶喂量 120ml | 本次预填 120ml |
| 最近 7 天无辅食记录 + 宝宝 < 6 月龄 | 隐藏辅食选项 |
| 最近 7 天有辅食记录 + 宝宝 >= 6 月龄 | 辅食选项排列靠前 |

**技术方案**：
- 新建 `SmartDefaultsProvider` 工具类，注入 `LittleGrowRepository`。
- 查询最近一条 `FeedingEntity` 判断上次喂奶类型/侧别/奶量。
- 根据 `System.currentTimeMillis()` 判断时段。
- 根据 `BabyProfile.birthday` 计算月龄，决定是否展示辅食选项。
- `QuickRecordSheet` 和 `RecordsScreen` 的表单读取该 Provider 获取默认值。

**涉及文件**：
- 新增: `data/SmartDefaultsProvider.kt`
- 修改: `ui/screens/QuickRecordSheet.kt`, `ui/screens/RecordsScreen.kt`

---

### 1.2 通知栏快捷操作

**需求**：常驻通知条，展开可直接点击「喂奶/换尿布/入睡」按钮，跳转到对应记录页面。

**技术方案**：
- 新建 `QuickActionNotificationService` (Foreground Service)，在 Settings 中提供开关。
- 通知使用 `NotificationCompat.Builder` 构建，添加 3 个 Action：
  - 喂奶 → PendingIntent 携带 `quickAction=FEEDING`
  - 换尿布 → PendingIntent 携带 `quickAction=DIAPER`
  - 入睡 → PendingIntent 携带 `quickAction=SLEEP`
- `MainActivity` 已有 intent extra 处理逻辑（`AppLaunchTarget`），复用 `pendingRecordQuickAction` 机制。
- 通知渠道: 新建 `quick_action` channel，重要性 LOW（无声音）。

**涉及文件**：
- 新增: `notifications/QuickActionNotificationService.kt`
- 修改: `ui/screens/SettingsScreen.kt` (增加开关)
- 修改: `data/PreferencesRepository.kt` (持久化开关状态)
- 修改: `AndroidManifest.xml` (注册 Service)

---

### 1.3 Widget 一键记录

**需求**：桌面 Widget 上增加「尿了」「拉了」一键按钮，点击后直接写入记录，无需打开 App。

**技术方案**：
- 在现有 `LittleGrowWidget` 中增加 ActionCallback：
  - `QuickPeeAction`: 直接创建 `DiaperEntity(type=PEE, happenedAt=now)`。
  - `QuickPoopAction`: 直接创建 `DiaperEntity(type=POOP, happenedAt=now)`。
- ActionCallback 中通过 `AppDatabase.getInstance(context)` 直接写入，写入后 `LittleGrowWidget.updateAll(context)`。
- Widget 布局新增两个按钮行。

**涉及文件**：
- 修改: `widget/LittleGrowWidget.kt`

---

### 1.4 母乳计时器联动

**需求**：正在喂奶时点击「入睡」，自动结束喂奶计时器并保存喂奶记录。

**技术方案**：
- `MainViewModel` 中 `saveSleep()` 方法检查 `breastfeedingTimer` 状态：
  - 若 `isRunning == true`，自动调用 `stopBreastfeedingTimer()` 并 `saveFeeding()` 保存当前计时记录。
  - 弹出 Snackbar 提示「已自动结束喂奶记录」。

**涉及文件**：
- 修改: `MainViewModel.kt`

---

### 1.5 批量补录

**需求**：一次性快速补录多条记录（忙了一天忘记录的场景）。

**技术方案**：
- 新增 `BatchRecordScreen`，以列表形式展示可添加的记录行：
  - 每行：时间选择 + 类型下拉 + 关键字段（如奶量/尿布类型）+ 删除按钮。
  - 底部「+ 添加一行」按钮。
  - 顶部「保存全部」按钮，批量调用 `repository.upsertXxx()`。
- 入口：`RecordsScreen` 右上角菜单新增「批量补录」选项。
- 日期默认当天，可修改。

**涉及文件**：
- 新增: `ui/screens/BatchRecordScreen.kt`
- 修改: `ui/screens/RecordsScreen.kt` (增加入口)
- 修改: `ui/MainApp.kt` (增加导航路由)

---

### 1.6 喂奶功能增强

#### 1.6.1 左右乳切换提醒

- 在 `QuickRecordSheet` 喂奶表单顶部显示提示条：「上次喂的左侧，这次该喂右侧了」。
- 数据来源：复用 1.1 SmartDefaultsProvider 查询最近亲喂记录。

#### 1.6.2 奶量趋势图

- 在 `RecordsScreen` 喂奶 Tab 顶部新增折线图区域。
- 筛选 `FeedingType.BOTTLE_FORMULA` 和 `BOTTLE_BREAST_MILK` 类型记录。
- X 轴: 日期（最近 7/14/30 天可切换），Y 轴: 日累计奶量 (ml)。
- 使用 Compose Canvas 自绘，不引入第三方图表库。

#### 1.6.3 辅食过敏追踪

**数据模型变更**：
- `FeedingEntity` 新增字段：
  - `allergyObservation: AllergyStatus` (枚举: NONE / OBSERVING / SAFE / ALLERGIC)
  - `observationEndDate: LocalDate?`
- 当 `feedingType == SOLID_FOOD` 且 `foodName` 为首次出现的食材时，自动将 `allergyObservation` 设为 `OBSERVING`，`observationEndDate` 设为 `happenedAt + 3天`。
- 新增定时检查：`VaccineReminderScheduler` 扩展或新建 `ObservationReminderScheduler`，到期推送通知「XX 没有过敏反应，可以继续添加」。

**涉及文件**：
- 修改: `data/Models.kt` (新增枚举 + 字段)
- 修改: `data/AppDatabase.kt` (Migration v3→v4)
- 新增: `notifications/ObservationReminderScheduler.kt`
- 修改: `ui/screens/RecordsScreen.kt` (辅食过敏状态 UI)

---

### 1.7 睡眠功能增强

#### 1.7.1 睡眠模式分析

**数据模型变更**：
- `SleepEntity` 新增字段：
  - `sleepType: SleepType` (枚举: NAP / NIGHT_SLEEP)
- 自动推断规则：`startTime` 在 19:00-次日 7:00 之间 → `NIGHT_SLEEP`，否则 → `NAP`。用户可手动修改。

#### 1.7.2 夜醒次数追踪

- 定义「夜间」为 19:00-次日 7:00。
- 该时间段内的多段 `NIGHT_SLEEP` 记录，间隔 > 10 分钟视为一次夜醒。
- 在 `RecordsScreen` 睡眠 Tab 顶部显示「昨晚夜醒 X 次」卡片。
- 计算逻辑放在 `MainViewModel` 中作为 derived StateFlow。

#### 1.7.3 入睡方式记录

**数据模型变更**：
- `SleepEntity` 新增字段：
  - `fallingAsleepMethod: FallingAsleepMethod` (枚举: NURSING / ROCKING / SELF_SOOTHING / OTHER)
- 表单中新增选择项。

**涉及文件**：
- 修改: `data/Models.kt` (新增枚举 + 字段)
- 修改: `data/AppDatabase.kt` (统一在 Migration v3→v4)
- 修改: `ui/screens/RecordsScreen.kt` (UI 展示)

---

### 1.8 大便功能增强

#### 1.8.1 拍照记录

- `DiaperEntity` 新增字段：`photoPath: String?`。
- 复用现有 `PhotoStore` 和 `PhotoAttachment` 组件。
- 在换尿布表单中增加拍照入口。

#### 1.8.2 大便频次异常提醒

- 新增 `DiaperReminderScheduler`，每日 20:00 检查当天是否有 POOP 类型记录。
- 若连续 3 天无 POOP 记录，推送柔性提醒。
- Settings 中提供开关。

**涉及文件**：
- 修改: `data/Models.kt`, `data/AppDatabase.kt` (Migration v3→v4)
- 新增: `notifications/DiaperReminderScheduler.kt`
- 修改: `ui/screens/RecordsScreen.kt`

---

### 1.9 生长曲线增强

#### 1.9.1 BMI 曲线

- 在 `GrowthScreen` 增加 BMI Tab。
- BMI = weightKg / (heightCm/100)^2。
- 需要 WHO BMI-for-age 标准数据，新增 `assets/who/bmi_boys.csv` 和 `bmi_girls.csv`。

#### 1.9.2 生长速度

- 在每条生长记录旁显示与上一条记录的增量及月龄正常范围。
- 示例：「本月体重增长 800g（正常范围 500-1000g）」。
- 正常范围数据来自 `WhoGrowthStandards.kt` 扩展。

#### 1.9.3 预测曲线

- 基于已有数据点做线性回归，在生长曲线图上以虚线绘制预测趋势。
- 仅在数据点 >= 3 个时显示。

**涉及文件**：
- 修改: `ui/screens/GrowthScreen.kt`
- 修改: `data/WhoGrowthStandards.kt`
- 新增: `assets/who/bmi_boys.csv`, `assets/who/bmi_girls.csv`

---

### 1.10 疫苗管理增强

#### 1.10.1 自费疫苗推荐

- `VaccineSchedule.kt` 中新增自费疫苗列表：手足口 (EV71)、流感、水痘、轮状病毒、13价肺炎等。
- UI 中分「国家免疫规划」和「推荐自费疫苗」两个区域展示。
- 自费疫苗默认折叠。

#### 1.10.2 接种反应记录

**数据模型变更**：
- `VaccineEntity` 新增字段：
  - `reactionNote: String?` (反应描述)
  - `hadFever: Boolean` (是否发烧)
  - `reactionSeverity: ReactionSeverity?` (枚举: MILD / MODERATE / SEVERE)

#### 1.10.3 补种提醒

- 若某疫苗 `scheduledDate` 已过期 30 天且 `isDone == false`，在疫苗列表中标红并显示「建议尽快补种」。

**涉及文件**：
- 修改: `data/VaccineSchedule.kt`
- 修改: `data/Models.kt`, `data/AppDatabase.kt` (Migration v3→v4)
- 修改: `ui/screens/SettingsScreen.kt` (疫苗相关 UI 所在位置)

---

### 1.11 UI/交互改进

#### 1.11.1 深色模式自动切换

- 当前已支持 System/Light/Dark 三种模式。
- 新增「定时切换」选项：用户设定时段（如 20:00-7:00）自动使用深色模式。
- `PreferencesRepository` 新增 `darkModeStartHour`, `darkModeEndHour` 字段。
- `Theme.kt` 中根据当前时间和设置决定实际主题。

#### 1.11.2 大字体模式

- Settings 中新增「大字体模式」开关。
- 开启后，`Type.kt` 中所有字号放大 20%。
- 按钮最小高度从 48dp 提升到 64dp。

#### 1.11.3 首页自定义

- Settings 中新增「首页模块配置」，可勾选/取消显示的模块。
- 模块列表：今日摘要、最近喂奶、最近睡眠、最近体重、里程碑、疫苗提醒。
- `PreferencesRepository` 新增 `homeModules: Set<String>` 字段。
- `HomeScreen` 根据配置动态渲染模块。

---

## Phase 2: 数据价值释放

> 对应 update plan 第二节「数据解读」。
> 目标：把原始记录转化为可理解的洞察，让宝妈从「记了有什么用」变成「离不开」。

### 2.1 「这正常吗？」参考指标

**需求**：每个关键数据旁显示同月龄参考范围。

**参考数据源**：
- 喂奶频次：WHO/AAP 建议值，按月龄分段硬编码。
- 睡眠时长：同上。
- 体重/身高：已有 `WhoGrowthStandards.kt`，复用。

**技术方案**：
- 新建 `data/AgeBasedReference.kt`，包含各指标按月龄的正常范围数据。
- 结构：`data class ReferenceRange(val label: String, val min: Double, val max: Double, val unit: String)`。
- 在 `HomeScreen` 今日摘要区域，每项数据后显示小标签：如「8次 · 正常(8-12)」。
- 颜色编码：正常=绿，偏低=黄，偏高=橙。

**涉及文件**：
- 新增: `data/AgeBasedReference.kt`
- 修改: `ui/screens/HomeScreen.kt`

---

### 2.2 趋势分析

**需求**：自动生成周度对比文案。

**实现**：
- `MainViewModel` 新增 `weeklyTrends: StateFlow<List<TrendInsight>>`。
- `TrendInsight` 数据类：`(category: String, description: String, direction: UP/DOWN/STABLE)`。
- 计算逻辑：对比本周与上周的喂奶次数、总睡眠时长、喂奶间隔均值。
- 在 `HomeScreen` 新增「本周趋势」卡片，展示 2-3 条最显著变化。

**涉及文件**：
- 新增: `data/TrendAnalyzer.kt`
- 修改: `MainViewModel.kt`
- 修改: `ui/screens/HomeScreen.kt`

---

### 2.3 异常提醒

**需求**：关键指标异常时主动推送通知。

**规则**：

| 规则 | 触发条件 | 通知文案 |
|------|---------|---------|
| 喂奶间隔过长 | 最近一次喂奶距今 > 月龄对应最大间隔 | 「距离上次喂奶已经 X 小时了」 |
| 大便频次异常 | 连续 3 天无 POOP 记录 | 「宝宝已经 3 天没有大便记录了」 |
| 睡眠不足 | 连续 3 天日均睡眠 < 月龄正常下限 | 「最近几天宝宝睡眠时间偏少」 |

**技术方案**：
- 新建 `notifications/AnomalyChecker.kt`。
- 使用 `WorkManager` PeriodicWorkRequest（每 6 小时检查一次）替代自定义 AlarmManager，更省电。
- 通知渠道: `anomaly_reminder`，重要性 DEFAULT。
- Settings 中增加「智能提醒」总开关。

**涉及文件**：
- 新增: `notifications/AnomalyChecker.kt`
- 修改: `app/build.gradle.kts` (添加 WorkManager 依赖)
- 修改: `ui/screens/SettingsScreen.kt`
- 修改: `data/PreferencesRepository.kt`

---

### 2.4 作息规律发现

**需求**：自动发现宝宝的作息模式。

**实现**：
- 分析最近 14 天数据，按小时聚合各类事件频次。
- 找出高频时段，生成文案：「宝宝通常下午 2 点午睡」「晚上 8-9 点容易哭闹」。
- 展示在 `HomeScreen` 的「作息规律」卡片中。
- 复用 `TrendAnalyzer`，扩展时段分析方法。

---

### 2.5 阶段性报告

**需求**：满月/百天/半岁等节点自动生成阶段报告。

**触发时机**：
- 满月(30天)、百天(100天)、半岁(180天)、一岁(365天)。
- `MainViewModel` 启动时检查宝宝日龄是否命中节点（±1天容差）。

**报告内容**：
- 时间范围内的喂奶总次数、总睡眠时长、换尿布次数、体重变化。
- 达成的里程碑列表。

**展示方式**：
- 弹出 BottomSheet 展示报告卡片，支持截图分享。
- 可在 `TimelineScreen` 中回顾历史报告。

**涉及文件**：
- 新增: `ui/screens/StageReportSheet.kt`
- 新增: `data/StageReportGenerator.kt`
- 修改: `MainViewModel.kt`

---

### 2.6 就医摘要

**需求**：一键生成最近 N 天的数据摘要，供就医时参考。

**实现**：
- 在 `SettingsScreen` 或新增「就医工具」入口。
- 选择时间范围（默认 30 天）后生成结构化文本：
  - 基本信息：姓名、月龄、体重、身高。
  - 喂奶：日均次数、类型分布。
  - 睡眠：日均时长、夜醒次数。
  - 大便：频次、颜色分布。
  - 用药/就医记录。
- 生成为纯文本，支持复制到剪贴板 / 导出 PDF。
- 复用现有 `ExportWriters` 的 PDF 能力。

**涉及文件**：
- 新增: `ui/screens/MedicalSummaryScreen.kt`
- 新增: `data/MedicalSummaryGenerator.kt`
- 修改: `ui/MainApp.kt` (增加路由)

---

## Phase 3: 数据安全与备份

> 对应 update plan 第三节「数据安全」。
> 目标：确保用户数据不会因换手机/手机损坏而丢失。

### 3.1 本地完整备份/恢复

**需求**：将数据库 + 照片打包为单个文件，支持导入恢复。

**备份格式**：
- ZIP 文件，内含：
  - `little_grow.db` (Room 数据库文件)
  - `attachments/` (照片目录)
  - `meta.json` (App 版本号、数据库版本号、备份时间、宝宝名)

**备份流程**：
1. 调用 `AppDatabase.close()` 或使用 checkpoint 确保 WAL 数据写入主文件。
2. 复制 `little_grow.db` 和 `little_grow.db-wal`/`-shm`。
3. 复制 `attachments/` 目录。
4. 写入 `meta.json`。
5. 打包为 `.lgbackup` (实际是 ZIP) 文件。
6. 通过 `Intent.ACTION_CREATE_DOCUMENT` 让用户选择保存位置。

**恢复流程**：
1. 通过 `Intent.ACTION_OPEN_DOCUMENT` 让用户选择备份文件。
2. 读取 `meta.json`，校验数据库版本兼容性。
3. 关闭当前数据库连接。
4. 覆盖数据库文件和照片目录。
5. 重新打开数据库，执行必要的 Migration。
6. 重启 App（或重建 ViewModel）。

**涉及文件**：
- 新增: `data/BackupManager.kt`
- 修改: `ui/screens/SettingsScreen.kt` (备份/恢复按钮)
- 修改: `AndroidManifest.xml` (如需权限)

---

### 3.2 自动备份到本地存储

**需求**：定期自动备份到设备存储，无需用户手动操作。

**技术方案**：
- 使用 `WorkManager` PeriodicWorkRequest，默认每 7 天自动备份一次。
- 备份目标：`MediaStore` 的 Downloads 目录（`MediaStore.Downloads`），Android 10+ 无需权限。
- 保留最近 3 个备份文件，自动清理旧备份。
- Settings 中可配置频率（每天/每 3 天/每周/关闭）。

**涉及文件**：
- 新增: `data/AutoBackupWorker.kt`
- 修改: `data/PreferencesRepository.kt`
- 修改: `ui/screens/SettingsScreen.kt`
- 修改: `app/build.gradle.kts` (WorkManager 依赖，Phase 2 可能已添加)

---

### 3.3 数据导入

**需求**：当前只有 CSV/PDF 导出，缺少导入功能。

**方案**：
- 支持从 `.lgbackup` 文件恢复（3.1 已覆盖）。
- 支持从 CSV 导入记录（增量合并，不覆盖现有数据）：
  - 解析 CSV 表头匹配字段。
  - 按 `happenedAt` 去重，避免重复导入。
  - 导入前预览：显示将导入多少条记录。

**涉及文件**：
- 新增: `data/CsvImporter.kt`
- 修改: `ui/screens/SettingsScreen.kt`

---

## Phase 4: 情感设计与阶段适配

> 对应 update plan 第五节「情感设计」+ 第六节「阶段适配」。
> 目标：让 App 不只是冷冰冰的工具，而是有温度的育儿伙伴。

### 4.1 里程碑庆祝

**需求**：宝宝到达关键时间节点时，弹出庆祝动画。

**触发节点**：
- 时间型：满月(30天)、百天(100天)、半岁(180天)、一岁(365天)。
- 事件型：用户记录里程碑时（第一次翻身、第一颗牙等）。

**技术方案**：
- `MainViewModel` 启动时检查日龄 + `PreferencesRepository` 中已展示过的庆祝列表（避免重复弹出）。
- 使用 Compose Animation API 实现庆祝弹窗：星星/气球动画 + 温馨文案。
- 不引入 Lottie 等第三方动画库，保持轻量。

**涉及文件**：
- 新增: `ui/CelebrationDialog.kt`
- 修改: `MainViewModel.kt`
- 修改: `data/PreferencesRepository.kt`

---

### 4.2 成长回忆（「一年前的今天」）

**需求**：展示历史同日的记录回顾。

**触发条件**：宝宝年龄 >= 1 岁时，查询 365 天前同一天的记录。

**实现**：
- `MainViewModel` 增加 `memoryOfTheDay: StateFlow<MemorySnapshot?>`。
- `MemorySnapshot`: 包含那一天的喂奶次数、睡眠时长、里程碑、照片。
- 在 `HomeScreen` 顶部以卡片形式展示，可关闭/收起。

---

### 4.3 鼓励文案

**需求**：在首页展示基于当日数据的鼓励语句。

**实现**：
- 新建 `data/EncouragementProvider.kt`，根据当日统计数据匹配文案模板。
- 示例：
  - 喂奶 >= 6 次 → 「今天已经喂了 {n} 次奶，辛苦了妈妈 ❤️」
  - 夜间喂奶 >= 2 次 → 「昨晚起了 {n} 次夜，你真的很棒」
  - 连续记录 >= 7 天 → 「已经连续记录 {n} 天了，坚持就是力量」
- 文案库硬编码，每个场景 3-5 条随机选取。
- 在 `HomeScreen` 展示。

---

### 4.4 月龄指南

**需求**：进入新月龄时展示该阶段特点和注意事项。

**实现**：
- 新建 `data/MonthlyGuide.kt`，按月龄存储指南内容（0-24 个月）。
- 每个月龄包含：发育特点（3-5 条）、喂养建议、睡眠建议、注意事项。
- 月龄变化时，在 `HomeScreen` 弹出指南卡片。
- 可在 `TimelineScreen` 回顾所有月龄指南。

---

### 4.5 阶段适配首页

**需求**：根据宝宝月龄自动调整首页布局和功能入口优先级。

**适配规则**：

| 月龄 | 首页突出模块 | 记录类型排序 |
|------|------------|-------------|
| 0-3月 | 喂奶摘要、睡眠摘要、大便监测 | 喂奶 > 睡眠 > 尿布 > 医疗 |
| 4-6月 | 喂奶摘要、辅食引入提示、翻身里程碑 | 喂奶 > 睡眠 > 辅食 > 活动 |
| 6-12月 | 辅食摘要、出牙追踪、爬行/站立 | 辅食 > 睡眠 > 活动 > 喂奶 |
| 1-2岁 | 饮食、语言发展、行为习惯 | 活动 > 饮食 > 睡眠 > 医疗 |
| 2-3岁 | 如厕训练、入园准备 | 活动 > 饮食 > 睡眠 |

**技术方案**：
- 新建 `data/StageConfig.kt`，根据月龄返回模块排序和可见性。
- `HomeScreen` 和 `RecordsScreen` 的 Tab 顺序根据 StageConfig 动态调整。
- 不新增功能，仅调整展示优先级和默认可见性。

**涉及文件**：
- 新增: `data/StageConfig.kt`
- 修改: `ui/screens/HomeScreen.kt`
- 修改: `ui/screens/RecordsScreen.kt`

---

### 4.6 辛苦可视化（年度总结）

**需求**：生成年度/阶段性总结，展示妈妈的累计付出。

**实现**：
- 在满一岁或年底时可生成年度总结卡片：
  - 累计喂奶 X 次，共计 Y 小时
  - 累计换尿布 X 次
  - 累计起夜 X 次
  - 记录最勤的一天
- 卡片设计精美，支持截图分享。
- 入口：`TimelineScreen` 或 Settings 中「年度总结」。

---

## Phase 5: 多看护人协作

> 对应 update plan 第四节「多看护人协作」。
> 目标：支持多设备/多人使用场景。
> **注意**：这是架构影响最大的改动，需要谨慎规划。

### 5.1 看护人标记

**数据模型变更**（最小改动，不依赖网络）：
- 所有记录实体（Feeding/Sleep/Diaper/Medical/Activity）新增字段：
  - `caregiver: String?` (看护人名称，默认为 BabyProfile 中的主用户)
- Settings 中维护看护人列表（纯本地，如「妈妈/爸爸/奶奶/月嫂」）。
- 记录时可选择当前看护人（默认为上次选择的看护人）。
- 首页摘要支持按看护人筛选查看。

**涉及文件**：
- 修改: 所有 Entity 类 (`data/Models.kt`)
- 修改: `data/AppDatabase.kt` (Migration)
- 修改: `data/PreferencesRepository.kt` (看护人列表 + 当前看护人)
- 修改: 所有记录表单 UI

---

### 5.2 交接摘要

**需求**：生成「你离开后发生了什么」的摘要。

**实现**：
- 在 `HomeScreen` 或新增入口，选择「交接起始时间」（如上午 9:00）。
- 生成从该时间到当前的所有记录摘要文本。
- 支持复制到剪贴板 / 分享给微信等应用。

**涉及文件**：
- 新增: `data/HandoverSummaryGenerator.kt`
- 新增: `ui/screens/HandoverSummarySheet.kt`

---

### 5.3 多设备同步（远期）

> 此功能架构复杂度高，建议作为独立版本规划。此处仅给出方案概要。

**方案 A：局域网 P2P 同步**
- 使用 Android NSD (Network Service Discovery) 发现同局域网设备。
- 通过 Socket 传输增量数据（基于时间戳的 diff）。
- 优点：不依赖云端，隐私安全。
- 缺点：需要两台设备同时在线且在同一网络。

**方案 B：文件中转同步**
- 导出增量数据为文件 → 用户通过微信/蓝牙传给对方 → 对方导入合并。
- 优点：实现简单，无网络要求。
- 缺点：非实时，需手动操作。

**方案 C：坚果云 WebDAV 同步**
- 用户配置坚果云 WebDAV 账号。
- App 定期上传增量数据文件到坚果云。
- 其他设备拉取并合并。
- 优点：自动化程度高，不经过开发者服务器。
- 缺点：用户需要坚果云账号。

**数据合并策略**（三个方案通用）：
- 每条记录增加 `uuid: String` 全局唯一标识 + `lastModifiedAt: Long` 时间戳。
- 合并时按 UUID 匹配，`lastModifiedAt` 大的胜出。
- 删除操作使用软删除（`isDeleted: Boolean`）。

---

## 数据库迁移汇总

### Migration v3 → v4

以下字段变更在一次 Migration 中完成：

| 表 | 新增字段 | 类型 | 默认值 |
|----|---------|------|--------|
| `feeding_records` | `allergyObservation` | TEXT | `'NONE'` |
| `feeding_records` | `observationEndDate` | TEXT | `NULL` |
| `sleep_records` | `sleepType` | TEXT | `'NAP'` |
| `sleep_records` | `fallingAsleepMethod` | TEXT | `NULL` |
| `diaper_records` | `photoPath` | TEXT | `NULL` |
| `vaccine_records` | `reactionNote` | TEXT | `NULL` |
| `vaccine_records` | `hadFever` | INTEGER | `0` |
| `vaccine_records` | `reactionSeverity` | TEXT | `NULL` |
| `feeding_records` | `caregiver` | TEXT | `NULL` |
| `sleep_records` | `caregiver` | TEXT | `NULL` |
| `diaper_records` | `caregiver` | TEXT | `NULL` |
| `medical_records` | `caregiver` | TEXT | `NULL` |
| `activity_records` | `caregiver` | TEXT | `NULL` |

Migration SQL：
```sql
-- Feeding
ALTER TABLE feeding_records ADD COLUMN allergyObservation TEXT NOT NULL DEFAULT 'NONE';
ALTER TABLE feeding_records ADD COLUMN observationEndDate TEXT;
ALTER TABLE feeding_records ADD COLUMN caregiver TEXT;

-- Sleep
ALTER TABLE sleep_records ADD COLUMN sleepType TEXT NOT NULL DEFAULT 'NAP';
ALTER TABLE sleep_records ADD COLUMN fallingAsleepMethod TEXT;
ALTER TABLE sleep_records ADD COLUMN caregiver TEXT;

-- Diaper
ALTER TABLE diaper_records ADD COLUMN photoPath TEXT;
ALTER TABLE diaper_records ADD COLUMN caregiver TEXT;

-- Vaccine
ALTER TABLE vaccine_records ADD COLUMN reactionNote TEXT;
ALTER TABLE vaccine_records ADD COLUMN hadFever INTEGER NOT NULL DEFAULT 0;
ALTER TABLE vaccine_records ADD COLUMN reactionSeverity TEXT;

-- Medical & Activity
ALTER TABLE medical_records ADD COLUMN caregiver TEXT;
ALTER TABLE activity_records ADD COLUMN caregiver TEXT;
```

---

## 新增依赖

| 依赖 | 用途 | Phase |
|------|------|-------|
| `androidx.work:work-runtime-ktx` | 定时备份、异常检测 | Phase 2-3 |

> 原则：尽量不引入新依赖，保持 App 轻量。图表使用 Compose Canvas 自绘，动画使用 Compose Animation API。

---

## 新增文件清单

| 文件路径 | 说明 | Phase |
|---------|------|-------|
| `data/SmartDefaultsProvider.kt` | 智能默认值计算 | 1 |
| `data/AgeBasedReference.kt` | 月龄参考范围数据 | 2 |
| `data/TrendAnalyzer.kt` | 趋势分析引擎 | 2 |
| `data/StageReportGenerator.kt` | 阶段报告生成 | 2 |
| `data/MedicalSummaryGenerator.kt` | 就医摘要生成 | 2 |
| `data/EncouragementProvider.kt` | 鼓励文案 | 4 |
| `data/MonthlyGuide.kt` | 月龄指南内容 | 4 |
| `data/StageConfig.kt` | 阶段适配配置 | 4 |
| `data/BackupManager.kt` | 备份/恢复核心逻辑 | 3 |
| `data/AutoBackupWorker.kt` | 自动备份 Worker | 3 |
| `data/CsvImporter.kt` | CSV 导入 | 3 |
| `data/HandoverSummaryGenerator.kt` | 交接摘要生成 | 5 |
| `notifications/QuickActionNotificationService.kt` | 通知栏快捷操作 | 1 |
| `notifications/ObservationReminderScheduler.kt` | 辅食观察提醒 | 1 |
| `notifications/DiaperReminderScheduler.kt` | 大便异常提醒 | 1 |
| `notifications/AnomalyChecker.kt` | 异常检测 Worker | 2 |
| `ui/CelebrationDialog.kt` | 庆祝动画弹窗 | 4 |
| `ui/screens/BatchRecordScreen.kt` | 批量补录页面 | 1 |
| `ui/screens/StageReportSheet.kt` | 阶段报告 Sheet | 2 |
| `ui/screens/MedicalSummaryScreen.kt` | 就医摘要页面 | 2 |
| `ui/screens/HandoverSummarySheet.kt` | 交接摘要 Sheet | 5 |
| `assets/who/bmi_boys.csv` | WHO BMI 标准(男) | 1 |
| `assets/who/bmi_girls.csv` | WHO BMI 标准(女) | 1 |

---

## 实施顺序建议

为降低返工成本，建议按下面的交付顺序推进，而不是严格按文档章节顺序并行乱做：

### 第一批：不改 schema 的高收益功能

- `1.1 智能默认值`
- `1.2 通知栏快捷操作`
- `1.3 Widget 一键记录`
- `1.4 母乳计时器联动`
- `1.5 批量补录`
- `2.1 参考指标`
- `2.2 趋势分析`

目标：先提升记录效率和首页价值，不触碰数据库版本，确保主流程收益尽快可见。

### 第二批：统一数据库升级到 v4

建议把以下字段型需求合并进一次 `v3 → v4`：

- 辅食过敏追踪
- 睡眠类型 / 入睡方式
- 尿布照片
- 疫苗接种反应
- 看护人字段

这样可以避免 `v3 → v4 → v5 → v6` 的碎片式迁移，把兼容成本压到一次测试中完成。

### 第三批：依赖 v4 字段的数据服务

- 辅食观察提醒
- 夜醒统计
- 大便异常提醒
- 就医摘要
- 交接摘要

### 第四批：备份与恢复

备份恢复必须放在 schema 稳定后推进，否则备份格式会在短时间内连续失效。`3.1`、`3.2`、`3.3` 作为一个交付单元实施。

### 第五批：阶段适配与远期协作

- 阶段适配首页
- 月龄指南 / 鼓励文案 / 成长回忆
- 多看护人交接
- 多设备同步方案预研

这部分更偏体验升级，不能阻塞前面高频记录与数据安全能力。

---

## 测试与验收要求

以下要求适用于所有实现 PR，而不只是最终发版前手工验证。

### 1. 自动化测试

- 新增纯逻辑组件必须有单元测试：
  - `SmartDefaultsProvider`
  - `TrendAnalyzer`
  - `StageReportGenerator`
  - `MedicalSummaryGenerator`
  - `HandoverSummaryGenerator`
- 数据库版本升级必须更新迁移测试，覆盖从旧 schema 到最新 schema 的升级路径。
- 导入/恢复/批量补录必须至少覆盖一组重复记录去重场景。

### 2. 手工验证清单

- 前台记录、Quick Record Sheet、Widget 快捷入口、通知栏快捷操作都能落到正确表单或正确写库。
- Android 13+ 首次安装时，在未授权通知权限情况下不崩溃，并能正确提示。
- 设备重启、时区切换、应用升级后，疫苗提醒与新增提醒能恢复。
- 备份文件可在同机恢复，也可在清空数据后的新安装实例中恢复。
- 删除带图片的记录后，本地附件能同步清理；恢复备份后图片路径仍有效。

### 3. Phase 完成判定

- Phase 1 完成标准：高频记录入口明显减少点击步骤，且不新增 schema 风险。
- Phase 2 完成标准：首页或记录页至少能展示可解释的分析结果，而非单纯堆数字。
- Phase 3 完成标准：用户可在无开发者协助的情况下完成导出、恢复、自动备份。
- Phase 4 完成标准：新增情感化能力不影响主记录路径，也不引入明显打扰。
- Phase 5 完成标准：先完成本地“看护人标记 + 交接摘要”，同步能力只要求方案可评审，不要求当期上线。

---

## 风险与回滚策略

### 1. 最大风险

- `v3 → v4` 字段集中升级范围较大，最容易引入迁移遗漏或旧数据默认值错误。
- Widget 直接写库若处理不当，可能与前台页面并发写入时出现体验不一致。
- 备份恢复同时覆盖数据库与附件目录，最怕部分成功、部分失败导致数据半恢复状态。
- 多类提醒并存后，最容易出现重复通知、失效通知或开关状态不一致。

### 2. 对应缓解策略

- 数据库新增字段一律要求默认值、迁移测试、旧数据打开验证三件套一起提交。
- Widget 的一键写库只允许做原子、最小字段写入，复杂规则仍放回 App 前台完成。
- 恢复流程采用“先解包校验，后切换替换”的两阶段策略；未通过校验时禁止覆盖现有数据。
- 所有通知调度统一收口到调度器层，禁止页面直接发通知。

### 3. 回滚原则

- 若某个体验功能影响高频记录主链路，应优先下掉入口或降级为只读提示，不在主流程上硬扛。
- 若某次数据库升级已发版且发现问题，优先补迁移修复，不允许通过清库规避。
- 若自动备份稳定性不足，可先保留手动备份/恢复并关闭自动备份入口，不影响用户已有数据。

---

## 非目标 / 明确不做

以下内容即使在 update plan 中被提到，也不属于本轮必须交付范围：

- 不接入任何开发者自建云服务，不做登录、账号体系、远程同步后端。
- 不在本轮把单模块工程重构成多模块，也不为了“架构整洁”先迁移到 Hilt/DataStore。
- 不引入 AI 识别、拍照自动判便、医学诊断或带医疗建议性质的能力。
- 不覆盖 iOS、平板大屏专属布局、Wear OS、Web 端等跨平台扩展。
- 不做多宝宝支持；当前 spec 默认仍围绕单宝宝数据模型演进。
- 不引入需要长期维护的三方图表、动画、同步 SDK，除非现有 Compose/AndroidX 能力无法满足。
- 不承诺 Phase 5 的“多设备自动同步”在本轮上线；当前只要求输出可评审方案与本地协作能力。
