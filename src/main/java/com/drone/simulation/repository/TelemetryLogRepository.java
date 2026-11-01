package com.drone.simulation.repository;

import com.drone.simulation.model.TelemetryLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TelemetryLogRepository extends JpaRepository<TelemetryLog, Long> {
    List<TelemetryLog> findByDroneIdOrderByTimestampDesc(Long droneId);
    List<TelemetryLog> findByMissionIdOrderByTimestampAsc(Long missionId);

    @Query("SELECT t FROM TelemetryLog t WHERE t.droneId = :droneId ORDER BY t.timestamp DESC LIMIT 1")
    Optional<TelemetryLog> findLatestByDroneId(@Param("droneId") Long droneId);
}