# 库存岸桌版 / Android 进销存与考勤管理

> **[📥 点击下载最新 Android 安装包 (APK)](https://github.com/buvgh/kucunanzhuoban/raw/main/app-release.apk)**

这是一个基于 **Kotlin + Jetpack Compose + Room** 的原生 Android 项目，当前定位是离线使用的项目制进销存与考勤记录工具。

## 当前能力

- 项目管理：新增项目、删除项目、按项目查看总览
- 每日记录：按日期录入入库、出库、费用、付款、现场照片
- 入库优化：品名历史记忆、模糊匹配、自动补全、单价允许为空
- 考勤管理：
  - 名单只在当前项目内生效，不串到别的项目
  - 支持计时、计天两种模式
  - 支持一天多段计时
  - 录入考勤时可手动填写本次时薪/日薪，并自动记住为该项目人员的下次默认值
  - 历史工资按考勤记录快照保存，后续调价不会影响已录入历史数据
  - 月度考勤看板支持按项目展示、底部汇总、CSV/PDF 导出
- 汇总模块：可选择多个已创建项目组成一个“汇总组”，实时合并数据展示
- 数据工具：数据库备份/恢复、CSV/PDF 导出

## 主要模块

- `app/src/main/java/com/example/myapplication111/data/DockNoteData.kt`
  - Room `Entity / DAO / Database / Repository`
- `app/src/main/java/com/example/myapplication111/ui/DockNoteApp.kt`
  - 主要 Compose 页面、导航、弹窗与业务交互
- `app/src/main/java/com/example/myapplication111/ui/AttendanceBoardScreen.kt`
  - 考勤看板二维表格 UI
- `app/src/main/java/com/example/myapplication111/ui/DockNoteViewModel.kt`
  - ViewModel 与备份触发
- `app/src/main/java/com/example/myapplication111/util/ExportManager.kt`
  - CSV / PDF 导出

## 当前考勤设计

### 人员表 `workers`

- `id`
- `projectId`
- `name`
- `salaryMode`
- `hourlyRate`
- `dailyRate`

说明：

- 人员名单按 `projectId` 隔离
- `hourlyRate / dailyRate` 表示该项目该人员“最近一次录入时默认使用的工资”

### 考勤表 `attendance_records`

- `id`
- `projectId`
- `workerId`
- `date`
- `startTime`
- `endTime`
- `workHours`
- `isPresent`
- `hourlyRateSnapshot`
- `dailyRateSnapshot`

说明：

- 允许同一人同一天录入多条记录
- 月工资汇总按 `hourlyRateSnapshot / dailyRateSnapshot` 计算
- 修改人员默认工资不会回改历史考勤工资

## 首页与导航

底部固定 3 个入口：

- `库存`
- `汇总`
- `考勤`

中间内容区域可滚动，方便后续继续扩展模块。

## 调试说明

### 构建

```bash
./gradlew :app:compileDebugKotlin
```

### 安装调试版

```bash
./gradlew :app:installDebug
```

当前调试版使用独立 debug 包名，不覆盖正式版数据。

## 最近这版完成内容

- 项目内考勤名单隔离
- 录考勤时支持手动输入本次时薪/日薪
- 工资默认值自动记忆
- 历史工资快照化，汇总与导出按历史记录计算
- README 补充，便于换设备继续开发

## 后续建议

如果下一台设备继续开发，建议优先做下面几项：

1. 给“新增考勤人员”补编辑功能
2. 给考勤看板增加月份切换器
3. 把 `DockNoteData.kt` 按 `entity / dao / repository` 拆文件，降低维护成本
4. 增加基础 UI 测试与 Room 迁移测试

