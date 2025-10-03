# Test script for chat functionality with correct API path
Write-Host "Testing chat functionality with correct API path..."

$baseUrl = "https://agriculture-backend-1077945709935.europe-west1.run.app/api"

# Test the chat endpoint
$body = "farmerId=9&message=My crops look unhealthy, what should I do?&messageType=GENERAL"
try {
    Write-Host "Testing chat endpoint..."
    $response = Invoke-WebRequest -Uri "$baseUrl/v1/chat/send" -Method POST -Body $body -ContentType "application/x-www-form-urlencoded"
    Write-Host "Response Status: $($response.StatusCode)"
    Write-Host "Response Content: $($response.Content)"
} catch {
    Write-Host "Chat Test Error: $($_.Exception.Message)"
    if ($_.Exception.Response) {
        Write-Host "Response Status: $($_.Exception.Response.StatusCode)"
        try {
            $errorStream = $_.Exception.Response.GetResponseStream()
            $reader = New-Object System.IO.StreamReader($errorStream)
            $errorContent = $reader.ReadToEnd()
            Write-Host "Error Content: $errorContent"
        } catch {
            Write-Host "Could not read error content"
        }
    }
}

# Test the Gemini endpoint
try {
    Write-Host "Testing Gemini endpoint..."
    $geminiResponse = Invoke-WebRequest -Uri "$baseUrl/v1/chat/test-gemini" -Method GET
    Write-Host "Gemini Test Status: $($geminiResponse.StatusCode)"
    Write-Host "Gemini Test Content: $($geminiResponse.Content)"
} catch {
    Write-Host "Gemini Test Error: $($_.Exception.Message)"
    if ($_.Exception.Response) {
        Write-Host "Response Status: $($_.Exception.Response.StatusCode)"
    }
}

# Test health endpoint
try {
    Write-Host "Testing health endpoint..."
    $healthResponse = Invoke-WebRequest -Uri "$baseUrl/v1/chat/health" -Method GET
    Write-Host "Health Status: $($healthResponse.StatusCode)"
    Write-Host "Health Content: $($healthResponse.Content)"
} catch {
    Write-Host "Health Test Error: $($_.Exception.Message)"
}
