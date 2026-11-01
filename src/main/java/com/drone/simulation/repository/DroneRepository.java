package com.drone.simulation.repository;

import com.drone.simulation.model.Drone;
import com.drone.simulation.model.DroneStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DroneRepository extends JpaRepository<Drone, Long> {
    Optional<Drone> findBySerialNumber(String serialNumber);
    List<Drone> findByStatus(DroneStatus status);
}