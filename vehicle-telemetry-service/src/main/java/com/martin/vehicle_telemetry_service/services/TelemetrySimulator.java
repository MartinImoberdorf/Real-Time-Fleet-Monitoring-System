package com.martin.vehicle_telemetry_service.services;

import com.martin.model.VehicleData;
import com.martin.vehicle_telemetry_service.kafka.producer.KafkaProducer;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelemetrySimulator {

    private final KafkaProducer kafkaProducer;
    private final SecureRandom secureRandom;
    private final Map<String, Double> lastSpeedMap = new HashMap<>();
    private List<String> vehicles;

    @PostConstruct
    public void init() {
        vehicles = generateRandomVehicleIds(10);
    }

    private List<String> generateRandomVehicleIds(int count) {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add(generateId());
        }
        return list;
    }

    private String generateId() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt(secureRandom.nextInt(chars.length())));
        }
        return sb.toString();
    }

    @Scheduled(fixedRate = 4000)
    public void generateTelemetry() {
        vehicles.forEach(this::generateVehicleData);
    }

    private void generateVehicleData(String vehicleId) {
        String weather = pick(weatherOptions());
        String roadType = pick(List.of("urban", "highway", "rural"));
        boolean night = secureRandom.nextBoolean();
        int traffic = secureRandom.nextInt(5) + 1;

        double speedLimit = switch (roadType) {
            case "highway" -> 120;
            case "urban" -> 60;
            default -> 90;
        };

        double baseSpeed = generateBaseSpeed(speedLimit);
        double temperature = 70 + secureRandom.nextGaussian() * 5;
        double battery = 80 - secureRandom.nextDouble() * 0.5;
        double fuel = 100 - secureRandom.nextDouble() * 0.8;

        double previousSpeed = lastSpeedMap.getOrDefault(vehicleId, baseSpeed);
        double acceleration = baseSpeed - previousSpeed;
        lastSpeedMap.put(vehicleId, baseSpeed);

        boolean anomaly = false;
        String anomalyType = null;

        if (secureRandom.nextDouble() < 0.05) {
            int type = secureRandom.nextInt(3);

            switch (type) {
                case 0 -> {
                    baseSpeed = speedLimit + 50 + secureRandom.nextDouble() * 30;
                    anomaly = true;
                    anomalyType = "overspeed";
                }
                case 1 -> {
                    temperature = 110 + secureRandom.nextDouble() * 30;
                    anomaly = true;
                    anomalyType = "engine_overheat";
                }
                case 2 -> {
                    battery = 10 + secureRandom.nextDouble() * 5;
                    anomaly = true;
                    anomalyType = "battery_low";
                }
                default -> {
                    battery = 10 + secureRandom.nextDouble() * 10;
                    anomaly = false;
                    anomalyType = "";

                }
            }
        }

        VehicleData data = VehicleData.builder()
                .vehicleId(vehicleId)
                .timestamp(Instant.now())
                .latitude(-31.4201 + secureRandom.nextDouble() / 100)
                .longitude(-64.1888 + secureRandom.nextDouble() / 100)
                .speed(baseSpeed)
                .previousSpeed(previousSpeed)
                .acceleration(acceleration)
                .temperature(temperature)
                .battery(battery)
                .fuelLevel(fuel)
                .weather(weather)
                .roadType(roadType)
                .speedLimit(speedLimit)
                .night(night)
                .trafficLevel(traffic)
                .anomaly(anomaly)
                .anomalyType(anomalyType)
                .build();

        kafkaProducer.sendVehicleData("vehicle-telemetry", data);
        log.info("{}", data);
    }

    private List<String> weatherOptions() {
        return List.of("clear", "rain", "fog", "storm");
    }

    private String pick(List<String> list) {
        return list.get(secureRandom.nextInt(list.size()));
    }

    private double generateBaseSpeed(double limit) {
        double base = limit * 0.6 + secureRandom.nextGaussian() * 10;
        return Math.max(0, base);
    }
}