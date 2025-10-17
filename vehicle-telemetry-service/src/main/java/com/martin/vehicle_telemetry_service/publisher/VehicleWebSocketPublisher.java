package com.martin.vehicle_telemetry_service.publisher;

import com.martin.vehicle_telemetry_service.model.entity.VehicleData;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VehicleWebSocketPublisher {

    private final SimpMessagingTemplate template;

    public void sendVehicleData(VehicleData data) {
        template.convertAndSend("/topic/vehicles", data);
    }
}