package com.hackathon.agriculture_backend.dto.insectid;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class InsectIdClassificationDto {
    private List<InsectIdSuggestionDto> suggestions;
}
