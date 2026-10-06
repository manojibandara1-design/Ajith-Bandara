package com.example.budgetai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.budgetai.data.model.Account
import com.example.budgetai.data.model.Budget
import com.example.budgetai.data.model.RecurringTransaction
import com.example.budgetai.data.model.SavingsGoal
import com.example.budgetai.data.model.Transaction
import com.example.budgetai.domain.engine.FinancialCalculationEngine
import com.example.budgetai.localization.AppLocale
import com.example.budgetai.ui.components.FinancialHealthCard
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ExpenseRose
import com.example.ui.theme.BudgetAmber
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    accounts: List<Account>,
    transactions: List<Transaction>,
    budgets: List<Budget>,
    goals: List<SavingsGoal>,
    recurring: List<RecurringTransaction>,
    currency: String,
    language: String,
    hideAmounts: Boolean,
    onQuickAdd: (String) -> Unit,
    onNavigate: (String) -> Unit
) {
    val cal = Calendar.getInstance()
    val year = cal.get(Calendar.YEAR)
    val month = cal.get(Calendar.MONTH) + 1

    val totalBalanceMinor = FinancialCalculationEngine.calculateTotalBalance(accounts, transactions)
    val monthSummary = FinancialCalculationEngine.calculateMonthSummary(transactions, year, month)
    val monthlyBudget = budgets.firstOrNull { it.categoryId == null }?.amountMinor ?: 0L
    val budgetStatus = FinancialCalculationEngine.calculateBudgetStatus(monthlyBudget, transactions, year, month)
    val healthMetric = FinancialCalculationEngine.calculateFinancialHealth(monthSummary, budgetStatus, goals)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("home_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Current Balance Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("balance_hero_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = AppLocale.t("current_balance", language),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "100% Offline",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = FinancialCalculationEngine.formatCurrency(totalBalanceMinor, currency, hideAmounts),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 32.sp
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Spacer(modifier = Modifier.height(16.dp))

                // This Month Row: Income vs Expenses vs Remaining
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MonthMiniMetric(
                        label = AppLocale.t("income", language),
                        amount = FinancialCalculationEngine.formatCurrency(monthSummary.totalIncomeMinor, currency, hideAmounts),
                        color = EmeraldPrimary,
                        icon = Icons.Default.ArrowUpward
                    )
                    MonthMiniMetric(
                        label = AppLocale.t("expenses", language),
                        amount = FinancialCalculationEngine.formatCurrency(monthSummary.totalExpenseMinor, currency, hideAmounts),
                        color = ExpenseRose,
                        icon = Icons.Default.ArrowDownward
                    )
                    MonthMiniMetric(
                        label = AppLocale.t("remaining", language),
                        amount = FinancialCalculationEngine.formatCurrency(monthSummary.netSavingsMinor, currency, hideAmounts),
                        color = if (monthSummary.netSavingsMinor >= 0) EmeraldPrimary else ExpenseRose,
                        icon = Icons.Default.AccountBalanceWallet
                    )
                }
            }
        }

        // 2. Quick Action Buttons (+Income, +Expense, Transfer, Set Budget, Add Goal)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            QuickActionButton(
                label = AppLocale.t("add_expense", language),
                icon = Icons.Default.ArrowDownward,
                containerColor = ExpenseRose.copy(alpha = 0.15f),
                contentColor = ExpenseRose,
                onClick = { onQuickAdd("EXPENSE") },
                tag = "quick_action_expense"
            )
            QuickActionButton(
                label = AppLocale.t("add_income", language),
                icon = Icons.Default.ArrowUpward,
                containerColor = EmeraldPrimary.copy(alpha = 0.15f),
                contentColor = EmeraldPrimary,
                onClick = { onQuickAdd("INCOME") },
                tag = "quick_action_income"
            )
            QuickActionButton(
                label = AppLocale.t("transfer", language),
                icon = Icons.Default.SwapHoriz,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurface,
                onClick = { onQuickAdd("TRANSFER") },
                tag = "quick_action_transfer"
            )
            QuickActionButton(
                label = AppLocale.t("set_budget", language),
                icon = Icons.Default.AccountBalanceWallet,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.primary,
                onClick = { onNavigate("BUDGET") },
                tag = "quick_action_budget"
            )
            QuickActionButton(
                label = AppLocale.t("add_goal", language),
                icon = Icons.Default.Flag,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = BudgetAmber,
                onClick = { onNavigate("GOALS") },
                tag = "quick_action_goal"
            )
        }

        // 3. Monthly Budget & Safe Daily Spend Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigate("BUDGET") }
                .testTag("budget_summary_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = AppLocale.t("budget_status", language),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "${budgetStatus.usedPercentage}% ${AppLocale.t("budget_used", language)}",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (budgetStatus.isOverBudget) ExpenseRose else EmeraldPrimary
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                LinearProgressIndicator(
                    progress = { (budgetStatus.usedPercentage / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (budgetStatus.isOverBudget) ExpenseRose else EmeraldPrimary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = AppLocale.t("safe_daily_spend", language),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${FinancialCalculationEngine.formatCurrency(budgetStatus.safeDailySpendMinor, currency, hideAmounts)} / day",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
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

        // 4. Financial Health Card (Deterministic score & advice)
        FinancialHealthCard(metric = healthMetric, language = language)

        // 5. Savings Goals Preview
        if (goals.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate("GOALS") }
                    .testTag("goals_summary_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = AppLocale.t("savings_progress", language),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    goals.take(2).forEach { goal ->
                        val pct = if (goal.targetAmountMinor > 0) {
                            ((goal.currentAmountMinor.toDouble() / goal.targetAmountMinor.toDouble()) * 100).toInt()
                        } else 0

                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = goal.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Text(
                                    text = "$pct%",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { (pct / 100f).coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = BudgetAmber,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // 6. Upcoming Recurring Expenses
        if (recurring.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Repeat, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = AppLocale.t("upcoming_expenses", language),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())
                    recurring.take(2).forEach { rec ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = rec.description,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Text(
                                    text = "Due ${sdf.format(Date(rec.nextOccurrence))} • ${rec.frequency}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = FinancialCalculationEngine.formatCurrency(rec.amountMinor, currency, hideAmounts),
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

@Composable
fun MonthMiniMetric(
    label: String,
    amount: String,
    color: Color,
    icon: ImageVector
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = amount,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun QuickActionButton(
    label: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    tag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .testTag(tag)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(containerColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = contentColor, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 10.sp),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
