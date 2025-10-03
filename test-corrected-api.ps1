# Test Gemini API with Corrected JSON Structure
$apiKey = "AIzaSyALbmcSq_v5z9YBvaw_gJPiHLFjnOuCl-U"
$url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey"

# Correct JSON structure
$body = @{
    contents = @(
        @{
            parts = @(
                @{
                    text = "Hello, test message"
                }
            )
        }
    )
    generationConfig = @{
        maxOutputTokens = 8192
        temperature = 0.7
    }
} | ConvertTo-Json -Depth 4

Write-Host "Testing API Key: $($apiKey.Substring(0,10))..."
Write-Host "URL: $url"
Write-Host "Body: $body"
Write-Host ""

try {
    $response = Invoke-RestMethod -Uri $url -Method POST -ContentType "application/json" -Body $body
    Write-Host "SUCCESS: API call worked!"
    Write-Host "Response:"
    $response | ConvertTo-Json -Depth 3
} catch {
    Write-Host "ERROR: API call failed"
    Write-Host "Error Message: $($_.Exception.Message)"
    Write-Host "Status Code: $($_.Exception.Response.StatusCode)"
    Write-Host "Response Body:"
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host $responseBody
    }
}
