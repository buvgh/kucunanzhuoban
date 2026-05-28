package com.example.myapplication111.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.myapplication111.data.AttendanceDashboardRow
import com.example.myapplication111.data.AttendanceDayCell
import com.example.myapplication111.data.AttendanceEntity
import com.example.myapplication111.data.SalaryMode
import java.time.YearMonth
import java.util.Locale

@Composable
fun AttendanceBoardScreen(
    month: String,
    rows: List<AttendanceDashboardRow>,
    modifier: Modifier = Modifier,
    enableVerticalScroll: Boolean = true,
    onAddRecord: (workerId: Long, workerName: String, date: String) -> Unit = { _, _, _ -> },
    onEditRecord: (workerId: Long, workerName: String, record: AttendanceEntity) -> Unit = { _, _, _ -> },
    onDeleteRecord: (AttendanceEntity) -> Unit = {},
) {
    val days = remember(month) { (1..YearMonth.parse(month).lengthOfMonth()).toList() }
    val configuration = LocalConfiguration.current
    val compactWidth = configuration.screenWidthDp < 390
    val nameColumnWidth = if (compactWidth) 88.dp else 104.dp
    val dayColumnWidth = if (compactWidth) 46.dp else 52.dp
    val summaryColumnWidth = if (compactWidth) 78.dp else 92.dp
    val horizontalScrollState = rememberScrollState()
    val verticalScrollState = rememberScrollState()
    val totalHours = rows.sumOf { it.summary.totalWorkHours }
    val totalDays = rows.sumOf { it.summary.totalPresentDays }
    val totalSalary = rows.sumOf { it.summary.totalSalary }
    var selectedDetail by remember { mutableStateOf<AttendanceCellDetail?>(null) }
    val scrollModifier = if (enableVerticalScroll) {
        Modifier.verticalScroll(verticalScrollState)
    } else {
        Modifier
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(scrollModifier),
    ) {
        Column {
            AttendanceHeaderCell("人员", width = nameColumnWidth)
            Column(
                modifier = if (enableVerticalScroll) {
                    Modifier
                } else {
                    Modifier.heightIn(max = if (compactWidth) 430.dp else 520.dp).verticalScroll(verticalScrollState)
                },
            ) {
                rows.forEach { row ->
                    AttendanceNameCell(
                        name = row.summary.workerName,
                        width = nameColumnWidth,
                    )
                }
                AttendanceHeaderCell("总计", width = nameColumnWidth)
            }
        }

        Column {
            Row(modifier = Modifier.horizontalScroll(horizontalScrollState)) {
                days.forEach { day ->
                    AttendanceHeaderCell("${day}", width = dayColumnWidth)
                }
                AttendanceHeaderCell("合计", width = summaryColumnWidth)
                AttendanceHeaderCell("工资", width = summaryColumnWidth)
            }
            Column(
                modifier = if (enableVerticalScroll) {
                    Modifier
                } else {
                    Modifier.heightIn(max = if (compactWidth) 430.dp else 520.dp).verticalScroll(verticalScrollState)
                }.horizontalScroll(horizontalScrollState),
            ) {
                rows.forEach { row ->
                    Row {
                        days.forEach { day ->
                            AttendanceDayCell(
                                cell = row.cellsByDay[day],
                                width = dayColumnWidth,
                                onOpenDetail = { cell ->
                                    val date = "$month-${day.toString().padStart(2, '0')}"
                                    if (cell == null || cell.records.isEmpty()) {
                                        onAddRecord(row.summary.workerId, row.summary.workerName, date)
                                    } else {
                                        selectedDetail = AttendanceCellDetail(
                                            workerId = row.summary.workerId,
                                            workerName = row.summary.workerName,
                                            day = day,
                                            cell = cell,
                                        )
                                    }
                                },
                            )
                        }
                        AttendanceBodyCell(
                            text = formatSummaryValue(row.summary.totalWorkHours, row.summary.totalPresentDays),
                            width = summaryColumnWidth,
                            fontWeight = FontWeight.Bold,
                        )
                        AttendanceBodyCell(
                            text = "¥${formatAttendanceNumber(row.summary.totalSalary)}",
                            width = summaryColumnWidth,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
                Row {
                    days.forEach { _ ->
                        AttendanceBodyCell("", width = dayColumnWidth)
                    }
                    AttendanceBodyCell(
                        text = "${formatAttendanceNumber(totalHours)}h / ${formatAttendanceDayCount(totalDays)}天",
                        width = summaryColumnWidth,
                        fontWeight = FontWeight.Bold,
                    )
                    AttendanceBodyCell(
                        text = "¥${formatAttendanceNumber(totalSalary)}",
                        width = summaryColumnWidth,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }

    selectedDetail?.let { detail ->
        AlertDialog(
            onDismissRequest = { selectedDetail = null },
            title = { Text("${detail.workerName} · ${detail.day}日") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (detail.cell.records.isEmpty()) {
                        Text("暂无考勤记录")
                    } else {
                        detail.cell.records.forEachIndexed { index, record ->
                            val text = if (record.startTime != null && record.endTime != null) {
                                buildString {
                                    append("${index + 1}. ${record.startTime} - ${record.endTime}  ${formatAttendanceNumber(record.workHours)}h")
                                    if (record.overtimeHours > 0.0 && record.overtimeRate > 0.0) {
                                        append("  +加班 ${formatAttendanceNumber(record.overtimeHours)}h×¥${formatAttendanceNumber(record.overtimeRate)}")
                                    }
                                }
                            } else {
                                buildString {
                                    append("${index + 1}. ${attendanceRecordStatusText(record)}")
                                    if (record.overtimeHours > 0.0 && record.overtimeRate > 0.0) {
                                        append("  +加班 ${formatAttendanceNumber(record.overtimeHours)}h×¥${formatAttendanceNumber(record.overtimeRate)}")
                                    }
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                                TextButton(
                                    onClick = {
                                        selectedDetail = null
                                        onEditRecord(detail.workerId, detail.workerName, record)
                                    },
                                ) {
                                    Text("修改")
                                }
                                TextButton(
                                    onClick = {
                                        selectedDetail = null
                                        onDeleteRecord(record)
                                    },
                                ) {
                                    Text("删除")
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val date = "$month-${detail.day.toString().padStart(2, '0')}"
                        selectedDetail = null
                        onAddRecord(detail.workerId, detail.workerName, date)
                    },
                ) {
                    Text("再加一条")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedDetail = null }) {
                    Text("关闭")
                }
            },
        )
    }
}

@Composable
private fun AttendanceDayCell(
    cell: AttendanceDayCell?,
    width: Dp,
    onOpenDetail: (AttendanceDayCell?) -> Unit,
) {
    val text = when {
        cell == null -> ""
        cell.totalWorkHours > 0.0 -> {
            val suffix = if (cell.segmentCount > 1) "(${cell.segmentCount})" else ""
            "${formatAttendanceNumber(cell.totalWorkHours)}h$suffix"
        }
        cell.attendancePortion in 0.49..0.51 -> "✓/"
        cell.isPresent -> "✓"
        else -> ""
    }
    AttendanceBodyCell(
        text = text,
        width = width,
        backgroundColor = resolveAttendanceCellColor(
            colorInt = cell?.color ?: 0,
            defaultColor = if (cell != null && (cell.totalWorkHours > 0.0 || cell.isPresent)) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
            } else {
                MaterialTheme.colorScheme.surface
            },
        ),
        onClick = { onOpenDetail(cell) },
    )
}

private data class AttendanceCellDetail(
    val workerId: Long,
    val workerName: String,
    val day: Int,
    val cell: AttendanceDayCell,
)

@Composable
private fun AttendanceNameCell(
    name: String,
    width: Dp,
) {
    Column(
        modifier = cellModifier(width = width)
            .defaultMinSize(minHeight = 52.dp)
            .background(MaterialTheme.colorScheme.surface),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = name,
            modifier = Modifier.padding(horizontal = 8.dp),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
        )
    }
}

@Composable
private fun AttendanceHeaderCell(
    text: String,
    width: Dp = 52.dp,
) {
    Box(
        modifier = cellModifier(width)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .defaultMinSize(minHeight = 40.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun AttendanceBodyCell(
    text: String,
    width: Dp = 52.dp,
    fontWeight: FontWeight = FontWeight.Normal,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    onClick: (() -> Unit)? = null,
) {
    val clickableModifier = if (onClick == null) Modifier else Modifier.clickable(onClick = onClick)
    Box(
        modifier = cellModifier(width)
            .defaultMinSize(minHeight = 52.dp)
            .background(backgroundColor)
            .then(clickableModifier),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 4.dp),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = fontWeight,
            textAlign = TextAlign.Center,
            maxLines = 2,
        )
    }
}

private fun cellModifier(width: Dp): Modifier {
    return Modifier
        .width(width)
        .border(0.5.dp, Color.LightGray)
}

private fun formatAttendanceNumber(value: Double): String {
    return if (value % 1.0 == 0.0) {
        String.format(Locale.getDefault(), "%.0f", value)
    } else {
        String.format(Locale.getDefault(), "%.1f", value)
    }
}

private fun formatSummaryValue(totalHours: Double, totalDays: Double): String {
    return when {
        totalHours > 0.0 && totalDays > 0 -> "${formatAttendanceNumber(totalHours)}h/${formatAttendanceDayCount(totalDays)}天"
        totalHours > 0.0 -> "${formatAttendanceNumber(totalHours)}h"
        totalDays > 0 -> "${formatAttendanceDayCount(totalDays)}天"
        else -> ""
    }
}

private fun formatAttendanceDayCount(value: Double): String {
    return if (value % 1.0 == 0.0) {
        String.format(Locale.getDefault(), "%.0f", value)
    } else {
        String.format(Locale.getDefault(), "%.1f", value)
    }
}

private fun attendanceRecordStatusText(record: AttendanceEntity): String {
    return when {
        !record.isPresent -> "未出勤"
        record.attendancePortion in 0.49..0.51 -> "半天出勤"
        else -> "已出勤"
    }
}
