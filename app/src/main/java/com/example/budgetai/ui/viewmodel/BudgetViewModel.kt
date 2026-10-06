package com.example.budgetai.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgetai.data.model.Account
import com.example.budgetai.data.model.AppSettings
import com.example.budgetai.data.model.Budget
import com.example.budgetai.data.model.Category
import com.example.budgetai.data.model.RecurringTransaction
import com.example.budgetai.data.model.SavingsGoal
import com.example.budgetai.data.model.Transaction
import com.example.budgetai.data.repository.BudgetRepository
import com.example.budgetai.domain.ai.AiAssistantService
import com.example.budgetai.domain.ai.AiChatMessage
import com.example.budgetai.domain.ai.ParsedTransactionDraft
import com.example.budgetai.domain.engine.FinancialCalculationEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class BudgetViewModel(application: Application) : AndroidViewModel(application) {
    val repository = BudgetRepository(application)

    val settings: StateFlow<AppSettings> = repository.settingsState

    val allTransactions: StateFlow<List<Transaction>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAccounts: StateFlow<List<Account>> = repository.allAccounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCategories: StateFlow<List<Category>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBudgets: StateFlow<List<Budget>> = repository.allBudgets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allGoals: StateFlow<List<SavingsGoal>> = repository.allGoals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRecurring: StateFlow<List<RecurringTransaction>> = repository.allRecurring
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentScreen = MutableStateFlow("SPLASH")
    val currentScreen = _currentScreen.asStateFlow()

    private val _isPinLocked = MutableStateFlow(false)
    val isPinLocked = _isPinLocked.asStateFlow()

    // Quick Add Transaction Sheet state
    private val _showAddSheet = MutableStateFlow(false)
    val showAddSheet = _showAddSheet.asStateFlow()
    private val _addSheetType = MutableStateFlow("EXPENSE")
    val addSheetType = _addSheetType.asStateFlow()

    // AI Chat state
    private val _aiMessages = MutableStateFlow<List<AiChatMessage>>(emptyList())
    val aiMessages = _aiMessages.asStateFlow()

    private val _pendingNlDraft = MutableStateFlow<ParsedTransactionDraft?>(null)
    val pendingNlDraft = _pendingNlDraft.asStateFlow()

    // Backup & Restore Status Message
    private val _systemNotice = MutableStateFlow<String?>(null)
    val systemNotice = _systemNotice.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initializeDefaultDataIfEmpty()
            if (settings.value.isPinEnabled && settings.value.pinHash.isNotEmpty()) {
                _isPinLocked.value = true
            }
            initAiWelcomeMessage()
        }
    }

    private fun initAiWelcomeMessage() {
        val welcome = if (settings.value.language == "si") {
            "ආයුබෝවන්! මම BUDGET AI මූල්‍ය සහයක. ඔබගේ මුදල් පිළිබඳ ප්‍රශ්න අසන්න, නැතහොත් 'ඊයේ කෑම වලට 1500ක් ගියා' වැනි ඕනෑම වියදමක් මෙහි ලියා සෘජුවම එකතු කරන්න."
        } else {
            "Hello! I am your BUDGET AI Assistant. Ask any question about your spending, or type a transaction like 'Spent 2500 on fuel yesterday' to log it instantly."
        }
        _aiMessages.value = listOf(
            AiChatMessage(isUser = false, content = welcome)
        )
    }

    fun navigateTo(screen: String) {
        _currentScreen.value = screen
    }

    fun openAddSheet(type: String = "EXPENSE") {
        _addSheetType.value = type
        _showAddSheet.value = true
    }

    fun closeAddSheet() {
        _showAddSheet.value = false
    }

    fun unlockWithPin(pin: String): Boolean {
        if (repository.verifyPin(pin)) {
            _isPinLocked.value = false
            return true
        }
        return false
    }

    fun lockApp() {
        if (settings.value.isPinEnabled) {
            _isPinLocked.value = true
        }
    }

    fun setLanguage(lang: String) {
        repository.updateSettings(settings.value.copy(language = lang))
        initAiWelcomeMessage()
    }

    fun setCurrency(currency: String) {
        repository.updateSettings(settings.value.copy(currency = currency))
    }

    fun toggleHideAmounts() {
        repository.updateSettings(settings.value.copy(hideAmounts = !settings.value.hideAmounts))
    }

    fun enablePin(pin: String) {
        val hash = repository.hashPin(pin)
        repository.updateSettings(settings.value.copy(isPinEnabled = true, pinHash = hash))
        _isPinLocked.value = false
    }

    fun disablePin() {
        repository.updateSettings(settings.value.copy(isPinEnabled = false, pinHash = ""))
        _isPinLocked.value = false
    }

    fun completeOnboarding() {
        repository.updateSettings(settings.value.copy(isOnboardingCompleted = true))
        _currentScreen.value = "HOME"
    }

    fun addTransaction(
        type: String,
        amountMinor: Long,
        categoryId: String,
        accountId: String,
        date: Long,
        description: String,
        note: String,
        paymentMethod: String,
        isRecurring: Boolean
    ) {
        viewModelScope.launch {
            val tx = Transaction(
                accountId = accountId,
                categoryId = categoryId,
                type = type,
                amountMinor = amountMinor,
                date = date,
                description = description.ifBlank { if (type == "INCOME") "Income" else "Expense" },
                note = note,
                paymentMethod = paymentMethod,
                isRecurring = isRecurring
            )
            repository.insertTransaction(tx)
            _showAddSheet.value = false
            _pendingNlDraft.value = null
        }
    }

    fun deleteTransaction(id: String) {
        viewModelScope.launch {
            repository.deleteTransaction(id)
        }
    }

    fun setMonthlyBudget(amountMinor: Long) {
        viewModelScope.launch {
            val cal = Calendar.getInstance()
            val budget = Budget(
                categoryId = null,
                amountMinor = amountMinor,
                month = cal.get(Calendar.MONTH) + 1,
                year = cal.get(Calendar.YEAR)
            )
            repository.insertBudget(budget)
        }
    }

    fun addSavingsGoal(name: String, targetAmountMinor: Long, currentAmountMinor: Long, targetDate: Long, categoryType: String) {
        viewModelScope.launch {
            val goal = SavingsGoal(
                name = name,
                targetAmountMinor = targetAmountMinor,
                currentAmountMinor = currentAmountMinor,
                targetDate = targetDate,
                categoryType = categoryType
            )
            repository.insertGoal(goal)
        }
    }

    fun contributeToGoal(goal: SavingsGoal, addAmountMinor: Long) {
        viewModelScope.launch {
            val updated = goal.copy(currentAmountMinor = goal.currentAmountMinor + addAmountMinor)
            repository.updateGoal(updated)
        }
    }

    fun addCategory(name: String, type: String, colorHex: String, iconName: String) {
        viewModelScope.launch {
            val cat = Category(name = name, type = type, colorHex = colorHex, iconName = iconName)
            repository.insertCategory(cat)
        }
    }

    fun sendAiMessage(text: String) {
        if (text.isBlank()) return
        val userMsg = AiChatMessage(isUser = true, content = text)
        _aiMessages.value = _aiMessages.value + userMsg

        // Check if natural language transaction entry
        val draft = AiAssistantService.parseNaturalLanguageTransaction(text, allCategories.value)
        if (draft != null) {
            _pendingNlDraft.value = draft
            val formatted = FinancialCalculationEngine.formatCurrency(draft.amountMinor, settings.value.currency)
            val replyText = if (settings.value.language == "si") {
                "හඳුනාගත් ගනුදෙනුව:\n• වර්ගය: ${draft.type}\n• මුදල: $formatted\n• කාණ්ඩය: ${draft.categoryName}\nකරුණාකර තහවුරු කර සුරකින්න."
            } else {
                "Detected Transaction:\n• Type: ${draft.type}\n• Amount: $formatted\n• Category: ${draft.categoryName}\nPlease confirm below to save."
            }
            _aiMessages.value = _aiMessages.value + AiChatMessage(
                isUser = false,
                content = replyText,
                actionDraft = draft
            )
        } else {
            // General query
            val answer = AiAssistantService.answerUserQuestion(
                question = text,
                lang = settings.value.language,
                currency = settings.value.currency,
                accounts = allAccounts.value,
                transactions = allTransactions.value,
                budgets = allBudgets.value,
                goals = allGoals.value,
                recurring = allRecurring.value
            )
            _aiMessages.value = _aiMessages.value + AiChatMessage(isUser = false, content = answer)
        }
    }

    fun confirmNlDraft(draft: ParsedTransactionDraft) {
        viewModelScope.launch {
            val defaultAcc = allAccounts.value.firstOrNull()?.id ?: ""
            val defaultCat = draft.categoryId ?: allCategories.value.firstOrNull()?.id ?: ""
            addTransaction(
                type = draft.type,
                amountMinor = draft.amountMinor,
                categoryId = defaultCat,
                accountId = defaultAcc,
                date = draft.dateEpoch,
                description = draft.description,
                note = "AI entry: ${draft.rawPrompt}",
                paymentMethod = "Cash",
                isRecurring = false
            )
            _pendingNlDraft.value = null
            _systemNotice.value = "Transaction saved successfully!"
        }
    }

    fun dismissNlDraft() {
        _pendingNlDraft.value = null
    }

    fun loadDemoData() {
        viewModelScope.launch {
            repository.loadDemoData()
            _systemNotice.value = "Demo data loaded successfully!"
        }
    }

    fun resetAllData() {
        viewModelScope.launch {
            repository.clearAllDatabaseData()
            repository.initializeDefaultDataIfEmpty()
            _systemNotice.value = "All data cleared successfully."
        }
    }

    fun exportBackup(onExported: (String) -> Unit) {
        viewModelScope.launch {
            val json = repository.exportBackupJson()
            onExported(json)
        }
    }

    fun restoreBackup(json: String, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val res = repository.restoreBackupJson(json)
            if (res.isSuccess) {
                onComplete(true, res.getOrDefault("Restored successfully!"))
            } else {
                onComplete(false, res.exceptionOrNull()?.message ?: "Failed to restore backup")
            }
        }
    }

    fun clearSystemNotice() {
        _systemNotice.value = null
    }
}
