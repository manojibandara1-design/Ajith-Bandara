package com.example.budgetai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.budgetai.data.model.Category
import com.example.budgetai.data.model.Transaction
import com.example.budgetai.domain.engine.FinancialCalculationEngine
import com.example.budgetai.localization.AppLocale
import com.example.budgetai.ui.components.CategoryDonutChart
import com.example.budgetai.ui.components.CategorySpendItem
import com.example.budgetai.ui.components.IncomeExpenseBarChart
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ExpenseRose

@Composable
fun ReportsScreen(
    transactions: List<Transaction>,
    categories: List<Category>,
    currency: String,
    language: String,
    hideAmounts: Boolean
) {
    var selectedDays by remember { mutableStateOf(30) } // 7, 30, 90, 365

    val now = System.currentTimeMillis()
    val cutoffTime = now - (selectedDays.toLong() * 86400000L)

    val periodTransactions = transactions.filter { it.date >= cutoffTime }
    val totalIncome = periodTransactions.filter { it.type == "INCOME" }.sumOf { it.amountMinor }
    val totalExpense = periodTransactions.filter { it.type == "EXPENSE" }.sumOf { it.amountMinor }
    val netRemaining = totalIncome - totalExpense

    val catMap = remember(categories) { categories.associateBy { it.id } }

    val categoryColors = listOf(
        Color(0xFFEF4444),
        Color(0xFFF97316),
        Color(0xFFF59E0B),
        Color(0xFF10B981),
        Color(0xFF06B6D4),
        Color(0xFF3B82F6),
        Color(0xFF8B5CF6),
        Color(0xFFEC4899),
        Color(0xFF64748B)
    )

    val expenseItems = periodTransactions
        .filter { it.type == "EXPENSE" }
        .groupBy { it.categoryId }
        .mapNotNull { (catId, txs) ->
            val cat = catMap[catId]
            val amount = txs.sumOf { it.amountMinor }
            cat?.name to amount
        }
        .sortedByDescending { it.second }

    val donutItems = expenseItems.mapIndexed { index, (name, amount) ->
        CategorySpendItem(
            categoryName = name ?: "General",
            amountMinor = amount,
            color = categoryColors[index % categoryColors.size]
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("reports_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Time Filters: 7 days, 30 days, 3 months, 12 months
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filters = listOf(
                7 to "7 Days",
                30 to "30 Days",
                90 to "3 Months",
                365 to "12 Months"
            )
            filters.forEach { (days, label) ->
                val isSelected = selectedDays == days
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .clickable { selectedDays = days }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .testTag("report_filter_$days"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        ),
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Summary Metric Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = AppLocale.t("income", language), style = MaterialTheme.typography.labelSmall, color = EmeraldPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = FinancialCalculationEngine.formatCurrency(totalIncome, currency, hideAmounts),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = AppLocale.t("expenses", language), style = MaterialTheme.typography.labelSmall, color = ExpenseRose)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = FinancialCalculationEngine.formatCurrency(totalExpense, currency, hideAmounts),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }

        // Income vs Expenses Comparison Chart
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = AppLocale.t("income_vs_expense", language),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(14.dp))
                IncomeExpenseBarChart(
                    incomeMinor = totalIncome,
                    expenseMinor = totalExpense,
                    currency = currency,
                    hideAmounts = hideAmounts
                )
            }
        }

        // Spending by Category Donut Chart
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = AppLocale.t("spending_by_category", language),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(12.dp))
                CategoryDonutChart(
                    items = donutItems,
                    currency = currency,
                    hideAmounts = hideAmounts
                )
            }
        }

        // Top Expenses
        if (periodTransactions.any { it.type == "EXPENSE" }) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = AppLocale.t("top_expenses", language),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val topTxs = periodTransactions
                        .filter { it.type == "EXPENSE" }
                        .sortedByDescending { it.amountMinor }
                        .take(5)

                    topTxs.forEach { tx ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = tx.description,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = FinancialCalculationEngine.formatCurrency(tx.amountMinor, currency, hideAmounts),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = ExpenseRose
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
