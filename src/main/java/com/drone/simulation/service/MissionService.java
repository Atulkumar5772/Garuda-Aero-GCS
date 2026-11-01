package com.drone.simulation.service;

import com.drone.simulation.dto.MissionCreateRequest;
import com.drone.simulation.dto.WaypointDto;
import com.drone.simulation.model.*;
import com.drone.simulation.repository.DroneRepository;
import com.drone.simulation.repository.MissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MissionService {

    private final MissionRepository missionRepository;
    private final DroneRepository droneRepository;

    public MissionService(MissionRepository missionRepository, DroneRepository droneRepository) {
        this.missionRepository = missionRepository;
        this.droneRepository = droneRepository;
    }

    public List<Mission> getAllMissions() {
        return missionRepository.findAll();
    }

    public Optional<Mission> getMissionById(Long id) {
        return missionRepository.findById(id);
    }

    @Transactional
    public Mission createMission(MissionCreateRequest request) {
        Drone drone = droneRepository.findById(request.getDroneId())
                .orElseThrow(() -> new RuntimeException("Drone not found with ID: " + request.getDroneId()));

        List<WaypointDto> wpDtos = request.getWaypoints();
        if (wpDtos == null || wpDtos.size() < 2) {
            throw new IllegalArgumentException("Mission must have at least 2 waypoints.");
        }

        // Level 1 Pre-Flight Safety Validation: Check for Airport Red Zone Violations
        for (int i = 0; i < wpDtos.size(); i++) {
            WaypointDto wp = wpDtos.get(i);
            if (AlertRuleEngineService.isInsideAirportRedZone(wp.getLatitude(), wp.getLongitude())) {
                throw new IllegalArgumentException("RESTRICTED AIRSPACE: Waypoint #" + (i + 1) +
                        " is inside Jaipur International Airport (JAI) Red Zone! DGCA Safety Violation.");
            }
        }

        Mission mission = new Mission(request.getName(), drone);
        mission.setStatus(MissionStatus.DRAFT);
        mission.setCreatedAt(LocalDateTime.now());
        mission.setCurrentWaypointIndex(0);

        double totalDist = 0.0;
        for (int i = 0; i < wpDtos.size(); i++) {
            WaypointDto dto = wpDtos.get(i);
            Waypoint wp = new Waypoint(
                    i,
                    dto.getLatitude(),
                    dto.getLongitude(),
                    dto.getTargetAltitudeMeters() != null ? dto.getTargetAltitudeMeters() : 30.0,
                    dto.getSpeedMps() != null ? dto.getSpeedMps() : 10.0
            );
            mission.addWaypoint(wp);

            if (i > 0) {
                WaypointDto prev = wpDtos.get(i - 1);
                totalDist += AlertRuleEngineService.calculateHaversineDistance(
                        prev.getLatitude(), prev.getLongitude(),
                        dto.getLatitude(), dto.getLongitude()
                );
            }
        }
        mission.setTotalDistanceMeters(Math.round(totalDist * 100.0) / 100.0);

        // Note: Do NOT alter the physical drone's current position/telemetry during planning.
        // Drone position will only be set to Waypoint 1 when startMission() is executed.
        return missionRepository.save(mission);
    }

    @Transactional
    public Mission startMission(Long missionId) {
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new RuntimeException("Mission not found with ID: " + missionId));

        Drone drone = mission.getDrone();

        // Check 1: Drone already flying another mission
        if (drone.getStatus() == DroneStatus.IN_FLIGHT || drone.getStatus() == DroneStatus.RETURNING_TO_HOME) {
            throw new IllegalStateException("Drone " + drone.getSerialNumber() + " is already ACTIVE in another flight mission! Wait, pause, or abort the current flight before launching a new mission.");
        }

        // Check 2: Drone charging at dock
        if (drone.getStatus() == DroneStatus.CHARGING) {
            throw new IllegalStateException("Drone " + drone.getSerialNumber() + " is currently CHARGING at the docking station (" + Math.round(drone.getBatteryPercentage()) + "%). Please wait until fully charged.");
        }

        // Check 3: Minimum battery threshold
        if (drone.getBatteryPercentage() < 15.0) {
            throw new IllegalStateException("Cannot start mission: Drone battery is below 15%! Please recharge drone.");
        }

        List<Waypoint> waypoints = mission.getWaypoints();
        if (waypoints == null || waypoints.isEmpty()) {
            throw new IllegalStateException("Cannot start mission: No waypoints defined.");
        }

        Waypoint startWp = waypoints.get(0);

        // Check 4: Transit / Ferry Distance Check (20 km distance scenario)
        if (drone.getCurrentLatitude() != null && drone.getCurrentLongitude() != null) {
            double distToLaunchPad = AlertRuleEngineService.calculateHaversineDistance(
                    drone.getCurrentLatitude(), drone.getCurrentLongitude(),
                    startWp.getLatitude(), startWp.getLongitude()
            );

            // A typical commercial UAV cannot ferry 20 km on a single battery (max range ~6.0 km)
            if (distToLaunchPad > 6000.0) {
                throw new IllegalStateException(String.format(
                        "Relocation Required: Drone %s is currently %.1f km away from this mission's launch pad! Maximum drone operational ferry range is 6.0 km. A commercial drone cannot fly 20 km without exhausting battery mid-air. Please relocate drone or plan closer mission.",
                        drone.getSerialNumber(), distToLaunchPad / 1000.0
                ));
            }
        }

        // Position drone precisely at Waypoint 1 (Launch Pad)
        drone.setCurrentLatitude(startWp.getLatitude());
        drone.setCurrentLongitude(startWp.getLongitude());
        drone.setCurrentAltitude(startWp.getTargetAltitudeMeters() != null ? startWp.getTargetAltitudeMeters() : 20.0);
        drone.setCurrentSpeed(startWp.getSpeedMps() != null ? startWp.getSpeedMps() : 10.0);

        // Set initial heading and target to Waypoint 2 (Index 1)
        if (waypoints.size() > 1) {
            Waypoint nextWp = waypoints.get(1);
            double initialHeading = TelemetrySimulatorService.calculateBearing(
                    startWp.getLatitude(), startWp.getLongitude(),
                    nextWp.getLatitude(), nextWp.getLongitude()
            );
            drone.setCurrentHeading(Math.round(initialHeading * 10.0) / 10.0);
            mission.setCurrentWaypointIndex(1); // Target WP 2 immediately
        } else {
            mission.setCurrentWaypointIndex(0);
        }

        mission.setStatus(MissionStatus.ACTIVE);
        mission.setStartedAt(LocalDateTime.now());
        mission.setProgressPercentage(0.0);

        drone.setStatus(DroneStatus.IN_FLIGHT);
        droneRepository.save(drone);

        return missionRepository.save(mission);
    }

    @Transactional
    public Mission pauseMission(Long missionId) {
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new RuntimeException("Mission not found with ID: " + missionId));
        mission.setStatus(MissionStatus.PAUSED);
        return missionRepository.save(mission);
    }

    @Transactional
    public Mission resumeMission(Long missionId) {
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new RuntimeException("Mission not found with ID: " + missionId));
        mission.setStatus(MissionStatus.ACTIVE);
        mission.getDrone().setStatus(DroneStatus.IN_FLIGHT);
        droneRepository.save(mission.getDrone());
        return missionRepository.save(mission);
    }

    @Transactional
    public Mission abortMission(Long missionId) {
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new RuntimeException("Mission not found with ID: " + missionId));
        mission.setStatus(MissionStatus.ABORTED);
        mission.setCompletedAt(LocalDateTime.now());

        Drone drone = mission.getDrone();
        drone.setStatus(DroneStatus.IDLE);
        drone.setCurrentSpeed(0.0);
        droneRepository.save(drone);

        return missionRepository.save(mission);
    }

    @Transactional
    public Mission triggerReturnToHome(Long missionId) {
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new RuntimeException("Mission not found with ID: " + missionId));

        Drone drone = mission.getDrone();
        drone.setStatus(DroneStatus.RETURNING_TO_HOME);
        droneRepository.save(drone);

        return mission;
    }

    @Transactional
    public void deleteMission(Long missionId) {
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new RuntimeException("Mission not found with ID: " + missionId));
        if (mission.getStatus() == MissionStatus.ACTIVE) {
            throw new IllegalStateException("Cannot delete an active mission! Abort flight first.");
        }
        missionRepository.delete(mission);
    }
}