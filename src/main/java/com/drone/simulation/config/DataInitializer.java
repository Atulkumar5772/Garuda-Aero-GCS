package com.drone.simulation.config;

import com.drone.simulation.model.*;
import com.drone.simulation.repository.DroneRepository;
import com.drone.simulation.repository.FlightAlertRepository;
import com.drone.simulation.repository.MissionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private final DroneRepository droneRepository;
    private final MissionRepository missionRepository;
    private final FlightAlertRepository alertRepository;

    public DataInitializer(DroneRepository droneRepository,
                           MissionRepository missionRepository,
                           FlightAlertRepository alertRepository) {
        this.droneRepository = droneRepository;
        this.missionRepository = missionRepository;
        this.alertRepository = alertRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (droneRepository.count() == 0) {
            System.out.println(">>> Initializing Jaipur Smart City Drone & Mission in MySQL...");

            // 1 Clean Primary Drone: Garuda-X4 Jaipur Surveillance Base
            Drone drone1 = new Drone("DRONE-JAIPUR-01", "Garuda-X4 Smart Patrol", 18.0, 120.0, 26.8480, 75.8120);
            droneRepository.save(drone1);

            // 1 Clean Mission: Jaipur Airport Outer Corridor (Permitted Green Zone)
            Mission jaipurMission = new Mission("Jaipur Airport Outer Corridor Survey", drone1);
            jaipurMission.setStatus(MissionStatus.DRAFT);
            jaipurMission.setCreatedAt(LocalDateTime.now());

            // 5 Coordinates safely in the Green Zone (North of Airport Red Zone)
            jaipurMission.addWaypoint(new Waypoint(0, 26.8480, 75.8120, 25.0, 12.0)); // WP 1: Launch Pad - Jawahar Circle North
            jaipurMission.addWaypoint(new Waypoint(1, 26.8580, 75.8050, 45.0, 15.0)); // WP 2: Gaurav Tower / WTP Corridor
            jaipurMission.addWaypoint(new Waypoint(2, 26.8580, 75.8200, 50.0, 14.0)); // WP 3: Malviya Nagar Sector 3 IT Hub
            jaipurMission.addWaypoint(new Waypoint(3, 26.8480, 75.8200, 35.0, 12.0)); // WP 4: Apex Circle Safe Patrol
            jaipurMission.addWaypoint(new Waypoint(4, 26.8480, 75.8120, 10.0, 8.0));  // WP 5: Return to Launch Pad

            jaipurMission.setTotalDistanceMeters(4600.0);
            missionRepository.save(jaipurMission);

            // Initial informational alert (DGCA Jaipur Airspace Compliance)
            FlightAlert initAlert = new FlightAlert(
                    drone1.getId(),
                    jaipurMission.getId(),
                    AlertType.GEOFENCE_BREACH,
                    AlertSeverity.INFO,
                    "Jaipur Airspace Active. Safe buffer maintained outside Jaipur Airport Red Zone."
            );
            alertRepository.save(initAlert);

            System.out.println(">>> Jaipur Drone & Mission successfully initialized in MySQL!");
        }
    }
}