package com.example.myapplication111.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.viewModelScope
import com.example.myapplication111.data.AttendanceDashboardRow
import com.example.myapplication111.data.AttendanceEntity
import com.example.myapplication111.data.AttendanceMonthBoard
import com.example.myapplication111.data.AttendanceProjectSummary
import com.example.myapplication111.data.AttendanceSummary
import com.example.myapplication111.data.DockNoteRepository
import com.example.myapplication111.data.DayDetailUi
import com.example.myapplication111.data.DayPhotoEntity
import com.example.myapplication111.data.ProjectOverviewUi
import com.example.myapplication111.data.ProjectGroupSummaryUi
import com.example.myapplication111.data.FeeRecordEntity
import com.example.myapplication111.data.FundDayDetailUi
import com.example.myapplication111.data.FundProjectExportUi
import com.example.myapplication111.data.FundProjectOverviewUi
import com.example.myapplication111.data.FundProjectSummaryUi
import com.example.myapplication111.data.FundRecordEntity
import com.example.myapplication111.data.OutboundRecordEntity
import com.example.myapplication111.data.PaymentRecordEntity
import com.example.myapplication111.data.ProjectSummaryUi
import com.example.myapplication111.data.SecondarySaleRecordEntity
import com.example.myapplication111.data.StorageRecordEntity
import com.example.myapplication111.data.WorkerEntity
import com.example.myapplication111.util.BackupManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class DockNoteViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = DockNoteRepository.getInstance(application)

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _restoreSuccessTrigger = MutableSharedFlow<Boolean>()
    val restoreSuccessTrigger: SharedFlow<Boolean> = _restoreSuccessTrigger.asSharedFlow()

    private val _activePdf = MutableStateFlow<PdfViewerState?>(null)
    val activePdf: StateFlow<PdfViewerState?> = _activePdf.asStateFlow()

    fun restoreBackup(file: File) {
        viewModelScope.launch {
            if (BackupManager.restoreBackup(getApplication(), file)) {
                _restoreSuccessTrigger.emit(true)
            }
        }
    }

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    fun openPdf(uri: Uri, title: String = "PDF查看") {
        _activePdf.value = PdfViewerState(uri = uri, title = title)
    }

    fun closePdf() {
        _activePdf.value = null
    }

    val projectSummaries: StateFlow<List<ProjectSummaryUi>> = repository.observeProjectSummaries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val fundProjectSummaries: StateFlow<List<FundProjectSummaryUi>> = repository.observeFundProjectSummaries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val projectGroupSummaries: StateFlow<List<ProjectGroupSummaryUi>> = repository.observeProjectGroupSummaries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun observeProjectOverview(projectId: Long): Flow<ProjectOverviewUi?> {
        return repository.observeProjectOverview(projectId)
    }

    fun observeFundProjectOverview(projectId: Long): Flow<FundProjectOverviewUi?> {
        return repository.observeFundProjectOverview(projectId)
    }

    fun observeFundProjectExport(projectId: Long): Flow<FundProjectExportUi?> {
        return repository.observeFundProjectExport(projectId)
    }

    fun observeWorkers(projectId: Long): Flow<List<WorkerEntity>> {
        return repository.observeWorkers(projectId)
    }

    fun observeDayDetail(projectId: Long, dateId: Long): Flow<DayDetailUi?> {
        return repository.observeDayDetail(projectId, dateId)
    }

    fun observeFundDayDetail(projectId: Long, dateId: Long): Flow<FundDayDetailUi?> {
        return repository.observeFundDayDetail(projectId, dateId)
    }

    fun createProject(name: String) {
        viewModelScope.launch {
            repository.createProject(name)
            BackupManager.backupDatabase(getApplication())
        }
    }

    fun createFundProject(name: String) {
        viewModelScope.launch {
            repository.createFundProject(name)
            BackupManager.backupDatabase(getApplication())
        }
    }

    fun deleteProject(projectId: Long) {
        viewModelScope.launch {
            repository.deleteProject(projectId)
            BackupManager.backupDatabase(getApplication())
        }
    }

    fun deleteFundProject(projectId: Long) {
        viewModelScope.launch {
            repository.deleteFundProject(projectId)
            BackupManager.backupDatabase(getApplication())
        }
    }

    fun createProjectGroup(name: String, projectIds: List<Long>) {
        viewModelScope.launch {
            repository.createProjectGroup(name, projectIds)
            BackupManager.backupDatabase(getApplication())
        }
    }

    fun updateProjectGroup(groupId: Long, name: String, projectIds: List<Long>) {
        viewModelScope.launch {
            repository.updateProjectGroup(groupId, name, projectIds)
            BackupManager.backupDatabase(getApplication())
        }
    }

    fun deleteProjectGroup(groupId: Long) {
        viewModelScope.launch {
            repository.deleteProjectGroup(groupId)
            BackupManager.backupDatabase(getApplication())
        }
    }

    suspend fun createOrGetDate(projectId: Long, date: String): Long {
        val dateId = repository.createDate(projectId, date)
        BackupManager.backupDatabase(getApplication())
        return dateId
    }

    suspend fun createOrGetFundDate(projectId: Long, date: String): Long {
        val dateId = repository.createFundDate(projectId, date)
        BackupManager.backupDatabase(getApplication())
        return dateId
    }

    fun deleteDate(dateId: Long) {
        viewModelScope.launch {
            repository.deleteDate(dateId)
            BackupManager.backupDatabase(getApplication())
        }
    }

    fun deleteFundDate(dateId: Long) {
        viewModelScope.launch {
            repository.deleteFundDate(dateId)
            BackupManager.backupDatabase(getApplication())
        }
    }

    fun addStorageRecord(
        dateId: Long,
        name: String,
        count: Double,
        weightPerUnit: Double,
        pricePerWeight: Double,
    ) {
        viewModelScope.launch {
            repository.addStorageRecord(dateId, name, count, weightPerUnit, pricePerWeight)
            BackupManager.backupDatabase(getApplication())
        }
    }

    fun updateStorageRecord(
        record: StorageRecordEntity,
        name: String,
        count: Double,
        weightPerUnit: Double,
        pricePerWeight: Double,
    ) {
        viewModelScope.launch {
            repository.updateStorageRecord(record, name, count, weightPerUnit, pricePerWeight)
            BackupManager.backupDatabase(getApplication())
        }
    }

    fun deleteStorageRecord(record: StorageRecordEntity) {
        viewModelScope.launch {
            repository.deleteStorageRecord(record)
            BackupManager.backupDatabase(getApplication())
        }
    }

    fun addSecondarySaleRecord(
        dateId: Long,
        name: String,
        weight: Double,
        unitPrice: Double,
    ) {
        viewModelScope.launch {
            repository.addSecondarySaleRecord(dateId, name, weight, unitPrice)
            BackupManager.backupDatabase(getApplication())
        }
    }

    fun updateSecondarySaleRecord(
        record: SecondarySaleRecordEntity,
        name: String,
        weight: Double,
        unitPrice: Double,
    ) {
        viewModelScope.launch {
            repository.updateSecondarySaleRecord(record, name, weight, unitPrice)
            BackupManager.backupDatabase(getApplication())
        }
    }

    fun deleteSecondarySaleRecord(record: SecondarySaleRecordEntity) {
        viewModelScope.launch {
            repository.deleteSecondarySaleRecord(record)
            BackupManager.backupDatabase(getApplication())
        }
    }

    fun addFeeRecord(dateId: Long, type: String, amount: Double) {
        viewModelScope.launch {
            repository.addFeeRecord(dateId, type, amount)
            BackupManager.backupDatabase(getApplication())
        }
    }

    fun updateFeeRecord(record: FeeRecordEntity, type: String, amount: Double) {
        viewModelScope.launch {
            repository.updateFeeRecord(record, type, amount)
            BackupManager.backupDatabase(getApplication())
        }
    }

    fun deleteFeeRecord(record: FeeRecordEntity) {
        viewModelScope.launch {
            repository.deleteFeeRecord(record)
            BackupManager.backupDatabase(getApplication())
        }
    }

    fun addDayPhoto(dateId: Long, path: String) {
        viewModelScope.launch {
            repository.addDayPhoto(dateId, path)
            BackupManager.backupDatabase(getApplication())
        }
    }

    fun deleteDayPhoto(photo: DayPhotoEntity) {
        viewModelScope.launch {
            runCatching { File(photo.path).delete() }
            repository.deleteDayPhoto(photo)
            BackupManager.backupDatabase(getApplication())
        }
    }

    val uniqueItemNames: StateFlow<List<String>> = repository.observeUniqueItemNames()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    suspend fun getLastPriceForItem(name: String): Double? {
        return repository.getLastPriceForItem(name)
    }

    suspend fun getLastWeightForItem(name: String): Double? {
        return repository.getLastWeightForItem(name)
    }

    fun addOutboundRecord(
        dateId: Long,
        name: String,
        count: Double,
        weightPerUnit: Double,
    ) {
        viewModelScope.launch {
            repository.addOutboundRecord(dateId, name, count, weightPerUnit)
            BackupManager.backupDatabase(getApplication())
        }
    }

    fun updateOutboundRecord(
        record: OutboundRecordEntity,
        name: String,
        count: Double,
        weightPerUnit: Double,
    ) {
        viewModelScope.launch {
            repository.updateOutboundRecord(record, name, count, weightPerUnit)
            BackupManager.backupDatabase(getApplication())
        }
    }

    fun deleteOutboundRecord(record: OutboundRecordEntity) {
        viewModelScope.launch {
            repository.deleteOutboundRecord(record)
            BackupManager.backupDatabase(getApplication())
        }
    }

    fun addPaymentRecord(projectId: Long, amount: Double, date: String, remark: String) {
        viewModelScope.launch {
            repository.addPaymentRecord(projectId, amount, date, remark)
            BackupManager.backupDatabase(getApplication())
        }
    }

    fun updatePaymentRecord(record: PaymentRecordEntity, amount: Double, date: String, remark: String) {
        viewModelScope.launch {
            repository.updatePaymentRecord(record, amount, date, remark)
            BackupManager.backupDatabase(getApplication())
        }
    }

    fun deletePaymentRecord(record: PaymentRecordEntity) {
        viewModelScope.launch {
            repository.deletePaymentRecord(record)
            BackupManager.backupDatabase(getApplication())
        }
    }

    fun addFundRecord(dateId: Long, type: Int, name: String, amount: Double, remark: String) {
        viewModelScope.launch {
            repository.addFundRecord(dateId, type, name, amount, remark)
            BackupManager.backupDatabase(getApplication())
        }
    }

    fun updateFundRecord(record: FundRecordEntity, name: String, amount: Double, remark: String) {
        viewModelScope.launch {
            repository.updateFundRecord(record, name, amount, remark)
            BackupManager.backupDatabase(getApplication())
        }
    }

    fun deleteFundRecord(record: FundRecordEntity) {
        viewModelScope.launch {
            repository.deleteFundRecord(record)
            BackupManager.backupDatabase(getApplication())
        }
    }

    fun observeMonthlyAttendanceSummary(projectId: Long, month: String): Flow<List<AttendanceSummary>> {
        return repository.observeMonthlyAttendanceSummary(projectId, month)
    }

    fun observeMonthlyAttendanceDashboard(projectId: Long, month: String): Flow<List<AttendanceDashboardRow>> {
        return repository.observeMonthlyAttendanceDashboard(projectId, month)
    }

    fun observeProjectAttendanceSummaries(): Flow<List<AttendanceProjectSummary>> {
        return repository.observeProjectAttendanceSummaries()
    }

    fun observeAttendanceMonthBoards(projectId: Long): Flow<List<AttendanceMonthBoard>> {
        return repository.observeAttendanceMonthBoards(projectId)
    }

    fun createWorker(projectId: Long, name: String) {
        viewModelScope.launch {
            repository.createWorker(projectId, name)
            BackupManager.backupDatabase(getApplication())
        }
    }

    fun saveAttendanceRecord(
        projectId: Long,
        workerId: Long,
        date: String,
        salaryMode: Int,
        startTime: String?,
        endTime: String?,
        isPresent: Boolean,
        attendancePortion: Double,
        hourlyRate: Double?,
        dailyRate: Double?,
        overtimeHours: Double,
        overtimeRate: Double,
        cellColor: Int = 0,
    ) {
        viewModelScope.launch {
            repository.saveAttendanceRecord(projectId, workerId, date, salaryMode, startTime, endTime, isPresent, attendancePortion, hourlyRate, dailyRate, overtimeHours, overtimeRate, cellColor)
            BackupManager.backupDatabase(getApplication())
        }
    }

    fun updateAttendanceRecord(
        attendanceId: Long,
        date: String,
        salaryMode: Int,
        startTime: String?,
        endTime: String?,
        isPresent: Boolean,
        attendancePortion: Double,
        hourlyRate: Double?,
        dailyRate: Double?,
        overtimeHours: Double,
        overtimeRate: Double,
        cellColor: Int,
    ) {
        viewModelScope.launch {
            repository.updateAttendanceRecord(attendanceId, date, salaryMode, startTime, endTime, isPresent, attendancePortion, hourlyRate, dailyRate, overtimeHours, overtimeRate, cellColor)
            BackupManager.backupDatabase(getApplication())
        }
    }

    fun deleteAttendanceRecord(record: AttendanceEntity) {
        viewModelScope.launch {
            repository.deleteAttendanceRecord(record)
            BackupManager.backupDatabase(getApplication())
        }
    }

    fun deleteWorker(worker: WorkerEntity) {
        viewModelScope.launch {
            repository.deleteWorker(worker)
            BackupManager.backupDatabase(getApplication())
        }
    }

    fun toggleWorkerVisibility(worker: WorkerEntity, month: String) {
        viewModelScope.launch {
            val hiddenList = worker.hiddenMonths.split(",").filter { it.isNotBlank() }.toMutableSet()
            if (hiddenList.contains(month)) {
                hiddenList.remove(month)
            } else {
                hiddenList.add(month)
            }
            val updatedWorker = worker.copy(hiddenMonths = hiddenList.joinToString(","))
            repository.saveWorker(updatedWorker)
            BackupManager.backupDatabase(getApplication())
        }
    }

    fun debugFillMockData() {
        viewModelScope.launch {
            repository.debugFillMockData()
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                DockNoteViewModel(this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application)
            }
        }
    }
}

data class PdfViewerState(
    val uri: Uri,
    val title: String,
)
