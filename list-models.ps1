# List available Gemini models
$apiKey = "AIzaSyALbmcSq_v5z9YBvaw_gJPiHLFjnOuCl-U"
$url = "https://generativelanguage.googleapis.com/v1beta/models?key=$apiKey"

Write-Host "Fetching available models..."

try {
    $response = Invoke-RestMethod -Uri $url -Method GET
    Write-Host "Available Models:"
    $response.models | ForEach-Object { 
        Write-Host "- $($_.name)"
        Write-Host "  Display Name: $($_.displayName)"
        Write-Host "  Description: $($_.description)"
        Write-Host ""
    }
} catch {
    Write-Host "Error getting models: $($_.Exception.Message)"
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "Response Body: $responseBody"
    }
}
