package com.martin.vehicle_telemetry_service.services;


import com.martin.model.VehicleData;
import com.martin.vehicle_telemetry_service.kafka.producer.KafkaProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelemetrySimulator {
    private final Random random = new Random();
    private final List<String> vehicles = List.of("CAR-001","CAR-002","CAR-003","CAR-004");
    private final KafkaProducer kafkaProducer;

    @Scheduled(fixedRate = 4000)
    public void generateTelemetry() {
        vehicles.forEach(this::generateVehicleData);
    }

    private void generateVehicleData(String vehicleId) {
        double baseSpeed = 60 + random.nextGaussian() * 20;   // curva normal
        double temp = 75 + random.nextGaussian() * 10;
        double battery = 80 - random.nextDouble() * 0.2;      // lenta descarga
        double fuel = 100 - random.nextDouble() * 0.5;        // consumo gradual

        VehicleData data = VehicleData.builder()
                .vehicleId(vehicleId)
                .timestamp(Instant.now())
                .latitude(-31.4201 + random.nextDouble()/100)
                .longitude(-64.1888 + random.nextDouble()/100)
                .speed(Math.max(0, baseSpeed))
                .temperature(temp)
                .battery(battery)
                .fuelLevel(fuel)
                .build();
        kafkaProducer.sendVehicleData("vehicle-telemetry", data); // Enviar a Kafka
        log.info("Generated telemetry: {}", data);
    }
}