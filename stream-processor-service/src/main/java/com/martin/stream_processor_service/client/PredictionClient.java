package com.martin.stream_processor_service.client;

import com.martin.stream_processor_service.model.dto.PredictionRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class PredictionClient {


    private final WebClient webClient;

    public PredictionClient(WebClient.Builder builder) {
        // Usamos localhost mientras el ML está corriendo localmente
        this.webClient = builder
                .baseUrl("http://localhost:9000")
                .build();
    }

    public Mono<String> predict(PredictionRequest request) {
        return webClient.post()
                .uri("/predict")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .doOnError(error -> log.error("❌ ML request failed: {}", error.getMessage()));
    }
}
