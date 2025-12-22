package com.martin.stream_processor_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableKafka
@EnableScheduling
public class StreamProcessorServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(StreamProcessorServiceApplication.class, args);
	}

}
