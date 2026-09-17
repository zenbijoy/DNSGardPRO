package com.dnsguard.locker

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

private val BgCard      = Color(0xFF1C1C2A)
private val AccentGold  = Color(0xFFFFB300)
private val AccentBlue  = Color(0xFF4FC3F7)
private val TextPrimary = Color(0xFFECECF1)
private val TextSecondary = Color(0xFF9090A8)

/**
 * Google Play Store Compliant Prominent In-App Disclosure for AccessibilityService API.
 * Explicitly states why the service is required, what data is processed, and privacy guarantees.
 */
@Composable
fun AccessibilityDisclosureDialog(
    onAccept: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = BgCard,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, AccentGold.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(22.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(AccentGold.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Accessibility, contentDescription = null, tint = AccentGold)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            "Accessibility Disclosure",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Text(
                            "Google Play Transparency Notice",
                            color = AccentGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Divider(color = Color.White.copy(alpha = 0.08f))

                Text(
                    "DnsGuard uses Android's AccessibilityService API to provide strict habit protection and prevent digital relapse.",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )

                DisclosureSection(
                    icon = Icons.Default.Shield,
                    title = "Why Accessibility is Needed",
                    desc = "1. To detect and instantly block attempts to deactivate protection in Settings.\n2. To prevent bypassing DNS security or uninstalling during your active 1-year commitment."
                )

                DisclosureSection(
                    icon = Icons.Default.Security,
                    title = "What Data is Processed",
                    desc = "Processes foreground window package names and activity titles in real-time locally on your device to recognize settings tampering."
                )

                DisclosureSection(
                    icon = Icons.Default.Lock,
                    title = "Privacy & Data Protection Guarantee",
                    desc = "DnsGuard does NOT collect, store, record, or transmit any personal data, text input, keystrokes, or passwords. All processing happens 100% locally on your phone."
                )

                Spacer(Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cancel", color = TextSecondary)
                    }

                    Button(
                        onClick = onAccept,
                        modifier = Modifier.weight(1.5f),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentGold),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Accept & Continue", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun DisclosureSection(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, desc: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.04f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(title, color = AccentBlue, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }
            Text(desc, color = TextSecondary, fontSize = 11.sp, lineHeight = 16.sp)
        }
    }
}
