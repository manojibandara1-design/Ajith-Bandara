package com.example.budgetai.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.budgetai.data.model.AppSettings
import com.example.budgetai.localization.AppLocale
import com.example.ui.theme.ExpenseRose

@Composable
fun SettingsScreen(
    settings: AppSettings,
    onLanguageChange: (String) -> Unit,
    onCurrencyChange: (String) -> Unit,
    onToggleHideAmounts: () -> Unit,
    onEnablePin: (String) -> Unit,
    onDisablePin: () -> Unit,
    onExportBackup: ((String) -> Unit) -> Unit,
    onRestoreBackup: (String, (Boolean, String) -> Unit) -> Unit,
    onLoadDemoData: () -> Unit,
    onResetAllData: () -> Unit
) {
    val context = LocalContext.current

    var showPinDialog by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("") }

    var showExportDialog by remember { mutableStateOf(false) }
    var exportedJsonText by remember { mutableStateOf("") }

    var showImportDialog by remember { mutableStateOf(false) }
    var importJsonInput by remember { mutableStateOf("") }
    var importStatusMsg by remember { mutableStateOf<String?>(null) }

    var showResetConfirmDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("settings_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = AppLocale.t("nav_settings", settings.language),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )

        // 1. Language & Currency Card
        SettingsGroupCard(title = "Preferences") {
            // Language selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Language, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = AppLocale.t("language_select", settings.language), style = MaterialTheme.typography.bodyMedium)
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(2.dp)
                ) {
                    listOf("en" to "English", "si" to "සිංහල").forEach { (code, label) ->
                        val isSelected = settings.language == code
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .clickable { onLanguageChange(code) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("settings_lang_$code"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Currency selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Payments, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = AppLocale.t("currency_select", settings.language), style = MaterialTheme.typography.bodyMedium)
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(2.dp)
                ) {
                    listOf("LKR", "USD", "EUR", "GBP").forEach { curr ->
                        val isSelected = settings.currency == curr
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .clickable { onCurrencyChange(curr) }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                                .testTag("settings_curr_$curr"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = curr,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // 2. Security & Privacy Card
        SettingsGroupCard(title = "Security & Privacy") {
            // PIN Lock Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(text = AppLocale.t("security_pin", settings.language), style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = if (settings.isPinEnabled) "PIN active" else "Not configured",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Switch(
                    checked = settings.isPinEnabled,
                    onCheckedChange = { enable ->
                        if (enable) {
                            pinInput = ""
                            showPinDialog = true
                        } else {
                            onDisablePin()
                        }
                    },
                    modifier = Modifier.testTag("settings_pin_switch")
                )
            }

            // Hide Amounts Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Visibility, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = AppLocale.t("hide_amounts", settings.language), style = MaterialTheme.typography.bodyMedium)
                }

                Switch(
                    checked = settings.hideAmounts,
                    onCheckedChange = { onToggleHideAmounts() },
                    modifier = Modifier.testTag("settings_hide_amounts_switch")
                )
            }
        }

        // 3. Data Management & Multi-Device Card
        SettingsGroupCard(title = AppLocale.t("backup_and_restore", settings.language)) {
            // Export Backup
            SettingsActionRow(
                icon = Icons.Default.CloudUpload,
                title = AppLocale.t("export_backup", settings.language),
                subtitle = "Save encrypted JSON for phone migration",
                tag = "settings_export_backup",
                onClick = {
                    onExportBackup { json ->
                        exportedJsonText = json
                        showExportDialog = true
                    }
                }
            )

            // Import Backup
            SettingsActionRow(
                icon = Icons.Default.CloudDownload,
                title = AppLocale.t("import_backup", settings.language),
                subtitle = "Restore data from another device",
                tag = "settings_import_backup",
                onClick = {
                    importJsonInput = ""
                    importStatusMsg = null
                    showImportDialog = true
                }
            )

            // Load Demo Data
            SettingsActionRow(
                icon = Icons.Default.PlayArrow,
                title = AppLocale.t("demo_data", settings.language),
                subtitle = "Load sample income 150k, expenses 87.5k",
                tag = "settings_load_demo",
                onClick = onLoadDemoData
            )

            // Clear All Data
            SettingsActionRow(
                icon = Icons.Default.DeleteForever,
                title = AppLocale.t("clear_data", settings.language),
                subtitle = "Permanently remove all local records",
                tint = ExpenseRose,
                tag = "settings_clear_data",
                onClick = { showResetConfirmDialog = true }
            )
        }

        // 4. Privacy & About
        SettingsGroupCard(title = "Information") {
            SettingsActionRow(
                icon = Icons.Default.PrivacyTip,
                title = AppLocale.t("privacy_policy", settings.language),
                subtitle = "Local-first architecture details",
                tag = "settings_privacy",
                onClick = { showPrivacyDialog = true }
            )

            SettingsActionRow(
                icon = Icons.Default.Info,
                title = AppLocale.t("about_app", settings.language),
                subtitle = "Attribution & software version",
                tag = "settings_about",
                onClick = { showAboutDialog = true }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    // PIN Setup Dialog
    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text(AppLocale.t("pin_create", settings.language)) },
            text = {
                Column {
                    Text("Enter a 4-digit PIN for device security:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { if (it.length <= 4) pinInput = it },
                        label = { Text("4-digit PIN") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pinInput.length == 4) {
                            onEnablePin(pinInput)
                            showPinDialog = false
                        }
                    }
                ) {
                    Text(AppLocale.t("save", settings.language))
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false }) {
                    Text(AppLocale.t("cancel", settings.language))
                }
            }
        )
    }

    // Export Dialog
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text(AppLocale.t("export_backup", settings.language)) },
            text = {
                Column {
                    Text(
                        text = "Encrypted backup JSON generated. Copy this to import into another phone:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = exportedJsonText,
                        onValueChange = {},
                        readOnly = true,
                        maxLines = 8,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("BUDGET_AI_BACKUP", exportedJsonText)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Backup copied to clipboard!", Toast.LENGTH_SHORT).show()
                        showExportDialog = false
                    }
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy to Clipboard")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Import Dialog
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text(AppLocale.t("import_backup", settings.language)) },
            text = {
                Column {
                    Text(
                        text = "Paste backup JSON from your other phone:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = importJsonInput,
                        onValueChange = { importJsonInput = it },
                        placeholder = { Text("Paste JSON here...") },
                        maxLines = 8,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (importStatusMsg != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = importStatusMsg!!, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (importJsonInput.isNotBlank()) {
                            onRestoreBackup(importJsonInput) { success, msg ->
                                importStatusMsg = msg
                                if (success) {
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    showImportDialog = false
                                }
                            }
                        }
                    }
                ) {
                    Text("Validate & Restore")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text(AppLocale.t("cancel", settings.language))
                }
            }
        )
    }

    // Reset Confirm Dialog
    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = { Text("Reset All Data?") },
            text = {
                Text("This action will erase all accounts, transactions, budgets, and savings goals from this device. Are you sure?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onResetAllData()
                        showResetConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRose)
                ) {
                    Text("Erase Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text(AppLocale.t("cancel", settings.language))
                }
            }
        )
    }

    // Privacy Dialog
    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text(AppLocale.t("privacy_policy", settings.language)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("• Local-First: All financial data is stored securely in SQLite Room database on your device.", style = MaterialTheme.typography.bodySmall)
                    Text("• Zero Tracker Policy: No third-party behavioral trackers or ad telemetry.", style = MaterialTheme.typography.bodySmall)
                    Text("• AI Minimization: Only anonymized spending aggregates are processed for AI answers.", style = MaterialTheme.typography.bodySmall)
                    Text("• Device Portability: Data can be transferred to any other phone via encrypted backup.", style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) {
                    Text("Got it")
                }
            }
        )
    }

    // About Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("BUDGET AI") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Smart Money. Better Decisions.", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Created by Ajith Bandara", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.primary)
                    Text("© Ajith Bandara. All rights reserved.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Proof of Concept (POC) - Mobile-First Personal Financial Architecture.", style = MaterialTheme.typography.labelSmall)
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun SettingsGroupCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
fun SettingsActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    tag: String,
    tint: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 10.dp)
            .testTag(tag),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = tint)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                Text(text = subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
