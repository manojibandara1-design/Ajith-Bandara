package com.example.budgetai.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.budgetai.localization.AppLocale

@Composable
fun AppBottomBar(
    currentScreen: String,
    language: String,
    onNavigate: (String) -> Unit
) {
    val items = listOf(
        Triple("HOME", "nav_home", Icons.Default.Home),
        Triple("TRANSACTIONS", "nav_transactions", Icons.Default.ReceiptLong),
        Triple("BUDGET", "nav_budget", Icons.Default.AccountBalanceWallet),
        Triple("GOALS", "nav_goals", Icons.Default.Flag),
        Triple("AI", "nav_ai", Icons.Default.AutoAwesome)
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    ) {
        items.forEach { (screenKey, labelKey, icon) ->
            val isSelected = currentScreen == screenKey
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(screenKey) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = AppLocale.t(labelKey, language)
                    )
                },
                label = {
                    Text(
                        text = AppLocale.t(labelKey, language),
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.testTag("nav_item_${screenKey.lowercase()}")
            )
        }
    }
}
