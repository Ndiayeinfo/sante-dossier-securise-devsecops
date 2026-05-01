# Crée une branche par membre à partir de develop et pousse sur origin.
# Usage (depuis la racine du dépôt Git) :
#   .\scripts\creer-branches-equipe.ps1 -Membres @("Alice","Bob","Karim")
# Convention : branche equipe/<prenom> en minuscules sans espaces (tirets OK).

param(
  [Parameter(Mandatory = $true)]
  [string[]] $Membres
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$root = Resolve-Path (Join-Path $PSScriptRoot '..')
Set-Location $root
if (-not (Test-Path (Join-Path $root '.git'))) {
  throw "Pas de dépôt Git à la racine: $root"
}

function Normalize-BranchSegment([string] $name) {
  $s = $name.Trim().ToLowerInvariant() -replace '\s+', '-'
  if ($s -notmatch '^[a-z0-9][a-z0-9._-]*$') {
    throw "Nom invalide pour une branche Git: '$name' -> '$s'"
  }
  return $s
}

git fetch origin
git checkout develop
git pull origin develop

foreach ($m in $Membres) {
  $seg = Normalize-BranchSegment $m
  $branch = "equipe/$seg"
  git checkout develop
  git checkout -b $branch
  git push -u origin $branch
  Write-Host "OK: $branch"
}

git checkout develop
Write-Host "Terminé. Branche courante: develop"
