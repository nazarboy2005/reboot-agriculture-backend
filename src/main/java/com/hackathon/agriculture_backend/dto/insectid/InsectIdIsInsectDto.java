package com.hackathon.agriculture_backend.dto.insectid;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class InsectIdIsInsectDto {
    private boolean binary;
    private double probability;
}
