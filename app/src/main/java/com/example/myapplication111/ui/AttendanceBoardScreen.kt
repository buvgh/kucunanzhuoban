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
    onAddRecord: (workerId: Long, workerName: String, salaryMode: Int, date: String) -> Unit = { _, _, _, _ -> },
    onEditRecord: (workerId: Long, workerName: String, salaryMode: Int, record: AttendanceEntity) -> Unit = { _, _, _, _ -> },
    onDeleteRecord: (AttendanceEntity) -> Unit = {},
) {
    val days = remember(month) { (1..YearMonth.parse(month).lengthOfMonth()).toList() }
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
            AttendanceHeaderCell("员工姓名", width = 104.dp)
            rows.forEach { row ->
                AttendanceNameCell(
                    name = row.summary.workerName,
                    salaryMode = row.summary.salaryMode,
                )
            }
            AttendanceHeaderCell("总计", width = 104.dp)
        }

        Column(modifier = Modifier.horizontalScroll(horizontalScrollState)) {
            Row {
                days.forEach { day ->
                    AttendanceHeaderCell("${day}日")
                }
                AttendanceHeaderCell("合计", width = 92.dp)
                AttendanceHeaderCell("工资", width = 92.dp)
            }

            rows.forEach { row ->
                Row {
                    days.forEach { day ->
                        AttendanceDayCell(
                            cell = row.cellsByDay[day],
                            salaryMode = row.summary.salaryMode,
                            workerName = row.summary.workerName,
                            day = day,
                            onOpenDetail = { cell ->
                                val date = "$month-${day.toString().padStart(2, '0')}"
                                if (cell == null || cell.records.isEmpty()) {
                                    onAddRecord(row.summary.workerId, row.summary.workerName, row.summary.salaryMode, date)
                                } else {
                                    selectedDetail = AttendanceCellDetail(
                                        workerId = row.summary.workerId,
                                        workerName = row.summary.workerName,
                                        salaryMode = row.summary.salaryMode,
                                        day = day,
                                        cell = cell,
                                    )
                                }
                            },
                        )
                    }
                    AttendanceBodyCell(
                        text = if (row.summary.salaryMode == SalaryMode.HOURLY) {
                            "${formatAttendanceNumber(row.summary.totalWorkHours)}h"
                        } else {
                            "${row.summary.totalPresentDays}天"
                        },
                        width = 92.dp,
                        fontWeight = FontWeight.Bold,
                    )
                    AttendanceBodyCell(
                        text = "¥${formatAttendanceNumber(row.summary.totalSalary)}",
                        width = 92.dp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            Row {
                days.forEach { _ ->
                    AttendanceBodyCell("")
                }
                AttendanceBodyCell(
                    text = "${formatAttendanceNumber(totalHours)}h / ${totalDays}天",
                    width = 92.dp,
                    fontWeight = FontWeight.Bold,
                )
                AttendanceBodyCell(
                    text = "¥${formatAttendanceNumber(totalSalary)}",
                    width = 92.dp,
                    fontWeight = FontWeight.Bold,
                )
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
                                "${index + 1}. ${record.startTime} - ${record.endTime}  ${formatAttendanceNumber(record.workHours)}h"
                            } else {
                                "${index + 1}. ${if (record.isPresent) "已出勤" else "未出勤"}"
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                                TextButton(
                                    onClick = {
                                        selectedDetail = null
                                        onEditRecord(detail.workerId, detail.workerName, detail.salaryMode, record)
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
                        onAddRecord(detail.workerId, detail.workerName, detail.salaryMode, date)
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
    salaryMode: Int,
    workerName: String,
    day: Int,
    onOpenDetail: (AttendanceDayCell?) -> Unit,
) {
    val text = when {
        cell == null -> ""
        salaryMode == SalaryMode.HOURLY && cell.totalWorkHours > 0.0 -> {
            val suffix = if (cell.segmentCount > 1) "(${cell.segmentCount})" else ""
            "${formatAttendanceNumber(cell.totalWorkHours)}h$suffix"
        }
        salaryMode == SalaryMode.DAILY && cell.isPresent -> "✓"
        else -> ""
    }
    AttendanceBodyCell(
        text = text,
        backgroundColor = if (cell != null && (cell.totalWorkHours > 0.0 || cell.isPresent)) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
        } else {
            MaterialTheme.colorScheme.background
        },
        onClick = { onOpenDetail(cell) },
    )
}

private data class AttendanceCellDetail(
    val workerId: Long,
    val workerName: String,
    val salaryMode: Int,
    val day: Int,
    val cell: AttendanceDayCell,
)

@Composable
private fun AttendanceNameCell(
    name: String,
    salaryMode: Int,
) {
    val modeText = if (salaryMode == SalaryMode.HOURLY) "计时" else "计天"
    Column(
        modifier = cellModifier(width = 104.dp)
            .defaultMinSize(minHeight = 52.dp)
            .background(MaterialTheme.colorScheme.surface),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = name,
            modifier = Modifier.padding(horizontal = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
        )
        Text(
            text = modeText,
            modifier = Modifier.padding(horizontal = 8.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
            maxLines = 1,
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
