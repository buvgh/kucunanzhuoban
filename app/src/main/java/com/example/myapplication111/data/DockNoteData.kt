package com.example.myapplication111.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createTime: Long,
)

@Entity(
    tableName = "record_dates",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("projectId"), Index(value = ["projectId", "date"], unique = true)],
)
data class RecordDateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val date: String,
)

@Entity(
    tableName = "storage_records",
    foreignKeys = [
        ForeignKey(
            entity = RecordDateEntity::class,
            parentColumns = ["id"],
            childColumns = ["dateId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("dateId")],
)
data class StorageRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateId: Long,
    val name: String,
    val count: Double,
    val weightPerUnit: Double,
    val pricePerWeight: Double,
    val totalWeight: Double,
    val totalPrice: Double,
)

@Entity(
    tableName = "fee_records",
    foreignKeys = [
        ForeignKey(
            entity = RecordDateEntity::class,
            parentColumns = ["id"],
            childColumns = ["dateId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("dateId")],
)
data class FeeRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateId: Long,
    val type: String,
    val amount: Double,
)

@Entity(
    tableName = "day_photos",
    foreignKeys = [
        ForeignKey(
            entity = RecordDateEntity::class,
            parentColumns = ["id"],
            childColumns = ["dateId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("dateId")],
)
data class DayPhotoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateId: Long,
    val path: String,
    val createTime: Long,
)

object FeeTypes {
    const val LABOR = "人工费"
    const val AGENCY = "代办费"
    const val LOADING = "装车费"
    const val CUSTOM = "自定义"

    val all = listOf(LABOR, AGENCY, LOADING, CUSTOM)
}

data class ItemSummary(
    val name: String,
    val totalCount: Double,
    val totalWeight: Double,
    val totalAmount: Double,
)

data class Totals(
    val totalCount: Double = 0.0,
    val totalWeight: Double = 0.0,
    val totalStorageAmount: Double = 0.0,
    val laborFee: Double = 0.0,
    val agencyFee: Double = 0.0,
    val loadingFee: Double = 0.0,
    val otherFee: Double = 0.0,
    val totalFee: Double = 0.0,
    val itemSummaries: List<ItemSummary> = emptyList(),
)

data class ProjectSummaryUi(
    val id: Long,
    val name: String,
    val createTime: Long,
    val totals: Totals,
)

data class DateSummaryUi(
    val id: Long,
    val date: String,
    val totals: Totals,
)

data class ProjectOverviewUi(
    val id: Long,
    val name: String,
    val totals: Totals,
    val dates: List<DateSummaryUi>,
)

data class DayDetailUi(
    val projectId: Long,
    val projectName: String,
    val dateId: Long,
    val date: String,
    val totals: Totals,
    val photos: List<DayPhotoEntity>,
    val storageRecords: List<StorageRecordEntity>,
    val feeRecords: List<FeeRecordEntity>,
)

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY createTime DESC")
    fun observeAll(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :projectId LIMIT 1")
    fun observeById(projectId: Long): Flow<ProjectEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(project: ProjectEntity): Long

    @Query("DELETE FROM projects WHERE id = :projectId")
    suspend fun deleteById(projectId: Long)
}

@Dao
interface RecordDateDao {
    @Query("SELECT * FROM record_dates ORDER BY date DESC, id DESC")
    fun observeAll(): Flow<List<RecordDateEntity>>

    @Query("SELECT * FROM record_dates WHERE projectId = :projectId ORDER BY date DESC, id DESC")
    fun observeForProject(projectId: Long): Flow<List<RecordDateEntity>>

    @Query("SELECT * FROM record_dates WHERE id = :dateId LIMIT 1")
    fun observeById(dateId: Long): Flow<RecordDateEntity?>

    @Query("SELECT * FROM record_dates WHERE projectId = :projectId AND date = :date LIMIT 1")
    suspend fun findByProjectAndDate(projectId: Long, date: String): RecordDateEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(dateEntity: RecordDateEntity): Long

    @Query("DELETE FROM record_dates WHERE id = :dateId")
    suspend fun deleteById(dateId: Long)

    @Delete
    suspend fun delete(dateEntity: RecordDateEntity)
}

@Dao
interface StorageRecordDao {
    @Query("SELECT * FROM storage_records ORDER BY id DESC")
    fun observeAll(): Flow<List<StorageRecordEntity>>

    @Query("SELECT * FROM storage_records WHERE dateId = :dateId ORDER BY id DESC")
    fun observeForDate(dateId: Long): Flow<List<StorageRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: StorageRecordEntity): Long

    @Update
    suspend fun update(record: StorageRecordEntity)

    @Delete
    suspend fun delete(record: StorageRecordEntity)

    @Query("SELECT DISTINCT name FROM storage_records ORDER BY id DESC")
    fun observeUniqueItemNames(): Flow<List<String>>

    @Query("SELECT pricePerWeight FROM storage_records WHERE name = :name ORDER BY id DESC LIMIT 1")
    suspend fun getLastPriceForItem(name: String): Double?

    @Query("SELECT weightPerUnit FROM storage_records WHERE name = :name ORDER BY id DESC LIMIT 1")
    suspend fun getLastWeightForItem(name: String): Double?
}

@Dao
interface FeeRecordDao {
    @Query("SELECT * FROM fee_records ORDER BY id DESC")
    fun observeAll(): Flow<List<FeeRecordEntity>>

    @Query("SELECT * FROM fee_records WHERE dateId = :dateId ORDER BY id DESC")
    fun observeForDate(dateId: Long): Flow<List<FeeRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: FeeRecordEntity): Long

    @Update
    suspend fun update(record: FeeRecordEntity)

    @Delete
    suspend fun delete(record: FeeRecordEntity)
}

@Dao
interface DayPhotoDao {
    @Query("SELECT * FROM day_photos WHERE dateId = :dateId ORDER BY createTime DESC, id DESC")
    fun observeForDate(dateId: Long): Flow<List<DayPhotoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(photo: DayPhotoEntity): Long

    @Delete
    suspend fun delete(photo: DayPhotoEntity)
}

@Database(
    entities = [
        ProjectEntity::class,
        RecordDateEntity::class,
        StorageRecordEntity::class,
        FeeRecordEntity::class,
        DayPhotoEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
abstract class DockNoteDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun recordDateDao(): RecordDateDao
    abstract fun storageRecordDao(): StorageRecordDao
    abstract fun feeRecordDao(): FeeRecordDao
    abstract fun dayPhotoDao(): DayPhotoDao

    companion object {
        @Volatile
        private var instance: DockNoteDatabase? = null
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS day_photos (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        dateId INTEGER NOT NULL,
                        path TEXT NOT NULL,
                        createTime INTEGER NOT NULL,
                        FOREIGN KEY(dateId) REFERENCES record_dates(id) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent(),
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_day_photos_dateId ON day_photos(dateId)")
            }
        }

        fun getInstance(context: Context): DockNoteDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    DockNoteDatabase::class.java,
                    "dock_note.db",
                ).addMigrations(MIGRATION_1_2).build().also { instance = it }
            }
        }
    }
}

class DockNoteRepository private constructor(
    private val projectDao: ProjectDao,
    private val dateDao: RecordDateDao,
    private val storageDao: StorageRecordDao,
    private val feeDao: FeeRecordDao,
    private val dayPhotoDao: DayPhotoDao,
) {
    fun observeProjectSummaries(): Flow<List<ProjectSummaryUi>> {
        return combine(
            projectDao.observeAll(),
            dateDao.observeAll(),
            storageDao.observeAll(),
            feeDao.observeAll(),
        ) { projects, dates, storageRecords, feeRecords ->
            projects.map { project ->
                val projectDateIds = dates
                    .filter { it.projectId == project.id }
                    .map { it.id }
                    .toSet()
                ProjectSummaryUi(
                    id = project.id,
                    name = project.name,
                    createTime = project.createTime,
                    totals = buildTotals(
                        storageRecords.filter { it.dateId in projectDateIds },
                        feeRecords.filter { it.dateId in projectDateIds },
                    ),
                )
            }
        }
    }

    fun observeProjectOverview(projectId: Long): Flow<ProjectOverviewUi?> {
        return combine(
            projectDao.observeById(projectId),
            dateDao.observeForProject(projectId),
            storageDao.observeAll(),
            feeDao.observeAll(),
        ) { project, dates, storageRecords, feeRecords ->
            project?.let {
                val dateSummaries = dates.map { date ->
                    val dayStorageRecords = storageRecords.filter { it.dateId == date.id }
                    val dayFeeRecords = feeRecords.filter { it.dateId == date.id }
                    DateSummaryUi(
                        id = date.id,
                        date = date.date,
                        totals = buildTotals(dayStorageRecords, dayFeeRecords),
                    )
                }
                val projectDateIds = dates.map { it.id }.toSet()
                ProjectOverviewUi(
                    id = it.id,
                    name = it.name,
                    totals = buildTotals(
                        storageRecords.filter { storage -> storage.dateId in projectDateIds },
                        feeRecords.filter { fee -> fee.dateId in projectDateIds },
                    ),
                    dates = dateSummaries,
                )
            }
        }
    }

    fun observeDayDetail(projectId: Long, dateId: Long): Flow<DayDetailUi?> {
        return combine(
            projectDao.observeById(projectId),
            dateDao.observeById(dateId),
            dayPhotoDao.observeForDate(dateId),
            storageDao.observeForDate(dateId),
            feeDao.observeForDate(dateId),
        ) { project, date, photos, storageRecords, feeRecords ->
            if (project == null || date == null || date.projectId != projectId) {
                null
            } else {
                DayDetailUi(
                    projectId = project.id,
                    projectName = project.name,
                    dateId = date.id,
                    date = date.date,
                    totals = buildTotals(storageRecords, feeRecords),
                    photos = photos,
                    storageRecords = storageRecords,
                    feeRecords = feeRecords,
                )
            }
        }
    }

    suspend fun createProject(name: String) {
        val normalizedName = name.trim()
        if (normalizedName.isBlank()) return
        projectDao.insert(
            ProjectEntity(
                name = normalizedName,
                createTime = System.currentTimeMillis(),
            ),
        )
    }

    suspend fun deleteProject(projectId: Long) {
        projectDao.deleteById(projectId)
    }

    suspend fun createDate(projectId: Long, date: String): Long {
        return ensureDate(projectId, date.trim())
    }

    suspend fun deleteDate(dateId: Long) {
        dateDao.deleteById(dateId)
    }

    suspend fun addStorageRecord(
        dateId: Long,
        name: String,
        count: Double,
        weightPerUnit: Double,
        pricePerWeight: Double,
    ) {
        storageDao.insert(
            StorageRecordEntity(
                dateId = dateId,
                name = name.trim(),
                count = count,
                weightPerUnit = weightPerUnit,
                pricePerWeight = pricePerWeight,
                totalWeight = count * weightPerUnit,
                totalPrice = count * weightPerUnit * pricePerWeight,
            ),
        )
    }

    suspend fun updateStorageRecord(
        record: StorageRecordEntity,
        name: String,
        count: Double,
        weightPerUnit: Double,
        pricePerWeight: Double,
    ) {
        storageDao.update(
            record.copy(
                name = name.trim(),
                count = count,
                weightPerUnit = weightPerUnit,
                pricePerWeight = pricePerWeight,
                totalWeight = count * weightPerUnit,
                totalPrice = count * weightPerUnit * pricePerWeight,
            ),
        )
    }

    suspend fun deleteStorageRecord(record: StorageRecordEntity) {
        storageDao.delete(record)
    }

    suspend fun addFeeRecord(dateId: Long, type: String, amount: Double) {
        feeDao.insert(
            FeeRecordEntity(
                dateId = dateId,
                type = normalizeFeeType(type),
                amount = amount,
            ),
        )
    }

    suspend fun updateFeeRecord(record: FeeRecordEntity, type: String, amount: Double) {
        feeDao.update(record.copy(type = normalizeFeeType(type), amount = amount))
    }

    suspend fun deleteFeeRecord(record: FeeRecordEntity) {
        feeDao.delete(record)
    }

    suspend fun addDayPhoto(dateId: Long, path: String) {
        val normalized = path.trim()
        if (normalized.isBlank()) return
        dayPhotoDao.insert(
            DayPhotoEntity(
                dateId = dateId,
                path = normalized,
                createTime = System.currentTimeMillis(),
            ),
        )
    }

    suspend fun deleteDayPhoto(photo: DayPhotoEntity) {
        dayPhotoDao.delete(photo)
    }

    fun observeUniqueItemNames(): Flow<List<String>> = storageDao.observeUniqueItemNames()

    suspend fun getLastPriceForItem(name: String): Double? = storageDao.getLastPriceForItem(name)

    suspend fun getLastWeightForItem(name: String): Double? = storageDao.getLastWeightForItem(name)

    private suspend fun ensureDate(projectId: Long, date: String): Long {
        require(date.isNotBlank())
        val existing = dateDao.findByProjectAndDate(projectId, date)
        if (existing != null) return existing.id
        return dateDao.insert(RecordDateEntity(projectId = projectId, date = date))
    }

    private fun normalizeFeeType(type: String): String {
        return type.ifBlank { FeeTypes.LABOR }
    }

    private fun buildTotals(
        storageRecords: List<StorageRecordEntity>,
        feeRecords: List<FeeRecordEntity>,
    ): Totals {
        val laborFee = feeRecords.filter { it.type == FeeTypes.LABOR }.sumOf { it.amount }
        val agencyFee = feeRecords.filter { it.type == FeeTypes.AGENCY }.sumOf { it.amount }
        val loadingFee = feeRecords.filter { it.type == FeeTypes.LOADING }.sumOf { it.amount }
        val otherFee = feeRecords
            .filter { it.type !in listOf(FeeTypes.LABOR, FeeTypes.AGENCY, FeeTypes.LOADING) }
            .sumOf { it.amount }

        val itemSummaries = storageRecords.groupBy { it.name }.map { (name, records) ->
            ItemSummary(
                name = name,
                totalCount = records.sumOf { it.count },
                totalWeight = records.sumOf { it.totalWeight },
                totalAmount = records.sumOf { it.totalPrice }
            )
        }.sortedByDescending { it.totalAmount }

        return Totals(
            totalCount = storageRecords.sumOf { it.count },
            totalWeight = storageRecords.sumOf { it.totalWeight },
            totalStorageAmount = storageRecords.sumOf { it.totalPrice },
            laborFee = laborFee,
            agencyFee = agencyFee,
            loadingFee = loadingFee,
            otherFee = otherFee,
            totalFee = laborFee + agencyFee + loadingFee + otherFee,
            itemSummaries = itemSummaries,
        )
    }

    companion object {
        @Volatile
        private var instance: DockNoteRepository? = null

        fun getInstance(context: Context): DockNoteRepository {
            return instance ?: synchronized(this) {
                val db = DockNoteDatabase.getInstance(context)
                instance ?: DockNoteRepository(
                    projectDao = db.projectDao(),
                    dateDao = db.recordDateDao(),
                    storageDao = db.storageRecordDao(),
                    feeDao = db.feeRecordDao(),
                    dayPhotoDao = db.dayPhotoDao(),
                ).also { instance = it }
            }
        }
    }
}
