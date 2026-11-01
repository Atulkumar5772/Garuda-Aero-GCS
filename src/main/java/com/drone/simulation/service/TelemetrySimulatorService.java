package com.drone.simulation.service;

import com.drone.simulation.dto.DroneTelemetryResponse;
import com.drone.simulation.model.*;
import com.drone.simulation.repository.DroneRepository;
import com.drone.simulation.repository.MissionRepository;
import com.drone.simulation.repository.TelemetryLogRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TelemetrySimulatorService {

    private final DroneRepository droneRepository;
    private final MissionRepository missionRepository;
    private final TelemetryLogRepository telemetryLogRepository;
    private final AlertRuleEngineService alertRuleEngineService;

    // Simulation tick delta time in seconds
    private static final double TICK_SECONDS = 1.0;

    public TelemetrySimulatorService(DroneRepository droneRepository,
                                    MissionRepository missionRepository,
                                    TelemetryLogRepository telemetryLogRepository,
                                    AlertRuleEngineService alertRuleEngineService) {
        this.droneRepository = droneRepository;
        this.missionRepository = missionRepository;
        this.telemetryLogRepository = telemetryLogRepository;
        this.alertRuleEngineService = alertRuleEngineService;
    }

    /**
     * Physics and Telemetry Ticker running every 1 second in the background.
     */
    @Scheduled(fixedRate = 1000)
    @Transactional
    public void simulateFlightTelemetryTick() {
        List<Mission> activeMissions = missionRepository.findByStatus(MissionStatus.ACTIVE);

        for (Mission mission : activeMissions) {
            processMissionTick(mission);
        }

        // Also process drones specifically in RETURNING_TO_HOME status
        List<Drone> rthDrones = droneRepository.findByStatus(DroneStatus.RETURNING_TO_HOME);
        for (Drone drone : rthDrones) {
            processRthTick(drone);
        }

        // Process drones at docking station in CHARGING status
        List<Drone> chargingDrones = droneRepository.findByStatus(DroneStatus.CHARGING);
        for (Drone drone : chargingDrones) {
            processChargingTick(drone);
        }
    }

    private void processMissionTick(Mission mission) {
        Drone drone = mission.getDrone();
        List<Waypoint> waypoints = mission.getWaypoints();

        if (waypoints == null || waypoints.isEmpty() || drone == null) {
            return;
        }

        int targetIndex = mission.getCurrentWaypointIndex() != null ? mission.getCurrentWaypointIndex() : 0;
        if (targetIndex >= waypoints.size()) {
            // Mission Completed Successfully
            mission.setStatus(MissionStatus.COMPLETED);
            mission.setCompletedAt(LocalDateTime.now());
            mission.setProgressPercentage(100.0);

            drone.setStatus(DroneStatus.IDLE);
            drone.setCurrentSpeed(0.0);
            drone.setCurrentAltitude(0.0);
            droneRepository.save(drone);
            missionRepository.save(mission);
            return;
        }

        Waypoint targetWp = waypoints.get(targetIndex);
        double curLat = drone.getCurrentLatitude() != null ? drone.getCurrentLatitude() : waypoints.get(0).getLatitude();
        double curLng = drone.getCurrentLongitude() != null ? drone.getCurrentLongitude() : waypoints.get(0).getLongitude();
        double curAlt = drone.getCurrentAltitude() != null ? drone.getCurrentAltitude() : 0.0;

        double targetLat = targetWp.getLatitude();
        double targetLng = targetWp.getLongitude();
        double targetAlt = targetWp.getTargetAltitudeMeters() != null ? targetWp.getTargetAltitudeMeters() : 30.0;
        double speed = targetWp.getSpeedMps() != null ? targetWp.getSpeedMps() : 10.0;

        // Cap speed by drone specs
        if (drone.getMaxSpeedMps() != null && speed > drone.getMaxSpeedMps()) {
            speed = drone.getMaxSpeedMps();
        }

        double distanceToTarget = AlertRuleEngineService.calculateHaversineDistance(curLat, curLng, targetLat, targetLng);
        double stepDistance = speed * TICK_SECONDS;

        double nextLat;
        double nextLng;
        double heading = calculateBearing(curLat, curLng, targetLat, targetLng);

        if (distanceToTarget <= stepDistance || distanceToTarget < 2.0) {
            // Reached current waypoint
            nextLat = targetLat;
            nextLng = targetLng;
            mission.setCurrentWaypointIndex(targetIndex + 1);
        } else {
            // Move fractionally towards target
            double fraction = stepDistance / distanceToTarget;
            nextLat = curLat + (targetLat - curLat) * fraction;
            nextLng = curLng + (targetLng - curLng) * fraction;
        }

        // ===================================================================
        // LEVEL 2: ACTIVE IN-FLIGHT VIRTUAL WALL INTERCEPTOR (AIRPORT RED ZONE)
        // ===================================================================
        double distToAirport = AlertRuleEngineService.calculateHaversineDistance(
                AlertRuleEngineService.JAIPUR_AIRPORT_LAT, AlertRuleEngineService.JAIPUR_AIRPORT_LNG,
                nextLat, nextLng
        );

        if (distToAirport <= AlertRuleEngineService.AIRPORT_NO_FLY_ZONE_RADIUS_METERS) {
            // HARD VIRTUAL WALL TRIGGER: Prevent physical entry, brake instantly, and trigger RTH
            drone.setStatus(DroneStatus.RETURNING_TO_HOME);
            mission.setStatus(MissionStatus.ABORTED);
            drone.setCurrentSpeed(0.0);
            droneRepository.save(drone);
            missionRepository.save(mission);

            alertRuleEngineService.evaluateFlightSafetyRules(drone, mission, waypoints.get(0).getLatitude(), waypoints.get(0).getLongitude());
            return;
        }

        // Smooth altitude climb/descend
        double nextAlt;
        if (Math.abs(targetAlt - curAlt) > 2.0) {
            nextAlt = curAlt + (targetAlt > curAlt ? 2.0 : -2.0);
        } else {
            nextAlt = targetAlt;
        }

        // Dynamic battery consumption (Hover 0.06% + Speed drag 0.03% + Climb 0.05%)
        double batteryDrain = 0.06 + (speed / 10.0) * 0.03 + (nextAlt > curAlt ? 0.04 : 0.0);
        double newBattery = Math.max(0.0, drone.getBatteryPercentage() - batteryDrain);

        // Synthetic pitch and roll
        double pitch = (speed > 0) ? Math.min(12.0, speed * 1.1) : 0.0;
        double roll = (Math.sin(System.currentTimeMillis() / 1500.0) * 2.0);

        // Update Drone State
        drone.setCurrentLatitude(nextLat);
        drone.setCurrentLongitude(nextLng);
        drone.setCurrentAltitude(Math.round(nextAlt * 10.0) / 10.0);
        drone.setCurrentSpeed(Math.round(speed * 10.0) / 10.0);
        drone.setCurrentHeading(Math.round(heading * 10.0) / 10.0);
        drone.setBatteryPercentage(Math.round(newBattery * 10.0) / 10.0);
        drone.setLastTelemetryUpdate(LocalDateTime.now());

        // Update Mission Progress
        double progress = ((double) targetIndex / waypoints.size()) * 100.0;
        mission.setProgressPercentage(Math.round(progress * 10.0) / 10.0);

        // Persist Telemetry Log
        TelemetryLog log = new TelemetryLog(
                drone.getId(),
                mission.getId(),
                nextLat,
                nextLng,
                drone.getCurrentAltitude(),
                drone.getCurrentSpeed(),
                drone.getBatteryPercentage(),
                drone.getCurrentHeading()
        );
        log.setPitchDegrees(Math.round(pitch * 10.0) / 10.0);
        log.setRollDegrees(Math.round(roll * 10.0) / 10.0);
        telemetryLogRepository.save(log);

        // Evaluate Safety Rules
        Waypoint homeWp = waypoints.get(0);
        alertRuleEngineService.evaluateFlightSafetyRules(drone, mission, homeWp.getLatitude(), homeWp.getLongitude());

        // Low Battery Auto-RTH Trigger
        if (drone.getBatteryPercentage() <= AlertRuleEngineService.LOW_BATTERY_THRESHOLD &&
            drone.getStatus() != DroneStatus.RETURNING_TO_HOME) {
            drone.setStatus(DroneStatus.RETURNING_TO_HOME);
        }

        // Critical Battery Emergency Landing
        if (drone.getBatteryPercentage() <= 2.0) {
            drone.setStatus(DroneStatus.EMERGENCY_LANDED);
            drone.setCurrentSpeed(0.0);
            drone.setCurrentAltitude(0.0);
            mission.setStatus(MissionStatus.ABORTED);
        }

        droneRepository.save(drone);
        missionRepository.save(mission);
    }

    private void processRthTick(Drone drone) {
        // Find home launch pad
        double homeLat = 26.8480;
        double homeLng = 75.8120;

        List<Mission> missions = missionRepository.findByDroneId(drone.getId());
        if (!missions.isEmpty() && !missions.get(0).getWaypoints().isEmpty()) {
            homeLat = missions.get(0).getWaypoints().get(0).getLatitude();
            homeLng = missions.get(0).getWaypoints().get(0).getLongitude();
        }

        double curLat = drone.getCurrentLatitude() != null ? drone.getCurrentLatitude() : homeLat;
        double curLng = drone.getCurrentLongitude() != null ? drone.getCurrentLongitude() : homeLng;
        double dist = AlertRuleEngineService.calculateHaversineDistance(curLat, curLng, homeLat, homeLng);

        if (dist <= 4.0) {
            // Safely landed at Home Launch Pad
            drone.setStatus(DroneStatus.IDLE);
            drone.setCurrentSpeed(0.0);
            drone.setCurrentAltitude(0.0);
            droneRepository.save(drone);
        } else {
            double heading = calculateBearing(curLat, curLng, homeLat, homeLng);
            double speed = (drone.getMaxSpeedMps() != null ? drone.getMaxSpeedMps() : 12.0);
            double stepDist = speed * TICK_SECONDS;
            double fraction = Math.min(1.0, stepDist / dist);

            drone.setCurrentLatitude(curLat + (homeLat - curLat) * fraction);
            drone.setCurrentLongitude(curLng + (homeLng - curLng) * fraction);
            drone.setCurrentHeading(Math.round(heading * 10.0) / 10.0);
            drone.setCurrentSpeed(speed);
            drone.setCurrentAltitude(Math.max(15.0, (drone.getCurrentAltitude() != null ? drone.getCurrentAltitude() : 20.0) - 1.0));
            drone.setBatteryPercentage(Math.max(0.0, drone.getBatteryPercentage() - 0.08));
            droneRepository.save(drone);
        }
    }

    private void processChargingTick(Drone drone) {
        double currentBatt = drone.getBatteryPercentage() != null ? drone.getBatteryPercentage() : 0.0;
        // Fast docking station charge (+5.0% per second)
        double newBatt = Math.min(100.0, currentBatt + 5.0);
        drone.setBatteryPercentage(Math.round(newBatt * 10.0) / 10.0);
        drone.setCurrentSpeed(0.0);
        drone.setCurrentAltitude(0.0);
        if (newBatt >= 100.0) {
            drone.setStatus(DroneStatus.IDLE); // Fully charged, ready for mission!
        }
        droneRepository.save(drone);
    }

    public List<DroneTelemetryResponse> getAllLiveTelemetry() {
        List<Drone> drones = droneRepository.findAll();
        List<DroneTelemetryResponse> responses = new ArrayList<>();

        for (Drone d : drones) {
            DroneTelemetryResponse resp = new DroneTelemetryResponse();
            resp.setDroneId(d.getId());
            resp.setSerialNumber(d.getSerialNumber());
            resp.setModelName(d.getModelName());
            resp.setStatus(d.getStatus());
            resp.setBatteryPercentage(d.getBatteryPercentage());
            resp.setLatitude(d.getCurrentLatitude());
            resp.setLongitude(d.getCurrentLongitude());
            resp.setAltitude(d.getCurrentAltitude());
            resp.setSpeed(d.getCurrentSpeed());
            resp.setHeading(d.getCurrentHeading());
            resp.setTimestamp(d.getLastTelemetryUpdate());

            // Check if active mission exists
            List<Mission> missions = missionRepository.findByDroneId(d.getId());
            for (Mission m : missions) {
                if (m.getStatus() == MissionStatus.ACTIVE || m.getStatus() == MissionStatus.PAUSED) {
                    resp.setActiveMissionId(m.getId());
                    resp.setActiveMissionName(m.getName());
                    resp.setMissionProgress(m.getProgressPercentage());
                    break;
                }
            }
            responses.add(resp);
        }
        return responses;
    }

    public static double calculateBearing(double lat1, double lon1, double lat2, double lon2) {
        double dLon = Math.toRadians(lon2 - lon1);
        double y = Math.sin(dLon) * Math.cos(Math.toRadians(lat2));
        double x = Math.cos(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) -
                   Math.sin(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(dLon);
        double brng = Math.toDegrees(Math.atan2(y, x));
        return (brng + 360) % 360;
    }
}