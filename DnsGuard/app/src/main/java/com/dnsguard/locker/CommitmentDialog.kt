package com.dnsguard.locker

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

private val BgCard      = Color(0xFF1C1C2A)
private val AccentGold  = Color(0xFFFFB300)
private val AccentRed   = Color(0xFFFF3D3D)
private val TextPrimary = Color(0xFFECECF1)
private val TextSecondary = Color(0xFF9090A8)

/**
 * The Uncompromising 1-Year Commitment Gate.
 * "If you are not serious, do not activate."
 * Enforces:
 *  1. Master Security PIN creation
 *  2. Solemn pledge acknowledgment
 *  3. Irreversible 365-day full-strict lockdown
 */
@Composable
fun CommitmentDialog(
    onConfirm: (pin: String) -> Unit,
    onDismiss: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var pledgeChecked by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = BgCard,
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, AccentRed.copy(alpha = 0.6f), RoundedCornerShape(22.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(22.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Warning Banner
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = AccentRed.copy(alpha = 0.15f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = AccentRed)
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                "ARE YOU 100% SERIOUS?",
                                color = AccentRed,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                "If you are not serious, DO NOT activate.",
                                color = AccentRed.copy(alpha = 0.85f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Text(
                    "This is an uncompromising, strict 1-year contract with yourself. Once locked:",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    StrictRuleItem("⛔ NO WAY to disable protection for 365 days.")
                    StrictRuleItem("⛔ ALL adult and relapse websites permanently blocked.")
                    StrictRuleItem("⛔ Device Admin & Accessibility will block uninstall attempts.")
                    StrictRuleItem("⛔ No secret bypass, no toggle, and no easy exit.")
                }

                Divider(color = Color.White.copy(alpha = 0.08f))

                Text(
                    "Set Master Security PIN",
                    color = AccentGold,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = pin,
                    onValueChange = { if (it.length <= 6) pin = it },
                    label = { Text("Enter 4 to 6 digit PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentGold,
                        focusedLabelColor = AccentGold
                    )
                )

                OutlinedTextField(
                    value = confirmPin,
                    onValueChange = { if (it.length <= 6) confirmPin = it },
                    label = { Text("Confirm PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentGold,
                        focusedLabelColor = AccentGold
                    )
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Checkbox(
                        checked = pledgeChecked,
                        onCheckedChange = { pledgeChecked = it },
                        colors = CheckboxDefaults.colors(checkedColor = AccentRed)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "I consciously choose 365 days of freedom. I swear to stay strict.",
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                errorMessage?.let {
                    Text(it, color = AccentRed, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Not Ready", color = TextSecondary)
                    }

                    Button(
                        onClick = {
                            if (pin.length < 4) {
                                errorMessage = "PIN must be at least 4 digits."
                                return@Button
                            }
                            if (pin != confirmPin) {
                                errorMessage = "PINs do not match."
                                return@Button
                            }
                            if (!pledgeChecked) {
                                errorMessage = "You must accept the strict commitment pledge."
                                return@Button
                            }
                            onConfirm(pin)
                        },
                        modifier = Modifier.weight(1.6f),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("SEAL 1-YEAR", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun StrictRuleItem(text: String) {
    Text(
        text = text,
        color = TextSecondary,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 16.sp
    )
}
