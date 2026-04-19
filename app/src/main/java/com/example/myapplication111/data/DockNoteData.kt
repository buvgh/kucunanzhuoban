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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

@Entity(
    tableName = "payment_records",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("projectId")],
)
data class PaymentRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val amount: Double,
    val date: String,
    val remark: String = "",
)

@Dao
interface PaymentRecordDao {
    @Query("SELECT * FROM payment_records WHERE projectId = :projectId ORDER BY date DESC, id DESC")
    fun observeForProject(projectId: Long): Flow<List<PaymentRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: PaymentRecordEntity): Long

    @Update
    suspend fun update(record: PaymentRecordEntity)

    @Delete
    suspend fun delete(record: PaymentRecordEntity)
}

@Dao
interface OutboundRecordDao {
    @Query("SELECT * FROM outbound_records ORDER BY id DESC")
    fun observeAll(): Flow<List<OutboundRecordEntity>>

    @Query("SELECT * FROM outbound_records WHERE dateId = :dateId ORDER BY id DESC")
    fun observeForDate(dateId: Long): Flow<List<OutboundRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: OutboundRecordEntity): Long

    @Update
    suspend fun update(record: OutboundRecordEntity)

    @Delete
    suspend fun delete(record: OutboundRecordEntity)
}

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

data class FeeSummary(
    val type: String,
    val totalAmount: Double,
)

data class Totals(
    val totalCount: Double = 0.0,
    val totalWeight: Double = 0.0,
    val totalStorageAmount: Double = 0.0,
    val totalOutboundCount: Double = 0.0,
    val totalOutboundWeight: Double = 0.0,
    val netTotalCount: Double = 0.0,
    val netTotalWeight: Double = 0.0,
    val laborFee: Double = 0.0,
    val agencyFee: Double = 0.0,
    val loadingFee: Double = 0.0,
    val otherFee: Double = 0.0,
    val totalFee: Double = 0.0,
    val totalPaid: Double = 0.0,
    val totalDebt: Double = 0.0,
    val itemSummaries: List<ItemSummary> = emptyList(),
    val feeSummaries: List<FeeSummary> = emptyList(),
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
    val paymentRecords: List<PaymentRecordEntity> = emptyList(),
)

data class DayDetailUi(
    val projectId: Long,
    val projectName: String,
    val dateId: Long,
    val date: String,
    val totals: Totals,
    val projectTotals: Totals, // Add project-level totals for debt info
    val photos: List<DayPhotoEntity>,
    val storageRecords: List<StorageRecordEntity>,
    val outboundRecords: List<OutboundRecordEntity>,
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
        OutboundRecordEntity::class,
        PaymentRecordEntity::class,
    ],
    version = 4, // Increment database version
    exportSchema = false,
)
abstract class DockNoteDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun recordDateDao(): RecordDateDao
    abstract fun storageRecordDao(): StorageRecordDao
    abstract fun feeRecordDao(): FeeRecordDao
    abstract fun dayPhotoDao(): DayPhotoDao
    abstract fun outboundRecordDao(): OutboundRecordDao
    abstract fun paymentRecordDao(): PaymentRecordDao

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

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS outbound_records (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        dateId INTEGER NOT NULL,
                        name TEXT NOT NULL,
                        count REAL NOT NULL,
                        weightPerUnit REAL NOT NULL,
                        timestamp INTEGER NOT NULL,
                        FOREIGN KEY(dateId) REFERENCES record_dates(id) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent(),
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_outbound_records_dateId ON outbound_records(dateId)")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS payment_records (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        projectId INTEGER NOT NULL,
                        amount REAL NOT NULL,
                        date TEXT NOT NULL,
                        remark TEXT NOT NULL DEFAULT '',
                        FOREIGN KEY(projectId) REFERENCES projects(id) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent(),
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_payment_records_projectId ON payment_records(projectId)")
            }
        }

        fun getInstance(context: Context): DockNoteDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    DockNoteDatabase::class.java,
                    "dock_note.db",
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4).build().also { instance = it }
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
    private val outboundDao: OutboundRecordDao,
    private val paymentDao: PaymentRecordDao,
) {
    fun observeProjectSummaries(): Flow<List<ProjectSummaryUi>> {
        return combine(
            projectDao.observeAll(),
            dateDao.observeAll(),
            storageDao.observeAll(),
            feeDao.observeAll(),
            outboundDao.observeAll(),
            QueryAllPaymentsFlow(),
        ) { flows: Array<Any> ->
            val projects = flows[0] as List<ProjectEntity>
            val dates = flows[1] as List<RecordDateEntity>
            val storageRecords = flows[2] as List<StorageRecordEntity>
            val feeRecords = flows[3] as List<FeeRecordEntity>
            val outboundRecords = flows[4] as List<OutboundRecordEntity>
            val paymentRecords = flows[5] as List<PaymentRecordEntity>

            val datesByProject = dates.groupBy { it.projectId }
            val storageByDate = storageRecords.groupBy { it.dateId }
            val feeByDate = feeRecords.groupBy { it.dateId }
            val outboundByDate = outboundRecords.groupBy { it.dateId }
            val paymentsByProject = paymentRecords.groupBy { it.projectId }

            projects.map { project ->
                val projectDates = datesByProject[project.id] ?: emptyList()
                val dateIds = projectDates.map { it.id }
                
                val projectStorage = dateIds.flatMap { storageByDate[it] ?: emptyList() }
                val projectFees = dateIds.flatMap { feeByDate[it] ?: emptyList() }
                val projectOutbound = dateIds.flatMap { outboundByDate[it] ?: emptyList() }
                val projectPayments = paymentsByProject[project.id] ?: emptyList()

                ProjectSummaryUi(
                    id = project.id,
                    name = project.name,
                    createTime = project.createTime,
                    totals = buildTotals(projectStorage, projectOutbound, projectFees, projectPayments),
                )
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun QueryAllPaymentsFlow(): Flow<List<PaymentRecordEntity>> {
        // Simple way to observe all payments across all projects for summary
        return projectDao.observeAll().flatMapLatest { projects ->
            if (projects.isEmpty()) flowOf(emptyList())
            else {
                combine(projects.map { paymentDao.observeForProject(it.id) }) { arrays ->
                    arrays.flatMap { it }
                }
            }
        }
    }

    fun observeProjectOverview(projectId: Long): Flow<ProjectOverviewUi?> {
        return combine(
            projectDao.observeById(projectId),
            dateDao.observeForProject(projectId),
            storageDao.observeAll(),
            feeDao.observeAll(),
            outboundDao.observeAll(),
            paymentDao.observeForProject(projectId),
        ) { flows: Array<Any?> ->
            val project = flows[0] as ProjectEntity?
            val dates = flows[1] as List<RecordDateEntity>
            val storageRecords = flows[2] as List<StorageRecordEntity>
            val feeRecords = flows[3] as List<FeeRecordEntity>
            val outboundRecords = flows[4] as List<OutboundRecordEntity>
            val payments = flows[5] as List<PaymentRecordEntity>

            project?.let {
                val storageByDate = storageRecords.groupBy { it.dateId }
                val feeByDate = feeRecords.groupBy { it.dateId }
                val outboundByDate = outboundRecords.groupBy { it.dateId }

                val dateSummaries = dates.map { date ->
                    val dayStorageRecords = storageByDate[date.id] ?: emptyList()
                    val dayFeeRecords = feeByDate[date.id] ?: emptyList()
                    val dayOutboundRecords = outboundByDate[date.id] ?: emptyList()
                    DateSummaryUi(
                        id = date.id,
                        date = date.date,
                        totals = buildTotals(dayStorageRecords, dayOutboundRecords, dayFeeRecords),
                    )
                }

                val dateIds = dates.map { it.id }
                val projectStorage = dateIds.flatMap { storageByDate[it] ?: emptyList() }
                val projectFees = dateIds.flatMap { feeByDate[it] ?: emptyList() }
                val projectOutbound = dateIds.flatMap { outboundByDate[it] ?: emptyList() }

                ProjectOverviewUi(
                    id = it.id,
                    name = it.name,
                    totals = buildTotals(projectStorage, projectOutbound, projectFees, payments),
                    dates = dateSummaries,
                    paymentRecords = payments,
                )
            }
        }
    }

    fun observeDayDetail(projectId: Long, dateId: Long): Flow<DayDetailUi?> {
        return combine(
            projectDao.observeById(projectId),
            dateDao.observeForProject(projectId), // Observe all dates for project totals
            dateDao.observeById(dateId),
            dayPhotoDao.observeForDate(dateId),
            storageDao.observeAll(), // Observe all storage for project totals
            feeDao.observeAll(),     // Observe all fees for project totals
            outboundDao.observeAll(), // Observe all outbound for project totals
            paymentDao.observeForProject(projectId),
        ) { array ->
            val project = array[0] as ProjectEntity?
            val projectDates = array[1] as List<RecordDateEntity>
            val date = array[2] as RecordDateEntity?
            val photos = array[3] as List<DayPhotoEntity>
            val storageRecords = array[4] as List<StorageRecordEntity>
            val feeRecords = array[5] as List<FeeRecordEntity>
            val outboundRecords = array[6] as List<OutboundRecordEntity>
            val projectPayments = array[7] as List<PaymentRecordEntity>

            if (project == null || date == null || date.projectId != projectId) {
                null
            } else {
                val storageByDate = storageRecords.groupBy { it.dateId }
                val feeByDate = feeRecords.groupBy { it.dateId }
                val outboundByDate = outboundRecords.groupBy { it.dateId }

                val dayStorage = storageByDate[date.id] ?: emptyList()
                val dayFee = feeByDate[date.id] ?: emptyList()
                val dayOutbound = outboundByDate[date.id] ?: emptyList()

                val projectDateIds = projectDates.map { it.id }
                val projectStorage = projectDateIds.flatMap { storageByDate[it] ?: emptyList() }
                val projectFees = projectDateIds.flatMap { feeByDate[it] ?: emptyList() }
                val projectOutbound = projectDateIds.flatMap { outboundByDate[it] ?: emptyList() }

                DayDetailUi(
                    projectId = project.id,
                    projectName = project.name,
                    dateId = date.id,
                    date = date.date,
                    totals = buildTotals(dayStorage, dayOutbound, dayFee),
                    projectTotals = buildTotals(projectStorage, projectOutbound, projectFees, projectPayments),
                    photos = photos,
                    storageRecords = dayStorage,
                    outboundRecords = dayOutbound,
                    feeRecords = dayFee,
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

    suspend fun debugFillMockData() {
        val projectId = projectDao.insert(
            ProjectEntity(
                name = "性能测试项目_${System.currentTimeMillis() % 1000}",
                createTime = System.currentTimeMillis()
            )
        )
        
        // 模拟 100 天的数据
        for (i in 1..100) {
            val dateId = dateDao.insert(
                RecordDateEntity(
                    projectId = projectId,
                    date = "2023-01-${i.toString().padStart(2, '0')}"
                )
            )
            
            // 每天 10 条存储记录
            for (j in 1..10) {
                storageDao.insert(
                    StorageRecordEntity(
                        dateId = dateId,
                        name = "货物_${j}",
                        count = j * 10.0,
                        weightPerUnit = 5.0,
                        pricePerWeight = 2.0,
                        totalWeight = j * 50.0,
                        totalPrice = j * 100.0
                    )
                )
            }
            
            // 每天 2 条费用记录
            feeDao.insert(FeeRecordEntity(dateId = dateId, type = FeeTypes.LABOR, amount = 50.0))
            feeDao.insert(FeeRecordEntity(dateId = dateId, type = FeeTypes.AGENCY, amount = 20.0))

            // 每天 1 条出库记录
            outboundDao.insert(
                OutboundRecordEntity(
                    dateId = dateId,
                    name = "出库货物_1",
                    count = 5.0,
                    weightPerUnit = 3.0,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun observeUniqueItemNames(): Flow<List<String>> = storageDao.observeUniqueItemNames()

    suspend fun getLastPriceForItem(name: String): Double? = storageDao.getLastPriceForItem(name)

    suspend fun getLastWeightForItem(name: String): Double? = storageDao.getLastWeightForItem(name)

    suspend fun addOutboundRecord(
        dateId: Long,
        name: String,
        count: Double,
        weightPerUnit: Double,
    ) {
        outboundDao.insert(
            OutboundRecordEntity(
                dateId = dateId,
                name = name.trim(),
                count = count,
                weightPerUnit = weightPerUnit,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun updateOutboundRecord(
        record: OutboundRecordEntity,
        name: String,
        count: Double,
        weightPerUnit: Double,
    ) {
        outboundDao.update(
            record.copy(
                name = name.trim(),
                count = count,
                weightPerUnit = weightPerUnit,
            )
        )
    }

    suspend fun deleteOutboundRecord(record: OutboundRecordEntity) {
        outboundDao.delete(record)
    }

    suspend fun addPaymentRecord(projectId: Long, amount: Double, date: String, remark: String) {
        paymentDao.insert(
            PaymentRecordEntity(
                projectId = projectId,
                amount = amount,
                date = date,
                remark = remark.trim(),
            )
        )
    }

    suspend fun updatePaymentRecord(record: PaymentRecordEntity, amount: Double, date: String, remark: String) {
        paymentDao.update(record.copy(amount = amount, date = date, remark = remark.trim()))
    }

    suspend fun deletePaymentRecord(record: PaymentRecordEntity) {
        paymentDao.delete(record)
    }

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
        outboundRecords: List<OutboundRecordEntity>,
        feeRecords: List<FeeRecordEntity>,
        paymentRecords: List<PaymentRecordEntity> = emptyList(),
    ): Totals {
        val laborFee = feeRecords.filter { it.type == FeeTypes.LABOR }.sumOf { it.amount }
        val agencyFee = feeRecords.filter { it.type == FeeTypes.AGENCY }.sumOf { it.amount }
        val loadingFee = feeRecords.filter { it.type == FeeTypes.LOADING }.sumOf { it.amount }
        val otherFee = feeRecords
            .filter { it.type !in listOf(FeeTypes.LABOR, FeeTypes.AGENCY, FeeTypes.LOADING) }
            .sumOf { it.amount }

        val totalStorageCount = storageRecords.sumOf { it.count }
        val totalStorageWeight = storageRecords.sumOf { it.totalWeight }
        val totalStorageAmount = storageRecords.sumOf { it.totalPrice }

        val totalOutboundCount = outboundRecords.sumOf { it.count }
        val totalOutboundWeight = outboundRecords.sumOf { it.count * it.weightPerUnit }

        val totalPaid = paymentRecords.sumOf { it.amount }
        val totalAmountNeeded = totalStorageAmount + (laborFee + agencyFee + loadingFee + otherFee)

        val itemSummaries = storageRecords.groupBy { it.name }.map { (name, records) ->
            ItemSummary(
                name = name,
                totalCount = records.sumOf { it.count },
                totalWeight = records.sumOf { it.totalWeight },
                totalAmount = records.sumOf { it.totalPrice }
            )
        }.sortedByDescending { it.totalAmount }

        val feeSummaries = feeRecords.groupBy { it.type }.map { (type, records) ->
            FeeSummary(
                type = type,
                totalAmount = records.sumOf { it.amount }
            )
        }.sortedByDescending { it.totalAmount }

        return Totals(
            totalCount = totalStorageCount,
            totalWeight = totalStorageWeight,
            totalStorageAmount = totalStorageAmount,
            totalOutboundCount = totalOutboundCount,
            totalOutboundWeight = totalOutboundWeight,
            netTotalCount = totalStorageCount - totalOutboundCount,
            netTotalWeight = totalStorageWeight - totalOutboundWeight,
            laborFee = laborFee,
            agencyFee = agencyFee,
            loadingFee = loadingFee,
            otherFee = otherFee,
            totalFee = laborFee + agencyFee + loadingFee + otherFee,
            totalPaid = totalPaid,
            totalDebt = totalAmountNeeded - totalPaid,
            itemSummaries = itemSummaries,
            feeSummaries = feeSummaries,
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
                    outboundDao = db.outboundRecordDao(),
                    paymentDao = db.paymentRecordDao(),
                ).also { instance = it }
            }
        }
    }
}
