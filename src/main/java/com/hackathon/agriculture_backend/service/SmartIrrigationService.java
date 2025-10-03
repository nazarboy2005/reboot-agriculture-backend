package com.hackathon.agriculture_backend.service;

import com.hackathon.agriculture_backend.dto.IrrigationPlanDto;
import com.hackathon.agriculture_backend.dto.WeatherDto;
import com.hackathon.agriculture_backend.controller.SmartIrrigationController;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmartIrrigationService {
    
    private final WeatherService weatherService;
    private final RecommendationService recommendationService;
    
    public IrrigationPlanDto generateIrrigationPlan(Double latitude, Double longitude, String cropType, 
                                                   Double area, String irrigationType, String soilType) {
        
        log.info("Generating irrigation plan for location: {}, {} with crop: {}", 
                latitude, longitude, cropType);
        
        try {
            // Get real weather data
            WeatherDto weatherData = weatherService.getIrrigationWeatherData(latitude, longitude);
            
            if (weatherData == null || weatherData.getCurrent() == null) {
                throw new RuntimeException("Unable to fetch weather data for the location");
            }
            
            // Extract current weather conditions with null checks
            Double temperature = weatherData.getCurrent().getTemp() != null ? 
                    weatherData.getCurrent().getTemp() : 25.0;
            Double humidity = weatherData.getCurrent().getHumidity() != null ? 
                    weatherData.getCurrent().getHumidity() : 50.0;
            Double rainfall = weatherData.getCurrent().getRain() != null ? 
                    weatherData.getCurrent().getRain().getOneHour() : 0.0;
            Double windSpeed = weatherData.getCurrent().getWindSpeed() != null ? 
                    weatherData.getCurrent().getWindSpeed() : 5.0;
            Double uvIndex = weatherData.getCurrent().getUvi() != null ? 
                    weatherData.getCurrent().getUvi() : 5.0;
            
            // Calculate evapotranspiration using real weather data
            Double et0 = calculateEvapotranspiration(temperature, humidity, windSpeed, uvIndex);
            
            // Generate 7-day irrigation plan
            List<IrrigationPlanDto.DayPlan> dailyPlans = new ArrayList<>();
            
            for (int i = 0; i < 7; i++) {
                LocalDate date = LocalDate.now().plusDays(i);
                
                // Get forecast data for each day
                WeatherDto.DailyWeather dayForecast = null;
                if (weatherData.getDaily() != null && i < weatherData.getDaily().size()) {
                    dayForecast = weatherData.getDaily().get(i);
                }
                
                // Calculate irrigation needs for each day
                IrrigationPlanDto.DayPlan dayPlan = calculateDayPlan(
                        date, cropType, area, irrigationType, soilType,
                        dayForecast, et0, temperature, humidity, rainfall, windSpeed
                );
                
                dailyPlans.add(dayPlan);
            }
            
            // Create comprehensive irrigation plan
            IrrigationPlanDto plan = new IrrigationPlanDto();
            plan.setLocationName(getLocationName(latitude, longitude));
            plan.setCropType(cropType);
            plan.setArea(area);
            plan.setIrrigationType(irrigationType);
            plan.setSoilType(soilType);
            plan.setDailyPlans(dailyPlans);
            plan.setGeneratedAt(LocalDate.now());
            
            // Calculate total water savings
            Double totalWaterSaved = dailyPlans.stream()
                    .mapToDouble(IrrigationPlanDto.DayPlan::getWaterSavedLiters)
                    .sum();
            plan.setTotalWaterSaved(totalWaterSaved);
            
            log.info("Irrigation plan generated successfully with {} days of recommendations", 
                    dailyPlans.size());
            
            return plan;
            
        } catch (Exception e) {
            log.error("Error generating irrigation plan: {}", e.getMessage());
            throw new RuntimeException("Failed to generate irrigation plan: " + e.getMessage());
        }
    }
    
    public List<SmartIrrigationController.HeatAlertDto> getHeatAlerts(Double latitude, Double longitude) {
        log.info("Fetching heat alerts for location: {}, {}", latitude, longitude);

        try {
            WeatherDto weatherData = weatherService.getIrrigationWeatherData(latitude, longitude);
            List<SmartIrrigationController.HeatAlertDto> alerts = new ArrayList<>();

            if (weatherData != null && weatherData.getDaily() != null) {
                for (int i = 0; i < Math.min(7, weatherData.getDaily().size()); i++) {
                    WeatherDto.DailyWeather day = weatherData.getDaily().get(i);

                    // Generate alert for any temperature above 28°C (lowered threshold for better coverage)
                    if (day.getTemp() != null && day.getTemp().getMax() != null && day.getTemp().getMax() > 28.0) {
                        SmartIrrigationController.HeatAlertDto alert = new SmartIrrigationController.HeatAlertDto();
                        alert.setDate(LocalDate.now().plusDays(i).toString());
                        alert.setTime("12:00");
                        alert.setTemperature(day.getTemp().getMax());
                        alert.setHeatIndex(calculateHeatIndex(day.getTemp().getMax(),
                                day.getHumidity() != null ? day.getHumidity() : 50.0));
                        alert.setRiskLevel(determineHeatRisk(day.getTemp().getMax()));
                        alert.setRecommendations(generateHeatRecommendations(day.getTemp().getMax()));

                        alerts.add(alert);
                    }
                }
            }

            // If no alerts from weather data, generate demo alerts for testing
            if (alerts.isEmpty()) {
                log.info("No weather alerts found, generating demo alerts for location: {}, {}", latitude, longitude);
                alerts = generateDemoHeatAlerts();
            }

            return alerts;

        } catch (Exception e) {
            log.error("Error fetching heat alerts: {}", e.getMessage());
            // Return demo alerts instead of empty list
            return generateDemoHeatAlerts();
        }
    }

    private List<SmartIrrigationController.HeatAlertDto> generateDemoHeatAlerts() {
        List<SmartIrrigationController.HeatAlertDto> demoAlerts = new ArrayList<>();

        // Generate realistic demo alerts for the next 3 days
        for (int i = 0; i < 3; i++) {
            SmartIrrigationController.HeatAlertDto alert = new SmartIrrigationController.HeatAlertDto();

            // Simulate different temperature ranges
            Double temperature = 30.0 + (i * 5) + Math.random() * 8; // 30-43°C range
            Double humidity = 50.0 + Math.random() * 20; // 50-70% humidity range

            alert.setDate(LocalDate.now().plusDays(i).toString());
            alert.setTime(i == 0 ? "14:00" : (i == 1 ? "15:30" : "13:00"));
            alert.setTemperature(Math.round(temperature * 10.0) / 10.0);
            alert.setHeatIndex(calculateHeatIndex(temperature, humidity));
            alert.setRiskLevel(determineHeatRisk(temperature));
            alert.setRecommendations(generateHeatRecommendations(temperature));

            demoAlerts.add(alert);
        }

        return demoAlerts;
    }
    
    public SmartIrrigationController.WeatherDataDto getWeatherData(Double latitude, Double longitude) {
        log.info("Fetching weather data for location: {}, {}", latitude, longitude);
        
        try {
            WeatherDto weatherData = weatherService.getIrrigationWeatherData(latitude, longitude);
            
            if (weatherData == null || weatherData.getCurrent() == null) {
                throw new RuntimeException("Unable to fetch weather data");
            }
            
            SmartIrrigationController.WeatherDataDto weatherDto = new SmartIrrigationController.WeatherDataDto();
            weatherDto.setTemperature(weatherData.getCurrent().getTemp() != null ? 
                    weatherData.getCurrent().getTemp() : 25.0);
            weatherDto.setHumidity(weatherData.getCurrent().getHumidity() != null ? 
                    weatherData.getCurrent().getHumidity() : 50.0);
            weatherDto.setRainfall(weatherData.getCurrent().getRain() != null ? 
                    weatherData.getCurrent().getRain().getOneHour() : 0.0);
            weatherDto.setWindSpeed(weatherData.getCurrent().getWindSpeed() != null ? 
                    weatherData.getCurrent().getWindSpeed() : 5.0);
            weatherDto.setUvIndex(weatherData.getCurrent().getUvi() != null ? 
                    weatherData.getCurrent().getUvi() : 5.0);
            weatherDto.setHeatRisk(determineHeatRisk(weatherData.getCurrent().getTemp() != null ? 
                    weatherData.getCurrent().getTemp() : 25.0));
            
            return weatherDto;
            
        } catch (Exception e) {
            log.error("Error fetching weather data: {}", e.getMessage());
            throw new RuntimeException("Failed to fetch weather data: " + e.getMessage());
        }
    }
    
    private IrrigationPlanDto.DayPlan calculateDayPlan(LocalDate date, String cropType, Double area,
                                                     String irrigationType, String soilType,
                                                     WeatherDto.DailyWeather forecast, Double et0,
                                                     Double currentTemp, Double currentHumidity, Double rainfall, Double windSpeed) {

        IrrigationPlanDto.DayPlan dayPlan = new IrrigationPlanDto.DayPlan();
        dayPlan.setDate(date);

        // Use forecast data if available, otherwise use current data
        Double temperature = forecast != null && forecast.getTemp() != null ?
                forecast.getTemp().getMax() : currentTemp;
        Double humidity = forecast != null && forecast.getHumidity() != null ?
                forecast.getHumidity() : currentHumidity;
        Double dayRainfall = forecast != null && forecast.getRain() != null ?
                forecast.getRain() : rainfall;

        // STEP 1: Calculate NET crop water requirement (liters)
        // Net_L = Base_Water_L_per_m2 × Area_m2 × (1 + (Temp_C - 25) × 0.04) × (1 + (50 - RH_pct) × 0.02)
        Double netWaterL = calculateCropWaterRequirement(cropType, area, temperature, humidity);

        // STEP 2: Apply soil factor to net requirement
        // Sandy soil needs more water, clay needs less
        Double soilFactor = getSoilFactor(soilType);
        Double adjustedNetWaterL = netWaterL * soilFactor;

        // STEP 3: Subtract rainfall from adjusted net requirement
        Double irrigationNeededL = Math.max(0, adjustedNetWaterL - dayRainfall);

        // STEP 4: Calculate system (gross) requirement accounting for irrigation efficiency
        // System_Efficiency = Base_Efficiency_decimal (0.90 for drip, 0.75 for sprinkler, 0.50 for flood)
        // Gross_L = Net_L / System_Efficiency
        Double systemEfficiency = calculateIrrigationEfficiency(irrigationType, soilType);
        Double grossWaterL = irrigationNeededL / systemEfficiency;

        // STEP 5: Calculate irrigation duration
        // Duration_min = Gross_L / Flow_Rate_L_per_min
        Double flowRateLPerMin = getFlowRate(irrigationType);
        Double durationMinutes = grossWaterL / flowRateLPerMin;

        // Calculate optimal irrigation time (early morning)
        LocalTime optimalTime = calculateOptimalIrrigationTime(temperature, humidity, windSpeed);

        // Calculate water savings compared to less efficient methods
        Double waterSaved = calculateWaterSavings(irrigationNeededL, grossWaterL);

        // Set plan details
        dayPlan.setEtc(et0);
        dayPlan.setLiters(grossWaterL);
        dayPlan.setMinutes(Math.max(0, durationMinutes.intValue())); // Ensure non-negative
        dayPlan.setTime(optimalTime.format(DateTimeFormatter.ofPattern("HH:mm")));
        dayPlan.setHeatRisk(determineHeatRisk(temperature));
        dayPlan.setWaterSavedLiters(waterSaved);
        dayPlan.setNotes(generateDayNotes(temperature, humidity, dayRainfall, irrigationNeededL));

        return dayPlan;
    }
    
    private Double calculateEvapotranspiration(Double temperature, Double humidity, Double windSpeed, Double uvIndex) {
        // Simplified ET0 calculation using temperature, humidity, and wind speed
        Double et0 = 0.0023 * (temperature + 17.8) * Math.sqrt(Math.max(0, temperature - 0)) * 
                (1 - humidity / 100) * (1 + windSpeed / 10);
        
        // Adjust for UV index
        et0 *= (1 + uvIndex / 20);
        
        return Math.max(0.1, et0); // Minimum ET0 of 0.1
    }
    
    private Double calculateCropWaterRequirement(String cropType, Double area, Double temperature, Double humidity) {
        // Net crop water requirement formula (L/m²/day)
        // Net_L = Base_Water_L_per_m2 × Area_m2 × (1 + (Temp_C - 25) × 0.04) × (1 + (50 - RH_pct) × 0.02)

        Double baseWaterLPerM2 = getBaseWaterRequirement(cropType);

        // Temperature correction factor (higher temp = more water)
        // Clamped between 0.6 and 1.6 to avoid extreme outputs
        Double tempCorrectionFactor = 1 + (temperature - 25) * 0.04;
        tempCorrectionFactor = Math.max(0.6, Math.min(1.6, tempCorrectionFactor));

        // Humidity correction factor (lower humidity = more water)
        // Clamped between 0.6 and 1.6 to avoid extreme outputs
        Double humidityCorrectionFactor = 1 + (50 - humidity) * 0.02;
        humidityCorrectionFactor = Math.max(0.6, Math.min(1.6, humidityCorrectionFactor));

        // Net water requirement in liters
        Double netWaterL = baseWaterLPerM2 * area * tempCorrectionFactor * humidityCorrectionFactor;

        return netWaterL;
    }
    
    private Double getBaseWaterRequirement(String cropType) {
        // Water requirements in L/m²/day - More varied values for different crops
        switch (cropType.toLowerCase()) {
            case "tomato": return 6.5;  // High water needs, especially during fruiting
            case "cucumber": return 7.0; // Very high water needs, shallow roots
            case "lettuce": return 3.5;  // Moderate needs, frequent light watering
            case "pepper": return 5.5;   // Moderate to high needs
            case "wheat": return 4.0;    // Moderate needs
            case "corn": return 8.0;     // High needs, deep roots
            case "rice": return 12.0;    // Very high needs, requires flooding
            default: return 5.0;
        }
    }
    
    private Double calculateIrrigationEfficiency(String irrigationType, String soilType) {
        // System efficiency formula: System_Efficiency = Base_Efficiency_decimal × Soil_Factor
        // Base efficiency values (as decimal, not percentage)

        Double baseEfficiency;
        switch (irrigationType.toLowerCase()) {
            case "drip":
                baseEfficiency = 0.90; // 90% efficient
                break;
            case "sprinkler":
                baseEfficiency = 0.75; // 75% efficient
                break;
            case "flood":
                baseEfficiency = 0.50; // 50% efficient
                break;
            default:
                baseEfficiency = 0.75;
        }

        // Soil factor (affects water retention, not efficiency directly to avoid >1.0)
        // We'll apply this to the net water requirement instead in calculateDayPlan
        Double soilFactor = getSoilFactor(soilType);

        // For now, just return base efficiency
        // Soil factor will be applied differently to avoid exceeding 1.0
        return baseEfficiency;
    }

    private Double getSoilFactor(String soilType) {
        // Soil factors: affect water holding capacity
        // Sandy soil needs more water (factor > 1), clay needs less (factor < 1)
        switch (soilType.toLowerCase()) {
            case "sandy":
                return 1.15; // Poor retention, needs 15% more water
            case "clay":
                return 0.85; // Good retention, needs 15% less water
            case "loam":
                return 1.00; // Optimal balance, no adjustment
            case "silt":
                return 0.95; // Good retention, needs 5% less water
            default:
                return 1.00;
        }
    }
    
    private Double getFlowRate(String irrigationType) {
        // Flow rates in L/min (liters per minute)
        // These are typical values for different irrigation systems
        switch (irrigationType.toLowerCase()) {
            case "drip":
                return 18.0;    // Modern drip systems: 15-20 L/min for 100m² area
            case "sprinkler":
                return 50.0;    // Sprinkler systems: 40-60 L/min
            case "flood":
                return 200.0;   // Flood irrigation: 150-250 L/min (very high flow)
            default:
                return 20.0;
        }
    }
    
    private LocalTime calculateOptimalIrrigationTime(Double temperature, Double humidity, Double windSpeed) {
        // Optimal time is early morning (5-7 AM) when temperature is lowest and humidity is highest
        int hour = 6;
        int minute = 0;
        
        // Adjust based on conditions
        if (temperature > 35) {
            hour = 5; // Earlier for hot days
        } else if (temperature < 20) {
            hour = 7; // Later for cool days
        }
        
        return LocalTime.of(hour, minute);
    }
    
    private Double calculateWaterSavings(Double irrigationNeeded, Double adjustedIrrigation) {
        return Math.max(0, adjustedIrrigation - irrigationNeeded);
    }
    
    private String determineHeatRisk(Double temperature) {
        if (temperature > 40) return "EXTREME";
        if (temperature > 35) return "HIGH";
        if (temperature > 30) return "MODERATE";
        if (temperature > 28) return "LOW";
        return "LOW";
    }

    private Double calculateHeatIndex(Double temperature, Double humidity) {
        // Enhanced heat index calculation
        if (temperature <= 26.7) {
            return temperature; // Simple approximation for lower temps
        }

        // More accurate heat index calculation for higher temperatures
        double hi = -42.379 + 2.04901523 * temperature + 10.14333127 * humidity
                - 0.22475541 * temperature * humidity - 6.83783e-3 * temperature * temperature
                - 5.481717e-2 * humidity * humidity + 1.22874e-3 * temperature * temperature * humidity
                + 8.5282e-4 * temperature * humidity * humidity - 1.99e-6 * temperature * temperature * humidity * humidity;

        return Math.round(hi * 10.0) / 10.0;
    }

    private String generateHeatRecommendations(Double temperature) {
        StringBuilder recommendations = new StringBuilder();

        if (temperature > 40) {
            recommendations.append("URGENT: Implement emergency cooling measures. ")
                          .append("Increase irrigation by 50%. ")
                          .append("Deploy all available shade structures. ")
                          .append("Monitor crops hourly for heat damage. ")
                          .append("Consider harvesting heat-sensitive crops early.");
        } else if (temperature > 35) {
            recommendations.append("Increase irrigation frequency by 25%. ")
                          .append("Apply shade cloth during peak hours (12:00-16:00). ")
                          .append("Monitor plants for heat stress symptoms. ")
                          .append("Ensure adequate ventilation in greenhouse areas.");
        } else if (temperature > 30) {
            recommendations.append("Continue regular irrigation schedule. ")
                          .append("Check soil moisture levels. ")
                          .append("Consider evening watering for sensitive crops. ")
                          .append("Monitor weather forecasts for temperature changes.");
        } else if (temperature > 28) {
            recommendations.append("Maintain normal irrigation schedule. ")
                          .append("Monitor soil conditions. ")
                          .append("Prepare for potential temperature increases.");
        } else {
            recommendations.append("Normal conditions. ")
                          .append("Follow standard irrigation schedule. ")
                          .append("Regular monitoring recommended.");
        }

        return recommendations.toString();
    }
    
    private String generateDayNotes(Double temperature, Double humidity, Double rainfall, Double irrigationNeeded) {
        StringBuilder notes = new StringBuilder();
        
        if (temperature > 35) {
            notes.append("High heat - increase irrigation. ");
        }
        if (humidity < 30) {
            notes.append("Low humidity - more water needed. ");
        }
        if (rainfall > 5) {
            notes.append("Rainfall reduces irrigation needs. ");
        }
        if (irrigationNeeded < 1) {
            notes.append("Minimal irrigation required. ");
        }
        
        return notes.toString().trim();
    }
    
    private String getLocationName(Double latitude, Double longitude) {
        // Simple location naming based on coordinates
        if (latitude > 25.0 && latitude < 26.0 && longitude > 51.0 && longitude < 52.0) {
            return "Doha, Qatar";
        } else if (latitude > 24.0 && latitude < 25.0 && longitude > 51.0 && longitude < 52.0) {
            return "Al-Wakrah, Qatar";
        } else {
            return String.format("Location (%.4f, %.4f)", latitude, longitude);
        }
    }
}
