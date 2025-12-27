# Real-Time Fleet Telemetry & Anomaly Detection Platform
**Event-Driven Architecture · Streaming · Kafka · Spring Boot · Machine Learning · WebSockets**

<p align="center">
  <img src="https://github.com/MartinImoberdorf/Real-Time-Fleet-Monitoring-System/blob/main/Documentation/Imagen1.png?raw=true" alt="System Overview" width="100%">
</p>

## Overview

This project implements a **real-time, event-driven telemetry processing platform** designed to ingest high-frequency vehicle data, evaluate behavioral anomalies using machine learning, and stream enriched results to clients with **sub-second latency**.

The system mirrors architectures used in **large-scale mobility, IoT, and observability platforms**, emphasizing **scalability, loose coupling, and asynchronous processing** over UI complexity.


## Problem Statement

Fleet and telemetry systems must process **continuous streams of data** while detecting abnormal behavior (e.g., sudden deceleration, sensor malfunction, risky driving) **without blocking ingestion or degrading throughput**.

Traditional request-response architectures do not scale well for this type of workload. This project addresses the problem using:
* **Streaming-first design** to handle high ingestion rates.
* **Event-driven communication** to decouple services.
* **ML-based inference** for intelligent real-time analysis.

## Architecture

<p align="center">
  <img src="https://github.com/MartinImoberdorf/Real-Time-Fleet-Monitoring-System/blob/main/Documentation/Imagen2.png?raw=true" alt="Detailed Architecture" width="100%">
</p>

## Core Components

### 1. Telemetry Producer
The **Telemetry Producer** acts as the primary data source by simulating real-time vehicle behavior. It generates high-frequency telemetry events containing multiple sensor variables and publishes them directly to **Kafka topics**. This component is designed to be highly scalable, allowing the system to simulate and manage thousands of independent vehicles simultaneously without performance degradation.

### 2. Stream Processor Service
Serving as the "brain" of the data pipeline, the **Stream Processor Service** orchestrates the flow of information. It consumes raw events from Kafka and performs real-time data transformation to ensure payloads are compatible with the machine learning model. By leveraging **Spring WebFlux**, it calls the inference service asynchronously, ensuring the system remains non-blocking and handles backpressure efficiently while enriching the telemetry with anomaly scores.

### 3. ML Inference Service
The **ML Inference Service** provides the critical intelligence needed for real-time risk assessment. It exposes a high-throughput REST API that evaluates telemetry data using a reconstruction error-based detection logic. This service is stateless and optimized for low-latency responses, allowing it to determine if a specific set of vehicle metrics indicates a potential safety incident or a technical malfunction.

| Input Sample | Output Sample |
| :--- | :--- |
| `{"speed": 30.76, "accel": -9.12...}` | `{"reconstruction_error": 0.0719, "is_anomaly": false}` |

### 4. WebSocket Gateway
The **WebSocket Gateway** ensures seamless real-time data delivery to the end user by maintaining persistent, open connections. Instead of relying on inefficient polling, the gateway pushes enriched telemetry—including both raw data and its corresponding ML anomaly score—to connected clients the moment it is processed. This architecture is vital for achieving the sub-second latency required for live fleet monitoring.

### 5. Frontend Client
The **Frontend Client** provides a lightweight yet powerful monitoring dashboard built with vanilla JavaScript and the native WebSocket API. It focuses on high-frequency data visualization, rendering live telemetry trends and immediately flagging anomalies with visual alerts (such as red highlighting). This allows dispatchers or fleet managers to identify and respond to risky driving behavior or sensor failures in real time.

## Machine Learning Approach

### 1. Data Processing
The foundation of the detection system lies in meticulous data preparation. The model evaluates critical telemetry metrics such as vehicle speed, acceleration, engine temperature, fuel levels, and local traffic conditions. To ensure accuracy, all data undergoes normalization using a **StandardScaler**. This process scales features with different magnitudes to a uniform range, preventing variables with higher absolute values from disproportionately influencing the error calculation.

### 2. Autoencoder Architecture

The system utilizes a **Deep Autoencoder** architecture implemented in Python using TensorFlow and Keras. The **Encoder** phase focuses on compressing the high-dimensional input telemetry into a lower-dimensional latent space, which forces the model to learn the most essential correlations between driving variables. Subsequently, the **Decoder** phase attempts to reconstruct the original telemetry from this compressed representation, serving as the basis for anomaly detection.

### 3. Training Strategy
The training strategy is based on the principle of learning "normality." The model is trained exclusively on datasets representing standard, safe driving patterns with the primary objective of minimizing the **Mean Squared Error (MSE)** between the original input and its reconstruction. Once training is complete, a percentile-based threshold is established; any data point that the model cannot reconstruct within this defined boundary is classified as an outlier.

### 4. Mathematical Foundation
The model identifies anomalies by quantifying how difficult it is to reconstruct specific data points. During the detection phase, if the input telemetry is significantly different from the patterns learned during training (out of distribution), the model will fail to reconstruct it accurately. This results in a high reconstruction error. The final decision is governed by a simple but effective rule: if the **reconstruction error** exceeds the pre-calculated **anomaly threshold**, the event is flagged as an **Anomaly**; otherwise, it is treated as **Normal Behavior**.

* **Logic:** The model attempts to reconstruct the input telemetry. 
* **Detection:** If the input is "strange" (out of distribution), the model fails to reconstruct it accurately.
* **Decision Rule:**
    - If `reconstruction_error > anomaly_threshold` → **Anomaly**
    - Else → **Normal Behavior**
 
      
## Technology Stack

| Layer | Technologies |
| :--- | :--- |
| **Backend** | Java 17, Spring Boot, Spring WebFlux, Apache Kafka |
| **Machine Learning** | Python, Scikit-Learn/TensorFlow, REST API |
| **Streaming** | Kafka, WebSockets |
| **Infrastructure** | Docker, Docker Compose |
| **Frontend** | HTML5, Vanilla JavaScript |

## Author

* **Martin Imoberdorf** - [GitHub](https://www.github.com/MartinImoberdorf) 
