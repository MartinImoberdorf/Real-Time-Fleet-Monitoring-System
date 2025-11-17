package com.martin.model;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleData {
    private String vehicleId;
    private Instant timestamp;
    private double latitude;
    private double longitude;
    private double speed;
    private double temperature;
    private double battery;
    private double fuelLevel;
}