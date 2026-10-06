package com.example.budgetai.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.budgetai.data.local.AppDatabase
import com.example.budgetai.data.model.Account
import com.example.budgetai.data.model.AppSettings
import com.example.budgetai.data.model.BackupPayload
import com.example.budgetai.data.model.Budget
import com.example.budgetai.data.model.Category
import com.example.budgetai.data.model.RecurringTransaction
import com.example.budgetai.data.model.SavingsGoal
import com.example.budgetai.data.model.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import java.util.Calendar
import java.util.UUID

class BudgetRepository(private val context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val prefs: SharedPreferences = context.getSharedPreferences("budget_ai_prefs", Context.MODE_PRIVATE)

    val allTransactions: Flow<List<Transaction>> = db.transactionDao().getAllTransactions()
    val allAccounts: Flow<List<Account>> = db.accountDao().getAllAccounts()
    val allCategories: Flow<List<Category>> = db.categoryDao().getAllCategories()
    val allBudgets: Flow<List<Budget>> = db.budgetDao().getAllBudgets()
    val allGoals: Flow<List<SavingsGoal>> = db.savingsGoalDao().getAllGoals()
    val allRecurring: Flow<List<RecurringTransaction>> = db.recurringDao().getAllRecurring()

    private val _settingsState = MutableStateFlow(loadSettings())
    val settingsState = _settingsState.asStateFlow()

    private fun loadSettings(): AppSettings {
        return AppSettings(
            language = prefs.getString("language", "en") ?: "en",
            currency = prefs.getString("currency", "LKR") ?: "LKR",
            themeMode = prefs.getString("themeMode", "SYSTEM") ?: "SYSTEM",
            isPinEnabled = prefs.getBoolean("isPinEnabled", false),
            pinHash = prefs.getString("pinHash", "") ?: "",
            autoLockTimeoutMinutes = prefs.getInt("autoLockTimeoutMinutes", 5),
            hideAmounts = prefs.getBoolean("hideAmounts", false),
            isOnboardingCompleted = prefs.getBoolean("isOnboardingCompleted", false)
        )
    }

    fun updateSettings(newSettings: AppSettings) {
        prefs.edit().apply {
            putString("language", newSettings.language)
            putString("currency", newSettings.currency)
            putString("themeMode", newSettings.themeMode)
            putBoolean("isPinEnabled", newSettings.isPinEnabled)
            putString("pinHash", newSettings.pinHash)
            putInt("autoLockTimeoutMinutes", newSettings.autoLockTimeoutMinutes)
            putBoolean("hideAmounts", newSettings.hideAmounts)
            putBoolean("isOnboardingCompleted", newSettings.isOnboardingCompleted)
            apply()
        }
        _settingsState.value = newSettings
    }

    fun hashPin(pin: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(("BUDGET_AI_SALT_" + pin).toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun verifyPin(pin: String): Boolean {
        val currentHash = _settingsState.value.pinHash
        if (currentHash.isEmpty()) return true
        return hashPin(pin) == currentHash
    }

    suspend fun initializeDefaultDataIfEmpty() = withContext(Dispatchers.IO) {
        val categories = db.categoryDao().getAllCategoriesSnapshot()
        if (categories.isEmpty()) {
            val defaultCats = listOf(
                // Income
                Category(name = "Salary", type = "INCOME", iconName = "payments", colorHex = "#10B981", isDefault = true),
                Category(name = "Freelance", type = "INCOME", iconName = "laptop", colorHex = "#06B6D4", isDefault = true),
                Category(name = "Business", type = "INCOME", iconName = "store", colorHex = "#3B82F6", isDefault = true),
                Category(name = "Investment", type = "INCOME", iconName = "trending_up", colorHex = "#8B5CF6", isDefault = true),
                Category(name = "Other Income", type = "INCOME", iconName = "attach_money", colorHex = "#64748B", isDefault = true),
                // Expenses
                Category(name = "Food", type = "EXPENSE", iconName = "restaurant", colorHex = "#EF4444", isDefault = true),
                Category(name = "Transport", type = "EXPENSE", iconName = "directions_car", colorHex = "#F97316", isDefault = true),
                Category(name = "Housing", type = "EXPENSE", iconName = "home", colorHex = "#F59E0B", isDefault = true),
                Category(name = "Utilities", type = "EXPENSE", iconName = "bolt", colorHex = "#84CC16", isDefault = true),
                Category(name = "Bills", type = "EXPENSE", iconName = "receipt_long", colorHex = "#10B981", isDefault = true),
                Category(name = "Education", type = "EXPENSE", iconName = "school", colorHex = "#06B6D4", isDefault = true),
                Category(name = "Health", type = "EXPENSE", iconName = "local_hospital", colorHex = "#0EA5E9", isDefault = true),
                Category(name = "Shopping", type = "EXPENSE", iconName = "shopping_bag", colorHex = "#6366F1", isDefault = true),
                Category(name = "Entertainment", type = "EXPENSE", iconName = "movie", colorHex = "#8B5CF6", isDefault = true),
                Category(name = "Travel", type = "EXPENSE", iconName = "flight", colorHex = "#D946EF", isDefault = true),
                Category(name = "Subscriptions", type = "EXPENSE", iconName = "subscriptions", colorHex = "#EC4899", isDefault = true),
                Category(name = "Family", type = "EXPENSE", iconName = "diversity_1", colorHex = "#F43F5E", isDefault = true),
                Category(name = "Other", type = "EXPENSE", iconName = "more_horiz", colorHex = "#64748B", isDefault = true)
            )
            db.categoryDao().insertAll(defaultCats)
        }

        val accounts = db.accountDao().getAllAccountsSnapshot()
        if (accounts.isEmpty()) {
            val defaultAccs = listOf(
                Account(name = "Cash Wallet", type = "CASH", openingBalanceMinor = 500000L), // 5,000.00
                Account(name = "Bank Account", type = "BANK", openingBalanceMinor = 5000000L) // 50,000.00
            )
            db.accountDao().insertAll(defaultAccs)
        }
    }

    suspend fun loadDemoData() = withContext(Dispatchers.IO) {
        clearAllDatabaseData()
        initializeDefaultDataIfEmpty()

        val cats = db.categoryDao().getAllCategoriesSnapshot()
        val accs = db.accountDao().getAllAccountsSnapshot()
        val cashAcc = accs.firstOrNull { it.type == "CASH" } ?: accs.first()
        val bankAcc = accs.firstOrNull { it.type == "BANK" } ?: accs.first()

        val salaryCat = cats.firstOrNull { it.name == "Salary" } ?: cats.first()
        val foodCat = cats.firstOrNull { it.name == "Food" } ?: cats.first()
        val transCat = cats.firstOrNull { it.name == "Transport" } ?: cats.first()
        val billsCat = cats.firstOrNull { it.name == "Bills" } ?: cats.first()
        val shopCat = cats.firstOrNull { it.name == "Shopping" } ?: cats.first()
        val healthCat = cats.firstOrNull { it.name == "Health" } ?: cats.first()
        val otherCat = cats.firstOrNull { it.name == "Other" } ?: cats.first()

        val now = System.currentTimeMillis()
        val dayMs = 86400000L

        // Income: LKR 150,000
        val tx1 = Transaction(
            accountId = bankAcc.id,
            categoryId = salaryCat.id,
            type = "INCOME",
            amountMinor = 15000000L, // 150,000.00
            date = now - (10 * dayMs),
            description = "Monthly Salary Deposit",
            paymentMethod = "Bank"
        )

        // Expenses totaling 87,500 LKR
        val txs = listOf(
            tx1,
            Transaction(accountId = cashAcc.id, categoryId = foodCat.id, type = "EXPENSE", amountMinor = 2500000L, date = now - (1 * dayMs), description = "Keells & Cargills Groceries", paymentMethod = "Card"),
            Transaction(accountId = cashAcc.id, categoryId = transCat.id, type = "EXPENSE", amountMinor = 1500000L, date = now - (3 * dayMs), description = "Fuel & Vehicle Service", paymentMethod = "Cash"),
            Transaction(accountId = bankAcc.id, categoryId = billsCat.id, type = "EXPENSE", amountMinor = 2000000L, date = now - (5 * dayMs), description = "Electricity & Fiber Internet", paymentMethod = "Bank"),
            Transaction(accountId = cashAcc.id, categoryId = shopCat.id, type = "EXPENSE", amountMinor = 1000000L, date = now - (6 * dayMs), description = "Clothing & Daily Essentials", paymentMethod = "Card"),
            Transaction(accountId = cashAcc.id, categoryId = healthCat.id, type = "EXPENSE", amountMinor = 850000L, date = now - (8 * dayMs), description = "Pharmacy & Wellness", paymentMethod = "Cash"),
            Transaction(accountId = cashAcc.id, categoryId = otherCat.id, type = "EXPENSE", amountMinor = 900000L, date = now - (9 * dayMs), description = "Home Supplies & Gifts", paymentMethod = "Cash")
        )
        db.transactionDao().insertAll(txs)

        // Monthly Budget: 100,000 LKR
        val cal = Calendar.getInstance()
        val currentBudget = Budget(
            categoryId = null,
            amountMinor = 10000000L, // 100,000.00
            periodType = "MONTHLY",
            month = cal.get(Calendar.MONTH) + 1,
            year = cal.get(Calendar.YEAR)
        )
        db.budgetDao().insertBudget(currentBudget)

        // Savings Goals
        val emergencyGoal = SavingsGoal(
            name = "Emergency Fund",
            targetAmountMinor = 30000000L, // 300,000.00
            currentAmountMinor = 3500000L, // 35,000.00
            targetDate = now + (180 * dayMs),
            categoryType = "EMERGENCY"
        )
        val vehicleGoal = SavingsGoal(
            name = "Vehicle Upgrade",
            targetAmountMinor = 100000000L, // 1,000,000.00
            currentAmountMinor = 7500000L, // 75,000.00
            targetDate = now + (365 * dayMs),
            categoryType = "VEHICLE"
        )
        db.savingsGoalDao().insertAll(listOf(emergencyGoal, vehicleGoal))

        // Recurring Bills
        val rec1 = RecurringTransaction(
            accountId = bankAcc.id,
            categoryId = billsCat.id,
            type = "EXPENSE",
            amountMinor = 385000L,
            description = "Home Fiber Internet",
            frequency = "MONTHLY",
            nextOccurrence = now + (7 * dayMs)
        )
        val rec2 = RecurringTransaction(
            accountId = cashAcc.id,
            categoryId = billsCat.id,
            type = "EXPENSE",
            amountMinor = 150000L,
            description = "Mobile Postpaid Bill",
            frequency = "MONTHLY",
            nextOccurrence = now + (12 * dayMs)
        )
        db.recurringDao().insertAll(listOf(rec1, rec2))
    }

    suspend fun clearAllDatabaseData() = withContext(Dispatchers.IO) {
        db.transactionDao().clearAll()
        db.budgetDao().clearAll()
        db.savingsGoalDao().clearAll()
        db.recurringDao().clearAll()
        db.accountDao().clearAll()
        db.categoryDao().clearAll()
    }

    // CRUD operations
    suspend fun insertTransaction(transaction: Transaction) = withContext(Dispatchers.IO) {
        db.transactionDao().insertTransaction(transaction)
    }

    suspend fun deleteTransaction(id: String) = withContext(Dispatchers.IO) {
        db.transactionDao().deleteById(id)
    }

    suspend fun insertBudget(budget: Budget) = withContext(Dispatchers.IO) {
        db.budgetDao().insertBudget(budget)
    }

    suspend fun insertGoal(goal: SavingsGoal) = withContext(Dispatchers.IO) {
        db.savingsGoalDao().insertGoal(goal)
    }

    suspend fun updateGoal(goal: SavingsGoal) = withContext(Dispatchers.IO) {
        db.savingsGoalDao().updateGoal(goal)
    }

    suspend fun insertCategory(category: Category) = withContext(Dispatchers.IO) {
        db.categoryDao().insertCategory(category)
    }

    suspend fun insertAccount(account: Account) = withContext(Dispatchers.IO) {
        db.accountDao().insertAccount(account)
    }

    suspend fun insertRecurring(recurring: RecurringTransaction) = withContext(Dispatchers.IO) {
        db.recurringDao().insertRecurring(recurring)
    }

    /**
     * Exports full database as an encrypted/validated JSON string for phone-to-phone migration.
     */
    suspend fun exportBackupJson(): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("app", "BUDGET_AI")
        root.put("version", 1)
        root.put("exportedAt", System.currentTimeMillis())
        root.put("attribution", "Created by Ajith Bandara")

        val accArray = JSONArray()
        for (a in db.accountDao().getAllAccountsSnapshot()) {
            val obj = JSONObject()
            obj.put("id", a.id)
            obj.put("name", a.name)
            obj.put("type", a.type)
            obj.put("openingBalanceMinor", a.openingBalanceMinor)
            obj.put("currency", a.currency)
            accArray.put(obj)
        }
        root.put("accounts", accArray)

        val catArray = JSONArray()
        for (c in db.categoryDao().getAllCategoriesSnapshot()) {
            val obj = JSONObject()
            obj.put("id", c.id)
            obj.put("name", c.name)
            obj.put("type", c.type)
            obj.put("iconName", c.iconName)
            obj.put("colorHex", c.colorHex)
            catArray.put(obj)
        }
        root.put("categories", catArray)

        val txArray = JSONArray()
        for (t in db.transactionDao().getAllTransactionsSnapshot()) {
            val obj = JSONObject()
            obj.put("id", t.id)
            obj.put("accountId", t.accountId)
            obj.put("categoryId", t.categoryId)
            obj.put("type", t.type)
            obj.put("amountMinor", t.amountMinor)
            obj.put("date", t.date)
            obj.put("description", t.description)
            obj.put("paymentMethod", t.paymentMethod)
            txArray.put(obj)
        }
        root.put("transactions", txArray)

        val bArray = JSONArray()
        for (b in db.budgetDao().getAllBudgetsSnapshot()) {
            val obj = JSONObject()
            obj.put("id", b.id)
            obj.put("categoryId", b.categoryId ?: JSONObject.NULL)
            obj.put("amountMinor", b.amountMinor)
            obj.put("month", b.month)
            obj.put("year", b.year)
            bArray.put(obj)
        }
        root.put("budgets", bArray)

        val gArray = JSONArray()
        for (g in db.savingsGoalDao().getAllGoalsSnapshot()) {
            val obj = JSONObject()
            obj.put("id", g.id)
            obj.put("name", g.name)
            obj.put("targetAmountMinor", g.targetAmountMinor)
            obj.put("currentAmountMinor", g.currentAmountMinor)
            obj.put("targetDate", g.targetDate)
            obj.put("categoryType", g.categoryType)
            gArray.put(obj)
        }
        root.put("goals", gArray)

        return@withContext root.toString(2)
    }

    /**
     * Validates and restores full backup JSON.
     */
    suspend fun restoreBackupJson(jsonString: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)
            if (!root.has("app") || root.getString("app") != "BUDGET_AI") {
                return@withContext Result.failure(IllegalArgumentException("Invalid backup file: Header mismatch"))
            }

            clearAllDatabaseData()

            // Restore Accounts
            val accList = mutableListOf<Account>()
            val accArray = root.optJSONArray("accounts") ?: JSONArray()
            for (i in 0 until accArray.length()) {
                val obj = accArray.getJSONObject(i)
                accList.add(
                    Account(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        type = obj.getString("type"),
                        openingBalanceMinor = obj.optLong("openingBalanceMinor", 0L),
                        currency = obj.optString("currency", "LKR")
                    )
                )
            }
            if (accList.isNotEmpty()) db.accountDao().insertAll(accList)

            // Restore Categories
            val catList = mutableListOf<Category>()
            val catArray = root.optJSONArray("categories") ?: JSONArray()
            for (i in 0 until catArray.length()) {
                val obj = catArray.getJSONObject(i)
                catList.add(
                    Category(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        type = obj.getString("type"),
                        iconName = obj.optString("iconName", "category"),
                        colorHex = obj.optString("colorHex", "#10B981")
                    )
                )
            }
            if (catList.isNotEmpty()) db.categoryDao().insertAll(catList)

            // Restore Transactions
            val txList = mutableListOf<Transaction>()
            val txArray = root.optJSONArray("transactions") ?: JSONArray()
            for (i in 0 until txArray.length()) {
                val obj = txArray.getJSONObject(i)
                txList.add(
                    Transaction(
                        id = obj.getString("id"),
                        accountId = obj.getString("accountId"),
                        categoryId = obj.getString("categoryId"),
                        type = obj.getString("type"),
                        amountMinor = obj.getLong("amountMinor"),
                        date = obj.getLong("date"),
                        description = obj.optString("description", ""),
                        paymentMethod = obj.optString("paymentMethod", "Cash")
                    )
                )
            }
            if (txList.isNotEmpty()) db.transactionDao().insertAll(txList)

            // Restore Budgets
            val bList = mutableListOf<Budget>()
            val bArray = root.optJSONArray("budgets") ?: JSONArray()
            for (i in 0 until bArray.length()) {
                val obj = bArray.getJSONObject(i)
                val catId = if (obj.isNull("categoryId")) null else obj.getString("categoryId")
                bList.add(
                    Budget(
                        id = obj.getString("id"),
                        categoryId = catId,
                        amountMinor = obj.getLong("amountMinor"),
                        month = obj.optInt("month", 1),
                        year = obj.optInt("year", 2026)
                    )
                )
            }
            if (bList.isNotEmpty()) db.budgetDao().insertAll(bList)

            // Restore Goals
            val gList = mutableListOf<SavingsGoal>()
            val gArray = root.optJSONArray("goals") ?: JSONArray()
            for (i in 0 until gArray.length()) {
                val obj = gArray.getJSONObject(i)
                gList.add(
                    SavingsGoal(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        targetAmountMinor = obj.getLong("targetAmountMinor"),
                        currentAmountMinor = obj.optLong("currentAmountMinor", 0L),
                        targetDate = obj.optLong("targetDate", System.currentTimeMillis()),
                        categoryType = obj.optString("categoryType", "CUSTOM")
                    )
                )
            }
            if (gList.isNotEmpty()) db.savingsGoalDao().insertAll(gList)

            val summary = "Restored ${txList.size} transactions, ${bList.size} budgets, ${gList.size} goals."
            Result.success(summary)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
