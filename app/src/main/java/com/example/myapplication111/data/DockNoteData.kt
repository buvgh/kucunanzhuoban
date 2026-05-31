package com.example.myapplication111.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.ColumnInfo
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
import java.time.Duration
import java.time.LocalTime
import java.time.format.DateTimeParseException

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

@Entity(tableName = "fund_projects")
data class FundProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createTime: Long,
)

@Entity(
    tableName = "fund_dates",
    foreignKeys = [
        ForeignKey(
            entity = FundProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("projectId"), Index(value = ["projectId", "date"], unique = true)],
)
data class FundDateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val date: String,
)

object FundRecordType {
    const val INITIAL = 0
    const val INCOME = 1
    const val EXPENSE = 2
}

@Entity(
    tableName = "fund_records",
    foreignKeys = [
        ForeignKey(
            entity = FundDateEntity::class,
            parentColumns = ["id"],
            childColumns = ["dateId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("dateId"), Index("type")],
)
data class FundRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateId: Long,
    val type: Int,
    val name: String,
    val amount: Double,
    val remark: String = "",
    val createTime: Long,
)

@Dao
interface FundProjectDao {
    @Query("SELECT * FROM fund_projects ORDER BY createTime DESC")
    fun observeAll(): Flow<List<FundProjectEntity>>

    @Query("SELECT * FROM fund_projects WHERE id = :projectId LIMIT 1")
    fun observeById(projectId: Long): Flow<FundProjectEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(project: FundProjectEntity): Long

    @Query("DELETE FROM fund_projects WHERE id = :projectId")
    suspend fun deleteById(projectId: Long)
}

@Dao
interface FundDateDao {
    @Query("SELECT * FROM fund_dates ORDER BY date DESC, id DESC")
    fun observeAll(): Flow<List<FundDateEntity>>

    @Query("SELECT * FROM fund_dates WHERE projectId = :projectId ORDER BY date DESC, id DESC")
    fun observeForProject(projectId: Long): Flow<List<FundDateEntity>>

    @Query("SELECT * FROM fund_dates WHERE id = :dateId LIMIT 1")
    fun observeById(dateId: Long): Flow<FundDateEntity?>

    @Query("SELECT * FROM fund_dates WHERE projectId = :projectId AND date = :date LIMIT 1")
    suspend fun findByProjectAndDate(projectId: Long, date: String): FundDateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(date: FundDateEntity): Long

    @Query("DELETE FROM fund_dates WHERE id = :dateId")
    suspend fun deleteById(dateId: Long)
}

@Dao
interface FundRecordDao {
    @Query("SELECT * FROM fund_records ORDER BY createTime DESC, id DESC")
    fun observeAll(): Flow<List<FundRecordEntity>>

    @Query("SELECT * FROM fund_records WHERE dateId = :dateId ORDER BY type ASC, createTime DESC, id DESC")
    fun observeForDate(dateId: Long): Flow<List<FundRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: FundRecordEntity): Long

    @Update
    suspend fun update(record: FundRecordEntity)

    @Delete
    suspend fun delete(record: FundRecordEntity)
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

@Entity(tableName = "project_groups")
data class ProjectGroupEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createTime: Long,
)

@Entity(
    tableName = "project_group_projects",
    primaryKeys = ["groupId", "projectId"],
    foreignKeys = [
        ForeignKey(
            entity = ProjectGroupEntity::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("groupId"), Index("projectId")],
)
data class ProjectGroupProjectEntity(
    val groupId: Long,
    val projectId: Long,
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
    tableName = "secondary_sale_records",
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
data class SecondarySaleRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateId: Long,
    val name: String,
    val weight: Double,
    val unitPrice: Double,
    val totalAmount: Double,
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

object SalaryMode {
    const val HOURLY = 0
    const val DAILY = 1
}

@Entity(
    tableName = "workers",
    indices = [Index("projectId")],
)
data class WorkerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val name: String,
    val salaryMode: Int,
    val hourlyRate: Double = 0.0,
    val dailyRate: Double = 0.0,
    @ColumnInfo(defaultValue = "") val hiddenMonths: String = "",
)

@Entity(
    tableName = "attendance_records",
    foreignKeys = [
        ForeignKey(
            entity = WorkerEntity::class,
            parentColumns = ["id"],
            childColumns = ["workerId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("workerId"),
        Index("projectId"),
        Index(value = ["projectId", "workerId", "date"]),
    ],
)
data class AttendanceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val workerId: Long,
    val date: String,
    val salaryModeSnapshot: Int = SalaryMode.HOURLY,
    val startTime: String? = null,
    val endTime: String? = null,
    val workHours: Double = 0.0,
    val isPresent: Boolean = false,
    val attendancePortion: Double = 0.0,
    val hourlyRateSnapshot: Double = 0.0,
    val dailyRateSnapshot: Double = 0.0,
    val overtimeHours: Double = 0.0,
    val overtimeRate: Double = 0.0,
    val cellColor: Int = 0,
)

data class AttendanceSummary(
    val workerId: Long,
    val workerName: String,
    val salaryMode: Int,
    val totalWorkHours: Double,
    val totalPresentDays: Double,
    val totalSalary: Double,
)

data class AttendanceDashboardRow(
    val summary: AttendanceSummary,
    val cellsByDay: Map<Int, AttendanceDayCell>,
)

data class AttendanceMonthBoard(
    val month: String,
    val rows: List<AttendanceDashboardRow>,
)

data class AttendanceDayCell(
    val totalWorkHours: Double = 0.0,
    val isPresent: Boolean = false,
    val attendancePortion: Double = 0.0,
    val segmentCount: Int = 0,
    val color: Int = 0,
    val records: List<AttendanceEntity> = emptyList(),
)

data class AttendanceProjectSummary(
    val projectId: Long,
    val projectName: String,
    val totalWorkHours: Double,
    val totalPresentDays: Double,
    val totalSalary: Double,
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

data class SecondarySaleSummary(
    val name: String,
    val totalWeight: Double,
    val totalAmount: Double,
    val averageUnitPrice: Double,
)

data class Totals(
    val totalCount: Double = 0.0,
    val totalWeight: Double = 0.0,
    val totalStorageAmount: Double = 0.0,
    val totalOutboundCount: Double = 0.0,
    val totalOutboundWeight: Double = 0.0,
    val netTotalCount: Double = 0.0,
    val netTotalWeight: Double = 0.0,
    val totalSecondarySaleWeight: Double = 0.0,
    val totalSecondarySaleAmount: Double = 0.0,
    val laborFee: Double = 0.0,
    val agencyFee: Double = 0.0,
    val loadingFee: Double = 0.0,
    val otherFee: Double = 0.0,
    val totalFee: Double = 0.0,
    val totalPaid: Double = 0.0,
    val totalDebt: Double = 0.0,
    val itemSummaries: List<ItemSummary> = emptyList(),
    val feeSummaries: List<FeeSummary> = emptyList(),
    val secondarySaleSummaries: List<SecondarySaleSummary> = emptyList(),
)

data class ProjectSummaryUi(
    val id: Long,
    val name: String,
    val createTime: Long,
    val totals: Totals,
)

data class ProjectGroupSummaryUi(
    val id: Long,
    val name: String,
    val createTime: Long,
    val projects: List<ProjectSummaryUi>,
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
    val secondarySaleRecords: List<SecondarySaleRecordEntity>,
    val outboundRecords: List<OutboundRecordEntity>,
    val feeRecords: List<FeeRecordEntity>,
)

data class FundTotals(
    val initial: Double = 0.0,
    val income: Double = 0.0,
    val expense: Double = 0.0,
    val balance: Double = 0.0,
)

data class FundProjectSummaryUi(
    val id: Long,
    val name: String,
    val createTime: Long,
    val totals: FundTotals,
)

data class FundDateSummaryUi(
    val id: Long,
    val date: String,
    val totals: FundTotals,
)

data class FundProjectOverviewUi(
    val id: Long,
    val name: String,
    val totals: FundTotals,
    val dates: List<FundDateSummaryUi>,
)

data class FundDayDetailUi(
    val projectId: Long,
    val projectName: String,
    val dateId: Long,
    val date: String,
    val totals: FundTotals,
    val initialRecords: List<FundRecordEntity>,
    val incomeRecords: List<FundRecordEntity>,
    val expenseRecords: List<FundRecordEntity>,
)

data class FundExportDateUi(
    val id: Long,
    val date: String,
    val totals: FundTotals,
    val initialRecords: List<FundRecordEntity>,
    val incomeRecords: List<FundRecordEntity>,
    val expenseRecords: List<FundRecordEntity>,
)

data class FundProjectExportUi(
    val id: Long,
    val name: String,
    val totals: FundTotals,
    val dates: List<FundExportDateUi>,
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
interface ProjectGroupDao {
    @Query("SELECT * FROM project_groups ORDER BY createTime DESC")
    fun observeGroups(): Flow<List<ProjectGroupEntity>>

    @Query("SELECT * FROM project_group_projects")
    fun observeMembers(): Flow<List<ProjectGroupProjectEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: ProjectGroupEntity): Long

    @Update
    suspend fun updateGroup(group: ProjectGroupEntity)

    @Query("DELETE FROM project_groups WHERE id = :groupId")
    suspend fun deleteGroup(groupId: Long)

    @Query("DELETE FROM project_group_projects WHERE groupId = :groupId")
    suspend fun deleteMembers(groupId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMembers(members: List<ProjectGroupProjectEntity>)
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
interface SecondarySaleRecordDao {
    @Query("SELECT * FROM secondary_sale_records ORDER BY id DESC")
    fun observeAll(): Flow<List<SecondarySaleRecordEntity>>

    @Query("SELECT * FROM secondary_sale_records WHERE dateId = :dateId ORDER BY id DESC")
    fun observeForDate(dateId: Long): Flow<List<SecondarySaleRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: SecondarySaleRecordEntity): Long

    @Update
    suspend fun update(record: SecondarySaleRecordEntity)

    @Delete
    suspend fun delete(record: SecondarySaleRecordEntity)
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

@Dao
interface WorkerDao {
    @Query("SELECT * FROM workers WHERE projectId = :projectId ORDER BY id ASC")
    fun observeForProject(projectId: Long): Flow<List<WorkerEntity>>

    @Query("SELECT * FROM workers WHERE id = :workerId LIMIT 1")
    suspend fun getById(workerId: Long): WorkerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(worker: WorkerEntity): Long

    @Update
    suspend fun update(worker: WorkerEntity)

    @Delete
    suspend fun delete(worker: WorkerEntity)
}

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance_records ORDER BY date DESC, id DESC")
    fun observeAll(): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance_records WHERE workerId = :workerId ORDER BY date DESC, id DESC")
    fun observeForWorker(workerId: Long): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance_records WHERE projectId = :projectId AND substr(date, 1, 7) = :month ORDER BY date ASC, id ASC")
    fun observeForProjectMonth(projectId: Long, month: String): Flow<List<AttendanceEntity>>

    @Query("SELECT DISTINCT substr(date, 1, 7) FROM attendance_records WHERE projectId = :projectId ORDER BY substr(date, 1, 7) DESC")
    fun observeMonthsForProject(projectId: Long): Flow<List<String>>

    @Query(
        """
        SELECT
            w.id AS workerId,
            w.name AS workerName,
            COALESCE(MAX(a.salaryModeSnapshot), 0) AS salaryMode,
            COALESCE(SUM(CASE WHEN a.salaryModeSnapshot = 0 THEN a.workHours ELSE 0 END), 0) AS totalWorkHours,
            COALESCE(SUM(CASE WHEN a.salaryModeSnapshot = 1 AND a.isPresent = 1 THEN a.attendancePortion ELSE 0 END), 0) AS totalPresentDays,
            COALESCE(SUM(
                CASE
                    WHEN a.salaryModeSnapshot = 0 THEN a.workHours * a.hourlyRateSnapshot
                    WHEN a.salaryModeSnapshot = 1 AND a.isPresent = 1 THEN a.dailyRateSnapshot
                    ELSE 0
                END
            ) + SUM(COALESCE(a.overtimeHours * a.overtimeRate, 0)), 0) AS totalSalary
        FROM workers AS w
        LEFT JOIN attendance_records AS a
            ON a.workerId = w.id
            AND a.projectId = :projectId
            AND substr(a.date, 1, 7) = :month
        WHERE w.projectId = :projectId
          AND w.hiddenMonths NOT LIKE '%' || :month || '%'
        GROUP BY w.id, w.name
        ORDER BY w.id DESC
        """
    )
    fun observeProjectMonthlySummary(projectId: Long, month: String): Flow<List<AttendanceSummary>>

    @Query(
        """
        SELECT
            p.id AS projectId,
            p.name AS projectName,
            COALESCE(SUM(CASE WHEN a.salaryModeSnapshot = 0 THEN a.workHours ELSE 0 END), 0) AS totalWorkHours,
            COALESCE(SUM(CASE WHEN a.salaryModeSnapshot = 1 AND a.isPresent = 1 THEN a.attendancePortion ELSE 0 END), 0) AS totalPresentDays,
            COALESCE(SUM(
                CASE
                    WHEN a.salaryModeSnapshot = 0 THEN a.workHours * a.hourlyRateSnapshot
                    WHEN a.salaryModeSnapshot = 1 AND a.isPresent = 1 THEN a.dailyRateSnapshot
                    ELSE 0
                END
            ) + SUM(COALESCE(a.overtimeHours * a.overtimeRate, 0)), 0) AS totalSalary
        FROM projects AS p
        LEFT JOIN attendance_records AS a
            ON a.projectId = p.id
        GROUP BY p.id, p.name
        ORDER BY p.createTime DESC
        """
    )
    fun observeProjectAttendanceSummaries(): Flow<List<AttendanceProjectSummary>>

    @Query("SELECT * FROM attendance_records WHERE id = :attendanceId LIMIT 1")
    suspend fun getById(attendanceId: Long): AttendanceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: AttendanceEntity): Long

    @Update
    suspend fun update(record: AttendanceEntity)

    @Delete
    suspend fun delete(record: AttendanceEntity)
}

@Database(
    entities = [
        ProjectEntity::class,
        FundProjectEntity::class,
        ProjectGroupEntity::class,
        ProjectGroupProjectEntity::class,
        RecordDateEntity::class,
        FundDateEntity::class,
        StorageRecordEntity::class,
        SecondarySaleRecordEntity::class,
        FeeRecordEntity::class,
        DayPhotoEntity::class,
        OutboundRecordEntity::class,
        PaymentRecordEntity::class,
        FundRecordEntity::class,
        WorkerEntity::class,
        AttendanceEntity::class,
    ],
    version = 16,
    exportSchema = false,
)
abstract class DockNoteDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun fundProjectDao(): FundProjectDao
    abstract fun projectGroupDao(): ProjectGroupDao
    abstract fun recordDateDao(): RecordDateDao
    abstract fun fundDateDao(): FundDateDao
    abstract fun storageRecordDao(): StorageRecordDao
    abstract fun secondarySaleRecordDao(): SecondarySaleRecordDao
    abstract fun feeRecordDao(): FeeRecordDao
    abstract fun dayPhotoDao(): DayPhotoDao
    abstract fun outboundRecordDao(): OutboundRecordDao
    abstract fun paymentRecordDao(): PaymentRecordDao
    abstract fun fundRecordDao(): FundRecordDao
    abstract fun workerDao(): WorkerDao
    abstract fun attendanceDao(): AttendanceDao

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

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS workers (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        salaryMode INTEGER NOT NULL,
                        hourlyRate REAL NOT NULL,
                        dailyRate REAL NOT NULL
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS attendance_records (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        projectId INTEGER NOT NULL DEFAULT 0,
                        workerId INTEGER NOT NULL,
                        date TEXT NOT NULL,
                        startTime TEXT,
                        endTime TEXT,
                        workHours REAL NOT NULL,
                        isPresent INTEGER NOT NULL,
                        FOREIGN KEY(workerId) REFERENCES workers(id) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent(),
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_attendance_records_workerId ON attendance_records(workerId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_attendance_records_projectId ON attendance_records(projectId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_attendance_records_projectId_workerId_date ON attendance_records(projectId, workerId, date)")
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP INDEX IF EXISTS index_attendance_records_workerId_date")
                db.execSQL("ALTER TABLE attendance_records ADD COLUMN projectId INTEGER NOT NULL DEFAULT 0")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_attendance_records_workerId ON attendance_records(workerId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_attendance_records_projectId ON attendance_records(projectId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_attendance_records_projectId_workerId_date ON attendance_records(projectId, workerId, date)")
            }
        }

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS project_groups (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        createTime INTEGER NOT NULL
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS project_group_projects (
                        groupId INTEGER NOT NULL,
                        projectId INTEGER NOT NULL,
                        PRIMARY KEY(groupId, projectId),
                        FOREIGN KEY(groupId) REFERENCES project_groups(id) ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY(projectId) REFERENCES projects(id) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent(),
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_project_group_projects_groupId ON project_group_projects(groupId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_project_group_projects_projectId ON project_group_projects(projectId)")
            }
        }

        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE workers ADD COLUMN projectId INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE attendance_records ADD COLUMN hourlyRateSnapshot REAL NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE attendance_records ADD COLUMN dailyRateSnapshot REAL NOT NULL DEFAULT 0")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_workers_projectId ON workers(projectId)")
            }
        }

        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE attendance_records ADD COLUMN salaryModeSnapshot INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS secondary_sale_records (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        dateId INTEGER NOT NULL,
                        name TEXT NOT NULL,
                        weight REAL NOT NULL,
                        amount REAL NOT NULL DEFAULT 0,
                        FOREIGN KEY(dateId) REFERENCES record_dates(id) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent(),
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_secondary_sale_records_dateId ON secondary_sale_records(dateId)")
            }
        }

        private val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                val columnNames = buildSet {
                    val cursor = db.query("PRAGMA table_info(secondary_sale_records)")
                    cursor.use {
                        val nameIndex = it.getColumnIndex("name")
                        while (it.moveToNext()) {
                            add(it.getString(nameIndex))
                        }
                    }
                }

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS secondary_sale_records_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        dateId INTEGER NOT NULL,
                        name TEXT NOT NULL,
                        weight REAL NOT NULL,
                        unitPrice REAL NOT NULL,
                        totalAmount REAL NOT NULL,
                        FOREIGN KEY(dateId) REFERENCES record_dates(id) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent(),
                )

                when {
                    "amount" in columnNames -> {
                        db.execSQL(
                            """
                            INSERT INTO secondary_sale_records_new (id, dateId, name, weight, unitPrice, totalAmount)
                            SELECT id, dateId, name, weight,
                                   CASE WHEN weight = 0 THEN 0 ELSE amount / weight END,
                                   amount
                            FROM secondary_sale_records
                            """.trimIndent(),
                        )
                    }

                    "unitPrice" in columnNames && "totalAmount" in columnNames -> {
                        db.execSQL(
                            """
                            INSERT INTO secondary_sale_records_new (id, dateId, name, weight, unitPrice, totalAmount)
                            SELECT id, dateId, name, weight, unitPrice, totalAmount
                            FROM secondary_sale_records
                            """.trimIndent(),
                        )
                    }
                }

                db.execSQL("DROP TABLE secondary_sale_records")
                db.execSQL("ALTER TABLE secondary_sale_records_new RENAME TO secondary_sale_records")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_secondary_sale_records_dateId ON secondary_sale_records(dateId)")
            }
        }

        private val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE attendance_records ADD COLUMN cellColor INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE attendance_records ADD COLUMN attendancePortion REAL NOT NULL DEFAULT 0")
                db.execSQL(
                    """
                    UPDATE attendance_records
                    SET attendancePortion = CASE
                        WHEN salaryModeSnapshot = 1 AND isPresent = 1 THEN 1.0
                        ELSE 0.0
                    END
                    """.trimIndent(),
                )
            }
        }

        private val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE attendance_records ADD COLUMN overtimeHours REAL NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE attendance_records ADD COLUMN overtimeRate REAL NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_14_15 = object : Migration(14, 15) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS fund_projects (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        createTime INTEGER NOT NULL
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS fund_dates (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        projectId INTEGER NOT NULL,
                        date TEXT NOT NULL,
                        FOREIGN KEY(projectId) REFERENCES fund_projects(id) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent(),
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_fund_dates_projectId ON fund_dates(projectId)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_fund_dates_projectId_date ON fund_dates(projectId, date)")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS fund_records (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        dateId INTEGER NOT NULL,
                        type INTEGER NOT NULL,
                        name TEXT NOT NULL,
                        amount REAL NOT NULL,
                        remark TEXT NOT NULL DEFAULT '',
                        createTime INTEGER NOT NULL,
                        FOREIGN KEY(dateId) REFERENCES fund_dates(id) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent(),
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_fund_records_dateId ON fund_records(dateId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_fund_records_type ON fund_records(type)")
            }
        }

        private val MIGRATION_15_16 = object : Migration(15, 16) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE workers ADD COLUMN hiddenMonths TEXT NOT NULL DEFAULT ''")
            }
        }

        fun getInstance(context: Context): DockNoteDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    DockNoteDatabase::class.java,
                    "dock_note.db",
                ).setJournalMode(RoomDatabase.JournalMode.TRUNCATE)
                .addMigrations(
                    MIGRATION_1_2,
                    MIGRATION_2_3,
                    MIGRATION_3_4,
                    MIGRATION_4_5,
                    MIGRATION_5_6,
                    MIGRATION_6_7,
                    MIGRATION_7_8,
                    MIGRATION_8_9,
                    MIGRATION_9_10,
                    MIGRATION_10_11,
                    MIGRATION_11_12,
                    MIGRATION_12_13,
                    MIGRATION_13_14,
                    MIGRATION_14_15,
                    MIGRATION_15_16,
                ).build().also { instance = it }
            }
        }
    }
}

class DockNoteRepository private constructor(
    private val projectDao: ProjectDao,
    private val fundProjectDao: FundProjectDao,
    private val projectGroupDao: ProjectGroupDao,
    private val dateDao: RecordDateDao,
    private val fundDateDao: FundDateDao,
    private val storageDao: StorageRecordDao,
    private val secondarySaleDao: SecondarySaleRecordDao,
    private val feeDao: FeeRecordDao,
    private val dayPhotoDao: DayPhotoDao,
    private val outboundDao: OutboundRecordDao,
    private val paymentDao: PaymentRecordDao,
    private val fundRecordDao: FundRecordDao,
    private val workerDao: WorkerDao,
    private val attendanceDao: AttendanceDao,
) {
    fun observeProjectSummaries(): Flow<List<ProjectSummaryUi>> {
        return combine(
            projectDao.observeAll(),
            dateDao.observeAll(),
            storageDao.observeAll(),
            secondarySaleDao.observeAll(),
            feeDao.observeAll(),
            outboundDao.observeAll(),
            QueryAllPaymentsFlow(),
        ) { flows: Array<Any> ->
            val projects = flows[0] as List<ProjectEntity>
            val dates = flows[1] as List<RecordDateEntity>
            val storageRecords = flows[2] as List<StorageRecordEntity>
            val secondarySaleRecords = flows[3] as List<SecondarySaleRecordEntity>
            val feeRecords = flows[4] as List<FeeRecordEntity>
            val outboundRecords = flows[5] as List<OutboundRecordEntity>
            val paymentRecords = flows[6] as List<PaymentRecordEntity>

            val datesByProject = dates.groupBy { it.projectId }
            val storageByDate = storageRecords.groupBy { it.dateId }
            val secondarySalesByDate = secondarySaleRecords.groupBy { it.dateId }
            val feeByDate = feeRecords.groupBy { it.dateId }
            val outboundByDate = outboundRecords.groupBy { it.dateId }
            val paymentsByProject = paymentRecords.groupBy { it.projectId }

            projects.map { project ->
                val projectDates = datesByProject[project.id] ?: emptyList()
                val dateIds = projectDates.map { it.id }
                
                val projectStorage = dateIds.flatMap { storageByDate[it] ?: emptyList() }
                val projectSecondarySales = dateIds.flatMap { secondarySalesByDate[it] ?: emptyList() }
                val projectFees = dateIds.flatMap { feeByDate[it] ?: emptyList() }
                val projectOutbound = dateIds.flatMap { outboundByDate[it] ?: emptyList() }
                val projectPayments = paymentsByProject[project.id] ?: emptyList()

                ProjectSummaryUi(
                    id = project.id,
                    name = project.name,
                    createTime = project.createTime,
                    totals = buildTotals(projectStorage, projectSecondarySales, projectOutbound, projectFees, projectPayments),
                )
            }
        }
    }

    fun observeFundProjectSummaries(): Flow<List<FundProjectSummaryUi>> {
        return combine(
            fundProjectDao.observeAll(),
            fundDateDao.observeAll(),
            fundRecordDao.observeAll(),
        ) { projects, dates, records ->
            val datesByProject = dates.groupBy { it.projectId }
            val recordsByDate = records.groupBy { it.dateId }

            projects.map { project ->
                val projectRecords = datesByProject[project.id]
                    .orEmpty()
                    .flatMap { recordsByDate[it.id].orEmpty() }
                FundProjectSummaryUi(
                    id = project.id,
                    name = project.name,
                    createTime = project.createTime,
                    totals = buildFundTotals(projectRecords),
                )
            }
        }
    }

    fun observeProjectGroupSummaries(): Flow<List<ProjectGroupSummaryUi>> {
        return combine(
            projectGroupDao.observeGroups(),
            projectGroupDao.observeMembers(),
            observeProjectSummaries(),
        ) { groups, members, projectSummaries ->
            val projectsById = projectSummaries.associateBy { it.id }
            val memberIdsByGroup = members.groupBy { it.groupId }

            groups.map { group ->
                val selectedProjects = memberIdsByGroup[group.id]
                    .orEmpty()
                    .mapNotNull { projectsById[it.projectId] }

                ProjectGroupSummaryUi(
                    id = group.id,
                    name = group.name,
                    createTime = group.createTime,
                    projects = selectedProjects,
                    totals = mergeTotals(selectedProjects.map { it.totals }),
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
            secondarySaleDao.observeAll(),
            feeDao.observeAll(),
            outboundDao.observeAll(),
            paymentDao.observeForProject(projectId),
        ) { flows: Array<Any?> ->
            val project = flows[0] as ProjectEntity?
            val dates = flows[1] as List<RecordDateEntity>
            val storageRecords = flows[2] as List<StorageRecordEntity>
            val secondarySaleRecords = flows[3] as List<SecondarySaleRecordEntity>
            val feeRecords = flows[4] as List<FeeRecordEntity>
            val outboundRecords = flows[5] as List<OutboundRecordEntity>
            val payments = flows[6] as List<PaymentRecordEntity>

            project?.let {
                val storageByDate = storageRecords.groupBy { it.dateId }
                val secondarySalesByDate = secondarySaleRecords.groupBy { it.dateId }
                val feeByDate = feeRecords.groupBy { it.dateId }
                val outboundByDate = outboundRecords.groupBy { it.dateId }

                val dateSummaries = dates.map { date ->
                    val dayStorageRecords = storageByDate[date.id] ?: emptyList()
                    val daySecondarySaleRecords = secondarySalesByDate[date.id] ?: emptyList()
                    val dayFeeRecords = feeByDate[date.id] ?: emptyList()
                    val dayOutboundRecords = outboundByDate[date.id] ?: emptyList()
                    DateSummaryUi(
                        id = date.id,
                        date = date.date,
                        totals = buildTotals(dayStorageRecords, daySecondarySaleRecords, dayOutboundRecords, dayFeeRecords),
                    )
                }

                val dateIds = dates.map { it.id }
                val projectStorage = dateIds.flatMap { storageByDate[it] ?: emptyList() }
                val projectSecondarySales = dateIds.flatMap { secondarySalesByDate[it] ?: emptyList() }
                val projectFees = dateIds.flatMap { feeByDate[it] ?: emptyList() }
                val projectOutbound = dateIds.flatMap { outboundByDate[it] ?: emptyList() }

                ProjectOverviewUi(
                    id = it.id,
                    name = it.name,
                    totals = buildTotals(projectStorage, projectSecondarySales, projectOutbound, projectFees, payments),
                    dates = dateSummaries,
                    paymentRecords = payments,
                )
            }
        }
    }

    fun observeFundProjectOverview(projectId: Long): Flow<FundProjectOverviewUi?> {
        return combine(
            fundProjectDao.observeById(projectId),
            fundDateDao.observeForProject(projectId),
            fundRecordDao.observeAll(),
        ) { project, dates, records ->
            project?.let {
                val recordsByDate = records.groupBy { record -> record.dateId }
                val dateSummaries = dates.map { date ->
                    FundDateSummaryUi(
                        id = date.id,
                        date = date.date,
                        totals = buildFundTotals(recordsByDate[date.id].orEmpty()),
                    )
                }
                val projectRecords = dates.flatMap { date -> recordsByDate[date.id].orEmpty() }
                FundProjectOverviewUi(
                    id = it.id,
                    name = it.name,
                    totals = buildFundTotals(projectRecords),
                    dates = dateSummaries,
                )
            }
        }
    }

    fun observeFundProjectExport(projectId: Long): Flow<FundProjectExportUi?> {
        return combine(
            fundProjectDao.observeById(projectId),
            fundDateDao.observeForProject(projectId),
            fundRecordDao.observeAll(),
        ) { project, dates, records ->
            project?.let {
                val recordsByDate = records.groupBy { record -> record.dateId }
                val orderedDates = dates.sortedBy { date -> date.date }
                val exportDates = orderedDates.map { date ->
                    val dayRecords = recordsByDate[date.id].orEmpty()
                    FundExportDateUi(
                        id = date.id,
                        date = date.date,
                        totals = buildFundTotals(dayRecords),
                        initialRecords = dayRecords.filter { it.type == FundRecordType.INITIAL },
                        incomeRecords = dayRecords.filter { it.type == FundRecordType.INCOME },
                        expenseRecords = dayRecords.filter { it.type == FundRecordType.EXPENSE },
                    )
                }
                FundProjectExportUi(
                    id = it.id,
                    name = it.name,
                    totals = buildFundTotals(exportDates.flatMap { date -> date.initialRecords + date.incomeRecords + date.expenseRecords }),
                    dates = exportDates,
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
            secondarySaleDao.observeAll(),
            feeDao.observeAll(),     // Observe all fees for project totals
            outboundDao.observeAll(), // Observe all outbound for project totals
            paymentDao.observeForProject(projectId),
        ) { array ->
            val project = array[0] as ProjectEntity?
            val projectDates = array[1] as List<RecordDateEntity>
            val date = array[2] as RecordDateEntity?
            val photos = array[3] as List<DayPhotoEntity>
            val storageRecords = array[4] as List<StorageRecordEntity>
            val secondarySaleRecords = array[5] as List<SecondarySaleRecordEntity>
            val feeRecords = array[6] as List<FeeRecordEntity>
            val outboundRecords = array[7] as List<OutboundRecordEntity>
            val projectPayments = array[8] as List<PaymentRecordEntity>

            if (project == null || date == null || date.projectId != projectId) {
                null
            } else {
                val storageByDate = storageRecords.groupBy { it.dateId }
                val secondarySalesByDate = secondarySaleRecords.groupBy { it.dateId }
                val feeByDate = feeRecords.groupBy { it.dateId }
                val outboundByDate = outboundRecords.groupBy { it.dateId }

                val dayStorage = storageByDate[date.id] ?: emptyList()
                val daySecondarySales = secondarySalesByDate[date.id] ?: emptyList()
                val dayFee = feeByDate[date.id] ?: emptyList()
                val dayOutbound = outboundByDate[date.id] ?: emptyList()

                val projectDateIds = projectDates.map { it.id }
                val projectStorage = projectDateIds.flatMap { storageByDate[it] ?: emptyList() }
                val projectSecondarySales = projectDateIds.flatMap { secondarySalesByDate[it] ?: emptyList() }
                val projectFees = projectDateIds.flatMap { feeByDate[it] ?: emptyList() }
                val projectOutbound = projectDateIds.flatMap { outboundByDate[it] ?: emptyList() }

                DayDetailUi(
                    projectId = project.id,
                    projectName = project.name,
                    dateId = date.id,
                    date = date.date,
                    totals = buildTotals(dayStorage, daySecondarySales, dayOutbound, dayFee),
                    projectTotals = buildTotals(projectStorage, projectSecondarySales, projectOutbound, projectFees, projectPayments),
                    photos = photos,
                    storageRecords = dayStorage,
                    secondarySaleRecords = daySecondarySales,
                    outboundRecords = dayOutbound,
                    feeRecords = dayFee,
                )
            }
        }
    }

    fun observeFundDayDetail(projectId: Long, dateId: Long): Flow<FundDayDetailUi?> {
        return combine(
            fundProjectDao.observeById(projectId),
            fundDateDao.observeById(dateId),
            fundRecordDao.observeForDate(dateId),
        ) { project, date, records ->
            if (project == null || date == null || date.projectId != projectId) {
                null
            } else {
                FundDayDetailUi(
                    projectId = project.id,
                    projectName = project.name,
                    dateId = date.id,
                    date = date.date,
                    totals = buildFundTotals(records),
                    initialRecords = records.filter { it.type == FundRecordType.INITIAL },
                    incomeRecords = records.filter { it.type == FundRecordType.INCOME },
                    expenseRecords = records.filter { it.type == FundRecordType.EXPENSE },
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

    suspend fun createFundProject(name: String) {
        val normalizedName = name.trim()
        if (normalizedName.isBlank()) return
        fundProjectDao.insert(
            FundProjectEntity(
                name = normalizedName,
                createTime = System.currentTimeMillis(),
            ),
        )
    }

    suspend fun deleteProject(projectId: Long) {
        projectDao.deleteById(projectId)
    }

    suspend fun deleteFundProject(projectId: Long) {
        fundProjectDao.deleteById(projectId)
    }

    suspend fun createProjectGroup(name: String, projectIds: List<Long>): Long {
        val normalizedName = name.trim()
        require(normalizedName.isNotBlank()) { "汇总名称不能为空" }
        require(projectIds.isNotEmpty()) { "至少选择一个项目" }

        val groupId = projectGroupDao.insertGroup(
            ProjectGroupEntity(
                name = normalizedName,
                createTime = System.currentTimeMillis(),
            ),
        )
        projectGroupDao.insertMembers(projectIds.distinct().map { ProjectGroupProjectEntity(groupId, it) })
        return groupId
    }

    suspend fun updateProjectGroup(groupId: Long, name: String, projectIds: List<Long>) {
        val normalizedName = name.trim()
        require(normalizedName.isNotBlank()) { "汇总名称不能为空" }
        require(projectIds.isNotEmpty()) { "至少选择一个项目" }

        projectGroupDao.updateGroup(
            ProjectGroupEntity(
                id = groupId,
                name = normalizedName,
                createTime = System.currentTimeMillis(),
            ),
        )
        projectGroupDao.deleteMembers(groupId)
        projectGroupDao.insertMembers(projectIds.distinct().map { ProjectGroupProjectEntity(groupId, it) })
    }

    suspend fun deleteProjectGroup(groupId: Long) {
        projectGroupDao.deleteGroup(groupId)
    }

    suspend fun createDate(projectId: Long, date: String): Long {
        return ensureDate(projectId, date.trim())
    }

    suspend fun createFundDate(projectId: Long, date: String): Long {
        return ensureFundDate(projectId, date.trim())
    }

    suspend fun deleteDate(dateId: Long) {
        dateDao.deleteById(dateId)
    }

    suspend fun deleteFundDate(dateId: Long) {
        fundDateDao.deleteById(dateId)
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

    suspend fun addSecondarySaleRecord(
        dateId: Long,
        name: String,
        weight: Double,
        unitPrice: Double,
    ) {
        secondarySaleDao.insert(
            SecondarySaleRecordEntity(
                dateId = dateId,
                name = name.trim(),
                weight = weight,
                unitPrice = unitPrice,
                totalAmount = weight * unitPrice,
            ),
        )
    }

    suspend fun updateSecondarySaleRecord(
        record: SecondarySaleRecordEntity,
        name: String,
        weight: Double,
        unitPrice: Double,
    ) {
        secondarySaleDao.update(
            record.copy(
                name = name.trim(),
                weight = weight,
                unitPrice = unitPrice,
                totalAmount = weight * unitPrice,
            ),
        )
    }

    suspend fun deleteSecondarySaleRecord(record: SecondarySaleRecordEntity) {
        secondarySaleDao.delete(record)
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

    suspend fun addFundRecord(dateId: Long, type: Int, name: String, amount: Double, remark: String) {
        require(type in setOf(FundRecordType.INITIAL, FundRecordType.INCOME, FundRecordType.EXPENSE)) { "资金类型不合法" }
        fundRecordDao.insert(
            FundRecordEntity(
                dateId = dateId,
                type = type,
                name = name.trim(),
                amount = amount,
                remark = remark.trim(),
                createTime = System.currentTimeMillis(),
            ),
        )
    }

    suspend fun updateFundRecord(record: FundRecordEntity, name: String, amount: Double, remark: String) {
        fundRecordDao.update(
            record.copy(
                name = name.trim(),
                amount = amount,
                remark = remark.trim(),
            ),
        )
    }

    suspend fun deleteFundRecord(record: FundRecordEntity) {
        fundRecordDao.delete(record)
    }

    fun observeWorkers(projectId: Long): Flow<List<WorkerEntity>> = workerDao.observeForProject(projectId)

    fun observeAttendanceRecords(): Flow<List<AttendanceEntity>> = attendanceDao.observeAll()

    fun observeAttendanceForWorker(workerId: Long): Flow<List<AttendanceEntity>> {
        return attendanceDao.observeForWorker(workerId)
    }

    fun observeMonthlyAttendanceSummary(projectId: Long, month: String): Flow<List<AttendanceSummary>> {
        require(month.matches(Regex("""\d{4}-\d{2}"""))) { "月份格式必须为 yyyy-MM" }
        return attendanceDao.observeProjectMonthlySummary(projectId, month)
    }

    fun observeProjectAttendanceSummaries(): Flow<List<AttendanceProjectSummary>> {
        return attendanceDao.observeProjectAttendanceSummaries()
    }

    fun observeMonthlyAttendanceDashboard(projectId: Long, month: String): Flow<List<AttendanceDashboardRow>> {
        require(month.matches(Regex("""\d{4}-\d{2}"""))) { "月份格式必须为 yyyy-MM" }
        return combine(
            attendanceDao.observeProjectMonthlySummary(projectId, month),
            attendanceDao.observeForProjectMonth(projectId, month),
        ) { summaries, records ->
            val recordsByWorker = records.groupBy { it.workerId }
            summaries.map { summary ->
                val cellsByDay = recordsByWorker[summary.workerId]
                    .orEmpty()
                    .groupBy { it.date.substringAfterLast("-").toInt() }
                    .mapValues { (_, dayRecords) ->
                        val orderedRecords = dayRecords.sortedWith(
                            compareBy<AttendanceEntity> { it.startTime.orEmpty() }.thenBy { it.id },
                        )
                        AttendanceDayCell(
                            totalWorkHours = orderedRecords.sumOf { it.workHours },
                            isPresent = orderedRecords.any { it.isPresent },
                            attendancePortion = orderedRecords.maxOfOrNull { it.attendancePortion } ?: 0.0,
                            segmentCount = orderedRecords.size,
                            color = orderedRecords.lastOrNull { it.cellColor != 0 }?.cellColor ?: 0,
                            records = orderedRecords,
                        )
                    }

                AttendanceDashboardRow(
                    summary = summary,
                    cellsByDay = cellsByDay,
                )
            }
        }
    }

    fun observeAttendanceMonthBoards(projectId: Long): Flow<List<AttendanceMonthBoard>> {
        return attendanceDao.observeMonthsForProject(projectId).flatMapLatest { months ->
            if (months.isEmpty()) {
                flowOf(emptyList())
            } else {
                combine(months.map { month ->
                    observeMonthlyAttendanceDashboard(projectId, month)
                }) { monthRows ->
                    months.mapIndexed { index, month ->
                        AttendanceMonthBoard(
                            month = month,
                            rows = monthRows[index] as List<AttendanceDashboardRow>,
                        )
                    }
                }
            }
        }
    }

    suspend fun createWorker(
        projectId: Long,
        name: String,
        salaryMode: Int = SalaryMode.HOURLY,
        hourlyRate: Double = 0.0,
        dailyRate: Double = 0.0,
    ): Long {
        val normalizedName = name.trim()
        require(normalizedName.isNotBlank()) { "人员姓名不能为空" }
        require(salaryMode == SalaryMode.HOURLY || salaryMode == SalaryMode.DAILY) { "计薪模式不合法" }

        return workerDao.insert(
            WorkerEntity(
                projectId = projectId,
                name = normalizedName,
                salaryMode = salaryMode,
                hourlyRate = hourlyRate,
                dailyRate = dailyRate,
            ),
        )
    }

    suspend fun updateWorker(
        worker: WorkerEntity,
        name: String,
        salaryMode: Int,
        hourlyRate: Double,
        dailyRate: Double,
    ) {
        val normalizedName = name.trim()
        require(normalizedName.isNotBlank()) { "人员姓名不能为空" }
        require(salaryMode == SalaryMode.HOURLY || salaryMode == SalaryMode.DAILY) { "计薪模式不合法" }

        workerDao.update(
            worker.copy(
                name = normalizedName,
                salaryMode = salaryMode,
                hourlyRate = hourlyRate,
                dailyRate = dailyRate,
            ),
        )
    }

    suspend fun saveWorker(worker: WorkerEntity) {
        workerDao.update(worker)
    }

    suspend fun deleteWorker(worker: WorkerEntity) {
        workerDao.delete(worker)
    }

    suspend fun saveAttendanceRecord(
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
    ): Long {
        val worker = workerDao.getById(workerId) ?: error("人员不存在")
        require(worker.projectId == projectId) { "该人员不属于当前项目" }
        val record = buildAttendanceRecord(
            existingId = 0,
            projectId = projectId,
            worker = worker,
            date = date,
            salaryMode = salaryMode,
            startTime = startTime,
            endTime = endTime,
            isPresent = isPresent,
            attendancePortion = attendancePortion,
            hourlyRate = hourlyRate,
            dailyRate = dailyRate,
            overtimeHours = overtimeHours,
            overtimeRate = overtimeRate,
            cellColor = cellColor,
        )
        rememberWorkerRates(
            worker = worker,
            hourlyRate = record.hourlyRateSnapshot,
            dailyRate = if (record.salaryModeSnapshot == SalaryMode.DAILY && record.attendancePortion in 0.0..0.99) {
                worker.dailyRate
            } else {
                record.dailyRateSnapshot
            },
        )
        return attendanceDao.insert(record)
    }

    suspend fun updateAttendanceRecord(
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
        val existing = attendanceDao.getById(attendanceId) ?: error("考勤记录不存在")
        val worker = workerDao.getById(existing.workerId) ?: error("人员不存在")
        require(worker.projectId == existing.projectId) { "该人员不属于当前项目" }
        val record = buildAttendanceRecord(
            existingId = attendanceId,
            projectId = existing.projectId,
            worker = worker,
            date = date,
            salaryMode = salaryMode,
            startTime = startTime,
            endTime = endTime,
            isPresent = isPresent,
            attendancePortion = attendancePortion,
            hourlyRate = hourlyRate,
            dailyRate = dailyRate,
            overtimeHours = overtimeHours,
            overtimeRate = overtimeRate,
            cellColor = cellColor,
        )
        rememberWorkerRates(
            worker = worker,
            hourlyRate = record.hourlyRateSnapshot,
            dailyRate = if (record.salaryModeSnapshot == SalaryMode.DAILY && record.attendancePortion in 0.0..0.99) {
                worker.dailyRate
            } else {
                record.dailyRateSnapshot
            },
        )
        attendanceDao.update(record)
    }

    suspend fun deleteAttendanceRecord(record: AttendanceEntity) {
        attendanceDao.delete(record)
    }

    private suspend fun ensureDate(projectId: Long, date: String): Long {
        require(date.isNotBlank())
        val existing = dateDao.findByProjectAndDate(projectId, date)
        if (existing != null) return existing.id
        return dateDao.insert(RecordDateEntity(projectId = projectId, date = date))
    }

    private suspend fun ensureFundDate(projectId: Long, date: String): Long {
        require(date.isNotBlank())
        val existing = fundDateDao.findByProjectAndDate(projectId, date)
        if (existing != null) return existing.id
        return fundDateDao.insert(FundDateEntity(projectId = projectId, date = date))
    }

    private fun buildAttendanceRecord(
        existingId: Long,
        projectId: Long,
        worker: WorkerEntity,
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
    ): AttendanceEntity {
        val normalizedDate = date.trim()
        require(normalizedDate.isNotBlank()) { "考勤日期不能为空" }

        return when (salaryMode) {
            SalaryMode.HOURLY -> {
                val start = startTime?.trim().orEmpty()
                val end = endTime?.trim().orEmpty()
                val normalizedHourlyRate = hourlyRate ?: worker.hourlyRate
                require(start.isNotBlank() && end.isNotBlank()) { "计时人员必须填写起始和结束时间" }
                require(normalizedHourlyRate >= 0.0) { "时薪不能小于 0" }
                require(overtimeHours >= 0.0 && overtimeRate >= 0.0) { "加班信息不能小于 0" }

                AttendanceEntity(
                    id = existingId,
                    projectId = projectId,
                    workerId = worker.id,
                    date = normalizedDate,
                    salaryModeSnapshot = SalaryMode.HOURLY,
                    startTime = start,
                    endTime = end,
                    workHours = AttendanceCalculator.calculateWorkHours(start, end),
                    isPresent = true,
                    attendancePortion = 0.0,
                    hourlyRateSnapshot = normalizedHourlyRate,
                    dailyRateSnapshot = 0.0,
                    overtimeHours = overtimeHours,
                    overtimeRate = overtimeRate,
                    cellColor = cellColor,
                )
            }
            SalaryMode.DAILY -> {
                val normalizedDailyRate = dailyRate ?: worker.dailyRate
                require(normalizedDailyRate >= 0.0) { "日薪不能小于 0" }
                require(overtimeHours >= 0.0 && overtimeRate >= 0.0) { "加班信息不能小于 0" }
                val normalizedPortion = if (isPresent) {
                    when {
                        attendancePortion >= 0.99 -> 1.0
                        attendancePortion > 0.0 -> 0.5
                        else -> 1.0
                    }
                } else {
                    0.0
                }
                AttendanceEntity(
                    id = existingId,
                    projectId = projectId,
                    workerId = worker.id,
                    date = normalizedDate,
                    salaryModeSnapshot = SalaryMode.DAILY,
                    startTime = null,
                    endTime = null,
                    workHours = 0.0,
                    isPresent = isPresent,
                    attendancePortion = normalizedPortion,
                    hourlyRateSnapshot = 0.0,
                    dailyRateSnapshot = normalizedDailyRate,
                    overtimeHours = overtimeHours,
                    overtimeRate = overtimeRate,
                    cellColor = cellColor,
                )
            }
            else -> error("考勤模式不合法")
        }
    }

    private suspend fun rememberWorkerRates(worker: WorkerEntity, hourlyRate: Double, dailyRate: Double) {
        val shouldUpdate = worker.hourlyRate != hourlyRate || worker.dailyRate != dailyRate
        if (!shouldUpdate) return
        workerDao.update(
            worker.copy(
                hourlyRate = hourlyRate,
                dailyRate = dailyRate,
            ),
        )
    }

    private fun normalizeFeeType(type: String): String {
        return type.ifBlank { FeeTypes.LABOR }
    }

    private fun buildFundTotals(records: List<FundRecordEntity>): FundTotals {
        val initial = records.filter { it.type == FundRecordType.INITIAL }.sumOf { it.amount }
        val income = records.filter { it.type == FundRecordType.INCOME }.sumOf { it.amount }
        val expense = records.filter { it.type == FundRecordType.EXPENSE }.sumOf { it.amount }
        return FundTotals(
            initial = initial,
            income = income,
            expense = expense,
            balance = initial + income - expense,
        )
    }

    private fun buildTotals(
        storageRecords: List<StorageRecordEntity>,
        secondarySaleRecords: List<SecondarySaleRecordEntity>,
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
        val totalSecondarySaleWeight = secondarySaleRecords.sumOf { it.weight }
        val totalSecondarySaleAmount = secondarySaleRecords.sumOf { it.totalAmount }

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

        val secondarySaleSummaries = secondarySaleRecords.groupBy { it.name }.map { (name, records) ->
            SecondarySaleSummary(
                name = name,
                totalWeight = records.sumOf { it.weight },
                totalAmount = records.sumOf { it.totalAmount },
                averageUnitPrice = records.sumOf { it.totalAmount } / records.sumOf { it.weight }.coerceAtLeast(1.0),
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
            totalSecondarySaleWeight = totalSecondarySaleWeight,
            totalSecondarySaleAmount = totalSecondarySaleAmount,
            laborFee = laborFee,
            agencyFee = agencyFee,
            loadingFee = loadingFee,
            otherFee = otherFee,
            totalFee = laborFee + agencyFee + loadingFee + otherFee,
            totalPaid = totalPaid,
            totalDebt = totalAmountNeeded - totalPaid,
            itemSummaries = itemSummaries,
            feeSummaries = feeSummaries,
            secondarySaleSummaries = secondarySaleSummaries,
        )
    }

    private fun mergeTotals(totals: List<Totals>): Totals {
        val itemSummaries = totals
            .flatMap { it.itemSummaries }
            .groupBy { it.name }
            .map { (name, items) ->
                ItemSummary(
                    name = name,
                    totalCount = items.sumOf { it.totalCount },
                    totalWeight = items.sumOf { it.totalWeight },
                    totalAmount = items.sumOf { it.totalAmount },
                )
            }
            .sortedByDescending { it.totalAmount }

        val feeSummaries = totals
            .flatMap { it.feeSummaries }
            .groupBy { it.type }
            .map { (type, fees) -> FeeSummary(type, fees.sumOf { it.totalAmount }) }
            .sortedByDescending { it.totalAmount }

        return Totals(
            totalCount = totals.sumOf { it.totalCount },
            totalWeight = totals.sumOf { it.totalWeight },
            totalStorageAmount = totals.sumOf { it.totalStorageAmount },
            totalOutboundCount = totals.sumOf { it.totalOutboundCount },
            totalOutboundWeight = totals.sumOf { it.totalOutboundWeight },
            netTotalCount = totals.sumOf { it.netTotalCount },
            netTotalWeight = totals.sumOf { it.netTotalWeight },
            totalSecondarySaleWeight = totals.sumOf { it.totalSecondarySaleWeight },
            totalSecondarySaleAmount = totals.sumOf { it.totalSecondarySaleAmount },
            laborFee = totals.sumOf { it.laborFee },
            agencyFee = totals.sumOf { it.agencyFee },
            loadingFee = totals.sumOf { it.loadingFee },
            otherFee = totals.sumOf { it.otherFee },
            totalFee = totals.sumOf { it.totalFee },
            totalPaid = totals.sumOf { it.totalPaid },
            totalDebt = totals.sumOf { it.totalDebt },
            itemSummaries = itemSummaries,
            feeSummaries = feeSummaries,
            secondarySaleSummaries = totals
                .flatMap { it.secondarySaleSummaries }
                .groupBy { it.name }
                .map { (name, sales) ->
                    SecondarySaleSummary(
                        name = name,
                        totalWeight = sales.sumOf { it.totalWeight },
                        totalAmount = sales.sumOf { it.totalAmount },
                        averageUnitPrice = sales.sumOf { it.totalAmount } / sales.sumOf { it.totalWeight }.coerceAtLeast(1.0),
                    )
                }
                .sortedByDescending { it.totalAmount },
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
                    fundProjectDao = db.fundProjectDao(),
                    projectGroupDao = db.projectGroupDao(),
                    dateDao = db.recordDateDao(),
                    fundDateDao = db.fundDateDao(),
                    storageDao = db.storageRecordDao(),
                    secondarySaleDao = db.secondarySaleRecordDao(),
                    feeDao = db.feeRecordDao(),
                    dayPhotoDao = db.dayPhotoDao(),
                    outboundDao = db.outboundRecordDao(),
                    paymentDao = db.paymentRecordDao(),
                    fundRecordDao = db.fundRecordDao(),
                    workerDao = db.workerDao(),
                    attendanceDao = db.attendanceDao(),
                ).also { instance = it }
            }
        }
    }
}

object AttendanceCalculator {
    fun calculateWorkHours(startTime: String, endTime: String): Double {
        try {
            val start = LocalTime.parse(startTime.trim())
            val end = LocalTime.parse(endTime.trim())
            require(!end.isBefore(start)) { "结束时间不能早于起始时间" }

            val minutes = Duration.between(start, end).toMinutes()
            return minutes / 60.0
        } catch (e: DateTimeParseException) {
            throw IllegalArgumentException("时间格式必须为 HH:mm", e)
        }
    }
}
