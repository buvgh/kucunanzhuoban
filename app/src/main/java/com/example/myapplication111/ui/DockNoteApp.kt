package com.example.myapplication111.ui

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material.icons.filled.ArrowDropDown
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
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.input.KeyboardType
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.navArgument
import com.example.myapplication111.data.DateSummaryUi
import com.example.myapplication111.data.DayDetailUi
import com.example.myapplication111.data.DayPhotoEntity
import com.example.myapplication111.data.FeeRecordEntity
import com.example.myapplication111.data.FeeTypes
import com.example.myapplication111.data.PaymentRecordEntity
import androidx.compose.material3.contentColorFor
import com.example.myapplication111.data.AttendanceDashboardRow
import com.example.myapplication111.data.AttendanceEntity
import com.example.myapplication111.data.AttendanceMonthBoard
import com.example.myapplication111.data.AttendanceProjectSummary
import com.example.myapplication111.data.AttendanceSummary
import com.example.myapplication111.data.FundRecordEntity
import com.example.myapplication111.data.FundRecordType
import com.example.myapplication111.data.OutboundRecordEntity
import com.example.myapplication111.data.ProjectOverviewUi
import com.example.myapplication111.data.ProjectGroupSummaryUi
import com.example.myapplication111.data.ProjectSummaryUi
import com.example.myapplication111.data.SalaryMode
import com.example.myapplication111.data.SecondarySaleRecordEntity
import com.example.myapplication111.data.StorageRecordEntity
import com.example.myapplication111.data.Totals
import com.example.myapplication111.data.WorkerEntity
import java.time.LocalDate
import java.time.YearMonth
import java.util.Locale
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DockNoteApp(viewModel: DockNoteViewModel) {
    val projects by viewModel.projectSummaries.collectAsState()
    val fundProjects by viewModel.fundProjectSummaries.collectAsState()
    val projectGroups by viewModel.projectGroupSummaries.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val activePdf by viewModel.activePdf.collectAsState()
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val coroutineScope = rememberCoroutineScope()
    val destinationRoute = navBackStackEntry?.destination?.route

    val currentProjectId = navBackStackEntry?.arguments?.getLong(NavArgs.ProjectId)
    val currentDateId = navBackStackEntry?.arguments?.getLong(NavArgs.DateId)
    val currentFundProjectId = navBackStackEntry?.arguments?.getLong(NavArgs.FundProjectId)
    val currentFundDateId = navBackStackEntry?.arguments?.getLong(NavArgs.FundDateId)
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
    val currentFundOverview by remember(currentFundProjectId) {
        if (currentFundProjectId == null) {
            flowOf(null)
        } else {
            viewModel.observeFundProjectOverview(currentFundProjectId)
        }
    }.collectAsState(initial = null)
    val currentFundExport by remember(currentFundProjectId) {
        if (currentFundProjectId == null) {
            flowOf(null)
        } else {
            viewModel.observeFundProjectExport(currentFundProjectId)
        }
    }.collectAsState(initial = null)
    val currentFundDayDetail by remember(currentFundProjectId, currentFundDateId) {
        if (currentFundProjectId == null || currentFundDateId == null) {
            flowOf(null)
        } else {
            viewModel.observeFundDayDetail(currentFundProjectId, currentFundDateId)
        }
    }.collectAsState(initial = null)

    var showProjectDialog by rememberSaveable { mutableStateOf(false) }
    var showStorageDialog by rememberSaveable { mutableStateOf(false) }
    var showSecondarySaleDialog by rememberSaveable { mutableStateOf(false) }
    var showOutboundDialog by rememberSaveable { mutableStateOf(false) }
    var showFeeDialog by rememberSaveable { mutableStateOf(false) }
    var showPaymentDialog by rememberSaveable { mutableStateOf(false) }
    var showDatePickerDialog by rememberSaveable { mutableStateOf(false) }
    var showProjectGroupDialog by rememberSaveable { mutableStateOf(false) }
    var showFundProjectDialog by rememberSaveable { mutableStateOf(false) }
    var showFundDatePickerDialog by rememberSaveable { mutableStateOf(false) }
    var showFundRecordDialog by rememberSaveable { mutableStateOf(false) }
    var editingProjectGroup by remember { mutableStateOf<ProjectGroupSummaryUi?>(null) }
    var editingStorage by remember { mutableStateOf<StorageRecordEntity?>(null) }
    var editingSecondarySale by remember { mutableStateOf<SecondarySaleRecordEntity?>(null) }
    var editingOutbound by remember { mutableStateOf<OutboundRecordEntity?>(null) }
    var editingFee by remember { mutableStateOf<FeeRecordEntity?>(null) }
    var editingPayment by remember { mutableStateOf<PaymentRecordEntity?>(null) }
    var editingFundRecord by remember { mutableStateOf<FundRecordEntity?>(null) }

    var selectedDayTab by rememberSaveable { mutableStateOf(0) }
    var selectedFundTab by rememberSaveable { mutableStateOf(0) }
    var newFundRecordType by rememberSaveable { mutableStateOf(FundRecordType.INCOME) }

    val itemNames by viewModel.uniqueItemNames.collectAsState()

    var showBackupDialog by rememberSaveable { mutableStateOf(false) }
    var showWorkerDialog by rememberSaveable { mutableStateOf(false) }
    var attendanceEditTarget by remember { mutableStateOf<AttendanceEditTarget?>(null) }
    val workers by remember(currentProjectId) {
        if (currentProjectId == null) {
            flowOf(emptyList())
        } else {
            viewModel.observeWorkers(currentProjectId)
        }
    }.collectAsState(initial = emptyList())

    val title = when {
        activePdf != null -> activePdf?.title ?: "PDF查看"
        else -> when (destinationRoute) {
        Routes.ProjectList -> "库存管理"
        Routes.ProjectOverview -> currentOverview?.name ?: "项目总览"
        Routes.DayDetail -> currentDayDetail?.date ?: "每日详情"
        Routes.Camera -> "拍照"
        Routes.Attendance -> "考勤看板"
        Routes.Summary -> "汇总"
        Routes.Funds -> "资金"
        Routes.FundProjectOverview -> currentFundOverview?.name ?: "资金项目"
        Routes.FundDayDetail -> currentFundDayDetail?.date ?: "资金明细"
        else -> "库存管理"
        }
    }

    val context = LocalContext.current
    val topLevelRoutes = setOf(Routes.ProjectList, Routes.Summary, Routes.Attendance, Routes.Funds)
    val showNavigateBack = activePdf != null || (destinationRoute !in topLevelRoutes && navController.previousBackStackEntry != null)

    BackHandler(enabled = destinationRoute == Routes.Summary || destinationRoute == Routes.Attendance || destinationRoute == Routes.Funds) {
        (context as? Activity)?.finish()
    }
    BackHandler(enabled = activePdf != null) {
        viewModel.closePdf()
    }

    fun navigateTopLevel(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

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
                    if (showNavigateBack) {
                        IconButton(onClick = {
                            if (activePdf != null) {
                                viewModel.closePdf()
                            } else {
                                navController.popBackStack()
                            }
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                        }
                    }
                },
                actions = {
                    if (activePdf != null) {
                        IconButton(
                            onClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "application/pdf"
                                    putExtra(Intent.EXTRA_STREAM, activePdf!!.uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "分享PDF"))
                            }
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "分享PDF")
                        }
                    } else {
                        IconButton(onClick = { showBackupDialog = true }) {
                            Icon(Icons.Default.Settings, contentDescription = "备份管理")
                        }
                        IconButton(onClick = { viewModel.toggleDarkMode() }) {
                            Icon(
                                imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "切换模式"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                )
            )
        },
        bottomBar = {
            if (destinationRoute != Routes.Camera && activePdf == null) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    NavigationBarItem(
                        selected = destinationRoute != Routes.Attendance && destinationRoute != Routes.Summary && destinationRoute != Routes.Funds,
                        onClick = { navigateTopLevel(Routes.ProjectList) },
                        icon = { Icon(Icons.Default.Inventory, contentDescription = null) },
                        label = { Text("库存") },
                    )
                    NavigationBarItem(
                        selected = destinationRoute == Routes.Summary,
                        onClick = { navigateTopLevel(Routes.Summary) },
                        icon = { Icon(Icons.Default.Inbox, contentDescription = null) },
                        label = { Text("汇总") },
                    )
                    NavigationBarItem(
                        selected = destinationRoute == Routes.Attendance,
                        onClick = { navigateTopLevel(Routes.Attendance) },
                        icon = { Icon(Icons.Default.History, contentDescription = null) },
                        label = { Text("考勤") },
                    )
                    NavigationBarItem(
                        selected = destinationRoute == Routes.Funds,
                        onClick = { navigateTopLevel(Routes.Funds) },
                        icon = { Icon(Icons.Default.Payments, contentDescription = null) },
                        label = { Text("资金") },
                    )
                }
            }
        },
        floatingActionButton = {
            when {
                activePdf != null -> Unit
                destinationRoute == Routes.DayDetail -> {
                    val fabColor = when (selectedDayTab) {
                        3 -> MaterialTheme.colorScheme.secondary
                        else -> MaterialTheme.colorScheme.primary
                    }
                    val fabIcon = when (selectedDayTab) {
                        4 -> Icons.Default.Payments
                        5 -> Icons.Default.Add
                        else -> Icons.Default.Add
                    }
                    
                    if (selectedDayTab != 0 && selectedDayTab != 5 && selectedDayTab != 6) {
                        FloatingActionButton(
                            onClick = {
                                when (selectedDayTab) {
                                    1 -> {
                                        editingStorage = null
                                        showStorageDialog = true
                                    }
                                    2 -> {
                                        editingSecondarySale = null
                                        showSecondarySaleDialog = true
                                    }
                                    3 -> {
                                        editingOutbound = null
                                        showOutboundDialog = true
                                    }
                                    4 -> {
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
                    } else if (selectedDayTab == 5) {
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
                destinationRoute == Routes.ProjectOverview -> {
                    FloatingActionButton(
                        onClick = { showDatePickerDialog = true },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Icon(Icons.Default.Add, "新建日期")
                    }
                }
                destinationRoute == Routes.ProjectList -> {
                    FloatingActionButton(
                        onClick = { showProjectDialog = true },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Icon(Icons.Default.Add, "新建项目")
                    }
                }
                destinationRoute == Routes.FundProjectOverview -> {
                    FloatingActionButton(
                        onClick = { showFundDatePickerDialog = true },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Icon(Icons.Default.Add, "新建日期")
                    }
                }
                destinationRoute == Routes.Funds -> {
                    FloatingActionButton(
                        onClick = { showFundProjectDialog = true },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Icon(Icons.Default.Add, "新建资金项目")
                    }
                }
                destinationRoute == Routes.FundDayDetail -> {
                    if (selectedFundTab != 0) {
                        FloatingActionButton(
                            onClick = {
                                editingFundRecord = null
                                newFundRecordType = when (selectedFundTab) {
                                    1 -> FundRecordType.INITIAL
                                    2 -> FundRecordType.INCOME
                                    else -> FundRecordType.EXPENSE
                                }
                                showFundRecordDialog = true
                            },
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        ) {
                            Icon(Icons.Default.Add, "新增资金记录")
                        }
                    }
                }
                else -> Unit
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding),
        ) {
            if (activePdf != null) {
                PdfViewerScreen(
                    pdfUri = activePdf!!.uri,
                    modifier = Modifier.fillMaxSize(),
                )
            } else NavHost(
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
                        val attendanceBoards by remember(detail.projectId) {
                            viewModel.observeAttendanceMonthBoards(detail.projectId)
                        }.collectAsState(initial = emptyList())
                        DayDetailScreen(
                            detail = detail,
                            selectedTab = selectedDayTab,
                            onTabSelected = { selectedDayTab = it },
                            attendanceBoards = attendanceBoards,
                            workers = workers,
                            onDeletePhoto = viewModel::deleteDayPhoto,
                            onEditStorage = {
                                editingStorage = it
                                showStorageDialog = true
                            },
                            onDeleteStorage = viewModel::deleteStorageRecord,
                            onEditSecondarySale = {
                                editingSecondarySale = it
                                showSecondarySaleDialog = true
                            },
                            onDeleteSecondarySale = viewModel::deleteSecondarySaleRecord,
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
                            onAddWorker = { showWorkerDialog = true },
                            onDeleteWorker = viewModel::deleteWorker,
                            onToggleWorkerVisibility = viewModel::toggleWorkerVisibility,
                            onAddAttendanceForCell = { workerId, workerName, date ->
                                val worker = workers.firstOrNull { it.id == workerId }
                                if (worker != null) {
                                    attendanceEditTarget = AttendanceEditTarget(
                                        workerId = workerId,
                                        workerName = workerName,
                                        date = date,
                                        record = null,
                                        hourlyRate = worker.hourlyRate,
                                        dailyRate = worker.dailyRate,
                                        cellColor = 0,
                                    )
                                }
                            },
                            onEditAttendance = { workerId, workerName, record ->
                                val worker = workers.firstOrNull { it.id == workerId }
                                attendanceEditTarget = AttendanceEditTarget(
                                    workerId = workerId,
                                    workerName = workerName,
                                    date = record.date,
                                    record = record,
                                    hourlyRate = if (record.hourlyRateSnapshot > 0.0) record.hourlyRateSnapshot else worker?.hourlyRate ?: 0.0,
                                    dailyRate = if (record.dailyRateSnapshot > 0.0) record.dailyRateSnapshot else worker?.dailyRate ?: 0.0,
                                    cellColor = record.cellColor,
                                )
                            },
                            onDeleteAttendance = viewModel::deleteAttendanceRecord,
                            onExportAttendanceCsv = { month, rows ->
                                ExportManager.exportAttendanceBoardToCsv(context, detail.projectName, month, rows)
                            },
                            onExportAttendancePdf = { month, rows ->
                                ExportManager.exportAttendanceBoardToPdf(context, detail.projectName, month, rows)
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
                composable(Routes.Attendance) {
                    var selectedProject by remember { mutableStateOf<AttendanceProjectSummary?>(null) }
                    val projectAttendanceSummaries by remember {
                        viewModel.observeProjectAttendanceSummaries()
                    }.collectAsState(initial = emptyList())
                    val selected = selectedProject
                    BackHandler(enabled = selected != null) {
                        selectedProject = null
                    }
                    if (selected == null) {
                        AttendanceProjectSummaryScreen(
                            summaries = projectAttendanceSummaries,
                            onOpenProject = { selectedProject = it },
                        )
                    } else {
                        val boards by remember(selected.projectId) {
                            viewModel.observeAttendanceMonthBoards(selected.projectId)
                        }.collectAsState(initial = emptyList())
                        AttendanceProjectBoardScreen(
                            projectName = selected.projectName,
                            boards = boards,
                            onBack = { selectedProject = null },
                            onExportCsv = { month, rows ->
                                ExportManager.exportAttendanceBoardToCsv(context, selected.projectName, month, rows)
                            },
                            onExportPdf = { month, rows ->
                                ExportManager.exportAttendanceBoardToPdf(context, selected.projectName, month, rows)
                            },
                        )
                    }
                }
                composable(Routes.Funds) {
                    FundProjectListScreen(
                        projects = fundProjects,
                        onOpenProject = { fundProjectId -> navController.navigate(Routes.fundProject(fundProjectId)) },
                        onDeleteProject = viewModel::deleteFundProject,
                    )
                }
                composable(
                    route = Routes.FundProjectOverview,
                    arguments = listOf(navArgument(NavArgs.FundProjectId) { type = NavType.LongType }),
                ) {
                    val fundProjectId = currentFundProjectId
                    val overview = currentFundOverview
                    if (fundProjectId == null || overview == null) {
                        EmptyState("资金项目不存在或已删除")
                    } else {
                        FundProjectOverviewScreen(
                            overview = overview,
                            onOpenDate = { dateId -> navController.navigate(Routes.fundDay(fundProjectId, dateId)) },
                            onDeleteProject = {
                                viewModel.deleteFundProject(fundProjectId)
                                navController.popBackStack(Routes.Funds, false)
                            },
                            onDeleteDate = viewModel::deleteFundDate,
                            onExportCsv = {
                                currentFundExport?.let { export ->
                                    ExportManager.exportFundProjectToCsv(context, export)
                                }
                            },
                            onExportPdf = {
                                currentFundExport?.let { export ->
                                    ExportManager.exportFundProjectToPdf(context, export)
                                }
                            },
                        )
                    }
                }
                composable(
                    route = Routes.FundDayDetail,
                    arguments = listOf(
                        navArgument(NavArgs.FundProjectId) { type = NavType.LongType },
                        navArgument(NavArgs.FundDateId) { type = NavType.LongType },
                    ),
                ) {
                    val detail = currentFundDayDetail
                    if (detail == null) {
                        EmptyState("资金日期不存在或已删除")
                    } else {
                        FundDayDetailScreen(
                            detail = detail,
                            selectedTab = selectedFundTab,
                            onTabSelected = { selectedFundTab = it },
                            onEditRecord = { record ->
                                editingFundRecord = record
                                newFundRecordType = record.type
                                showFundRecordDialog = true
                            },
                            onDeleteRecord = viewModel::deleteFundRecord,
                        )
                    }
                }
                composable(Routes.Summary) {
                    ProjectGroupScreen(
                        groups = projectGroups,
                        onCreateGroup = {
                            editingProjectGroup = null
                            showProjectGroupDialog = true
                        },
                        onEditGroup = {
                            editingProjectGroup = it
                            showProjectGroupDialog = true
                        },
                        onDeleteGroup = { viewModel.deleteProjectGroup(it.id) },
                    )
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

    if (showFundProjectDialog) {
        FundProjectDialog(
            onDismiss = { showFundProjectDialog = false },
            onConfirm = { name ->
                viewModel.createFundProject(name)
                showFundProjectDialog = false
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

    if (showProjectGroupDialog) {
        ProjectGroupDialog(
            group = editingProjectGroup,
            projects = projects,
            onDismiss = {
                showProjectGroupDialog = false
                editingProjectGroup = null
            },
            onConfirm = { groupId, name, projectIds ->
                if (groupId == null) {
                    viewModel.createProjectGroup(name, projectIds)
                } else {
                    viewModel.updateProjectGroup(groupId, name, projectIds)
                }
                showProjectGroupDialog = false
                editingProjectGroup = null
            },
        )
    }

    if (showFundDatePickerDialog) {
        FundDatePickerDialog(
            onDismiss = { showFundDatePickerDialog = false },
            onConfirm = { date ->
                val fundProjectId = currentFundProjectId ?: return@FundDatePickerDialog
                coroutineScope.launch {
                    val dateId = viewModel.createOrGetFundDate(fundProjectId, date)
                    showFundDatePickerDialog = false
                    navController.navigate(Routes.fundDay(fundProjectId, dateId))
                }
            },
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

    if (showSecondarySaleDialog && currentDayDetail != null) {
        val detail = currentDayDetail
        SecondarySaleRecordDialog(
            record = editingSecondarySale,
            onDismiss = {
                showSecondarySaleDialog = false
                editingSecondarySale = null
            },
            onConfirm = { name, weight, unitPrice ->
                if (detail != null) {
                    val record = editingSecondarySale
                    if (record == null) {
                        viewModel.addSecondarySaleRecord(detail.dateId, name, weight, unitPrice)
                    } else {
                        viewModel.updateSecondarySaleRecord(record, name, weight, unitPrice)
                    }
                    showSecondarySaleDialog = false
                    editingSecondarySale = null
                }
            },
            itemNames = itemNames,
            onGetLastWeight = { name: String ->
                viewModel.getLastWeightForItem(name)
            },
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

    if (showWorkerDialog) {
        WorkerDialog(
            onDismiss = { showWorkerDialog = false },
            onConfirm = { name ->
                val projectId = currentDayDetail?.projectId ?: return@WorkerDialog
                viewModel.createWorker(projectId, name)
                showWorkerDialog = false
            },
        )
    }

    if (showFundRecordDialog) {
        FundRecordDialog(
            record = editingFundRecord,
            type = if (editingFundRecord == null) newFundRecordType else editingFundRecord!!.type,
            onDismiss = {
                showFundRecordDialog = false
                editingFundRecord = null
            },
            onConfirm = { name, amount, remark ->
                val dateId = currentFundDayDetail?.dateId ?: return@FundRecordDialog
                if (editingFundRecord == null) {
                    viewModel.addFundRecord(dateId, newFundRecordType, name, amount, remark)
                } else {
                    viewModel.updateFundRecord(editingFundRecord!!, name, amount, remark)
                }
                showFundRecordDialog = false
                editingFundRecord = null
            },
        )
    }

    attendanceEditTarget?.let { target ->
        AttendanceRecordDialog(
            target = target,
            onDismiss = { attendanceEditTarget = null },
            onConfirm = { record, workerId, date, salaryMode, startTime, endTime, isPresent, attendancePortion, hourlyRate, dailyRate, overtimeHours, overtimeRate, cellColor ->
                if (record == null) {
                    val projectId = currentDayDetail?.projectId ?: return@AttendanceRecordDialog
                    viewModel.saveAttendanceRecord(projectId, workerId, date, salaryMode, startTime, endTime, isPresent, attendancePortion, hourlyRate, dailyRate, overtimeHours, overtimeRate, cellColor)
                } else {
                    viewModel.updateAttendanceRecord(record.id, date, salaryMode, startTime, endTime, isPresent, attendancePortion, hourlyRate, dailyRate, overtimeHours, overtimeRate, cellColor)
                }
                attendanceEditTarget = null
            },
        )
    }
}

private object NavArgs {
    const val ProjectId = "projectId"
    const val DateId = "dateId"
    const val FundProjectId = "fundProjectId"
    const val FundDateId = "fundDateId"
}

private object Routes {
    const val ProjectList = "projects"
    const val ProjectOverview = "project/{projectId}"
    const val DayDetail = "project/{projectId}/day/{dateId}"
    const val Camera = "project/{projectId}/day/{dateId}/camera"
    const val Attendance = "attendance"
    const val Summary = "summary"
    const val Funds = "funds"
    const val FundProjectOverview = "funds/{fundProjectId}"
    const val FundDayDetail = "funds/{fundProjectId}/day/{fundDateId}"

    fun project(projectId: Long): String = "project/$projectId"
    fun day(projectId: Long, dateId: Long): String = "project/$projectId/day/$dateId"
    fun camera(projectId: Long, dateId: Long): String = "project/$projectId/day/$dateId/camera"
    fun fundProject(projectId: Long): String = "funds/$projectId"
    fun fundDay(projectId: Long, dateId: Long): String = "funds/$projectId/day/$dateId"
}

private data class AttendanceEditTarget(
    val workerId: Long,
    val workerName: String,
    val date: String,
    val record: AttendanceEntity?,
    val hourlyRate: Double,
    val dailyRate: Double,
    val cellColor: Int,
)

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

@Composable
private fun NativeDatePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    val today = remember { LocalDate.now() }
    var year by rememberSaveable { mutableStateOf(today.year) }
    var month by rememberSaveable { mutableStateOf(today.monthValue) }
    val maxDay = remember(year, month) { YearMonth.of(year, month).lengthOfMonth() }
    var day by rememberSaveable { mutableStateOf(today.dayOfMonth) }

    if (day > maxDay) {
        day = maxDay
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择日期") },
        text = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NumberDropdown(
                    value = year,
                    values = ((today.year - 5)..(today.year + 2)).toList(),
                    suffix = "年",
                    onValueChange = { year = it },
                    modifier = Modifier.weight(1.25f),
                )
                NumberDropdown(
                    value = month,
                    values = (1..12).toList(),
                    suffix = "月",
                    onValueChange = { month = it },
                    modifier = Modifier.weight(1f),
                )
                NumberDropdown(
                    value = day,
                    values = (1..maxDay).toList(),
                    suffix = "日",
                    onValueChange = { day = it },
                    modifier = Modifier.weight(1f),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm("%04d-%02d-%02d".format(year, month, day))
            }) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        },
    )
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
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Text(
                "我的项目",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 2.dp)
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
                shape = RoundedCornerShape(12.dp),
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
    val configuration = LocalConfiguration.current
    val compactLayout = configuration.screenWidthDp < 380

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = { },
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(if (compactLayout) 36.dp else 40.dp),
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
                        style = MaterialTheme.typography.titleMedium.copy(
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
                Spacer(modifier = Modifier.width(10.dp))
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(
                        project.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = if (compactLayout) 2 else 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                IconButton(
                    modifier = Modifier.size(36.dp),
                    onClick = { showDeleteConfirm = true },
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "删除",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            TotalsCompactText(project.totals, compactLayout = compactLayout)
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
    var paymentExpanded by rememberSaveable { mutableStateOf(false) }
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
        if (overview.totals.secondarySaleSummaries.isNotEmpty()) {
            item {
                SecondarySaleSummaryCard(
                    title = "卖出次果汇总",
                    totals = overview.totals,
                )
            }
        }
        
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .clickable { paymentExpanded = !paymentExpanded }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("付款记录", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Icon(
                        if (paymentExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "展开/收起",
                        modifier = Modifier.padding(start = 4.dp).size(22.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                TextButton(onClick = onAddPayment) {
                    Text("登记付款", style = MaterialTheme.typography.labelLarge)
                }
            }
        }

        if (paymentExpanded) {
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
private fun ProjectGroupScreen(
    groups: List<ProjectGroupSummaryUi>,
    onCreateGroup: () -> Unit,
    onEditGroup: (ProjectGroupSummaryUi) -> Unit,
    onDeleteGroup: (ProjectGroupSummaryUi) -> Unit,
) {
    var selectedGroupId by rememberSaveable { mutableStateOf<Long?>(null) }
    val selectedGroup = groups.firstOrNull { it.id == selectedGroupId }
    BackHandler(enabled = selectedGroup != null) {
        selectedGroupId = null
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("项目汇总", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("从已创建项目中组合汇总", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                TextButton(onClick = onCreateGroup) {
                    Text("新建汇总")
                }
            }
        }

        if (groups.isEmpty()) {
            item { EmptyState("还没有汇总，点击右上角新建。") }
        } else if (selectedGroup == null) {
            items(groups, key = { it.id }) { group ->
                ProjectGroupCard(
                    group = group,
                    onOpen = { selectedGroupId = group.id },
                    onEdit = { onEditGroup(group) },
                    onDelete = { onDeleteGroup(group) },
                )
            }
        } else {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = { selectedGroupId = null }) {
                        Text("返回列表")
                    }
                    TextButton(onClick = { onEditGroup(selectedGroup) }) {
                        Text("增删项目")
                    }
                }
            }
            item {
                SummaryCard(
                    title = selectedGroup.name,
                    totals = selectedGroup.totals,
                    isProjectTotal = true,
                )
            }
            if (selectedGroup.totals.secondarySaleSummaries.isNotEmpty()) {
                item {
                    SecondarySaleSummaryCard(
                        title = "${selectedGroup.name} · 卖出次果",
                        totals = selectedGroup.totals,
                    )
                }
            }
            item {
                Text("包含项目", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            items(selectedGroup.projects, key = { it.id }) { project ->
                ProjectCard(project = project, onClick = {}, onDelete = {})
            }
        }
    }
}

@Composable
private fun ProjectGroupCard(
    group: ProjectGroupSummaryUi,
    onOpen: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .combinedClickable(onClick = onOpen, onLongClick = {}),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(group.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("${group.projects.size} 个项目", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                TextButton(onClick = onEdit) { Text("编辑") }
                IconButton(modifier = Modifier.size(34.dp), onClick = { showDeleteConfirm = true }) {
                    Icon(Icons.Default.Delete, contentDescription = "删除汇总", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error)
                }
            }
            TotalsCompactText(group.totals)
            Text(
                group.projects.joinToString("、") { it.name },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }

    if (showDeleteConfirm) {
        DeleteConfirmDialog(
            title = "删除汇总",
            message = "确定删除“${group.name}”吗？不会删除原项目。",
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
                if (dateSummary.totals.secondarySaleSummaries.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.9f),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                "卖出次果",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                            )
                            Text(
                                "${formatNumber(dateSummary.totals.totalSecondarySaleWeight)}斤 · ¥${formatNumber(dateSummary.totals.totalSecondarySaleAmount)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.85f),
                            )
                        }
                    }
                }
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
    attendanceBoards: List<AttendanceMonthBoard>,
    workers: List<WorkerEntity>,
    onDeletePhoto: (DayPhotoEntity) -> Unit,
    onEditStorage: (StorageRecordEntity) -> Unit,
    onDeleteStorage: (StorageRecordEntity) -> Unit,
    onEditSecondarySale: (SecondarySaleRecordEntity) -> Unit,
    onDeleteSecondarySale: (SecondarySaleRecordEntity) -> Unit,
    onEditOutbound: (OutboundRecordEntity) -> Unit,
    onDeleteOutbound: (OutboundRecordEntity) -> Unit,
    onEditFee: (FeeRecordEntity) -> Unit,
    onDeleteFee: (FeeRecordEntity) -> Unit,
    onTakePhoto: () -> Unit,
    onAddFee: () -> Unit,
    onAddWorker: () -> Unit,
    onDeleteWorker: (WorkerEntity) -> Unit,
    onToggleWorkerVisibility: (WorkerEntity, String) -> Unit,
    onAddAttendanceForCell: (Long, String, String) -> Unit,
    onEditAttendance: (Long, String, AttendanceEntity) -> Unit,
    onDeleteAttendance: (AttendanceEntity) -> Unit,
    onExportAttendanceCsv: (String, List<AttendanceDashboardRow>) -> Unit,
    onExportAttendancePdf: (String, List<AttendanceDashboardRow>) -> Unit,
    onExportCsv: (DayDetailUi) -> Unit,
    onExportPdf: (DayDetailUi) -> Unit,
) {
    val tabs = listOf("汇总", "入库清单", "卖出次果", "出库清单", "费用支出", "现场照片", "考勤表")
    
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
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(selectedTab) {
                    var dragOffset = 0f
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { _, dragAmount -> dragOffset += dragAmount },
                        onDragEnd = {
                            when {
                                dragOffset > 80f -> onTabSelected((selectedTab - 1).coerceAtLeast(0))
                                dragOffset < -80f -> onTabSelected((selectedTab + 1).coerceAtMost(tabs.lastIndex))
                            }
                            dragOffset = 0f
                        },
                        onDragCancel = { dragOffset = 0f },
                    )
                },
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
                    if (detail.totals.secondarySaleSummaries.isNotEmpty()) {
                        item {
                            SecondarySaleSummaryCard(
                                title = "当日卖出次果",
                                totals = detail.totals,
                            )
                        }
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
                    if (detail.secondarySaleRecords.isEmpty()) {
                        item { EmptyState("暂无卖出次果记录") }
                    } else {
                        items(detail.secondarySaleRecords, key = { "secondary_${it.id}" }) { record ->
                            SecondarySaleRecordCard(
                                record = record,
                                onEdit = { onEditSecondarySale(record) },
                                onDelete = { onDeleteSecondarySale(record) },
                            )
                        }
                    }
                }
                3 -> {
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
                4 -> {
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
                5 -> {
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
                6 -> {
                    item {
                        AttendanceTabContent(
                            currentMonth = detail.date.take(7),
                            workers = workers,
                            boards = attendanceBoards,
                            onAddWorker = onAddWorker,
                            onDeleteWorker = onDeleteWorker,
                            onToggleWorkerVisibility = onToggleWorkerVisibility,
                            onAddAttendanceForCell = onAddAttendanceForCell,
                            onEditAttendance = onEditAttendance,
                            onDeleteAttendance = onDeleteAttendance,
                            onExportCsv = onExportAttendanceCsv,
                            onExportPdf = onExportAttendancePdf,
                        )
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }
}

@Composable
private fun AttendanceTabContent(
    currentMonth: String,
    workers: List<WorkerEntity>,
    boards: List<AttendanceMonthBoard>,
    onAddWorker: () -> Unit,
    onDeleteWorker: (WorkerEntity) -> Unit,
    onToggleWorkerVisibility: (WorkerEntity, String) -> Unit,
    onAddAttendanceForCell: (Long, String, String) -> Unit,
    onEditAttendance: (Long, String, AttendanceEntity) -> Unit,
    onDeleteAttendance: (AttendanceEntity) -> Unit,
    onExportCsv: (String, List<AttendanceDashboardRow>) -> Unit,
    onExportPdf: (String, List<AttendanceDashboardRow>) -> Unit,
) {
    var workersExpanded by rememberSaveable { mutableStateOf(false) }
    val currentBoard = remember(currentMonth, workers, boards) {
        val board = boards.firstOrNull { it.month == currentMonth }
        val currentRows = board?.rows.orEmpty()
        val existingWorkerIds = currentRows.map { it.summary.workerId }.toSet()
        val missingRows = workers
            .filterNot { it.id in existingWorkerIds }
            .filterNot { it.hiddenMonths.split(",").contains(currentMonth) }
            .map { worker ->
                AttendanceDashboardRow(
                    summary = AttendanceSummary(
                        workerId = worker.id,
                        workerName = worker.name,
                        salaryMode = SalaryMode.HOURLY,
                        totalWorkHours = 0.0,
                        totalPresentDays = 0.0,
                        totalSalary = 0.0,
                    ),
                    cellsByDay = emptyMap(),
                )
            }

        if (board == null) {
            AttendanceMonthBoard(
                month = currentMonth,
                rows = missingRows,
            )
        } else {
            board.copy(rows = board.rows + missingRows)
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text("考勤表", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("$currentMonth", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row {
                TextButton(onClick = onAddWorker) {
                    Text("新增人员")
                }
            }
        }

        if (workers.isEmpty()) {
            EmptyState("还没有人员，先新增人员后再添加考勤。")
        } else {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.24f),
            ) {
                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { workersExpanded = !workersExpanded },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text("人员名单", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("${workers.size} 人", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextButton(onClick = onAddWorker) {
                                Text("新增")
                            }
                            Icon(
                                if (workersExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = if (workersExpanded) "收起人员名单" else "展开人员名单",
                            )
                        }
                    }
                    if (workersExpanded) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            workers.forEach { worker ->
                                WorkerCompactRow(
                                    worker = worker,
                                    isHiddenForCurrentMonth = worker.hiddenMonths.split(",").contains(currentMonth),
                                    onToggleVisibility = { onToggleWorkerVisibility(worker, currentMonth) },
                                    onDelete = { onDeleteWorker(worker) }
                                )
                            }
                        }
                    }
                }
            }
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(currentBoard.month, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("当前日期所在月份", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = { onExportCsv(currentBoard.month, currentBoard.rows) }, enabled = currentBoard.rows.isNotEmpty()) {
                                Text("导出CSV")
                            }
                            TextButton(onClick = { onExportPdf(currentBoard.month, currentBoard.rows) }, enabled = currentBoard.rows.isNotEmpty()) {
                                Text("导出PDF")
                            }
                        }
                    }
                    AttendanceBoardScreen(
                        month = currentBoard.month,
                        rows = currentBoard.rows,
                        modifier = Modifier.fillMaxWidth(),
                        enableVerticalScroll = false,
                        onAddRecord = onAddAttendanceForCell,
                        onEditRecord = onEditAttendance,
                        onDeleteRecord = onDeleteAttendance,
                    )
                }
            }
        }
    }
}

@Composable
private fun AttendanceProjectSummaryScreen(
    summaries: List<AttendanceProjectSummary>,
    onOpenProject: (AttendanceProjectSummary) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("项目考勤汇总", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("跨月份汇总", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (summaries.isEmpty()) {
            item { EmptyState("暂无项目数据") }
        } else {
            items(summaries, key = { it.projectId }) { summary ->
                AttendanceProjectSummaryCard(summary = summary, onClick = { onOpenProject(summary) })
            }
        }
    }
}

@Composable
private fun AttendanceProjectBoardScreen(
    projectName: String,
    boards: List<AttendanceMonthBoard>,
    onBack: () -> Unit,
    onExportCsv: (String, List<AttendanceDashboardRow>) -> Unit,
    onExportPdf: (String, List<AttendanceDashboardRow>) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(projectName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("按月份分表展示", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                TextButton(onClick = onBack) {
                    Text("返回")
                }
            }
        }
        if (boards.isEmpty()) {
            item { EmptyState("这个项目还没有考勤数据") }
        } else {
            items(boards, key = { it.month }) { board ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f),
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(board.month, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TextButton(onClick = { onExportCsv(board.month, board.rows) }, enabled = board.rows.isNotEmpty()) {
                                    Text("导出CSV")
                                }
                                TextButton(onClick = { onExportPdf(board.month, board.rows) }, enabled = board.rows.isNotEmpty()) {
                                    Text("导出PDF")
                                }
                            }
                        }
                        AttendanceBoardScreen(
                            month = board.month,
                            rows = board.rows,
                            modifier = Modifier.fillMaxWidth(),
                            enableVerticalScroll = false,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AttendanceProjectSummaryCard(
    summary: AttendanceProjectSummary,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .combinedClickable(onClick = onClick, onLongClick = {}),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(summary.projectName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CompactMetric("工时", "${formatNumber(summary.totalWorkHours)}h", Modifier.weight(1f), highlight = true)
                CompactMetric("天数", "${formatNumber(summary.totalPresentDays)}天", Modifier.weight(1f), highlight = true)
                CompactMetric("工资", formatCurrency(summary.totalSalary), Modifier.weight(1f), highlight = true)
            }
        }
    }
}

@Composable
private fun WorkerCompactRow(
    worker: WorkerEntity,
    isHiddenForCurrentMonth: Boolean,
    onToggleVisibility: () -> Unit,
    onDelete: () -> Unit,
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(worker.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Text(
                    if (worker.salaryMode == SalaryMode.HOURLY) "计时 · ¥${formatNumber(worker.hourlyRate)}/小时" else "计天 · ¥${formatNumber(worker.dailyRate)}/天",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(modifier = Modifier.size(32.dp), onClick = onToggleVisibility) {
                    Icon(
                        if (isHiddenForCurrentMonth) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (isHiddenForCurrentMonth) "显示该人员" else "隐藏该人员",
                        modifier = Modifier.size(18.dp),
                        tint = if (isHiddenForCurrentMonth) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(modifier = Modifier.size(32.dp), onClick = { showDeleteConfirm = true }) {
                    Icon(Icons.Default.Delete, contentDescription = "删除人员", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }

    if (showDeleteConfirm) {
        DeleteConfirmDialog(
            title = "删除考勤人员",
            message = "确定删除“${worker.name}”吗？该人员的考勤记录也会一起删除。",
            onDismiss = { showDeleteConfirm = false },
            onConfirm = {
                showDeleteConfirm = false
                onDelete()
            },
        )
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
private fun SecondarySaleRecordCard(
    record: SecondarySaleRecordEntity,
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
                        Icons.Default.Payments,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(record.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                Row(modifier = Modifier.fillMaxWidth()) {
                    InfoItem("斤数", "${formatNumber(record.weight)}斤", Modifier.weight(1f))
                    InfoItem("单价", formatCurrency(record.unitPrice), Modifier.weight(1f))
                    InfoItem("总额", formatCurrency(record.totalAmount), Modifier.weight(1f))
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
            title = "删除卖出次果记录",
            message = "确定要删除“${record.name}”的卖出次果记录吗？",
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
private fun SecondarySaleRecordDialog(
    record: SecondarySaleRecordEntity?,
    itemNames: List<String>,
    onGetLastWeight: suspend (String) -> Double?,
    onDismiss: () -> Unit,
    onConfirm: (String, Double, Double) -> Unit,
) {
    var name by remember(record?.id) { mutableStateOf(record?.name.orEmpty()) }
    var weight by remember(record?.id) { mutableStateOf(record?.weight?.toText().orEmpty()) }
    var unitPrice by remember(record?.id) { mutableStateOf(record?.unitPrice?.toText().orEmpty()) }
    var showHistory by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val nameInteractionSource = remember { MutableInteractionSource() }
    val isNamePressed by nameInteractionSource.collectIsPressedAsState()

    val weightValue = weight.toDoubleOrNull()
    val unitPriceValue = unitPrice.toDoubleOrNull()
    val canSave = name.isNotBlank() && weightValue != null && unitPriceValue != null
    val filteredItemNames = remember(itemNames, name) {
        if (name.isBlank()) itemNames else itemNames.filter { it.contains(name, ignoreCase = true) }
    }

    LaunchedEffect(isNamePressed) {
        if (isNamePressed && itemNames.isNotEmpty()) {
            showHistory = true
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (record == null) "登记卖出次果" else "修改卖出次果") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Box {
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            showHistory = itemNames.isNotEmpty()
                        },
                        label = { Text("项目名称") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { state ->
                                if (state.isFocused && itemNames.isNotEmpty()) {
                                    showHistory = true
                                }
                            },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(Icons.Default.Inventory, null) },
                        interactionSource = nameInteractionSource,
                    )
                    if (showHistory && filteredItemNames.isNotEmpty()) {
                        DropdownMenu(
                            expanded = showHistory,
                            onDismissRequest = { showHistory = false },
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .heightIn(max = 240.dp),
                            properties = PopupProperties(focusable = false)
                        ) {
                            filteredItemNames.forEach { historyName ->
                                DropdownMenuItem(
                                    leadingIcon = { Icon(Icons.Default.History, null, modifier = Modifier.size(18.dp)) },
                                    text = { Text(historyName) },
                                    onClick = {
                                        name = historyName
                                        showHistory = false
                                        scope.launch {
                                            val lastWeight = onGetLastWeight(historyName)
                                            if (lastWeight != null) {
                                                weight = formatNumber(lastWeight)
                                            }
                                        }
                                    },
                                )
                            }
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = weight,
                        onValueChange = { weight = it },
                        label = { Text("斤数") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = unitPrice,
                        onValueChange = { unitPrice = it },
                        label = { Text("单价") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                if (canSave) {
                    val totalAmount = weightValue!! * unitPriceValue!!
                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("卖出次果汇总", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onTertiaryContainer)
                            Text(
                                "斤数: ${formatNumber(weightValue)}斤  |  单价: ${formatCurrency(unitPriceValue)}  |  总额: ${formatCurrency(totalAmount)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, weightValue!!, unitPriceValue!!) },
                enabled = canSave,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
            ) {
                Text("确认保存")
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
private fun SecondarySaleSummaryCard(
    title: String,
    totals: Totals,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.78f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Payments, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onTertiaryContainer)
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onTertiaryContainer)
            }

            totals.secondarySaleSummaries.forEach { summary ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(summary.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onTertiaryContainer)
                        Text(
                            "斤数 ${formatNumber(summary.totalWeight)}斤",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.75f)
                        )
                    }
                    Text(
                        formatCurrency(summary.totalAmount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                }
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.14f))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "合计 ${formatNumber(totals.totalSecondarySaleWeight)}斤",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                )
                Text(
                    formatCurrency(totals.totalSecondarySaleAmount),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                )
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
private fun TotalsCompactText(totals: Totals, compactLayout: Boolean = false) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(1.dp)) {
        if (compactLayout) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CompactMetric("入库", "${formatNumber(totals.totalCount)}件", Modifier.weight(1f))
                CompactMetric("出库", "${formatNumber(totals.totalOutboundCount)}件", Modifier.weight(1f))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CompactMetric("库存", "${formatNumber(totals.netTotalCount)}件", Modifier.weight(1f), highlight = true)
                CompactMetric("净重", "${formatNumber(totals.netTotalWeight)}斤", Modifier.weight(1f))
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CompactMetric("入库", "${formatNumber(totals.totalCount)}件", Modifier.weight(1f))
                CompactMetric("出库", "${formatNumber(totals.totalOutboundCount)}件", Modifier.weight(1f))
                CompactMetric("库存", "${formatNumber(totals.netTotalCount)}件", Modifier.weight(1f), highlight = true)
            }
        }
        Text(
            "支出 ${formatCurrency(totals.totalFee)}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun CompactMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    highlight: Boolean = false,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
        Text(
            value,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (highlight) FontWeight.Bold else FontWeight.Medium,
            color = if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
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
private fun ProjectGroupDialog(
    group: ProjectGroupSummaryUi?,
    projects: List<ProjectSummaryUi>,
    onDismiss: () -> Unit,
    onConfirm: (Long?, String, List<Long>) -> Unit,
) {
    var name by remember(group?.id) { mutableStateOf(group?.name.orEmpty()) }
    var selectedIds by remember(group?.id, projects) {
        mutableStateOf(group?.projects?.map { it.id }?.toSet().orEmpty())
    }
    val canSave = name.isNotBlank() && selectedIds.isNotEmpty()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (group == null) "新建汇总" else "编辑汇总") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("汇总名称") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                )
                Text("选择已创建项目", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (projects.isEmpty()) {
                    Text("还没有项目，先去库存页创建项目。", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 320.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        items(projects, key = { it.id }) { project ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        selectedIds = if (project.id in selectedIds) {
                                            selectedIds - project.id
                                        } else {
                                            selectedIds + project.id
                                        }
                                    }
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Checkbox(
                                    checked = project.id in selectedIds,
                                    onCheckedChange = { checked ->
                                        selectedIds = if (checked) selectedIds + project.id else selectedIds - project.id
                                    },
                                )
                                Column {
                                    Text(project.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                    Text(
                                        "库存 ${formatNumber(project.totals.netTotalCount)}件 · 欠款 ${formatCurrency(project.totals.totalDebt)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(group?.id, name, selectedIds.toList()) },
                enabled = canSave,
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("保存汇总")
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
private fun WorkerDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var name by rememberSaveable { mutableStateOf("") }
    val canSave = name.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("新增考勤人员") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("姓名") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                )
                Text(
                    "这里只添加人名。考勤方式和当天薪资，在点单元格录考勤时再填写。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(name)
                },
                enabled = canSave,
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("保存人员")
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
private fun AttendanceRecordDialog(
    target: AttendanceEditTarget,
    onDismiss: () -> Unit,
    onConfirm: (AttendanceEntity?, Long, String, Int, String?, String?, Boolean, Double, Double?, Double?, Double, Double, Int) -> Unit,
) {
    val initialStartParts = remember(target.record?.id) { parseTimeParts(target.record?.startTime, 8, 0) }
    val initialEndParts = remember(target.record?.id) { parseTimeParts(target.record?.endTime, 18, 0) }
    var mode by remember(target.record?.id) {
        mutableStateOf(
            if (target.record?.salaryModeSnapshot == SalaryMode.DAILY) {
                SalaryMode.DAILY
            } else {
                SalaryMode.HOURLY
            }
        )
    }
    var startHour by remember(target.record?.id) { mutableStateOf(initialStartParts.first) }
    var startMinute by remember(target.record?.id) { mutableStateOf(initialStartParts.second) }
    var endHour by remember(target.record?.id) { mutableStateOf(initialEndParts.first) }
    var endMinute by remember(target.record?.id) { mutableStateOf(initialEndParts.second) }
    var isPresent by remember(target.record?.id) { mutableStateOf(target.record?.isPresent ?: true) }
    var attendancePortion by remember(target.record?.id) {
        mutableStateOf(
            target.record?.attendancePortion?.takeIf { it > 0.0 }
                ?: if (target.record?.salaryModeSnapshot == SalaryMode.DAILY) 1.0 else 0.0,
        )
    }
    var hourlyRateText by remember(target.record?.id, target.hourlyRate) { mutableStateOf(target.hourlyRate.toText()) }
    val initialFullDayRate = remember(target.record?.id, target.dailyRate) {
        when {
            target.record?.salaryModeSnapshot == SalaryMode.DAILY && target.record.attendancePortion in 0.49..0.51 ->
                (target.dailyRate * 2).toText()
            else -> target.dailyRate.toText()
        }
    }
    val initialHalfDayRate = remember(target.record?.id, target.dailyRate) {
        when {
            target.record?.salaryModeSnapshot == SalaryMode.DAILY && target.record.attendancePortion in 0.49..0.51 ->
                target.dailyRate.toText()
            target.dailyRate > 0.0 -> (target.dailyRate / 2.0).toText()
            else -> ""
        }
    }
    var fullDayRateText by remember(target.record?.id, target.dailyRate) { mutableStateOf(initialFullDayRate) }
    var halfDayRateText by remember(target.record?.id, target.dailyRate) { mutableStateOf(initialHalfDayRate) }
    var overtimeHoursText by remember(target.record?.id) { mutableStateOf(target.record?.overtimeHours?.takeIf { it > 0.0 }?.toText().orEmpty()) }
    var overtimeRateText by remember(target.record?.id) { mutableStateOf(target.record?.overtimeRate?.takeIf { it > 0.0 }?.toText().orEmpty()) }
    var cellColor by remember(target.record?.id, target.cellColor) { mutableStateOf(target.cellColor) }

    val isHourly = mode == SalaryMode.HOURLY
    val startTime = "%02d:%02d".format(startHour, startMinute)
    val endTime = "%02d:%02d".format(endHour, endMinute)
    val hourlyRateValue = hourlyRateText.toDoubleOrNull()
    val isHalfDay = !isHourly && isPresent && attendancePortion in 0.49..0.51
    val dailyRateValue = if (isHalfDay) halfDayRateText.toDoubleOrNull() else fullDayRateText.toDoubleOrNull()
    val overtimeHoursValue = overtimeHoursText.toDoubleOrNull() ?: 0.0
    val overtimeRateValue = overtimeRateText.toDoubleOrNull() ?: 0.0
    val overtimeInputValid = (overtimeHoursText.isBlank() || overtimeHoursText.toDoubleOrNull() != null) &&
        (overtimeRateText.isBlank() || overtimeRateText.toDoubleOrNull() != null)
    val canSave = (if (isHourly) hourlyRateValue != null else dailyRateValue != null) && overtimeInputValid

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (target.record == null) "添加考勤" else "修改考勤") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("${target.workerName} · ${target.date}", style = MaterialTheme.typography.bodyMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { mode = SalaryMode.HOURLY },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = if (isHourly) ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors(),
                    ) {
                        Text("按时间")
                    }
                    Button(
                        onClick = { mode = SalaryMode.DAILY },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = if (!isHourly) ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors(),
                    ) {
                        Text("按天")
                    }
                }

                if (isHourly) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        TimePickerField(
                            label = "开始",
                            hour = startHour,
                            minute = startMinute,
                            onHourChange = { startHour = it },
                            onMinuteChange = { startMinute = it },
                            modifier = Modifier.weight(1f),
                        )
                        TimePickerField(
                            label = "结束",
                            hour = endHour,
                            minute = endMinute,
                            onHourChange = { endHour = it },
                            onMinuteChange = { endMinute = it },
                            modifier = Modifier.weight(1f),
                        )
                        OutlinedTextField(
                            value = hourlyRateText,
                            onValueChange = { hourlyRateText = it },
                            label = { Text("时薪") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        )
                    }
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Checkbox(
                                checked = isPresent,
                                onCheckedChange = { isPresent = it },
                            )
                            Text("已出勤")
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    isPresent = true
                                    attendancePortion = 1.0
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = if (attendancePortion >= 0.99 && isPresent) ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors(),
                            ) {
                                Text("全天")
                            }
                            OutlinedButton(
                                onClick = {
                                    isPresent = true
                                    attendancePortion = 0.5
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = if (attendancePortion in 0.49..0.51 && isPresent) ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors(),
                            ) {
                                Text("半天")
                            }
                        }
                        OutlinedTextField(
                            value = if (isHalfDay) halfDayRateText else fullDayRateText,
                            onValueChange = {
                                if (isHalfDay) {
                                    halfDayRateText = it
                                } else {
                                    fullDayRateText = it
                                }
                            },
                            label = { Text(if (isHalfDay) "半天工资" else "全天工资") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        )
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = overtimeHoursText,
                        onValueChange = { overtimeHoursText = it },
                        label = { Text("加班小时") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                    OutlinedTextField(
                        value = overtimeRateText,
                        onValueChange = { overtimeRateText = it },
                        label = { Text("加班单价") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                }
                Text(
                    "工资保存后会作为下次默认值",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (target.record != null) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "格子颜色",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            attendanceClassicColors.forEach { option ->
                                AttendanceColorChip(
                                    label = option.label,
                                    colorInt = option.colorInt,
                                    selected = cellColor == option.colorInt,
                                    onClick = { cellColor = option.colorInt },
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        target.record,
                        target.workerId,
                        target.date,
                        mode,
                        if (isHourly) startTime else null,
                        if (isHourly) endTime else null,
                        if (!isHourly) isPresent else true,
                        if (!isHourly && isPresent) attendancePortion else 0.0,
                        hourlyRateValue,
                        dailyRateValue,
                        overtimeHoursValue,
                        overtimeRateValue,
                        if (target.record == null) 0 else cellColor,
                    )
                },
                enabled = canSave,
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("保存考勤")
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
private fun AttendanceColorChip(
    label: String,
    colorInt: Int,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val swatchColor = resolveAttendanceCellColor(
        colorInt = colorInt,
        defaultColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
    )
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                shape = RoundedCornerShape(12.dp),
            )
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(swatchColor),
        )
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

private fun parseTimeParts(value: String?, defaultHour: Int, defaultMinute: Int): Pair<Int, Int> {
    val parts = value?.split(":")
    val hour = parts?.getOrNull(0)?.toIntOrNull() ?: defaultHour
    val minute = parts?.getOrNull(1)?.toIntOrNull() ?: defaultMinute
    return hour.coerceIn(0, 23) to minute.coerceIn(0, 59)
}

@Composable
private fun TimePickerField(
    label: String,
    hour: Int,
    minute: Int,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            NumberDropdown(
                value = hour,
                values = (0..23).toList(),
                suffix = "时",
                onValueChange = onHourChange,
                modifier = Modifier.weight(1f),
            )
            NumberDropdown(
                value = minute,
                values = (0..59 step 5).toList(),
                suffix = "分",
                onValueChange = onMinuteChange,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun NumberDropdown(
    value: Int,
    values: List<Int>,
    suffix: String,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
        ) {
            Text("%02d$suffix".format(value))
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.heightIn(max = 220.dp),
        ) {
            values.forEach { item ->
                DropdownMenuItem(
                    text = { Text("%02d$suffix".format(item)) },
                    onClick = {
                        onValueChange(item)
                        expanded = false
                    },
                )
            }
        }
    }
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
    val nameInteractionSource = remember { MutableInteractionSource() }
    val isNamePressed by nameInteractionSource.collectIsPressedAsState()

    val countValue = count.toDoubleOrNull()
    val weightValue = weightPerUnit.toDoubleOrNull()
    val priceValue = pricePerWeight.toDoubleOrNull() ?: 0.0
    val isPriceValid = pricePerWeight.isBlank() || pricePerWeight.toDoubleOrNull() != null
    val canSave = name.isNotBlank() && countValue != null && weightValue != null && isPriceValid
    val filteredItemNames = remember(itemNames, name) {
        if (name.isBlank()) {
            itemNames
        } else {
            itemNames.filter { it.contains(name, ignoreCase = true) }
        }
    }

    LaunchedEffect(isNamePressed) {
        if (isNamePressed && itemNames.isNotEmpty()) {
            showHistory = true
        }
    }

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
                            showHistory = itemNames.isNotEmpty()
                        },
                        label = { Text("物品/货物名称") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { state ->
                                if (state.isFocused && itemNames.isNotEmpty()) {
                                    showHistory = true
                                }
                            },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(Icons.Default.Inventory, null) },
                        interactionSource = nameInteractionSource
                    )
                    if (showHistory && filteredItemNames.isNotEmpty()) {
                        DropdownMenu(
                            expanded = showHistory,
                            onDismissRequest = { showHistory = false },
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .heightIn(max = 240.dp),
                            properties = PopupProperties(focusable = false)
                        ) {
                            filteredItemNames.forEach { historyName ->
                                DropdownMenuItem(
                                    leadingIcon = { Icon(Icons.Default.History, null, modifier = Modifier.size(18.dp)) },
                                    text = { Text(historyName) },
                                    onClick = {
                                        name = historyName
                                        showHistory = false
                                        scope.launch {
                                            val lastPrice = onGetLastPrice(historyName)
                                            if (lastPrice != null) {
                                                pricePerWeight = if (lastPrice == 0.0) "" else formatNumber(lastPrice)
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
                        label = { Text("单价(元/斤，可不填)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
                
                if (canSave) {
                    val totalWeight = countValue!! * weightValue!!
                    val totalPrice = totalWeight * priceValue
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
                onClick = { onConfirm(name, countValue!!, weightValue!!, priceValue) },
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
