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
    private double previousSpeed;
    private double acceleration;

    private double temperature;
    private double battery;
    private double fuelLevel;

    private String weather;       // clear, rain, fog, storm
    private String roadType;      // highway, urban, rural
    private double speedLimit;
    private boolean night;
    private int trafficLevel;     // 1–5

    private boolean anomaly;      // para ML
    private String anomalyType;

}