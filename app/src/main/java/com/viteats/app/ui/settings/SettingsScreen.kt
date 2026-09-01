package com.viteats.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.viteats.app.ui.components.NeobrutalButton
import com.viteats.app.ui.components.NeobrutalCard
import com.viteats.app.ui.components.NeobrutalPill
import com.viteats.app.ui.theme.*

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onLogout: () -> Unit
) {
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val studentProfile by viewModel.studentProfile.collectAsState()
    val scrollState = rememberScrollState()

    val screenBg = if (isDarkMode) DarkCharcoalBg else LavenderBackground
    val cardBg = if (isDarkMode) DarkCardBg else NeobrutalWhite
    val textPrimary = if (isDarkMode) DarkTextPrimary else NeobrutalBlack
    val textMuted = if (isDarkMode) DarkTextSecondary else MutedText

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBg)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // --- Header ---
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = textPrimary,
                letterSpacing = (-0.5).sp
            )
            Text(
                text = "Theme & Preferences",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = textMuted
            )
        }

        // --- Appearance & Dark Mode Card ---
        NeobrutalCard(
            backgroundColor = cardBg,
            borderColor = NeobrutalBlack,
            borderWidth = 2.5.dp,
            shadowOffset = 4.dp,
            cornerRadius = 16.dp
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isDarkMode) PastelYellow else SoftCyan)
                                .border(2.dp, NeobrutalBlack, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                                contentDescription = null,
                                tint = NeobrutalBlack,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Dark Mode",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = textPrimary
                            )
                            Text(
                                text = if (isDarkMode) "Deep Charcoal Theme" else "Pastel Lavender Theme",
                                style = MaterialTheme.typography.bodySmall,
                                color = textMuted
                            )
                        }
                    }

                    // Neobrutalist Interactive Toggle Switch
                    NeobrutalSwitch(
                        checked = isDarkMode,
                        onCheckedChange = { viewModel.setDarkMode(it) }
                    )
                }

                HorizontalDivider(color = NeobrutalBlack.copy(alpha = 0.15f), thickness = 1.dp)

                Text(
                    text = "Swaps pastel backgrounds for deep charcoal while keeping vibrant yellow, mint, and coral accents with crisp black drop shadows.",
                    style = MaterialTheme.typography.bodySmall,
                    color = textMuted,
                    lineHeight = 18.sp
                )
            }
        }

        // --- University Mess Schedule Card ---
        NeobrutalCard(
            backgroundColor = cardBg,
            borderColor = NeobrutalBlack,
            borderWidth = 2.5.dp,
            shadowOffset = 4.dp,
            cornerRadius = 16.dp
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Mess Operating Hours",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = textPrimary
                )

                HorizontalDivider(color = NeobrutalBlack.copy(alpha = 0.15f), thickness = 1.dp)

                MessHourRow("Breakfast", "07:00 AM – 09:00 AM", SoftCyan, textPrimary)
                MessHourRow("Lunch", "12:00 PM – 02:00 PM", PastelYellow, textPrimary)
                MessHourRow("Snacks", "05:00 PM – 06:30 PM", SoftCoral, textPrimary)
                MessHourRow("Dinner", "07:00 PM – 09:00 PM", MintGreen, textPrimary)
            }
        }

        // --- App Information & Version Card ---
        NeobrutalCard(
            backgroundColor = cardBg,
            borderColor = NeobrutalBlack,
            borderWidth = 2.5.dp,
            shadowOffset = 4.dp,
            cornerRadius = 16.dp
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "VITeats Version",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                    Text(
                        text = "v1.1.0",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Black,
                        color = textMuted
                    )
                }
            }
        }

        // --- Logout Action Card ---
        NeobrutalCard(
            backgroundColor = cardBg,
            borderColor = NeobrutalBlack,
            borderWidth = 2.5.dp,
            shadowOffset = 4.dp,
            cornerRadius = 16.dp
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Account Session",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = textPrimary
                )

                Text(
                    text = "Log out of your VIT mess portal account on this device.",
                    style = MaterialTheme.typography.bodySmall,
                    color = textMuted
                )

                NeobrutalButton(
                    onClick = {
                        viewModel.logout()
                        onLogout()
                    },
                    backgroundColor = SoftCoral,
                    contentColor = NeobrutalBlack,
                    borderColor = NeobrutalBlack,
                    borderWidth = 2.dp,
                    shadowOffset = 2.5.dp,
                    cornerRadius = 10.dp,
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = "Logout",
                        modifier = Modifier.size(16.dp),
                        tint = NeobrutalBlack
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Logout from VITeats",
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun NeobrutalSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val shape = RoundedCornerShape(50)
    val trackBg = if (checked) MintGreen else Color(0xFFE2E8F0)

    Box(
        modifier = Modifier
            .width(56.dp)
            .height(30.dp)
            .clip(shape)
            .background(trackBg)
            .border(2.dp, NeobrutalBlack, shape)
            .clickable { onCheckedChange(!checked) }
            .padding(3.dp),
        contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(if (checked) PastelYellow else NeobrutalWhite)
                .border(1.5.dp, NeobrutalBlack, CircleShape)
        )
    }
}

@Composable
private fun SettingsDetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    textColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = textColor
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
    }
}

@Composable
private fun MessHourRow(
    mealName: String,
    timing: String,
    pillColor: Color,
    textColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(pillColor)
                    .border(1.dp, NeobrutalBlack, CircleShape)
            )
            Text(
                text = mealName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }

        Text(
            text = timing,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}
