package com.martin.stream_processor_service.services;

import com.martin.model.VehicleData;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Slf4j
@Service
public class StreamProcessorService {

    private final List<VehicleData> vehicleDataList = new ArrayList<>();
    private int counter = 0;

    @KafkaListener(topics = "vehicle-telemetry", groupId = "vehicle-processor-group")
    public void consumeVehicleData(VehicleData data) {
        log.info("Received telemetry #{} -> {}", ++counter, data.getVehicleId());

        vehicleDataList.add(data);

        if (vehicleDataList.size() >= 2000) {
            saveToExcel(vehicleDataList);
            vehicleDataList.clear();
            counter = 0;
        }
    }

    private void saveToExcel(List<VehicleData> dataList) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Vehicle Data");

        // ---------- HEADERS ----------
        String[] headers = {
                "vehicleId", "timestamp",
                "latitude", "longitude",
                "speed", "previousSpeed", "acceleration",
                "temperature", "battery", "fuelLevel",
                "weather", "roadType", "speedLimit",
                "night", "trafficLevel",
                "anomaly", "anomalyType"
        };

        Row headerRow = sheet.createRow(0);

        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }

        // ---------- DATA ----------
        int rowNum = 1;
        for (VehicleData data : dataList) {
            Row row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(data.getVehicleId());
            row.createCell(1).setCellValue(data.getTimestamp().toString());

            row.createCell(2).setCellValue(data.getLatitude());
            row.createCell(3).setCellValue(data.getLongitude());

            row.createCell(4).setCellValue(data.getSpeed());
            row.createCell(5).setCellValue(data.getPreviousSpeed());
            row.createCell(6).setCellValue(data.getAcceleration());

            row.createCell(7).setCellValue(data.getTemperature());
            row.createCell(8).setCellValue(data.getBattery());
            row.createCell(9).setCellValue(data.getFuelLevel());

            row.createCell(10).setCellValue(data.getWeather());
            row.createCell(11).setCellValue(data.getRoadType());
            row.createCell(12).setCellValue(data.getSpeedLimit());

            row.createCell(13).setCellValue(data.isNight());
            row.createCell(14).setCellValue(data.getTrafficLevel());

            row.createCell(15).setCellValue(data.isAnomaly());
            row.createCell(16).setCellValue(
                    data.getAnomalyType() != null ? data.getAnomalyType() : ""
            );
        }

        // Ajusta automático
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // ---------- SAVE FILE ----------
        String fileName = "vehicle_data" + ".xlsx";

        try (FileOutputStream fileOut = new FileOutputStream(fileName)) {
            workbook.write(fileOut);
            log.info("Excel saved: {}", fileName);
        } catch (IOException e) {
            log.error("Error saving Excel", e);
        } finally {
            try {
                workbook.close();
            } catch (IOException ignored) {}
        }
    }
}

