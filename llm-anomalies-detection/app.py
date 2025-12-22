from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
import numpy as np
import pickle
import os
from contextlib import asynccontextmanager
import traceback

app = FastAPI()

# --------------------------
# Componentes del modelo
# --------------------------
autoencoder_model = None
scaler = None
anomaly_threshold = None

@asynccontextmanager
async def lifespan(app: FastAPI):
    global autoencoder_model, scaler, anomaly_threshold
    model_path = os.getenv("MODEL_PATH", "model.pkl")

    try:
        with open(model_path, "rb") as f:
            loaded_data = pickle.load(f)
            autoencoder_model = loaded_data['autoencoder_model']
            scaler = loaded_data['scaler']
            anomaly_threshold = loaded_data['threshold']
        print("✅ Componentes del modelo cargados correctamente")
    except Exception as e:
        print(f"ERROR during lifespan startup - Error loading model components: {e}")
        traceback.print_exc()
        raise HTTPException(status_code=500, detail=f"Error al cargar los componentes del modelo: {e}")
    yield

app = FastAPI(lifespan=lifespan)

# --------------------------
# Esquema de entrada
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
        "model_loaded": autoencoder_model is not None,
        "scaler_loaded": scaler is not None,
        "threshold_loaded": anomaly_threshold is not None
    }

# --------------------------
# Predict
# --------------------------
@app.post("/predict")
def predict(data: DataPoint):

    if autoencoder_model is None or scaler is None or anomaly_threshold is None:
        print("ERROR: Model or components not loaded completely in predict function.")
        raise HTTPException(status_code=500, detail="Modelo o componentes no cargados completamente.")

    try:
        # Crear array numpy con el mismo orden de características que en el entrenamiento
        input_array = np.array([
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
        print(f"DEBUG: input_array shape: {input_array.shape}, dtype: {input_array.dtype}")

        # Escalar la entrada usando el scaler cargado
        scaled_input = scaler.transform(input_array)
        print(f"DEBUG: scaled_input shape: {scaled_input.shape}, dtype: {scaled_input.dtype}")

        # Reconstrucción con autoencoder (se pasa la entrada escalada)
        reconstructed_input = autoencoder_model.predict(scaled_input, verbose=0)
        print(f"DEBUG: reconstructed_input shape: {reconstructed_input.shape}, dtype: {reconstructed_input.dtype}")

        # Error de reconstrucción (MSE) entre la entrada ESCALADA y la salida RECONSTRUIDA ESCALADA
        reconstruction_error = float(
            np.mean(np.square(scaled_input - reconstructed_input))
        )
        print(f"DEBUG: reconstruction_error: {reconstruction_error}")

        # Regla de anomalía usando el umbral cargado
        is_anomaly = bool(reconstruction_error > anomaly_threshold) # Convertir a bool estándar de Python
        print(f"DEBUG: anomaly_threshold: {anomaly_threshold}, is_anomaly: {is_anomaly}")

        return {
            "input": {
                "latitude": data.latitude,
                "longitude": data.longitude,
                "speed": data.speed,
                "previousSpeed": data.previousSpeed,
                "acceleration": data.acceleration,
                "temperature": data.temperature,
                "battery": data.battery,
                "fuelLevel": data.fuelLevel,
                "speedLimit": data.speedLimit,
                "trafficLevel": data.trafficLevel
            },
            "reconstruction_error": reconstruction_error,
            "anomaly_threshold": anomaly_threshold,
            "is_anomaly": is_anomaly
        }
    except Exception as e:
        print(f"ERROR in predict endpoint processing: {e}")
        traceback.print_exc()
        raise HTTPException(status_code=500, detail=f"Error al procesar la predicción: {str(e)}")