package com.dnsguard.locker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.*

private val BgDeep        = Color(0xFF0A0A0F)
private val BgSurface     = Color(0xFF13131C)
private val BgCard        = Color(0xFF1C1C2A)
private val AccentGold    = Color(0xFFFFB300)
private val AccentGreen   = Color(0xFF00C853)
private val AccentRed     = Color(0xFFFF3D3D)
private val AccentBlue    = Color(0xFF4FC3F7)
private val TextPrimary   = Color(0xFFECECF1)
private val TextSecondary = Color(0xFF9090A8)

class EmergencyRecoveryActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DhizukuHelper.init(this)

        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    background = BgDeep,
                    surface    = BgSurface,
                    primary    = AccentGold,
                    error      = AccentRed
                )
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BgDeep
                ) {
                    RecoveryScreen(activity = this)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecoveryScreen(activity: ComponentActivity) {
    val isLocked = DnsLocker.isLocked(activity)
    val isAllowedHours = remember { RecoveryManager.isWithinAllowedHours() }

    var coolOffRemaining by remember {
        mutableStateOf(RecoveryManager.getCoolOffRemainingMs(activity))
    }
    var isCoolOffActive by remember {
        mutableStateOf(RecoveryManager.isCoolOffInitiated(activity))
    }
    var isCoolOffPassed by remember {
        mutableStateOf(RecoveryManager.isCoolOffComplete(activity))
    }

    var mathAns1 by remember { mutableStateOf("") }
    var mathAns2 by remember { mutableStateOf("") }
    var pledgeText by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isWorking by remember { mutableStateOf(false) }

    val targetPledge = "I am calm, intentional, and sober. I consciously choose to deactivate protection."

    // Tick countdown every second
    LaunchedEffect(Unit) {
        while (true) {
            coolOffRemaining = RecoveryManager.getCoolOffRemainingMs(activity)
            isCoolOffActive = RecoveryManager.isCoolOffInitiated(activity)
            isCoolOffPassed = RecoveryManager.isCoolOffComplete(activity)
            delay(1_000L)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = AccentGold)
                        Spacer(Modifier.width(8.dp))
                        Text("Emergency Recovery Gate", color = TextPrimary, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgSurface)
            )
        },
        containerColor = BgDeep
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // If device is not locked at all
            if (!isLocked) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = AccentGreen.copy(alpha = 0.15f),
                    modifier = Modifier.fillMaxWidth().border(1.dp, AccentGreen, RoundedCornerShape(14.dp))
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AccentGreen)
                        Spacer(Modifier.width(10.dp))
                        Text("DNS Guard is currently unlocked. No recovery required.", color = AccentGreen)
                    }
                }
                return@Column
            }

            // Condition 1: Daylight Hours Check
            if (!isAllowedHours) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = AccentRed.copy(alpha = 0.15f),
                    modifier = Modifier.fillMaxWidth().border(1.dp, AccentRed, RoundedCornerShape(14.dp))
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.NightlightRound, contentDescription = null, tint = AccentRed)
                            Spacer(Modifier.width(8.dp))
                            Text("Night Lockdown Active", color = AccentRed, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            "Emergency recovery is strictly forbidden between 6:00 PM and 10:00 AM to prevent nighttime impulsive decisions. Return during daylight hours (10:00 AM – 6:00 PM).",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
                return@Column
            }

            // Condition 2: 24-Hour Cooling-Off
            if (!isCoolOffActive) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = BgCard,
                    modifier = Modifier.fillMaxWidth().border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Condition 1: Mandatory 24-Hour Cool-Off", color = TextPrimary, fontWeight = FontWeight.Bold)
                        Text(
                            "To eliminate impulsive cravings, initiating recovery requires a strict 24-hour waiting delay. Your DNS will remain protected during this time.",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                        Button(
                            onClick = {
                                RecoveryManager.initiateCoolOff(activity)
                                isCoolOffActive = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentGold),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        ) {
                            Text("Start 24-Hour Cool-Off", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                return@Column
            }

            if (isCoolOffActive && !isCoolOffPassed) {
                val hours = coolOffRemaining / (1000 * 60 * 60)
                val mins = (coolOffRemaining % (1000 * 60 * 60)) / (1000 * 60)
                val secs = (coolOffRemaining % (1000 * 60)) / 1000

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = BgCard,
                    modifier = Modifier.fillMaxWidth().border(1.dp, AccentGold.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.HourglassTop, contentDescription = null, tint = AccentGold)
                            Spacer(Modifier.width(8.dp))
                            Text("Cool-Off in Progress", color = AccentGold, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            "%02dh %02dm %02ds remaining before recovery unlocks.".format(hours, mins, secs),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            "Your mind is re-centering. If this was an impulse, you can cancel this request at any time.",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        OutlinedButton(
                            onClick = {
                                RecoveryManager.cancelCoolOff(activity)
                                isCoolOffActive = false
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        ) {
                            Text("Cancel Emergency Request (Keep Guard Active)", color = AccentGreen)
                        }
                    }
                }
                return@Column
            }

            // Conditions 3 & 4: Cooling off is passed!
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = BgCard,
                modifier = Modifier.fillMaxWidth().border(1.dp, AccentGreen.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("✓ 24-Hour Cool-Off Completed", color = AccentGreen, fontWeight = FontWeight.Bold)
                    Text("Complete the final cognitive verification to confirm deactivation.", color = TextSecondary, fontSize = 12.sp)
                }
            }

            // Math challenge
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = BgCard,
                modifier = Modifier.fillMaxWidth().border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Cognitive Verification", color = TextPrimary, fontWeight = FontWeight.Bold)
                    Text("Problem 1: 17 × 8 = ?", color = TextSecondary, fontSize = 13.sp)
                    OutlinedTextField(
                        value = mathAns1,
                        onValueChange = { mathAns1 = it },
                        placeholder = { Text("Answer", color = TextSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Text("Problem 2: 245 - 68 = ?", color = TextSecondary, fontSize = 13.sp)
                    OutlinedTextField(
                        value = mathAns2,
                        onValueChange = { mathAns2 = it },
                        placeholder = { Text("Answer", color = TextSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }

            // Purpose statement
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = BgCard,
                modifier = Modifier.fillMaxWidth().border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Clarity Statement", color = TextPrimary, fontWeight = FontWeight.Bold)
                    Text("Type the exact sentence below (no copy-paste):", color = TextSecondary, fontSize = 12.sp)
                    Text(targetPledge, color = AccentGold, fontSize = 12.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                    OutlinedTextField(
                        value = pledgeText,
                        onValueChange = { pledgeText = it },
                        placeholder = { Text("Type here...", color = TextSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            }

            val mathValid = mathAns1.trim() == "136" && mathAns2.trim() == "177"
            val pledgeValid = pledgeText.trim().equals(targetPledge.trim(), ignoreCase = true)
            val canDeactivate = mathValid && pledgeValid

            statusMessage?.let { msg ->
                Text(msg, color = AccentRed, fontSize = 13.sp)
            }

            Button(
                onClick = {
                    isWorking = true
                    CoroutineScope(Dispatchers.IO).launch {
                        val dpm = DhizukuHelper.getDpm()
                        val admin = DhizukuHelper.getAdmin()
                        if (dpm != null && admin != null) {
                            DnsLocker.unlockEverything(activity, dpm, admin)
                            RecoveryManager.cancelCoolOff(activity)
                            LockMonitorService.stop(activity)
                            AlarmScheduler.cancel(activity)
                            NtpSyncWorker.cancel(activity)
                            CountdownWidget.requestUpdate(activity)
                            withContext(Dispatchers.Main) {
                                statusMessage = "Guard deactivated successfully. All restrictions cleared."
                                isWorking = false
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                statusMessage = "Error: Dhizuku DPM connection unavailable."
                                isWorking = false
                            }
                        }
                    }
                },
                enabled = canDeactivate && !isWorking,
                colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                if (isWorking) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                } else {
                    Text("DEACTIVATE GUARD & UNLOCK", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
