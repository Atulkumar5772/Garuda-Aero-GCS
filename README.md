<div align="center">

# 🚁 Garuda Aero GCS — Autonomous Drone Mission Planning & Airspace Safety Platform

[![Java 17+](https://img.shields.io/badge/Java-17%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Spring Boot 3](https://img.shields.io/badge/Spring_Boot-3.3.4-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white)](https://www.mysql.com/)
[![Leaflet.js](https://img.shields.io/badge/Leaflet-1.9.4-199900?style=for-the-badge&logo=leaflet&logoColor=white)](https://leafletjs.com/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=for-the-badge)](https://opensource.org/licenses/MIT)

<br><br>

<img src="assets/garuda-preview.svg" alt="Garuda Aero GCS Telemetry HUD Preview" width="100%" style="border-radius: 12px;"/>

<br>

*An enterprise-grade Ground Control Station (GCS) and real-time Digital Twin flight simulator featuring dynamic waypoint path generation, dual-layer active airspace geofencing (DGCA compliance), physics telemetry simulation, and an interactive 3D web dashboard.*

</div>

---

## 🌟 Key Highlights & Features

- 🗺️ **Interactive Waypoint Flight Planner:** Drag-and-drop / click-to-plot GPS flight paths directly on high-resolution satellite imagery (Esri World Imagery) and street cartography.
- 🛡️ **Dual-Layer Active Geofencing & Virtual Wall (DGCA Compliance):**
  - **Level 1 (Pre-Flight Validator):** Blocks waypoint placement inside restricted airport no-fly zones (*Jaipur International Airport - JAI 1500m buffer*).
  - **Level 2 (In-Flight Autonomous Virtual Wall):** Real-time collision avoidance that actively brakes the drone at restricted perimeters and engages automatic **Return-To-Home (RTH)** trajectory override.
- ⚡ **Real-Time Physics & Telemetry Engine (1000ms Ticker):** Continuous spherical Haversine distance interpolation, speed dynamics, dynamic climb/descent rates, true bearing calculation, synthetic 3D attitude (pitch/roll), and multi-variable battery drain modeling.
- 🛸 **Animated 3D Quadcopter Visualizer:** Custom SVG drone avatar featuring active spinning rotor discs, pulsing beacon LEDs, directional radar heading cones, and live particle flight trails.
- 📊 **Aerospace Ground Control HUD:** Dark-theme glassmorphism operations dashboard with digital airspeed gauges, altitude meters, heading compasses, and safety alert feeds.
- 💾 **Relational MySQL Persistence:** Full JPA/Hibernate data layer recording drone fleet states, compiled missions, waypoints, and time-series telemetry logs.

---

## 🏛️ System Architecture

<div align="center">
  <img src="assets/architecture.svg" alt="Garuda Aero GCS Distributed System Architecture" width="100%" style="border-radius: 12px;"/>
</div>

---

## 📁 Project Directory Structure

```
garuda-aero-gcs/
├── pom.xml                                      # Maven dependencies & build definitions
├── .gitignore                                   # Git exclusions (target/, .idea/)
├── README.md                                    # Project documentation
│
└── src/
    ├── main/
    │   ├── java/com/drone/simulation/
    │   │   ├── DroneSimulationApplication.java  # Spring Boot Main Entry Point
    │   │   │
    │   │   ├── config/                          # Configuration & Data Seeding
    │   │   │   ├── CorsConfig.java              # Cross-Origin Resource Sharing
    │   │   │   └── DataInitializer.java        # Initial fleet & mission bootstrapper
    │   │   │
    │   │   ├── controller/                      # REST API Endpoints
    │   │   │   ├── DroneController.java         # /api/drones
    │   │   │   ├── MissionController.java       # /api/missions
    │   │   │   ├── TelemetryController.java     # /api/telemetry
    │   │   │   ├── FlightAlertController.java   # /api/alerts
    │   │   │   └── DashboardController.java     # /api/dashboard
    │   │   │
    │   │   ├── model/                           # JPA Entities & Domain Models
    │   │   │   ├── Drone.java                   # Fleet registry & physical specs
    │   │   │   ├── Mission.java                 # Flight plans & lifecycle states
    │   │   │   ├── Waypoint.java                # Sequence GPS coordinates & altitudes
    │   │   │   ├── TelemetryLog.java            # Time-series blackbox flight logs
    │   │   │   ├── FlightAlert.java             # Safety rule violation incident logs
    │   │   │   └── DroneStatus.java             # Enums: IDLE, IN_FLIGHT, RTH, etc.
    │   │   │
    │   │   ├── repository/                      # Spring Data JPA Repositories
    │   │   │   ├── DroneRepository.java
    │   │   │   ├── MissionRepository.java
    │   │   │   ├── WaypointRepository.java
    │   │   │   ├── TelemetryLogRepository.java
    │   │   │   └── FlightAlertRepository.java
    │   │   │
    │   │   ├── service/                         # Core Business Logic & Simulation
    │   │   │   ├── TelemetrySimulatorService.java # 1-sec physics engine & coordinate math
    │   │   │   ├── AlertRuleEngineService.java    # Hard virtual wall & safety triggers
    │   │   │   ├── DroneService.java              # Fleet operations & battery recharge
    │   │   │   └── MissionService.java            # Mission planning & lifecycle management
    │   │   │
    │   │   └── dto/                             # Data Transfer Objects
    │   │       ├── MissionCreateRequest.java
    │   │       ├── WaypointDto.java
    │   │       ├── DroneTelemetryResponse.java
    │   │       └── DashboardStatsDto.java
    │   │
    │   └── resources/
    │       ├── application.properties           # MySQL database & server configurations
    │       └── static/                          # Web Ground Control Station Frontend
    │           ├── index.html                   # GCS Dashboard UI
    │           ├── css/style.css                # Modern aerospace dark styling
    │           └── js/app.js                    # Map engine, 3D drone visuals & polling
```

---

## 🛠️ Technology Stack

| Layer | Technologies |
| :--- | :--- |
| **Backend Core** | Java 17+ / Java 26, Spring Boot 3.3.4, Spring Web, Spring Data JPA |
| **ORM & Database** | Hibernate 6.5, MySQL 8.0 / MySQL Connector/J, HikariCP Connection Pool |
| **Simulation & Tasks** | Spring `@Scheduled` Task Execution Engine, Spherical Trigonometry (Haversine & Bearing) |
| **Frontend UI** | HTML5, CSS3 (Glassmorphism Dark HUD), JavaScript (ES6+), FontAwesome 6 |
| **Mapping Engine** | Leaflet.js 1.9.4, Esri World Imagery (Satellite), OpenStreetMap, CartoDB Dark |

---

## ⚡ Getting Started

### 1. Prerequisites
- **Java JDK 17** or higher
- **MySQL Community Server 8.0+**
- **IntelliJ IDEA** (Community or Ultimate) or any Java IDE

### 2. Database Setup
Open **MySQL Workbench** or your MySQL CLI and create the database:
```sql
CREATE DATABASE drone_simulation_db;
```

### 3. Application Configuration
Verify your database credentials in `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/drone_simulation_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD
```

### 4. Running the Project
1. Open the project folder in **IntelliJ IDEA**.
2. Run `src/main/java/com/drone/simulation/DroneSimulationApplication.java`.
3. Open your browser and navigate to:
   ```
   http://localhost:8080
   ```

---

## 📡 REST API Reference

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/drones` | Fetch all registered drones in the fleet |
| `POST` | `/api/drones` | Register a new drone with custom speed/altitude specs |
| `POST` | `/api/drones/{id}/recharge` | Recharge drone battery to 100% |
| `GET` | `/api/missions` | List all planned flight missions |
| `POST` | `/api/missions` | Validate & compile a new mission with waypoints |
| `POST` | `/api/missions/{id}/start` | Launch flight simulation for a mission |
| `POST` | `/api/missions/{id}/pause` | Pause active flight in mid-air (Hover) |
| `POST` | `/api/missions/{id}/resume` | Resume flight towards target waypoint |
| `POST` | `/api/missions/{id}/rth` | Trigger manual Return-To-Home (RTH) |
| `POST` | `/api/missions/{id}/abort` | Abort mission and disarm drone |
| `GET` | `/api/telemetry/live` | Stream real-time GPS, altitude, speed, attitude & battery data |
| `GET` | `/api/alerts` | Get real-time active safety & geofence violation alerts |
| `POST` | `/api/alerts/acknowledge-all` | Clear/acknowledge all active alerts |

---

## 📄 License
This project is licensed under the [MIT License](LICENSE).