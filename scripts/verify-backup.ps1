$ErrorActionPreference = "Stop"

$ProjectRoot = Split-Path -Parent $PSScriptRoot
$BackupDir = Join-Path $ProjectRoot "backups"

$LatestBackup = Get-ChildItem $BackupDir -Filter "url_shortener_*.dump" |
    Sort-Object LastWriteTime -Descending |
    Select-Object -First 1

if ($null -eq $LatestBackup) {
    Write-Error "No PostgreSQL backup found."
    exit 1
}

$Age = (Get-Date) - $LatestBackup.LastWriteTime

Write-Host "Latest backup:"
Write-Host "File: $($LatestBackup.Name)"
Write-Host "Size: $($LatestBackup.Length) bytes"
Write-Host "Age : $($Age.TotalHours) hours"

if ($LatestBackup.Length -eq 0) {
    Write-Error "Latest backup is empty."
    exit 1
}

# Fail if the newest backup is older than 26 hours.
if ($Age.TotalHours -gt 26) {
    Write-Error "Latest backup is too old."
    exit 1
}

Write-Host "Backup health: OK"
exit 0