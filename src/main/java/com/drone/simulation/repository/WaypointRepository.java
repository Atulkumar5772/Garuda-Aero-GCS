package com.drone.simulation.repository;

import com.drone.simulation.model.Waypoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WaypointRepository extends JpaRepository<Waypoint, Long> {
    List<Waypoint> findByMissionIdOrderBySequenceOrderAsc(Long missionId);
}