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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay

private val BgDeep        = Color(0xFF0A0A0F)
private val BgSurface     = Color(0xFF13131C)
private val BgCard        = Color(0xFF1C1C2A)
private val AccentGold    = Color(0xFFFFB300)
private val AccentGreen   = Color(0xFF00C853)
private val AccentBlue    = Color(0xFF4FC3F7)
private val AccentRed     = Color(0xFFFF3D3D)
private val TextPrimary   = Color(0xFFECECF1)
private val TextSecondary = Color(0xFF9090A8)

@Composable
fun BreathingGuideDialog(onDismiss: () -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = BgSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, AccentGold.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Spa, contentDescription = null, tint = AccentGold)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Calm & Focus Guide",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Tab Row
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = BgCard,
                    contentColor = AccentGold,
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Box Breathing", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("5-4-3-2-1 Grounding", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Urge Surfer", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Content by tab
                Box(modifier = Modifier.weight(1f)) {
                    when (selectedTab) {
                        0 -> BoxBreathingView()
                        1 -> GroundingView()
                        2 -> UrgeSurferView()
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Tab 1: Box Breathing (4-4-4-4)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun BoxBreathingView() {
    val phases = listOf("Inhale Slowly...", "Hold Breath...", "Exhale Calmly...", "Hold Still...")
    var phaseIndex by remember { mutableIntStateOf(0) }
    var countdown by remember { mutableIntStateOf(4) }

    LaunchedEffect(Unit) {
        while (true) {
            for (phase in 0..3) {
                phaseIndex = phase
                for (sec in 4 downTo 1) {
                    countdown = sec
                    delay(1_000L)
                }
            }
        }
    }

    // Scale animation based on phase
    val targetScale = when (phaseIndex) {
        0 -> 1.25f // Inhaling -> expanding
        1 -> 1.25f // Holding full
        2 -> 0.75f // Exhaling -> shrinking
        else -> 0.75f // Holding empty
    }

    val animatedScale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = tween(durationMillis = 4000, easing = LinearEasing),
        label = "breathing_scale"
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Navy SEAL Box Breathing",
            color = AccentGold,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        Text(
            "Lowers heart rate and returns blood flow to your prefrontal cortex.",
            color = TextSecondary,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
        )

        Spacer(Modifier.height(30.dp))

        // Breathing Circle
        Box(
            modifier = Modifier
                .size(200.dp)
                .scale(animatedScale)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(AccentBlue.copy(alpha = 0.35f), AccentGold.copy(alpha = 0.15f), Color.Transparent)
                    )
                )
                .border(2.dp, AccentGold.copy(alpha = 0.7f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    phases[phaseIndex],
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    "$countdown",
                    color = AccentGold,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 32.sp
                )
            }
        }

        Spacer(Modifier.height(36.dp))

        Text(
            "Follow the circle. Breathe in deeply through your nose, out through your mouth.",
            color = TextSecondary,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 30.dp)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Tab 2: 5-4-3-2-1 Sensory Grounding
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun GroundingView() {
    val steps = listOf(
        "5" to "Acknowledge 5 things you can SEE around you (a pen, shadows, light, objects).",
        "4" to "Acknowledge 4 things you can physically TOUCH (your clothes, chair, the floor, your phone).",
        "3" to "Acknowledge 3 sounds you can HEAR (traffic, a fan, breathing, distant birds).",
        "2" to "Acknowledge 2 things you can SMELL or identify by scent in your environment.",
        "1" to "Acknowledge 1 thing you are deeply GRATEFUL for in your life right now."
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "5-4-3-2-1 Sensory Re-Anchoring",
            color = AccentGold,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        Text(
            "Engages the sensory cortex to break internal loop thoughts and pull focus back to physical reality.",
            color = TextSecondary,
            fontSize = 12.sp
        )

        steps.forEach { (num, desc) ->
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = BgCard,
                modifier = Modifier.fillMaxWidth().border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(AccentGold.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(num, color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(desc, color = TextPrimary, fontSize = 13.sp)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Tab 3: Urge Surfing 15-Minute Timer
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun UrgeSurferView() {
    var timerRunning by remember { mutableStateOf(false) }
    var secondsLeft by remember { mutableIntStateOf(15 * 60) }

    LaunchedEffect(timerRunning) {
        while (timerRunning && secondsLeft > 0) {
            delay(1000L)
            secondsLeft--
        }
        if (secondsLeft == 0) timerRunning = false
    }

    val minutes = secondsLeft / 60
    val seconds = secondsLeft % 60

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "The 15-Minute Wave",
            color = AccentGold,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Text(
            "Neuroscience shows that impulses are like ocean waves: they rise, peak, and naturally crash within 10–15 minutes. You do not have to fight the wave — simply ride it out.",
            color = TextSecondary,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )

        Spacer(Modifier.height(20.dp))

        Text(
            "%02d:%02d".format(minutes, seconds),
            color = AccentGold,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 44.sp
        )

        Spacer(Modifier.height(20.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = { timerRunning = !timerRunning },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (timerRunning) AccentRed else AccentGreen
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(if (timerRunning) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text(if (timerRunning) "Pause" else "Ride The Wave", color = Color.White, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = {
                    timerRunning = false
                    secondsLeft = 15 * 60
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Reset", color = TextSecondary)
            }
        }
    }
}
