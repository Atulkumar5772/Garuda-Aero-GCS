package com.drone.simulation.repository;

import com.drone.simulation.model.Mission;
import com.drone.simulation.model.MissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MissionRepository extends JpaRepository<Mission, Long> {
    List<Mission> findByStatus(MissionStatus status);
    List<Mission> findByDroneId(Long droneId);
}