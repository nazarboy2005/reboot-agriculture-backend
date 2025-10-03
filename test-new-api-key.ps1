# Test script for new Gemini API key
Write-Host "Testing new Gemini API key..."

$baseUrl = "https://agriculture-backend-1077945709935.europe-west1.run.app/api"

# Test the Gemini endpoint first
try {
    Write-Host "Testing Gemini endpoint with new API key..."
    $geminiResponse = Invoke-WebRequest -Uri "$baseUrl/v1/chat/test-gemini" -Method GET
    Write-Host "Gemini Test Status: $($geminiResponse.StatusCode)"
    Write-Host "Gemini Test Content: $($geminiResponse.Content)"
} catch {
    Write-Host "Gemini Test Error: $($_.Exception.Message)"
    if ($_.Exception.Response) {
        Write-Host "Response Status: $($_.Exception.Response.StatusCode)"
    }
}

# Test the chat endpoint with a specific question
$body = "farmerId=9&message=My crops look unhealthy, what should I do?&messageType=GENERAL"
try {
    Write-Host "Testing chat endpoint with new API key..."
    $response = Invoke-WebRequest -Uri "$baseUrl/v1/chat/send" -Method POST -Body $body -ContentType "application/x-www-form-urlencoded"
    Write-Host "Response Status: $($response.StatusCode)"
    Write-Host "Response Content: $($response.Content)"
} catch {
    Write-Host "Chat Test Error: $($_.Exception.Message)"
    if ($_.Exception.Response) {
        Write-Host "Response Status: $($_.Exception.Response.StatusCode)"
    }
}
