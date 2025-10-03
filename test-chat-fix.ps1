# Test script for chat functionality
Write-Host "Testing chat functionality..."

# Test the chat endpoint
$body = "farmerId=9&message=My crops look unhealthy, what should I do?&messageType=GENERAL"
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/v1/chat/send" -Method POST -Body $body -ContentType "application/x-www-form-urlencoded"
    Write-Host "Response Status: $($response.StatusCode)"
    Write-Host "Response Content: $($response.Content)"
} catch {
    Write-Host "Error: $($_.Exception.Message)"
}

# Test the Gemini endpoint
try {
    $geminiResponse = Invoke-WebRequest -Uri "http://localhost:8080/v1/chat/test-gemini" -Method GET
    Write-Host "Gemini Test Status: $($geminiResponse.StatusCode)"
    Write-Host "Gemini Test Content: $($geminiResponse.Content)"
} catch {
    Write-Host "Gemini Test Error: $($_.Exception.Message)"
}
