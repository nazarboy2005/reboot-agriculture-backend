# Gemini API Setup Guide

## Issue: AI Chat Not Responding - Only Hardcoded Templates

The chat functionality is currently falling back to hardcoded template responses because the Gemini API is not properly configured or the API key is invalid.

## Solution Steps

### 1. Get a Valid Gemini API Key

1. Go to [Google AI Studio](https://aistudio.google.com/)
2. Sign in with your Google account
3. Click "Get API Key" 
4. Create a new API key
5. Copy the API key

### 2. Set the API Key

#### Option A: Environment Variable (Recommended)
```bash
# Windows
set GEMINI_API_KEY=your-actual-api-key-here

# Linux/Mac
export GEMINI_API_KEY=your-actual-api-key-here
```

#### Option B: Update application.properties
```properties
app.gemini.api.key=your-actual-api-key-here
```

### 3. Test the API Key

Run this command to test if your API key works:

```bash
curl -X POST "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=YOUR_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{"contents":[{"parts":[{"text":"Hello, test message"}]}]}'
```

### 4. Current Configuration

The application is configured to:
- Use `gemini-1.5-flash` model
- Fallback to enhanced mock responses if API fails
- Provide detailed logging for debugging

### 5. Enhanced Mock Responses

Even without a valid API key, the system now provides intelligent, context-aware responses based on:
- User question analysis
- Agricultural best practices
- Professional farming advice
- Specific recommendations for different topics

### 6. Troubleshooting

If you're still getting hardcoded responses:

1. Check the logs for API key status
2. Verify the API key is valid
3. Ensure the environment variable is set correctly
4. Check network connectivity to Google's API

### 7. Fallback Behavior

The system now gracefully handles API failures by:
- Detecting invalid API keys
- Providing enhanced mock responses
- Maintaining professional agricultural advice
- Logging detailed error information

## Testing

After setting up the API key, test the chat functionality:

1. Start the application
2. Send a chat message
3. Check the logs for API call status
4. Verify you receive AI-generated responses instead of templates

## Current Status

✅ **Fixed**: Enhanced error handling
✅ **Fixed**: Better fallback responses  
✅ **Fixed**: Improved API key validation
✅ **Fixed**: Professional agricultural advice templates
⏳ **Pending**: Valid Gemini API key setup
