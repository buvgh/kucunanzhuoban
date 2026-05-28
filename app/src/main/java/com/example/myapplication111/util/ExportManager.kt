package com.example.myapplication111.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.myapplication111.MainActivity
import com.example.myapplication111.data.AttendanceDashboardRow
import com.example.myapplication111.data.DayDetailUi
import com.example.myapplication111.data.FundProjectExportUi
import com.example.myapplication111.data.FundRecordEntity
import com.example.myapplication111.data.FundRecordType
import com.example.myapplication111.data.SalaryMode
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.time.YearMonth
import java.util.Date
import java.util.Locale

object ExportManager {
    fun exportFundProjectToCsv(
        context: Context,
        detail: FundProjectExportUi,
    ) {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val file = File(context.cacheDir, "Fund_${detail.name}_$timestamp.csv")
        try {
            FileOutputStream(file).use { output ->
                output.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
                val writer = output.bufferedWriter()
                writeCsvRow(writer, listOf("资金项目", detail.name))
                writeCsvRow(writer, listOf("导出时间", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())))
                writer.newLine()

                writeCsvRow(writer, listOf("项目汇总"))
                writeCsvRow(writer, listOf("初始资金", "收到资金", "支出资金", "当前余额"))
                writeCsvRow(
                    writer,
                    listOf(
                        fmtMoney(detail.totals.initial),
                        fmtMoney(detail.totals.income),
                        fmtMoney(detail.totals.expense),
                        fmtMoney(detail.totals.balance),
                    ),
                )
                writer.newLine()

                writeCsvRow(writer, listOf("按日期汇总"))
                writeCsvRow(writer, listOf("日期", "初始资金", "收到资金", "支出资金", "余额"))
                detail.dates.forEach { date ->
                    writeCsvRow(
                        writer,
                        listOf(
                            date.date,
                            fmtMoney(date.totals.initial),
                            fmtMoney(date.totals.income),
                            fmtMoney(date.totals.expense),
                            fmtMoney(date.totals.balance),
                        ),
                    )
                }
                writer.newLine()

                writeCsvRow(writer, listOf("资金明细"))
                writeCsvRow(writer, listOf("日期", "类型", "名称", "金额", "备注"))
                detail.dates.forEach { date ->
                    val records = orderedFundRecords(date.initialRecords, date.incomeRecords, date.expenseRecords)
                    records.forEach { record ->
                        writeCsvRow(
                            writer,
                            listOf(
                                date.date,
                                fundTypeLabel(record.type),
                                record.name,
                                fmtMoney(record.amount),
                                record.remark,
                            ),
                        )
                    }
                }
                writer.flush()
            }
            shareFile(context, file, "text/csv")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun exportFundProjectToPdf(
        context: Context,
        detail: FundProjectExportUi,
    ) {
        val pdfDocument = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        val margin = 32f
        val contentWidth = pageWidth - margin * 2
        val titlePaint = Paint().apply {
            textSize = 18f
            isFakeBoldText = true
            color = Color.BLACK
        }
        val headerPaint = Paint().apply {
            textSize = 12f
            isFakeBoldText = true
            color = Color.BLACK
        }
        val textPaint = Paint().apply {
            textSize = 9.5f
            color = Color.BLACK
            isAntiAlias = true
        }
        val boldTextPaint = Paint(textPaint).apply { isFakeBoldText = true }
        val smallPaint = Paint(textPaint).apply {
            textSize = 8.5f
            color = Color.DKGRAY
        }
        val linePaint = Paint().apply {
            color = Color.rgb(185, 185, 185)
            strokeWidth = 0.8f
            style = Paint.Style.STROKE
        }
        val headerBgPaint = Paint().apply {
            color = Color.rgb(244, 244, 244)
            style = Paint.Style.FILL
        }

        var pageNumber = 1
        var page = pdfDocument.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
        var canvas = page.canvas
        var y = drawFundPdfHeader(canvas, margin, titlePaint, headerPaint, textPaint, detail.name, pageNumber)

        fun startNewPage(sectionTitle: String? = null) {
            pdfDocument.finishPage(page)
            pageNumber += 1
            page = pdfDocument.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
            canvas = page.canvas
            y = drawFundPdfHeader(canvas, margin, titlePaint, headerPaint, textPaint, detail.name, pageNumber)
            if (sectionTitle != null) {
                canvas.drawText(sectionTitle, margin, y, headerPaint)
                y += 16f
            }
        }

        canvas.drawText("项目汇总", margin, y, headerPaint)
        y += 8f
        y = drawFundMetricGrid(
            canvas = canvas,
            startY = y,
            margin = margin,
            contentWidth = contentWidth,
            labels = listOf("初始资金", "收到资金", "支出资金", "当前余额"),
            values = listOf(
                fmtMoney(detail.totals.initial),
                fmtMoney(detail.totals.income),
                fmtMoney(detail.totals.expense),
                fmtMoney(detail.totals.balance),
            ),
            labelPaint = smallPaint,
            valuePaint = boldTextPaint,
            bgPaint = headerBgPaint,
        )
        y += 18f

        val initialRecords = detail.dates.flatMap { date ->
            date.initialRecords.map { record -> date.date to record }
        }
        if (initialRecords.isNotEmpty()) {
            canvas.drawText("初始资金", margin, y, headerPaint)
            y += 10f
            val initialCols = listOf(100f, 180f, 90f, contentWidth - 100f - 180f - 90f)
            fun drawInitialHeader() {
                drawFundTableRow(
                    canvas = canvas,
                    startX = margin,
                    startY = y,
                    rowHeight = 22f,
                    widths = initialCols,
                    values = listOf("日期", "名称", "金额", "备注"),
                    textPaint = boldTextPaint,
                    linePaint = linePaint,
                    bgPaint = headerBgPaint,
                )
                y += 22f
            }
            drawInitialHeader()
            initialRecords.forEach { (date, record) ->
                val rowHeight = calculateFundRowHeight(record.remark, initialCols.last(), textPaint)
                if (y + rowHeight > pageHeight - 54f) {
                    startNewPage("初始资金")
                    drawInitialHeader()
                }
                drawFundTableRow(
                    canvas = canvas,
                    startX = margin,
                    startY = y,
                    rowHeight = rowHeight,
                    widths = initialCols,
                    values = listOf(date, record.name, fmtMoney(record.amount), record.remark.ifBlank { "-" }),
                    textPaint = textPaint,
                    linePaint = linePaint,
                    bgPaint = null,
                )
                y += rowHeight
            }
            y += 18f
        }

        val incomeRecords = detail.dates.flatMap { date -> date.incomeRecords.map { date.date to it } }
        val expenseRecords = detail.dates.flatMap { date -> date.expenseRecords.map { date.date to it } }
        if (incomeRecords.isNotEmpty() || expenseRecords.isNotEmpty()) {
            if (y > pageHeight - 260f) {
                startNewPage()
            }
            canvas.drawText("资金明细", margin, y, headerPaint)
            y += 10f
            val gap = 12f
            val columnWidth = (contentWidth - gap) / 2f
            val incomeCols = listOf(66f, 96f, 58f, columnWidth - 66f - 96f - 58f)
            val expenseCols = listOf(66f, 96f, 58f, columnWidth - 66f - 96f - 58f)
            val titleHeight = 18f
            val headerHeight = 20f

            fun drawDualHeader() {
                canvas.drawText("收入明细", margin, y + 12f, boldTextPaint)
                canvas.drawText("支出明细", margin + columnWidth + gap, y + 12f, boldTextPaint)
                y += titleHeight
                drawFundTableRow(
                    canvas = canvas,
                    startX = margin,
                    startY = y,
                    rowHeight = headerHeight,
                    widths = incomeCols,
                    values = listOf("日期", "名称", "金额", "备注"),
                    textPaint = boldTextPaint,
                    linePaint = linePaint,
                    bgPaint = headerBgPaint,
                )
                drawFundTableRow(
                    canvas = canvas,
                    startX = margin + columnWidth + gap,
                    startY = y,
                    rowHeight = headerHeight,
                    widths = expenseCols,
                    values = listOf("日期", "名称", "金额", "备注"),
                    textPaint = boldTextPaint,
                    linePaint = linePaint,
                    bgPaint = headerBgPaint,
                )
                y += headerHeight
            }

            drawDualHeader()
            val maxRows = maxOf(incomeRecords.size, expenseRecords.size)
            repeat(maxRows) { index ->
                val income = incomeRecords.getOrNull(index)
                val expense = expenseRecords.getOrNull(index)
                val incomeHeight = income?.let { calculateFundRowHeight(it.second.remark, incomeCols.last(), textPaint) } ?: 22f
                val expenseHeight = expense?.let { calculateFundRowHeight(it.second.remark, expenseCols.last(), textPaint) } ?: 22f
                val rowHeight = maxOf(incomeHeight, expenseHeight)
                if (y + rowHeight > pageHeight - 54f) {
                    startNewPage("资金明细")
                    drawDualHeader()
                }
                drawFundTableRow(
                    canvas = canvas,
                    startX = margin,
                    startY = y,
                    rowHeight = rowHeight,
                    widths = incomeCols,
                    values = income?.let { (date, record) ->
                        listOf(date, record.name, fmtMoney(record.amount), record.remark.ifBlank { "-" })
                    } ?: listOf("", "", "", ""),
                    textPaint = textPaint,
                    linePaint = linePaint,
                    bgPaint = null,
                )
                drawFundTableRow(
                    canvas = canvas,
                    startX = margin + columnWidth + gap,
                    startY = y,
                    rowHeight = rowHeight,
                    widths = expenseCols,
                    values = expense?.let { (date, record) ->
                        listOf(date, record.name, fmtMoney(record.amount), record.remark.ifBlank { "-" })
                    } ?: listOf("", "", "", ""),
                    textPaint = textPaint,
                    linePaint = linePaint,
                    bgPaint = null,
                )
                y += rowHeight
            }
        }

        pdfDocument.finishPage(page)
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val file = File(context.cacheDir, "Fund_${detail.name}_$timestamp.pdf")
        try {
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()
            openPdfInApp(context, file)
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
        }
    }

    fun exportAttendanceBoardToCsv(
        context: Context,
        projectName: String,
        month: String,
        rows: List<AttendanceDashboardRow>,
    ) {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val file = File(context.cacheDir, "Attendance_${projectName}_${month}_$timestamp.csv")
        val allDays = (1..YearMonth.parse(month).lengthOfMonth()).toList()
        val days = allDays.filter { day -> rows.any { row -> row.cellsByDay[day]?.records?.isNotEmpty() == true } }

        try {
            FileOutputStream(file).use { output ->
                output.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
                val writer = output.bufferedWriter()
                writeCsvRow(writer, listOf("项目", projectName))
                writeCsvRow(writer, listOf("月份", month))
                writer.newLine()

                writeCsvRow(writer, listOf("月度汇总"))
                writeCsvRow(writer, listOf("员工", "计薪模式", "总工时(h)", "总出勤(天)", "总工资(¥)"))
                rows.forEach { row ->
                    val summary = row.summary
                    writeCsvRow(
                        writer,
                        listOf(
                            summary.workerName,
                            salaryModeLabel(summary.salaryMode),
                            fmtInt(summary.totalWorkHours),
                            formatDayCount(summary.totalPresentDays),
                            fmtInt(summary.totalSalary),
                        ),
                    )
                }
                writeCsvRow(
                    writer,
                    listOf(
                        "全表汇总",
                        "",
                        fmtInt(rows.sumOf { it.summary.totalWorkHours }),
                        formatDayCount(rows.sumOf { it.summary.totalPresentDays }),
                        fmtInt(rows.sumOf { it.summary.totalSalary }),
                    ),
                )
                writer.newLine()

                writeCsvRow(writer, listOf("每日看板"))
                writeCsvRow(writer, buildList {
                    add("员工")
                    add("计薪模式")
                    days.forEach { add("${it}日") }
                    add("合计")
                    add("工资(¥)")
                })
                rows.forEach { row ->
                    val summary = row.summary
                    writeCsvRow(
                        writer,
                        buildList {
                            add(summary.workerName)
                            add(salaryModeLabel(summary.salaryMode))
                            days.forEach { day ->
                                val cell = row.cellsByDay[day]
                                add(
                                    when {
                                        cell == null -> ""
                                        summary.salaryMode == SalaryMode.HOURLY && cell.totalWorkHours > 0.0 ->
                                            buildString {
                                                append(fmtInt(cell.totalWorkHours))
                                                append("h")
                                                if (cell.segmentCount > 1) {
                                                    append(" (")
                                                    append(cell.segmentCount)
                                                    append("段)")
                                                }
                                            }

                                        summary.salaryMode == SalaryMode.DAILY && cell.attendancePortion in 0.49..0.51 -> "✓/"
                                        summary.salaryMode == SalaryMode.DAILY && cell.isPresent -> "✓"
                                        else -> ""
                                    },
                                )
                            }
                            add(
                                if (summary.salaryMode == SalaryMode.HOURLY) {
                                    "${fmtInt(summary.totalWorkHours)}h"
                                } else {
                                    "${formatDayCount(summary.totalPresentDays)}天"
                                },
                            )
                            add(fmtInt(summary.totalSalary))
                        },
                    )
                }
                writer.newLine()

                writeCsvRow(writer, listOf("原始考勤明细"))
                writeCsvRow(
                    writer,
                    listOf("员工", "日期", "计薪模式", "序号", "开始时间", "结束时间", "工时(h)", "是否出勤", "时薪(¥)", "日薪(¥)", "加班小时", "加班单价", "加班金额"),
                )
                rows.forEach { row ->
                    val summary = row.summary
                    days.forEach { day ->
                        row.cellsByDay[day]?.records?.forEachIndexed { index, record ->
                            writeCsvRow(
                                writer,
                                listOf(
                                    summary.workerName,
                                    record.date,
                                    salaryModeLabel(record.salaryModeSnapshot),
                                    (index + 1).toString(),
                                    record.startTime ?: "",
                                    record.endTime ?: "",
                                    fmtInt(record.workHours),
                                    attendanceStatusLabel(record),
                                    fmtInt(record.hourlyRateSnapshot),
                                    fmtInt(record.dailyRateSnapshot),
                                    fmtInt(record.overtimeHours),
                                    fmtInt(record.overtimeRate),
                                    fmtInt(record.overtimeHours * record.overtimeRate),
                                ),
                            )
                        }
                    }
                }
                writer.flush()
            }
            shareFile(context, file, "text/csv")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun exportAttendanceBoardToPdf(
        context: Context,
        projectName: String,
        month: String,
        rows: List<AttendanceDashboardRow>,
    ) {
        val pdfDocument = PdfDocument()
        val titlePaint = Paint().apply {
            textSize = 14f
            isFakeBoldText = true
            color = Color.BLACK
        }
        val textPaint = Paint().apply {
            textSize = 7f
            color = Color.BLACK
            isAntiAlias = true
        }
        val boldPaint = Paint(textPaint).apply { isFakeBoldText = true }
        val rightPaint = Paint(textPaint).apply { textAlign = Paint.Align.RIGHT }
        val linePaint = Paint().apply {
            strokeWidth = 0.5f
            color = Color.LTGRAY
        }
        val headerBgPaint = Paint().apply { color = Color.rgb(245, 245, 245) }
        val subHeaderPaint = Paint(textPaint).apply {
            textSize = 8f
            isFakeBoldText = true
        }
        val gridPaint = Paint().apply {
            strokeWidth = 0.8f
            color = Color.rgb(170, 170, 170)
            style = Paint.Style.STROKE
        }

        val margin = 20f
        val days = (1..YearMonth.parse(month).lengthOfMonth()).toList()
        val pageWidth = 842
        val pageHeight = 595
        val nameWidth = 86f
        val totalWidth = 58f
        val salaryWidth = 66f
        val rowHeight = 14f
        val headerHeight = 18f
        val topMetaHeight = 52f
        val footerReserved = 84f
        val dayWidth = ((pageWidth - margin * 2 - nameWidth - totalWidth - salaryWidth) / days.size).coerceAtLeast(16f)
        val rowsPerPage = ((pageHeight - topMetaHeight - headerHeight - footerReserved) / rowHeight).toInt().coerceAtLeast(1)
        val rowPages = if (rows.isEmpty()) listOf(emptyList()) else rows.chunked(rowsPerPage)
        val totalPageCount = rowPages.size

        rowPages.forEachIndexed { rowPageIndex, pageRows ->
            val pageNumber = rowPageIndex + 1
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            canvas.drawText("考勤表", margin, 24f, titlePaint)
            canvas.drawText("项目: $projectName", margin, 40f, subHeaderPaint)
            canvas.drawText("月份: $month", margin + 190f, 40f, subHeaderPaint)
            val pageText = "第 $pageNumber/$totalPageCount 页"
            canvas.drawText(pageText, pageWidth - margin - rightPaint.measureText(pageText), 24f, textPaint)

            var y = topMetaHeight
            canvas.drawRect(margin, y, pageWidth - margin, y + headerHeight, headerBgPaint)
            var x = margin
            canvas.drawText("员工", x + 4f, y + 12f, boldPaint)
            x += nameWidth
            days.forEach { day ->
                canvas.drawText(day.toString(), x + 2f, y + 12f, boldPaint)
                x += dayWidth
            }
            canvas.drawText("合计", x + 3f, y + 12f, boldPaint)
            x += totalWidth
            canvas.drawText("工资", x + 3f, y + 12f, boldPaint)
            drawAttendanceGridRow(
                canvas = canvas,
                startX = margin,
                startY = y,
                rowHeight = headerHeight,
                nameWidth = nameWidth,
                dayWidth = dayWidth,
                dayCount = days.size,
                totalWidth = totalWidth,
                salaryWidth = salaryWidth,
                paint = gridPaint,
            )
            y += headerHeight

            pageRows.forEachIndexed { index, row ->
                if (index % 2 == 0) {
                    canvas.drawRect(margin, y, pageWidth - margin, y + rowHeight, Paint().apply { color = Color.rgb(252, 252, 252) })
                }
                x = margin
                canvas.drawText(row.summary.workerName.take(10), x + 3f, y + 10f, textPaint)
                x += nameWidth
                days.forEach { day ->
                    val cell = row.cellsByDay[day]
                    val value = when {
                        cell == null -> ""
                        row.summary.salaryMode == SalaryMode.HOURLY && cell.totalWorkHours > 0.0 ->
                            buildString {
                                append(fmtInt(cell.totalWorkHours))
                                append("h")
                                if (cell.segmentCount > 1) append("*")
                            }

                        row.summary.salaryMode == SalaryMode.DAILY && cell.attendancePortion in 0.49..0.51 -> "✓/"
                        row.summary.salaryMode == SalaryMode.DAILY && cell.isPresent -> "✓"
                        else -> ""
                    }
                    canvas.drawText(value, x + 1f, y + 10f, textPaint)
                    x += dayWidth
                }
                val total = if (row.summary.salaryMode == SalaryMode.HOURLY) {
                    "${fmtInt(row.summary.totalWorkHours)}h"
                } else {
                    "${formatDayCount(row.summary.totalPresentDays)}天"
                }
                canvas.drawText(total, x + 2f, y + 10f, textPaint)
                x += totalWidth
                canvas.drawText(fmtInt(row.summary.totalSalary), x + 2f, y + 10f, textPaint)
                drawAttendanceGridRow(
                    canvas = canvas,
                    startX = margin,
                    startY = y,
                    rowHeight = rowHeight,
                    nameWidth = nameWidth,
                    dayWidth = dayWidth,
                    dayCount = days.size,
                    totalWidth = totalWidth,
                    salaryWidth = salaryWidth,
                    paint = gridPaint,
                )
                y += rowHeight
            }

            pdfDocument.finishPage(page)
        }

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val file = File(context.cacheDir, "Attendance_${projectName}_${month}_$timestamp.pdf")
        try {
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()
            openPdfInApp(context, file)
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
        }
    }

    fun exportDayDetailToCsv(context: Context, detail: DayDetailUi) {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "Export_${detail.projectName}_${detail.date}_$timestamp.csv"
        val file = File(context.cacheDir, fileName)

        try {
            FileOutputStream(file).use { output ->
                output.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
                val writer = output.bufferedWriter()
                
                writer.write("项目名称,${detail.projectName}\n")
                writer.write("日期,${detail.date}\n\n")

                writer.write("汇总信息\n")
                writer.write("入库总件数,${fmtInt(detail.totals.totalCount)}件\n")
                writer.write("入库总重量,${fmtInt(detail.totals.totalWeight)}斤\n")
                writer.write("入库总果款,¥${fmtInt(detail.totals.totalStorageAmount)}\n")
                writer.write("卖出次果,${fmtInt(detail.totals.totalSecondarySaleWeight)}斤 / ¥${fmtInt(detail.totals.totalSecondarySaleAmount)}\n")
                writer.write("出库总件数,${fmtInt(detail.totals.totalOutboundCount)}件\n")
                writer.write("出库总重量,${fmtInt(detail.totals.totalOutboundWeight)}斤\n")
                writer.write("净库存件数,${fmtInt(detail.totals.netTotalCount)}件\n")
                writer.write("净库存重量,${fmtInt(detail.totals.netTotalWeight)}斤\n")
                writer.write("支出费用合计,¥${fmtInt(detail.totals.totalFee)}\n")
                val dayTotal = detail.totals.totalStorageAmount + detail.totals.totalFee
                writer.write("当日费用合计,¥${fmtInt(dayTotal)}\n\n")

                writer.write("入库清单\n")
                writer.write("品名,件数(件),单重(斤),单价(¥/斤),总重(斤),金额(¥)\n")
                detail.storageRecords.forEach { r ->
                    writer.write("${r.name},${fmtInt(r.count)},${fmtInt(r.weightPerUnit)},¥${fmtInt(r.pricePerWeight)},${fmtInt(r.totalWeight)},¥${fmtInt(r.totalPrice)}\n")
                }
                writer.write("\n")

                writer.write("卖出次果\n")
                writer.write("项目名称,斤数,单价(¥),总额(¥)\n")
                detail.secondarySaleRecords.forEach { r ->
                    writer.write("${r.name},${fmtInt(r.weight)},¥${fmtInt(r.unitPrice)},¥${fmtInt(r.totalAmount)}\n")
                }
                writer.write("\n")

                writer.write("出库清单\n")
                writer.write("品名,件数(件),单重(斤),总重(斤)\n")
                detail.outboundRecords.forEach { r ->
                    writer.write("${r.name},${fmtInt(r.count)},${fmtInt(r.weightPerUnit)},${fmtInt(r.count * r.weightPerUnit)}\n")
                }
                writer.write("\n")

                writer.write("费用支出\n")
                writer.write("费用类型,金额(¥)\n")
                detail.feeRecords.forEach { f ->
                    writer.write("${f.type},¥${fmtInt(f.amount)}\n")
                }
                
                writer.flush()
            }
            shareFile(context, file, "text/csv")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun exportDayDetailToPdf(context: Context, detail: DayDetailUi) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        
        val titlePaint = Paint().apply {
            textSize = 20f
            isFakeBoldText = true
            color = Color.BLACK
        }
        val headerPaint = Paint().apply {
            textSize = 12f
            isFakeBoldText = true
            color = Color.BLACK
        }
        val textPaint = Paint().apply {
            textSize = 10f
            color = Color.BLACK
        }
        val boldTextPaint = Paint().apply {
            textSize = 10f
            isFakeBoldText = true
            color = Color.BLACK
        }
        val rightAlignPaint = Paint().apply {
            textSize = 10f
            color = Color.BLACK
            textAlign = Paint.Align.RIGHT
        }
        val smallTextPaint = Paint().apply {
            textSize = 8f
            color = Color.GRAY
        }
        val linePaint = Paint().apply {
            strokeWidth = 0.5f
            color = Color.LTGRAY
        }
        val tableHeaderBgPaint = Paint().apply {
            color = Color.rgb(245, 245, 245)
        }

        val margin = 40f
        val pageWidth = pageInfo.pageWidth.toFloat()
        var y = 50f

        // 1. 页头：标题在左，项目+日期在右
        canvas.drawText("当日明细报告", margin, y, titlePaint)
        val infoStr = "项目: ${detail.projectName}   日期: ${detail.date}"
        canvas.drawText(infoStr, pageWidth - margin - textPaint.measureText(infoStr), y - 2, textPaint)
        y += 20f
        canvas.drawLine(margin, y, pageWidth - margin, y, linePaint)
        y += 30f

        // 2. 汇总区：指标横向排列
        canvas.drawRect(margin, y - 15, pageWidth - margin, y + 35, tableHeaderBgPaint)
        val labels = listOf("当日件数", "当日重量", "当日果款", "支出费用", "当日合计")
        val values = listOf(
            "${fmtInt(detail.totals.totalCount)}件",
            "${fmtInt(detail.totals.totalWeight)}斤",
            "¥${fmtInt(detail.totals.totalStorageAmount)}",
            "¥${fmtInt(detail.totals.totalFee)}",
            "¥${fmtInt(detail.totals.totalStorageAmount + detail.totals.totalFee)}"
        )
        
        val summaryWidth = (pageWidth - 2 * margin) / labels.size

        labels.forEachIndexed { i, label ->
            val startX = margin + i * summaryWidth
            canvas.drawText(label, startX + 5, y, smallTextPaint)
            val vPaint = if (label.contains("合计")) boldTextPaint else textPaint
            canvas.drawText(values[i], startX + 5, y + 20, vPaint)
        }
        y += 60f

        // 3. 主表：入库清单
        canvas.drawText("入库清单", margin, y, headerPaint)
        y += 10f
        
        // 表头
        canvas.drawRect(margin, y, pageWidth - margin, y + 20, tableHeaderBgPaint)
        val cols = listOf("品名", "件数(件)", "单重(斤)", "单价(¥/斤)", "总重(斤)", "金额(¥)")
        val colWeights = listOf(0.25f, 0.15f, 0.15f, 0.15f, 0.15f, 0.15f)
        var curX = margin
        cols.forEachIndexed { i, col ->
            val w = (pageWidth - 2 * margin) * colWeights[i]
            if (i == 0) {
                canvas.drawText(col, curX + 5, y + 14, boldTextPaint)
            } else {
                canvas.drawText(col, curX + w - 5, y + 14, rightAlignPaint)
            }
            curX += w
        }
        y += 20f

        detail.storageRecords.forEach { r ->
            curX = margin
            val rowData = listOf(
                r.name,
                fmtInt(r.count),
                fmtInt(r.weightPerUnit),
                "¥${fmtInt(r.pricePerWeight)}",
                fmtInt(r.totalWeight),
                "¥${fmtInt(r.totalPrice)}"
            )
            rowData.forEachIndexed { i, data ->
                val w = (pageWidth - 2 * margin) * colWeights[i]
                if (i == 0) {
                    canvas.drawText(data, curX + 5, y + 14, textPaint)
                } else {
                    canvas.drawText(data, curX + w - 5, y + 14, rightAlignPaint)
                }
                curX += w
            }
            y += 20f
            canvas.drawLine(margin, y, pageWidth - margin, y, linePaint)
        }
        y += 40f

        // 4. 底部：左右两栏
        val halfWidth = (pageWidth - 2 * margin - 20) / 2
        
        // 左：出库清单
        var leftY = y
        canvas.drawText("出库清单", margin, leftY, headerPaint)
        leftY += 10f
        canvas.drawRect(margin, leftY, margin + halfWidth, leftY + 20, tableHeaderBgPaint)
        canvas.drawText("品名", margin + 5, leftY + 14, boldTextPaint)
        canvas.drawText("件数", margin + halfWidth * 0.5f - 5, leftY + 14, rightAlignPaint)
        canvas.drawText("总重(斤)", margin + halfWidth - 5, leftY + 14, rightAlignPaint)
        leftY += 20f
        detail.outboundRecords.take(15).forEach { r -> // Limit to keep on one page
            canvas.drawText(r.name, margin + 5, leftY + 14, textPaint)
            canvas.drawText(fmtInt(r.count), margin + halfWidth * 0.5f - 5, leftY + 14, rightAlignPaint)
            canvas.drawText(fmtInt(r.count * r.weightPerUnit), margin + halfWidth - 5, leftY + 14, rightAlignPaint)
            leftY += 20f
            canvas.drawLine(margin, leftY, margin + halfWidth, leftY, linePaint)
        }

        // 右：费用
        var rightY = y
        val rightStartX = margin + halfWidth + 20
        canvas.drawText("费用支出", rightStartX, rightY, headerPaint)
        rightY += 10f
        canvas.drawRect(rightStartX, rightY, pageWidth - margin, rightY + 20, tableHeaderBgPaint)
        canvas.drawText("项目", rightStartX + 5, rightY + 14, boldTextPaint)
        canvas.drawText("金额(¥)", pageWidth - margin - 5, rightY + 14, rightAlignPaint)
        rightY += 20f
        detail.feeRecords.take(15).forEach { f ->
            canvas.drawText(f.type, rightStartX + 5, rightY + 14, textPaint)
            canvas.drawText("¥${fmtInt(f.amount)}", pageWidth - margin - 5, rightY + 14, rightAlignPaint)
            rightY += 20f
            canvas.drawLine(rightStartX, rightY, pageWidth - margin, rightY, linePaint)
        }

        // 5. 签名区 (已移除)

        // 6. 页脚
        val footerY = 820f
        canvas.drawText("导出时间: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}", pageWidth - margin, footerY, smallTextPaint)

        pdfDocument.finishPage(page)

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "Detail_${detail.projectName}_${detail.date}_$timestamp.pdf"
        val file = File(context.cacheDir, fileName)

        try {
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()
            openPdfInApp(context, file)
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
        }
    }

    private fun fmtInt(d: Double): String {
        return if (d % 1.0 == 0.0) {
            String.format(Locale.getDefault(), "%.0f", d)
        } else {
            String.format(Locale.getDefault(), "%.1f", d)
        }
    }

    private fun formatDayCount(value: Double): String = fmtInt(value)

    private fun attendanceStatusLabel(record: com.example.myapplication111.data.AttendanceEntity): String {
        return when {
            !record.isPresent -> ""
            record.attendancePortion in 0.49..0.51 -> "半天"
            else -> "全天"
        }
    }

    private fun fmtMoney(value: Double): String {
        return if (value % 1.0 == 0.0) {
            String.format(Locale.getDefault(), "%.0f", value)
        } else {
            String.format(Locale.getDefault(), "%.2f", value)
        }
    }

    private fun orderedFundRecords(
        initialRecords: List<FundRecordEntity>,
        incomeRecords: List<FundRecordEntity>,
        expenseRecords: List<FundRecordEntity>,
    ): List<FundRecordEntity> {
        return (initialRecords + incomeRecords + expenseRecords).sortedWith(
            compareBy<FundRecordEntity>({ it.type }, { it.createTime }, { it.id }),
        )
    }

    private fun fundTypeLabel(type: Int): String = when (type) {
        FundRecordType.INITIAL -> "初始资金"
        FundRecordType.INCOME -> "收到资金"
        FundRecordType.EXPENSE -> "支出资金"
        else -> "资金记录"
    }

    private fun drawFundPdfHeader(
        canvas: Canvas,
        margin: Float,
        titlePaint: Paint,
        headerPaint: Paint,
        textPaint: Paint,
        projectName: String,
        pageNumber: Int,
    ): Float {
        canvas.drawText("资金汇总表", margin, 28f, titlePaint)
        canvas.drawText("项目: $projectName", margin, 46f, headerPaint)
        val exportTime = "导出: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())}"
        canvas.drawText(exportTime, margin + 220f, 46f, textPaint)
        canvas.drawText("第 $pageNumber 页", 505f, 46f, textPaint)
        return 72f
    }

    private fun drawFundMetricGrid(
        canvas: Canvas,
        startY: Float,
        margin: Float,
        contentWidth: Float,
        labels: List<String>,
        values: List<String>,
        labelPaint: Paint,
        valuePaint: Paint,
        bgPaint: Paint,
    ): Float {
        val cardHeight = 48f
        val cellWidth = contentWidth / labels.size
        labels.forEachIndexed { index, label ->
            val x = margin + index * cellWidth
            canvas.drawRect(x, startY, x + cellWidth - 6f, startY + cardHeight, bgPaint)
            canvas.drawText(label, x + 8f, startY + 16f, labelPaint)
            canvas.drawText(values[index], x + 8f, startY + 34f, valuePaint)
        }
        return startY + cardHeight
    }

    private fun drawFundTableRow(
        canvas: Canvas,
        startX: Float,
        startY: Float,
        rowHeight: Float,
        widths: List<Float>,
        values: List<String>,
        textPaint: Paint,
        linePaint: Paint,
        bgPaint: Paint?,
    ) {
        var x = startX
        if (bgPaint != null) {
            canvas.drawRect(startX, startY, startX + widths.sum(), startY + rowHeight, bgPaint)
        }
        widths.forEachIndexed { index, width ->
            canvas.drawRect(x, startY, x + width, startY + rowHeight, linePaint)
            val text = values.getOrElse(index) { "" }
            if (index == values.lastIndex && text.length > 18) {
                val lines = wrapPdfLines(text, width - 8f, textPaint)
                lines.forEachIndexed { lineIndex, line ->
                    canvas.drawText(line, x + 4f, startY + 12f + lineIndex * 12f, textPaint)
                }
            } else {
                canvas.drawText(text, x + 4f, startY + 12f, textPaint)
            }
            x += width
        }
    }

    private fun calculateFundRowHeight(
        remark: String,
        remarkWidth: Float,
        paint: Paint,
    ): Float {
        val lineCount = wrapPdfText(remark.ifBlank { "-" }, remarkWidth - 8f, paint).coerceAtLeast(1)
        return (14f + (lineCount - 1) * 12f).coerceAtLeast(22f)
    }

    private fun wrapPdfText(text: String, width: Float, paint: Paint): Int {
        return wrapPdfLines(text, width, paint).size
    }

    private fun wrapPdfLines(text: String, width: Float, paint: Paint): List<String> {
        if (text.isBlank()) return listOf("")
        val result = mutableListOf<String>()
        var current = ""
        text.forEach { char ->
            val next = current + char
            if (paint.measureText(next) > width && current.isNotEmpty()) {
                result += current
                current = char.toString()
            } else {
                current = next
            }
        }
        if (current.isNotEmpty()) result += current
        return result
    }

    private fun drawAttendanceGridRow(
        canvas: Canvas,
        startX: Float,
        startY: Float,
        rowHeight: Float,
        nameWidth: Float,
        dayWidth: Float,
        dayCount: Int,
        totalWidth: Float,
        salaryWidth: Float,
        paint: Paint,
    ) {
        val rowWidth = nameWidth + dayWidth * dayCount + totalWidth + salaryWidth
        canvas.drawRect(startX, startY, startX + rowWidth, startY + rowHeight, paint)

        var x = startX + nameWidth
        canvas.drawLine(x, startY, x, startY + rowHeight, paint)
        repeat(dayCount) {
            x += dayWidth
            canvas.drawLine(x, startY, x, startY + rowHeight, paint)
        }
        x += totalWidth
        canvas.drawLine(x, startY, x, startY + rowHeight, paint)
    }

    private fun salaryModeLabel(mode: Int): String = if (mode == SalaryMode.HOURLY) "计时" else "计天"

    private fun salaryModeShortLabel(mode: Int): String = if (mode == SalaryMode.HOURLY) "时" else "天"

    private fun writeCsvRow(writer: java.io.BufferedWriter, values: List<String>) {
        writer.write(values.joinToString(",") { csvEscape(it) })
        writer.newLine()
    }

    private fun csvEscape(value: String): String {
        val escaped = value.replace("\"", "\"\"")
        return if (escaped.any { it == ',' || it == '\n' || it == '\r' || it == '"' }) {
            "\"$escaped\""
        } else {
            escaped
        }
    }

    private fun shareFile(context: Context, file: File, mimeType: String) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "导出数据"))
    }

    private fun openPdfInApp(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )
        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(intent)
    }
}
