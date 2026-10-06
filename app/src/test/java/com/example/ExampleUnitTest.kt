package com.example

import com.example.budgetai.data.model.Account
import com.example.budgetai.data.model.Transaction
import com.example.budgetai.domain.engine.FinancialCalculationEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class ExampleUnitTest {

    @Test
    fun testCurrencyParsingAndFormatting() {
        // LKR 1,250.50 -> 125050 minor units
        val minor = FinancialCalculationEngine.parseToMinorUnits("1,250.50")
        assertEquals(125050L, minor)

        val formatted = FinancialCalculationEngine.formatCurrency(minor, "LKR")
        assertEquals("LKR 1,250.50", formatted)

        val hidden = FinancialCalculationEngine.formatCurrency(minor, "LKR", hideAmounts = true)
        assertEquals("••••••", hidden)
    }

    @Test
    fun testDeterministicBalanceCalculation() {
        val accounts = listOf(
            Account(id = "acc1", name = "Bank", type = "BANK", openingBalanceMinor = 1000000L) // 10,000.00
        )
        val transactions = listOf(
            Transaction(accountId = "acc1", categoryId = "c1", type = "INCOME", amountMinor = 15000000L, date = System.currentTimeMillis(), description = "Salary"), // +150,000
            Transaction(accountId = "acc1", categoryId = "c2", type = "EXPENSE", amountMinor = 8750000L, date = System.currentTimeMillis(), description = "Expenses") // -87,500
        )

        val totalBalance = FinancialCalculationEngine.calculateTotalBalance(accounts, transactions)
        // 10,000 + 150,000 - 87,500 = 72,500.00 -> 7250000 minor units
        assertEquals(7250000L, totalBalance)
    }

    @Test
    fun testBudgetCalculations() {
        // Budget = 100,000; Spent = 62,000; Remaining = 38,000; Usage = 62%
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1

        val monthlyBudgetMinor = 10000000L // 100,000.00
        val txs = listOf(
            Transaction(
                accountId = "acc1",
                categoryId = "c1",
                type = "EXPENSE",
                amountMinor = 6200000L, // 62,000.00
                date = System.currentTimeMillis(),
                description = "Spent"
            )
        )

        val status = FinancialCalculationEngine.calculateBudgetStatus(monthlyBudgetMinor, txs, year, month)
        assertEquals(6200000L, status.spentMinor)
        assertEquals(3800000L, status.remainingMinor)
        assertEquals(62, status.usedPercentage)
        assertEquals(38, status.remainingPercentage)
        assertTrue(status.daysRemainingInMonth >= 1)
        assertTrue(status.safeDailySpendMinor > 0)
    }
}
