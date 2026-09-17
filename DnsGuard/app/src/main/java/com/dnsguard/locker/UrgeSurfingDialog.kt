package com.dnsguard.locker

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.delay

private val BgCard      = Color(0xFF1C1C2A)
private val AccentGold  = Color(0xFFFFB300)
private val AccentBlue  = Color(0xFF4FC3F7)
private val AccentGreen = Color(0xFF00C853)
private val TextPrimary = Color(0xFFECECF1)
private val TextSecondary = Color(0xFF9090A8)

/**
 * 15-Minute Urge Surfing Emergency Protocol.
 * Based on Dr. Alan Marlatt's relapse prevention psychology and Stanford dopamine neurobiology.
 * Helps the user ride out acute dopamine craving waves without fighting or relapsing.
 */
@Composable
fun UrgeSurfingDialog(onDismiss: () -> Unit) {
    var remainingSeconds by remember { mutableStateOf(15 * 60) } // 15 minutes
    var isPaused by remember { mutableStateOf(false) }
    var currentArmorQuote by remember { mutableStateOf(QuoteManager.getRandomQuote()) }

    LaunchedEffect(isPaused) {
        while (!isPaused && remainingSeconds > 0) {
            delay(1000L)
            remainingSeconds--
        }
    }

    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60

    // Determine current phase based on remaining time
    val (phaseTitle, phaseColor, phaseInstruction) = when {
        remainingSeconds > 12 * 60 -> Triple(
            "Phase 1: Acknowledge & Observe",
            AccentBlue,
            "Notice the urge in your body. It is just electrical signals and temporary dopamine anticipation. Do not fight it; observe it like a wave in the ocean."
        )
        remainingSeconds > 7 * 60 -> Triple(
            "Phase 2: Rhythmic 4-4 Breathing",
            AccentGold,
            "Inhale deep for 4 seconds... Hold 4 seconds... Exhale 4 seconds... Hold 4 seconds. Your nervous system is cooling down."
        )
        remainingSeconds > 0 -> Triple(
            "Phase 3: Dopamine Normalization",
            AccentGreen,
            "The craving peak has broken. The neurochemical wave is subsiding. You are keeping your 1-year contract intact."
        )
        else -> Triple(
            "Protocol Complete!",
            AccentGreen,
            "You conquered the wave! The craving has passed without a relapse. Stand proud."
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = BgCard,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, phaseColor.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(22.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Waves, contentDescription = null, tint = phaseColor, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Urge Surfing Protocol", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                // Breathing Pulse Graphic
                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                val breathScale by infiniteTransition.animateFloat(
                    initialValue = 0.85f,
                    targetValue = 1.15f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(4000, easing = EaseInOutCubic),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "breath"
                )

                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .scale(breathScale)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(phaseColor.copy(alpha = 0.35f), phaseColor.copy(alpha = 0.05f))
                            )
                        )
                        .border(2.dp, phaseColor.copy(alpha = 0.6f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "%02d:%02d".format(minutes, seconds),
                            color = TextPrimary,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = if (remainingSeconds > 0) "SURFING WAVE" else "VICTORY",
                            color = phaseColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }

                // Phase Directive Card
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = phaseColor.copy(alpha = 0.1f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, phaseColor.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                ) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(phaseTitle, color = phaseColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(phaseInstruction, color = TextPrimary.copy(alpha = 0.9f), fontSize = 12.sp, lineHeight = 17.sp)
                    }
                }

                // Mental Armor Stoic Quote Card
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White.copy(alpha = 0.03f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
                ) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = AccentGold, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Mental Armor Affirmation", color = AccentGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            IconButton(
                                onClick = { currentArmorQuote = QuoteManager.getRandomQuote() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "New Quote", tint = AccentGold, modifier = Modifier.size(16.dp))
                            }
                        }

                        Text(
                            text = "\"${currentArmorQuote.text}\"",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontStyle = FontStyle.Italic,
                            lineHeight = 17.sp
                        )
                        Text(
                            text = "— ${currentArmorQuote.author}",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Action Button
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = phaseColor),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text(
                        if (remainingSeconds > 0) "I Am Grounded & Strong" else "Close Protocol",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
