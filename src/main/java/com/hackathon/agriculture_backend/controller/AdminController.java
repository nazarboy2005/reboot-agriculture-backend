package com.hackathon.agriculture_backend.controller;

import com.hackathon.agriculture_backend.dto.AdminMetricsDto;
import com.hackathon.agriculture_backend.dto.ApiResponse;
import com.hackathon.agriculture_backend.dto.WaterSavingsDto;
import com.hackathon.agriculture_backend.service.FarmerService;
import com.hackathon.agriculture_backend.repository.IrrigationRecommendationRepository;
import com.hackathon.agriculture_backend.repository.AlertLogRepository;
import com.hackathon.agriculture_backend.repository.FarmerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.hackathon.agriculture_backend.model.Farmer;
import com.hackathon.agriculture_backend.model.IrrigationRecommendation;
import com.hackathon.agriculture_backend.repository.FarmerRepository;


import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {
    
    private final FarmerService farmerService;
    private final IrrigationRecommendationRepository recommendationRepository;
    private final AlertLogRepository alertLogRepository;
    private final FarmerRepository farmerRepository;
    
    @GetMapping("/metrics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdminMetricsDto>> getAdminMetrics() {
        log.info("Fetching admin metrics");
        
        try {
            LocalDate today = LocalDate.now();
            LocalDate last30Days = today.minusDays(30);

            AdminMetricsDto metrics = new AdminMetricsDto();
            metrics.setTotalFarmers(farmerRepository.count());
            metrics.setActiveFarmers(recommendationRepository.countActiveFarmersByDateRange(last30Days, today));
            metrics.setFarmersWithSmsOptIn(farmerRepository.countBySmsOptInTrue());

            Double totalWaterSaved = recommendationRepository.calculateTotalWaterSavedByDateRange(last30Days, today);
            metrics.setTotalWaterSavedLiters(totalWaterSaved != null ? totalWaterSaved : 0.0);

            metrics.setTotalRecommendations(recommendationRepository.count());
            metrics.setSuccessfulAlerts(alertLogRepository.countByStatusAndDateRange("SENT", last30Days.atStartOfDay().toInstant(ZoneOffset.UTC), today.atTime(LocalTime.MAX).toInstant(ZoneOffset.UTC)));
            metrics.setFailedAlerts(alertLogRepository.countByStatusAndDateRange("FAILED", last30Days.atStartOfDay().toInstant(ZoneOffset.UTC), today.atTime(LocalTime.MAX).toInstant(ZoneOffset.UTC)));

            List<AdminMetricsDto.CropDistribution> cropDistribution = farmerRepository.findCropDistribution().stream()
                .map(obj -> new AdminMetricsDto.CropDistribution((String) obj[0], (Long) obj[1], 0.0)) // Percentage calculation needed
                .collect(Collectors.toList());
            metrics.setCropDistribution(cropDistribution);

            List<AdminMetricsDto.RecommendationDistribution> recommendationDistribution = recommendationRepository.findRecommendationDistributionByDateRange(last30Days, today).stream()
                .map(obj -> new AdminMetricsDto.RecommendationDistribution((String) obj[0], (Long) obj[1], 0.0)) // Percentage calculation needed
                .collect(Collectors.toList());
            metrics.setRecommendationDistribution(recommendationDistribution);

            List<AdminMetricsDto.AlertTypeDistribution> alertTypeDistribution = alertLogRepository.findAlertTypeDistributionByDateRange(last30Days.atStartOfDay().toInstant(ZoneOffset.UTC), today.atTime(LocalTime.MAX).toInstant(ZoneOffset.UTC)).stream()
                .map(obj -> new AdminMetricsDto.AlertTypeDistribution((String) obj[0], (Long) obj[1], 0.0)) // Percentage calculation needed
                .collect(Collectors.toList());
            metrics.setAlertTypeDistribution(alertTypeDistribution);

            Map<String, Object> dailyMetrics = Map.of(
                "todayRecommendations", recommendationRepository.findByDate(today).size(),
                "todayWaterSaved", recommendationRepository.calculateTotalWaterSavedByDateRange(today, today),
                "todayAlerts", alertLogRepository.findByCreatedAtBetween(today.atStartOfDay().toInstant(ZoneOffset.UTC), today.atTime(LocalTime.MAX).toInstant(ZoneOffset.UTC)).size(),
                "activeUsers", recommendationRepository.countActiveFarmersByDateRange(today, today)
            );
            metrics.setDailyMetrics(dailyMetrics);

            return ResponseEntity.ok(ApiResponse.success("Admin metrics retrieved successfully", metrics));

        } catch (Exception e) {
            log.error("Error fetching admin metrics", e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to fetch admin metrics: " + e.getMessage()));
        }
    }
    
    @GetMapping("/farmers/{farmerId}/water-savings")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<WaterSavingsDto>> getWaterSavings(
            @PathVariable Long farmerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        log.info("Fetching water savings for farmer ID: {} from {} to {}", farmerId, from, to);

        try {
            Farmer farmer = farmerRepository.findById(farmerId)
                    .orElseThrow(() -> new IllegalArgumentException("Farmer not found with ID: " + farmerId));

            WaterSavingsDto waterSavings = new WaterSavingsDto();
            waterSavings.setFarmerId(farmerId);
            waterSavings.setFarmerName(farmer.getName());
            waterSavings.setFromDate(from);
            waterSavings.setToDate(to);

            Double totalWaterSaved = recommendationRepository.calculateWaterSavedByFarmerAndDateRange(farmerId, from, to);
            waterSavings.setTotalWaterSavedLiters(totalWaterSaved != null ? totalWaterSaved : 0.0);

            List<IrrigationRecommendation> recommendations = recommendationRepository.findByFarmerIdAndDateBetween(farmerId, from, to);
            List<WaterSavingsDto.DailyWaterSavings> dailySavings = recommendations.stream()
                .map(rec -> new WaterSavingsDto.DailyWaterSavings(
                    rec.getDate(),
                    rec.getCropType(),
                    rec.getRecommendation(),
                    rec.getWaterSavedLiters(),
                    rec.getExplanation()
                ))
                .collect(Collectors.toList());
            waterSavings.setDailySavings(dailySavings);

            return ResponseEntity.ok(ApiResponse.success("Water savings retrieved successfully", waterSavings));

        } catch (Exception e) {
            log.error("Error fetching water savings", e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to fetch water savings: " + e.getMessage()));
        }
    }
    
    @GetMapping("/data/export")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> exportData(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "json") String format) {
        
        log.info("Exporting data from {} to {} in {} format", from, to, format);
        
        try {
            // Mock data export response
            String exportData = String.format(
                "Data export for period %s to %s in %s format\n" +
                "Export includes:\n" +
                "- Farmer data\n" +
                "- Irrigation recommendations\n" +
                "- Alert logs\n" +
                "- Water savings data\n" +
                "Export file: agriculture_data_%s_to_%s.%s",
                from, to, format, from, to, format
            );
            
            return ResponseEntity.ok(ApiResponse.success("Data export completed successfully", exportData));
            
        } catch (Exception e) {
            log.error("Error exporting data", e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to export data: " + e.getMessage()));
        }
    }
    
    @GetMapping("/stats/farmers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Long>> getTotalFarmers() {
        log.info("Fetching total farmers count");
        
        try {
            Long count = farmerService.getTotalFarmersCount();
            return ResponseEntity.ok(ApiResponse.success(count));
            
        } catch (Exception e) {
            log.error("Error fetching total farmers count", e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to fetch farmers count: " + e.getMessage()));
        }
    }
    
    @GetMapping("/stats/sms-opt-in")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Long>> getFarmersWithSmsOptIn() {
        log.info("Fetching farmers with SMS opt-in count");
        
        try {
            Long count = farmerService.getFarmersWithSmsOptInCount();
            return ResponseEntity.ok(ApiResponse.success(count));
            
        } catch (Exception e) {
            log.error("Error fetching SMS opt-in count", e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to fetch SMS opt-in count: " + e.getMessage()));
        }
    }
}
