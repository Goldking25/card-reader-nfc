#!/usr/bin/env pwsh
<#
.SYNOPSIS
    Tags a new release and pushes it to trigger the GitHub Actions release pipeline.

.DESCRIPTION
    1. Validates the repo is clean (no uncommitted changes)
    2. Creates a git tag like v1.0.0
    3. Pushes the tag to origin → triggers .github/workflows/release.yml

.EXAMPLE
    .\scripts\tag_release.ps1 -Version "1.0.0"
    .\scripts\tag_release.ps1 -Version "1.1.0" -Message "Add FeliCa improvements"
#>

param(
    [Parameter(Mandatory=$true)]
    [string]$Version,

    [string]$Message = "Release v$Version"
)

$ErrorActionPreference = "Stop"
$tag = "v$Version"

Write-Host "`n=== NFC PoC Release: $tag ===" -ForegroundColor Cyan

# 1. Check for uncommitted changes
$status = git status --porcelain
if ($status) {
    Write-Host "ERROR: Uncommitted changes detected. Commit or stash before releasing." -ForegroundColor Red
    Write-Host $status
    exit 1
}

# 2. Ensure we are on main
$branch = git rev-parse --abbrev-ref HEAD
if ($branch -ne "main") {
    Write-Host "WARNING: You are on branch '$branch', not 'main'." -ForegroundColor Yellow
    $confirm = Read-Host "Continue? (y/N)"
    if ($confirm -notmatch "^[yY]$") { exit 0 }
}

# 3. Pull latest
Write-Host "Pulling latest from origin/$branch..." -ForegroundColor Gray
git pull origin $branch

# 4. Create annotated tag
Write-Host "Creating tag $tag..." -ForegroundColor Gray
git tag -a $tag -m $Message

# 5. Push tag
Write-Host "Pushing $tag to origin..." -ForegroundColor Gray
git push origin $tag

Write-Host "`n[OK] Tag $tag pushed!" -ForegroundColor Green
Write-Host "     GitHub Actions release pipeline will start shortly."
Write-Host "     Check: https://github.com/<your-org>/<your-repo>/actions`n"
