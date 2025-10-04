package com.hackathon.agriculture_backend.dto.insectid;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class InsectIdSuggestionDto {
    private String id;
    private String name;
    private double probability;
}
