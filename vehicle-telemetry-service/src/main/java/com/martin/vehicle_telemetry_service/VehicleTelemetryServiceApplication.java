package com.martin.vehicle_telemetry_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // activar la generacion de datos
public class VehicleTelemetryServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(VehicleTelemetryServiceApplication.class, args);
	}

}


