$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$listPath = Join-Path $PSScriptRoot 'cwe-list.md'
$outMd = Join-Path $PSScriptRoot 'cwe-title-description.md'
$outCsv = Join-Path $PSScriptRoot 'cwe-title-description.csv'

function Get-CweLanguage([int]$id, [string]$html) {
  # Extract the Languages cell from the Applicable Platforms table
  $langPattern = "(?s)oc_${id}_Applicable_Platforms.{0,4000}?<td[^>]*>\s*Languages\s*</td>\s*<td[^>]*>(.*?)</td>"
  $langMatch   = [regex]::Match($html, $langPattern, [Text.RegularExpressions.RegexOptions]::IgnoreCase)
  if (-not $langMatch.Success) { return '' }

  # Strip tags, decode HTML entities, collapse whitespace
  $raw  = $langMatch.Groups[1].Value -replace '<[^>]+>', ' ' -replace '\s+', ' '
  $text = [System.Net.WebUtility]::HtmlDecode($raw).Trim()

  # Remove prevalence qualifiers such as "(Often Prevalent)" / "(Undetermined Prevalence)"
  $clean = $text -replace '\((Often|Undetermined|Sometimes|Rarely) Prevalent\)', '' `
                 -replace '\s+', ' ' `
                 -replace 'Class:\s*', '' `
                 | ForEach-Object { $_.Trim() }

  # Memory-Unsafe or explicit C + C++ → C/C++
  if ($clean -match 'Memory-Unsafe' -or ($clean -match '\bC\b' -and $clean -match '\bC\+\+\b')) {
    return 'C/C++'
  }

  # SQL named explicitly → SQL (takes priority over Not Language-Specific)
  if ($clean -match '\bSQL\b') {
    return 'SQL'
  }

  # Not Language-Specific → all
  if ($clean -match 'Not Language-Specific') {
    return 'all'
  }

  # Named language list – scan for known language names
  $knownLangs = @('Java','Python','PHP','Ruby','JavaScript','Perl','C#','VB\.NET',
                  'ASP\.NET','Go','Rust','Swift','Kotlin','Scala','R','COBOL',
                  'Fortran','Ada','Assembly')
  $found = @()
  foreach ($lang in $knownLangs) {
    if ($clean -match "\b$lang\b") {
      $found += $lang -replace '\\', ''
    }
  }
  if ($found.Count -gt 0) { return $found -join ', ' }

  return ''
}

$ids = Get-Content $listPath |
  ForEach-Object { $_.Trim() } |
  Where-Object { $_ -match '^CWE-\d+$' } |
  ForEach-Object { [int]($_ -replace 'CWE-', '') }

$results = @()
foreach ($id in $ids) {
  $url = "https://cwe.mitre.org/data/definitions/$id.html"
  try {
    $content = (Invoke-WebRequest -Uri $url -UseBasicParsing -TimeoutSec 60).Content

    $titlePattern = "CWE-$($id):\s*([^<\r\n]+)"
    $titleMatch = [regex]::Match($content, $titlePattern)
    $rawTitle = if ($titleMatch.Success) { $titleMatch.Groups[1].Value.Trim() } else { '' }
    $rawTitle = $rawTitle -replace '\s*\(\d+\.\d+(?:\.\d+)?\)\s*$', ''
    $title = [System.Net.WebUtility]::HtmlDecode($rawTitle)

    $rawDesc = ''
    $descPatterns = @(
      ('<div name="oc_{0}_Description"[\s\S]*?<div class="indent">([\s\S]*?)</div>' -f $id),
      ('<div name="oc_{0}_Description"[\s\S]*?<td[^>]*>([\s\S]*?)</td>' -f $id),
      ('<div id="Description">[\s\S]*?<a href="javascript:toggleblocksOC\(''{0}_Description''\);">[\s\S]*?<div class="indent">([\s\S]*?)</div>' -f $id)
    )
    foreach ($pattern in $descPatterns) {
      $descMatch = [regex]::Match($content, $pattern, [Text.RegularExpressions.RegexOptions]::IgnoreCase)
      if ($descMatch.Success -and -not [string]::IsNullOrWhiteSpace($descMatch.Groups[1].Value)) {
        $rawDesc = $descMatch.Groups[1].Value
        break
      }
    }
    $cleanDesc = $rawDesc -replace '<[^>]+>', ' ' -replace '\s+', ' '
    $desc = [System.Net.WebUtility]::HtmlDecode($cleanDesc).Trim()

    if ([string]::IsNullOrWhiteSpace($title)) { $title = '(not found)' }
    if ([string]::IsNullOrWhiteSpace($desc)) { $desc = '(not found)' }

    $language = Get-CweLanguage $id $content

    $results += [pscustomobject]@{
      CWE_ID = "CWE-$id"
      Title = $title
      Description = $desc
      Language = $language
      Url = $url
      Status = 'ok'
    }
  }
  catch {
    $results += [pscustomobject]@{
      CWE_ID = "CWE-$id"
      Title = '(error)'
      Description = $_.Exception.Message
      Language = ''
      Url = $url
      Status = 'error'
    }
  }
}

$results | Export-Csv -NoTypeInformation -Encoding UTF8 -Path $outCsv

$md = @()
$md += '# CWE Title and Description'
$md += ''
foreach ($r in $results) {
  $md += "## $($r.CWE_ID): $($r.Title)"
  $md += ''
  $md += "- URL: $($r.Url)"
  $md += "- Language: $($r.Language)"
  $md += "- Description: $($r.Description)"
  $md += ''
}
Set-Content -Path $outMd -Value $md -Encoding UTF8

Write-Host "Extracted: $($results.Count)"
Write-Host "Errors: $((($results | Where-Object Status -eq 'error').Count))"
$results | Select-Object -First 10 | Format-Table -AutoSize
