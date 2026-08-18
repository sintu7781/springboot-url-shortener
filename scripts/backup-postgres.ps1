try {
    $ErrorActionPreference = "Stop"

    $ProjectRoot = Split-Path -Parent $PSScriptRoot
    $ComposeFile = Join-Path $ProjectRoot "compose.prod.yaml"
    $BackupDir = Join-Path $ProjectRoot "backups"
    $LogDir = Join-Path $ProjectRoot "logs"
    $LogFile =Join-Path $LogDir "postgres-backup.log"

    New-Item -ItemType Directory -Force -Path $LogDir | Out-Null

    Start-Transcript -Path $LogFile -Append



    $Database = "url_shortener"
    $User = "postgres"
    $RetentionCount = 7

    New-Item -ItemType Directory -Force -Path $BackupDir | Out-Null

    Write-Host ""
    Write-Host "========================================"
    Write-Host " PostgreSQL Backup"
    Write-Host "========================================"
    Write-Host ""

    # Resolve the actual PostgreSQL container through Compose.
    $Container = docker compose -f $ComposeFile ps -q postgres

    if ($LASTEXITCODE -ne 0) {
        throw "Unable to query Docker Compose."
    }

    $Container = $Container.Trim()

    if ([string]::IsNullOrWhiteSpace($Container)) {
        throw "PostgreSQL container is not running."
    }

    Write-Host "Container : $Container"
    Write-Host "Database  : $Database"

    $Timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
    $FileName = "url_shortener_$Timestamp.dump"
    $ContainerBackupPath = "/tmp/$FileName"
    $HostBackupPath = Join-Path $BackupDir $FileName

    Write-Host "Backup    : $HostBackupPath"
    Write-Host ""

    # --------------------------------------------------
    # 1. Create PostgreSQL custom-format backup
    # --------------------------------------------------

    Write-Host "Creating PostgreSQL backup..."

    docker exec $Container `
        pg_dump `
        -U $User `
        -d $Database `
        -Fc `
        -f $ContainerBackupPath

    if ($LASTEXITCODE -ne 0) {
        throw "pg_dump failed with exit code $LASTEXITCODE."
    }

    Write-Host "pg_dump completed successfully."

    # --------------------------------------------------
    # 2. Verify backup exists inside container
    # --------------------------------------------------

    docker exec $Container `
        sh -c "test -s '$ContainerBackupPath'"

    if ($LASTEXITCODE -ne 0) {
        throw "Backup file was not created or is empty inside the PostgreSQL container."
    }

    # --------------------------------------------------
    # 3. Copy backup from container to host
    # --------------------------------------------------

    Write-Host "Copying backup to host..."

    docker cp `
        "${Container}:${ContainerBackupPath}" `
        $HostBackupPath

    if ($LASTEXITCODE -ne 0) {
        throw "docker cp failed with exit code $LASTEXITCODE."
    }

    # --------------------------------------------------
    # 4. Verify host backup
    # --------------------------------------------------

    if (-not (Test-Path $HostBackupPath)) {
        throw "Backup file does not exist on host."
    }

    $BackupFile = Get-Item $HostBackupPath

    if ($BackupFile.Length -eq 0) {
        Remove-Item $HostBackupPath -Force
        throw "Backup file on host is empty."
    }

    Write-Host "Backup copied successfully."
    Write-Host "Size      : $($BackupFile.Length) bytes"

    # --------------------------------------------------
    # 5. Verify PostgreSQL archive
    # --------------------------------------------------

    Write-Host "Validating backup archive..."

    docker exec $Container `
        pg_restore `
        --list `
        $ContainerBackupPath `
        | Out-Null

    if ($LASTEXITCODE -ne 0) {
        Remove-Item $HostBackupPath -Force
        throw "Backup archive validation failed."
    }

    Write-Host "Backup archive validation successful."

    # --------------------------------------------------
    # 6. Remove temporary container backup
    # --------------------------------------------------

    docker exec $Container `
        rm -f `
        $ContainerBackupPath

    if ($LASTEXITCODE -ne 0) {
        Write-Warning "Could not remove temporary backup from PostgreSQL container."
    }

    # --------------------------------------------------
    # 7. Retention policy
    # --------------------------------------------------

    Write-Host ""
    Write-Host "Applying retention policy..."

    $Backups = @(
        Get-ChildItem $BackupDir -Filter "url_shortener_*.dump" |
        Sort-Object LastWriteTime -Descending
    )

    if ($Backups.Count -gt $RetentionCount) {

        $OldBackups = $Backups |
            Select-Object -Skip $RetentionCount

        foreach ($OldBackup in $OldBackups) {
            Write-Host "Deleting old backup: $($OldBackup.Name)"
            Remove-Item $OldBackup.FullName -Force
        }
    }

    Write-Host ""
    Write-Host "========================================"
    Write-Host " Backup completed successfully"
    Write-Host "========================================"
    Write-Host ""
}
catch {
    Write-Error "PostgreSQL backup FAILED: $($_.Exception.Message)"
    exit 1
}
finally {
    Stop-Transcript
}