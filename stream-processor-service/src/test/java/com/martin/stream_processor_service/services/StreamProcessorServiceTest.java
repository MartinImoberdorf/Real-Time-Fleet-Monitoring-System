package com.martin.stream_processor_service.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.martin.model.VehicleData;
import com.martin.stream_processor_service.client.PredictionClient;
import com.martin.stream_processor_service.config.TelemetryWebSocketHandler;
import com.martin.stream_processor_service.model.dto.PredictionRequest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.awaitility.Awaitility;
import org.testcontainers.utility.DockerImageName;
import reactor.core.publisher.Mono;
import static org.assertj.core.api.Assertions.assertThat;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.TimeUnit;



@SpringBootTest
@Testcontainers
class StreamProcessorServiceTest {

    @Container
    static KafkaContainer kafka =
            new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"));

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Autowired
    KafkaTemplate<String, VehicleData> kafkaTemplate;

    @MockBean
    PredictionClient predictionClient;

    @MockBean
    TelemetryWebSocketHandler wsHandler;

    @Autowired
    ObjectMapper objectMapper;

    private static final String TOPIC = "vehicle-telemetry";

    // Integration Test

    @Test
    void shouldConsumeVehicleData() throws Exception{
        VehicleData data = VehicleData.builder()
                .vehicleId("vehicle123")
                .timestamp(Instant.now())
                .latitude(37.7749)
                .longitude(-122.4194)
                .speed(80.0)
                .previousSpeed(75.0)
                .acceleration(0.5)
                .temperature(22.5)
                .battery(85.0)
                .fuelLevel(50.0)
                .weather("clear")
                .roadType("highway")
                .speedLimit(100.0)
                .night(false)
                .trafficLevel(2)
                .build();

        PredictionRequest predictionRequest =
                PredictionRequest.fromVehicleData(data);

        String mlResponseJson = objectMapper.writeValueAsString(
                Map.of(
                        "input", predictionRequest,
                        "reconstruction_error", 0.12,
                        "anomaly_threshold", 0.3,
                        "is_anomaly", false
                )
        );



        Mockito.when(predictionClient.predict(Mockito.any()))
                .thenReturn(Mono.just(mlResponseJson));

        kafkaTemplate.send(TOPIC, data);
        kafkaTemplate.flush();

        ArgumentCaptor<VehicleData> captor =
                ArgumentCaptor.forClass(VehicleData.class);

        Awaitility.await()
                .atMost(3, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    Mockito.verify(predictionClient, Mockito.atLeastOnce())
                            .predict(Mockito.any());

                    Mockito.verify(wsHandler, Mockito.atLeastOnce())
                            .sendTelemetry(captor.capture());
                });

        VehicleData sent = captor.getValue();
        assertThat(sent.getVehicleId()).isEqualTo("vehicle123");
        assertThat(sent.isAnomaly()).isFalse();
    }


    @Test
    void shouldConsumeVehicleWithAnomaly() throws Exception{
        VehicleData data = VehicleData.builder()
                .vehicleId("vehicle123")
                .timestamp(Instant.now())
                .latitude(37.7749)
                .longitude(-122.4194)
                .speed(80.0)
                .previousSpeed(75.0)
                .acceleration(0.5)
                .temperature(22.5)
                .battery(85.0)
                .fuelLevel(50.0)
                .weather("clear")
                .roadType("highway")
                .speedLimit(100.0)
                .night(false)
                .trafficLevel(2)
                .build();

        PredictionRequest predictionRequest =
                PredictionRequest.fromVehicleData(data);

        String mlResponseJson = objectMapper.writeValueAsString(
                Map.of(
                        "input", predictionRequest,
                        "reconstruction_error", 50.12,
                        "anomaly_threshold", 36.7,
                        "is_anomaly", true
                )
        );



        Mockito.when(predictionClient.predict(Mockito.any()))
                .thenReturn(Mono.just(mlResponseJson));

        kafkaTemplate.send(TOPIC, data);
        kafkaTemplate.flush();

        ArgumentCaptor<VehicleData> captor =
                ArgumentCaptor.forClass(VehicleData.class);

        Awaitility.await()
                .atMost(3, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    Mockito.verify(predictionClient, Mockito.atLeastOnce())
                            .predict(Mockito.any());

                    Mockito.verify(wsHandler, Mockito.atLeastOnce())
                            .sendTelemetry(captor.capture());
                });

        VehicleData sent = captor.getValue();
        assertThat(sent.getVehicleId()).isEqualTo("vehicle123");
        assertThat(sent.isAnomaly()).isTrue();
    }

    // Unit with Mocks

    @Test
    void shouldMLGiveAnError(){
        VehicleData data = VehicleData.builder()
                .vehicleId("vehicle123")
                .timestamp(Instant.now())
                .latitude(37.7749)
                .longitude(-122.4194)
                .speed(80.0)
                .previousSpeed(75.0)
                .acceleration(0.5)
                .temperature(22.5)
                .battery(85.0)
                .fuelLevel(50.0)
                .weather("clear")
                .roadType("highway")
                .speedLimit(100.0)
                .night(false)
                .trafficLevel(2)
                .build();

        Mockito.when(predictionClient.predict(Mockito.any()))
                .thenReturn(Mono.error(new RuntimeException("ML down")));

        kafkaTemplate.send(TOPIC, data);
        kafkaTemplate.flush();

        Awaitility.await()
                .atMost(3, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    Mockito.verify(wsHandler, Mockito.never())
                            .sendTelemetry(Mockito.any());
                });
    }


    @Test
    void shouldMLResponseAInvalidJSON(){
        VehicleData data = VehicleData.builder()
                .vehicleId("vehicle123")
                .timestamp(Instant.now())
                .latitude(37.7749)
                .longitude(-122.4194)
                .speed(80.0)
                .previousSpeed(75.0)
                .acceleration(0.5)
                .temperature(22.5)
                .battery(85.0)
                .fuelLevel(50.0)
                .weather("clear")
                .roadType("highway")
                .speedLimit(100.0)
                .night(false)
                .trafficLevel(2)
                .build();

        Mockito.when(predictionClient.predict(Mockito.any()))
                .thenReturn(Mono.just("{ invalid json "));

        kafkaTemplate.send(TOPIC, data);
        kafkaTemplate.flush();

        Awaitility.await()
                .atMost(3, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    Mockito.verify(wsHandler, Mockito.never())
                            .sendTelemetry(Mockito.any());
                });

    }



}


