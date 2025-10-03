package com.hackathon.agriculture_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NasaEtDto {
    
    private String type;
    private Geometry geometry;
    private Properties properties;
    private Header header;
    private List<String> messages;
    private Parameters parameters;
    private Times times;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Geometry {
        private String type;
        private List<Double> coordinates;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Properties {
        private Map<String, Map<String, String>> parameter;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Header {
        private String title;
        private Api api;
        private Integer fill_value;
        private String start;
        private String end;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Api {
        private String version;
        private String name;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Parameters {
        private Map<String, ParameterInfo> ET0;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParameterInfo {
        private String units;
        private String longname;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Times {
        private String data;
        private String process;
    }
}



