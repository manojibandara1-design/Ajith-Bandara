package com.example.budgetai.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.budgetai.data.model.Budget
import com.example.budgetai.data.model.Category
import com.example.budgetai.data.model.Transaction
import com.example.budgetai.domain.engine.FinancialCalculationEngine
import com.example.budgetai.localization.AppLocale
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ExpenseRose
import java.util.Calendar

@Composable
fun BudgetScreen(
    budgets: List<Budget>,
    transactions: List<Transaction>,
    categories: List<Category>,
    currency: String,
    language: String,
    hideAmounts: Boolean,
    onSetMonthlyBudget: (Long) -> Unit
) {
    val cal = Calendar.getInstance()
    val year = cal.get(Calendar.YEAR)
    val month = cal.get(Calendar.MONTH) + 1

    val currentBudget = budgets.firstOrNull { it.categoryId == null }
    val monthlyBudgetMinor = currentBudget?.amountMinor ?: 0L

    val budgetStatus = FinancialCalculationEngine.calculateBudgetStatus(
        monthlyBudgetMinor = monthlyBudgetMinor,
        transactions = transactions,
        year = year,
        month = month
    )

    var showEditBudgetDialog by remember { mutableStateOf(false) }
    var budgetInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("budget_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Main Monthly Budget Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("monthly_budget_main_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = AppLocale.t("monthly_budget", language),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "${cal.getDisplayName(Calendar.MONTH, Calendar.LONG, java.util.Locale.getDefault())} $year",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = {
                            val initial = if (monthlyBudgetMinor > 0) (monthlyBudgetMinor / 100).toString() else ""
                            budgetInput = initial
                            showEditBudgetDialog = true
                        },
                        modifier = Modifier.testTag("edit_budget_button")
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Budget", tint = MaterialTheme.colorScheme.primary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (monthlyBudgetMinor <= 0L) {
                    // Empty budget state
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = AppLocale.t("no_budgets", language),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    budgetInput = "100000"
                                    showEditBudgetDialog = true
                                },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(AppLocale.t("set_budget", language))
                            }
                        }
                    }
                } else {
                    // Active Budget Summary
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = "Budget Amount",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = FinancialCalculationEngine.formatCurrency(budgetStatus.budgetAmountMinor, currency, hideAmounts),
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${budgetStatus.usedPercentage}% ${AppLocale.t("budget_used", language)}",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (budgetStatus.isOverBudget) ExpenseRose else EmeraldPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LinearProgressIndicator(
                        progress = { (budgetStatus.usedPercentage / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = if (budgetStatus.isOverBudget) ExpenseRose else EmeraldPrimary,
                        trackColor = MaterialTheme.colorScheme.surface
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Spent vs Remaining row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Spent",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = FinancialCalculationEngine.formatCurrency(budgetStatus.spentMinor, currency, hideAmounts),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = ExpenseRose
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = AppLocale.t("budget_remaining", language),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = FinancialCalculationEngine.formatCurrency(budgetStatus.remainingMinor, currency, hideAmounts),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (budgetStatus.remainingMinor >= 0) EmeraldPrimary else ExpenseRose
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Deterministic Safe Daily Spending Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = AppLocale.t("safe_daily_spend", language),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "${FinancialCalculationEngine.formatCurrency(budgetStatus.safeDailySpendMinor, currency, hideAmounts)} / day",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = AppLocale.t("days_remaining", language),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${budgetStatus.daysRemainingInMonth} days",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Category Budgets / Breakdown Header
        Text(
            text = AppLocale.t("category_budget", language),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )

        // Category breakdown list
        val expenseCats = categories.filter { it.type == "EXPENSE" }
        val categorySpends = expenseCats.map { cat ->
            val spent = transactions
                .filter { tx ->
                    cal.timeInMillis = tx.date
                    cal.get(Calendar.YEAR) == year && (cal.get(Calendar.MONTH) + 1) == month &&
                            tx.type == "EXPENSE" && tx.categoryId == cat.id
                }
                .sumOf { it.amountMinor }
            cat to spent
        }.filter { it.second > 0L }

        if (categorySpends.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No category spending recorded this month.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            categorySpends.forEach { (cat, spentMinor) ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = cat.name,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = FinancialCalculationEngine.formatCurrency(spentMinor, currency, hideAmounts),
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

    // Set/Edit Budget Dialog
    if (showEditBudgetDialog) {
        AlertDialog(
            onDismissRequest = { showEditBudgetDialog = false },
            title = { Text(AppLocale.t("set_budget", language)) },
            text = {
                Column {
                    Text(
                        text = "Enter total monthly spending budget:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = budgetInput,
                        onValueChange = { budgetInput = it },
                        label = { Text("Budget Amount ($currency)") },
                        placeholder = { Text("100000") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("budget_input_field")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsedMinor = FinancialCalculationEngine.parseToMinorUnits(budgetInput)
                        if (parsedMinor > 0) {
                            onSetMonthlyBudget(parsedMinor)
                            showEditBudgetDialog = false
                        }
                    },
                    modifier = Modifier.testTag("budget_save_dialog_button")
                ) {
                    Text(AppLocale.t("save", language))
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditBudgetDialog = false }) {
                    Text(AppLocale.t("cancel", language))
                }
            }
        )
    }
}
