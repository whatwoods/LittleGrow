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
    val avatarPath: String? = null,
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
    val photoPath: String?,
    val note: String?,
    val allergyObservation: AllergyStatus = AllergyStatus.NONE,
    val observationEndDate: LocalDate? = null,
    val caregiver: String? = null,
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
    val sleepType: SleepType = SleepType.NAP,
    val fallingAsleepMethod: FallingAsleepMethod? = null,
    val caregiver: String? = null,
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
    val photoPath: String? = null,
    val caregiver: String? = null,
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
    val photoPath: String?,
    val note: String?,
)

@Entity(
    tableName = "medical_records",
    indices = [Index("happenedAt"), Index("type")],
)
data class MedicalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val happenedAt: LocalDateTime,
    val type: MedicalRecordType,
    val title: String,
    val temperatureC: Float?,
    val dosage: String?,
    val note: String?,
    val caregiver: String? = null,
)

@Entity(
    tableName = "activity_records",
    indices = [Index("happenedAt"), Index("type")],
)
data class ActivityEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val happenedAt: LocalDateTime,
    val type: ActivityType,
    val durationMinutes: Int?,
    val note: String?,
    val caregiver: String? = null,
)

@Entity(
    tableName = "vaccine_records",
    indices = [Index("scheduledDate"), Index("isDone")],
)
data class VaccineEntity(
    @PrimaryKey val scheduleKey: String,
    val vaccineName: String,
    val doseNumber: Int,
    val category: VaccineCategory = VaccineCategory.NATIONAL,
    val scheduledDate: LocalDate,
    val actualDate: LocalDate?,
    val isDone: Boolean,
    val reactionNote: String? = null,
    val hadFever: Boolean = false,
    val reactionSeverity: ReactionSeverity? = null,
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
    fun allergyStatusToString(value: AllergyStatus?): String? = value?.name

    @TypeConverter
    fun stringToAllergyStatus(value: String?): AllergyStatus? = value?.let(AllergyStatus::valueOf)

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

    @TypeConverter
    fun medicalRecordTypeToString(value: MedicalRecordType?): String? = value?.name

    @TypeConverter
    fun stringToMedicalRecordType(value: String?): MedicalRecordType? =
        value?.let(MedicalRecordType::valueOf)

    @TypeConverter
    fun sleepTypeToString(value: SleepType?): String? = value?.name

    @TypeConverter
    fun stringToSleepType(value: String?): SleepType? = value?.let(SleepType::valueOf)

    @TypeConverter
    fun fallingAsleepMethodToString(value: FallingAsleepMethod?): String? = value?.name

    @TypeConverter
    fun stringToFallingAsleepMethod(value: String?): FallingAsleepMethod? =
        value?.let(FallingAsleepMethod::valueOf)

    @TypeConverter
    fun reactionSeverityToString(value: ReactionSeverity?): String? = value?.name

    @TypeConverter
    fun stringToReactionSeverity(value: String?): ReactionSeverity? =
        value?.let(ReactionSeverity::valueOf)

    @TypeConverter
    fun vaccineCategoryToString(value: VaccineCategory?): String? = value?.name

    @TypeConverter
    fun stringToVaccineCategory(value: String?): VaccineCategory? = value?.let(VaccineCategory::valueOf)

    @TypeConverter
    fun activityTypeToString(value: ActivityType?): String? = value?.name

    @TypeConverter
    fun stringToActivityType(value: String?): ActivityType? = value?.let(ActivityType::valueOf)
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

    @Query("SELECT * FROM feeding_records ORDER BY happenedAt DESC")
    suspend fun getAll(): List<FeedingEntity>

    @Query("SELECT * FROM feeding_records WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): FeedingEntity?

    @Upsert
    suspend fun upsert(record: FeedingEntity)

    @Upsert
    suspend fun upsertAll(records: List<FeedingEntity>)

    @Query("DELETE FROM feeding_records WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM feeding_records")
    suspend fun deleteAll()
}

@Dao
interface SleepDao {
    @Query("SELECT * FROM sleep_records ORDER BY startTime DESC")
    fun observeAll(): Flow<List<SleepEntity>>

    @Query("SELECT * FROM sleep_records ORDER BY startTime DESC")
    suspend fun getAll(): List<SleepEntity>

    @Upsert
    suspend fun upsert(record: SleepEntity)

    @Upsert
    suspend fun upsertAll(records: List<SleepEntity>)

    @Query("DELETE FROM sleep_records WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM sleep_records")
    suspend fun deleteAll()
}

@Dao
interface DiaperDao {
    @Query("SELECT * FROM diaper_records ORDER BY happenedAt DESC")
    fun observeAll(): Flow<List<DiaperEntity>>

    @Query("SELECT * FROM diaper_records ORDER BY happenedAt DESC")
    suspend fun getAll(): List<DiaperEntity>

    @Upsert
    suspend fun upsert(record: DiaperEntity)

    @Upsert
    suspend fun upsertAll(records: List<DiaperEntity>)

    @Query("DELETE FROM diaper_records WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM diaper_records")
    suspend fun deleteAll()
}

@Dao
interface GrowthDao {
    @Query("SELECT * FROM growth_records ORDER BY date DESC")
    fun observeAll(): Flow<List<GrowthEntity>>

    @Query("SELECT * FROM growth_records ORDER BY date DESC")
    suspend fun getAll(): List<GrowthEntity>

    @Upsert
    suspend fun upsert(record: GrowthEntity)

    @Upsert
    suspend fun upsertAll(records: List<GrowthEntity>)

    @Query("DELETE FROM growth_records WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM growth_records")
    suspend fun deleteAll()
}

@Dao
interface MilestoneDao {
    @Query("SELECT * FROM milestone_records ORDER BY achievedDate DESC")
    fun observeAll(): Flow<List<MilestoneEntity>>

    @Query("SELECT * FROM milestone_records ORDER BY achievedDate DESC")
    suspend fun getAll(): List<MilestoneEntity>

    @Query("SELECT * FROM milestone_records WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): MilestoneEntity?

    @Upsert
    suspend fun upsert(record: MilestoneEntity)

    @Upsert
    suspend fun upsertAll(records: List<MilestoneEntity>)

    @Query("DELETE FROM milestone_records WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM milestone_records")
    suspend fun deleteAll()
}

@Dao
interface MedicalDao {
    @Query("SELECT * FROM medical_records ORDER BY happenedAt DESC")
    fun observeAll(): Flow<List<MedicalEntity>>

    @Query("SELECT * FROM medical_records ORDER BY happenedAt DESC")
    suspend fun getAll(): List<MedicalEntity>

    @Upsert
    suspend fun upsert(record: MedicalEntity)

    @Upsert
    suspend fun upsertAll(records: List<MedicalEntity>)

    @Query("DELETE FROM medical_records WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM medical_records")
    suspend fun deleteAll()
}

@Dao
interface ActivityDao {
    @Query("SELECT * FROM activity_records ORDER BY happenedAt DESC")
    fun observeAll(): Flow<List<ActivityEntity>>

    @Query("SELECT * FROM activity_records ORDER BY happenedAt DESC")
    suspend fun getAll(): List<ActivityEntity>

    @Upsert
    suspend fun upsert(record: ActivityEntity)

    @Upsert
    suspend fun upsertAll(records: List<ActivityEntity>)

    @Query("DELETE FROM activity_records WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM activity_records")
    suspend fun deleteAll()
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

    @Query(
        """
        UPDATE vaccine_records
        SET reactionNote = :reactionNote,
            hadFever = :hadFever,
            reactionSeverity = :reactionSeverity
        WHERE scheduleKey = :scheduleKey
        """,
    )
    suspend fun updateReaction(
        scheduleKey: String,
        reactionNote: String?,
        hadFever: Boolean,
        reactionSeverity: ReactionSeverity?,
    )

    @Query("DELETE FROM vaccine_records")
    suspend fun deleteAll()
}

@Database(
    entities = [
        BabyEntity::class,
        FeedingEntity::class,
        SleepEntity::class,
        DiaperEntity::class,
        GrowthEntity::class,
        MilestoneEntity::class,
        MedicalEntity::class,
        ActivityEntity::class,
        VaccineEntity::class,
    ],
    version = 5,
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
    abstract fun medicalDao(): MedicalDao
    abstract fun activityDao(): ActivityDao
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
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .build()
                    .also { instance = it }
            }
        }

        fun closeInstance() {
            instance?.close()
            instance = null
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

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE `feeding_records` ADD COLUMN `photoPath` TEXT",
                )
                db.execSQL(
                    "ALTER TABLE `milestone_records` ADD COLUMN `photoPath` TEXT",
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `medical_records` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `happenedAt` TEXT NOT NULL,
                        `type` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `temperatureC` REAL,
                        `dosage` TEXT,
                        `note` TEXT
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_medical_records_happenedAt` ON `medical_records` (`happenedAt`)",
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_medical_records_type` ON `medical_records` (`type`)",
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `activity_records` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `happenedAt` TEXT NOT NULL,
                        `type` TEXT NOT NULL,
                        `durationMinutes` INTEGER,
                        `note` TEXT
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_activity_records_happenedAt` ON `activity_records` (`happenedAt`)",
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_activity_records_type` ON `activity_records` (`type`)",
                )
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE `feeding_records` ADD COLUMN `allergyObservation` TEXT NOT NULL DEFAULT 'NONE'",
                )
                db.execSQL(
                    "ALTER TABLE `feeding_records` ADD COLUMN `observationEndDate` TEXT",
                )
                db.execSQL(
                    "ALTER TABLE `feeding_records` ADD COLUMN `caregiver` TEXT",
                )
                db.execSQL(
                    "ALTER TABLE `sleep_records` ADD COLUMN `sleepType` TEXT NOT NULL DEFAULT 'NAP'",
                )
                db.execSQL(
                    "ALTER TABLE `sleep_records` ADD COLUMN `fallingAsleepMethod` TEXT",
                )
                db.execSQL(
                    "ALTER TABLE `sleep_records` ADD COLUMN `caregiver` TEXT",
                )
                db.execSQL(
                    "ALTER TABLE `diaper_records` ADD COLUMN `photoPath` TEXT",
                )
                db.execSQL(
                    "ALTER TABLE `diaper_records` ADD COLUMN `caregiver` TEXT",
                )
                db.execSQL(
                    "ALTER TABLE `medical_records` ADD COLUMN `caregiver` TEXT",
                )
                db.execSQL(
                    "ALTER TABLE `activity_records` ADD COLUMN `caregiver` TEXT",
                )
                db.execSQL(
                    "ALTER TABLE `vaccine_records` ADD COLUMN `category` TEXT NOT NULL DEFAULT 'NATIONAL'",
                )
                db.execSQL(
                    "ALTER TABLE `vaccine_records` ADD COLUMN `reactionNote` TEXT",
                )
                db.execSQL(
                    "ALTER TABLE `vaccine_records` ADD COLUMN `hadFever` INTEGER NOT NULL DEFAULT 0",
                )
                db.execSQL(
                    "ALTER TABLE `vaccine_records` ADD COLUMN `reactionSeverity` TEXT",
                )
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE `baby_profile` ADD COLUMN `avatarPath` TEXT",
                )
            }
        }
    }
}
