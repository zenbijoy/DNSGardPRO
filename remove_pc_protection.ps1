# ==============================================================================
# PC Protection Removal Script (Revert Changes)
# ==============================================================================

$isAdmin = ([Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
if (-not $isAdmin) {
    Start-Process powershell -Verb RunAs -ArgumentList "-NoProfile -ExecutionPolicy Bypass -File `"$PSCommandPath`""
    exit
}

Write-Host "Reverting PC Protection settings..." -ForegroundColor Yellow

# 1. Reset DNS to DHCP (Automatic)
Get-NetAdapter | Where-Object { $_.Status -eq "Up" } | ForEach-Object {
    Set-DnsClientServerAddress -InterfaceIndex $_.ifIndex -ResetServerAddresses -ErrorAction SilentlyContinue
}

# 2. Remove Network Lock Policies
Remove-ItemProperty -Path "HKLM:\SOFTWARE\Policies\Microsoft\Windows\Network Connections" -Name "NC_LanProperties" -ErrorAction SilentlyContinue
Remove-ItemProperty -Path "HKLM:\SOFTWARE\Policies\Microsoft\Windows\Network Connections" -Name "NC_AllowAdvancedTCPIPConfig" -ErrorAction SilentlyContinue

# 3. Remove Browser Policies
$bPaths = @(
    "HKLM:\SOFTWARE\Policies\Google\Chrome",
    "HKLM:\SOFTWARE\Policies\Microsoft\Edge",
    "HKLM:\SOFTWARE\Policies\BraveSoftware\Brave"
)
foreach ($bp in $bPaths) {
    Remove-ItemProperty -Path $bp -Name "DnsOverHttpsMode" -ErrorAction SilentlyContinue
    Remove-ItemProperty -Path $bp -Name "DnsOverHttpsTemplates" -ErrorAction SilentlyContinue
    Remove-ItemProperty -Path $bp -Name "ForceGoogleSafeSearch" -ErrorAction SilentlyContinue
    Remove-ItemProperty -Path $bp -Name "ForceYouTubeRestrict" -ErrorAction SilentlyContinue
    Remove-ItemProperty -Path $bp -Name "IncognitoModeAvailability" -ErrorAction SilentlyContinue
    Remove-ItemProperty -Path $bp -Name "InPrivateModeAvailability" -ErrorAction SilentlyContinue
}

# 4. Clean hosts file
$hostsPath = "$env:SystemRoot\System32\drivers\etc\hosts"
Set-ItemProperty -Path $hostsPath -Name IsReadOnly -Value $false -ErrorAction SilentlyContinue
$content = Get-Content -Path $hostsPath -Raw -ErrorAction SilentlyContinue
if ($content) {
    $cleaned = $content -replace "(?ms)# ==========================================.*?duckduckgo.com\r?\n?", ""
    Set-Content -Path $hostsPath -Value $cleaned -Encoding UTF8
}

Clear-DnsClientCache
ipconfig /flushdns | Out-Null
Write-Host "All PC protection settings have been reset to default." -ForegroundColor Green
[void][System.Console]::ReadKey($true)
