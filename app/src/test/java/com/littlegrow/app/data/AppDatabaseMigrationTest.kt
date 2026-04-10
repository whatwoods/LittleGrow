package com.littlegrow.app.data

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import java.io.IOException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class AppDatabaseMigrationTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val databaseName = "migration-test.db"

    @After
    fun tearDown() {
        context.deleteDatabase(databaseName)
    }

    @Test
    fun migrateFrom1To5_preservesExistingDataAndAddsNewStructures() = runBlocking {
        createDatabase(version = 1) { db ->
            createVersion1Schema(db)
            seedVersion1Data(db)
        }

        val migrated = buildMigratedDatabase()

        try {
            val profile = migrated.babyDao().observeProfile().first()
            val feedings = migrated.feedingDao().observeAll().first()
            val milestones = migrated.milestoneDao().observeAll().first()
            val medical = migrated.medicalDao().observeAll().first()
            val activities = migrated.activityDao().observeAll().first()
            val sleeps = migrated.sleepDao().observeAll().first()

            assertNotNull(profile)
            assertEquals("测试宝宝", profile?.name)
            assertEquals(null, profile?.avatarPath)
            assertEquals(1, feedings.size)
            assertEquals(null, feedings.single().photoPath)
            assertEquals(AllergyStatus.NONE, feedings.single().allergyObservation)
            assertEquals(null, feedings.single().observationEndDate)
            assertEquals(null, feedings.single().caregiver)
            assertEquals(1, milestones.size)
            assertEquals(null, milestones.single().photoPath)
            assertEquals(0, medical.size)
            assertEquals(0, activities.size)
            assertEquals(SleepType.NAP, sleeps.single().sleepType)
            assertEquals(null, sleeps.single().fallingAsleepMethod)
        } finally {
            migrated.close()
        }
    }

    @Test
    fun migrateFrom2To5_preservesVaccines() = runBlocking {
        createDatabase(version = 2) { db ->
            createVersion2Schema(db)
            seedVersion1Data(db)
            db.execSQL(
                "INSERT INTO `vaccine_records` (`scheduleKey`, `vaccineName`, `doseNumber`, `scheduledDate`, `actualDate`, `isDone`) VALUES ('hepb_2', '乙肝疫苗', 2, '2026-01-01', '2026-01-02', 1)",
            )
        }

        val migrated = buildMigratedDatabase()

        try {
            val vaccines = migrated.vaccineDao().getAll()
            val feedings = migrated.feedingDao().observeAll().first()
            val milestones = migrated.milestoneDao().observeAll().first()

            assertEquals(1, vaccines.size)
            assertEquals("乙肝疫苗", vaccines.single().vaccineName)
            assertEquals(VaccineCategory.NATIONAL, vaccines.single().category)
            assertEquals(false, vaccines.single().hadFever)
            assertEquals(null, vaccines.single().reactionSeverity)
            assertEquals(null, migrated.babyDao().observeProfile().first()?.avatarPath)
            assertEquals(null, feedings.single().photoPath)
            assertEquals(null, milestones.single().photoPath)
        } finally {
            migrated.close()
        }
    }

    @Test
    fun migrateFrom3To5_addsNewColumnsWithDefaults() = runBlocking {
        createDatabase(version = 3) { db ->
            createVersion3Schema(db)
            seedVersion3Data(db)
        }

        val migrated = buildMigratedDatabase()

        try {
            val feedings = migrated.feedingDao().observeAll().first()
            val diapers = migrated.diaperDao().observeAll().first()
            val vaccines = migrated.vaccineDao().getAll()
            val profile = migrated.babyDao().observeProfile().first()

            assertEquals(AllergyStatus.NONE, feedings.single().allergyObservation)
            assertEquals(null, diapers.single().photoPath)
            assertEquals(VaccineCategory.NATIONAL, vaccines.single().category)
            assertEquals(null, profile?.avatarPath)
        } finally {
            migrated.close()
        }
    }

    @Test
    fun migrateFrom4To5_addsAvatarPathDefault() = runBlocking {
        createDatabase(version = 4) { db ->
            createVersion4Schema(db)
            seedVersion4Data(db)
        }

        val migrated = buildMigratedDatabase()

        try {
            val profile = migrated.babyDao().observeProfile().first()
            assertEquals("测试宝宝", profile?.name)
            assertEquals(null, profile?.avatarPath)
        } finally {
            migrated.close()
        }
    }

    private fun buildMigratedDatabase(): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, databaseName)
            .addMigrations(
                AppDatabase.MIGRATION_1_2,
                AppDatabase.MIGRATION_2_3,
                AppDatabase.MIGRATION_3_4,
                AppDatabase.MIGRATION_4_5,
            )
            .build()
    }

    private fun createDatabase(
        version: Int,
        onCreate: (SupportSQLiteDatabase) -> Unit,
    ) {
        context.deleteDatabase(databaseName)
        val helper = FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(databaseName)
                .callback(
                    object : SupportSQLiteOpenHelper.Callback(version) {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            onCreate(db)
                        }

                        override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit

                        override fun onDowngrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit

                        override fun onOpen(db: SupportSQLiteDatabase) = Unit

                        override fun onCorruption(db: SupportSQLiteDatabase) {
                            throw IOException("test database corrupted")
                        }
                    },
                )
                .build(),
        )
        helper.writableDatabase.close()
        helper.close()
    }

    private fun createVersion1Schema(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `baby_profile` (`id` INTEGER NOT NULL, `name` TEXT NOT NULL, `birthday` TEXT NOT NULL, `gender` TEXT NOT NULL, PRIMARY KEY(`id`))",
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `feeding_records` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `type` TEXT NOT NULL, `happenedAt` TEXT NOT NULL, `durationMinutes` INTEGER, `amountMl` INTEGER, `foodName` TEXT, `note` TEXT)",
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_feeding_records_happenedAt` ON `feeding_records` (`happenedAt`)")
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `sleep_records` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `startTime` TEXT NOT NULL, `endTime` TEXT NOT NULL, `note` TEXT)",
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_sleep_records_startTime` ON `sleep_records` (`startTime`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_sleep_records_endTime` ON `sleep_records` (`endTime`)")
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `diaper_records` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `happenedAt` TEXT NOT NULL, `type` TEXT NOT NULL, `poopColor` TEXT, `poopTexture` TEXT, `note` TEXT)",
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_diaper_records_happenedAt` ON `diaper_records` (`happenedAt`)")
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `growth_records` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `date` TEXT NOT NULL, `weightKg` REAL, `heightCm` REAL, `headCircCm` REAL)",
        )
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_growth_records_date` ON `growth_records` (`date`)")
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `milestone_records` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `category` TEXT NOT NULL, `achievedDate` TEXT NOT NULL, `note` TEXT)",
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_milestone_records_achievedDate` ON `milestone_records` (`achievedDate`)")
    }

    private fun createVersion2Schema(db: SupportSQLiteDatabase) {
        createVersion1Schema(db)
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `vaccine_records` (`scheduleKey` TEXT NOT NULL, `vaccineName` TEXT NOT NULL, `doseNumber` INTEGER NOT NULL, `scheduledDate` TEXT NOT NULL, `actualDate` TEXT, `isDone` INTEGER NOT NULL, PRIMARY KEY(`scheduleKey`))",
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_vaccine_records_scheduledDate` ON `vaccine_records` (`scheduledDate`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_vaccine_records_isDone` ON `vaccine_records` (`isDone`)")
    }

    private fun createVersion3Schema(db: SupportSQLiteDatabase) {
        createVersion2Schema(db)
        db.execSQL("ALTER TABLE `feeding_records` ADD COLUMN `photoPath` TEXT")
        db.execSQL("ALTER TABLE `milestone_records` ADD COLUMN `photoPath` TEXT")
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `medical_records` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `happenedAt` TEXT NOT NULL, `type` TEXT NOT NULL, `title` TEXT NOT NULL, `temperatureC` REAL, `dosage` TEXT, `note` TEXT)",
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_medical_records_happenedAt` ON `medical_records` (`happenedAt`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_medical_records_type` ON `medical_records` (`type`)")
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `activity_records` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `happenedAt` TEXT NOT NULL, `type` TEXT NOT NULL, `durationMinutes` INTEGER, `note` TEXT)",
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_activity_records_happenedAt` ON `activity_records` (`happenedAt`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_activity_records_type` ON `activity_records` (`type`)")
    }

    private fun createVersion4Schema(db: SupportSQLiteDatabase) {
        createVersion3Schema(db)
        db.execSQL("ALTER TABLE `feeding_records` ADD COLUMN `allergyObservation` TEXT NOT NULL DEFAULT 'NONE'")
        db.execSQL("ALTER TABLE `feeding_records` ADD COLUMN `observationEndDate` TEXT")
        db.execSQL("ALTER TABLE `feeding_records` ADD COLUMN `caregiver` TEXT")
        db.execSQL("ALTER TABLE `sleep_records` ADD COLUMN `sleepType` TEXT NOT NULL DEFAULT 'NAP'")
        db.execSQL("ALTER TABLE `sleep_records` ADD COLUMN `fallingAsleepMethod` TEXT")
        db.execSQL("ALTER TABLE `sleep_records` ADD COLUMN `caregiver` TEXT")
        db.execSQL("ALTER TABLE `diaper_records` ADD COLUMN `photoPath` TEXT")
        db.execSQL("ALTER TABLE `diaper_records` ADD COLUMN `caregiver` TEXT")
        db.execSQL("ALTER TABLE `medical_records` ADD COLUMN `caregiver` TEXT")
        db.execSQL("ALTER TABLE `activity_records` ADD COLUMN `caregiver` TEXT")
        db.execSQL("ALTER TABLE `vaccine_records` ADD COLUMN `category` TEXT NOT NULL DEFAULT 'NATIONAL'")
        db.execSQL("ALTER TABLE `vaccine_records` ADD COLUMN `reactionNote` TEXT")
        db.execSQL("ALTER TABLE `vaccine_records` ADD COLUMN `hadFever` INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE `vaccine_records` ADD COLUMN `reactionSeverity` TEXT")
    }

    private fun seedVersion1Data(db: SupportSQLiteDatabase) {
        db.execSQL("INSERT INTO `baby_profile` (`id`, `name`, `birthday`, `gender`) VALUES (1, '测试宝宝', '2025-12-01', 'GIRL')")
        db.execSQL(
            "INSERT INTO `feeding_records` (`id`, `type`, `happenedAt`, `durationMinutes`, `amountMl`, `foodName`, `note`) VALUES (1, 'BREAST_LEFT', '2026-04-09T08:00', 12, NULL, NULL, '夜奶')",
        )
        db.execSQL(
            "INSERT INTO `sleep_records` (`id`, `startTime`, `endTime`, `note`) VALUES (1, '2026-04-08T22:00', '2026-04-09T05:30', '整夜睡')",
        )
        db.execSQL(
            "INSERT INTO `diaper_records` (`id`, `happenedAt`, `type`, `poopColor`, `poopTexture`, `note`) VALUES (1, '2026-04-09T07:40', 'POOP', 'YELLOW', 'SOFT', '正常')",
        )
        db.execSQL(
            "INSERT INTO `growth_records` (`id`, `date`, `weightKg`, `heightCm`, `headCircCm`) VALUES (1, '2026-04-01', 6.5, 63.2, 40.8)",
        )
        db.execSQL(
            "INSERT INTO `milestone_records` (`id`, `title`, `category`, `achievedDate`, `note`) VALUES (1, '会翻身', 'GROSS_MOTOR', '2026-04-05', '从俯卧翻到仰卧')",
        )
    }

    private fun seedVersion3Data(db: SupportSQLiteDatabase) {
        seedVersion1Data(db)
        db.execSQL("UPDATE `feeding_records` SET `photoPath` = NULL WHERE `id` = 1")
        db.execSQL("UPDATE `milestone_records` SET `photoPath` = NULL WHERE `id` = 1")
        db.execSQL(
            "INSERT INTO `vaccine_records` (`scheduleKey`, `vaccineName`, `doseNumber`, `scheduledDate`, `actualDate`, `isDone`) VALUES ('dtap_1', '百白破疫苗', 1, '2026-03-01', NULL, 0)",
        )
    }

    private fun seedVersion4Data(db: SupportSQLiteDatabase) {
        seedVersion3Data(db)
        db.execSQL("UPDATE `feeding_records` SET `allergyObservation` = 'NONE', `observationEndDate` = NULL, `caregiver` = NULL WHERE `id` = 1")
        db.execSQL("UPDATE `sleep_records` SET `sleepType` = 'NAP', `fallingAsleepMethod` = NULL, `caregiver` = NULL WHERE `id` = 1")
        db.execSQL("UPDATE `diaper_records` SET `photoPath` = NULL, `caregiver` = NULL WHERE `id` = 1")
        db.execSQL("UPDATE `medical_records` SET `caregiver` = NULL")
        db.execSQL("UPDATE `activity_records` SET `caregiver` = NULL")
        db.execSQL("UPDATE `vaccine_records` SET `category` = 'NATIONAL', `reactionNote` = NULL, `hadFever` = 0, `reactionSeverity` = NULL")
    }
}
