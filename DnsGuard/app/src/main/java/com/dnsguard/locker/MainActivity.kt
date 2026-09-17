package com.dnsguard.locker

import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.*

// ─────────────────────────────────────────────────────────────────────────────
// Color palette (unchanged)
// ─────────────────────────────────────────────────────────────────────────────

private val BgDeep        = Color(0xFF0A0A0F)
private val BgSurface     = Color(0xFF13131C)
private val BgCard        = Color(0xFF1C1C2A)
private val AccentGold    = Color(0xFFFFB300)
private val AccentGoldDim = Color(0xFF7A5500)
private val AccentOrange  = Color(0xFFFF9800)
private val AccentGreen   = Color(0xFF00C853)
private val AccentRed     = Color(0xFFFF3D3D)
private val AccentBlue    = Color(0xFF4FC3F7)
private val TextPrimary   = Color(0xFFECECF1)
private val TextSecondary = Color(0xFF9090A8)

private val AppColorScheme = darkColorScheme(
    background   = BgDeep,
    surface      = BgSurface,
    primary      = AccentGold,
    onPrimary    = Color(0xFF1A1000),
    secondary    = AccentBlue,
    onBackground = TextPrimary,
    onSurface    = TextPrimary,
    error        = AccentRed,
)

// ─────────────────────────────────────────────────────────────────────────────
// MainActivity
// ─────────────────────────────────────────────────────────────────────────────

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DhizukuHelper.init(this)

        // Feature 5: start background NTP sync if already locked
        if (DnsLocker.isLocked(this)) {
            NtpSyncWorker.schedule(this)
        }

        // Nightly encouragement notifications (at 9:30 PM every night)
        NightlyScheduler.scheduleNext(this)

        // Morning focus directive notifications (at 8:00 AM every morning)
        MorningScheduler.scheduleNext(this)

        // Clear any old or stuck warning notification from previous app versions
        LockReApplyReceiver.cancelAccessibilityWarning(this)

        setContent {
            MaterialTheme(colorScheme = AppColorScheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color    = MaterialTheme.colorScheme.background
                ) {
                    DnsGuardApp(activity = this)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Navigation root
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun DnsGuardApp(activity: ComponentActivity) {
    var isAuthenticated by remember {
        mutableStateOf(!AuthManager.isPinSet(activity) || AuthManager.isAuthenticatedSession)
    }

    if (!isAuthenticated && AuthManager.isPinSet(activity)) {
        PinAuthScreen(
            context = activity,
            onAuthenticated = {
                AuthManager.isAuthenticatedSession = true
                isAuthenticated = true
            }
        )
    } else {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = "home") {
            composable("home") {
                HomeScreen(activity = activity, onOpenSettings = { navController.navigate("settings") })
            }
            composable("settings") {
                SettingsScreen(activity = activity, onBack = { navController.popBackStack() })
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Home Screen
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(activity: ComponentActivity, onOpenSettings: () -> Unit) {

    var hasPermission   by remember { mutableStateOf(DhizukuHelper.isPermissionGranted()) }
    var isLocked        by remember { mutableStateOf(DnsLocker.isLocked(activity)) }
    var remainingTime   by remember { mutableStateOf(TimerManager.getRemainingTime(activity)) }
    var statusMessage   by remember { mutableStateOf<String?>(null) }
    var isWorking       by remember { mutableStateOf(false) }
    var accessibilityState by remember { mutableStateOf(getAccessibilityState(activity)) }
    var isBatteryIgnored   by remember { mutableStateOf(isBatteryOptimizationIgnored(activity)) }
    var isAdminActive      by remember { mutableStateOf(DeviceAdminManager.isAdminActive(activity)) }
    var isVpnActive        by remember { mutableStateOf(DnsVpnService.isRunning) }

    var showBreathingDialog by remember { mutableStateOf(false) }
    var showWisdomVault by remember { mutableStateOf(false) }
    var showCommitmentDialog by remember { mutableStateOf(false) }
    var showAccessibilityDisclosure by remember { mutableStateOf(false) }

    val vpnLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            DnsVpnService.start(activity)
            isVpnActive = true
            statusMessage = "DNS Shield activated!"
        }
    }

    val adminLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        isAdminActive = DeviceAdminManager.isAdminActive(activity)
        if (isAdminActive) {
            statusMessage = "Device Administrator activated! App uninstall protected."
        }
    }

    // Refresh state periodically
    LaunchedEffect(Unit) {
        while (true) {
            accessibilityState = getAccessibilityState(activity)
            isBatteryIgnored = isBatteryOptimizationIgnored(activity)
            isAdminActive = DeviceAdminManager.isAdminActive(activity)
            isVpnActive = DnsVpnService.isRunning
            hasPermission = DhizukuHelper.isPermissionGranted()
            if (accessibilityState == AccessibilityState.ACTIVE) {
                LockReApplyReceiver.cancelAccessibilityWarning(activity)
            }
            delay(2_000L)
        }
    }

    // Daily quote
    val quote = remember { QuoteManager.todayQuote() }

    // Progress fraction
    val progress = remember(remainingTime) { TimerManager.progressFraction(activity) }

    // Tick countdown every second
    LaunchedEffect(Unit) {
        while (true) {
            remainingTime = TimerManager.getRemainingTime(activity)
            delay(1_000L)
        }
    }

    // Fetch NTP once on open; update high-water mark + widget
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val networkNow = NtpClient.nowMs()
            TimerManager.updateHighWater(activity, networkNow)
            CountdownWidget.requestUpdate(activity)
        }
        remainingTime = TimerManager.getRemainingTime(activity)
    }

    val bd = remember(remainingTime) { TimerManager.breakdown(remainingTime) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = AccentGold)
                        Spacer(Modifier.width(8.dp))
                        Text("DnsGuard", fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { showBreathingDialog = true }) {
                        Icon(Icons.Default.Spa, contentDescription = "Calm & Ground", tint = AccentGold)
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = TextSecondary)
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

            // Status snackbar
            AnimatedVisibility(visible = statusMessage != null, enter = fadeIn(), exit = fadeOut()) {
                statusMessage?.let { msg ->
                    val isError = msg.startsWith("Error") || msg.startsWith("Failed") || msg.startsWith("Security")
                    Surface(
                        shape    = RoundedCornerShape(12.dp),
                        color    = if (isError) AccentRed.copy(alpha = 0.15f) else AccentGreen.copy(alpha = 0.15f),
                        modifier = Modifier.fillMaxWidth()
                            .border(1.dp, if (isError) AccentRed else AccentGreen, RoundedCornerShape(12.dp))
                    ) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (isError) Icons.Default.Error else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (isError) AccentRed else AccentGreen
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(msg, color = if (isError) AccentRed else AccentGreen, fontSize = 13.sp)
                        }
                    }
                }
            }

            if (isLocked) {
                // ── Quick SOS Calm & Breathe Banner ───────────────────────
                CalmActionBanner(onOpenBreathing = { showBreathingDialog = true })

                // ── Live DNS Shield Telemetry ─────────────────────────────
                DnsTelemetryCard(activity = activity)

                // ── Feature 9: Progress ring ───────────────────────────────
                if (TimerManager.isStarted(activity)) {
                    ProgressRingCard(progress = progress, remainingMs = remainingTime)
                }

                // ── Countdown card ─────────────────────────────────────────
                if (TimerManager.isStarted(activity)) {
                    CountdownCard(bd = bd, isComplete = remainingTime == 0L)
                }

                // ── Milestone Badges & Progression ────────────────────────
                if (TimerManager.isStarted(activity)) {
                    MilestoneProgressionCard(activity = activity)
                }

                // ── Locked Status Banner ───────────────────────────────────
                LockedInfo()

                // ── Accessibility Guard Status ────────────────────────────
                AccessibilityGuardCard(
                    state    = accessibilityState,
                    activity = activity
                )

                // ── Battery Optimization Status ─────────────────────────
                BatteryOptimizationCard(
                    isIgnored = isBatteryIgnored,
                    activity  = activity
                )

                // ── Daily In-App Wisdom & Story ───────────────────────────
                DailyWisdomStoryCard(onOpenVault = { showWisdomVault = true })

                // ── Feature 8: Daily quote card ────────────────────────────
                QuoteCard(quote = quote)

            } else {
                // ── Standalone 3-Step Setup Wizard ─────────────────────────
                SeriousWarningBanner()

                Text(
                    "Complete all 3 steps below to seal protection:",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )

                // Step 1: DNS Shield VPN
                SetupStepCard(
                    stepNumber = 1,
                    icon = Icons.Default.Shield,
                    title = "1. Enable DNS Shield",
                    description = "Forces private DNS to KahfGuard with local loopback.",
                    isComplete = isVpnActive || hasPermission,
                    actionButtonText = "Activate",
                    onAction = {
                        val vpnIntent = VpnService.prepare(activity)
                        if (vpnIntent != null) {
                            vpnLauncher.launch(vpnIntent)
                        } else {
                            DnsVpnService.start(activity)
                            isVpnActive = true
                        }
                    }
                )

                // Step 2: Prevent App Uninstall (Device Admin)
                SetupStepCard(
                    stepNumber = 2,
                    icon = Icons.Default.Security,
                    title = "2. Prevent App Uninstall",
                    description = "Sets DnsGuard as Device Admin so it cannot be removed.",
                    isComplete = isAdminActive || hasPermission,
                    actionButtonText = "Activate",
                    onAction = {
                        val intent = DeviceAdminManager.getRequestAdminIntent(activity)
                        adminLauncher.launch(intent)
                    }
                )

                // Step 3: Tamper Protection (Accessibility Guard)
                SetupStepCard(
                    stepNumber = 3,
                    icon = Icons.Default.Accessibility,
                    title = "3. Tamper Protection",
                    description = "Blocks settings access and deactivating Device Admin.",
                    isComplete = accessibilityState == AccessibilityState.ACTIVE,
                    actionButtonText = if (accessibilityState == AccessibilityState.ZOMBIE) "Fix Now" else "Enable",
                    onAction = {
                        showAccessibilityDisclosure = true
                    }
                )

                // Battery Optimization Notice
                BatteryOptimizationCard(
                    isIgnored = isBatteryIgnored,
                    activity  = activity
                )

                // Seal Commitment Action
                val isReadyToLock = (isVpnActive || hasPermission) &&
                                    (isAdminActive || hasPermission) &&
                                    (accessibilityState == AccessibilityState.ACTIVE)

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LockButton(
                        loading = isWorking,
                        enabled = isReadyToLock,
                        onClick = {
                            if (!isBatteryIgnored) {
                                requestIgnoreBatteryOptimization(activity)
                            }
                            showCommitmentDialog = true
                        }
                    )

                    if (!isReadyToLock) {
                        Text(
                            "Complete Steps 1, 2, and 3 above to unlock the commitment seal.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Text(
                            "Ready to lock. Tap above to create your Master PIN & solemn pledge.",
                            color = AccentGold,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Daily Story & Quote previews
                DailyWisdomStoryCard(onOpenVault = { showWisdomVault = true })
                QuoteCard(quote = quote)
            }
        }
    }

    if (showBreathingDialog) {
        BreathingGuideDialog(onDismiss = { showBreathingDialog = false })
    }

    if (showWisdomVault) {
        WisdomVaultDialog(onDismiss = { showWisdomVault = false })
    }

    if (showCommitmentDialog) {
        CommitmentDialog(
            onConfirm = { pin ->
                showCommitmentDialog = false
                isWorking = true
                statusMessage = null
                AuthManager.setMasterPin(activity, pin)
                AuthManager.recordPledgeSigned(activity)
                CoroutineScope(Dispatchers.IO).launch {
                    val networkNow = DnsLocker.fetchNetworkTimeMs()
                    TimerManager.updateHighWater(activity, networkNow)
                    val dpm   = DhizukuHelper.getDpm()
                    val admin = DhizukuHelper.getAdmin()
                    val success = DnsLocker.lockEverything(activity, dpm, admin)
                    withContext(Dispatchers.Main) {
                        if (success) {
                            TimerManager.startTimer(activity)

                            // Start ALL defense layers simultaneously
                            LockMonitorService.start(activity)
                            AlarmScheduler.scheduleReApply(activity)
                            NtpSyncWorker.schedule(activity)

                            CountdownWidget.requestUpdate(activity)
                            isLocked      = true
                            remainingTime = TimerManager.getRemainingTime(activity)
                            statusMessage = "Locked! 1-Year Strict Addiction Protection Activated."
                        } else {
                            statusMessage = "Failed to lock DNS. Please check permissions."
                        }
                        isWorking = false
                    }
                }
            },
            onDismiss = { showCommitmentDialog = false }
        )
    }

    if (showAccessibilityDisclosure) {
        AccessibilityDisclosureDialog(
            onAccept = {
                showAccessibilityDisclosure = false
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                activity.startActivity(intent)
            },
            onDismiss = { showAccessibilityDisclosure = false }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Settings Screen
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(activity: ComponentActivity, onBack: () -> Unit) {

    var isLocked      by remember { mutableStateOf(DnsLocker.isLocked(activity)) }
    var remainingTime by remember { mutableStateOf(TimerManager.getRemainingTime(activity)) }
    var isWorking     by remember { mutableStateOf(false) }

    val yearPassed = TimerManager.isYearPassed(activity)
    val bd         = remember(remainingTime) { TimerManager.breakdown(remainingTime) }

    LaunchedEffect(Unit) {
        while (true) { remainingTime = TimerManager.getRemainingTime(activity); delay(1_000L) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title          = { Text("Settings", color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextSecondary)
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

            if (isLocked && !yearPassed) {
                Surface(
                    shape    = RoundedCornerShape(14.dp),
                    color    = AccentRed.copy(alpha = 0.12f),
                    modifier = Modifier.fillMaxWidth()
                        .border(1.dp, AccentRed.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Block, contentDescription = null, tint = AccentRed)
                            Spacer(Modifier.width(8.dp))
                            Text("Unlock Restricted", color = AccentRed, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            "You committed to 1 year. ${bd.days}d ${bd.hours}h ${bd.minutes}m ${bd.seconds}s remaining.",
                            color = AccentRed.copy(alpha = 0.8f), fontSize = 13.sp
                        )
                    }
                }
            }

            SectionTitle("Active Restrictions")
            RestrictionRow("DNS Shield → high.kahfguard.com",    isLocked)
            RestrictionRow("Device Administrator Protection",     isLocked)
            RestrictionRow("Tamper Protection (Accessibility)",   isLocked)
            RestrictionRow("Master PIN Session Security",         isLocked)
            RestrictionRow("App Uninstall Blocked",               isLocked)
            RestrictionRow("Settings Tampering Blocked",          isLocked)

            SectionTitle("Background Protection")
            RestrictionRow("Boot auto-start (BootReceiver)",      isLocked)
            RestrictionRow("NTP sync every 6 hours (WorkManager)", isLocked)
            RestrictionRow("Triple-redundant encrypted timer",    isLocked)

            Spacer(Modifier.height(4.dp))
            SectionTitle("Unlock Options")

            when {
                yearPassed && isLocked -> {
                    Button(
                        onClick = {
                            isWorking = true
                            CoroutineScope(Dispatchers.IO).launch {
                                val dpm   = DhizukuHelper.getDpm()
                                val admin = DhizukuHelper.getAdmin()
                                DnsLocker.unlockEverything(activity, dpm, admin)

                                // Stop all 3 defense layers
                                LockMonitorService.stop(activity)
                                AlarmScheduler.cancel(activity)
                                NtpSyncWorker.cancel(activity)

                                CountdownWidget.requestUpdate(activity)
                                withContext(Dispatchers.Main) {
                                    isLocked  = DnsLocker.isLocked(activity)
                                    isWorking = false
                                }
                            }
                        },
                        enabled  = !isWorking,
                        colors   = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape    = RoundedCornerShape(14.dp)
                    ) {
                        if (isWorking) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.Black)
                        } else {
                            Icon(Icons.Default.LockOpen, contentDescription = null, tint = Color.Black)
                            Spacer(Modifier.width(8.dp))
                            Text("UNLOCK (1 YEAR PASSED)", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                isLocked -> {
                    Button(onClick = {}, enabled = false,
                        modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(14.dp)) {
                        Text("Device is locked. You cannot make changes.", color = MaterialTheme.colorScheme.error)
                    }
                    Text("Unlock in: ${bd.days}d ${bd.hours}h ${bd.minutes}m",
                        color = TextSecondary, fontSize = 12.sp, textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth())
                }
                else -> Text("Nothing to unlock — DNS is not currently locked.", color = TextSecondary)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Feature 9: Progress Ring Card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ProgressRingCard(progress: Float, remainingMs: Long) {
    val yearPassed = remainingMs == 0L
    Surface(
        shape    = RoundedCornerShape(16.dp),
        color    = BgCard,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Circular progress
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp)) {
                CircularProgressIndicator(
                    progress        = { 1f },
                    modifier        = Modifier.fillMaxSize(),
                    color           = AccentGold.copy(alpha = 0.15f),
                    strokeWidth     = 8.dp,
                    strokeCap       = StrokeCap.Round
                )
                CircularProgressIndicator(
                    progress        = { progress },
                    modifier        = Modifier.fillMaxSize(),
                    color           = if (yearPassed) AccentGreen else AccentGold,
                    strokeWidth     = 8.dp,
                    strokeCap       = StrokeCap.Round
                )
                Text(
                    text       = "${(progress * 100).toInt()}%",
                    color      = if (yearPassed) AccentGreen else AccentGold,
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Journey Progress", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Text(
                    if (yearPassed) "Complete! 🎉" else "${(progress * 365).toInt()} of 365 days",
                    color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold
                )
                Text(
                    if (yearPassed) "You can now unlock." else "Stay strong. Keep going.",
                    color = TextSecondary, fontSize = 11.sp
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Feature 8: Daily Quote Card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun QuoteCard(quote: QuoteManager.Quote) {
    Surface(
        shape    = RoundedCornerShape(16.dp),
        color    = BgCard,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.FormatQuote, contentDescription = null,
                    tint = AccentBlue, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Today's Motivation", color = AccentBlue,
                    fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.8.sp)
            }
            Text(
                text       = "\"${quote.text}\"",
                color      = TextPrimary,
                fontSize   = 13.sp,
                fontStyle  = FontStyle.Italic,
                lineHeight = 19.sp
            )
            Text(
                text      = "— ${quote.author}",
                color     = TextSecondary,
                fontSize  = 11.sp,
                modifier  = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Shared UI components (unchanged)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun StatusCard(icon: ImageVector, title: String, value: String, color: Color, detail: String) {
    Surface(shape = RoundedCornerShape(16.dp), color = BgCard, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(26.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text(title,  color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Text(value,  color = color,         fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(detail, color = TextSecondary, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun CountdownCard(bd: TimerManager.Breakdown, isComplete: Boolean) {
    val gradientBrush = Brush.horizontalGradient(
        listOf(AccentGold.copy(alpha = 0.25f), AccentGoldDim.copy(alpha = 0.1f))
    )
    Surface(shape = RoundedCornerShape(16.dp), color = BgCard,
        modifier = Modifier.fillMaxWidth().border(1.dp, AccentGold.copy(alpha = 0.3f), RoundedCornerShape(16.dp))) {
        Column(Modifier.background(gradientBrush).padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Timer, contentDescription = null, tint = AccentGold)
                Spacer(Modifier.width(6.dp))
                Text(if (isComplete) "1 Year Complete!" else "Time Remaining",
                    color = AccentGold, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(14.dp))
            if (!isComplete) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                    modifier = Modifier.fillMaxWidth()) {
                    TimeUnit(bd.days,    "DAYS")
                    TimeUnitDivider()
                    TimeUnit(bd.hours,   "HRS")
                    TimeUnitDivider()
                    TimeUnit(bd.minutes, "MIN")
                    TimeUnitDivider()
                    TimeUnit(bd.seconds, "SEC")
                }
            } else {
                Text("You made it! 🎉", fontSize = 22.sp, color = AccentGold, fontWeight = FontWeight.Bold)
                Text("You can now unlock in Settings.", color = TextSecondary, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun TimeUnit(value: Long, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value.toString().padStart(2, '0'), color = TextPrimary, fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Text(label, color = TextSecondary, fontSize = 10.sp, letterSpacing = 1.sp)
    }
}

@Composable
private fun TimeUnitDivider() {
    Text(":", color = AccentGold, fontSize = 28.sp, fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 10.dp))
}

@Composable
private fun LockButton(loading: Boolean, enabled: Boolean = true, onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.04f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "scale"
    )
    Button(
        onClick  = { if (!loading && enabled) onClick() },
        enabled  = !loading && enabled,
        modifier = Modifier.fillMaxWidth().height(60.dp).scale(if (!loading && enabled) scale else 1f),
        shape    = RoundedCornerShape(16.dp),
        colors   = ButtonDefaults.buttonColors(
            containerColor = AccentGold,
            disabledContainerColor = AccentGoldDim.copy(alpha = 0.4f)
        )
    ) {
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp, color = Color.Black.copy(alpha = 0.6f))
        } else {
            Icon(Icons.Default.Lock, contentDescription = null, tint = if (enabled) Color.Black else TextSecondary)
            Spacer(Modifier.width(10.dp))
            Text("SEAL 1-YEAR COMMITMENT", color = if (enabled) Color.Black else TextSecondary, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, letterSpacing = 0.5.sp)
        }
    }
}

@Composable
private fun SeriousWarningBanner() {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = AccentRed.copy(alpha = 0.12f),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, AccentRed.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
    ) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = AccentRed, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    "TARGET: 1 YEAR OF COMPLETE FREEDOM",
                    color = AccentRed,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp,
                    letterSpacing = 0.5.sp
                )
            }
            Text(
                "This app is for individuals 100% committed to quitting porn. Once locked, there is STRICTLY NO WAY to view adult content or bypass the shield for 365 days. If you are not serious, do not activate.",
                color = TextPrimary.copy(alpha = 0.9f),
                fontSize = 12.sp,
                lineHeight = 17.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun SetupStepCard(
    stepNumber: Int,
    icon: ImageVector,
    title: String,
    description: String,
    isComplete: Boolean,
    actionButtonText: String,
    onAction: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = BgCard,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isComplete) AccentGreen.copy(alpha = 0.4f) else AccentGold.copy(alpha = 0.3f),
                RoundedCornerShape(16.dp)
            )
    ) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isComplete) AccentGreen.copy(alpha = 0.15f) else AccentGold.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (isComplete) Icons.Default.CheckCircle else icon,
                    contentDescription = null,
                    tint = if (isComplete) AccentGreen else AccentGold,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    title,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    description,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }

            if (isComplete) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AccentGreen.copy(alpha = 0.15f)
                ) {
                    Text(
                        "DONE",
                        color = AccentGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            } else {
                Button(
                    onClick = onAction,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGold),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        actionButtonText,
                        color = Color.Black,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun PrimaryButton(text: String, icon: ImageVector, loading: Boolean, onClick: () -> Unit) {
    Button(
        onClick  = { if (!loading) onClick() },
        enabled  = !loading,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        shape    = RoundedCornerShape(14.dp),
        colors   = ButtonDefaults.buttonColors(containerColor = AccentBlue)
    ) {
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
        } else {
            Icon(icon, contentDescription = null, tint = Color.Black)
            Spacer(Modifier.width(8.dp))
            Text(text, color = Color.Black, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun LockedInfo() {
    Surface(shape = RoundedCornerShape(14.dp), color = AccentGold.copy(alpha = 0.08f),
        modifier = Modifier.fillMaxWidth().border(1.dp, AccentGold.copy(alpha = 0.3f), RoundedCornerShape(14.dp))) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Lock, contentDescription = null, tint = AccentGold)
            Spacer(Modifier.width(10.dp))
            Column {
                Text("Device is locked.", color = AccentGold, fontWeight = FontWeight.SemiBold)
                Text("Open Settings (⚙) to view unlock status.", color = TextSecondary, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun InfoText(text: String) {
    Text(text = text, color = TextSecondary, fontSize = 12.sp, textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp))
}

@Composable
private fun SectionTitle(title: String) {
    Text(title, color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
}

@Composable
private fun RestrictionRow(label: String, active: Boolean) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(
            if (active) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint     = if (active) AccentGold else TextSecondary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(label, color = if (active) TextPrimary else TextSecondary, fontSize = 14.sp)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Accessibility Guard Card
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Checks both the Settings.Secure database AND GuardAccessibilityService.isRunning via AccessibilityHelper.
 */
fun getAccessibilityState(context: android.content.Context): AccessibilityState =
    AccessibilityHelper.getAccessibilityState(context)

fun requestIgnoreBatteryOptimization(context: android.content.Context) {
    try {
        val intent = android.content.Intent(
            android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
        ).apply {
            data = android.net.Uri.parse("package:${context.packageName}")
        }
        context.startActivity(intent)
    } catch (_: Exception) {
        val fallback = android.content.Intent(
            android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
        )
        context.startActivity(fallback)
    }
}

/**
 * Checks if DnsGuard is exempted from battery optimizations (Doze mode).
 */
fun isBatteryOptimizationIgnored(context: android.content.Context): Boolean {
    val pm = context.getSystemService(android.content.Context.POWER_SERVICE) as? android.os.PowerManager ?: return true
    return pm.isIgnoringBatteryOptimizations(context.packageName)
}

/**
 * Card that shows live accessibility guard status (ACTIVE, FROZEN/ZOMBIE, or NOT ENABLED).
 */
@Composable
fun AccessibilityGuardCard(state: AccessibilityState, activity: ComponentActivity) {
    val (statusColor, statusText, statusDesc, btnText) = when (state) {
        AccessibilityState.ACTIVE -> listOf(
            AccentGreen,
            "ACTIVE",
            "Dhizuku UI & Settings are guarded against tampering.",
            null
        )
        AccessibilityState.ZOMBIE -> listOf(
            AccentOrange,
            "FROZEN (RESTART NEEDED)",
            "⚠ Disconnected overnight. Tap Fix Now to toggle OFF then ON.",
            "Fix Now"
        )
        AccessibilityState.DISABLED -> listOf(
            AccentRed,
            "NOT ENABLED",
            "⚠ Enable this to block the Dhizuku deactivate button.",
            "Enable"
        )
    }

    val color = statusColor as Color
    val title = statusText as String
    val desc  = statusDesc as String
    val buttonLabel = btnText as? String

    Surface(
        shape    = RoundedCornerShape(16.dp),
        color    = BgCard,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                color.copy(alpha = 0.4f),
                RoundedCornerShape(16.dp)
            )
    ) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Icon
            Box(
                Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (state == AccessibilityState.ACTIVE) Icons.Default.Accessibility else Icons.Default.AccessibilityNew,
                    contentDescription = null,
                    tint     = color,
                    modifier = Modifier.size(26.dp)
                )
            }

            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    "Accessibility Guard",
                    color      = TextSecondary,
                    fontSize   = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    title,
                    color      = color,
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    desc,
                    color    = TextSecondary,
                    fontSize = 11.sp
                )
            }

            // Action button (Fix Now or Enable)
            if (buttonLabel != null) {
                Button(
                    onClick = {
                        val intent = android.content.Intent(
                            android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS
                        )
                        activity.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = color),
                    shape  = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(buttonLabel, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * Card that warns if Battery Optimization is active (which kills the app overnight).
 * Hidden if the app is already unrestricted.
 */
@Composable
fun BatteryOptimizationCard(isIgnored: Boolean, activity: ComponentActivity) {
    if (isIgnored) return // Clean UI: hide if already unrestricted

    Surface(
        shape    = RoundedCornerShape(16.dp),
        color    = BgCard,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                AccentOrange.copy(alpha = 0.4f),
                RoundedCornerShape(16.dp)
            )
    ) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AccentOrange.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.BatteryAlert,
                    contentDescription = null,
                    tint     = AccentOrange,
                    modifier = Modifier.size(26.dp)
                )
            }

            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    "Battery Optimization",
                    color      = TextSecondary,
                    fontSize   = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    "RESTRICTED",
                    color      = AccentOrange,
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Android may kill guard overnight. Set to Unrestricted.",
                    color    = TextSecondary,
                    fontSize = 11.sp
                )
            }

            Button(
                onClick = { requestIgnoreBatteryOptimization(activity) },
                colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
                shape  = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("Fix", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// New Features: Calm Banner, Milestones, Telemetry, Daily Story
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CalmActionBanner(onOpenBreathing: () -> Unit) {
    Surface(
        onClick = onOpenBreathing,
        shape = RoundedCornerShape(16.dp),
        color = BgCard,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, AccentGold.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(AccentGold.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Spa, contentDescription = null, tint = AccentGold)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Need Calm & Clarity?", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("Tap for 4-4-4-4 Box Breathing & Grounding", color = TextSecondary, fontSize = 12.sp)
            }
            Icon(Icons.Default.ArrowForwardIos, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun MilestoneProgressionCard(activity: ComponentActivity) {
    val elapsedDays = remember { MilestoneManager.getElapsedDays(activity) }
    val progressList = remember(elapsedDays) { MilestoneManager.getMilestoneProgress(activity) }
    val nextMilestone = remember(elapsedDays) { MilestoneManager.getNextMilestone(activity) }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = BgCard,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = AccentGold)
                    Spacer(Modifier.width(8.dp))
                    Text("Journey Milestones", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                Text("$elapsedDays Days Conquered", color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            nextMilestone?.let { next ->
                val daysToGo = next.day - elapsedDays
                Text(
                    "Next Goal: ${next.badgeEmoji} ${next.title} (in $daysToGo days)",
                    color = AccentBlue,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Horizontal Badges Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                progressList.forEach { (milestone, unlocked) ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (unlocked) AccentGold.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.03f),
                        modifier = Modifier.border(
                            1.dp,
                            if (unlocked) AccentGold.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.08f),
                            RoundedCornerShape(12.dp)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(milestone.badgeEmoji, fontSize = 22.sp)
                            Text(
                                "Day ${milestone.day}",
                                color = if (unlocked) AccentGold else TextSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                            Text(
                                milestone.title,
                                color = if (unlocked) TextPrimary else TextSecondary.copy(alpha = 0.6f),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DnsTelemetryCard(activity: ComponentActivity) {
    var diagnostic by remember { mutableStateOf<DnsDiagnosticHelper.DiagnosticResult?>(null) }
    var isChecking by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        diagnostic = DnsDiagnosticHelper.runDiagnostic(activity)
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = BgCard,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.NetworkCheck, contentDescription = null, tint = AccentBlue)
                    Spacer(Modifier.width(8.dp))
                    Text("DNS Shield Telemetry", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }

                IconButton(
                    onClick = {
                        isChecking = true
                        CoroutineScope(Dispatchers.IO).launch {
                            val res = DnsDiagnosticHelper.runDiagnostic(activity)
                            withContext(Dispatchers.Main) {
                                diagnostic = res
                                isChecking = false
                            }
                        }
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    if (isChecking) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = AccentBlue)
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = "Test DNS", tint = AccentBlue, modifier = Modifier.size(18.dp))
                    }
                }
            }

            val d = diagnostic
            if (d != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Active Resolver", color = TextSecondary, fontSize = 11.sp)
                        Text(d.dnsHost, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Shield Health", color = TextSecondary, fontSize = 11.sp)
                        val isHealthy = d.isConfigured && d.isResolving
                        Text(
                            if (isHealthy) "● 100% Protected" else "● Checking...",
                            color = if (isHealthy) AccentGreen else AccentGold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (d.latencyMs >= 0) {
                    Text(
                        "Resolution Latency: ${d.latencyMs} ms",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            } else {
                Text("Analyzing DNS connection...", color = TextSecondary, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun DailyWisdomStoryCard(onOpenVault: () -> Unit) {
    val story = remember { NightlyMotivationManager.getTodayStory() }
    var expanded by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = BgCard,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(AccentGold.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(story.emoji, fontSize = 20.sp)
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            story.title,
                            color = AccentGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            "${story.categoryEmoji} ${story.category}",
                            color = AccentBlue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                OutlinedButton(
                    onClick = onOpenVault,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Icon(Icons.Default.AutoStories, contentDescription = null, tint = AccentGold, modifier = Modifier.size(13.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Vault (50)", color = AccentGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Text(
                if (expanded) story.body else story.body.take(120) + "...",
                color = TextSecondary,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            if (expanded) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = AccentGold.copy(alpha = 0.08f),
                    modifier = Modifier.fillMaxWidth().border(1.dp, AccentGold.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text("✨", fontSize = 13.sp)
                        Spacer(Modifier.width(6.dp))
                        Column {
                            Text("MORAL", color = AccentGold, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                            Text(story.moral, color = TextPrimary, fontSize = 12.sp, fontStyle = FontStyle.Italic)
                        }
                    }
                }
            }

            TextButton(
                onClick = { expanded = !expanded },
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(if (expanded) "Show Less" else "Read Full Story", color = AccentGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}


