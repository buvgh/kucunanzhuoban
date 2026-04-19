package com.example.myapplication111.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import android.widget.Toast
import com.example.myapplication111.util.BackupManager
import com.example.myapplication111.util.ExportManager
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myapplication111.data.DateSummaryUi
import com.example.myapplication111.data.DayDetailUi
import com.example.myapplication111.data.DayPhotoEntity
import com.example.myapplication111.data.FeeRecordEntity
import com.example.myapplication111.data.FeeTypes
import com.example.myapplication111.data.PaymentRecordEntity
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.rememberDatePickerState
import com.example.myapplication111.data.OutboundRecordEntity
import com.example.myapplication111.data.ProjectOverviewUi
import com.example.myapplication111.data.ProjectSummaryUi
import com.example.myapplication111.data.StorageRecordEntity
import com.example.myapplication111.data.Totals
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DockNoteApp(viewModel: DockNoteViewModel) {
    val projects by viewModel.projectSummaries.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val coroutineScope = rememberCoroutineScope()
    val destinationRoute = navBackStackEntry?.destination?.route

    val currentProjectId = navBackStackEntry?.arguments?.getLong(NavArgs.ProjectId)
    val currentDateId = navBackStackEntry?.arguments?.getLong(NavArgs.DateId)
    val currentOverview by remember(currentProjectId) {
        if (currentProjectId == null) {
            flowOf(null)
        } else {
            viewModel.observeProjectOverview(currentProjectId)
        }
    }.collectAsState(initial = null)
    val currentDayDetail by remember(currentProjectId, currentDateId) {
        if (currentProjectId == null || currentDateId == null) {
            flowOf(null)
        } else {
            viewModel.observeDayDetail(currentProjectId, currentDateId)
        }
    }.collectAsState(initial = null)

    var showProjectDialog by rememberSaveable { mutableStateOf(false) }
    var showStorageDialog by rememberSaveable { mutableStateOf(false) }
    var showOutboundDialog by rememberSaveable { mutableStateOf(false) }
    var showFeeDialog by rememberSaveable { mutableStateOf(false) }
    var showPaymentDialog by rememberSaveable { mutableStateOf(false) }
    var showDatePickerDialog by rememberSaveable { mutableStateOf(false) }
    var editingStorage by remember { mutableStateOf<StorageRecordEntity?>(null) }
    var editingOutbound by remember { mutableStateOf<OutboundRecordEntity?>(null) }
    var editingFee by remember { mutableStateOf<FeeRecordEntity?>(null) }
    var editingPayment by remember { mutableStateOf<PaymentRecordEntity?>(null) }

    var selectedDayTab by rememberSaveable { mutableStateOf(0) }

    val itemNames by viewModel.uniqueItemNames.collectAsState()

    var showBackupDialog by rememberSaveable { mutableStateOf(false) }

    val title = when (destinationRoute) {
        Routes.ProjectList -> "库存管理"
        Routes.ProjectOverview -> currentOverview?.name ?: "项目总览"
        Routes.DayDetail -> currentDayDetail?.date ?: "每日详情"
        Routes.Camera -> "拍照"
        else -> "库存管理"
    }

    val context = LocalContext.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        title, 
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    ) 
                },
                navigationIcon = {
                    if (navController.previousBackStackEntry != null) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { showBackupDialog = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "备份管理")
                    }
                    IconButton(onClick = { viewModel.toggleDarkMode() }) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "切换模式"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                )
            )
        },
        floatingActionButton = {
            when (destinationRoute) {
                Routes.DayDetail -> {
                    val fabColor = when (selectedDayTab) {
                        2 -> MaterialTheme.colorScheme.secondary
                        else -> MaterialTheme.colorScheme.primary
                    }
                    val fabIcon = when (selectedDayTab) {
                        3 -> Icons.Default.Payments
                        4 -> Icons.Default.Add // Photos handled differently maybe?
                        else -> Icons.Default.Add
                    }
                    
                    if (selectedDayTab != 0 && selectedDayTab != 4) {
                        FloatingActionButton(
                            onClick = {
                                when (selectedDayTab) {
                                    1 -> {
                                        editingStorage = null
                                        showStorageDialog = true
                                    }
                                    2 -> {
                                        editingOutbound = null
                                        showOutboundDialog = true
                                    }
                                    3 -> {
                                        editingFee = null
                                        showFeeDialog = true
                                    }
                                }
                            },
                            containerColor = fabColor,
                            contentColor = contentColorFor(fabColor)
                        ) {
                            Icon(fabIcon, contentDescription = null)
                        }
                    } else if (selectedDayTab == 4) {
                        FloatingActionButton(
                            onClick = {
                                val projectId = currentProjectId
                                val dateId = currentDateId
                                if (projectId != null && dateId != null) {
                                    navController.navigate(Routes.camera(projectId, dateId))
                                }
                            },
                            containerColor = MaterialTheme.colorScheme.tertiary,
                            contentColor = MaterialTheme.colorScheme.onTertiary
                        ) {
                            Icon(Icons.Default.Add, "拍照")
                        }
                    }
                }
                Routes.ProjectOverview -> {
                    FloatingActionButton(
                        onClick = { showDatePickerDialog = true },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Icon(Icons.Default.Add, "新建日期")
                    }
                }
                Routes.ProjectList -> {
                    FloatingActionButton(
                        onClick = { showProjectDialog = true },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Icon(Icons.Default.Add, "新建项目")
                    }
                }
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding),
        ) {
            NavHost(
                navController = navController,
                startDestination = Routes.ProjectList,
            ) {
                composable(Routes.ProjectList) {
                    ProjectListScreen(
                        projects = projects,
                        onOpenProject = { projectId -> navController.navigate(Routes.project(projectId)) },
                        onDeleteProject = viewModel::deleteProject,
                    )
                }
                composable(
                    route = Routes.ProjectOverview,
                    arguments = listOf(navArgument(NavArgs.ProjectId) { type = NavType.LongType }),
                ) {
                    val projectId = currentProjectId
                    val overview = currentOverview
                    if (projectId == null || overview == null) {
                        EmptyState("项目不存在或已删除")
                    } else {
                        ProjectOverviewScreen(
                            overview = overview,
                            onOpenDate = { dateId -> navController.navigate(Routes.day(projectId, dateId)) },
                            onDeleteProject = {
                                viewModel.deleteProject(projectId)
                                navController.popBackStack(Routes.ProjectList, false)
                            },
                            onDeleteDate = { dateId -> viewModel.deleteDate(dateId) },
                            onAddPayment = {
                                editingPayment = null
                                showPaymentDialog = true
                            },
                            onEditPayment = {
                                editingPayment = it
                                showPaymentDialog = true
                            },
                            onDeletePayment = viewModel::deletePaymentRecord
                        )
                    }
                }
                composable(
                    route = Routes.DayDetail,
                    arguments = listOf(
                        navArgument(NavArgs.ProjectId) { type = NavType.LongType },
                        navArgument(NavArgs.DateId) { type = NavType.LongType },
                    ),
                ) {
                    val detail = currentDayDetail
                    if (detail == null) {
                        EmptyState("日期不存在或已删除")
                    } else {
                        DayDetailScreen(
                            detail = detail,
                            selectedTab = selectedDayTab,
                            onTabSelected = { selectedDayTab = it },
                            onDeletePhoto = viewModel::deleteDayPhoto,
                            onEditStorage = {
                                editingStorage = it
                                showStorageDialog = true
                            },
                            onDeleteStorage = viewModel::deleteStorageRecord,
                            onEditOutbound = {
                                editingOutbound = it
                                showOutboundDialog = true
                            },
                            onDeleteOutbound = viewModel::deleteOutboundRecord,
                            onEditFee = {
                                editingFee = it
                                showFeeDialog = true
                            },
                            onDeleteFee = viewModel::deleteFeeRecord,
                            onTakePhoto = {
                                val projectId = currentProjectId
                                val dateId = currentDateId
                                if (projectId != null && dateId != null) {
                                    navController.navigate(Routes.camera(projectId, dateId))
                                }
                            },
                            onAddFee = {
                                editingFee = null
                                showFeeDialog = true
                            },
                            onExportCsv = {
                                ExportManager.exportDayDetailToCsv(context, it)
                            },
                            onExportPdf = {
                                ExportManager.exportDayDetailToPdf(context, it)
                            }
                        )
                    }
                }
                composable(
                    route = Routes.Camera,
                    arguments = listOf(
                        navArgument(NavArgs.ProjectId) { type = NavType.LongType },
                        navArgument(NavArgs.DateId) { type = NavType.LongType },
                    ),
                ) {
                    val dateId = currentDateId
                    if (dateId == null) {
                        EmptyState("日期不存在或已删除")
                    } else {
                        CameraCaptureScreen(
                            dateId = dateId,
                            onPhotoSaved = { path ->
                                viewModel.addDayPhoto(dateId, path)
                                navController.popBackStack()
                            },
                        )
                    }
                }
            }
        }
    }

    if (showProjectDialog) {
        ProjectDialog(
            onDismiss = { showProjectDialog = false },
            onConfirm = { name ->
                viewModel.createProject(name)
                showProjectDialog = false
            },
        )
    }

    if (showBackupDialog) {
        BackupManagementDialog(
            onDismiss = { showBackupDialog = false },
            onRestore = { file ->
                viewModel.restoreBackup(file)
                showBackupDialog = false
            },
            onExportAll = {
                coroutineScope.launch {
                    BackupManager.exportAndShareDatabase(context)
                }
            },
            onDebugFill = {
                viewModel.debugFillMockData()
                Toast.makeText(context, "正在注入数据...", Toast.LENGTH_SHORT).show()
                showBackupDialog = false
            },
            backups = BackupManager.getBackups(context)
        )
    }

    if (showDatePickerDialog && currentProjectId != null) {
        NativeDatePickerDialog(
            onDismiss = { showDatePickerDialog = false },
            onConfirm = { date ->
                showDatePickerDialog = false
                coroutineScope.launch {
                    val dateId = viewModel.createOrGetDate(currentProjectId, date)
                    navController.navigate(Routes.day(currentProjectId, dateId))
                }
            },
        )
    }

    if (showStorageDialog && currentDayDetail != null) {
        val detail = currentDayDetail
        StorageRecordDialog(
            record = editingStorage,
            onDismiss = {
                showStorageDialog = false
                editingStorage = null
            },
            onConfirm = { name, count, weightPerUnit, pricePerWeight ->
                if (detail != null) {
                    val record = editingStorage
                    if (record == null) {
                        viewModel.addStorageRecord(detail.dateId, name, count, weightPerUnit, pricePerWeight)
                    } else {
                        viewModel.updateStorageRecord(record, name, count, weightPerUnit, pricePerWeight)
                    }
                    showStorageDialog = false
                    editingStorage = null
                }
            },
            itemNames = itemNames,
            onGetLastPrice = { name: String -> 
                viewModel.getLastPriceForItem(name) 
            },
            onGetLastWeight = { name: String ->
                viewModel.getLastWeightForItem(name)
            }
        )
    }

    if (showOutboundDialog && currentDayDetail != null) {
        val detail = currentDayDetail
        OutboundRecordDialog(
            record = editingOutbound,
            onDismiss = {
                showOutboundDialog = false
                editingOutbound = null
            },
            onConfirm = { name, count, weightPerUnit ->
                if (detail != null) {
                    val record = editingOutbound
                    if (record == null) {
                        viewModel.addOutboundRecord(detail.dateId, name, count, weightPerUnit)
                    } else {
                        viewModel.updateOutboundRecord(record, name, count, weightPerUnit)
                    }
                    showOutboundDialog = false
                    editingOutbound = null
                }
            },
            itemNames = itemNames,
            onGetLastWeight = { name: String ->
                viewModel.getLastWeightForItem(name)
            }
        )
    }

    if (showFeeDialog && currentDayDetail != null) {
        val detail = currentDayDetail
        FeeRecordDialog(
            record = editingFee,
            onDismiss = {
                showFeeDialog = false
                editingFee = null
            },
            onConfirm = { type, amount ->
                if (detail != null) {
                    val record = editingFee
                    if (record == null) {
                        viewModel.addFeeRecord(detail.dateId, type, amount)
                    } else {
                        viewModel.updateFeeRecord(record, type, amount)
                    }
                    showFeeDialog = false
                    editingFee = null
                }
            },
        )
    }

    if (showPaymentDialog && currentOverview != null) {
        PaymentRecordDialog(
            record = editingPayment,
            onDismiss = {
                showPaymentDialog = false
                editingPayment = null
            },
            onConfirm = { amount, date, remark ->
                val projectId = currentOverview?.id ?: return@PaymentRecordDialog
                if (editingPayment == null) {
                    viewModel.addPaymentRecord(projectId, amount, date, remark)
                } else {
                    viewModel.updatePaymentRecord(editingPayment!!, amount, date, remark)
                }
                showPaymentDialog = false
                editingPayment = null
            }
        )
    }
}

private object NavArgs {
    const val ProjectId = "projectId"
    const val DateId = "dateId"
}

private object Routes {
    const val ProjectList = "projects"
    const val ProjectOverview = "project/{projectId}"
    const val DayDetail = "project/{projectId}/day/{dateId}"
    const val Camera = "project/{projectId}/day/{dateId}/camera"

    fun project(projectId: Long): String = "project/$projectId"
    fun day(projectId: Long, dateId: Long): String = "project/$projectId/day/$dateId"
    fun camera(projectId: Long, dateId: Long): String = "project/$projectId/day/$dateId/camera"
}

@Composable
private fun DeleteConfirmDialog(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NativeDatePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    val initialMillis = remember {
        val todayUtc = LocalDate.now(ZoneId.of("UTC"))
        todayUtc.atStartOfDay(ZoneId.of("UTC"))
            .toInstant()
            .toEpochMilli()
    }
    val pickerState = androidx.compose.material3.rememberDatePickerState(initialSelectedDateMillis = initialMillis)

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val millis = pickerState.selectedDateMillis
                    if (millis != null) {
                        val date = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.of("UTC"))
                            .toLocalDate()
                            .toString()
                        onConfirm(date)
                    }
                },
                enabled = pickerState.selectedDateMillis != null,
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        },
    ) {
        DatePicker(state = pickerState)
    }
}

@Composable
private fun ProjectListScreen(
    projects: List<ProjectSummaryUi>,
    onOpenProject: (Long) -> Unit,
    onDeleteProject: (Long) -> Unit,
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val filteredProjects = remember(projects, searchQuery) {
        if (searchQuery.isBlank()) {
            projects
        } else {
            projects.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
    }

    if (projects.isEmpty()) {
        EmptyState("还没有项目，点击右下角按钮新建一个。")
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text(
                "我的项目",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("搜索项目名称...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, "清除搜索")
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                )
            )
        }
        if (filteredProjects.isEmpty()) {
            item {
                EmptyState("找不到匹配的项目。")
            }
        } else {
            items(filteredProjects, key = { it.id }) { project ->
                ProjectCard(
                    project = project,
                    onClick = { onOpenProject(project.id) },
                    onDelete = { onDeleteProject(project.id) },
                )
            }
        }
    }
}

@Composable
private fun ProjectCard(
    project: ProjectSummaryUi,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = { },
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                color = when (project.name.firstOrNull()?.uppercaseChar() ?: ' ') {
                    in 'A'..'G' -> MaterialTheme.colorScheme.primaryContainer
                    in 'H'..'N' -> MaterialTheme.colorScheme.secondaryContainer
                    in 'O'..'U' -> MaterialTheme.colorScheme.tertiaryContainer
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        project.name.take(1).uppercase(),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = when (project.name.firstOrNull()?.uppercaseChar() ?: ' ') {
                                in 'A'..'G' -> MaterialTheme.colorScheme.onPrimaryContainer
                                in 'H'..'N' -> MaterialTheme.colorScheme.onSecondaryContainer
                                in 'O'..'U' -> MaterialTheme.colorScheme.onTertiaryContainer
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    project.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TotalsCompactText(project.totals)
            }
            IconButton(onClick = { showDeleteConfirm = true }) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "删除",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                )
            }
        }
    }

    if (showDeleteConfirm) {
        DeleteConfirmDialog(
            title = "删除项目",
            message = "确定要删除项目“${project.name}”吗？这将删除该项目下的所有日期和记录，且无法恢复。",
            onDismiss = { showDeleteConfirm = false },
            onConfirm = {
                showDeleteConfirm = false
                onDelete()
            },
        )
    }
}

@Composable
private fun ProjectOverviewScreen(
    overview: ProjectOverviewUi,
    onOpenDate: (Long) -> Unit,
    onDeleteProject: () -> Unit,
    onDeleteDate: (Long) -> Unit,
    onAddPayment: () -> Unit,
    onEditPayment: (PaymentRecordEntity) -> Unit,
    onDeletePayment: (PaymentRecordEntity) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            SummaryCard(
                title = "项目全周期汇总",
                totals = overview.totals,
                isProjectTotal = true
            )
        }
        
        item {
            SectionHeader("付款记录", actionLabel = "登记付款", onAction = onAddPayment)
        }

        if (overview.paymentRecords.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("暂无付款记录", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            items(overview.paymentRecords) { record ->
                PaymentRecordCard(
                    record = record,
                    onEdit = { onEditPayment(record) },
                    onDelete = { onDeletePayment(record) }
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "每日记录",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onDeleteProject) {
                    Text("删除项目", color = MaterialTheme.colorScheme.error)
                }
            }
        }
        if (overview.dates.isEmpty()) {
            item {
                EmptyState("还没有日期记录，点击右下角按钮新建。")
            }
        } else {
            items(overview.dates, key = { it.id }) { dateSummary ->
                DateCard(
                    dateSummary = dateSummary,
                    onClick = { onOpenDate(dateSummary.id) },
                    onDelete = { onDeleteDate(dateSummary.id) },
                )
            }
        }
    }
}

@Composable
private fun PaymentRecordCard(
    record: PaymentRecordEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Payments,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(record.date, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    if (record.remark.isNotBlank()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "(${record.remark})", 
                            style = MaterialTheme.typography.labelSmall, 
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
                Text(formatCurrency(record.amount), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, "编辑", modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.outline)
                }
                IconButton(onClick = { showDeleteConfirm = true }) {
                    Icon(Icons.Default.Delete, "删除", modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                }
            }
        }
    }

    if (showDeleteConfirm) {
        DeleteConfirmDialog(
            title = "删除付款记录",
            message = "确定要删除这条金额为 ${formatCurrency(record.amount)} 的付款记录吗？",
            onDismiss = { showDeleteConfirm = false },
            onConfirm = {
                showDeleteConfirm = false
                onDelete()
            },
        )
    }
}

@Composable
private fun DateCard(
    dateSummary: DateSummaryUi,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .combinedClickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    dateSummary.date,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TotalsCompactText(dateSummary.totals)
            }
            IconButton(onClick = { showDeleteConfirm = true }) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "删除",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }

    if (showDeleteConfirm) {
        DeleteConfirmDialog(
            title = "删除日期记录",
            message = "确定要删除 ${dateSummary.date} 的所有记录吗？",
            onDismiss = { showDeleteConfirm = false },
            onConfirm = {
                showDeleteConfirm = false
                onDelete()
            },
        )
    }
}

@Composable
private fun DayDetailScreen(
    detail: DayDetailUi,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onDeletePhoto: (DayPhotoEntity) -> Unit,
    onEditStorage: (StorageRecordEntity) -> Unit,
    onDeleteStorage: (StorageRecordEntity) -> Unit,
    onEditOutbound: (OutboundRecordEntity) -> Unit,
    onDeleteOutbound: (OutboundRecordEntity) -> Unit,
    onEditFee: (FeeRecordEntity) -> Unit,
    onDeleteFee: (FeeRecordEntity) -> Unit,
    onTakePhoto: () -> Unit,
    onAddFee: () -> Unit,
    onExportCsv: (DayDetailUi) -> Unit,
    onExportPdf: (DayDetailUi) -> Unit,
) {
    val tabs = listOf("汇总", "入库清单", "出库清单", "费用支出", "现场照片")
    
    var selectedFeeTab by remember { mutableStateOf(0) }
    val feeCategories = listOf("全部", FeeTypes.LABOR, FeeTypes.AGENCY, FeeTypes.LOADING, "其他")
    
    val categoryTotals = remember(detail.feeRecords) {
        feeCategories.map { cat ->
            when (cat) {
                "全部" -> detail.feeRecords.sumOf { it.amount }
                FeeTypes.LABOR -> detail.feeRecords.filter { it.type == FeeTypes.LABOR }.sumOf { it.amount }
                FeeTypes.AGENCY -> detail.feeRecords.filter { it.type == FeeTypes.AGENCY }.sumOf { it.amount }
                FeeTypes.LOADING -> detail.feeRecords.filter { it.type == FeeTypes.LOADING }.sumOf { it.amount }
                else -> detail.feeRecords.filter { it.type !in listOf(FeeTypes.LABOR, FeeTypes.AGENCY, FeeTypes.LOADING) }.sumOf { it.amount }
            }
        }
    }

    val filteredFees = remember(selectedFeeTab, detail.feeRecords) {
        when (selectedFeeTab) {
            0 -> detail.feeRecords
            1 -> detail.feeRecords.filter { it.type == FeeTypes.LABOR }
            2 -> detail.feeRecords.filter { it.type == FeeTypes.AGENCY }
            3 -> detail.feeRecords.filter { it.type == FeeTypes.LOADING }
            else -> detail.feeRecords.filter { it.type !in listOf(FeeTypes.LABOR, FeeTypes.AGENCY, FeeTypes.LOADING) }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        SecondaryScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            edgePadding = 16.dp,
            divider = {}
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { onTabSelected(index) },
                    text = { Text(title) }
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            when (selectedTab) {
                0 -> {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { onExportCsv(detail) }) {
                                Icon(Icons.Default.Share, null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("导出 CSV")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            TextButton(onClick = { onExportPdf(detail) }) {
                                Icon(Icons.Default.Share, null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("导出 PDF")
                            }
                        }
                    }
                    item {
                        SummaryCard(title = "当日详细汇总", totals = detail.totals)
                    }
                }
                1 -> {
                    if (detail.storageRecords.isEmpty()) {
                        item { EmptyState("暂无入库记录") }
                    } else {
                        items(detail.storageRecords, key = { "storage_${it.id}" }) { record ->
                            StorageRecordCard(
                                record = record,
                                onEdit = { onEditStorage(record) },
                                onDelete = { onDeleteStorage(record) },
                            )
                        }
                    }
                }
                2 -> {
                    if (detail.outboundRecords.isEmpty()) {
                        item { EmptyState("暂无出库记录") }
                    } else {
                        items(detail.outboundRecords, key = { "outbound_${it.id}" }) { record ->
                            OutboundRecordCard(
                                record = record,
                                onEdit = { onEditOutbound(record) },
                                onDelete = { onDeleteOutbound(record) },
                            )
                        }
                    }
                }
                3 -> {
                    item {
                        SectionHeader("费用明细", onAction = onAddFee, actionLabel = "记一笔")
                        Spacer(modifier = Modifier.height(8.dp))
                        SecondaryScrollableTabRow(
                            selectedTabIndex = selectedFeeTab,
                            containerColor = MaterialTheme.colorScheme.background,
                            contentColor = MaterialTheme.colorScheme.primary,
                            edgePadding = 0.dp,
                            divider = {},
                            indicator = {
                                TabRowDefaults.SecondaryIndicator(
                                    Modifier.tabIndicatorOffset(selectedFeeTab, true),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        ) {
                            feeCategories.forEachIndexed { index, title ->
                                val selected = selectedFeeTab == index
                                Tab(
                                    selected = selected,
                                    onClick = { selectedFeeTab = index },
                                    text = { 
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                title, 
                                                style = if (selected) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
                                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                            )
                                            if (categoryTotals[index] > 0) {
                                                Text(
                                                    "¥${formatNumber(categoryTotals[index])}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                    if (filteredFees.isEmpty()) {
                        item { 
                            val emptyMsg = if (selectedFeeTab == 0) "暂无费用记录" else "暂无${feeCategories[selectedFeeTab]}记录"
                            EmptyState(emptyMsg) 
                        }
                    } else {
                        items(filteredFees, key = { "fee_${it.id}" }) { record ->
                            FeeRecordCard(
                                record = record,
                                onEdit = { onEditFee(record) },
                                onDelete = { onDeleteFee(record) },
                            )
                        }
                    }
                }
                4 -> {
                    item {
                        SectionHeader("现场照片", onAction = onTakePhoto, actionLabel = "去拍照")
                    }
                    if (detail.photos.isEmpty()) {
                        item { 
                            Surface(
                                modifier = Modifier.fillMaxWidth().height(100.dp),
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("暂无照片", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    } else {
                        items(detail.photos, key = { "photo_${it.id}" }) { photo ->
                            DayPhotoCard(
                                photo = photo,
                                onDelete = { onDeletePhoto(photo) },
                            )
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }
}

@Composable
private fun SectionHeader(title: String, actionLabel: String, onAction: (() -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        if (onAction != null && actionLabel.isNotBlank()) {
            TextButton(onClick = onAction) {
                Text(actionLabel, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun StorageRecordCard(
    record: StorageRecordEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Inventory,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(record.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                
                Row(modifier = Modifier.fillMaxWidth()) {
                    InfoItem("件数", formatNumber(record.count), Modifier.weight(1f))
                    InfoItem("单件重量", "${formatNumber(record.weightPerUnit)}斤", Modifier.weight(1f))
                    InfoItem("单价", "¥${formatNumber(record.pricePerWeight)}", Modifier.weight(1f))
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("总计: ${formatNumber(record.totalWeight)} 斤", style = MaterialTheme.typography.bodySmall)
                    Text(formatCurrency(record.totalPrice), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }
            Column(modifier = Modifier.padding(start = 8.dp)) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, "编辑", modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.outline)
                }
                IconButton(onClick = { showDeleteConfirm = true }) {
                    Icon(Icons.Default.Delete, "删除", modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                }
            }
        }
    }

    if (showDeleteConfirm) {
        DeleteConfirmDialog(
            title = "删除入库记录",
            message = "确定要删除“${record.name}”的入库记录吗？",
            onDismiss = { showDeleteConfirm = false },
            onConfirm = {
                showDeleteConfirm = false
                onDelete()
            },
        )
    }
}

@Composable
private fun InfoItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun FeeRecordCard(
    record: FeeRecordEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Payments,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(record.type, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(formatCurrency(record.amount), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary)
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, "编辑", modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.outline)
                }
                IconButton(onClick = { showDeleteConfirm = true }) {
                    Icon(Icons.Default.Delete, "删除", modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                }
            }
        }
    }

    if (showDeleteConfirm) {
        DeleteConfirmDialog(
            title = "删除费用记录",
            message = "确定要删除“${record.type}”费用吗？",
            onDismiss = { showDeleteConfirm = false },
            onConfirm = {
                showDeleteConfirm = false
                onDelete()
            },
        )
    }
}

@Composable
private fun OutboundRecordCard(
    record: OutboundRecordEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Inventory,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(record.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                
                Row(modifier = Modifier.fillMaxWidth()) {
                    InfoItem("件数", formatNumber(record.count), Modifier.weight(1f))
                    InfoItem("单件重量", "${formatNumber(record.weightPerUnit)}斤", Modifier.weight(1f))
                    InfoItem("时间", SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(record.timestamp)), Modifier.weight(1f))
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("总重计: ${formatNumber(record.count * record.weightPerUnit)} 斤", style = MaterialTheme.typography.bodySmall)
                }
            }
            Column(modifier = Modifier.padding(start = 8.dp)) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, "编辑", modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.outline)
                }
                IconButton(onClick = { showDeleteConfirm = true }) {
                    Icon(Icons.Default.Delete, "删除", modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                }
            }
        }
    }

    if (showDeleteConfirm) {
        DeleteConfirmDialog(
            title = "删除出库记录",
            message = "确定要删除“${record.name}”的出库记录吗？",
            onDismiss = { showDeleteConfirm = false },
            onConfirm = {
                showDeleteConfirm = false
                onDelete()
            },
        )
    }
}

@Composable
private fun OutboundRecordDialog(
    record: OutboundRecordEntity?,
    itemNames: List<String>,
    onGetLastWeight: suspend (String) -> Double?,
    onDismiss: () -> Unit,
    onConfirm: (String, Double, Double) -> Unit,
) {
    var name by remember(record?.id) { mutableStateOf(record?.name.orEmpty()) }
    var count by remember(record?.id) { mutableStateOf(record?.count?.toText().orEmpty()) }
    var weightPerUnit by remember(record?.id) { mutableStateOf(record?.weightPerUnit?.toText().orEmpty()) }

    var showHistory by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val countValue = count.toDoubleOrNull()
    val weightValue = weightPerUnit.toDoubleOrNull()
    val canSave = name.isNotBlank() && countValue != null && weightValue != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (record == null) "登记货物出库" else "修改出库信息") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Box {
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            showHistory = it.isNotBlank()
                        },
                        label = { Text("物品/货物名称") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(Icons.Default.Inventory, null) }
                    )
                    if (showHistory && itemNames.isNotEmpty()) {
                        val filtered = itemNames.filter { it.contains(name, ignoreCase = true) }
                        if (filtered.isNotEmpty()) {
                            DropdownMenu(
                                expanded = showHistory,
                                onDismissRequest = { showHistory = false },
                                modifier = Modifier.fillMaxWidth(0.8f),
                                properties = PopupProperties(focusable = false)
                            ) {
                                filtered.forEach { historyName ->
                                    DropdownMenuItem(
                                        leadingIcon = { Icon(Icons.Default.History, null, modifier = Modifier.size(18.dp)) },
                                        text = { Text(historyName) },
                                        onClick = {
                                            name = historyName
                                            showHistory = false
                                        scope.launch {
                                            val lastWeight = onGetLastWeight(historyName)
                                            if (lastWeight != null) {
                                                weightPerUnit = formatNumber(lastWeight)
                                            }
                                        }
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
                OutlinedTextField(
                    value = count,
                    onValueChange = { count = it },
                    label = { Text("出库件数") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = weightPerUnit,
                    onValueChange = { weightPerUnit = it },
                    label = { Text("单件重量(斤)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                
                if (canSave) {
                    val totalWeight = countValue!! * weightValue!!
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("预估合计", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                            Text("总重: ${formatNumber(totalWeight)} 斤", 
                                style = MaterialTheme.typography.bodyMedium, 
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, countValue!!, weightValue!!) },
                enabled = canSave,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("确认出库")
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
private fun SummaryCard(
    title: String,
    totals: Totals,
    isProjectTotal: Boolean = false,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            
            Row(modifier = Modifier.fillMaxWidth()) {
                SummaryBox("入库(件/斤)", "${formatNumber(totals.totalCount)} / ${formatNumber(totals.totalWeight)}", Modifier.weight(1f))
                SummaryBox("出库(件/斤)", "${formatNumber(totals.totalOutboundCount)} / ${formatNumber(totals.totalOutboundWeight)}", Modifier.weight(1f))
            }
            
            Row(modifier = Modifier.fillMaxWidth()) {
                SummaryBox("净库存(件)", formatNumber(totals.netTotalCount), Modifier.weight(1f))
                SummaryBox("净库存(斤)", formatNumber(totals.netTotalWeight), Modifier.weight(1f))
            }
            
            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f))

            if (totals.itemSummaries.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("入库清单细目", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f))
                    totals.itemSummaries.forEach { summary ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(summary.name, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text(
                                "${formatNumber(summary.totalCount)}件 / ${formatNumber(summary.totalWeight)}斤 / ${formatCurrency(summary.totalAmount)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f))
            }
            
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SmallSummaryRow("果款合计", formatCurrency(totals.totalStorageAmount))
                
                if (totals.feeSummaries.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("费用支出细目", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f))
                    totals.feeSummaries.forEach { fee ->
                        SmallSummaryRow(fee.type, formatCurrency(fee.totalAmount))
                    }
                }
                
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f))
                SmallSummaryRow("各项费用合计", formatCurrency(totals.totalFee))

                val totalCost = totals.totalStorageAmount + totals.totalFee
                if (isProjectTotal) {
                    SmallSummaryRow("总应付金额", formatCurrency(totalCost))
                    SmallSummaryRow("已付金额", formatCurrency(totals.totalPaid))
                    HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("还欠金额", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                        Text(formatCurrency(totals.totalDebt), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.error)
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("合计", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text(formatCurrency(totalCost), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryBox(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
        Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
    }
}

@Composable
private fun SmallSummaryRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun TotalsCompactText(totals: Totals) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "入: ${formatNumber(totals.totalCount)}件 / ${formatNumber(totals.totalWeight)}斤",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "出: ${formatNumber(totals.totalOutboundCount)}件",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "存: ${formatNumber(totals.netTotalCount)}件 / ${formatNumber(totals.netTotalWeight)}斤",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                "支: ${formatCurrency(totals.totalFee)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmptyState(message: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Inbox,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
private fun ProjectDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var name by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("新建工作项目") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("例如：张三家苹果、4月出库项目") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name) },
                enabled = name.isNotBlank(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("开始记录")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("暂不")
            }
        },
    )
}

@Composable
private fun StorageRecordDialog(
    record: StorageRecordEntity?,
    itemNames: List<String>,
    onGetLastPrice: suspend (String) -> Double?,
    onGetLastWeight: suspend (String) -> Double?,
    onDismiss: () -> Unit,
    onConfirm: (String, Double, Double, Double) -> Unit,
) {
    var name by remember(record?.id) { mutableStateOf(record?.name.orEmpty()) }
    var count by remember(record?.id) { mutableStateOf(record?.count?.toText().orEmpty()) }
    var weightPerUnit by remember(record?.id) { mutableStateOf(record?.weightPerUnit?.toText().orEmpty()) }
    var pricePerWeight by remember(record?.id) { mutableStateOf(record?.pricePerWeight?.toText().orEmpty()) }

    var showHistory by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val countValue = count.toDoubleOrNull()
    val weightValue = weightPerUnit.toDoubleOrNull()
    val priceValue = pricePerWeight.toDoubleOrNull()
    val canSave = name.isNotBlank() && countValue != null && weightValue != null && priceValue != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (record == null) "登记货物入库" else "修改登记信息") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Box {
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            showHistory = it.isNotBlank()
                        },
                        label = { Text("物品/货物名称") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(Icons.Default.Inventory, null) }
                    )
                    if (showHistory && itemNames.isNotEmpty()) {
                        val filtered = itemNames.filter { it.contains(name, ignoreCase = true) }
                        if (filtered.isNotEmpty()) {
                            DropdownMenu(
                                expanded = showHistory,
                                onDismissRequest = { showHistory = false },
                                modifier = Modifier.fillMaxWidth(0.8f),
                                properties = PopupProperties(focusable = false)
                            ) {
                                filtered.forEach { historyName ->
                                    DropdownMenuItem(
                                        leadingIcon = { Icon(Icons.Default.History, null, modifier = Modifier.size(18.dp)) },
                                        text = { Text(historyName) },
                                        onClick = {
                                            name = historyName
                                            showHistory = false
                                            scope.launch {
                                                val lastPrice = onGetLastPrice(historyName)
                                                if (lastPrice != null) {
                                                    pricePerWeight = formatNumber(lastPrice)
                                                }
                                                val lastWeight = onGetLastWeight(historyName)
                                                if (lastWeight != null) {
                                                    weightPerUnit = formatNumber(lastWeight)
                                                }
                                            }
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
                OutlinedTextField(
                    value = count,
                    onValueChange = { count = it },
                    label = { Text("入库总件数") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = weightPerUnit,
                        onValueChange = { weightPerUnit = it },
                        label = { Text("单件重量(斤)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = pricePerWeight,
                        onValueChange = { pricePerWeight = it },
                        label = { Text("单价(元/斤)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
                
                if (canSave) {
                    val totalWeight = countValue!! * weightValue!!
                    val totalPrice = totalWeight * priceValue!!
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("预估合计", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                            Text("总重: ${formatNumber(totalWeight)} 斤  |  总额: ${formatCurrency(totalPrice)}", 
                                style = MaterialTheme.typography.bodyMedium, 
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, countValue!!, weightValue!!, priceValue!!) },
                enabled = canSave,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("确认入库")
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
private fun FeeRecordDialog(
    record: FeeRecordEntity?,
    onDismiss: () -> Unit,
    onConfirm: (String, Double) -> Unit,
) {
    val isInitialCustom = record != null && record.type !in listOf(FeeTypes.LABOR, FeeTypes.AGENCY, FeeTypes.LOADING)
    var selectedType by remember(record?.id) {
        mutableStateOf(if (isInitialCustom) FeeTypes.CUSTOM else (record?.type ?: FeeTypes.LABOR))
    }
    var customName by remember(record?.id) {
        mutableStateOf(if (isInitialCustom) record!!.type else "")
    }
    var amount by remember(record?.id) { mutableStateOf(record?.amount?.toText().orEmpty()) }
    var menuExpanded by remember { mutableStateOf(false) }

    val amountValue = amount.toDoubleOrNull()
    val finalTypeName = if (selectedType == FeeTypes.CUSTOM) customName else selectedType
    val canSave = amountValue != null && (selectedType != FeeTypes.CUSTOM || customName.isNotBlank())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (record == null) "添加费用" else "修改费用") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("费用类型：$selectedType")
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    FeeTypes.all.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                selectedType = option
                                menuExpanded = false
                            },
                        )
                    }
                }

                if (selectedType == FeeTypes.CUSTOM) {
                    OutlinedTextField(
                        value = customName,
                        onValueChange = { customName = it },
                        label = { Text("自定义费用名称") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("金额 (¥)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(finalTypeName, amountValue!!) },
                enabled = canSave,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("保存费用")
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
private fun BackupManagementDialog(
    onDismiss: () -> Unit,
    onRestore: (File) -> Unit,
    onExportAll: () -> Unit,
    onDebugFill: () -> Unit,
    backups: List<File>,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("数据备份与恢复") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = onExportAll,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Share, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("导出全部数据分享")
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Text("本地备份记录 (最近5次)：", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(8.dp))
                if (backups.isEmpty()) {
                    Text("暂无备份文件", style = MaterialTheme.typography.bodyMedium)
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                        items(backups) { file ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                onClick = { onRestore(file) },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(file.name, style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(file.lastModified())),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("提示：还原备份将覆盖当前所有数据，完成后应用将自动重启。", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = onDebugFill,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("注入性能测试数据 (1000条)")
                }

                Spacer(modifier = Modifier.height(16.dp))
                val context = LocalContext.current
                val packageInfo = remember {
                    try {
                        context.packageManager.getPackageInfo(context.packageName, 0)
                    } catch (e: Exception) {
                        null
                    }
                }
                val versionText = packageInfo?.let { "版本：${it.versionName} (${it.versionCode})" } ?: "版本：未知"
                Text(
                    text = versionText,
                    modifier = Modifier.align(Alignment.End),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("关闭") }
        }
    )
}

@Composable
private fun PaymentRecordDialog(
    record: PaymentRecordEntity?,
    onDismiss: () -> Unit,
    onConfirm: (Double, String, String) -> Unit,
) {
    var amount by remember(record?.id) { mutableStateOf(record?.amount?.toText().orEmpty()) }
    var date by remember(record?.id) { mutableStateOf(record?.date ?: LocalDate.now().toString()) }
    var remark by remember(record?.id) { mutableStateOf(record?.remark.orEmpty()) }
    var showDatePicker by remember { mutableStateOf(false) }

    val amountValue = amount.toDoubleOrNull()
    val canSave = amountValue != null && amountValue > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (record == null) "登记付款" else "修改付款记录") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("付款金额 (¥)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.History, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("付款日期：$date")
                }

                OutlinedTextField(
                    value = remark,
                    onValueChange = { remark = it },
                    label = { Text("备注 (可选)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(amountValue!!, date, remark) },
                enabled = canSave,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("保存付款")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
    )

    if (showDatePicker) {
        NativeDatePickerDialog(
            onDismiss = { showDatePicker = false },
            onConfirm = {
                date = it
                showDatePicker = false
            }
        )
    }
}

private fun Double.toText(): String {
    return if (this == 0.0) "" else formatNumber(this)
}

private fun formatNumber(value: Double): String {
    return String.format(Locale.getDefault(), "%.2f", value)
}

private fun formatCurrency(value: Double): String {
    return "¥${formatNumber(value)}"
}
