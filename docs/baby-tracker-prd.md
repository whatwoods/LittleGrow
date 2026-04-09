# 宝宝成长记录 App — 产品需求文档（PRD）

> 项目名称：长呀长 (LittleGrow)  
> 版本：v1.0  
> 最后更新：2026-04-08  
> 状态：草稿

---

## 一、项目概述

### 1.1 产品愿景

为新手父母提供一个简单好用的宝宝成长记录工具，涵盖**日常照护记录、身体发育追踪、里程碑时光轴**三大核心场景，帮助家庭科学记录、轻松回忆宝宝的每一步成长。

### 1.2 目标用户

| 用户角色 | 描述 | 核心诉求 |
|---------|------|---------|
| 主要照护者（妈妈/爸爸） | 25–38 岁，每天高频使用 | 快速记录喂奶、换尿布、睡眠；查看发育趋势 |
| 儿科医生（间接用户） | 复诊时家长展示数据 | 清晰的生长曲线图表，便于截图或导出 |

### 1.3 产品定位

- **平台**：Android 原生（Jetpack Compose + Material 3）
- **语言**：中文（预留 i18n 扩展能力）
- **网络**：纯离线本地应用，无需联网，无需注册账号
- **商业模式**：免费
- **最低支持版本**：Android 8.0（API 26）

---

## 二、信息架构

```
首页（Home）
├── 宝宝概览卡片（头像、姓名、月龄/天数）
├── 关键指标摘要（最新体重/身高、今日喂奶/换尿布次数）
├── 快速记录入口（喂奶 / 换尿布 / 睡眠）
└── 最近里程碑预览

成长（Growth）
├── 生长曲线图（体重/身高/头围，可叠加 WHO 参考线）
├── 记录列表（按时间倒序）
├── 疫苗与体检日程
└── 添加记录表单

时光（Timeline）
├── 里程碑时间轴（带 emoji 图标 + 可选照片）
├── 添加里程碑表单
└── 照片查看（点击放大）

设置（Settings）
├── 宝宝资料编辑（姓名、生日、性别、头像）
├── 数据导出（CSV / PDF）
├── 通知管理
└── 关于 / 反馈
```

---

## 三、功能需求

### 3.1 模块一：极速日常记录

> 设计原则：单手操作、最少点击次数。

#### 3.1.1 饮食记录

| 类型 | 记录字段 | 说明 |
|------|---------|------|
| 母乳 | 开始时间、结束时间、左/右侧 | 支持计时器，自动计算时长 |
| 瓶喂 | 时间、类型（母乳/配方奶）、奶量(ml) | 手动输入奶量 |
| 辅食 | 时间、食材名称、拍照记录、备注 | 支持从相册选取照片 |

#### 3.1.2 作息记录

| 类型 | 记录字段 | 说明 |
|------|---------|------|
| 睡眠 | 开始时间、结束时间 | 自动计算时长，统计每日总睡眠 |
| 日常活动 | 时间、类型（洗澡/腹爬/早教/户外等）、备注 | 支持自定义活动类型 |

#### 3.1.3 排泄记录

| 类型 | 记录字段 | 说明 |
|------|---------|------|
| 小便 | 时间 | 计数即可 |
| 大便 | 时间、颜色、性状 | 提供颜色/性状选项卡；红色、白色便便高亮警示 |

#### 3.1.4 桌面小组件 (Widget)

- 提供 Android 桌面小组件，无需打开 App 即可一键打卡
- 小组件显示：今日喂奶次数、最近一次喂奶距今时长、快捷记录按钮
- 使用 Glance (Jetpack Compose) 实现

### 3.2 模块二：生长发育与健康追踪

#### 3.2.1 生长曲线

- 记录身高(cm)、体重(kg)、头围(cm)
- 采用 **WHO 婴幼儿生长发育标准** 生成百分位曲线图
- 同时内置**中国卫健委标准**参考线，用户可切换
- 图表支持手势缩放、数据点击查看详情

#### 3.2.2 疫苗接种管家

- 内置**中国国家免疫规划**（一类/二类疫苗）时间表
- 根据宝宝出生日期自动计算推荐接种日期
- 支持标记已接种/未接种状态
- 接种前 3 天发送**本地通知**提醒

#### 3.2.3 里程碑打卡

- 预置里程碑清单：大动作（翻身、坐、爬、站、走）、精细动作、语言能力
- 提供不同月龄的参考标准
- 支持拍照记录里程碑瞬间
- 已完成里程碑在时光轴中展示

#### 3.2.4 疾病与用药记录

- 发烧体温记录，支持体温趋势曲线
- 用药记录：药品名称、剂量、时间
- 过敏史记录

### 3.3 模块三：时光轴

- 照片/视频按宝宝"出生第 X 天"自动排列
- 里程碑事件在时光轴中以特殊样式展示
- 支持点击照片全屏查看
- 照片存储在设备本地，App 仅保存引用路径

### 3.4 模块四：设置

- 宝宝资料编辑：姓名、生日、性别、头像
- 数据导出：支持 CSV 和 PDF 格式
- 通知管理：开关各类提醒
- 关于 / 版本信息 / 反馈入口

---

## 四、数据模型

### 4.1 核心实体

```
Baby（宝宝）
├── id: Long (PK)
├── name: String
├── birthday: LocalDate
├── gender: Enum (MALE / FEMALE)
├── avatarUri: String?
├── createdAt: Instant

FeedingRecord（喂养记录）
├── id: Long (PK)
├── babyId: Long (FK)
├── type: Enum (BREAST_LEFT / BREAST_RIGHT / BOTTLE_BREAST_MILK / BOTTLE_FORMULA / SOLID_FOOD)
├── startTime: Instant
├── endTime: Instant?
├── amountMl: Int?           -- 瓶喂奶量
├── foodName: String?        -- 辅食名称
├── photoUri: String?        -- 辅食照片
├── note: String?

SleepRecord（睡眠记录）
├── id: Long (PK)
├── babyId: Long (FK)
├── startTime: Instant
├── endTime: Instant?

DiaperRecord（排泄记录）
├── id: Long (PK)
├── babyId: Long (FK)
├── time: Instant
├── type: Enum (PEE / POOP)
├── poopColor: Enum? (YELLOW / GREEN / BROWN / RED / WHITE / BLACK)
├── poopTexture: Enum? (LIQUID / SOFT / NORMAL / HARD)
├── note: String?

ActivityRecord（活动记录）
├── id: Long (PK)
├── babyId: Long (FK)
├── time: Instant
├── type: String             -- 洗澡/腹爬/早教/户外等
├── durationMinutes: Int?
├── note: String?

GrowthRecord（生长记录）
├── id: Long (PK)
├── babyId: Long (FK)
├── date: LocalDate
├── weightKg: Float?
├── heightCm: Float?
├── headCircCm: Float?

VaccineRecord（疫苗记录）
├── id: Long (PK)
├── babyId: Long (FK)
├── vaccineName: String
├── doseNumber: Int
├── scheduledDate: LocalDate
├── actualDate: LocalDate?
├── isDone: Boolean

MilestoneRecord（里程碑记录）
├── id: Long (PK)
├── babyId: Long (FK)
├── title: String
├── category: Enum (GROSS_MOTOR / FINE_MOTOR / LANGUAGE / SOCIAL / COGNITIVE)
├── achievedDate: LocalDate
├── photoUri: String?
├── note: String?

MedicalRecord（健康记录）
├── id: Long (PK)
├── babyId: Long (FK)
├── type: Enum (TEMPERATURE / MEDICATION / ALLERGY)
├── time: Instant
├── temperatureCelsius: Float?
├── medicineName: String?
├── dosage: String?
├── allergen: String?
├── note: String?
```

### 4.2 存储方案

- 本地数据库：**Room**（SQLite）
- 图片存储：App 内部存储目录或 MediaStore，数据库仅存 URI 引用
- 偏好设置：DataStore (Preferences)

---

## 五、技术架构

### 5.1 技术栈

| 层级 | 技术选型 | 说明 |
|------|---------|------|
| UI | Jetpack Compose + Material 3 | 声明式 UI |
| 导航 | Navigation Compose | 页面路由 |
| 架构 | MVVM + Clean Architecture | Repository → ViewModel → UI |
| 数据库 | Room | 本地 SQLite ORM |
| 偏好存储 | DataStore | 替代 SharedPreferences |
| 依赖注入 | Hilt | 基于 Dagger 的 DI |
| 图表 | Vico | Compose 原生图表库 |
| 图片加载 | Coil | Compose 友好的图片加载 |
| 小组件 | Glance | Compose 风格的 App Widget |
| 通知 | AlarmManager + NotificationCompat | 本地定时通知 |
| 导出 | Apache POI (CSV) + Android PDF API | 数据导出 |

### 5.2 模块划分

```
:app                    -- Application 入口、Hilt 配置、导航
:core:database          -- Room 数据库、DAO、Entity
:core:datastore         -- DataStore 偏好存储
:core:model             -- 领域模型（跨模块共享）
:core:common            -- 工具类、扩展函数
:feature:home           -- 首页模块
:feature:feeding        -- 喂养记录模块
:feature:sleep          -- 睡眠记录模块
:feature:diaper         -- 排泄记录模块
:feature:growth         -- 生长发育模块
:feature:timeline       -- 时光轴模块
:feature:settings       -- 设置模块
:feature:widget         -- 桌面小组件
```

### 5.3 架构图

```
┌─────────────────────────────────────────────────┐
│                   UI Layer                       │
│  Compose Screen ← State ← ViewModel             │
├─────────────────────────────────────────────────┤
│                 Domain Layer                     │
│  UseCase（可选，复杂业务逻辑时引入）               │
├─────────────────────────────────────────────────┤
│                  Data Layer                      │
│  Repository → Room DAO / DataStore               │
├─────────────────────────────────────────────────┤
│               Local Storage                      │
│  SQLite (Room)  │  Files  │  DataStore           │
└─────────────────────────────────────────────────┘
```

---

## 六、非功能性需求

### 6.1 性能

- 冷启动时间 < 1.5 秒
- 页面切换流畅，无掉帧（目标 60fps）
- 数据库查询响应 < 100ms（常规列表查询）

### 6.2 隐私与安全

- 所有数据仅存储在设备本地，不上传任何服务器
- 无网络权限（manifest 中不声明 INTERNET 权限）
- 照片仅存储引用路径，不复制到 App 私有目录（节省空间）
- 导出文件保存到用户选择的目录（通过 SAF）

### 6.3 离线能力

- 100% 离线可用，所有功能无需网络

### 6.4 可访问性

- 支持 Android 系统字体大小设置
- 关键操作区域最小触摸目标 48dp
- 支持 TalkBack 无障碍

### 6.5 数据可靠性

- Room 数据库使用 WAL 模式，防止写入中断导致数据丢失
- 导出功能作为用户手动备份手段

---

## 七、UI/UX 设计原则

### 7.1 色彩

- 主色调采用**马卡龙/莫兰迪色系**（奶油黄、婴儿蓝、治愈绿）
- 避免高饱和度刺眼颜色，传达温馨、安全感
- 使用 Material 3 Dynamic Color，可跟随系统主题

### 7.2 夜间模式

- **至关重要** — 宝妈经常在深夜喂奶/哄睡时使用
- 完美适配 Dark Mode，降低屏幕亮度对眼睛的刺激
- 深色模式下避免纯白元素

### 7.3 单手操作

- 核心记录按钮（"开始喂奶"、"换尿布"）放置在屏幕**中下部**
- 确保单手抱娃时大拇指可轻松触达
- 常用操作 ≤ 2 次点击完成

### 7.4 情感化设计

- 对未按时记录的用户**不发送焦虑型通知**
- App 语气鼓励性、温暖
- 里程碑完成时给予温馨庆祝动效

---

## 八、MVP 范围与迭代计划

### V1.0 — MVP（当前版本）

| 优先级 | 功能 | 说明 |
|-------|------|------|
| P0 | 宝宝资料管理 | 创建/编辑宝宝信息 |
| P0 | 喂养记录 | 母乳计时、瓶喂、辅食 |
| P0 | 睡眠记录 | 计时器，每日统计 |
| P0 | 排泄记录 | 小便计数、大便颜色/性状 |
| P0 | 首页概览 | 今日摘要卡片、快速入口 |
| P1 | 生长曲线 | WHO 标准百分位图 |
| P1 | 里程碑打卡 | 预置清单 + 拍照 |
| P1 | 时光轴 | 按出生天数排列 |
| P1 | 夜间模式 | 完整 Dark Mode 适配 |
| P2 | 疫苗管家 | 日程 + 本地提醒 |
| P2 | 疾病/用药记录 | 体温曲线、用药记录 |
| P2 | 数据导出 | CSV / PDF |
| P2 | 桌面小组件 | Glance Widget |

### V1.1 — 计划中

- 多宝宝支持
- 中国卫健委生长标准切换
- 活动记录（洗澡/早教等）
- 更多里程碑类别

### V2.0 — 远期

- 数据备份与恢复（本地文件导入导出）
- i18n 多语言支持
- 更丰富的统计报表

---

## 九、WHO 生长标准参考数据

App 需内置以下 WHO 标准数据（JSON 或 SQLite 预填充）：

- 0–5 岁男/女童体重百分位（P3, P15, P50, P85, P97）
- 0–5 岁男/女童身高百分位
- 0–5 岁男/女童头围百分位

数据来源：[WHO Child Growth Standards](https://www.who.int/tools/child-growth-standards)

---

## 十、约束与假设

### 约束

- 纯离线应用，不依赖任何后端服务
- 单一宝宝（V1.0），数据模型预留 babyId 字段以便后续扩展
- 不使用任何 AI / 机器学习功能

### 假设

- 用户设备运行 Android 8.0 及以上
- 用户授予必要权限（通知、相册访问）
- WHO 生长标准数据以静态资源形式打包在 APK 中
