from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
import numpy as np
import pickle
import os

app = FastAPI()

# --------------------------
# Modelo
# --------------------------
model = None

# Threshold calculado como percentil 95
ANOMALY_THRESHOLD = 0.8574

@app.on_event("startup")
def load_model():
    global model
    model_path = os.getenv("MODEL_PATH", "model.pkl")

    try:
        with open(model_path, "rb") as f:
            model = pickle.load(f)

        print("✅ Modelo cargado correctamente")

    except Exception as e:
        raise RuntimeError(f"Error cargando el modelo: {e}")

# --------------------------
# Input schema
# --------------------------
class DataPoint(BaseModel):
    latitude: float
    longitude: float
    speed: float
    previousSpeed: float
    acceleration: float
    temperature: float
    battery: float
    fuelLevel: float
    speedLimit: float
    trafficLevel: float

# --------------------------
# Healthcheck
# --------------------------
@app.get("/health")
def health():
    return {
        "status": "ok",
        "model_loaded": model is not None
    }

# --------------------------
# Predict
# --------------------------
@app.post("/predict")
def predict(data: DataPoint):

    if model is None:
        raise HTTPException(status_code=500, detail="Model not loaded")

    # Mismo orden que en el entrenamiento
    x = np.array([
        data.latitude,
        data.longitude,
        data.speed,
        data.previousSpeed,
        data.acceleration,
        data.temperature,
        data.battery,
        data.fuelLevel,
        data.speedLimit,
        data.trafficLevel
    ], dtype=np.float32).reshape(1, -1)

    # Reconstrucción con autoencoder
    x_hat = model.predict(x, verbose=0)

    # Error de reconstrucción (MSE)
    reconstruction_error = float(
        np.mean(np.square(x - x_hat))
    )

    # Regla EXACTA de anomalía
    is_anomaly = reconstruction_error > ANOMALY_THRESHOLD

    return {
        "reconstruction_error": reconstruction_error,
        "is_anomaly": is_anomaly
    }
