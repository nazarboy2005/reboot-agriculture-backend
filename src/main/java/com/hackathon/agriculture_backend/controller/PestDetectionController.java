package com.hackathon.agriculture_backend.controller;

import com.hackathon.agriculture_backend.dto.ApiResponse;
import com.hackathon.agriculture_backend.service.PestDetectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/insect")
@RequiredArgsConstructor
@Slf4j
public class PestDetectionController {

    private final PestDetectionService pestDetectionService;

    @PostMapping("/detect")
    public CompletableFuture<ResponseEntity<Object>> detectPest(@RequestParam("image") MultipartFile image) {
        log.info("Received pest detection request for image: {}", image.getOriginalFilename());

        try {
            if (image.isEmpty()) {
                return CompletableFuture.completedFuture(ResponseEntity.badRequest().body(Map.of("error", "No image provided")));
            }

            return pestDetectionService.detectPest(image)
                .map(apiResponse -> {
                    Map<String, Object> response = new HashMap<>();
                    if (apiResponse.getResult() != null && apiResponse.getResult().getIsInsect() != null && apiResponse.getResult().getIsInsect().isBinary()) {
                        response.put("pestDetected", true);
                        if (apiResponse.getResult().getClassification() != null && !apiResponse.getResult().getClassification().getSuggestions().isEmpty()) {
                            var topSuggestion = apiResponse.getResult().getClassification().getSuggestions().get(0);
                            response.put("pestName", topSuggestion.getName());
                            response.put("confidence", topSuggestion.getProbability());
                            // Mocking other fields for now as they are not in the insect.id response
                            response.put("severity", "Unknown"); // Or maybe based on probability
                            response.put("recommendations", List.of("Consult with an expert for treatment."));
                            response.put("treatment", "Further analysis required.");
                            response.put("prevention", "Monitor crops regularly.");
                        } else {
                            response.put("pestName", "Unknown Insect");
                            response.put("confidence", apiResponse.getResult().getIsInsect().getProbability());
                        }
                    } else {
                        response.put("pestDetected", false);
                        if (apiResponse.getResult() != null && apiResponse.getResult().getIsInsect() != null) {
                            response.put("pestName", "Not an insect");
                            response.put("confidence", 1.0 - apiResponse.getResult().getIsInsect().getProbability());
                        } else {
                            response.put("pestName", "Detection failed");
                            response.put("confidence", 0.0);
                        }
                    }
                    return ResponseEntity.ok(response);
                })
                .toFuture()
                .exceptionally(ex -> {
                     log.error("Error processing pest detection: {}", ex.getMessage());
                     return ResponseEntity.status(500).body(Map.of("error", "Pest detection failed: " + ex.getMessage()));
                });

        } catch (Exception e) {
            log.error("Error processing pest detection request: {}", e.getMessage());
            return CompletableFuture.completedFuture(ResponseEntity.status(500).body(Map.of("error", "Pest detection failed: " + e.getMessage())));
        }
    }

    @GetMapping("/history")
    public ResponseEntity<Object> getDetectionHistory() {
        log.info("Fetching pest detection history");

        try {
            // Create mock history data
            List<Map<String, Object>> history = List.of(
                Map.of(
                    "id", 1,
                    "imageUrl", "/images/pest1.jpg",
                    "pestName", "Aphids",
                    "confidence", 0.85,
                    "detectedAt", "2024-01-15T10:30:00Z",
                    "severity", "Medium"
                ),
                Map.of(
                    "id", 2,
                    "imageUrl", "/images/pest2.jpg",
                    "pestName", "Whiteflies",
                    "confidence", 0.92,
                    "detectedAt", "2024-01-14T15:45:00Z",
                    "severity", "High"
                )
            );

            return ResponseEntity.ok(history);

        } catch (Exception e) {
            log.error("Error fetching detection history: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Failed to fetch history: " + e.getMessage()));
        }
    }

    @DeleteMapping("/history/{id}")
    public ResponseEntity<Object> deleteDetection(@PathVariable Long id) {
        log.info("Deleting pest detection record with ID: {}", id);

        try {
            // Mock deletion
            return ResponseEntity.ok(Map.of("message", "Detection record deleted successfully", "id", id));

        } catch (Exception e) {
            log.error("Error deleting detection record: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Failed to delete record: " + e.getMessage()));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        log.info("Pest detection service health check");
        return ResponseEntity.ok("Pest detection service is running");
    }
}
