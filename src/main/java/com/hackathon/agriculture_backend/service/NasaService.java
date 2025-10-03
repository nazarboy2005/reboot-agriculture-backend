package com.hackathon.agriculture_backend.service;

import com.hackathon.agriculture_backend.dto.NasaEtDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NasaService {
    
    private final WebClient.Builder webClientBuilder;
    
    @Value("${app.nasa.api.url}")
    private String apiUrl;
    
    @Value("${app.nasa.api.key}")
    private String apiKey;
    
    public Double getEvapotranspiration(Double latitude, Double longitude, LocalDate date) {
        log.info("Fetching evapotranspiration for coordinates: {}, {} on date: {}", latitude, longitude, date);
        
        try {
            String startDate = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String endDate = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            
            String url = String.format("%s?start=%s&end=%s&latitude=%s&longitude=%s&community=ag&parameters=ET0&format=JSON&user=agriculture-app&api_key=%s",
                    apiUrl, startDate, endDate, latitude, longitude, apiKey);
            
            NasaEtDto nasaData = webClientBuilder.build()
                    .get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(NasaEtDto.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();
            
            if (nasaData != null && 
                nasaData.getProperties() != null && 
                nasaData.getProperties().getParameter() != null &&
                nasaData.getProperties().getParameter().containsKey("ET0")) {
                
                // Extract ET0 values from the properties.parameter.ET0 map
                Map<String, String> et0Data = nasaData.getProperties().getParameter().get("ET0");
                if (et0Data != null && !et0Data.isEmpty()) {
                    // Get the first available ET0 value
                    String et0Value = et0Data.values().iterator().next();
                    if (et0Value != null && !et0Value.isEmpty()) {
                        try {
                            Double et0 = Double.parseDouble(et0Value);
                            log.info("ET0 data fetched successfully: {} mm/day", et0);
                            return et0;
                        } catch (NumberFormatException e) {
                            log.warn("Failed to parse ET0 value: {}", et0Value);
                        }
                    }
                }
            }
            
            log.warn("No ET0 data available for the specified date and location");
            return 0.0;
            
        } catch (WebClientResponseException e) {
            int statusCode = e.getStatusCode().value();
            String errorMessage;
            
            switch (statusCode) {
                case 422:
                    errorMessage = "Validation Error: Invalid request parameters for NASA POWER API";
                    break;
                case 429:
                    errorMessage = "Too Many Requests: NASA POWER API rate limit exceeded. Please try again later.";
                    break;
                case 500:
                    errorMessage = "Internal Server Error: NASA POWER API is experiencing issues";
                    break;
                case 503:
                    errorMessage = "Service Unreachable: NASA POWER API backend service is unavailable";
                    break;
                default:
                    errorMessage = "NASA POWER API error: " + e.getMessage();
            }
            
            log.error("NASA POWER API error ({}): {}", statusCode, errorMessage);
            throw new RuntimeException(errorMessage);
        } catch (Exception e) {
            log.error("Unexpected error fetching NASA ET0 data", e);
            throw new RuntimeException("Failed to fetch NASA ET0 data: " + e.getMessage());
        }
    }
    
    public Double getEvapotranspirationForDateRange(Double latitude, Double longitude, LocalDate startDate, LocalDate endDate) {
        log.info("Fetching evapotranspiration for coordinates: {}, {} from {} to {}", 
                latitude, longitude, startDate, endDate);
        
        try {
            String startDateStr = startDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String endDateStr = endDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            
            String url = String.format("%s?start=%s&end=%s&latitude=%s&longitude=%s&community=ag&parameters=ET0&format=JSON&user=agriculture-app&api_key=%s",
                    apiUrl, startDateStr, endDateStr, latitude, longitude, apiKey);
            
            NasaEtDto nasaData = webClientBuilder.build()
                    .get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(NasaEtDto.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();
            
            if (nasaData != null && 
                nasaData.getProperties() != null && 
                nasaData.getProperties().getParameter() != null &&
                nasaData.getProperties().getParameter().containsKey("ET0")) {
                
                // Extract ET0 values from the properties.parameter.ET0 map
                Map<String, String> et0Data = nasaData.getProperties().getParameter().get("ET0");
                if (et0Data != null && !et0Data.isEmpty()) {
                    // Calculate average ET0 for the date range
                    double sum = 0.0;
                    int count = 0;
                    for (String et0Value : et0Data.values()) {
                        if (et0Value != null && !et0Value.isEmpty()) {
                            try {
                                sum += Double.parseDouble(et0Value);
                                count++;
                            } catch (NumberFormatException e) {
                                log.warn("Failed to parse ET0 value: {}", et0Value);
                            }
                        }
                    }
                    
                    if (count > 0) {
                        Double averageEt0 = sum / count;
                        log.info("Average ET0 data fetched successfully: {} mm/day for {} days", 
                                averageEt0, count);
                        return averageEt0;
                    }
                }
            }
            
            log.warn("No ET0 data available for the specified date range and location");
            return 0.0;
            
        } catch (WebClientResponseException e) {
            int statusCode = e.getStatusCode().value();
            String errorMessage;
            
            switch (statusCode) {
                case 422:
                    errorMessage = "Validation Error: Invalid request parameters for NASA POWER API";
                    break;
                case 429:
                    errorMessage = "Too Many Requests: NASA POWER API rate limit exceeded. Please try again later.";
                    break;
                case 500:
                    errorMessage = "Internal Server Error: NASA POWER API is experiencing issues";
                    break;
                case 503:
                    errorMessage = "Service Unreachable: NASA POWER API backend service is unavailable";
                    break;
                default:
                    errorMessage = "NASA POWER API error: " + e.getMessage();
            }
            
            log.error("NASA POWER API error ({}): {}", statusCode, errorMessage);
            throw new RuntimeException(errorMessage);
        } catch (Exception e) {
            log.error("Unexpected error fetching NASA ET0 data for date range", e);
            throw new RuntimeException("Failed to fetch NASA ET0 data for date range: " + e.getMessage());
        }
    }
    
    public boolean isHighEvapotranspiration(Double et0, Double threshold) {
        return et0 != null && threshold != null && et0 > threshold;
    }
}



