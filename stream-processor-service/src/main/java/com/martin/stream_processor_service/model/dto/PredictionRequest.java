package com.martin.stream_processor_service.model.dto;

import com.martin.model.VehicleData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PredictionRequest {

    private Double latitude;
    private Double longitude;
    private Double speed;
    private Double previousSpeed;
    private Double acceleration;
    private Double temperature;
    private Double battery;
    private Double fuelLevel;
    private Double speedLimit;
    private Double trafficLevel;

    public static PredictionRequest fromVehicleData(VehicleData v) {
        return new PredictionRequest(
                v.getLatitude(),
                v.getLongitude(),
                v.getSpeed(),
                v.getPreviousSpeed(),
                v.getAcceleration(),
                v.getTemperature(),
                v.getBattery(),
                v.getFuelLevel(),
                v.getSpeedLimit(),
                (double) v.getTrafficLevel()
        );
    }
}
