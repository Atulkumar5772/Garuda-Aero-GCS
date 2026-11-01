package com.drone.simulation.controller;

import com.drone.simulation.model.Drone;
import com.drone.simulation.service.DroneService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/drones")
@CrossOrigin(origins = "*")
public class DroneController {

    private final DroneService droneService;

    public DroneController(DroneService droneService) {
        this.droneService = droneService;
    }

    @GetMapping
    public ResponseEntity<List<Drone>> getAllDrones() {
        return ResponseEntity.ok(droneService.getAllDrones());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Drone> getDroneById(@PathVariable("id") Long id) {
        return droneService.getDroneById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Drone> registerDrone(@RequestBody Drone drone) {
        return ResponseEntity.ok(droneService.registerDrone(drone));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Drone> updateDrone(@PathVariable("id") Long id, @RequestBody Drone drone) {
        return ResponseEntity.ok(droneService.updateDrone(id, drone));
    }

    @PostMapping("/{id}/recharge")
    public ResponseEntity<String> rechargeDrone(@PathVariable("id") Long id) {
        droneService.rechargeDrone(id);
        return ResponseEntity.ok("Drone battery recharged to 100%");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDrone(@PathVariable("id") Long id) {
        droneService.deleteDrone(id);
        return ResponseEntity.noContent().build();
    }
}