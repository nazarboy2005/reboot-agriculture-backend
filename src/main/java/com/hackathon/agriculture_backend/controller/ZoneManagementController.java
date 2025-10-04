package com.hackathon.agriculture_backend.controller;

import com.hackathon.agriculture_backend.dto.ApiResponse;
import com.hackathon.agriculture_backend.dto.FarmerZoneDto;
import com.hackathon.agriculture_backend.service.FarmerZoneService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/zones")
@RequiredArgsConstructor
@Slf4j
public class ZoneManagementController {
    
    private final FarmerZoneService farmerZoneService;
    
    @GetMapping("/farmer/{farmerId}")
    public ResponseEntity<ApiResponse<List<FarmerZoneDto>>> getFarmerZones(@PathVariable Long farmerId) {
        log.info("Fetching zones for farmer ID: {}", farmerId);
        
        try {
            List<FarmerZoneDto> zones = farmerZoneService.getZonesByFarmerId(farmerId);
            return ResponseEntity.ok(ApiResponse.success("Zones retrieved successfully", zones));
        } catch (Exception e) {
            log.error("Error fetching farmer zones: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to fetch zones: " + e.getMessage()));
        }
    }
    
    @PostMapping("/farmer/{farmerId}")
    public ResponseEntity<ApiResponse<FarmerZoneDto>> createZone(
            @PathVariable Long farmerId,
            @RequestBody FarmerZoneDto zoneDto) {
        
        log.info("Creating zone for farmer ID: {}", farmerId);
        
        try {
            FarmerZoneDto createdZone = farmerZoneService.createZone(farmerId, zoneDto);
            return ResponseEntity.ok(ApiResponse.success("Zone created successfully", createdZone));
        } catch (Exception e) {
            log.error("Error creating zone: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to create zone: " + e.getMessage()));
        }
    }
    
    @GetMapping("/{zoneId}")
    public ResponseEntity<ApiResponse<FarmerZoneDto>> getZone(@PathVariable Long zoneId) {
        log.info("Fetching zone with ID: {}", zoneId);
        
        try {
            FarmerZoneDto zone = farmerZoneService.getZoneById(zoneId);
            return ResponseEntity.ok(ApiResponse.success("Zone retrieved successfully", zone));
        } catch (Exception e) {
            log.error("Error fetching zone: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to fetch zone: " + e.getMessage()));
        }
    }
    
    @PutMapping("/{zoneId}")
    public ResponseEntity<ApiResponse<FarmerZoneDto>> updateZone(
            @PathVariable Long zoneId,
            @RequestBody FarmerZoneDto zoneDto) {
        
        log.info("Updating zone with ID: {}", zoneId);
        
        try {
            FarmerZoneDto updatedZone = farmerZoneService.updateZone(zoneId, zoneDto);
            return ResponseEntity.ok(ApiResponse.success("Zone updated successfully", updatedZone));
        } catch (Exception e) {
            log.error("Error updating zone: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to update zone: " + e.getMessage()));
        }
    }
    
    @DeleteMapping("/{zoneId}")
    public ResponseEntity<ApiResponse<String>> deleteZone(@PathVariable Long zoneId) {
        log.info("Deleting zone with ID: {}", zoneId);
        
        try {
            farmerZoneService.deleteZone(zoneId);
            return ResponseEntity.ok(ApiResponse.success("Zone deleted successfully", "Zone deleted"));
        } catch (Exception e) {
            log.error("Error deleting zone: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to delete zone: " + e.getMessage()));
        }
    }
    
    @GetMapping("/{zoneId}/irrigation-status")
    public ResponseEntity<ApiResponse<Object>> getZoneIrrigationStatus(@PathVariable Long zoneId) {
        log.info("Fetching irrigation status for zone ID: {}", zoneId);
        
        try {
            // Create mock irrigation status
            Object status = new Object() {
                public final Long zoneId = zoneId;
                public final String status = "ACTIVE";
                public final Double moistureLevel = 65.5;
                public final String lastIrrigation = "2024-01-15T08:30:00Z";
                public final String nextScheduled = "2024-01-16T08:30:00Z";
                public final Double waterUsed = 150.5;
                public final String recommendations = "Zone is well irrigated";
            };
            
            return ResponseEntity.ok(ApiResponse.success("Irrigation status retrieved successfully", status));
        } catch (Exception e) {
            log.error("Error fetching irrigation status: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to fetch irrigation status: " + e.getMessage()));
        }
    }
    
    @PostMapping("/{zoneId}/irrigate")
    public ResponseEntity<ApiResponse<String>> triggerIrrigation(@PathVariable Long zoneId) {
        log.info("Triggering irrigation for zone ID: {}", zoneId);
        
        try {
            // Mock irrigation trigger
            String message = "Irrigation triggered successfully for zone " + zoneId;
            return ResponseEntity.ok(ApiResponse.success("Irrigation triggered successfully", message));
        } catch (Exception e) {
            log.error("Error triggering irrigation: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to trigger irrigation: " + e.getMessage()));
        }
    }
    
    @GetMapping("/{zoneId}/sensors")
    public ResponseEntity<ApiResponse<List<Object>>> getZoneSensors(@PathVariable Long zoneId) {
        log.info("Fetching sensors for zone ID: {}", zoneId);
        
        try {
            List<Object> sensors = List.of(
                new Object() {
                    public final Long id = 1L;
                    public final String type = "SOIL_MOISTURE";
                    public final Double value = 65.5;
                    public final String unit = "%";
                    public final String lastReading = "2024-01-15T10:30:00Z";
                    public final String status = "ACTIVE";
                },
                new Object() {
                    public final Long id = 2L;
                    public final String type = "TEMPERATURE";
                    public final Double value = 25.8;
                    public final String unit = "°C";
                    public final String lastReading = "2024-01-15T10:30:00Z";
                    public final String status = "ACTIVE";
                }
            );
            
            return ResponseEntity.ok(ApiResponse.success("Sensors retrieved successfully", sensors));
        } catch (Exception e) {
            log.error("Error fetching zone sensors: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to fetch sensors: " + e.getMessage()));
        }
    }
}
