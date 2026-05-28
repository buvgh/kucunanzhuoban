package com.example.myapplication111.ui

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.myapplication111.data.FundDateSummaryUi
import com.example.myapplication111.data.FundDayDetailUi
import com.example.myapplication111.data.FundProjectOverviewUi
import com.example.myapplication111.data.FundProjectSummaryUi
import com.example.myapplication111.data.FundRecordEntity
import com.example.myapplication111.data.FundRecordType
import com.example.myapplication111.data.FundTotals
import java.time.LocalDate
import java.util.Locale

@Composable
fun FundProjectListScreen(
    projects: List<FundProjectSummaryUi>,
    onOpenProject: (Long) -> Unit,
    onDeleteProject: (Long) -> Unit,
) {
    if (projects.isEmpty()) {
        FundEmptyState("还没有资金项目，点右下角先创建一个。")
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(projects, key = { it.id }) { project ->
            FundProjectCard(
                project = project,
                onClick = { onOpenProject(project.id) },
                onDelete = { onDeleteProject(project.id) },
            )
        }
    }
}

@Composable
fun FundProjectOverviewScreen(
    overview: FundProjectOverviewUi,
    onOpenDate: (Long) -> Unit,
    onDeleteProject: () -> Unit,
    onDeleteDate: (Long) -> Unit,
    onExportCsv: () -> Unit,
    onExportPdf: () -> Unit,
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(overview.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text("按日期管理收到资金、支出资金和初始资金", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "删除资金项目", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                    FundTotalsCard(totals = overview.totals, compact = false)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        TextButton(onClick = onExportCsv, enabled = overview.dates.isNotEmpty()) {
                            Icon(Icons.Default.Share, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("导出表格")
                        }
                        TextButton(onClick = onExportPdf, enabled = overview.dates.isNotEmpty()) {
                            Icon(Icons.Default.Share, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("导出PDF")
                        }
                    }
                }
            }
        }
        item {
            Text("按日期汇总", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        if (overview.dates.isEmpty()) {
            item { FundEmptyState("这个资金项目还没有日期记录，点右下角添加日期。") }
        } else {
            items(overview.dates, key = { it.id }) { date ->
                FundDateCard(
                    date = date,
                    onClick = { onOpenDate(date.id) },
                    onDelete = { onDeleteDate(date.id) },
                )
            }
        }
    }

    if (showDeleteConfirm) {
        FundDeleteConfirmDialog(
            title = "删除资金项目",
            message = "确定删除“${overview.name}”吗？里面的日期和资金记录会一起删除。",
            onDismiss = { showDeleteConfirm = false },
            onConfirm = {
                showDeleteConfirm = false
                onDeleteProject()
            },
        )
    }
}

@Composable
fun FundDayDetailScreen(
    detail: FundDayDetailUi,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onEditRecord: (FundRecordEntity) -> Unit,
    onDeleteRecord: (FundRecordEntity) -> Unit,
) {
    val tabs = listOf("汇总", "初始资金", "收到资金", "支出资金")
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FundTotalsCard(totals = detail.totals, compact = false)
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            tabs.forEachIndexed { index, label ->
                FilterChip(
                    selected = selectedTab == index,
                    onClick = { onTabSelected(index) },
                    label = { Text(label) },
                )
            }
        }
        when (selectedTab) {
            0 -> FundSummaryBoard(detail = detail)
            1 -> FundRecordList(records = detail.initialRecords, emptyText = "还没有初始资金记录。", onEditRecord = onEditRecord, onDeleteRecord = onDeleteRecord)
            2 -> FundRecordList(records = detail.incomeRecords, emptyText = "还没有收到资金记录。", onEditRecord = onEditRecord, onDeleteRecord = onDeleteRecord)
            else -> FundRecordList(records = detail.expenseRecords, emptyText = "还没有支出资金记录。", onEditRecord = onEditRecord, onDeleteRecord = onDeleteRecord)
        }
    }
}

@Composable
fun FundProjectDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var name by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("新建资金项目") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("例如：张三工程资金、5月周转资金") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
            )
        },
        confirmButton = {
            Button(onClick = { onConfirm(name) }, enabled = name.isNotBlank(), shape = RoundedCornerShape(12.dp)) {
                Text("开始记录")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
    )
}

@Composable
fun FundRecordDialog(
    record: FundRecordEntity?,
    type: Int,
    onDismiss: () -> Unit,
    onConfirm: (String, Double, String) -> Unit,
) {
    var name by remember(record?.id) { mutableStateOf(record?.name.orEmpty()) }
    var amount by remember(record?.id) { mutableStateOf(record?.amount?.let { fundNumber(it) }.orEmpty()) }
    var remark by remember(record?.id) { mutableStateOf(record?.remark.orEmpty()) }
    val amountValue = amount.toDoubleOrNull()
    val canSave = name.isNotBlank() && amountValue != null && amountValue >= 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (record == null) "新增${fundTypeLabel(type)}" else "修改${fundTypeLabel(type)}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("名称") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("金额 (¥)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
                OutlinedTextField(
                    value = remark,
                    onValueChange = { remark = it },
                    label = { Text("备注") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 2,
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(name, amountValue!!, remark) }, enabled = canSave, shape = RoundedCornerShape(12.dp)) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
    )
}

@Composable
fun FundDatePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    val context = LocalContext.current
    val today = remember { LocalDate.now() }
    var selectedDate by rememberSaveable { mutableStateOf(today.toString()) }

    fun showSystemDatePicker() {
        val parsed = runCatching { LocalDate.parse(selectedDate) }.getOrDefault(today)
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth)
            },
            parsed.year,
            parsed.monthValue - 1,
            parsed.dayOfMonth,
        ).show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择资金日期") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = selectedDate,
                    onValueChange = {},
                    label = { Text("资金日期") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = {
                        Icon(Icons.Default.Event, contentDescription = null)
                    },
                )
                Button(
                    onClick = { showSystemDatePicker() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Icon(Icons.Default.Event, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("选择日期")
                }
                Text("保存后会进入这一天的资金明细。", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedDate) },
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
    )
}

@Composable
private fun FundSummaryBoard(detail: FundDayDetailUi) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (detail.initialRecords.isNotEmpty()) {
            item { FundSectionCard(title = "初始资金", records = detail.initialRecords) }
        }
        if (detail.incomeRecords.isNotEmpty()) {
            item { FundSectionCard(title = "收到资金", records = detail.incomeRecords) }
        }
        if (detail.expenseRecords.isNotEmpty()) {
            item { FundSectionCard(title = "支出资金", records = detail.expenseRecords) }
        }
        if (detail.initialRecords.isEmpty() && detail.incomeRecords.isEmpty() && detail.expenseRecords.isEmpty()) {
            item { FundEmptyState("这一天还没有资金记录。") }
        }
    }
}

@Composable
private fun FundSectionCard(
    title: String,
    records: List<FundRecordEntity>,
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f))) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            records.forEach { record ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(record.name, fontWeight = FontWeight.Bold)
                        if (record.remark.isNotBlank()) {
                            Text(record.remark, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Text(fundCurrency(record.amount), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun FundProjectCard(
    project: FundProjectSummaryUi,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(project.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("收入 ${fundCurrency(project.totals.income)} · 支出 ${fundCurrency(project.totals.expense)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = { showDeleteConfirm = true }) {
                    Icon(Icons.Default.Delete, contentDescription = "删除资金项目", tint = MaterialTheme.colorScheme.error)
                }
            }
            FundTotalsCard(totals = project.totals, compact = true)
        }
    }

    if (showDeleteConfirm) {
        FundDeleteConfirmDialog(
            title = "删除资金项目",
            message = "确定删除“${project.name}”吗？",
            onDismiss = { showDeleteConfirm = false },
            onConfirm = {
                showDeleteConfirm = false
                onDelete()
            },
        )
    }
}

@Composable
private fun FundDateCard(
    date: FundDateSummaryUi,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth().combinedClickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f)),
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(date.date, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                FundTotalsCard(totals = date.totals, compact = true)
            }
            IconButton(onClick = { showDeleteConfirm = true }) {
                Icon(Icons.Default.Delete, contentDescription = "删除资金日期", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.9f))
            }
        }
    }

    if (showDeleteConfirm) {
        FundDeleteConfirmDialog(
            title = "删除资金日期",
            message = "确定删除 ${date.date} 的资金记录吗？",
            onDismiss = { showDeleteConfirm = false },
            onConfirm = {
                showDeleteConfirm = false
                onDelete()
            },
        )
    }
}

@Composable
private fun FundRecordList(
    records: List<FundRecordEntity>,
    emptyText: String,
    onEditRecord: (FundRecordEntity) -> Unit,
    onDeleteRecord: (FundRecordEntity) -> Unit,
) {
    if (records.isEmpty()) {
        FundEmptyState(emptyText)
        return
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(records, key = { it.id }) { record ->
            FundRecordCard(record = record, onEdit = { onEditRecord(record) }, onDelete = { onDeleteRecord(record) })
        }
    }
}

@Composable
private fun FundRecordCard(
    record: FundRecordEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.24f))) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(record.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Text(fundTypeLabel(record.type), style = MaterialTheme.typography.labelSmall, color = fundTypeColor(record.type))
                }
                Text(fundCurrency(record.amount), fontWeight = FontWeight.Bold, color = fundTypeColor(record.type))
            }
            if (record.remark.isNotBlank()) {
                Text(record.remark, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("修改")
                }
                TextButton(onClick = { showDeleteConfirm = true }) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }

    if (showDeleteConfirm) {
        FundDeleteConfirmDialog(
            title = "删除资金记录",
            message = "确定删除“${record.name}”吗？",
            onDismiss = { showDeleteConfirm = false },
            onConfirm = {
                showDeleteConfirm = false
                onDelete()
            },
        )
    }
}

@Composable
private fun FundTotalsCard(
    totals: FundTotals,
    compact: Boolean,
) {
    val cardColor = MaterialTheme.colorScheme.surface
    Card(colors = CardDefaults.cardColors(containerColor = cardColor)) {
        if (compact) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                FundMetric("初始", totals.initial)
                FundMetric("收入", totals.income)
                FundMetric("支出", totals.expense)
                FundMetric("余额", totals.balance, highlight = true)
            }
        } else {
            Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    FundMetric("初始资金", totals.initial)
                    FundMetric("收到资金", totals.income)
                }
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    FundMetric("支出资金", totals.expense)
                    FundMetric("当前余额", totals.balance, highlight = true)
                }
            }
        }
    }
}

@Composable
private fun FundMetric(
    label: String,
    amount: Double,
    highlight: Boolean = false,
) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            fundCurrency(amount),
            style = if (highlight) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun FundDeleteConfirmDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("确定删除", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
    )
}

private fun fundCurrency(value: Double): String = "¥${fundNumber(value)}"

private fun fundNumber(value: Double): String {
    return if (value % 1.0 == 0.0) {
        String.format(Locale.getDefault(), "%.0f", value)
    } else {
        String.format(Locale.getDefault(), "%.2f", value)
    }
}

@Composable
private fun fundTypeColor(type: Int): Color {
    return when (type) {
        FundRecordType.INCOME -> MaterialTheme.colorScheme.primary
        FundRecordType.EXPENSE -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.tertiary
    }
}

private fun fundTypeLabel(type: Int): String = when (type) {
    FundRecordType.INITIAL -> "初始资金"
    FundRecordType.INCOME -> "收到资金"
    FundRecordType.EXPENSE -> "支出资金"
    else -> "资金记录"
}

@Composable
private fun FundEmptyState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
