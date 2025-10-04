package com.hackathon.agriculture_backend.dto.insectid;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class InsectIdResponseDto {
    @JsonProperty("access_token")
    private String accessToken;
    private InsectIdResultDto result;
}
