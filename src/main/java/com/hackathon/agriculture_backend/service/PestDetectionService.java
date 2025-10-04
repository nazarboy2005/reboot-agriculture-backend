package com.hackathon.agriculture_backend.service;

import com.hackathon.agriculture_backend.dto.insectid.InsectIdResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;

@Service
@Slf4j
public class PestDetectionService {

    private final WebClient webClient;

    @Value("${insect.id.api.key}")
    private String apiKey;

    private final String apiUrl = "https://insect.kindwise.com/api/v1/identification";

    public PestDetectionService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public Mono<InsectIdResponseDto> detectPest(MultipartFile image) throws IOException {
        log.info("Sending pest detection request for image: {}", image.getOriginalFilename());

        if (apiKey == null || apiKey.isEmpty() || "your_api_key".equals(apiKey)) {
            log.error("Insect.id API key is not configured.");
            return Mono.error(new IllegalStateException("Insect.id API key is not configured. Please set INSECT_ID_API_KEY environment variable in your configuration. You can get a key from https://insect.kindwise.com/"));
        }

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("images", new ByteArrayResource(image.getBytes()) {
            @Override
            public String getFilename() {
                return image.getOriginalFilename();
            }
        });
        body.add("similar_images", "true");


        return webClient.post()
                .uri(apiUrl)
                .header("Api-Key", apiKey)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(body))
                .retrieve()
                .bodyToMono(InsectIdResponseDto.class)
                .doOnSuccess(response -> log.info("Successfully received response from Insect.id API"))
                .doOnError(error -> log.error("Error calling Insect.id API: {}", error.getMessage()));
    }
}
