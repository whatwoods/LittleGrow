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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class AppDatabaseMigrationTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val databaseName = "migration-test.db"

    @Before
    fun setUp() {
        context.deleteDatabase(databaseName)
    }

    @After
    fun tearDown() {
        context.deleteDatabase(databaseName)
    }

    @Test
    fun migrateFrom1To2_preservesExistingDataAndAddsVaccineTable() = runBlocking {
        createVersion1Database()

        val migrated = Room.databaseBuilder(context, AppDatabase::class.java, databaseName)
            .addMigrations(AppDatabase.MIGRATION_1_2)
            .build()

        try {
            val profile = migrated.babyDao().observeProfile().first()
            val feedings = migrated.feedingDao().observeAll().first()
            val sleeps = migrated.sleepDao().observeAll().first()
            val diapers = migrated.diaperDao().observeAll().first()
            val growthRecords = migrated.growthDao().observeAll().first()
            val milestones = migrated.milestoneDao().observeAll().first()
            val vaccines = migrated.vaccineDao().getAll()

            assertNotNull(profile)
            assertEquals("测试宝宝", profile?.name)
            assertEquals(1, feedings.size)
            assertEquals(1, sleeps.size)
            assertEquals(1, diapers.size)
            assertEquals(1, growthRecords.size)
            assertEquals(1, milestones.size)
            assertEquals(0, vaccines.size)
            assertEquals(6.5f, growthRecords.single().weightKg ?: -1f, 0f)
            assertTrue(feedings.single().foodName == null)
        } finally {
            migrated.close()
        }
    }

    private fun createVersion1Database() {
        val helper = FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(databaseName)
                .callback(
                    object : SupportSQLiteOpenHelper.Callback(1) {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            createVersion1Schema(db)
                            seedVersion1Data(db)
                        }

                        override fun onUpgrade(
                            db: SupportSQLiteDatabase,
                            oldVersion: Int,
                            newVersion: Int,
                        ) = Unit

                        override fun onDowngrade(
                            db: SupportSQLiteDatabase,
                            oldVersion: Int,
                            newVersion: Int,
                        ) = Unit

                        override fun onOpen(db: SupportSQLiteDatabase) = Unit

                        override fun onCorruption(db: SupportSQLiteDatabase) {
                            throw IOException("version 1 test database corrupted")
                        }
                    },
                )
                .build(),
        )

        val db = helper.writableDatabase
        db.close()
        helper.close()
    }

    private fun createVersion1Schema(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `baby_profile` (`id` INTEGER NOT NULL, `name` TEXT NOT NULL, `birthday` TEXT NOT NULL, `gender` TEXT NOT NULL, PRIMARY KEY(`id`))",
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `feeding_records` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `type` TEXT NOT NULL, `happenedAt` TEXT NOT NULL, `durationMinutes` INTEGER, `amountMl` INTEGER, `foodName` TEXT, `note` TEXT)",
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_feeding_records_happenedAt` ON `feeding_records` (`happenedAt`)",
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `sleep_records` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `startTime` TEXT NOT NULL, `endTime` TEXT NOT NULL, `note` TEXT)",
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_sleep_records_startTime` ON `sleep_records` (`startTime`)",
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_sleep_records_endTime` ON `sleep_records` (`endTime`)",
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `diaper_records` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `happenedAt` TEXT NOT NULL, `type` TEXT NOT NULL, `poopColor` TEXT, `poopTexture` TEXT, `note` TEXT)",
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_diaper_records_happenedAt` ON `diaper_records` (`happenedAt`)",
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `growth_records` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `date` TEXT NOT NULL, `weightKg` REAL, `heightCm` REAL, `headCircCm` REAL)",
        )
        db.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_growth_records_date` ON `growth_records` (`date`)",
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `milestone_records` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `category` TEXT NOT NULL, `achievedDate` TEXT NOT NULL, `note` TEXT)",
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_milestone_records_achievedDate` ON `milestone_records` (`achievedDate`)",
        )
    }

    private fun seedVersion1Data(db: SupportSQLiteDatabase) {
        db.execSQL(
            "INSERT INTO `baby_profile` (`id`, `name`, `birthday`, `gender`) VALUES (1, '测试宝宝', '2025-12-01', 'GIRL')",
        )
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
}
