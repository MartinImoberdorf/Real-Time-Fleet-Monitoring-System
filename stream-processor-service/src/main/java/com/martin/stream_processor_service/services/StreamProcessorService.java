package com.martin.stream_processor_service.services;

import com.martin.model.VehicleData;
import com.martin.stream_processor_service.client.PredictionClient;
import com.martin.stream_processor_service.model.dto.PredictionRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class StreamProcessorService {

    private final PredictionClient predictionClient;

    @KafkaListener(topics = "vehicle-telemetry", groupId = "vehicle-processor-group")
    public void consumeVehicleData(VehicleData data) {

        PredictionRequest request = PredictionRequest.fromVehicleData(data);

        // Asíncrono, no bloquea el listener
        predictionClient.predict(request)
                .subscribe(
                        response -> log.info("🤖 ML response vehicle {} → {}", data.getVehicleId(), response),
                        error -> log.error("❌ ML error vehicle {}: {}", data.getVehicleId(), error.getMessage())
                );
    }



}

