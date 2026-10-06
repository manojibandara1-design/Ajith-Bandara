package com.example.budgetai.localization

object AppLocale {
    const val EN = "en"
    const val SI = "si"

    private val strings = mapOf(
        "app_name" to mapOf(
            EN to "BUDGET AI",
            SI to "BUDGET AI"
        ),
        "tagline" to mapOf(
            EN to "Smart Money. Better Decisions.",
            SI to "බුද්ධිමත් මූල්‍ය කළමනාකරණය. වඩා හොඳ තීරණ."
        ),
        "created_by" to mapOf(
            EN to "Created by Ajith Bandara",
            SI to "නිර්මාණය: අජිත් බණ්ඩාර"
        ),
        "nav_home" to mapOf(
            EN to "Home",
            SI to "මුල් පිටුව"
        ),
        "nav_transactions" to mapOf(
            EN to "Transactions",
            SI to "ගනුදෙනු"
        ),
        "nav_budget" to mapOf(
            EN to "Budget",
            SI to "අයවැය"
        ),
        "nav_goals" to mapOf(
            EN to "Goals",
            SI to "ඉලක්ක"
        ),
        "nav_ai" to mapOf(
            EN to "AI Assistant",
            SI to "AI සහයක"
        ),
        "nav_reports" to mapOf(
            EN to "Reports",
            SI to "වාර්තා"
        ),
        "nav_settings" to mapOf(
            EN to "Settings",
            SI to "සැකසුම්"
        ),
        "current_balance" to mapOf(
            EN to "Total Balance",
            SI to "මුළු ශේෂය"
        ),
        "this_month" to mapOf(
            EN to "This Month",
            SI to "මෙම මාසය"
        ),
        "income" to mapOf(
            EN to "Income",
            SI to "ආදායම"
        ),
        "expenses" to mapOf(
            EN to "Expenses",
            SI to "වියදම්"
        ),
        "remaining" to mapOf(
            EN to "Remaining",
            SI to "ඉතිරි මුදල"
        ),
        "budget_status" to mapOf(
            EN to "Budget Status",
            SI to "අයවැය තත්ත්වය"
        ),
        "savings_progress" to mapOf(
            EN to "Savings Goals",
            SI to "ඉතිරිකිරීමේ ඉලක්ක"
        ),
        "upcoming_expenses" to mapOf(
            EN to "Upcoming Recurring",
            SI to "ඉදිරි පුනරාවර්තී වියදම්"
        ),
        "quick_actions" to mapOf(
            EN to "Quick Actions",
            SI to "කඩිනම් ක්‍රියා"
        ),
        "add_income" to mapOf(
            EN to "+ Income",
            SI to "+ ආදායම"
        ),
        "add_expense" to mapOf(
            EN to "+ Expense",
            SI to "+ වියදම"
        ),
        "transfer" to mapOf(
            EN to "Transfer",
            SI to "මාරු කිරීම"
        ),
        "set_budget" to mapOf(
            EN to "Set Budget",
            SI to "අයවැයක් සාදන්න"
        ),
        "add_goal" to mapOf(
            EN to "Add Goal",
            SI to "ඉලක්කයක් එක්කරන්න"
        ),
        "financial_health" to mapOf(
            EN to "Financial Health",
            SI to "මූල්‍ය සෞඛ්‍යය"
        ),
        "health_score" to mapOf(
            EN to "Score",
            SI to "ලකුණු"
        ),
        "health_status_healthy" to mapOf(
            EN to "Healthy & Balanced",
            SI to "සතුටුදායක සහ සමබරයි"
        ),
        "health_status_good" to mapOf(
            EN to "Good Progress",
            SI to "යහපත් මට්ටමක පවතී"
        ),
        "health_status_caution" to mapOf(
            EN to "Budget Caution",
            SI to "වියදම් පාලනය අවශ්‍යයි"
        ),
        "health_status_warning" to mapOf(
            EN to "Overspending Alert",
            SI to "අයවැය ඉක්මවා යාමක්"
        ),
        "safe_daily_spend" to mapOf(
            EN to "Safe Daily Spending Limit",
            SI to "දිනකට වියදම් කළ හැකි උපරිමය"
        ),
        "days_remaining" to mapOf(
            EN to "Days left in month",
            SI to "මාසයේ ඉතිරි දින"
        ),
        "budget_used" to mapOf(
            EN to "Used",
            SI to "භාවිත කළ"
        ),
        "budget_remaining" to mapOf(
            EN to "Remaining",
            SI to "ඉතිරි"
        ),
        "search_transactions" to mapOf(
            EN to "Search transactions...",
            SI to "ගනුදෙනු සොයන්න..."
        ),
        "filter_all" to mapOf(
            EN to "All",
            SI to "සියල්ල"
        ),
        "no_transactions" to mapOf(
            EN to "No transactions recorded yet.",
            SI to "තවමත් ගනුදෙනු සටහන් කර නොමැත."
        ),
        "no_transactions_sub" to mapOf(
            EN to "Tap '+ Expense' or '+ Income' to add your first transaction.",
            SI to "පළමු ගනුදෙනුව ඇතුළත් කිරීමට '+ වියදම' හෝ '+ ආදායම' තට්ටු කරන්න."
        ),
        "no_budgets" to mapOf(
            EN to "No budget set for this month.",
            SI to "මෙම මාසය සඳහා තවමත් අයවැයක් සකසා නැත."
        ),
        "no_budgets_sub" to mapOf(
            EN to "Create a monthly budget to control your spending effectively.",
            SI to "වියදම් පාලනය සඳහා මාසික අයවැයක් සකසන්න."
        ),
        "no_goals" to mapOf(
            EN to "No savings goals created.",
            SI to "ඉතිරිකිරීමේ ඉලක්ක නොමැත."
        ),
        "no_goals_sub" to mapOf(
            EN to "Set targets for emergency fund, vehicle, home, or education.",
            SI to "හදිසි අරමුදලක්, වාහනයක් හෝ අධ්‍යාපනයක් සඳහා ඉලක්කයක් එක්කරන්න."
        ),
        "ai_assistant_title" to mapOf(
            EN to "AI Financial Assistant",
            SI to "AI මූල්‍ය සහයක"
        ),
        "ai_assistant_sub" to mapOf(
            EN to "Deterministic calculations powered by smart natural language insights.",
            SI to "නිරවද්‍ය ගණනය කිරීම් සහ බුද්ධිමත් විශ්ලේෂණය."
        ),
        "ai_chat_placeholder" to mapOf(
            EN to "Ask a question or enter transaction...",
            SI to "ප්‍රශ්නයක් අසන්න හෝ ගනුදෙනුවක් ලියන්න..."
        ),
        "ai_nl_title" to mapOf(
            EN to "Quick Natural Language Entry",
            SI to "ස්වාභාවික බසින් ගනුදෙනු ඇතුළත් කිරීම"
        ),
        "ai_nl_example" to mapOf(
            EN to "e.g., 'Spent 1500 for groceries yesterday' or 'Received 5000 freelance'",
            SI to "උදා: 'ඊයේ කෑම වලට 1500 ක් ගියා' හෝ 'ෆ්‍රීලාන්ස් 50000 ලැබුණා'"
        ),
        "parse_and_add" to mapOf(
            EN to "Parse & Add",
            SI to "විශ්ලේෂණය කර එක්කරන්න"
        ),
        "confirm_transaction" to mapOf(
            EN to "Confirm & Save",
            SI to "තහවුරු කර සුරකින්න"
        ),
        "cancel" to mapOf(
            EN to "Cancel",
            SI to "අවලංගු කරන්න"
        ),
        "save" to mapOf(
            EN to "Save",
            SI to "සුරකින්න"
        ),
        "amount" to mapOf(
            EN to "Amount",
            SI to "මුදල"
        ),
        "category" to mapOf(
            EN to "Category",
            SI to "කාණ්ඩය"
        ),
        "account" to mapOf(
            EN to "Account",
            SI to "ගිණුම"
        ),
        "date" to mapOf(
            EN to "Date",
            SI to "දිනය"
        ),
        "note" to mapOf(
            EN to "Note (Optional)",
            SI to "සටහන (විකල්ප)"
        ),
        "recurring" to mapOf(
            EN to "Recurring Transaction",
            SI to "පුනරාවර්තී ගනුදෙනුවක්"
        ),
        "security_pin" to mapOf(
            EN to "App PIN Lock",
            SI to "PIN ආරක්ෂාව"
        ),
        "pin_enter" to mapOf(
            EN to "Enter 4-digit PIN",
            SI to "ඉලක්කම් 4ක PIN අංකය ඇතුළත් කරන්න"
        ),
        "pin_create" to mapOf(
            EN to "Create 4-digit PIN",
            SI to "නව PIN අංකයක් සකසන්න"
        ),
        "pin_confirm" to mapOf(
            EN to "Confirm 4-digit PIN",
            SI to "PIN අංකය තහවුරු කරන්න"
        ),
        "pin_incorrect" to mapOf(
            EN to "Incorrect PIN. Try again.",
            SI to "PIN අංකය වැරදියි. නැවත උත්සාහ කරන්න."
        ),
        "pin_mismatch" to mapOf(
            EN to "PINs do not match.",
            SI to "PIN අංක සමාන නොවේ."
        ),
        "hide_amounts" to mapOf(
            EN to "Hide Financial Amounts",
            SI to "මුදල් ප්‍රමාණ සඟවන්න"
        ),
        "backup_and_restore" to mapOf(
            EN to "Backup & Multi-Device Transfer",
            SI to "උපාංග මාරුව සහ දත්ත උපස්ථය"
        ),
        "export_backup" to mapOf(
            EN to "Export Encrypted Backup",
            SI to "උපස්ථය අපනයනය (Export)"
        ),
        "import_backup" to mapOf(
            EN to "Restore Backup",
            SI to "උපස්ථය ප්‍රතිස්ථාපනය (Restore)"
        ),
        "demo_data" to mapOf(
            EN to "Load Realistic Demo Data",
            SI to "ආදර්ශ දත්ත ඇතුළත් කරන්න"
        ),
        "clear_data" to mapOf(
            EN to "Reset All Data",
            SI to "සියලු දත්ත මකන්න"
        ),
        "language_select" to mapOf(
            EN to "Language",
            SI to "භාෂාව"
        ),
        "currency_select" to mapOf(
            EN to "Currency",
            SI to "මුදල් ඒකකය"
        ),
        "privacy_policy" to mapOf(
            EN to "Privacy & Data Policy",
            SI to "පෞද්ගලිකත්ව ප්‍රතිපත්තිය"
        ),
        "about_app" to mapOf(
            EN to "About BUDGET AI",
            SI to "BUDGET AI ගැන"
        ),
        "copyright" to mapOf(
            EN to "© Ajith Bandara. All rights reserved.",
            SI to "© අජිත් බණ්ඩාර. සියලු හිමිකම් ඇවිරිණි."
        ),
        "get_started" to mapOf(
            EN to "Get Started",
            SI to "ආරම්භ කරන්න"
        ),
        "restore_existing" to mapOf(
            EN to "Restore Backup",
            SI to "උපස්ථයකින් ලබාගන්න"
        ),
        "spending_by_category" to mapOf(
            EN to "Spending by Category",
            SI to "කාණ්ඩ අනුව වියදම්"
        ),
        "income_vs_expense" to mapOf(
            EN to "Income vs Expenses",
            SI to "ආදායම සහ වියදම සංසන්දනය"
        ),
        "top_expenses" to mapOf(
            EN to "Top Expenses",
            SI to "ප්‍රධාන වියදම්"
        ),
        "target_amount" to mapOf(
            EN to "Target Amount",
            SI to "ඉලක්කගත මුදල"
        ),
        "current_saved" to mapOf(
            EN to "Saved So Far",
            SI to "දැනට ඉතිරි කළ මුදල"
        ),
        "add_funds" to mapOf(
            EN to "Add Funds",
            SI to "මුදල් එක්කරන්න"
        ),
        "goal_name" to mapOf(
            EN to "Goal Name",
            SI to "ඉලක්කයේ නම"
        ),
        "monthly_budget" to mapOf(
            EN to "Monthly Budget",
            SI to "මාසික අයවැය"
        ),
        "category_budget" to mapOf(
            EN to "Category Budgets",
            SI to "කාණ්ඩ අයවැය"
        ),
        "offline_ready" to mapOf(
            EN to "100% Offline Capable",
            SI to "100% අන්තර්ජාලය රහිතව ක්‍රියා කරයි"
        )
    )

    fun t(key: String, lang: String = EN): String {
        val entry = strings[key] ?: return key
        return entry[lang] ?: entry[EN] ?: key
    }
}
