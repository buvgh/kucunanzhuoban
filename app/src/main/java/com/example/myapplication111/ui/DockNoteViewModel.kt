package com.example.myapplication111.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.viewModelScope
import com.example.myapplication111.data.DockNoteRepository
import com.example.myapplication111.data.DayDetailUi
import com.example.myapplication111.data.DayPhotoEntity
import com.example.myapplication111.data.ProjectOverviewUi
import com.example.myapplication111.data.FeeRecordEntity
import com.example.myapplication111.data.ProjectSummaryUi
import com.example.myapplication111.data.StorageRecordEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class DockNoteViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = DockNoteRepository.getInstance(application)

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    val projectSummaries: StateFlow<List<ProjectSummaryUi>> = repository.observeProjectSummaries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun observeProjectOverview(projectId: Long): Flow<ProjectOverviewUi?> {
        return repository.observeProjectOverview(projectId)
    }

    fun observeDayDetail(projectId: Long, dateId: Long): Flow<DayDetailUi?> {
        return repository.observeDayDetail(projectId, dateId)
    }

    fun createProject(name: String) {
        viewModelScope.launch {
            repository.createProject(name)
        }
    }

    fun deleteProject(projectId: Long) {
        viewModelScope.launch {
            repository.deleteProject(projectId)
        }
    }

    suspend fun createOrGetDate(projectId: Long, date: String): Long {
        return repository.createDate(projectId, date)
    }

    fun deleteDate(dateId: Long) {
        viewModelScope.launch {
            repository.deleteDate(dateId)
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
        }
    }

    fun deleteStorageRecord(record: StorageRecordEntity) {
        viewModelScope.launch {
            repository.deleteStorageRecord(record)
        }
    }

    fun addFeeRecord(dateId: Long, type: String, amount: Double) {
        viewModelScope.launch {
            repository.addFeeRecord(dateId, type, amount)
        }
    }

    fun updateFeeRecord(record: FeeRecordEntity, type: String, amount: Double) {
        viewModelScope.launch {
            repository.updateFeeRecord(record, type, amount)
        }
    }

    fun deleteFeeRecord(record: FeeRecordEntity) {
        viewModelScope.launch {
            repository.deleteFeeRecord(record)
        }
    }

    fun addDayPhoto(dateId: Long, path: String) {
        viewModelScope.launch {
            repository.addDayPhoto(dateId, path)
        }
    }

    fun deleteDayPhoto(photo: DayPhotoEntity) {
        viewModelScope.launch {
            runCatching { File(photo.path).delete() }
            repository.deleteDayPhoto(photo)
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

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                DockNoteViewModel(this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application)
            }
        }
    }
}
