package com.example.budgetai.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.budgetai.data.model.Account
import com.example.budgetai.data.model.Budget
import com.example.budgetai.data.model.Category
import com.example.budgetai.data.model.RecurringTransaction
import com.example.budgetai.data.model.SavingsGoal
import com.example.budgetai.data.model.Transaction

@Database(
    entities = [
        Account::class,
        Category::class,
        Transaction::class,
        Budget::class,
        SavingsGoal::class,
        RecurringTransaction::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun savingsGoalDao(): SavingsGoalDao
    abstract fun recurringDao(): RecurringDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "budget_ai_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
