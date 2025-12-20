from fastapi import FastAPI
from pydantic import BaseModel
import numpy as np
import pickle

# --------------------------
# Cargar modelo .pkl
# --------------------------
with open("model.pkl", "rb") as f:
    model = pickle.load(f)

app = FastAPI()

# Definir los datos que recibe el endpoint
class DataPoint(BaseModel):
    speed: float
    acceleration: float
    engine_temp: float
    fuel_consumption: float

# --------------------------
# Endpoint de predicción
# --------------------------
@app.post("/predict")
def predict(data: DataPoint):

    input_array = np.array([
        data.speed,
        data.acceleration,
        data.engine_temp,
        data.fuel_consumption
    ]).reshape(1, -1)

    pred = model.predict(input_array)[0]

    return {
        "input": data.model_dump(),
        "prediction": int(pred)
    }
