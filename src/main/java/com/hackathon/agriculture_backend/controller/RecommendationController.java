package com.hackathon.agriculture_backend.controller;

import com.hackathon.agriculture_backend.dto.ApiResponse;
import com.hackathon.agriculture_backend.dto.RecommendationRequestDto;
import com.hackathon.agriculture_backend.dto.RecommendationResponseDto;
import com.hackathon.agriculture_backend.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/recommendations")
@RequiredArgsConstructor
@Slf4j
public class RecommendationController {
    
    private final RecommendationService recommendationService;
    
    @PostMapping("/ad-hoc")
    public ResponseEntity<ApiResponse<Object>> getAdHocRecommendation(
            @RequestParam Long farmerId,
            @RequestParam(required = false) String date) {
        
        log.info("Generating ad-hoc recommendation for farmer ID: {}, date: {}", farmerId, date);
        
        try {
            // Create mock recommendation response
            Object recommendation = createMockRecommendation(farmerId, date);
            return ResponseEntity.ok(ApiResponse.success("Ad-hoc recommendation generated successfully", recommendation));
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
    public ResponseEntity<ApiResponse<List<Object>>> getFarmerRecommendations(
            @PathVariable Long farmerId,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate) {
        
        log.info("Fetching recommendations for farmer ID: {}, from: {}, to: {}", 
                farmerId, fromDate, toDate);
        
        try {
            List<Object> recommendations = createMockRecommendations(farmerId);
            return ResponseEntity.ok(ApiResponse.success(recommendations));
        } catch (Exception e) {
            log.error("Error fetching farmer recommendations: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to fetch recommendations: " + e.getMessage()));
        }
    }
    
    @GetMapping("/farmers/{farmerId}/latest")
    public ResponseEntity<ApiResponse<Object>> getLatestRecommendation(@PathVariable Long farmerId) {
        
        log.info("Fetching latest recommendation for farmer ID: {}", farmerId);
        
        try {
            Object recommendation = createMockRecommendation(farmerId, null);
            return ResponseEntity.ok(ApiResponse.success(recommendation));
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
        return new Object() {
            public final Long id = 1L;
            public final Long farmerId = farmerId;
            public final String date = date != null ? date : "2024-01-15";
            public final String cropType = "Wheat";
            public final String locationName = "Test Location";
            public final Double tempC = 25.5;
            public final Double humidity = 65.0;
            public final Double rainfallMm = 5.2;
            public final Double evapotranspiration = 3.8;
            public final String recommendation = "MODERATE";
            public final String explanation = "Based on current weather conditions, moderate irrigation is recommended.";
            public final Double waterSavedLiters = 45.5;
            public final String createdAt = "2024-01-15T10:30:00Z";
            public final String updatedAt = "2024-01-15T10:30:00Z";
        };
    }
    
    private List<Object> createMockRecommendations(Long farmerId) {
        return List.of(
            createMockRecommendation(farmerId, "2024-01-15"),
            createMockRecommendation(farmerId, "2024-01-14"),
            createMockRecommendation(farmerId, "2024-01-13")
        );
    }
}

