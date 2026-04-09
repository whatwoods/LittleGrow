package com.littlegrow.app.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.Upsert
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(tableName = "baby_profile")
data class BabyEntity(
    @PrimaryKey val id: Long = 1,
    val name: String,
    val birthday: LocalDate,
    val gender: Gender,
)

@Entity(
    tableName = "feeding_records",
    indices = [Index("happenedAt")],
)
data class FeedingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: FeedingType,
    val happenedAt: LocalDateTime,
    val durationMinutes: Int?,
    val amountMl: Int?,
    val foodName: String?,
    val note: String?,
)

@Entity(
    tableName = "sleep_records",
    indices = [Index("startTime"), Index("endTime")],
)
data class SleepEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val note: String?,
)

@Entity(
    tableName = "diaper_records",
    indices = [Index("happenedAt")],
)
data class DiaperEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val happenedAt: LocalDateTime,
    val type: DiaperType,
    val poopColor: PoopColor?,
    val poopTexture: PoopTexture?,
    val note: String?,
)

@Entity(
    tableName = "growth_records",
    indices = [Index(value = ["date"], unique = true)],
)
data class GrowthEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: LocalDate,
    val weightKg: Float?,
    val heightCm: Float?,
    val headCircCm: Float?,
)

@Entity(
    tableName = "milestone_records",
    indices = [Index("achievedDate")],
)
data class MilestoneEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: MilestoneCategory,
    val achievedDate: LocalDate,
    val note: String?,
)

@Entity(
    tableName = "vaccine_records",
    indices = [Index("scheduledDate"), Index("isDone")],
)
data class VaccineEntity(
    @PrimaryKey val scheduleKey: String,
    val vaccineName: String,
    val doseNumber: Int,
    val scheduledDate: LocalDate,
    val actualDate: LocalDate?,
    val isDone: Boolean,
)

class LittleGrowConverters {
    @TypeConverter
    fun localDateToString(value: LocalDate?): String? = value?.toString()

    @TypeConverter
    fun stringToLocalDate(value: String?): LocalDate? = value?.let(LocalDate::parse)

    @TypeConverter
    fun localDateTimeToString(value: LocalDateTime?): String? = value?.toString()

    @TypeConverter
    fun stringToLocalDateTime(value: String?): LocalDateTime? = value?.let(LocalDateTime::parse)

    @TypeConverter
    fun genderToString(value: Gender?): String? = value?.name

    @TypeConverter
    fun stringToGender(value: String?): Gender? = value?.let(Gender::valueOf)

    @TypeConverter
    fun feedingTypeToString(value: FeedingType?): String? = value?.name

    @TypeConverter
    fun stringToFeedingType(value: String?): FeedingType? = value?.let(FeedingType::valueOf)

    @TypeConverter
    fun diaperTypeToString(value: DiaperType?): String? = value?.name

    @TypeConverter
    fun stringToDiaperType(value: String?): DiaperType? = value?.let(DiaperType::valueOf)

    @TypeConverter
    fun poopColorToString(value: PoopColor?): String? = value?.name

    @TypeConverter
    fun stringToPoopColor(value: String?): PoopColor? = value?.let(PoopColor::valueOf)

    @TypeConverter
    fun poopTextureToString(value: PoopTexture?): String? = value?.name

    @TypeConverter
    fun stringToPoopTexture(value: String?): PoopTexture? = value?.let(PoopTexture::valueOf)

    @TypeConverter
    fun milestoneCategoryToString(value: MilestoneCategory?): String? = value?.name

    @TypeConverter
    fun stringToMilestoneCategory(value: String?): MilestoneCategory? =
        value?.let(MilestoneCategory::valueOf)
}

@Dao
interface BabyDao {
    @Query("SELECT * FROM baby_profile WHERE id = 1")
    fun observeProfile(): Flow<BabyEntity?>

    @Upsert
    suspend fun upsertProfile(profile: BabyEntity)
}

@Dao
interface FeedingDao {
    @Query("SELECT * FROM feeding_records ORDER BY happenedAt DESC")
    fun observeAll(): Flow<List<FeedingEntity>>

    @Upsert
    suspend fun upsert(record: FeedingEntity)

    @Query("DELETE FROM feeding_records WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface SleepDao {
    @Query("SELECT * FROM sleep_records ORDER BY startTime DESC")
    fun observeAll(): Flow<List<SleepEntity>>

    @Upsert
    suspend fun upsert(record: SleepEntity)

    @Query("DELETE FROM sleep_records WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface DiaperDao {
    @Query("SELECT * FROM diaper_records ORDER BY happenedAt DESC")
    fun observeAll(): Flow<List<DiaperEntity>>

    @Upsert
    suspend fun upsert(record: DiaperEntity)

    @Query("DELETE FROM diaper_records WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface GrowthDao {
    @Query("SELECT * FROM growth_records ORDER BY date DESC")
    fun observeAll(): Flow<List<GrowthEntity>>

    @Upsert
    suspend fun upsert(record: GrowthEntity)

    @Query("DELETE FROM growth_records WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface MilestoneDao {
    @Query("SELECT * FROM milestone_records ORDER BY achievedDate DESC")
    fun observeAll(): Flow<List<MilestoneEntity>>

    @Upsert
    suspend fun upsert(record: MilestoneEntity)

    @Query("DELETE FROM milestone_records WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface VaccineDao {
    @Query("SELECT * FROM vaccine_records ORDER BY scheduledDate ASC")
    fun observeAll(): Flow<List<VaccineEntity>>

    @Query("SELECT * FROM vaccine_records ORDER BY scheduledDate ASC")
    suspend fun getAll(): List<VaccineEntity>

    @Query("SELECT * FROM vaccine_records WHERE scheduleKey = :scheduleKey LIMIT 1")
    suspend fun getByKey(scheduleKey: String): VaccineEntity?

    @Upsert
    suspend fun upsertAll(records: List<VaccineEntity>)

    @Query(
        "UPDATE vaccine_records SET isDone = :isDone, actualDate = :actualDate WHERE scheduleKey = :scheduleKey",
    )
    suspend fun updateStatus(
        scheduleKey: String,
        isDone: Boolean,
        actualDate: LocalDate?,
    )
}

@Database(
    entities = [
        BabyEntity::class,
        FeedingEntity::class,
        SleepEntity::class,
        DiaperEntity::class,
        GrowthEntity::class,
        MilestoneEntity::class,
        VaccineEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
@TypeConverters(LittleGrowConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun babyDao(): BabyDao
    abstract fun feedingDao(): FeedingDao
    abstract fun sleepDao(): SleepDao
    abstract fun diaperDao(): DiaperDao
    abstract fun growthDao(): GrowthDao
    abstract fun milestoneDao(): MilestoneDao
    abstract fun vaccineDao(): VaccineDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "little_grow.db",
                )
                    .setJournalMode(JournalMode.WRITE_AHEAD_LOGGING)
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { instance = it }
            }
        }

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `vaccine_records` (
                        `scheduleKey` TEXT NOT NULL,
                        `vaccineName` TEXT NOT NULL,
                        `doseNumber` INTEGER NOT NULL,
                        `scheduledDate` TEXT NOT NULL,
                        `actualDate` TEXT,
                        `isDone` INTEGER NOT NULL,
                        PRIMARY KEY(`scheduleKey`)
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_vaccine_records_scheduledDate` ON `vaccine_records` (`scheduledDate`)",
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_vaccine_records_isDone` ON `vaccine_records` (`isDone`)",
                )
            }
        }
    }
}
