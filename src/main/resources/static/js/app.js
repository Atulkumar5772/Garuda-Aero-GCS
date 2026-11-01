/**
 * GARUDA AERO GCS - Ground Control Station Engine
 * Student: ATUL KUMAR (PIET24CS082)
 * Autonomous Drone Mission Planning & Airspace Safety Platform
 */

let map;
let baseLayers = {};
let currentLayerName = 'satellite';

let droneMarkers = {};
let flightPathPolyline = null;
let flightTrailPolyline = null;
let flightTrailCoords = [];
let plannerPolyline = null;
let waypointMarkers = [];
let plannedWaypoints = [];
let plannerMarkers = [];
let geofenceCircle = null;
let airportNoFlyZone = null;

let isPlanningMode = false;
let selectedDroneId = null;
let activeMissionId = null;
let filterBySelectedDrone = false;
let cachedDrones = [];
let cachedMissions = [];

// Jaipur International Airport (JAI) Center & Red Zone Constants
const JAIPUR_AIRPORT_LAT = 26.8286;
const JAIPUR_AIRPORT_LNG = 75.8056;
const AIRPORT_RED_ZONE_RADIUS = 1500; // meters

// Initialize Dashboard
document.addEventListener("DOMContentLoaded", () => {
    initMap();
    loadDashboardStats();
    loadDrones();
    loadMissions();
    loadAlerts();

    // 1-Second Real-Time Telemetry & Alerts Poll
    setInterval(pollTelemetryAndAlerts, 1000);
});

/* ===================================================================
   1. MAP INITIALIZATION & FREE HIGH-RES TILE LAYERS
   =================================================================== */
function initMap() {
    map = L.map('map', {
        zoomControl: true,
        attributionControl: false
    }).setView([26.8500, 75.8120], 14);

    // 1. Esri World Imagery (Satellite) - 100% FREE
    baseLayers['satellite'] = L.tileLayer('https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}', {
        maxZoom: 19
    });

    // 2. OpenStreetMap Standard (Streets) - 100% FREE
    baseLayers['streets'] = L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19
    });

    // 3. Carto Dark Matter (Dark HUD) - 100% FREE
    baseLayers['dark'] = L.tileLayer('https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png', {
        maxZoom: 19
    });

    baseLayers['satellite'].addTo(map);

    // 🔴 Jaipur Airport Red No-Fly Zone (DGCA 1500m Restricted Airspace)
    airportNoFlyZone = L.circle([JAIPUR_AIRPORT_LAT, JAIPUR_AIRPORT_LNG], {
        radius: AIRPORT_RED_ZONE_RADIUS,
        color: '#ff3366',
        weight: 2.5,
        dashArray: '6, 6',
        fillColor: '#ff3366',
        fillOpacity: 0.22
    }).bindPopup("<b>🚫 JAIPUR AIRPORT RED NO-FLY ZONE</b><br>DGCA Restricted Airspace (1500m Runway Buffer). Drone entry strictly prohibited!").addTo(map);

    // 🔵 Permitted Green Corridor Geofence (6000m radius)
    geofenceCircle = L.circle([26.8480, 75.8120], {
        radius: 6000,
        color: '#00d2ff',
        weight: 1.5,
        dashArray: '8, 8',
        fillColor: '#00d2ff',
        fillOpacity: 0.02
    }).addTo(map);

    // Click handler for mission planner
    map.on('click', (e) => {
        if (!isPlanningMode) return;
        addPlannedWaypoint(e.latlng.lat, e.latlng.lng);
    });
}

function switchMapLayer(layerName) {
    if (baseLayers[currentLayerName]) {
        map.removeLayer(baseLayers[currentLayerName]);
    }
    baseLayers[layerName].addTo(map);
    currentLayerName = layerName;

    document.querySelectorAll('.btn-layer').forEach(b => b.classList.remove('active'));
    event.currentTarget.classList.add('active');
}

/* ===================================================================
   2. HIGH-TECH 3D QUADCOPTER SVG MARKER WITH SPINNING ROTORS
   =================================================================== */
function getDroneSvgIcon(heading = 0, status = 'IDLE') {
    const isAirborne = (status === 'IN_FLIGHT');
    const isRTH = (status === 'RETURNING_TO_HOME');
    const isCharging = (status === 'CHARGING');
    
    let glowColor = '#00d2ff';
    let beaconColor = '#00e676';
    if (isAirborne) { glowColor = '#00e676'; beaconColor = '#00e676'; }
    if (isRTH) { glowColor = '#ff6d00'; beaconColor = '#ffb300'; }
    if (isCharging) { glowColor = '#00d2ff'; beaconColor = '#ffb300'; }

    const rotorAnimClass = (isAirborne || isRTH) ? 'spinning-prop' : '';

    const svg = `
        <div class="drone-marker-wrapper" style="transform: rotate(${heading}deg);">
            <div class="radar-cone"></div>
            <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 100 100" class="drone-svg">
                <defs>
                    <radialGradient id="rotorGlow" cx="50%" cy="50%" r="50%">
                        <stop offset="0%" stop-color="${glowColor}" stop-opacity="0.8"/>
                        <stop offset="100%" stop-color="${glowColor}" stop-opacity="0.1"/>
                    </radialGradient>
                    <filter id="neon" x="-20%" y="-20%" width="140%" height="140%">
                        <feDropShadow dx="0" dy="0" stdDeviation="3" flood-color="${glowColor}"/>
                    </filter>
                </defs>

                <!-- Carbon Fiber Cross Arms -->
                <line x1="22" y1="22" x2="78" y2="78" stroke="#334155" stroke-width="6" stroke-linecap="round"/>
                <line x1="78" y1="22" x2="22" y2="78" stroke="#334155" stroke-width="6" stroke-linecap="round"/>
                <line x1="22" y1="22" x2="78" y2="78" stroke="#64748b" stroke-width="2" stroke-linecap="round"/>
                <line x1="78" y1="22" x2="22" y2="78" stroke="#64748b" stroke-width="2" stroke-linecap="round"/>

                <!-- 4 Rotor Discs with Spinning Animation -->
                <g class="rotor top-left ${rotorAnimClass}" transform-origin="22 22">
                    <circle cx="22" cy="22" r="14" fill="url(#rotorGlow)" stroke="${glowColor}" stroke-width="1.5" opacity="0.85"/>
                    <line x1="10" y1="22" x2="34" y2="22" stroke="#fff" stroke-width="2"/>
                </g>
                <g class="rotor top-right ${rotorAnimClass}" transform-origin="78 22">
                    <circle cx="78" cy="22" r="14" fill="url(#rotorGlow)" stroke="${glowColor}" stroke-width="1.5" opacity="0.85"/>
                    <line x1="66" y1="22" x2="90" y2="22" stroke="#fff" stroke-width="2"/>
                </g>
                <g class="rotor bottom-left ${rotorAnimClass}" transform-origin="22 78">
                    <circle cx="22" cy="78" r="14" fill="url(#rotorGlow)" stroke="${glowColor}" stroke-width="1.5" opacity="0.85"/>
                    <line x1="10" y1="78" x2="34" y2="78" stroke="#fff" stroke-width="2"/>
                </g>
                <g class="rotor bottom-right ${rotorAnimClass}" transform-origin="78 78">
                    <circle cx="78" cy="78" r="14" fill="url(#rotorGlow)" stroke="${glowColor}" stroke-width="1.5" opacity="0.85"/>
                    <line x1="66" y1="78" x2="90" y2="78" stroke="#fff" stroke-width="2"/>
                </g>

                <!-- Fuselage Main Body -->
                <path d="M50 18 L66 38 L60 80 L40 80 L34 38 Z" fill="#0f172a" stroke="${glowColor}" stroke-width="2.5" filter="url(#neon)"/>

                <!-- Directional Forward Heading Nose Cone (Yellow/Cyan) -->
                <polygon points="50,14 56,26 44,26" fill="#00d2ff"/>

                <!-- Center Pulsing LED Beacon -->
                <circle cx="50" cy="50" r="6" fill="${beaconColor}" class="pulse-beacon"/>
                <circle cx="50" cy="50" r="2.5" fill="#fff"/>
            </svg>
        </div>
    `;
    return L.divIcon({
        html: svg,
        className: 'custom-drone-container',
        iconSize: [48, 48],
        iconAnchor: [24, 24]
    });
}

function getWaypointIcon(number) {
    return L.divIcon({
        html: `<div class="waypoint-badge">${number}</div>`,
        className: 'custom-wp-container',
        iconSize: [26, 26],
        iconAnchor: [13, 13]
    });
}

/* ===================================================================
   3. FLEET & MISSION LOADING
   =================================================================== */
async function loadDrones() {
    try {
        const res = await fetch('/api/drones');
        cachedDrones = await res.json();
        const container = document.getElementById('drone-list-container');
        const selectBox = document.getElementById('select-mission-drone');
        container.innerHTML = '';
        selectBox.innerHTML = '';

        document.getElementById('badge-drone-count').textContent = cachedDrones.length;
        document.getElementById('stat-total-drones').textContent = cachedDrones.length;

        cachedDrones.forEach((d, idx) => {
            if (idx === 0 && !selectedDroneId) {
                selectDrone(d.id);
            }

            updateDroneMarker(d);

            const isCharging = (d.status === 'CHARGING');
            const isFlying = (d.status === 'IN_FLIGHT' || d.status === 'RETURNING_TO_HOME');
            let rechargeBtnHtml = '';
            if (isCharging) {
                rechargeBtnHtml = `<button class="btn-recharge" disabled title="Dock charging in progress (+5%/s)"><i class="fa-solid fa-bolt-lightning text-warning"></i> Charging...</button>`;
            } else if (isFlying) {
                rechargeBtnHtml = `<button class="btn-recharge" disabled title="Cannot recharge in mid-air"><i class="fa-solid fa-ban"></i> Airborne</button>`;
            } else if (d.batteryPercentage >= 100) {
                rechargeBtnHtml = `<button class="btn-recharge" disabled title="Battery fully charged (100%)"><i class="fa-solid fa-check"></i> Full</button>`;
            } else {
                rechargeBtnHtml = `<button class="btn-recharge" onclick="event.stopPropagation(); rechargeDrone(${d.id})" title="Initiate Dock Fast Recharge"><i class="fa-solid fa-bolt"></i> Recharge</button>`;
            }

            const card = document.createElement('div');
            card.className = `item-card ${selectedDroneId === d.id ? 'active' : ''}`;
            card.onclick = () => selectDrone(d.id);
            card.innerHTML = `
                <div class="card-top">
                    <span class="card-title">${d.modelName}</span>
                    <span class="status-badge ${getStatusBadgeClass(d.status)}">${d.status}</span>
                </div>
                <div class="card-meta">
                    <span><i class="fa-solid fa-fingerprint"></i> ${d.serialNumber}</span>
                    <span><i class="fa-solid fa-battery-half"></i> ${Math.round(d.batteryPercentage)}%</span>
                    ${rechargeBtnHtml}
                </div>
            `;
            container.appendChild(card);

            const opt = document.createElement('option');
            opt.value = d.id;
            const statusLabel = (d.status === 'IN_FLIGHT') ? ' [AIRBORNE - Queue]' : (d.status === 'CHARGING' ? ' [CHARGING]' : '');
            opt.textContent = `${d.modelName} (${d.serialNumber})${statusLabel}`;
            selectBox.appendChild(opt);
        });
    } catch (err) {
        console.error("Error loading drones:", err);
    }
}

function toggleMissionFilter() {
    filterBySelectedDrone = !filterBySelectedDrone;
    const filterBtn = document.getElementById('btn-filter-drone-missions');
    const filterLabel = document.getElementById('filter-drone-label');
    if (filterBySelectedDrone && selectedDroneId) {
        const d = cachedDrones.find(x => x.id === selectedDroneId);
        filterLabel.textContent = d ? d.modelName.split(' ')[0] : 'Drone';
        filterBtn.classList.add('active');
    } else {
        filterBySelectedDrone = false;
        filterLabel.textContent = 'All';
        filterBtn.classList.remove('active');
    }
    renderMissionList();
}

async function loadMissions() {
    try {
        const res = await fetch('/api/missions');
        cachedMissions = await res.json();
        renderMissionList();
    } catch (err) {
        console.error("Error loading missions:", err);
    }
}

function renderMissionList() {
    const container = document.getElementById('mission-list-container');
    container.innerHTML = '';

    let displayedMissions = cachedMissions;
    if (filterBySelectedDrone && selectedDroneId) {
        displayedMissions = cachedMissions.filter(m => m.drone && m.drone.id === selectedDroneId);
    }

    document.getElementById('badge-mission-count').textContent = displayedMissions.length;
    document.getElementById('stat-missions').textContent = cachedMissions.length;

    if (displayedMissions.length === 0) {
        container.innerHTML = `<div class="empty-hint">${filterBySelectedDrone ? 'No missions planned for selected drone.' : 'No flight missions planned yet.'}</div>`;
        return;
    }

    // Auto-select first displayed mission if current selection is invalid
    if (!activeMissionId || !cachedMissions.some(m => m.id === activeMissionId)) {
        selectMission(displayedMissions[0].id);
        return;
    }

    displayedMissions.forEach((m) => {
        const assignedDroneName = m.drone ? m.drone.modelName : 'Fleet Drone';
        const card = document.createElement('div');
        card.className = `item-card ${activeMissionId === m.id ? 'active' : ''}`;
        card.onclick = () => selectMission(m.id);
        card.innerHTML = `
            <div class="card-top">
                <span class="card-title">${m.name}</span>
                <div style="display:flex; align-items:center; gap:0.4rem;">
                    <span class="status-badge ${getStatusBadgeClass(m.status)}">${m.status}</span>
                    <button class="btn-delete-mission" onclick="event.stopPropagation(); deleteMission(${m.id})" title="Delete Mission"><i class="fa-solid fa-trash-can"></i></button>
                </div>
            </div>
            <div class="card-meta" style="margin-top: 0.25rem;">
                <span class="mission-drone-tag"><i class="fa-solid fa-drone"></i> ${assignedDroneName}</span>
                <span><i class="fa-solid fa-route"></i> ${m.waypoints ? m.waypoints.length : 0} WP</span>
                <span><i class="fa-solid fa-ruler"></i> ${m.totalDistanceMeters ? Math.round(m.totalDistanceMeters) + 'm' : '0m'}</span>
            </div>
        `;
        container.appendChild(card);
    });
}

function selectDrone(droneId) {
    selectedDroneId = droneId;
    loadDrones();
    if (filterBySelectedDrone) {
        const d = cachedDrones.find(x => x.id === selectedDroneId);
        document.getElementById('filter-drone-label').textContent = d ? d.modelName.split(' ')[0] : 'Drone';
    }
    renderMissionList();
    if (droneMarkers[droneId]) {
        map.panTo(droneMarkers[droneId].getLatLng());
    }
}

function selectMission(missionId) {
    activeMissionId = missionId;
    const mission = cachedMissions.find(m => m.id === missionId);
    if (!mission) return;

    document.getElementById('active-mission-title').textContent = mission.name;
    updateMissionButtons(mission.status);

    drawMissionPath(mission);
    renderWaypointTable(mission.waypoints);
    renderMissionList();
}

function renderWaypointTable(waypoints) {
    const container = document.getElementById('waypoint-table-container');
    if (!waypoints || waypoints.length === 0) {
        container.innerHTML = '<div class="empty-hint">No waypoints in this mission.</div>';
        return;
    }

    container.innerHTML = '';
    waypoints.forEach((wp, idx) => {
        const row = document.createElement('div');
        row.className = 'wp-row';
        row.innerHTML = `
            <span><strong>WP #${idx + 1}</strong> (${wp.latitude.toFixed(4)}, ${wp.longitude.toFixed(4)})</span>
            <span>Alt: ${wp.targetAltitudeMeters}m | Speed: ${wp.speedMps}m/s</span>
        `;
        container.appendChild(row);
    });
}

function drawMissionPath(mission) {
    waypointMarkers.forEach(m => map.removeLayer(m));
    waypointMarkers = [];

    if (flightPathPolyline) map.removeLayer(flightPathPolyline);
    if (flightTrailPolyline) map.removeLayer(flightTrailPolyline);
    flightTrailCoords = [];

    if (!mission.waypoints || mission.waypoints.length === 0) return;

    const latLngs = [];
    mission.waypoints.forEach((wp, idx) => {
        const pos = [wp.latitude, wp.longitude];
        latLngs.push(pos);

        const marker = L.marker(pos, { icon: getWaypointIcon(idx + 1) })
            .bindPopup(`<b>Waypoint #${idx + 1}</b><br>Altitude: ${wp.targetAltitudeMeters}m<br>Target Speed: ${wp.speedMps}m/s`)
            .addTo(map);
        waypointMarkers.push(marker);
    });

    flightPathPolyline = L.polyline(latLngs, {
        color: '#00d2ff',
        weight: 3.5,
        dashArray: '6, 6',
        opacity: 0.9
    }).addTo(map);

    map.fitBounds(flightPathPolyline.getBounds(), { padding: [60, 60] });
}

function updateDroneMarker(drone) {
    if (!drone.currentLatitude || !drone.currentLongitude) return;

    const pos = [drone.currentLatitude, drone.currentLongitude];

    if (!droneMarkers[drone.id]) {
        droneMarkers[drone.id] = L.marker(pos, {
            icon: getDroneSvgIcon(drone.currentHeading, drone.status)
        }).addTo(map);
    } else {
        droneMarkers[drone.id].setLatLng(pos);
        droneMarkers[drone.id].setIcon(getDroneSvgIcon(drone.currentHeading, drone.status));
    }
}

async function rechargeDrone(droneId) {
    try {
        const res = await fetch(`/api/drones/${droneId}/recharge`, { method: 'POST' });
        if (!res.ok) {
            const err = await res.json();
            alert("⚡ Charging Notice: " + (err.message || "Failed to initiate recharge"));
        }
    } catch (e) {
        console.error("Recharge failed:", e);
    }
    loadDrones();
}

/* ===================================================================
   4. REAL-TIME TELEMETRY POLL & TRAIL RENDERING
   =================================================================== */
async function pollTelemetryAndAlerts() {
    try {
        const res = await fetch('/api/telemetry/live');
        const telemetryList = await res.json();

        let airborneCount = 0;
        telemetryList.forEach(t => {
            const isAirborne = (t.status === 'IN_FLIGHT' || t.status === 'RETURNING_TO_HOME');
            if (isAirborne) {
                airborneCount++;
                
                // Add to particle flight trail
                if (selectedDroneId === t.droneId) {
                    flightTrailCoords.push([t.latitude, t.longitude]);
                    if (flightTrailCoords.length > 50) flightTrailCoords.shift();

                    if (!flightTrailPolyline) {
                        flightTrailPolyline = L.polyline(flightTrailCoords, {
                            color: '#00e676',
                            weight: 2,
                            opacity: 0.7
                        }).addTo(map);
                    } else {
                        flightTrailPolyline.setLatLngs(flightTrailCoords);
                    }
                }
            }

            if (droneMarkers[t.droneId]) {
                droneMarkers[t.droneId].setLatLng([t.latitude, t.longitude]);
                droneMarkers[t.droneId].setIcon(getDroneSvgIcon(t.heading, t.status));
            }

            if (selectedDroneId === t.droneId) {
                updateHudDisplay(t);
            }
        });

        document.getElementById('stat-active-drones').textContent = airborneCount;
        loadAlerts();
    } catch (e) {}
}

function updateHudDisplay(t) {
    document.getElementById('hud-drone-name').textContent = t.modelName || 'Drone';
    document.getElementById('hud-drone-serial').textContent = t.serialNumber || 'SN';

    const statusEl = document.getElementById('hud-drone-status');
    statusEl.textContent = t.status;
    statusEl.className = `status-badge ${getStatusBadgeClass(t.status)}`;

    const batt = Math.max(0, Math.min(100, Math.round(t.batteryPercentage || 0)));
    const battFill = document.getElementById('hud-battery-fill');
    battFill.style.width = `${batt}%`;
    document.getElementById('hud-battery-val').textContent = `${batt}%`;

    if (batt < 20) battFill.style.backgroundColor = '#ff3366';
    else if (batt < 45) battFill.style.backgroundColor = '#ffb300';
    else battFill.style.backgroundColor = '#00e676';

    document.getElementById('hud-alt-val').innerHTML = `${(t.altitude || 0).toFixed(1)} <small>m</small>`;
    document.getElementById('hud-speed-val').innerHTML = `${(t.speed || 0).toFixed(1)} <small>m/s</small>`;
    document.getElementById('hud-heading-val').textContent = `${Math.round(t.heading || 0)}°`;
    document.getElementById('hud-attitude-val').textContent = `${(t.pitch || 0).toFixed(0)}° / ${(t.roll || 0).toFixed(0)}°`;

    if (t.activeMissionId) {
        const prog = Math.round(t.missionProgress || 0);
        document.getElementById('mission-progress-bar').style.width = `${prog}%`;
        document.getElementById('mission-progress-text').textContent = `${prog}%`;
    } else {
        document.getElementById('mission-progress-bar').style.width = '0%';
        document.getElementById('mission-progress-text').textContent = '0%';
    }
}

/* ===================================================================
   5. MISSION CONTROLS
   =================================================================== */
function updateMissionButtons(status) {
    const btnStart = document.getElementById('btn-start-mission');
    const btnPause = document.getElementById('btn-pause-mission');
    const btnResume = document.getElementById('btn-resume-mission');
    const btnRth = document.getElementById('btn-rth-mission');
    const btnAbort = document.getElementById('btn-abort-mission');

    btnStart.disabled = (status === 'ACTIVE' || status === 'COMPLETED');
    btnPause.disabled = (status !== 'ACTIVE');
    btnResume.disabled = (status !== 'PAUSED');
    btnRth.disabled = (status !== 'ACTIVE' && status !== 'PAUSED');
    btnAbort.disabled = (status !== 'ACTIVE' && status !== 'PAUSED');
}

async function startSelectedMission() {
    if (!activeMissionId) return;
    try {
        const res = await fetch(`/api/missions/${activeMissionId}/start`, { method: 'POST' });
        if (!res.ok) {
            const err = await res.json();
            alert("⚠️ Flight Launch Blocked:\n\n" + (err.message || "Failed to start mission."));
        }
    } catch (e) {
        console.error("Mission launch error:", e);
    }
    loadMissions();
    loadDrones();
}

async function deleteMission(missionId) {
    const mission = cachedMissions.find(m => m.id === missionId);
    const missionName = mission ? `"${mission.name}"` : 'this mission';
    if (!confirm(`Are you sure you want to delete ${missionName}?\nThis action cannot be undone.`)) {
        return;
    }
    try {
        const res = await fetch(`/api/missions/${missionId}`, { method: 'DELETE' });
        if (res.ok) {
            if (activeMissionId === missionId) {
                activeMissionId = null;
                waypointMarkers.forEach(m => map.removeLayer(m));
                waypointMarkers = [];
                if (flightPathPolyline) map.removeLayer(flightPathPolyline);
                if (flightTrailPolyline) map.removeLayer(flightTrailPolyline);
                document.getElementById('active-mission-title').textContent = 'No Mission Selected';
                document.getElementById('waypoint-table-container').innerHTML = '<div class="empty-hint">Select a mission to view route waypoints.</div>';
                updateMissionButtons('IDLE');
            }
            await loadMissions();
            await loadDashboardStats();
        } else {
            const err = await res.json();
            alert("Delete Failed: " + (err.message || "Could not delete mission"));
        }
    } catch (e) {
        console.error("Error deleting mission:", e);
    }
}

async function pauseMission() {
    if (!activeMissionId) return;
    await fetch(`/api/missions/${activeMissionId}/pause`, { method: 'POST' });
    loadMissions();
}

async function resumeMission() {
    if (!activeMissionId) return;
    await fetch(`/api/missions/${activeMissionId}/resume`, { method: 'POST' });
    loadMissions();
}

async function triggerRTH() {
    if (!activeMissionId) return;
    await fetch(`/api/missions/${activeMissionId}/rth`, { method: 'POST' });
    loadMissions();
}

async function abortMission() {
    if (!activeMissionId) return;
    await fetch(`/api/missions/${activeMissionId}/abort`, { method: 'POST' });
    loadMissions();
}

/* ===================================================================
   6. LEVEL 1: PRE-FLIGHT WAYPOINT PLANNER (HARD RED ZONE BLOCKING)
   =================================================================== */
function togglePlannerMode() {
    isPlanningMode = !isPlanningMode;
    const toolbar = document.getElementById('planner-toolbar');
    const btnToggle = document.getElementById('btn-plan-toggle');

    if (isPlanningMode) {
        toolbar.style.display = 'flex';
        btnToggle.innerHTML = '<i class="fa-solid fa-xmark"></i> Exit Planner';
        btnToggle.className = 'btn btn-danger';
        clearPlannedWaypoints();
    } else {
        toolbar.style.display = 'none';
        btnToggle.innerHTML = '<i class="fa-solid fa-draw-polygon"></i> Plan New Mission';
        btnToggle.className = 'btn btn-primary';
        clearPlannedWaypoints();
        // Restore active mission drawing if a mission was already selected/flying
        if (activeMissionId) {
            const currentMission = cachedMissions.find(m => m.id === activeMissionId);
            if (currentMission) drawMissionPath(currentMission);
        }
    }
}

function cancelPlanning() {
    togglePlannerMode();
}

function calculateHaversine(lat1, lon1, lat2, lon2) {
    const R = 6371000;
    const dLat = (lat2 - lat1) * Math.PI / 180;
    const dLon = (lon2 - lon1) * Math.PI / 180;
    const a = Math.sin(dLat/2) * Math.sin(dLat/2) +
              Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
              Math.sin(dLon/2) * Math.sin(dLon/2);
    return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
}

function addPlannedWaypoint(lat, lng) {
    // LEVEL 1 VALIDATION: Reject click if inside Airport Red Zone
    const distToAirport = calculateHaversine(JAIPUR_AIRPORT_LAT, JAIPUR_AIRPORT_LNG, lat, lng);
    if (distToAirport <= AIRPORT_RED_ZONE_RADIUS) {
        alert("🚫 RESTRICTED AIRSPACE VIOLATION!\n\nWaypoint cannot be placed inside Jaipur International Airport (JAI) Red Zone (< 1500m runway safety buffer).\n\nPlease click outside the Red Circle in the Green/Permitted zone.");
        return;
    }

    const wpNum = plannedWaypoints.length + 1;
    plannedWaypoints.push({ latitude: lat, longitude: lng });

    const marker = L.marker([lat, lng], { icon: getWaypointIcon(wpNum) }).addTo(map);
    plannerMarkers.push(marker);

    document.getElementById('plan-wp-count').textContent = plannedWaypoints.length;

    const latLngs = plannedWaypoints.map(w => [w.latitude, w.longitude]);
    if (plannerPolyline) map.removeLayer(plannerPolyline);
    plannerPolyline = L.polyline(latLngs, { color: '#00d2ff', weight: 3, dashArray: '5, 5' }).addTo(map);

    let dist = 0;
    for (let i = 1; i < latLngs.length; i++) {
        dist += map.distance(latLngs[i-1], latLngs[i]);
    }
    document.getElementById('plan-distance').textContent = `${Math.round(dist)} m`;
}

function clearPlannedWaypoints() {
    plannedWaypoints = [];
    plannerMarkers.forEach(m => map.removeLayer(m));
    plannerMarkers = [];
    if (plannerPolyline) map.removeLayer(plannerPolyline);
    document.getElementById('plan-wp-count').textContent = '0';
    document.getElementById('plan-distance').textContent = '0 m';
}

function openSaveMissionModal() {
    if (plannedWaypoints.length < 2) {
        alert("Please place at least 2 waypoints on the map first!");
        return;
    }
    document.getElementById('modal-save-mission').style.display = 'flex';
}

async function handleSaveMission(e) {
    e.preventDefault();
    const name = document.getElementById('input-mission-name').value;
    const droneId = parseInt(document.getElementById('select-mission-drone').value);
    const alt = parseFloat(document.getElementById('input-default-alt').value);
    const speed = parseFloat(document.getElementById('input-default-speed').value);

    const waypointsDto = plannedWaypoints.map((wp, idx) => ({
        sequenceOrder: idx,
        latitude: wp.latitude,
        longitude: wp.longitude,
        targetAltitudeMeters: alt,
        speedMps: speed
    }));

    const payload = {
        name: name,
        droneId: droneId,
        waypoints: waypointsDto
    };

    const res = await fetch('/api/missions', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
    });

    if (res.ok) {
        closeModal('modal-save-mission');
        togglePlannerMode();
        await loadMissions();
        await loadDashboardStats();
        alert("✅ Mission Plan Saved Successfully!\n\nStatus: DRAFT (Stored in MySQL)\n\nNote: This mission has NOT started. Your active drone will continue its current task. When you want to fly this mission, select it and click 'Launch Flight'.");
    } else {
        const err = await res.json();
        alert("Error saving mission: " + (err.message || "Failed"));
    }
}

/* ===================================================================
   7. DRONE REGISTRATION & ALERTS
   =================================================================== */
function openNewDroneModal() {
    document.getElementById('modal-drone').style.display = 'flex';
}

function closeModal(id) {
    document.getElementById(id).style.display = 'none';
}

async function handleCreateDrone(e) {
    e.preventDefault();
    const model = document.getElementById('input-drone-model').value;
    const serial = document.getElementById('input-drone-serial').value;
    const speed = parseFloat(document.getElementById('input-drone-speed').value);
    const alt = parseFloat(document.getElementById('input-drone-altitude').value);
    const lat = parseFloat(document.getElementById('input-drone-lat').value);
    const lng = parseFloat(document.getElementById('input-drone-lng').value);

    const payload = {
        modelName: model,
        serialNumber: serial,
        maxSpeedMps: speed,
        maxAltitudeMeters: alt,
        currentLatitude: lat,
        currentLongitude: lng
    };

    const res = await fetch('/api/drones', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
    });

    if (res.ok) {
        closeModal('modal-drone');
        await loadDrones();
        await loadDashboardStats();
        alert("New drone registered to fleet!");
    }
}

async function loadAlerts() {
    try {
        const res = await fetch('/api/alerts');
        const alerts = await res.json();
        const container = document.getElementById('alerts-feed-container');

        document.getElementById('stat-alerts').textContent = alerts.length;

        if (alerts.length === 0) {
            container.innerHTML = `
                <div class="alert-item alert-info">
                    <div class="alert-icon"><i class="fa-solid fa-circle-check"></i></div>
                    <div class="alert-content">
                        <div class="alert-title">Jaipur Airspace Nominal</div>
                        <div class="alert-msg">Dual-layer virtual wall & geofence monitoring active.</div>
                    </div>
                </div>
            `;
            return;
        }

        container.innerHTML = '';
        alerts.slice(0, 6).forEach(a => {
            const item = document.createElement('div');
            const sevClass = (a.severity === 'CRITICAL') ? 'alert-critical' : 'alert-warning';
            item.className = `alert-item ${sevClass}`;
            item.innerHTML = `
                <div class="alert-icon"><i class="fa-solid fa-shield-halved"></i></div>
                <div class="alert-content">
                    <div class="alert-title">${a.alertType} [${a.severity}]</div>
                    <div class="alert-msg">${a.message}</div>
                </div>
            `;
            container.appendChild(item);
        });
    } catch (e) {}
}

async function acknowledgeAllAlerts() {
    await fetch('/api/alerts/acknowledge-all', { method: 'POST' });
    loadAlerts();
}

async function loadDashboardStats() {
    try {
        const res = await fetch('/api/dashboard/stats');
        const stats = await res.json();
        document.getElementById('stat-total-drones').textContent = stats.totalDrones;
        document.getElementById('stat-active-drones').textContent = stats.activeDrones;
        document.getElementById('stat-missions').textContent = stats.totalMissions;
        document.getElementById('stat-alerts').textContent = stats.activeAlerts;
    } catch (e) {}
}

function getStatusBadgeClass(status) {
    switch (status) {
        case 'IN_FLIGHT': return 'badge-inflight';
        case 'RETURNING_TO_HOME': return 'badge-rth';
        case 'ACTIVE': return 'badge-active';
        case 'CHARGING': return 'badge-charging';
        default: return 'badge-idle';
    }
}