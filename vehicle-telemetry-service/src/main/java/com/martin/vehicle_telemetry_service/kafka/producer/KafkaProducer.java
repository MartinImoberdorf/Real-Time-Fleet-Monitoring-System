package com.martin.vehicle_telemetry_service.kafka.producer;

import com.martin.model.VehicleData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducer {

    private final KafkaTemplate<String, VehicleData> kafkaTemplate;

    public void sendVehicleData(String topic, VehicleData data) {
        kafkaTemplate.send(topic, data.getVehicleId(),data);
        log.info("✅ Enviado a Kafka: " + data);
    }
}
