package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [TransactionEntity::class, CnfOfferEntity::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun cnfOfferDao(): CnfOfferDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `cnf_offers` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `offerName` TEXT NOT NULL,
                        `serialNumber` TEXT NOT NULL,
                        `mediatorName` TEXT NOT NULL,
                        `customerName` TEXT NOT NULL,
                        `route` TEXT NOT NULL,
                        `offerDateMillis` INTEGER NOT NULL,
                        `calcPriceUsd` INTEGER NOT NULL,
                        `offerPriceUsd` INTEGER NOT NULL,
                        `costPriceUsd` INTEGER NOT NULL,
                        `profitLossUsd` INTEGER NOT NULL,
                        `unitPriceUsd` INTEGER NOT NULL,
                        `freightCostUsd` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `cnf_offers_v3` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `offerName` TEXT NOT NULL,
                        `serialNumber` TEXT NOT NULL,
                        `mediatorName` TEXT NOT NULL,
                        `customerName` TEXT NOT NULL,
                        `route` TEXT NOT NULL,
                        `offerDateMillis` INTEGER NOT NULL,
                        `exchangeRateIdr` INTEGER NOT NULL,
                        `dealPriceUsd` INTEGER NOT NULL,
                        `unitPriceSellIdr` INTEGER NOT NULL,
                        `unitPriceCostIdr` INTEGER NOT NULL,
                        `mobilisationSellIdr` INTEGER NOT NULL,
                        `mobilisationCostIdr` INTEGER NOT NULL,
                        `stuffingSellIdr` INTEGER NOT NULL,
                        `stuffingCostIdr` INTEGER NOT NULL,
                        `freightSellIdr` INTEGER NOT NULL,
                        `freightCostIdr` INTEGER NOT NULL,
                        `successFeeSellIdr` INTEGER NOT NULL,
                        `othersSellIdr` INTEGER NOT NULL,
                        `othersCostIdr` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO `cnf_offers_v3` (
                        `id`, `offerName`, `serialNumber`, `mediatorName`, `customerName`, `route`,
                        `offerDateMillis`, `exchangeRateIdr`, `dealPriceUsd`,
                        `unitPriceSellIdr`, `unitPriceCostIdr`,
                        `mobilisationSellIdr`, `mobilisationCostIdr`,
                        `stuffingSellIdr`, `stuffingCostIdr`,
                        `freightSellIdr`, `freightCostIdr`,
                        `successFeeSellIdr`, `othersSellIdr`, `othersCostIdr`, `createdAt`
                    )
                    SELECT
                        `id`, `offerName`, `serialNumber`, `mediatorName`, `customerName`, `route`,
                        `offerDateMillis`, 16000, `offerPriceUsd`,
                        `unitPriceUsd` * 16000, `unitPriceUsd` * 16000,
                        0, 0, 0, 0,
                        `freightCostUsd` * 16000, `freightCostUsd` * 16000,
                        0, 0, 0, `createdAt`
                    FROM `cnf_offers`
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE `cnf_offers`")
                db.execSQL("ALTER TABLE `cnf_offers_v3` RENAME TO `cnf_offers`")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `cnf_offers_v4` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `offerName` TEXT NOT NULL,
                        `serialNumber` TEXT NOT NULL,
                        `mediatorName` TEXT NOT NULL,
                        `customerName` TEXT NOT NULL,
                        `route` TEXT NOT NULL,
                        `offerDateMillis` INTEGER NOT NULL,
                        `exchangeRateIdr` INTEGER NOT NULL,
                        `spreadIdr` INTEGER NOT NULL,
                        `unitCount` INTEGER NOT NULL,
                        `unit1Idr` INTEGER NOT NULL,
                        `unit2Idr` INTEGER NOT NULL,
                        `unit3Idr` INTEGER NOT NULL,
                        `unit4Idr` INTEGER NOT NULL,
                        `unitPriceCostIdr` INTEGER NOT NULL,
                        `mob1Idr` INTEGER NOT NULL,
                        `mob2Idr` INTEGER NOT NULL,
                        `mob3Idr` INTEGER NOT NULL,
                        `mobilisationManualIdr` INTEGER NOT NULL,
                        `mobilisationCostIdr` INTEGER NOT NULL,
                        `stuffingSellIdr` INTEGER NOT NULL,
                        `stuffingCostIdr` INTEGER NOT NULL,
                        `freightContainersSpec` TEXT NOT NULL,
                        `freightManualIdr` INTEGER NOT NULL,
                        `freightCostIdr` INTEGER NOT NULL,
                        `successFeeMultiplier` INTEGER NOT NULL,
                        `othersMultiplier` INTEGER NOT NULL,
                        `othersCostIdr` INTEGER NOT NULL,
                        `uangMasukIdr` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO `cnf_offers_v4` (
                        `id`, `offerName`, `serialNumber`, `mediatorName`, `customerName`, `route`,
                        `offerDateMillis`, `exchangeRateIdr`, `spreadIdr`,
                        `unitCount`, `unit1Idr`, `unit2Idr`, `unit3Idr`, `unit4Idr`, `unitPriceCostIdr`,
                        `mob1Idr`, `mob2Idr`, `mob3Idr`, `mobilisationManualIdr`, `mobilisationCostIdr`,
                        `stuffingSellIdr`, `stuffingCostIdr`,
                        `freightContainersSpec`, `freightManualIdr`, `freightCostIdr`,
                        `successFeeMultiplier`, `othersMultiplier`, `othersCostIdr`,
                        `uangMasukIdr`, `createdAt`
                    )
                    SELECT
                        `id`, `offerName`, `serialNumber`, `mediatorName`, `customerName`, `route`,
                        `offerDateMillis`, `exchangeRateIdr`, 0,
                        1, `unitPriceSellIdr`, 0, 0, 0, `unitPriceCostIdr`,
                        0, 0, 0, `mobilisationSellIdr`, `mobilisationCostIdr`,
                        `stuffingSellIdr`, `stuffingCostIdr`,
                        '40''HC|0|1', `freightSellIdr`, `freightCostIdr`,
                        CAST((`successFeeSellIdr` / (1000 * CASE WHEN `exchangeRateIdr` > 0 THEN `exchangeRateIdr` ELSE 1 END)) AS INTEGER),
                        CAST((`othersSellIdr` / (1000 * CASE WHEN `exchangeRateIdr` > 0 THEN `exchangeRateIdr` ELSE 1 END)) AS INTEGER),
                        `othersCostIdr`,
                        `dealPriceUsd` * `exchangeRateIdr`, `createdAt`
                    FROM `cnf_offers`
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE `cnf_offers`")
                db.execSQL("ALTER TABLE `cnf_offers_v4` RENAME TO `cnf_offers`")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "hani_jaya_finance.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
