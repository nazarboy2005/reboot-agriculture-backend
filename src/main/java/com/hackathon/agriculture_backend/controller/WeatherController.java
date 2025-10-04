package com.hackathon.agriculture_backend.controller;

import com.hackathon.agriculture_backend.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/weather")
@RequiredArgsConstructor
@Slf4j
public class WeatherController {
    
    @GetMapping("/current/{latitude}/{longitude}")
    public ResponseEntity<ApiResponse<Object>> getCurrentWeather(
            @PathVariable Double latitude,
            @PathVariable Double longitude) {
        
        log.info("Fetching current weather for location: {}, {}", latitude, longitude);
        
        try {
            Map<String, Object> weather = new HashMap<>();
            weather.put("tempC", 25.5);
            weather.put("humidity", 65.0);
            weather.put("rainfallMm", 5.2);
            weather.put("forecastRainfallMm", 8.5);
            weather.put("heatAlert", false);
            weather.put("weatherDescription", "Partly cloudy");
            
            Map<String, Object> current = new HashMap<>();
            current.put("temp", 25.5);
            current.put("humidity", 65);
            current.put("uvi", 6.2);
            current.put("rain", 0.0);
            current.put("snow", 0.0);
            current.put("weather", List.of(
                Map.of("main", "Clouds", "description", "partly cloudy", "icon", "02d")
            ));
            weather.put("current", current);
            
            return ResponseEntity.ok(ApiResponse.success("Current weather retrieved successfully", weather));
            
        } catch (Exception e) {
            log.error("Error fetching current weather: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to fetch current weather: " + e.getMessage()));
        }
    }
    
    @GetMapping("/forecast/{latitude}/{longitude}")
    public ResponseEntity<ApiResponse<Object>> getWeatherForecast(
            @PathVariable Double latitude,
            @PathVariable Double longitude) {
        
        log.info("Fetching weather forecast for location: {}, {}", latitude, longitude);
        
        try {
            Map<String, Object> forecast = new HashMap<>();
            forecast.put("tempC", 25.5);
            forecast.put("humidity", 65.0);
            forecast.put("rainfallMm", 5.2);
            forecast.put("forecastRainfallMm", 8.5);
            forecast.put("heatAlert", false);
            forecast.put("weatherDescription", "Partly cloudy");
            
            List<Map<String, Object>> daily = List.of(
                Map.of(
                    "dt", 1705334400,
                    "temp", Map.of("day", 25.5, "min", 18.2, "max", 28.1),
                    "humidity", 65,
                    "rain", 0.0,
                    "weather", List.of(Map.of("main", "Clouds", "description", "partly cloudy", "icon", "02d"))
                ),
                Map.of(
                    "dt", 1705420800,
                    "temp", Map.of("day", 27.1, "min", 19.5, "max", 30.2),
                    "humidity", 58,
                    "rain", 2.5,
                    "weather", List.of(Map.of("main", "Rain", "description", "light rain", "icon", "10d"))
                )
            );
            forecast.put("daily", daily);
            
            List<Map<String, Object>> alerts = List.of(
                Map.of(
                    "senderName", "Weather Service",
                    "event", "Heat Warning",
                    "start", 1705334400,
                    "end", 1705420800,
                    "description", "High temperatures expected",
                    "tags", List.of("heat", "temperature")
                )
            );
            forecast.put("alerts", alerts);
            
            return ResponseEntity.ok(ApiResponse.success("Weather forecast retrieved successfully", forecast));
            
        } catch (Exception e) {
            log.error("Error fetching weather forecast: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to fetch weather forecast: " + e.getMessage()));
        }
    }
    
    @GetMapping("/alerts/{latitude}/{longitude}")
    public ResponseEntity<ApiResponse<List<Object>>> getWeatherAlerts(
            @PathVariable Double latitude,
            @PathVariable Double longitude) {
        
        log.info("Fetching weather alerts for location: {}, {}", latitude, longitude);
        
        try {
            List<Object> alerts = List.of(
                Map.of(
                    "id", 1,
                    "type", "HEAT_WARNING",
                    "severity", "Medium",
                    "message", "High temperatures expected in your area",
                    "startTime", "2024-01-15T12:00:00Z",
                    "endTime", "2024-01-15T18:00:00Z",
                    "recommendations", List.of(
                        "Increase irrigation frequency",
                        "Provide shade for sensitive crops",
                        "Monitor soil moisture levels"
                    )
                ),
                Map.of(
                    "id", 2,
                    "type", "RAIN_ALERT",
                    "severity", "Low",
                    "message", "Light rain expected",
                    "startTime", "2024-01-16T08:00:00Z",
                    "endTime", "2024-01-16T14:00:00Z",
                    "recommendations", List.of(
                        "Reduce irrigation",
                        "Check drainage systems",
                        "Protect sensitive crops"
                    )
                )
            );
            
            return ResponseEntity.ok(ApiResponse.success("Weather alerts retrieved successfully", alerts));
            
        } catch (Exception e) {
            log.error("Error fetching weather alerts: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to fetch weather alerts: " + e.getMessage()));
        }
    }
    
    @GetMapping("/heat-alerts/{latitude}/{longitude}")
    public ResponseEntity<ApiResponse<List<Object>>> getHeatAlerts(
            @PathVariable Double latitude,
            @PathVariable Double longitude) {
        
        log.info("Fetching heat alerts for location: {}, {}", latitude, longitude);
        
        try {
            List<Object> heatAlerts = List.of(
                Map.of(
                    "date", "2024-01-15",
                    "time", "14:00",
                    "temperature", 35.5,
                    "heatIndex", 38.2,
                    "riskLevel", "High",
                    "recommendations", "Avoid outdoor activities during peak hours"
                ),
                Map.of(
                    "date", "2024-01-16",
                    "time", "15:00",
                    "temperature", 33.8,
                    "heatIndex", 36.1,
                    "riskLevel", "Medium",
                    "recommendations", "Stay hydrated and take frequent breaks"
                )
            );
            
            return ResponseEntity.ok(ApiResponse.success("Heat alerts retrieved successfully", heatAlerts));
            
        } catch (Exception e) {
            log.error("Error fetching heat alerts: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to fetch heat alerts: " + e.getMessage()));
        }
    }
}
