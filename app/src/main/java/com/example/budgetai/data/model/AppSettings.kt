package com.example.budgetai.data.model

data class AppSettings(
    val language: String = "en",
    val currency: String = "LKR",
    val themeMode: String = "SYSTEM", // SYSTEM, LIGHT, DARK
    val isPinEnabled: Boolean = false,
    val pinHash: String = "",
    val autoLockTimeoutMinutes: Int = 5,
    val hideAmounts: Boolean = false,
    val isOnboardingCompleted: Boolean = false
)

data class BackupPayload(
    val app: String = "BUDGET_AI",
    val version: Int = 1,
    val exportedAt: Long = System.currentTimeMillis(),
    val attribution: String = "Created by Ajith Bandara",
    val accounts: List<Account> = emptyList(),
    val categories: List<Category> = emptyList(),
    val transactions: List<Transaction> = emptyList(),
    val budgets: List<Budget> = emptyList(),
    val goals: List<SavingsGoal> = emptyList(),
    val recurring: List<RecurringTransaction> = emptyList(),
    val settings: AppSettings = AppSettings()
)
