package com.example.budgetai.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "accounts")
data class Account(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: String, // CASH, BANK, WALLET, CREDIT, OTHER
    val openingBalanceMinor: Long = 0L,
    val currency: String = "LKR",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: String, // INCOME, EXPENSE
    val iconName: String,
    val colorHex: String,
    val isDefault: Boolean = false,
    val budgetMinor: Long = 0L
)

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val accountId: String,
    val categoryId: String,
    val type: String, // INCOME, EXPENSE, TRANSFER
    val amountMinor: Long, // 100 minor units = 1.00 (currency safe integer)
    val date: Long, // epoch timestamp
    val description: String,
    val note: String = "",
    val paymentMethod: String = "Cash",
    val isRecurring: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val categoryId: String? = null, // null means overall monthly budget
    val amountMinor: Long,
    val periodType: String = "MONTHLY", // MONTHLY, WEEKLY
    val month: Int, // 1-12
    val year: Int, // e.g. 2026
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "savings_goals")
data class SavingsGoal(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val targetAmountMinor: Long,
    val currentAmountMinor: Long = 0L,
    val targetDate: Long,
    val categoryType: String = "CUSTOM", // EMERGENCY, VEHICLE, HOME, EDUCATION, PHONE, CUSTOM
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "recurring_transactions")
data class RecurringTransaction(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val accountId: String,
    val categoryId: String,
    val type: String, // EXPENSE, INCOME
    val amountMinor: Long,
    val description: String,
    val frequency: String, // DAILY, WEEKLY, MONTHLY, YEARLY
    val nextOccurrence: Long,
    val isActive: Boolean = true
)
