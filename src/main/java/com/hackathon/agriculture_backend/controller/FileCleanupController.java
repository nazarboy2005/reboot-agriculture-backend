package com.hackathon.agriculture_backend.controller;

import com.hackathon.agriculture_backend.service.FileCleanupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin/cleanup")
@RequiredArgsConstructor
@Slf4j
public class FileCleanupController {

    private final FileCleanupService fileCleanupService;

    /**
     * Get cleanup statistics
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getCleanupStats() {
        try {
            FileCleanupService.CleanupStats stats = fileCleanupService.getCleanupStats();
            
            Map<String, Object> response = new HashMap<>();
            response.put("currentStorageMB", stats.currentStorageMB);
            response.put("maxStorageMB", stats.maxStorageMB);
            response.put("usagePercentage", Math.round(stats.usagePercentage * 100.0) / 100.0);
            response.put("cleanupEnabled", stats.cleanupEnabled);
            response.put("status", stats.usagePercentage > 90 ? "CRITICAL" : 
                        stats.usagePercentage > 70 ? "WARNING" : "OK");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting cleanup stats", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Trigger manual cleanup
     */
    @PostMapping("/trigger")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> triggerCleanup() {
        try {
            log.info("Manual cleanup triggered by admin");
            fileCleanupService.triggerManualCleanup();
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Cleanup triggered successfully");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error triggering manual cleanup", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get cleanup health status
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getCleanupHealth() {
        try {
            FileCleanupService.CleanupStats stats = fileCleanupService.getCleanupStats();
            
            Map<String, Object> health = new HashMap<>();
            health.put("status", stats.usagePercentage > 90 ? "CRITICAL" : 
                      stats.usagePercentage > 70 ? "WARNING" : "UP");
            health.put("storageUsage", stats.usagePercentage);
            health.put("cleanupEnabled", stats.cleanupEnabled);
            health.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(health);
        } catch (Exception e) {
            log.error("Error getting cleanup health", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}

