package com.example.budgetai.domain.engine

import com.example.budgetai.data.model.Account
import com.example.budgetai.data.model.Budget
import com.example.budgetai.data.model.SavingsGoal
import com.example.budgetai.data.model.Transaction
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

data class MonthFinancialSummary(
    val totalIncomeMinor: Long,
    val totalExpenseMinor: Long,
    val netSavingsMinor: Long,
    val savingsRatePercentage: Int
)

data class BudgetStatusSummary(
    val budgetAmountMinor: Long,
    val spentMinor: Long,
    val remainingMinor: Long,
    val usedPercentage: Int,
    val remainingPercentage: Int,
    val daysRemainingInMonth: Int,
    val safeDailySpendMinor: Long,
    val isOverBudget: Boolean
)

data class FinancialHealthMetric(
    val score: Int, // 0 - 100
    val statusKey: String, // health_status_healthy, etc.
    val savingsRate: Int,
    val budgetAdherence: Int,
    val adviceEn: String,
    val adviceSi: String
)

object FinancialCalculationEngine {

    /**
     * Converts a display decimal amount string (e.g. "1250.50") into integer minor units (125050).
     */
    fun parseToMinorUnits(input: String): Long {
        val sanitized = input.trim().replace(",", "")
        if (sanitized.isEmpty()) return 0L
        val parts = sanitized.split(".")
        val whole = parts[0].toLongOrNull() ?: 0L
        val fraction = if (parts.size > 1) {
            val fracPart = parts[1].take(2).padEnd(2, '0')
            fracPart.toLongOrNull() ?: 0L
        } else {
            0L
        }
        return (whole * 100L) + fraction
    }

    /**
     * Formats integer minor units into currency string (e.g., 125050 -> "1,250.50" or "LKR 1,250.50").
     */
    fun formatCurrency(
        amountMinor: Long,
        currencyCode: String = "LKR",
        hideAmounts: Boolean = false,
        includeCode: Boolean = true
    ): String {
        if (hideAmounts) return "••••••"
        val isNegative = amountMinor < 0
        val absMinor = kotlin.math.abs(amountMinor)
        val whole = absMinor / 100L
        val fraction = absMinor % 100L

        val nf = NumberFormat.getNumberInstance(Locale.US)
        val wholeFormatted = nf.format(whole)
        val fractionFormatted = String.format(Locale.US, "%02d", fraction)

        val sign = if (isNegative) "-" else ""
        val valueString = "$sign$wholeFormatted.$fractionFormatted"

        return if (includeCode) "$currencyCode $valueString" else valueString
    }

    /**
     * Calculates the deterministic total account balance across all accounts and transactions.
     */
    fun calculateTotalBalance(accounts: List<Account>, transactions: List<Transaction>): Long {
        val openingTotal = accounts.sumOf { it.openingBalanceMinor }
        var netTransactions = 0L
        for (tx in transactions) {
            when (tx.type) {
                "INCOME" -> netTransactions += tx.amountMinor
                "EXPENSE" -> netTransactions -= tx.amountMinor
                "TRANSFER" -> {
                    // Internal transfer between user accounts doesn't alter global balance
                }
            }
        }
        return openingTotal + netTransactions
    }

    /**
     * Calculates deterministic monthly income, expenses, and savings.
     */
    fun calculateMonthSummary(
        transactions: List<Transaction>,
        year: Int,
        month: Int // 1-12
    ): MonthFinancialSummary {
        val cal = Calendar.getInstance()
        var income = 0L
        var expense = 0L

        for (tx in transactions) {
            cal.timeInMillis = tx.date
            val txYear = cal.get(Calendar.YEAR)
            val txMonth = cal.get(Calendar.MONTH) + 1
            if (txYear == year && txMonth == month) {
                if (tx.type == "INCOME") {
                    income += tx.amountMinor
                } else if (tx.type == "EXPENSE") {
                    expense += tx.amountMinor
                }
            }
        }

        val netSavings = income - expense
        val rate = if (income > 0) {
            val calcRate = ((netSavings.toDouble() / income.toDouble()) * 100).toInt()
            calcRate.coerceIn(-100, 100)
        } else {
            0
        }

        return MonthFinancialSummary(
            totalIncomeMinor = income,
            totalExpenseMinor = expense,
            netSavingsMinor = netSavings,
            savingsRatePercentage = rate
        )
    }

    /**
     * Calculates deterministic budget usage, days remaining in the month, and safe daily spending limit.
     */
    fun calculateBudgetStatus(
        monthlyBudgetMinor: Long,
        transactions: List<Transaction>,
        year: Int,
        month: Int
    ): BudgetStatusSummary {
        val cal = Calendar.getInstance()
        var spent = 0L

        for (tx in transactions) {
            cal.timeInMillis = tx.date
            val txYear = cal.get(Calendar.YEAR)
            val txMonth = cal.get(Calendar.MONTH) + 1
            if (txYear == year && txMonth == month && tx.type == "EXPENSE") {
                spent += tx.amountMinor
            }
        }

        val remaining = (monthlyBudgetMinor - spent).coerceAtLeast(0L)
        val isOverBudget = spent > monthlyBudgetMinor && monthlyBudgetMinor > 0

        val usedPercent = if (monthlyBudgetMinor > 0) {
            ((spent.toDouble() / monthlyBudgetMinor.toDouble()) * 100).toInt().coerceIn(0, 100)
        } else {
            0
        }
        val remainingPercent = (100 - usedPercent).coerceAtLeast(0)

        // Days remaining in month
        val todayCal = Calendar.getInstance()
        val currentDay = todayCal.get(Calendar.DAY_OF_MONTH)
        val maxDays = todayCal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val daysRemaining = (maxDays - currentDay + 1).coerceAtLeast(1)

        val safeDaily = if (remaining > 0 && daysRemaining > 0) {
            remaining / daysRemaining
        } else {
            0L
        }

        return BudgetStatusSummary(
            budgetAmountMinor = monthlyBudgetMinor,
            spentMinor = spent,
            remainingMinor = if (spent > monthlyBudgetMinor) -(spent - monthlyBudgetMinor) else remaining,
            usedPercentage = usedPercent,
            remainingPercentage = remainingPercent,
            daysRemainingInMonth = daysRemaining,
            safeDailySpendMinor = safeDaily,
            isOverBudget = isOverBudget
        )
    }

    /**
     * Calculates deterministic overall financial health score (0-100).
     */
    fun calculateFinancialHealth(
        monthSummary: MonthFinancialSummary,
        budgetSummary: BudgetStatusSummary,
        goals: List<SavingsGoal>
    ): FinancialHealthMetric {
        var score = 60

        // 1. Savings rate weight (up to +20 or -20)
        if (monthSummary.totalIncomeMinor > 0) {
            when {
                monthSummary.savingsRatePercentage >= 30 -> score += 20
                monthSummary.savingsRatePercentage >= 15 -> score += 10
                monthSummary.savingsRatePercentage >= 0 -> score += 0
                else -> score -= 20
            }
        }

        // 2. Budget adherence weight (up to +15 or -20)
        if (budgetSummary.budgetAmountMinor > 0) {
            if (budgetSummary.isOverBudget) {
                score -= 25
            } else if (budgetSummary.usedPercentage < 80) {
                score += 15
            } else {
                score += 5
            }
        }

        // 3. Goals active weight (+5)
        if (goals.isNotEmpty()) {
            val hasFundedGoal = goals.any { it.currentAmountMinor > 0 }
            if (hasFundedGoal) score += 5
        }

        score = score.coerceIn(10, 100)

        val (statusKey, adviceEn, adviceSi) = when {
            score >= 80 -> Triple(
                "health_status_healthy",
                "Excellent financial control! Your savings rate and budget adherence are strong.",
                "විශිෂ්ට මූල්‍ය පාලනයක්! ඔබේ ඉතිරිකිරීමේ ප්‍රතිශතය සහ අයවැය හැසිරීම ඉතා යහපත්ය."
            )
            score >= 60 -> Triple(
                "health_status_good",
                "Good stability. Try to cut back on minor non-essential expenses to boost savings.",
                "යහපත් ස්ථාවරත්වයක්. ඉතිරිකිරීම වැඩි කරගැනීමට අනවශ්‍ය වියදම් මඳක් සීමා කරන්න."
            )
            score >= 40 -> Triple(
                "health_status_caution",
                "Caution: Your expenses are approaching your income limits. Stick to your safe daily spend.",
                "අවධානයෙන් සිටින්න: වියදම් ආදායම ඉක්මවීමට ආසන්නයි. දෛනික වියදම් සීමාවට අනුගත වන්න."
            )
            else -> Triple(
                "health_status_warning",
                "Warning: Spending exceeds safe budget thresholds. Focus on essential bills only.",
                "අවදානම්: වියදම් අයවැය ඉක්මවා ගොස් ඇත. අත්‍යවශ්‍ය බිල්පත් වලට පමණක් ප්‍රමුඛතාව දෙන්න."
            )
        }

        return FinancialHealthMetric(
            score = score,
            statusKey = statusKey,
            savingsRate = monthSummary.savingsRatePercentage,
            budgetAdherence = 100 - budgetSummary.usedPercentage,
            adviceEn = adviceEn,
            adviceSi = adviceSi
        )
    }
}
