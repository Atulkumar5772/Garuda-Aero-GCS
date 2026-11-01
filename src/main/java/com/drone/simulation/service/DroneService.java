package com.drone.simulation.service;

import com.drone.simulation.dto.DashboardStatsDto;
import com.drone.simulation.model.Drone;
import com.drone.simulation.model.DroneStatus;
import com.drone.simulation.model.MissionStatus;
import com.drone.simulation.repository.DroneRepository;
import com.drone.simulation.repository.FlightAlertRepository;
import com.drone.simulation.repository.MissionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class DroneService {

    private final DroneRepository droneRepository;
    private final MissionRepository missionRepository;
    private final FlightAlertRepository alertRepository;

    public DroneService(DroneRepository droneRepository, MissionRepository missionRepository, FlightAlertRepository alertRepository) {
        this.droneRepository = droneRepository;
        this.missionRepository = missionRepository;
        this.alertRepository = alertRepository;
    }

    public List<Drone> getAllDrones() {
        return droneRepository.findAll();
    }

    public Optional<Drone> getDroneById(Long id) {
        return droneRepository.findById(id);
    }

    public Drone registerDrone(Drone drone) {
        if (drone.getStatus() == null) {
            drone.setStatus(DroneStatus.IDLE);
        }
        if (drone.getBatteryPercentage() == null) {
            drone.setBatteryPercentage(100.0);
        }
        if (drone.getCurrentAltitude() == null) {
            drone.setCurrentAltitude(0.0);
        }
        if (drone.getCurrentSpeed() == null) {
            drone.setCurrentSpeed(0.0);
        }
        if (drone.getCurrentHeading() == null) {
            drone.setCurrentHeading(0.0);
        }
        drone.setLastTelemetryUpdate(LocalDateTime.now());
        return droneRepository.save(drone);
    }

    public Drone updateDrone(Long id, Drone updated) {
        return droneRepository.findById(id).map(drone -> {
            drone.setModelName(updated.getModelName());
            drone.setSerialNumber(updated.getSerialNumber());
            drone.setMaxSpeedMps(updated.getMaxSpeedMps());
            drone.setMaxAltitudeMeters(updated.getMaxAltitudeMeters());
            return droneRepository.save(drone);
        }).orElseThrow(() -> new RuntimeException("Drone not found with ID: " + id));
    }

    public void rechargeDrone(Long id) {
        droneRepository.findById(id).ifPresent(drone -> {
            if (drone.getStatus() == DroneStatus.IN_FLIGHT || drone.getStatus() == DroneStatus.RETURNING_TO_HOME) {
                throw new IllegalStateException("Cannot recharge drone while in flight! Land the drone at base first.");
            }
            drone.setStatus(DroneStatus.CHARGING);
            drone.setCurrentSpeed(0.0);
            drone.setCurrentAltitude(0.0);
            droneRepository.save(drone);
        });
    }

    public void deleteDrone(Long id) {
        droneRepository.deleteById(id);
    }

    public DashboardStatsDto getDashboardStats() {
        DashboardStatsDto stats = new DashboardStatsDto();
        stats.setTotalDrones(droneRepository.count());
        stats.setActiveDrones(droneRepository.findByStatus(DroneStatus.IN_FLIGHT).size() +
                              droneRepository.findByStatus(DroneStatus.RETURNING_TO_HOME).size());
        stats.setTotalMissions(missionRepository.count());
        stats.setCompletedMissions(missionRepository.findByStatus(MissionStatus.COMPLETED).size());
        stats.setActiveAlerts(alertRepository.findByAcknowledgedFalseOrderByTimestampDesc().size());

        double totalDist = missionRepository.findAll().stream()
                .mapToDouble(m -> m.getTotalDistanceMeters() != null ? m.getTotalDistanceMeters() : 0.0)
                .sum();
        stats.setTotalDistanceSimulatedMeters(Math.round(totalDist * 100.0) / 100.0);
        return stats;
    }
}