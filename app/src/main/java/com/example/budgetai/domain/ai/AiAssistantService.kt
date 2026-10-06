package com.example.budgetai.domain.ai

import com.example.budgetai.data.model.Account
import com.example.budgetai.data.model.Budget
import com.example.budgetai.data.model.Category
import com.example.budgetai.data.model.RecurringTransaction
import com.example.budgetai.data.model.SavingsGoal
import com.example.budgetai.data.model.Transaction
import com.example.budgetai.domain.engine.FinancialCalculationEngine
import java.util.Calendar
import java.util.Locale
import java.util.regex.Pattern

data class ParsedTransactionDraft(
    val amountMinor: Long,
    val type: String, // INCOME, EXPENSE
    val categoryId: String?,
    val categoryName: String,
    val description: String,
    val dateEpoch: Long,
    val rawPrompt: String
)

data class AiChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val isUser: Boolean,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val actionDraft: ParsedTransactionDraft? = null
)

object AiAssistantService {

    /**
     * Parses natural language statements into a structured transaction draft.
     * The deterministic engine identifies amounts, keywords, dates, and types.
     */
    fun parseNaturalLanguageTransaction(
        input: String,
        categories: List<Category>
    ): ParsedTransactionDraft? {
        val lower = input.lowercase().trim()
        if (lower.isEmpty()) return null

        // 1. Extract amount using regex (matches numbers like 1,500.50, 2500, 850)
        val amountPattern = Pattern.compile("(\\d{1,3}(,\\d{3})*(\\.\\d+)?|\\d+(\\.\\d+)?)")
        val matcher = amountPattern.matcher(lower)
        var amountMinor = 0L
        if (matcher.find()) {
            val matchedStr = matcher.group(1) ?: ""
            amountMinor = FinancialCalculationEngine.parseToMinorUnits(matchedStr)
        }

        if (amountMinor <= 0L) return null

        // 2. Detect type (Income vs Expense)
        val incomeKeywords = listOf(
            "salary", "income", "freelance", "earned", "received", "bonus", "dividend",
            "ආදායම", "ලැබුණා", "පඩි", "වැටුප", "ගෙවීම ලැබුණා"
        )
        val isIncome = incomeKeywords.any { lower.contains(it) }
        val type = if (isIncome) "INCOME" else "EXPENSE"

        // 3. Detect date (yesterday vs today)
        val calendar = Calendar.getInstance()
        if (lower.contains("yesterday") || lower.contains("ඊයේ")) {
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }

        // 4. Match Category
        var matchedCat: Category? = null
        if (isIncome) {
            matchedCat = categories.firstOrNull { it.type == "INCOME" && lower.contains(it.name.lowercase()) }
                ?: categories.firstOrNull { it.type == "INCOME" }
        } else {
            // Food keywords
            val foodWords = listOf("food", "groceries", "restaurant", "lunch", "dinner", "breakfast", "keells", "cargills", "kottu", "rice", "tea", "කෑම", "ආහාර", "බීම")
            val transportWords = listOf("transport", "fuel", "petrol", "diesel", "uber", "pickme", "bus", "train", "taxi", "තෙල්", "පෙට්‍රල්", "ගමන්")
            val billsWords = listOf("bill", "electricity", "water", "ceb", "dialog", "mobitel", "slt", "wifi", "internet", "බිල්", "විදුලි", "වතුර")
            val healthWords = listOf("doctor", "medicine", "pharmacy", "hospital", "බෙහෙත්", "වෛද්‍ය")
            val shoppingWords = listOf("clothes", "shoes", "dress", "shopping", "ඇඳුම්", "සාප්පු")

            when {
                foodWords.any { lower.contains(it) } ->
                    matchedCat = categories.firstOrNull { it.name.equals("Food", ignoreCase = true) || it.name.contains("කෑම") }
                transportWords.any { lower.contains(it) } ->
                    matchedCat = categories.firstOrNull { it.name.equals("Transport", ignoreCase = true) || it.name.contains("ප්‍රවාහන") }
                billsWords.any { lower.contains(it) } ->
                    matchedCat = categories.firstOrNull { it.name.equals("Bills", ignoreCase = true) || it.name.equals("Utilities", ignoreCase = true) }
                healthWords.any { lower.contains(it) } ->
                    matchedCat = categories.firstOrNull { it.name.equals("Health", ignoreCase = true) }
                shoppingWords.any { lower.contains(it) } ->
                    matchedCat = categories.firstOrNull { it.name.equals("Shopping", ignoreCase = true) }
            }

            if (matchedCat == null) {
                matchedCat = categories.firstOrNull { it.type == "EXPENSE" && lower.contains(it.name.lowercase()) }
            }
            if (matchedCat == null) {
                matchedCat = categories.firstOrNull { it.type == "EXPENSE" }
            }
        }

        val description = input.take(60)

        return ParsedTransactionDraft(
            amountMinor = amountMinor,
            type = type,
            categoryId = matchedCat?.id,
            categoryName = matchedCat?.name ?: "General",
            description = description,
            dateEpoch = calendar.timeInMillis,
            rawPrompt = input
        )
    }

    /**
     * Answers queries deterministically using strict mathematical context.
     */
    fun answerUserQuestion(
        question: String,
        lang: String,
        currency: String,
        accounts: List<Account>,
        transactions: List<Transaction>,
        budgets: List<Budget>,
        goals: List<SavingsGoal>,
        recurring: List<RecurringTransaction>
    ): String {
        val q = question.lowercase().trim()
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1

        val totalBalance = FinancialCalculationEngine.calculateTotalBalance(accounts, transactions)
        val monthSummary = FinancialCalculationEngine.calculateMonthSummary(transactions, year, month)
        val monthlyBudget = budgets.firstOrNull { it.categoryId == null }?.amountMinor ?: 0L
        val budgetStatus = FinancialCalculationEngine.calculateBudgetStatus(monthlyBudget, transactions, year, month)

        // 1. Check if asking about food spending
        if (q.contains("food") || q.contains("කෑම")) {
            val foodExpenses = transactions
                .filter { tx ->
                    cal.timeInMillis = tx.date
                    cal.get(Calendar.YEAR) == year && (cal.get(Calendar.MONTH) + 1) == month &&
                            tx.type == "EXPENSE" && (tx.description.lowercase().contains("food") || tx.description.lowercase().contains("කෑම") || tx.description.lowercase().contains("grocer") || tx.description.lowercase().contains("keells"))
                }
                .sumOf { it.amountMinor }

            val formatted = FinancialCalculationEngine.formatCurrency(foodExpenses, currency)
            return if (lang == "si") {
                "මෙම මාසයේ කෑම බීම (Food & Groceries) සඳහා ඔබේ මුළු වියදම $formatted කි."
            } else {
                "You have spent $formatted on Food and Groceries this month."
            }
        }

        // 2. Can I afford spending X today?
        val amountPattern = Pattern.compile("(\\d{1,3}(,\\d{3})*|\\d+)")
        val matcher = amountPattern.matcher(q)
        if ((q.contains("afford") || q.contains("spend") || q.contains("වියදම් කළ හැකිද")) && matcher.find()) {
            val askAmountMinor = FinancialCalculationEngine.parseToMinorUnits(matcher.group(1) ?: "0")
            val safeDaily = budgetStatus.safeDailySpendMinor
            val formattedAsk = FinancialCalculationEngine.formatCurrency(askAmountMinor, currency)
            val formattedDaily = FinancialCalculationEngine.formatCurrency(safeDaily, currency)

            return if (askAmountMinor <= safeDaily) {
                if (lang == "si") {
                    "ඔව්! $formattedAsk වියදම් කිරීම ආරක්ෂිතයි. ඔබගේ නිර්දේශිත දෛනික වියදම් සීමාව $formattedDaily කි."
                } else {
                    "Yes, you can safely afford $formattedAsk. Your recommended daily spending limit is $formattedDaily."
                }
            } else {
                if (lang == "si") {
                    "ප්‍රවේශම් වන්න: $formattedAsk ඔබේ දෛනික නිර්දේශිත සීමාව ($formattedDaily) ඉක්මවයි. මෙය අයවැයට බලපෑම් කළ හැක."
                } else {
                    "Caution: $formattedAsk exceeds your safe daily limit of $formattedDaily. Spending this will reduce your remaining monthly budget."
                }
            }
        }

        // 3. Why am I spending more / Overspending analysis
        if (q.contains("overspend") || q.contains("more") || q.contains("වැඩි") || q.contains("වියදම් වැඩි")) {
            val expFormatted = FinancialCalculationEngine.formatCurrency(monthSummary.totalExpenseMinor, currency)
            val budgetFormatted = FinancialCalculationEngine.formatCurrency(budgetStatus.budgetAmountMinor, currency)

            return if (lang == "si") {
                "මෙම මාසයේ මුළු වියදම $expFormatted කි. අයවැය $budgetFormatted කි (${budgetStatus.usedPercentage}% භාවිත කර ඇත). ඉතිරි දින ගණන ${budgetStatus.daysRemainingInMonth} කි."
            } else {
                "This month your total expense is $expFormatted against a budget of $budgetFormatted (${budgetStatus.usedPercentage}% consumed). Days remaining: ${budgetStatus.daysRemainingInMonth}."
            }
        }

        // 4. Upcoming bills / recurring
        if (q.contains("bill") || q.contains("recurring") || q.contains("බිල්") || q.contains("ඉදිරි")) {
            if (recurring.isEmpty()) {
                return if (lang == "si") {
                    "ඉදිරි පුනරාවර්තී බිල්පත් හෝ ගෙවීම් සටහන් කර නොමැත."
                } else {
                    "No upcoming recurring bills scheduled."
                }
            }
            val totalRec = recurring.sumOf { it.amountMinor }
            val formatted = FinancialCalculationEngine.formatCurrency(totalRec, currency)
            val count = recurring.size
            return if (lang == "si") {
                "ඔබට ඉදිරි පුනරාවර්තී ගෙවීම් $count ක් ඇති අතර මුළු වටිනාකම $formatted කි."
            } else {
                "You have $count upcoming recurring bills totaling $formatted."
            }
        }

        // 5. Default intelligent financial summary
        val bal = FinancialCalculationEngine.formatCurrency(totalBalance, currency)
        val inc = FinancialCalculationEngine.formatCurrency(monthSummary.totalIncomeMinor, currency)
        val exp = FinancialCalculationEngine.formatCurrency(monthSummary.totalExpenseMinor, currency)
        val safe = FinancialCalculationEngine.formatCurrency(budgetStatus.safeDailySpendMinor, currency)

        return if (lang == "si") {
            "මුළු ශේෂය: $bal | මෙම මාසයේ ආදායම: $inc | වියදම: $exp | දෛනික ආරක්ෂිත වියදම: $safe. ඔබගේ අයවැය පාලනය සාර්ථකව පවත්වා ගැනීමට දෛනික සීමාව ඉක්මවා නොයන්න."
        } else {
            "Current Balance: $bal | This Month Income: $inc | Expenses: $exp | Safe Daily Spend: $safe. Keeping within this daily limit will protect your savings target."
        }
    }
}
