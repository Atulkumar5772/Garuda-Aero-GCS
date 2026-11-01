package com.drone.simulation.service;

import com.drone.simulation.model.*;
import com.drone.simulation.repository.FlightAlertRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AlertRuleEngineService {

    private final FlightAlertRepository alertRepository;

    // Safety threshold constants
    public static final double MAX_SAFE_ALTITUDE_METERS = 120.0; // DGCA / FAA drone limit
    public static final double LOW_BATTERY_THRESHOLD = 20.0;      // 20% triggers RTH
    public static final double CRITICAL_BATTERY_THRESHOLD = 10.0; // 10% emergency landing
    public static final double MAX_GEOFENCE_RADIUS_METERS = 6000.0; // 6 km max outer radius

    // Jaipur International Airport (JAI) Restricted Coordinates & Radius
    public static final double JAIPUR_AIRPORT_LAT = 26.8286;
    public static final double JAIPUR_AIRPORT_LNG = 75.8056;
    public static final double AIRPORT_NO_FLY_ZONE_RADIUS_METERS = 1500.0; // 1.5 km DGCA Red Zone

    public AlertRuleEngineService(FlightAlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    /**
     * Evaluates all safety rules for a drone during active flight.
     */
    public void evaluateFlightSafetyRules(Drone drone, Mission mission, Double homeLat, Double homeLng) {
        if (drone == null) return;

        Long droneId = drone.getId();
        Long missionId = (mission != null) ? mission.getId() : null;

        // Rule 1: Jaipur Airport Red Zone Proximity Check
        if (drone.getCurrentLatitude() != null && drone.getCurrentLongitude() != null) {
            double distToAirport = calculateHaversineDistance(JAIPUR_AIRPORT_LAT, JAIPUR_AIRPORT_LNG,
                    drone.getCurrentLatitude(), drone.getCurrentLongitude());
            if (distToAirport <= AIRPORT_NO_FLY_ZONE_RADIUS_METERS + 50.0) {
                String msg = String.format("🛡️ VIRTUAL WALL TRIGGERED: Drone deflected at %.0fm boundary of Jaipur Airport (JAI) Red Zone! Autonomous RTH initiated.",
                        distToAirport);
                createAlertIfNotRecentlyTriggered(droneId, missionId, AlertType.GEOFENCE_BREACH, AlertSeverity.CRITICAL, msg);
            }
        }

        // Rule 2: Altitude Violation Check
        if (drone.getCurrentAltitude() != null && drone.getCurrentAltitude() > MAX_SAFE_ALTITUDE_METERS) {
            String msg = String.format("Altitude Limit Exceeded! Drone is at %.1fm (Max permitted: %.1fm). Regulatory ceiling breach.",
                    drone.getCurrentAltitude(), MAX_SAFE_ALTITUDE_METERS);
            createAlertIfNotRecentlyTriggered(droneId, missionId, AlertType.ALTITUDE_LIMIT_EXCEEDED, AlertSeverity.WARNING, msg);
        }

        // Rule 3: Low Battery & Critical Battery Alert
        if (drone.getBatteryPercentage() != null) {
            if (drone.getBatteryPercentage() <= CRITICAL_BATTERY_THRESHOLD) {
                String msg = String.format("CRITICAL BATTERY: %.1f%% remaining! Emergency landing sequence engaged.", drone.getBatteryPercentage());
                createAlertIfNotRecentlyTriggered(droneId, missionId, AlertType.LOW_BATTERY_RTH, AlertSeverity.CRITICAL, msg);
            } else if (drone.getBatteryPercentage() <= LOW_BATTERY_THRESHOLD) {
                String msg = String.format("Low Battery Warning (%.1f%%). Automatic Return-to-Home (RTH) initiated.", drone.getBatteryPercentage());
                createAlertIfNotRecentlyTriggered(droneId, missionId, AlertType.LOW_BATTERY_RTH, AlertSeverity.WARNING, msg);
            }
        }

        // Rule 4: Speed Limit Check
        if (drone.getCurrentSpeed() != null && drone.getMaxSpeedMps() != null && drone.getCurrentSpeed() > drone.getMaxSpeedMps()) {
            String msg = String.format("Overspeed Alert: Current speed %.1fm/s exceeds drone max design speed of %.1fm/s.",
                    drone.getCurrentSpeed(), drone.getMaxSpeedMps());
            createAlertIfNotRecentlyTriggered(droneId, missionId, AlertType.SPEED_LIMIT_EXCEEDED, AlertSeverity.WARNING, msg);
        }

        // Rule 5: Outer Geofence Distance Check from Launch Home Base
        if (homeLat != null && homeLng != null && drone.getCurrentLatitude() != null && drone.getCurrentLongitude() != null) {
            double distFromHome = calculateHaversineDistance(homeLat, homeLng, drone.getCurrentLatitude(), drone.getCurrentLongitude());
            if (distFromHome > MAX_GEOFENCE_RADIUS_METERS) {
                String msg = String.format("OUTER GEOFENCE BREACH: Drone is %.0fm away from launch base (Permitted limit: %.0fm)!",
                        distFromHome, MAX_GEOFENCE_RADIUS_METERS);
                createAlertIfNotRecentlyTriggered(droneId, missionId, AlertType.GEOFENCE_BREACH, AlertSeverity.CRITICAL, msg);
            }
        }
    }

    public static boolean isInsideAirportRedZone(double lat, double lng) {
        double dist = calculateHaversineDistance(JAIPUR_AIRPORT_LAT, JAIPUR_AIRPORT_LNG, lat, lng);
        return dist < AIRPORT_NO_FLY_ZONE_RADIUS_METERS;
    }

    private void createAlertIfNotRecentlyTriggered(Long droneId, Long missionId, AlertType type, AlertSeverity severity, String msg) {
        FlightAlert alert = new FlightAlert(droneId, missionId, type, severity, msg);
        alertRepository.save(alert);
    }

    public List<FlightAlert> getActiveAlerts() {
        return alertRepository.findByAcknowledgedFalseOrderByTimestampDesc();
    }

    public List<FlightAlert> getAlertsByDrone(Long droneId) {
        return alertRepository.findByDroneIdOrderByTimestampDesc(droneId);
    }

    public void acknowledgeAlert(Long alertId) {
        alertRepository.findById(alertId).ifPresent(a -> {
            a.setAcknowledged(true);
            alertRepository.save(a);
        });
    }

    public void acknowledgeAllAlerts() {
        List<FlightAlert> unacked = alertRepository.findByAcknowledgedFalseOrderByTimestampDesc();
        for (FlightAlert a : unacked) {
            a.setAcknowledged(true);
        }
        alertRepository.saveAll(unacked);
    }

    public static double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000; // Radius of Earth in meters
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c; // meters
    }
}