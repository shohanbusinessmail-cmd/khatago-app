package com.shohan.khatago.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [FinancialRecordEntity::class, PaymentEntity::class], version = 1, exportSchema = false)
abstract class FinanceDatabase : RoomDatabase() {
    abstract fun financeDao(): FinanceDao

    companion object {
        @Volatile private var INSTANCE: FinanceDatabase? = null
        fun get(context: Context): FinanceDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(context, FinanceDatabase::class.java, "khatago.db")
                // No destructive fallback: a future schema change must ship an explicit migration.
                .build()
                .also { INSTANCE = it }
        }
    }
}
