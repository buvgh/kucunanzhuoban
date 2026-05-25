package com.example.myapplication111.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.myapplication111.data.AttendanceDashboardRow
import com.example.myapplication111.data.DayDetailUi
import com.example.myapplication111.data.SalaryMode
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.time.YearMonth
import java.util.Date
import java.util.Locale

object ExportManager {
    fun exportAttendanceBoardToCsv(
        context: Context,
        projectName: String,
        month: String,
        rows: List<AttendanceDashboardRow>,
    ) {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val file = File(context.cacheDir, "Attendance_${projectName}_${month}_$timestamp.csv")
        val days = (1..YearMonth.parse(month).lengthOfMonth()).toList()

        try {
            FileOutputStream(file).use { output ->
                output.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
                val writer = output.bufferedWriter()
                writer.write("项目,$projectName\n")
                writer.write("月份,$month\n\n")
                writer.write(("员工,计薪模式," + days.joinToString(",") { "${it}日" } + ",合计,工资\n"))

                rows.forEach { row ->
                    val summary = row.summary
                    val modeText = if (summary.salaryMode == SalaryMode.HOURLY) "计时" else "计天"
                    val cells = days.joinToString(",") { day ->
                        val cell = row.cellsByDay[day]
                        when {
                            cell == null -> ""
                            summary.salaryMode == SalaryMode.HOURLY && cell.totalWorkHours > 0.0 -> "${fmtInt(cell.totalWorkHours)}h"
                            summary.salaryMode == SalaryMode.DAILY && cell.isPresent -> "出勤"
                            else -> ""
                        }
                    }
                    val total = if (summary.salaryMode == SalaryMode.HOURLY) {
                        "${fmtInt(summary.totalWorkHours)}h"
                    } else {
                        "${summary.totalPresentDays}天"
                    }
                    writer.write("${summary.workerName},$modeText,$cells,$total,${fmtInt(summary.totalSalary)}\n")
                }

                writer.write("\n汇总\n")
                writer.write("总工时,${fmtInt(rows.sumOf { it.summary.totalWorkHours })}h\n")
                writer.write("总天数,${rows.sumOf { it.summary.totalPresentDays }}天\n")
                writer.write("总工资,${fmtInt(rows.sumOf { it.summary.totalSalary })}\n")
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
            textSize = 18f
            isFakeBoldText = true
            color = Color.BLACK
        }
        val textPaint = Paint().apply {
            textSize = 8f
            color = Color.BLACK
        }
        val boldPaint = Paint(textPaint).apply { isFakeBoldText = true }
        val linePaint = Paint().apply {
            strokeWidth = 0.5f
            color = Color.LTGRAY
        }
        val headerBgPaint = Paint().apply { color = Color.rgb(245, 245, 245) }

        val margin = 24f
        val days = (1..YearMonth.parse(month).lengthOfMonth()).toList()
        val pageWidth = 842
        val pageHeight = 595
        val nameWidth = 70f
        val totalWidth = 52f
        val salaryWidth = 58f
        val dayWidth = ((pageWidth - margin * 2 - nameWidth - totalWidth - salaryWidth) / days.size).coerceAtLeast(16f)
        val rowHeight = 18f
        val titleY = 34f
        val headerY = 52f
        val firstRowY = headerY + rowHeight
        val footerReserved = 48f
        val rowsPerPage = ((pageHeight - firstRowY - footerReserved) / rowHeight).toInt().coerceAtLeast(1)
        val rowPages = if (rows.isEmpty()) listOf(emptyList()) else rows.chunked(rowsPerPage)

        rowPages.forEachIndexed { pageIndex, pageRows ->
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageIndex + 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas
            var y = titleY

            canvas.drawText("考勤表  $projectName  $month", margin, y, titlePaint)
            val pageText = "第 ${pageIndex + 1}/${rowPages.size} 页"
            canvas.drawText(pageText, pageWidth - margin - textPaint.measureText(pageText), y, textPaint)

            y = headerY
            canvas.drawRect(margin, y, pageWidth - margin, y + rowHeight, headerBgPaint)
            canvas.drawText("员工", margin + 3f, y + 12f, boldPaint)
            var x = margin + nameWidth
            days.forEach { day ->
                canvas.drawText(day.toString(), x + 2f, y + 12f, boldPaint)
                x += dayWidth
            }
            canvas.drawText("合计", x + 3f, y + 12f, boldPaint)
            canvas.drawText("工资", x + totalWidth + 3f, y + 12f, boldPaint)
            y += rowHeight

            pageRows.forEach { row ->
                x = margin
                canvas.drawText(row.summary.workerName.take(6), x + 3f, y + 12f, textPaint)
                x += nameWidth
                days.forEach { day ->
                    val cell = row.cellsByDay[day]
                    val value = when {
                        cell == null -> ""
                        row.summary.salaryMode == SalaryMode.HOURLY && cell.totalWorkHours > 0.0 -> fmtInt(cell.totalWorkHours)
                        row.summary.salaryMode == SalaryMode.DAILY && cell.isPresent -> "√"
                        else -> ""
                    }
                    canvas.drawText(value, x + 2f, y + 12f, textPaint)
                    x += dayWidth
                }
                val total = if (row.summary.salaryMode == SalaryMode.HOURLY) "${fmtInt(row.summary.totalWorkHours)}h" else "${row.summary.totalPresentDays}天"
                canvas.drawText(total, x + 3f, y + 12f, textPaint)
                canvas.drawText(fmtInt(row.summary.totalSalary), x + totalWidth + 3f, y + 12f, textPaint)
                canvas.drawLine(margin, y + rowHeight, pageWidth - margin, y + rowHeight, linePaint)
                y += rowHeight
            }

            if (pageIndex == rowPages.lastIndex) {
                y = (y + 16f).coerceAtMost(pageHeight - 28f)
                canvas.drawText(
                    "汇总: 工时 ${fmtInt(rows.sumOf { it.summary.totalWorkHours })}h   天数 ${rows.sumOf { it.summary.totalPresentDays }}天   工资 ${fmtInt(rows.sumOf { it.summary.totalSalary })}",
                    margin,
                    y,
                    boldPaint
                )
            }

            pdfDocument.finishPage(page)
        }

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val file = File(context.cacheDir, "Attendance_${projectName}_${month}_$timestamp.pdf")
        try {
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()
            shareFile(context, file, "application/pdf")
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
            shareFile(context, file, "application/pdf")
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
}
