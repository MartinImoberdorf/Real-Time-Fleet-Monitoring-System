package com.martin.stream_processor_service.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.martin.model.VehicleData;
import com.martin.stream_processor_service.client.PredictionClient;
import com.martin.stream_processor_service.config.TelemetryWebSocketHandler;
import com.martin.stream_processor_service.model.dto.PredictionRequest;
import com.martin.stream_processor_service.model.dto.PredictionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class StreamProcessorService {

    private final PredictionClient predictionClient;
    @Autowired
    private final TelemetryWebSocketHandler wsHandler;

    private final ObjectMapper mapper = new ObjectMapper();

    @KafkaListener(topics = "vehicle-telemetry", groupId = "vehicle-processor-group")
    public void consumeVehicleData(VehicleData data) {

        PredictionRequest request = PredictionRequest.fromVehicleData(data);

        predictionClient.predict(request)
                .subscribe(
                        response -> {
                            try {
                                PredictionResponse predictionResponse = mapper.readValue(response, PredictionResponse.class);
                                VehicleData vehicleData = mapper.convertValue(predictionResponse.getInput(), VehicleData.class);

                                // Copy missing fields from the original data
                                vehicleData.setVehicleId(data.getVehicleId());
                                vehicleData.setTimestamp(data.getTimestamp());
                                vehicleData.setWeather(data.getWeather());
                                vehicleData.setRoadType(data.getRoadType());
                                vehicleData.setAnomaly(predictionResponse.isAnomaly());
                                vehicleData.setAnomalyType(data.getAnomalyType());

                                wsHandler.sendTelemetry(vehicleData);
                            } catch (Exception e) {
                                log.error("❌ Failed to parse response to VehicleData: {}", e.getMessage());
                            }
                        },
                        error -> log.error("❌ ML error vehicle {}: {}", data.getVehicleId(), error.getMessage())
                );
    }
}


