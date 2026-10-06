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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import com.example.budgetai.data.model.SavingsGoal
import com.example.budgetai.domain.engine.FinancialCalculationEngine
import com.example.budgetai.localization.AppLocale
import com.example.ui.theme.BudgetAmber
import com.example.ui.theme.EmeraldPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun GoalsScreen(
    goals: List<SavingsGoal>,
    currency: String,
    language: String,
    hideAmounts: Boolean,
    onAddGoal: (name: String, targetMinor: Long, currentMinor: Long, targetDate: Long, categoryType: String) -> Unit,
    onContributeToGoal: (goal: SavingsGoal, addAmountMinor: Long) -> Unit
) {
    var showNewGoalDialog by remember { mutableStateOf(false) }
    var selectedGoalForDeposit by remember { mutableStateOf<SavingsGoal?>(null) }
    var depositAmountInput by remember { mutableStateOf("") }

    // New Goal State
    var newGoalName by remember { mutableStateOf("") }
    var newGoalTargetInput by remember { mutableStateOf("") }
    var newGoalCurrentInput by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("goals_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = AppLocale.t("savings_progress", language),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Target funds, emergency reserves, and long term plans.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (goals.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Flag,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = AppLocale.t("no_goals", language),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = AppLocale.t("no_goals_sub", language),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { showNewGoalDialog = true }) {
                            Text(AppLocale.t("add_goal", language))
                        }
                    }
                }
            } else {
                val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(goals, key = { it.id }) { goal ->
                        val pct = if (goal.targetAmountMinor > 0) {
                            ((goal.currentAmountMinor.toDouble() / goal.targetAmountMinor.toDouble()) * 100).toInt()
                        } else 0
                        val remainingMinor = (goal.targetAmountMinor - goal.currentAmountMinor).coerceAtLeast(0L)

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("goal_card_${goal.id}"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(BudgetAmber.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(imageVector = Icons.Default.Savings, contentDescription = null, tint = BudgetAmber, modifier = Modifier.size(20.dp))
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = goal.name,
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                            )
                                            Text(
                                                text = "Target Date: ${sdf.format(Date(goal.targetDate))}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Text(
                                        text = "$pct%",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                        color = if (pct >= 100) EmeraldPrimary else MaterialTheme.colorScheme.primary
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                LinearProgressIndicator(
                                    progress = { (pct / 100f).coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = if (pct >= 100) EmeraldPrimary else BudgetAmber,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Saved / Target",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "${FinancialCalculationEngine.formatCurrency(goal.currentAmountMinor, currency, hideAmounts)} of ${FinancialCalculationEngine.formatCurrency(goal.targetAmountMinor, currency, hideAmounts)}",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                        )
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            selectedGoalForDeposit = goal
                                            depositAmountInput = ""
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.testTag("add_funds_btn_${goal.id}")
                                    ) {
                                        Text(AppLocale.t("add_funds", language))
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = { showNewGoalDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("goals_fab"),
            containerColor = BudgetAmber,
            contentColor = Color.White
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Goal")
        }
    }

    // Add Funds Dialog
    if (selectedGoalForDeposit != null) {
        AlertDialog(
            onDismissRequest = { selectedGoalForDeposit = null },
            title = { Text("${AppLocale.t("add_funds", language)}: ${selectedGoalForDeposit?.name}") },
            text = {
                Column {
                    Text("Enter contribution amount:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = depositAmountInput,
                        onValueChange = { depositAmountInput = it },
                        label = { Text("Amount ($currency)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val addMinor = FinancialCalculationEngine.parseToMinorUnits(depositAmountInput)
                        if (addMinor > 0 && selectedGoalForDeposit != null) {
                            onContributeToGoal(selectedGoalForDeposit!!, addMinor)
                            selectedGoalForDeposit = null
                        }
                    }
                ) {
                    Text(AppLocale.t("save", language))
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedGoalForDeposit = null }) {
                    Text(AppLocale.t("cancel", language))
                }
            }
        )
    }

    // New Goal Dialog
    if (showNewGoalDialog) {
        AlertDialog(
            onDismissRequest = { showNewGoalDialog = false },
            title = { Text(AppLocale.t("add_goal", language)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newGoalName,
                        onValueChange = { newGoalName = it },
                        label = { Text(AppLocale.t("goal_name", language)) },
                        placeholder = { Text("e.g. Emergency Fund, Laptop") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newGoalTargetInput,
                        onValueChange = { newGoalTargetInput = it },
                        label = { Text(AppLocale.t("target_amount", language)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newGoalCurrentInput,
                        onValueChange = { newGoalCurrentInput = it },
                        label = { Text("Initial Amount (Optional)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val targetMinor = FinancialCalculationEngine.parseToMinorUnits(newGoalTargetInput)
                        val currentMinor = FinancialCalculationEngine.parseToMinorUnits(newGoalCurrentInput)
                        if (newGoalName.isNotBlank() && targetMinor > 0) {
                            val targetDate = System.currentTimeMillis() + (180L * 86400000L) // 6 months default
                            onAddGoal(newGoalName, targetMinor, currentMinor, targetDate, "CUSTOM")
                            showNewGoalDialog = false
                            newGoalName = ""
                            newGoalTargetInput = ""
                            newGoalCurrentInput = ""
                        }
                    }
                ) {
                    Text(AppLocale.t("save", language))
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewGoalDialog = false }) {
                    Text(AppLocale.t("cancel", language))
                }
            }
        )
    }
}
