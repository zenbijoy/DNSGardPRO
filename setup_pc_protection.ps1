# ==============================================================================
# PC Anti-Addiction & Adult Content Protection Setup Script
# Configures: Clean DNS, DoH, SafeSearch, Disables Incognito, Locks Network Config
# ==============================================================================

# Ensure script is running with elevated administrator privileges
$isAdmin = ([Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
if (-not $isAdmin) {
    Write-Host "[!] Requesting Administrator privileges..." -ForegroundColor Yellow
    Start-Process powershell -Verb RunAs -ArgumentList "-NoProfile -ExecutionPolicy Bypass -File `"$PSCommandPath`""
    exit
}

Clear-Host
Write-Host "====================================================================" -ForegroundColor Cyan
Write-Host "       ACTIVATING FULL PC ADULT CONTENT SHIELD (WINDOWS 11)        " -ForegroundColor Cyan
Write-Host "====================================================================" -ForegroundColor Cyan
Write-Host ""

# ------------------------------------------------------------------------------
# 1. CONFIGURE ADULT-BLOCKING DNS (IPv4 + IPv6) ON ALL ADAPTERS
# ------------------------------------------------------------------------------
Write-Host "[1/5] Applying Cloudflare Family Safe DNS (1.1.1.3 and 1.0.0.3)..." -ForegroundColor Yellow

$adapters = Get-NetAdapter | Where-Object { $_.Status -eq "Up" }
foreach ($adapter in $adapters) {
    Write-Host "  -> Configuring adapter: $($adapter.Name)" -ForegroundColor Gray
    try {
        # Set IPv4 Clean DNS
        Set-DnsClientServerAddress -InterfaceIndex $adapter.ifIndex -ServerAddresses @("1.1.1.3", "1.0.0.3") -ErrorAction SilentlyContinue
        
        # Set IPv6 Clean DNS
        Set-DnsClientServerAddress -InterfaceIndex $adapter.ifIndex -ServerAddresses @("2606:4700:4700::1113", "2606:4700:4700::1003") -AddressFamily IPv6 -ErrorAction SilentlyContinue
    } catch {
        Write-Warning "Could not update adapter $($adapter.Name)"
    }
}

Clear-DnsClientCache
Write-Host "  [+] Cloudflare 1.1.1.1 for Families (Adult and Malware Block) Activated." -ForegroundColor Green
Write-Host ""

# ------------------------------------------------------------------------------
# 2. LOCK NETWORK ADAPTER PROPERTIES (PREVENTS CHANGING DNS IN SETTINGS)
# ------------------------------------------------------------------------------
Write-Host "[2/5] Locking Network Adapter Settings and DNS Configuration..." -ForegroundColor Yellow

$netPolicies = @(
    "HKLM:\SOFTWARE\Policies\Microsoft\Windows\Network Connections",
    "HKCU:\Software\Policies\Microsoft\Windows\Network Connections"
)

foreach ($path in $netPolicies) {
    if (-not (Test-Path $path)) {
        New-Item -Path $path -Force | Out-Null
    }
    # Prohibit access to properties of a LAN connection
    Set-ItemProperty -Path $path -Name "NC_LanProperties" -Value 0 -Type DWord -Force
    # Prohibit access to advanced TCP/IP configuration
    Set-ItemProperty -Path $path -Name "NC_AllowAdvancedTCPIPConfig" -Value 0 -Type DWord -Force
}

Write-Host "  [+] Network Adapter Properties Locked via Group Policy." -ForegroundColor Green
Write-Host ""

# ------------------------------------------------------------------------------
# 3. ENFORCE BROWSER-LEVEL POLICIES (CHROME, EDGE, BRAVE)
#    - Forces Encrypted Family DNS (DoH)
#    - Disables Incognito / InPrivate browsing
#    - Enforces Strict SafeSearch on Google and Bing
#    - Enforces Restricted Mode on YouTube
# ------------------------------------------------------------------------------
Write-Host "[3/5] Applying Browser Enterprise Policies (Chrome / Edge / Brave)..." -ForegroundColor Yellow

$browserPaths = @(
    "HKLM:\SOFTWARE\Policies\Google\Chrome",
    "HKLM:\SOFTWARE\Policies\Microsoft\Edge",
    "HKLM:\SOFTWARE\Policies\BraveSoftware\Brave"
)

foreach ($bPath in $browserPaths) {
    if (-not (Test-Path $bPath)) {
        New-Item -Path $bPath -Force | Out-Null
    }

    # 1. Force Secure DNS over HTTPS using Cloudflare Family
    Set-ItemProperty -Path $bPath -Name "DnsOverHttpsMode" -Value "secure" -Type String -Force
    Set-ItemProperty -Path $bPath -Name "DnsOverHttpsTemplates" -Value "https://family.cloudflare-dns.com/dns-query" -Type String -Force

    # 2. Force SafeSearch (Google, Bing)
    Set-ItemProperty -Path $bPath -Name "ForceGoogleSafeSearch" -Value 1 -Type DWord -Force

    # 3. Force YouTube Strict Restricted Mode (blocks explicit videos and comments)
    Set-ItemProperty -Path $bPath -Name "ForceYouTubeRestrict" -Value 2 -Type DWord -Force

    # 4. Disable Incognito / InPrivate mode (prevents private bypass)
    Set-ItemProperty -Path $bPath -Name "IncognitoModeAvailability" -Value 1 -Type DWord -Force
    Set-ItemProperty -Path $bPath -Name "InPrivateModeAvailability" -Value 1 -Type DWord -Force
}

Write-Host "  [+] Browser Policies Applied: DoH Enforced, Incognito Disabled, SafeSearch Forced." -ForegroundColor Green
Write-Host ""

# ------------------------------------------------------------------------------
# 4. HARDEN HOSTS FILE FOR STRICT SAFESEARCH REDIRECTS
# ------------------------------------------------------------------------------
Write-Host "[4/5] Hardening System Hosts File with Strict SafeSearch VIPs..." -ForegroundColor Yellow

$hostsPath = "$env:SystemRoot\System32\drivers\etc\hosts"

if (Test-Path $hostsPath) {
    Set-ItemProperty -Path $hostsPath -Name IsReadOnly -Value $false -ErrorAction SilentlyContinue
}

$safeSearchEntries = "`r`n# Self-Control SafeSearch Enforcements`r`n216.239.38.120 www.google.com`r`n216.239.38.120 google.com`r`n204.79.197.220 www.bing.com`r`n204.79.197.220 bing.com`r`n52.142.124.215 duckduckgo.com`r`n"

$existingHosts = ""
if (Test-Path $hostsPath) {
    $existingHosts = Get-Content -Path $hostsPath -Raw -ErrorAction SilentlyContinue
}

if ($existingHosts -notmatch "Self-Control SafeSearch Enforcements") {
    Add-Content -Path $hostsPath -Value $safeSearchEntries -Encoding ASCII
    Write-Host "  [+] SafeSearch VIP redirects added to hosts file." -ForegroundColor Green
} else {
    Write-Host "  [+] SafeSearch VIP redirects already present in hosts file." -ForegroundColor Green
}

Set-ItemProperty -Path $hostsPath -Name IsReadOnly -Value $true -ErrorAction SilentlyContinue

Write-Host ""

# ------------------------------------------------------------------------------
# 5. REFRESH SYSTEM SERVICES AND NETWORKING
# ------------------------------------------------------------------------------
Write-Host "[5/5] Flushing DNS Cache and Applying Changes..." -ForegroundColor Yellow
Clear-DnsClientCache
ipconfig /flushdns | Out-Null
Write-Host "  [+] System DNS Cache Flushed." -ForegroundColor Green
Write-Host ""

Write-Host "====================================================================" -ForegroundColor Cyan
Write-Host "             PC PROTECTION ACTIVATION COMPLETE! [SECURED]           " -ForegroundColor Green
Write-Host "====================================================================" -ForegroundColor Cyan
Write-Host "Summary of Protections Activated:" -ForegroundColor White
Write-Host "  1. DNS Filtering:    Cloudflare Family (blocks all adult sites and malware)" -ForegroundColor Cyan
Write-Host "  2. DNS Protection:   Network Adapter properties locked in Settings" -ForegroundColor Cyan
Write-Host "  3. Browser Security: Encrypted DoH locked on Chrome, Edge and Brave" -ForegroundColor Cyan
Write-Host "  4. Incognito Mode:   DISABLED across all browsers" -ForegroundColor Cyan
Write-Host "  5. Search Engines:   Google and Bing forced to Strict SafeSearch" -ForegroundColor Cyan
Write-Host "  6. YouTube:          Strict Restricted Mode enforced" -ForegroundColor Cyan
Write-Host ""
Write-Host "Please restart your browsers (Chrome / Edge / Brave) for policies to take effect." -ForegroundColor Yellow
Write-Host ""
Write-Host "Press any key to close this window..." -ForegroundColor Gray
[void][System.Console]::ReadKey($true)
