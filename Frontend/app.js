function isMobileDevice() {
  return /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent) ||
         (window.matchMedia && window.matchMedia("(max-width: 768px)").matches) ||
         ('ontouchstart' in window || navigator.maxTouchPoints > 0);
}

if (isMobileDevice()) {
  document.body.innerHTML = `
    <div class="min-h-screen bg-slate-900 flex items-center justify-center px-4">
      <div class="bg-slate-800 rounded-xl shadow-2xl border border-red-700 p-8 max-w-md text-center">
        <div class="mb-4">
          <svg class="mx-auto h-16 w-16 text-red-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
          </svg>
        </div>
        <h1 class="text-2xl font-bold text-red-400 mb-4">Dispositivo No Soportado</h1>
        <p class="text-gray-300 mb-2">
          Esta aplicación solo está disponible para dispositivos de escritorio (PC).
        </p>
        <p class="text-gray-400 text-sm">
          Por favor, accede desde una computadora para usar esta aplicación.
        </p>
      </div>
    </div>
  `;
} else {
  const wsUrl = "ws://localhost:8082/ws/telemetry";
  const statusEl = document.getElementById("status");
  const tableBody = document.getElementById("telemetry-body");
  const anomalyBody = document.getElementById("anomaly-body");
  const normalCountEl = document.getElementById("normal-count");
  const anomalyCountEl = document.getElementById("anomaly-count");
  const vehicleDetailsEl = document.getElementById("vehicle-details");
  const vehicleDetailsContentEl = document.getElementById("vehicle-details-content");
  const selectedVehicleIdEl = document.getElementById("selected-vehicle-id");
  const maxRows = 100;
  let messageCount = 0;
  let ws = null;
  let reconnectInterval = 5000;
  // Almacenar los datos completos de cada vehículo por su ID
  const vehicleDataMap = new Map(); 

  function formatValue(value, isNumber = false, decimals = 1, suffix = '') {
    if (value === null || value === undefined) return '-';
    if (isNumber && typeof value === 'number') {
      return value.toFixed(decimals) + suffix;
    }
    return String(value);
  }


  function updateStatus(message, type = 'info') {
    statusEl.textContent = message;
    statusEl.className = 'px-4 py-2 rounded-lg font-semibold border transition-colors duration-300 ';
    switch(type) {
      case 'connected':
        statusEl.className += 'bg-green-900/30 border-green-700 text-green-400';
        break;
      case 'disconnected':
        statusEl.className += 'bg-red-900/30 border-red-700 text-red-400';
        break;
      case 'warning':
        statusEl.className += 'bg-yellow-900/30 border-yellow-700 text-yellow-400';
        break;
      case 'error':
        statusEl.className += 'bg-orange-900/30 border-orange-700 text-orange-400';
        break;
      default:
        statusEl.className += 'bg-slate-800 border-slate-700 text-gray-300';
    }
  }

  function showVehicleDetails(vehicleId) {
    const vehicleData = vehicleDataMap.get(vehicleId);
    if (vehicleData) {
      vehicleDetailsEl.classList.remove('hidden');
      selectedVehicleIdEl.textContent = `Vehicle ID: ${vehicleId}`;
      vehicleDetailsContentEl.textContent = JSON.stringify(vehicleData, null, 2);
      // Scroll to details section
      vehicleDetailsEl.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
    }
  }

  function connectWebSocket() {
    ws = new WebSocket(wsUrl);

    ws.onopen = () => {
      console.log("✅ WebSocket reconectado");
      updateStatus("Reconectado al WebSocket", 'connected');
      
      // Check after 5 seconds if we're receiving messages
      setTimeout(() => {
        if (messageCount === 0) {
          console.warn("⚠️ No messages received after 5 seconds");
          updateStatus("Conectado pero sin datos aún. Verificando servidor...", 'warning');
        }
      }, 5000);
    };

    ws.onclose = () => {
      console.warn("❌ WebSocket desconectado. Intentando reconectar...");
      updateStatus("Desconectado. Intentando reconectar...", 'disconnected');
      setTimeout(connectWebSocket, reconnectInterval);
    };

    ws.onerror = (error) => {
      console.error("⚠️ Error en WebSocket:", error);
      updateStatus("⚠️ Error en WebSocket. Ver consola.", 'error');
    };

    ws.onmessage = (event) => {
      messageCount++;
      console.log(`Message #${messageCount} received:`, event.data);
      
      try {
        const data = JSON.parse(event.data);
        console.log("✅ Parsed JSON data:", data);
        
        // Normalize field names (handle both camelCase and snake_case)
        // Always show data, even if null/undefined
        const vehicleId = data.vehicleId ?? data.vehicle_id ?? data.id ?? data.vehicle ?? '-';
        const speed = data.speed ?? data.speed_kmh ?? data.speed_km ?? null;
        const acceleration = data.acceleration ?? data.accel ?? null;
        const battery = data.battery ?? data.battery_level ?? data.batteryLevel ?? null;
        const temperature = data.temperature ?? data.temp ?? data.temp_c ?? data.tempC ?? null;
        const trafficLevel = data.trafficLevel ?? data.traffic_level ?? data.traffic ?? null;
        const anomaly = data.anomaly ?? false;
        const anomalyType = data.anomalyType ?? data.anomaly_type ?? null;

        // Guardar los datos completos del vehículo
        if (vehicleId && vehicleId !== '-') {
          vehicleDataMap.set(vehicleId, data);
        }

        const row = document.createElement("tr");
        const targetBody = anomaly ? anomalyBody : tableBody;
        
        // Apply Tailwind classes based on anomaly status and make it clickable
        if (anomaly) {
          row.className = "bg-red-900/20 hover:bg-red-900/30 border-l-4 border-red-500 transition-colors cursor-pointer";
        } else {
          row.className = "bg-slate-800/50 hover:bg-slate-700/50 border-l-4 border-green-500 transition-colors cursor-pointer";
        }

        // Agregar evento click para mostrar detalles
        row.addEventListener('click', () => {
          if (vehicleId && vehicleId !== '-') {
            showVehicleDetails(vehicleId);
          }
        });

        row.innerHTML = `
          <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-200">${formatValue(vehicleId)}</td>
          <td class="px-6 py-4 whitespace-nowrap text-sm text-blue-400">${formatValue(speed, true, 1)}</td>
          <td class="px-6 py-4 whitespace-nowrap text-sm text-purple-400">${formatValue(acceleration, true, 2)}</td>
          <td class="px-6 py-4 whitespace-nowrap text-sm text-yellow-400">${formatValue(battery, true, 1, '%')}</td>
          <td class="px-6 py-4 whitespace-nowrap text-sm text-orange-400">${formatValue(temperature, true, 1, '°C')}</td>
          <td class="px-6 py-4 whitespace-nowrap text-sm text-cyan-400">${formatValue(trafficLevel)}</td>
          <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-300">${formatValue(anomalyType)}</td>
        `;

        // Add the new row at the top of the appropriate table
        targetBody.prepend(row);
        console.log(`✅ Row added to ${anomaly ? 'anomaly' : 'normal'} table. Total rows:`, targetBody.rows.length);

        // Limit the number of rows in each table
        if (targetBody.rows.length > maxRows) {
          targetBody.deleteRow(targetBody.rows.length - 1);
        }
        
        // Update counters
        normalCountEl.textContent = `${tableBody.rows.length} registro(s)`;
        anomalyCountEl.textContent = `${anomalyBody.rows.length} registro(s)`;
        
        updateStatus(`Conectado `, 'connected');
      } catch (err) {
        console.error("Error processing WebSocket message:", err);
        console.error("Raw message:", event.data);
        // Even if JSON parsing fails, try to show the raw data in the normal table
        const row = document.createElement("tr");
        row.className = "bg-orange-900/20 border-l-4 border-orange-500";
        row.innerHTML = `
          <td colspan="7" class="px-6 py-4 text-sm text-orange-400">
            ⚠️ Error parsing: ${String(event.data).substring(0, 100)}
          </td>
        `;
        tableBody.prepend(row);
        normalCountEl.textContent = `${tableBody.rows.length} registro(s)`;
        updateStatus(`Error procesando mensaje: ${err.message}`, 'error');
      }
    };
  }

  // Llamar a la función de conexión inicial
  connectWebSocket();
}

