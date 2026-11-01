package com.drone.simulation.controller;

import com.drone.simulation.dto.DashboardStatsDto;
import com.drone.simulation.service.DroneService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    private final DroneService droneService;

    public DashboardController(DroneService droneService) {
        this.droneService = droneService;
    }

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDto> getStats() {
        return ResponseEntity.ok(droneService.getDashboardStats());
    }
}