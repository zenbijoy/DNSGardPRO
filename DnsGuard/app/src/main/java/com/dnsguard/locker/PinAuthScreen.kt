package com.dnsguard.locker

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val BgDeep        = Color(0xFF0A0A0F)
private val BgCard        = Color(0xFF1C1C2A)
private val AccentGold    = Color(0xFFFFB300)
private val AccentRed     = Color(0xFFFF3D3D)
private val TextPrimary   = Color(0xFFECECF1)
private val TextSecondary = Color(0xFF9090A8)

/**
 * Modern, secure Master PIN Keypad Screen.
 * Displayed when user opens the app to safeguard access.
 */
@Composable
fun PinAuthScreen(
    context: android.content.Context,
    onAuthenticated: () -> Unit
) {
    var enteredPin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDeep)
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Header
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(top = 24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(AccentGold.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    tint = if (isError) AccentRed else AccentGold,
                    modifier = Modifier.size(32.dp)
                )
            }

            Text(
                text = "DnsGuard Locked",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = if (isError) "Incorrect PIN. Try again." else "Enter your Master PIN to open",
                color = if (isError) AccentRed else TextSecondary,
                fontSize = 13.sp
            )

            Spacer(Modifier.height(16.dp))

            val targetLength = remember { AuthManager.getPinLength(context) }

            // PIN Dot Indicators
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                for (i in 0 until targetLength) {
                    val isFilled = i < enteredPin.length
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isError -> AccentRed
                                    isFilled -> AccentGold
                                    else -> Color.White.copy(alpha = 0.2f)
                                }
                            )
                            .border(
                                1.dp,
                                if (isError) AccentRed else AccentGold.copy(alpha = 0.4f),
                                CircleShape
                            )
                    )
                }
            }
        }

        // Numeric Keypad
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            val targetLength = remember { AuthManager.getPinLength(context) }
            val rows = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("C", "0", "DEL")
            )

            for (row in rows) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (key in row) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(64.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(BgCard)
                                .clickable {
                                    isError = false
                                    when (key) {
                                        "C" -> enteredPin = ""
                                        "DEL" -> if (enteredPin.isNotEmpty()) enteredPin = enteredPin.dropLast(1)
                                        else -> {
                                            if (enteredPin.length < targetLength) {
                                                val newPin = enteredPin + key
                                                enteredPin = newPin
                                                if (newPin.length == targetLength) {
                                                    if (AuthManager.verifyPin(context, newPin)) {
                                                        onAuthenticated()
                                                    } else {
                                                        scope.launch {
                                                            isError = true
                                                            delay(500)
                                                            enteredPin = ""
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (key == "DEL") {
                                Icon(Icons.Default.Backspace, contentDescription = "Delete", tint = TextPrimary)
                            } else {
                                Text(
                                    text = key,
                                    color = if (key == "C") AccentGold else TextPrimary,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
