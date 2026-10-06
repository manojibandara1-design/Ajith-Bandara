package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.budgetai.ui.components.AddTransactionSheet
import com.example.budgetai.ui.components.AppBottomBar
import com.example.budgetai.ui.components.AppTopBar
import com.example.budgetai.ui.components.PinLockScreen
import com.example.budgetai.ui.screens.AiScreen
import com.example.budgetai.ui.screens.BudgetScreen
import com.example.budgetai.ui.screens.GoalsScreen
import com.example.budgetai.ui.screens.HomeScreen
import com.example.budgetai.ui.screens.OnboardingScreen
import com.example.budgetai.ui.screens.ReportsScreen
import com.example.budgetai.ui.screens.SettingsScreen
import com.example.budgetai.ui.screens.SplashScreen
import com.example.budgetai.ui.screens.TransactionsScreen
import com.example.budgetai.ui.viewmodel.BudgetViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                BudgetAiApp()
            }
        }
    }
}

@Composable
fun BudgetAiApp(viewModel: BudgetViewModel = viewModel()) {
    val context = LocalContext.current

    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val isPinLocked by viewModel.isPinLocked.collectAsStateWithLifecycle()

    val transactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    val accounts by viewModel.allAccounts.collectAsStateWithLifecycle()
    val categories by viewModel.allCategories.collectAsStateWithLifecycle()
    val budgets by viewModel.allBudgets.collectAsStateWithLifecycle()
    val goals by viewModel.allGoals.collectAsStateWithLifecycle()
    val recurring by viewModel.allRecurring.collectAsStateWithLifecycle()

    val showAddSheet by viewModel.showAddSheet.collectAsStateWithLifecycle()
    val addSheetType by viewModel.addSheetType.collectAsStateWithLifecycle()
    val aiMessages by viewModel.aiMessages.collectAsStateWithLifecycle()
    val pendingNlDraft by viewModel.pendingNlDraft.collectAsStateWithLifecycle()
    val systemNotice by viewModel.systemNotice.collectAsStateWithLifecycle()

    // Handle Toast Notices
    LaunchedEffect(systemNotice) {
        systemNotice?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearSystemNotice()
        }
    }

    // Back button behavior
    BackHandler(enabled = currentScreen != "HOME" && currentScreen != "SPLASH" && currentScreen != "ONBOARDING") {
        viewModel.navigateTo("HOME")
    }

    // PIN Lock Gate
    if (isPinLocked) {
        PinLockScreen(
            language = settings.language,
            onVerifyPin = { pin -> viewModel.unlockWithPin(pin) },
            onUnlockSuccess = { /* Unlocked automatically in VM */ }
        )
        return
    }

    // Splash Screen
    if (currentScreen == "SPLASH") {
        SplashScreen(
            language = settings.language,
            isOnboardingCompleted = settings.isOnboardingCompleted,
            onNavigateNext = { next -> viewModel.navigateTo(next) }
        )
        return
    }

    // Onboarding Screen
    if (currentScreen == "ONBOARDING") {
        OnboardingScreen(
            language = settings.language,
            onLanguageChange = { lang -> viewModel.setLanguage(lang) },
            onGetStarted = { viewModel.completeOnboarding() },
            onRestoreBackup = {
                viewModel.completeOnboarding()
                viewModel.navigateTo("SETTINGS")
            },
            onLoadDemoData = {
                viewModel.loadDemoData()
                viewModel.completeOnboarding()
            }
        )
        return
    }

    // Main Scaffold
    Scaffold(
        topBar = {
            AppTopBar(
                currentScreen = currentScreen,
                language = settings.language,
                hideAmounts = settings.hideAmounts,
                isPinEnabled = settings.isPinEnabled,
                onLanguageToggle = {
                    val nextLang = if (settings.language == "en") "si" else "en"
                    viewModel.setLanguage(nextLang)
                },
                onHideAmountsToggle = { viewModel.toggleHideAmounts() },
                onLockApp = { viewModel.lockApp() },
                onOpenReports = {
                    if (currentScreen == "REPORTS") viewModel.navigateTo("HOME")
                    else viewModel.navigateTo("REPORTS")
                },
                onOpenSettings = {
                    if (currentScreen == "SETTINGS") viewModel.navigateTo("HOME")
                    else viewModel.navigateTo("SETTINGS")
                }
            )
        },
        bottomBar = {
            AppBottomBar(
                currentScreen = currentScreen,
                language = settings.language,
                onNavigate = { screen -> viewModel.navigateTo(screen) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                "HOME" -> {
                    HomeScreen(
                        accounts = accounts,
                        transactions = transactions,
                        budgets = budgets,
                        goals = goals,
                        recurring = recurring,
                        currency = settings.currency,
                        language = settings.language,
                        hideAmounts = settings.hideAmounts,
                        onQuickAdd = { type -> viewModel.openAddSheet(type) },
                        onNavigate = { screen -> viewModel.navigateTo(screen) }
                    )
                }
                "TRANSACTIONS" -> {
                    TransactionsScreen(
                        transactions = transactions,
                        categories = categories,
                        accounts = accounts,
                        currency = settings.currency,
                        language = settings.language,
                        hideAmounts = settings.hideAmounts,
                        onDeleteTransaction = { id -> viewModel.deleteTransaction(id) },
                        onAddTransaction = { type -> viewModel.openAddSheet(type) }
                    )
                }
                "BUDGET" -> {
                    BudgetScreen(
                        budgets = budgets,
                        transactions = transactions,
                        categories = categories,
                        currency = settings.currency,
                        language = settings.language,
                        hideAmounts = settings.hideAmounts,
                        onSetMonthlyBudget = { amountMinor -> viewModel.setMonthlyBudget(amountMinor) }
                    )
                }
                "GOALS" -> {
                    GoalsScreen(
                        goals = goals,
                        currency = settings.currency,
                        language = settings.language,
                        hideAmounts = settings.hideAmounts,
                        onAddGoal = { name, targetMinor, currentMinor, targetDate, categoryType ->
                            viewModel.addSavingsGoal(name, targetMinor, currentMinor, targetDate, categoryType)
                        },
                        onContributeToGoal = { goal, addMinor ->
                            viewModel.contributeToGoal(goal, addMinor)
                        }
                    )
                }
                "AI" -> {
                    AiScreen(
                        messages = aiMessages,
                        pendingDraft = pendingNlDraft,
                        currency = settings.currency,
                        language = settings.language,
                        onSendMessage = { prompt -> viewModel.sendAiMessage(prompt) },
                        onConfirmDraft = { draft -> viewModel.confirmNlDraft(draft) },
                        onDismissDraft = { viewModel.dismissNlDraft() }
                    )
                }
                "REPORTS" -> {
                    ReportsScreen(
                        transactions = transactions,
                        categories = categories,
                        currency = settings.currency,
                        language = settings.language,
                        hideAmounts = settings.hideAmounts
                    )
                }
                "SETTINGS" -> {
                    SettingsScreen(
                        settings = settings,
                        onLanguageChange = { lang -> viewModel.setLanguage(lang) },
                        onCurrencyChange = { curr -> viewModel.setCurrency(curr) },
                        onToggleHideAmounts = { viewModel.toggleHideAmounts() },
                        onEnablePin = { pin -> viewModel.enablePin(pin) },
                        onDisablePin = { viewModel.disablePin() },
                        onExportBackup = { callback -> viewModel.exportBackup(callback) },
                        onRestoreBackup = { json, callback -> viewModel.restoreBackup(json, callback) },
                        onLoadDemoData = { viewModel.loadDemoData() },
                        onResetAllData = { viewModel.resetAllData() }
                    )
                }
            }
        }

        // Quick Add Transaction Modal Sheet
        if (showAddSheet) {
            AddTransactionSheet(
                initialType = addSheetType,
                categories = categories,
                accounts = accounts,
                currency = settings.currency,
                language = settings.language,
                onDismiss = { viewModel.closeAddSheet() },
                onSave = { type, amountMinor, categoryId, accountId, date, description, note, paymentMethod, isRecurring ->
                    viewModel.addTransaction(
                        type = type,
                        amountMinor = amountMinor,
                        categoryId = categoryId,
                        accountId = accountId,
                        date = date,
                        description = description,
                        note = note,
                        paymentMethod = paymentMethod,
                        isRecurring = isRecurring
                    )
                }
            )
        }
    }
}
