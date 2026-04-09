# LittleGrow

`LittleGrow` 是一个纯离线的 Android 宝宝成长记录应用，当前仓库已落地为可构建的 Jetpack Compose 工程。

## 已实现

- 宝宝资料：昵称、生日、性别
- 首页概览：月龄/天数、今日喂养/排泄/睡眠摘要、最近记录预览
- 喂养记录：母乳、瓶喂、辅食，支持编辑与删除
- 睡眠记录：开始/结束时间与时长，支持编辑与删除
- 排泄记录：小便/大便、颜色/性状、异常颜色高亮，支持编辑与删除
- 成长记录：体重、身高、头围与本地趋势图，支持编辑与删除
- 疫苗管家：基于生日自动生成国家免疫规划时间表，可标记已接种并发送本地提醒
- 数据导出：在设置页将当前本地数据导出为 CSV 或 PDF
- 时光轴：里程碑记录与“出生第 X 天”换算，支持编辑与删除
- 主题模式：系统 / 浅色 / 深色
- 通知管理：疫苗提醒开关与通知权限衔接
- 本地存储：Room + 轻量偏好存储，带显式数据库迁移与迁移回归测试

## 技术栈

- Android Gradle Plugin `8.13.2`
- Kotlin `2.2.21`
- Jetpack Compose
- Room
- Navigation Compose

## 本地构建

项目已生成 `gradlew`，在 Windows PowerShell 下可直接构建：

```powershell
$env:JAVA_HOME = (Resolve-Path .tooling\jdk\jdk-17.0.18+8).Path
.\gradlew.bat assembleDebug
```

如需完整验证，可继续执行：

```powershell
.\gradlew.bat testDebugUnitTest lintDebug
```

如果需要重新下载或补齐 Android SDK，可使用仓库内的 `.tooling` 目录作为本地工具链根目录。该目录已被 `.gitignore` 排除。

## 运行说明

- Android 13 及以上若开启疫苗提醒，需要授予通知权限。
- 导出 CSV/PDF 时会调用系统文档选择器，文件保存到用户指定位置。
