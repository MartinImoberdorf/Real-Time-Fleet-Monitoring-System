package com.martin.vehicle_telemetry_service.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VehicleData {
    private String vehicleId;
    private Instant timestamp;
    private double latitude;
    private double longitude;
    private double speed;          // km/h
    private double temperature;    // °C del motor
    private double battery;        // %
    private double fuelLevel;      // %
}