package com.example.budgetai.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.budgetai.localization.AppLocale

@Composable
fun AppTopBar(
    currentScreen: String,
    language: String,
    hideAmounts: Boolean,
    isPinEnabled: Boolean,
    onLanguageToggle: () -> Unit,
    onHideAmountsToggle: () -> Unit,
    onLockApp: () -> Unit,
    onOpenReports: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // App Branding & Attribution
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "BUDGET AI",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "POC",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Text(
                    text = AppLocale.t("created_by", language),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Quick Actions: Reports, Language Switch, Privacy Eye, Lock, Settings
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Reports shortcut
                IconButton(
                    onClick = onOpenReports,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("topbar_reports_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = "Reports",
                        tint = if (currentScreen == "REPORTS") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Hide Amounts Toggle
                IconButton(
                    onClick = onHideAmountsToggle,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("topbar_hide_amounts_button")
                ) {
                    Icon(
                        imageVector = if (hideAmounts) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Hide Amounts",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Language Chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { onLanguageToggle() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("topbar_language_toggle"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (language == "si") "සිං" else "EN",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // App Lock if PIN enabled
                if (isPinEnabled) {
                    IconButton(
                        onClick = onLockApp,
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("topbar_lock_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Lock",
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }

                // Settings icon
                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("topbar_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = if (currentScreen == "SETTINGS") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
