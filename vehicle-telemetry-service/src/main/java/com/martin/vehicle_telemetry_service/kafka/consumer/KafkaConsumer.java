package com.martin.vehicle_telemetry_service.kafka.consumer;

import com.martin.vehicle_telemetry_service.model.entity.VehicleData;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaConsumer {
    private final SimpMessagingTemplate messageTemplate;

    @KafkaListener(topics = "vehicle-telemetry", groupId = "vehicle-telemetry-group")
    public void consumeVehicleData(VehicleData data) {
        messageTemplate.convertAndSend("/topic/vehicles", data);
    }



}
