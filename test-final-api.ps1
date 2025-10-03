# Test Gemini API with Correct Model Name
$apiKey = "AIzaSyALbmcSq_v5z9YBvaw_gJPiHLFjnOuCl-U"
$url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"

# Correct JSON structure
$body = @{
    contents = @(
        @{
            parts = @(
                @{
                    text = "Hello, test message. Please respond with a short greeting."
                }
            )
        }
    )
    generationConfig = @{
        maxOutputTokens = 100
        temperature = 0.7
    }
} | ConvertTo-Json -Depth 4

Write-Host "Testing API with correct model name..."
Write-Host "URL: $url"
Write-Host ""

try {
    $response = Invoke-RestMethod -Uri $url -Method POST -ContentType "application/json" -Body $body
    Write-Host "SUCCESS: API call worked!"
    Write-Host "Response:"
    $response | ConvertTo-Json -Depth 3
    
    # Extract the actual text response
    if ($response.candidates -and $response.candidates[0].content.parts) {
        $aiResponse = $response.candidates[0].content.parts[0].text
        Write-Host ""
        Write-Host "AI Response Text: $aiResponse"
    }
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
