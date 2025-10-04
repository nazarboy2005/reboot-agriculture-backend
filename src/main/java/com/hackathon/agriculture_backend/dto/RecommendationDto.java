package com.hackathon.agriculture_backend.dto;

import com.hackathon.agriculture_backend.model.IrrigationRecommendation;
import lombok.Data;
import java.time.LocalDate;
import java.time.Instant;

@Data
public class RecommendationDto {
    private Long id;
    private Long farmerId;
    private LocalDate date;
    private String cropType;
    private String locationName;
    private Double tempC;
    private Double humidity;
    private Double rainfallMm;
    private Double evapotranspiration;
    private String recommendation;
    private String explanation;
    private Double waterSavedLiters;
    private Instant createdAt;
    private Instant updatedAt;

    public static RecommendationDto fromEntity(IrrigationRecommendation entity) {
        if (entity == null) {
            return null;
        }
        RecommendationDto dto = new RecommendationDto();
        dto.setId(entity.getId());
        if (entity.getFarmer() != null) {
            dto.setFarmerId(entity.getFarmer().getId());
        }
        dto.setDate(entity.getDate());
        dto.setCropType(entity.getCropType());
        dto.setLocationName(entity.getLocationName());
        dto.setTempC(entity.getTempC());
        dto.setHumidity(entity.getHumidity());
        dto.setRainfallMm(entity.getRainfallMm());
        dto.setEvapotranspiration(entity.getEvapotranspiration());
        dto.setRecommendation(entity.getRecommendation());
        dto.setExplanation(entity.getExplanation());
        dto.setWaterSavedLiters(entity.getWaterSavedLiters());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }
}



