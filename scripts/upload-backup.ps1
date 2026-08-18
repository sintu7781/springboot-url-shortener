param(
    [Parameter(Mandatory = $false)]
    [string]$BackupFile
)

$ErrorActionPreference = "Stop"

$ProjectRoot = Split-Path -Parent $PSScriptRoot
$BackupDir = Join-Path $ProjectRoot "backups"

if ([string]::IsNullOrWhiteSpace($BackupFile)) {

    $LatestBackup = Get-ChildItem $BackupDir -Filter "url_shortener_*.dump" |
        Sort-Object LastWriteTime -Descending |
        Select-Object -First 1

    if ($null -eq $LatestBackup) {
        throw "No PostgreSQL backup found."
    }

    $BackupFile = $LatestBackup.FullName
}

if (-not (Test-Path $BackupFile)) {
    throw "Backup file does not exist: $BackupFile"
}

$File = Get-Item $BackupFile

if ($File.Length -eq 0) {
    throw "Backup file is empty: $BackupFile"
}

Write-Host ""
Write-Host "========================================"
Write-Host " Backup Upload Preparation"
Write-Host "========================================"
Write-Host ""

Write-Host "Backup selected:"
Write-Host $File.FullName
Write-Host "Size: $($File.Length) bytes"
Write-Host ""

Write-Host "Off-site upload is not configured yet."
Write-Host "Backup is ready for object-storage upload."