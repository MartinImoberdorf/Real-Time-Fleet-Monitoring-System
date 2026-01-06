package com.martin.stream_processor_service.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PredictionResponse {
    private PredictionRequest input;

    @JsonProperty("reconstruction_error")
    private double reconstructionError;

    @JsonProperty("anomaly_threshold")
    private double anomalyThreshold;

    @JsonProperty("is_anomaly")
    private boolean isAnomaly;
}