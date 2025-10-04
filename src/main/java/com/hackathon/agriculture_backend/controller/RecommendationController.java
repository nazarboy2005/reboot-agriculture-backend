package com.hackathon.agriculture_backend.controller;

import com.hackathon.agriculture_backend.dto.ApiResponse;
import com.hackathon.agriculture_backend.dto.RecommendationRequestDto;
import com.hackathon.agriculture_backend.dto.RecommendationResponseDto;
import com.hackathon.agriculture_backend.dto.RecommendationDto;
import com.hackathon.agriculture_backend.dto.FarmerDto;
import com.hackathon.agriculture_backend.dto.WeatherDto;
import com.hackathon.agriculture_backend.dto.RecommendationResult;
import com.hackathon.agriculture_backend.model.Farmer;
import com.hackathon.agriculture_backend.model.IrrigationRecommendation;
import com.hackathon.agriculture_backend.service.FarmerService;
import com.hackathon.agriculture_backend.service.RecommendationService;
import com.hackathon.agriculture_backend.service.WeatherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/recommendations")
@RequiredArgsConstructor
@Slf4j
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final FarmerService farmerService;
    private final WeatherService weatherService;

    @PostMapping("/ad-hoc")
    public ResponseEntity<ApiResponse<RecommendationDto>> getAdHocRecommendation(
            @RequestParam Long farmerId,
            @RequestParam(required = false) String date) {

        log.info("Generating ad-hoc recommendation for farmer ID: {}, date: {}", farmerId, date);

        try {
            Optional<FarmerDto> farmerDtoOpt = farmerService.getFarmerById(farmerId);
            if (farmerDtoOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Farmer not found with ID: " + farmerId));
            }
            FarmerDto farmerDto = farmerDtoOpt.get();

            Farmer farmer = new Farmer();
            farmer.setId(farmerDto.getId());
            farmer.setName(farmerDto.getName());
            farmer.setLocationName(farmerDto.getLocationName());
            farmer.setPreferredCrop(farmerDto.getPreferredCrop());
            farmer.setLatitude(farmerDto.getLatitude());
            farmer.setLongitude(farmerDto.getLongitude());

            WeatherDto weather = weatherService.getIrrigationWeatherData(farmer.getLatitude(), farmer.getLongitude());

            RecommendationResult result = recommendationService.calculateRecommendation(farmer, weather.getTempC(),
                    weather.getHumidity(), weather.getRainfallMm(), weather.getForecastRainfallMm(), weather.getEtc());

            LocalDate recommendationDate = (date != null && !date.isEmpty()) ? LocalDate.parse(date) : LocalDate.now();

            IrrigationRecommendation savedRecommendation = recommendationService.saveRecommendation(farmer,
                    recommendationDate, farmer.getPreferredCrop(), farmer.getLocationName(), weather.getTempC(),
                    weather.getHumidity(), weather.getRainfallMm(), weather.getEtc(), result);

            return ResponseEntity.ok(ApiResponse.success("Ad-hoc recommendation generated successfully", RecommendationDto.fromEntity(savedRecommendation)));
        } catch (Exception e) {
            log.error("Error generating ad-hoc recommendation: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to generate ad-hoc recommendation: " + e.getMessage()));
        }
    }

    @PostMapping("/schedule")
    public ResponseEntity<ApiResponse<String>> scheduleRecommendation(
            @RequestParam Long farmerId,
            @RequestParam(required = false) String date) {
        
        log.info("Scheduling recommendation for farmer ID: {}, date: {}", farmerId, date);
        
        try {
            String scheduleMessage = "Recommendation scheduled successfully for farmer " + farmerId;
            return ResponseEntity.ok(ApiResponse.success("Recommendation scheduled successfully", scheduleMessage));
        } catch (Exception e) {
            log.error("Error scheduling recommendation: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to schedule recommendation: " + e.getMessage()));
        }
    }
    
    @GetMapping("/farmers/{farmerId}")
    public ResponseEntity<ApiResponse<List<RecommendationDto>>> getFarmerRecommendations(
            @PathVariable Long farmerId,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate) {
        
        log.info("Fetching recommendations for farmer ID: {}, from: {}, to: {}", 
                farmerId, fromDate, toDate);
        
        try {
            LocalDate startDate = (fromDate != null && !fromDate.isEmpty()) ? LocalDate.parse(fromDate) : null;
            LocalDate endDate = (toDate != null && !toDate.isEmpty()) ? LocalDate.parse(toDate) : null;
            
            List<RecommendationDto> recommendations = recommendationService.getFarmerRecommendationsByDateRange(farmerId, startDate, endDate);
            return ResponseEntity.ok(ApiResponse.success(recommendations));
        } catch (Exception e) {
            log.error("Error fetching farmer recommendations: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to fetch recommendations: " + e.getMessage()));
        }
    }
    
    @GetMapping("/farmers/{farmerId}/latest")
    public ResponseEntity<ApiResponse<RecommendationDto>> getLatestRecommendation(@PathVariable Long farmerId) {
        
        log.info("Fetching latest recommendation for farmer ID: {}", farmerId);
        
        try {
            Optional<RecommendationDto> recommendation = recommendationService.getLatestRecommendation(farmerId);
            return recommendation.map(value -> ResponseEntity.ok(ApiResponse.success(value)))
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error fetching latest recommendation: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to fetch latest recommendation: " + e.getMessage()));
        }
    }
    
    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<List<RecommendationResponseDto>>> generateRecommendations(
            @RequestBody RecommendationRequestDto request) {
        
        log.info("Generating recommendations for farmer ID: {}, zone: {}, category: {}", 
                request.getFarmerId(), request.getZoneId(), request.getCategory());
        
        try {
            List<RecommendationResponseDto> recommendations = recommendationService.generateAIRecommendations(request);
            return ResponseEntity.ok(ApiResponse.success("Recommendations generated successfully", recommendations));
        } catch (Exception e) {
            log.error("Error generating recommendations: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to generate recommendations: " + e.getMessage()));
        }
    }
    
    @GetMapping("/farmer/{farmerId}")
    public ResponseEntity<ApiResponse<List<RecommendationResponseDto>>> getFarmerRecommendationsOld(
            @PathVariable Long farmerId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String zoneId) {
        
        log.info("Fetching recommendations for farmer ID: {}, category: {}, zone: {}", 
                farmerId, category, zoneId);
        
        try {
            List<RecommendationResponseDto> recommendations = recommendationService.getFarmerRecommendations(
                    farmerId, category, zoneId);
            return ResponseEntity.ok(ApiResponse.success(recommendations));
        } catch (Exception e) {
            log.error("Error fetching farmer recommendations: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to fetch recommendations: " + e.getMessage()));
        }
    }
    
    @GetMapping("/zone/{zoneId}")
    public ResponseEntity<ApiResponse<List<RecommendationResponseDto>>> getZoneRecommendations(
            @PathVariable Long zoneId,
            @RequestParam(required = false) String category) {
        
        log.info("Fetching recommendations for zone ID: {}, category: {}", zoneId, category);
        
        try {
            List<RecommendationResponseDto> recommendations = recommendationService.getZoneRecommendations(
                    zoneId, category);
            return ResponseEntity.ok(ApiResponse.success(recommendations));
        } catch (Exception e) {
            log.error("Error fetching zone recommendations: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to fetch zone recommendations: " + e.getMessage()));
        }
    }
    
    private Object createMockRecommendation(Long farmerId, String date) {
        return Map.ofEntries(
                Map.entry("id", 1L),
                Map.entry("farmerId", farmerId),
                Map.entry("date", date != null ? date : "2024-01-15"),
                Map.entry("cropType", "Wheat"),
                Map.entry("locationName", "Test Location"),
                Map.entry("tempC", 25.5),
                Map.entry("humidity", 65.0),
                Map.entry("rainfallMm", 5.2),
                Map.entry("evapotranspiration", 3.8),
                Map.entry("recommendation", "MODERATE"),
                Map.entry("explanation", "Based on current weather conditions, moderate irrigation is recommended."),
                Map.entry("waterSavedLiters", 45.5),
                Map.entry("createdAt", "2024-01-15T10:30:00Z"),
                Map.entry("updatedAt", "2024-01-15T10:30:00Z")
        );
    }
    
    private List<Object> createMockRecommendations(Long farmerId) {
        return List.of(
            createMockRecommendation(farmerId, "2024-01-15"),
            createMockRecommendation(farmerId, "2024-01-14"),
            createMockRecommendation(farmerId, "2024-01-13")
        );
    }
}

