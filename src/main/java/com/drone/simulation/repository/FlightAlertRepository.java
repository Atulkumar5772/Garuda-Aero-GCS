package com.drone.simulation.repository;

import com.drone.simulation.model.FlightAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FlightAlertRepository extends JpaRepository<FlightAlert, Long> {
    List<FlightAlert> findByDroneIdOrderByTimestampDesc(Long droneId);
    List<FlightAlert> findByMissionIdOrderByTimestampDesc(Long missionId);
    List<FlightAlert> findByAcknowledgedFalseOrderByTimestampDesc();
}