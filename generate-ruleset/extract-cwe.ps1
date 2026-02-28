$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$listPath = Join-Path $PSScriptRoot 'cwe-list.md'
$outMd = Join-Path $PSScriptRoot 'cwe-title-description.md'
$outCsv = Join-Path $PSScriptRoot 'cwe-title-description.csv'

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

    $results += [pscustomobject]@{
      CWE_ID = "CWE-$id"
      Title = $title
      Description = $desc
      Url = $url
      Status = 'ok'
    }
  }
  catch {
    $results += [pscustomobject]@{
      CWE_ID = "CWE-$id"
      Title = '(error)'
      Description = $_.Exception.Message
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
  $md += "- Description: $($r.Description)"
  $md += ''
}
Set-Content -Path $outMd -Value $md -Encoding UTF8

Write-Host "Extracted: $($results.Count)"
Write-Host "Errors: $((($results | Where-Object Status -eq 'error').Count))"
$results | Select-Object -First 10 | Format-Table -AutoSize
