# Real-Time Fleet Telemetry & Anomaly Detection Platform
**Event-Driven Architecture · Streaming · Kafka · Spring Boot · Machine Learning · WebSockets**

<p align="center">
  <img src="https://github.com/MartinImoberdorf/Real-Time-Fleet-Monitoring-System/blob/main/Documentation/Imagen1.png?raw=true" alt="System Overview" width="100%">
</p>


---

## 📝 Overview

This project implements a **real-time, event-driven telemetry processing platform** designed to ingest high-frequency vehicle data, evaluate behavioral anomalies using machine learning, and stream enriched results to clients with **sub-second latency**.

The system mirrors architectures used in **large-scale mobility, IoT, and observability platforms**, emphasizing **scalability, loose coupling, and asynchronous processing** over UI complexity.

---

## ⚠️ Problem Statement

Fleet and telemetry systems must process **continuous streams of data** while detecting abnormal behavior (e.g., sudden deceleration, sensor malfunction, risky driving) **without blocking ingestion or degrading throughput**.

Traditional request-response architectures do not scale well for this type of workload. This project addresses the problem using:
* **Streaming-first design** to handle high ingestion rates.
* **Event-driven communication** to decouple services.
* **ML-based inference** for intelligent real-time analysis.

---

## 🏗️ High-Level Architecture

<p align="center">
  <img src="https://github.com/MartinImoberdorf/Real-Time-Fleet-Monitoring-System/blob/main/Documentation/Imagen2.png?raw=true" alt="Detailed Architecture" width="100%">
</p>

---

## 🧩 Core Components

### 1. Telemetry Producer
* **Purpose:** Simulates real-time vehicle telemetry.
* **Action:** Generates high-frequency events and publishes them to **Kafka topics**.
* **Scalability:** Can scale horizontally to simulate thousands of vehicles independently.

### 2. Stream Processor Service
* **Purpose:** The "brain" of the data flow.
* **Action:** Consumes raw events, transforms them for ML compatibility, and calls the inference service asynchronously.
* **Attributes:** Stateless, non-blocking (**Spring WebFlux**), and backpressure-friendly.

### 3. ML Inference Service
* **Purpose:** Provides real-time anomaly scoring.
* **Logic:** Uses reconstruction error-based detection (**Autoencoders**).
* **Interface:** Exposes a high-throughput REST API.

| Input Sample | Output Sample |
| :--- | :--- |
| `{"speed": 30.76, "accel": -9.12...}` | `{"reconstruction_error": 0.0719, "is_anomaly": false}` |

### 4. WebSocket Gateway
* **Purpose:** Real-time data delivery to the end user.
* **Action:** Pushes enriched telemetry (Data + ML Score) to connected clients immediately, eliminating the need for polling.

### 5. Frontend Client
* **Purpose:** Monitoring dashboard.
* **Action:** A lightweight interface that highlights anomalies in red and displays live telemetry trends using the native WebSocket API.

---

## 🧠 Machine Learning Approach

The platform utilizes **Unsupervised Anomaly Detection** via **Autoencoders**.

* **Logic:** The model attempts to reconstruct the input telemetry. 
* **Detection:** If the input is "strange" (out of distribution), the model fails to reconstruct it accurately.
* **Decision Rule:**
    - If `reconstruction_error > anomaly_threshold` → **Anomaly**
    - Else → **Normal Behavior**

---

## 🛠️ Technology Stack

| Layer | Technologies |
| :--- | :--- |
| **Backend** | Java 17, Spring Boot, Spring WebFlux, Apache Kafka |
| **Machine Learning** | Python, Scikit-Learn/TensorFlow, REST API |
| **Streaming** | Kafka, WebSockets |
| **Infrastructure** | Docker, Docker Compose |
| **Frontend** | HTML5, Vanilla JavaScript |

---

## 👤 Author

* **Martin Imoberdorf** - [GitHub](https://www.github.com/MartinImoberdorf) - *Backend Engineer · Java · Distributed Systems*
