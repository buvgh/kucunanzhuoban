package com.example.myapplication111.util

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.room.Room
import com.example.myapplication111.data.DockNoteDatabase
import com.example.myapplication111.data.FundDateEntity
import com.example.myapplication111.data.FundProjectEntity
import com.example.myapplication111.data.FundRecordEntity
import com.example.myapplication111.data.ProjectEntity
import com.example.myapplication111.data.RecordDateEntity
import com.example.myapplication111.data.PaymentRecordEntity
import com.example.myapplication111.data.StorageRecordEntity
import com.example.myapplication111.data.SecondarySaleRecordEntity
import com.example.myapplication111.data.FeeRecordEntity
import com.example.myapplication111.data.OutboundRecordEntity
import com.example.myapplication111.data.DayPhotoEntity
import com.example.myapplication111.data.WorkerEntity
import com.example.myapplication111.data.AttendanceEntity
import com.example.myapplication111.data.ProjectGroupEntity
import com.example.myapplication111.data.ProjectGroupProjectEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import androidx.room.withTransaction
import kotlinx.coroutines.flow.first

enum class ConflictResolutionMode {
    MERGE,
    RENAME_NEW
}

data class ConflictPrecheckResult(
    val hasConflicts: Boolean,
    val tempDbFile: File
)

object DatabaseMerger {
    private const val TAG = "DatabaseMerger"
    private const val TEMP_DB_NAME = "import_temp.db"

    suspend fun precheckConflicts(context: Context, uri: Uri): ConflictPrecheckResult = withContext(Dispatchers.IO) {
        val tempDbFile = File(context.cacheDir, TEMP_DB_NAME)
        if (tempDbFile.exists()) tempDbFile.delete()

        // 复制 uri 内容到临时文件
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(tempDbFile).use { output ->
                input.copyTo(output)
            }
        } ?: throw IllegalStateException("Cannot open input stream for Uri")

        val tempDb = Room.databaseBuilder(context, DockNoteDatabase::class.java, tempDbFile.absolutePath)
            .fallbackToDestructiveMigration()
            .build()
        val mainDb = DockNoteDatabase.getInstance(context)

        var hasConflicts = false
        try {
            val localProjects = mainDb.projectDao().observeAll().first().map { it.name }.toSet()
            val tempProjects = tempDb.projectDao().observeAll().first().map { it.name }
            if (tempProjects.any { it in localProjects }) {
                hasConflicts = true
            }

            if (!hasConflicts) {
                val localFundProjects = mainDb.fundProjectDao().observeAll().first().map { it.name }.toSet()
                val tempFundProjects = tempDb.fundProjectDao().observeAll().first().map { it.name }
                if (tempFundProjects.any { it in localFundProjects }) {
                    hasConflicts = true
                }
            }
        } finally {
            tempDb.close()
        }

        ConflictPrecheckResult(hasConflicts, tempDbFile)
    }

    suspend fun mergeDatabase(context: Context, tempDbFile: File, resolutionMode: ConflictResolutionMode): Boolean = withContext(Dispatchers.IO) {
        val tempDb = Room.databaseBuilder(context, DockNoteDatabase::class.java, tempDbFile.absolutePath)
            .fallbackToDestructiveMigration()
            .build()
        val mainDb = DockNoteDatabase.getInstance(context)

        try {
            mainDb.withTransaction {
                // 1. Merge Fund Projects
                val fundProjectMap = mutableMapOf<Long, Long>()
                val localFundProjects = mainDb.fundProjectDao().observeAll().first().associateBy { it.name }
                val tempFundProjects = tempDb.fundProjectDao().observeAll().first()

                for (tempFp in tempFundProjects) {
                    val existing = localFundProjects[tempFp.name]
                    if (existing != null) {
                        if (resolutionMode == ConflictResolutionMode.MERGE) {
                            fundProjectMap[tempFp.id] = existing.id
                        } else {
                            val newName = generateUniqueName(tempFp.name, localFundProjects.keys)
                            val newId = mainDb.fundProjectDao().insert(tempFp.copy(id = 0, name = newName))
                            fundProjectMap[tempFp.id] = newId
                        }
                    } else {
                        val newId = mainDb.fundProjectDao().insert(tempFp.copy(id = 0))
                        fundProjectMap[tempFp.id] = newId
                    }
                }

                // Merge Fund Dates
                val fundDateMap = mutableMapOf<Long, Long>()
                val tempFundDates = tempDb.fundDateDao().observeAll().first()
                for (tempDate in tempFundDates) {
                    val newFpId = fundProjectMap[tempDate.projectId] ?: continue
                    val existing = mainDb.fundDateDao().findByProjectAndDate(newFpId, tempDate.date)
                    if (existing != null) {
                        fundDateMap[tempDate.id] = existing.id
                    } else {
                        val newId = mainDb.fundDateDao().insert(tempDate.copy(id = 0, projectId = newFpId))
                        fundDateMap[tempDate.id] = newId
                    }
                }

                // Merge Fund Records
                tempDb.query("SELECT * FROM fund_records", null).use { cursor ->
                    while (cursor.moveToNext()) {
                        val oldId = cursor.getLong(cursor.getColumnIndexOrThrow("id"))
                        val oldDateId = cursor.getLong(cursor.getColumnIndexOrThrow("dateId"))
                        val type = cursor.getInt(cursor.getColumnIndexOrThrow("type"))
                        val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                        val amount = cursor.getDouble(cursor.getColumnIndexOrThrow("amount"))
                        val remark = cursor.getString(cursor.getColumnIndexOrThrow("remark"))
                        val createTime = cursor.getLong(cursor.getColumnIndexOrThrow("createTime"))

                        val newDateId = fundDateMap[oldDateId] ?: continue
                        mainDb.fundRecordDao().insert(FundRecordEntity(0, newDateId, type, name, amount, remark, createTime))
                    }
                }

                // 2. Merge Projects
                val projectMap = mutableMapOf<Long, Long>()
                val localProjects = mainDb.projectDao().observeAll().first().associateBy { it.name }
                val tempProjects = tempDb.projectDao().observeAll().first()
                
                // Track names to ensure uniqueness during loop
                val currentProjectNames = localProjects.keys.toMutableSet()

                for (tempP in tempProjects) {
                    val existing = localProjects[tempP.name]
                    if (existing != null) {
                        if (resolutionMode == ConflictResolutionMode.MERGE) {
                            projectMap[tempP.id] = existing.id
                        } else {
                            val newName = generateUniqueName(tempP.name, currentProjectNames)
                            currentProjectNames.add(newName)
                            val newId = mainDb.projectDao().insert(tempP.copy(id = 0, name = newName))
                            projectMap[tempP.id] = newId
                        }
                    } else {
                        val newId = mainDb.projectDao().insert(tempP.copy(id = 0))
                        projectMap[tempP.id] = newId
                        currentProjectNames.add(tempP.name)
                    }
                }

                // 3. Merge Project Groups
                val groupMap = mutableMapOf<Long, Long>()
                val localGroups = mainDb.projectGroupDao().observeGroups().first().associateBy { it.name }
                val tempGroups = tempDb.projectGroupDao().observeGroups().first()

                for (tempG in tempGroups) {
                    val existing = localGroups[tempG.name]
                    if (existing != null) {
                        groupMap[tempG.id] = existing.id
                    } else {
                        val newId = mainDb.projectGroupDao().insertGroup(tempG.copy(id = 0))
                        groupMap[tempG.id] = newId
                    }
                }

                val tempGroupProjects = tempDb.projectGroupDao().observeMembers().first()
                val currentGroupProjects = mainDb.projectGroupDao().observeMembers().first().map { Pair(it.groupId, it.projectId) }.toSet()
                val newGroupProjects = mutableListOf<ProjectGroupProjectEntity>()
                for (tgp in tempGroupProjects) {
                    val newGroupId = groupMap[tgp.groupId] ?: continue
                    val newProjectId = projectMap[tgp.projectId] ?: continue
                    if (Pair(newGroupId, newProjectId) !in currentGroupProjects) {
                        newGroupProjects.add(ProjectGroupProjectEntity(newGroupId, newProjectId))
                    }
                }
                if (newGroupProjects.isNotEmpty()) {
                    mainDb.projectGroupDao().insertMembers(newGroupProjects)
                }

                // 4. Merge Payment Records
                tempDb.query("SELECT * FROM payment_records", null).use { cursor ->
                    while (cursor.moveToNext()) {
                        val oldProjectId = cursor.getLong(cursor.getColumnIndexOrThrow("projectId"))
                        val amount = cursor.getDouble(cursor.getColumnIndexOrThrow("amount"))
                        val date = cursor.getString(cursor.getColumnIndexOrThrow("date"))
                        val remark = cursor.getString(cursor.getColumnIndexOrThrow("remark"))

                        val newProjectId = projectMap[oldProjectId] ?: continue
                        mainDb.paymentRecordDao().insert(PaymentRecordEntity(0, newProjectId, amount, date, remark))
                    }
                }

                // 5. Merge Workers
                val workerMap = mutableMapOf<Long, Long>()
                // Instead of observeAll, we query workers from db
                tempDb.query("SELECT * FROM workers", null).use { cursor ->
                    while (cursor.moveToNext()) {
                        val oldId = cursor.getLong(cursor.getColumnIndexOrThrow("id"))
                        val oldProjectId = cursor.getLong(cursor.getColumnIndexOrThrow("projectId"))
                        val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                        val salaryMode = cursor.getInt(cursor.getColumnIndexOrThrow("salaryMode"))
                        val hourlyRate = cursor.getDouble(cursor.getColumnIndexOrThrow("hourlyRate"))
                        val dailyRate = cursor.getDouble(cursor.getColumnIndexOrThrow("dailyRate"))
                        val hiddenMonths = cursor.getString(cursor.getColumnIndexOrThrow("hiddenMonths"))

                        val newProjectId = projectMap[oldProjectId] ?: continue
                        // Check if this worker exists in this project
                        val existingWorker = mainDb.workerDao().observeForProject(newProjectId).first().find { it.name == name }
                        if (existingWorker != null) {
                            workerMap[oldId] = existingWorker.id
                            // Optionally update worker info? Let's just keep existing.
                        } else {
                            val newId = mainDb.workerDao().insert(WorkerEntity(0, newProjectId, name, salaryMode, hourlyRate, dailyRate, hiddenMonths))
                            workerMap[oldId] = newId
                        }
                    }
                }

                // 6. Merge Attendance Records
                val tempAttendances = tempDb.attendanceDao().observeAll().first()
                val currentAttendances = mainDb.attendanceDao().observeAll().first()
                    .map { Triple(it.projectId, it.workerId, it.date) }.toSet()

                for (tempAtt in tempAttendances) {
                    val newProjectId = projectMap[tempAtt.projectId] ?: continue
                    val newWorkerId = workerMap[tempAtt.workerId] ?: continue
                    
                    if (Triple(newProjectId, newWorkerId, tempAtt.date) !in currentAttendances) {
                        mainDb.attendanceDao().insert(tempAtt.copy(id = 0, projectId = newProjectId, workerId = newWorkerId))
                    }
                }

                // 7. Merge Record Dates
                val dateMap = mutableMapOf<Long, Long>()
                val tempDates = tempDb.recordDateDao().observeAll().first()
                for (tempDate in tempDates) {
                    val newProjectId = projectMap[tempDate.projectId] ?: continue
                    val existing = mainDb.recordDateDao().findByProjectAndDate(newProjectId, tempDate.date)
                    if (existing != null) {
                        dateMap[tempDate.id] = existing.id
                    } else {
                        val newId = mainDb.recordDateDao().insert(tempDate.copy(id = 0, projectId = newProjectId))
                        dateMap[tempDate.id] = newId
                    }
                }

                // 8. Merge Storage Records
                val tempStorage = tempDb.storageRecordDao().observeAll().first()
                for (temp in tempStorage) {
                    val newDateId = dateMap[temp.dateId] ?: continue
                    mainDb.storageRecordDao().insert(temp.copy(id = 0, dateId = newDateId))
                }

                // 9. Merge Secondary Sale Records
                val tempSecondary = tempDb.secondarySaleRecordDao().observeAll().first()
                for (temp in tempSecondary) {
                    val newDateId = dateMap[temp.dateId] ?: continue
                    mainDb.secondarySaleRecordDao().insert(temp.copy(id = 0, dateId = newDateId))
                }

                // 10. Merge Fee Records
                val tempFees = tempDb.feeRecordDao().observeAll().first()
                for (temp in tempFees) {
                    val newDateId = dateMap[temp.dateId] ?: continue
                    mainDb.feeRecordDao().insert(temp.copy(id = 0, dateId = newDateId))
                }

                // 11. Merge Outbound Records
                val tempOutbounds = tempDb.outboundRecordDao().observeAll().first()
                for (temp in tempOutbounds) {
                    val newDateId = dateMap[temp.dateId] ?: continue
                    mainDb.outboundRecordDao().insert(temp.copy(id = 0, dateId = newDateId))
                }

                // 12. Merge Day Photos
                tempDb.query("SELECT * FROM day_photos", null).use { cursor ->
                    while (cursor.moveToNext()) {
                        val oldDateId = cursor.getLong(cursor.getColumnIndexOrThrow("dateId"))
                        val path = cursor.getString(cursor.getColumnIndexOrThrow("path"))
                        val createTime = cursor.getLong(cursor.getColumnIndexOrThrow("createTime"))

                        val newDateId = dateMap[oldDateId] ?: continue
                        mainDb.dayPhotoDao().insert(DayPhotoEntity(0, newDateId, path, createTime))
                    }
                }
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error merging database", e)
            false
        } finally {
            tempDb.close()
            tempDbFile.delete()
            File(tempDbFile.absolutePath + "-wal").delete()
            File(tempDbFile.absolutePath + "-shm").delete()
        }
    }

    private fun generateUniqueName(baseName: String, existingNames: Set<String>): String {
        var newName = "$baseName(导入)"
        var counter = 1
        while (newName in existingNames) {
            newName = "$baseName(导入 $counter)"
            counter++
        }
        return newName
    }
}
