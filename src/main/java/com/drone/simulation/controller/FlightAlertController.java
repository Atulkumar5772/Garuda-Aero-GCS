package com.drone.simulation.controller;

import com.drone.simulation.model.FlightAlert;
import com.drone.simulation.service.AlertRuleEngineService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
@CrossOrigin(origins = "*")
public class FlightAlertController {

    private final AlertRuleEngineService alertService;

    public FlightAlertController(AlertRuleEngineService alertService) {
        this.alertService = alertService;
    }

    @GetMapping
    public ResponseEntity<List<FlightAlert>> getActiveAlerts() {
        return ResponseEntity.ok(alertService.getActiveAlerts());
    }

    @GetMapping("/drone/{droneId}")
    public ResponseEntity<List<FlightAlert>> getAlertsByDrone(@PathVariable("droneId") Long droneId) {
        return ResponseEntity.ok(alertService.getAlertsByDrone(droneId));
    }

    @PostMapping("/{id}/acknowledge")
    public ResponseEntity<String> acknowledgeAlert(@PathVariable("id") Long id) {
        alertService.acknowledgeAlert(id);
        return ResponseEntity.ok("Alert acknowledged.");
    }

    @PostMapping("/acknowledge-all")
    public ResponseEntity<String> acknowledgeAll() {
        alertService.acknowledgeAllAlerts();
        return ResponseEntity.ok("All alerts acknowledged.");
    }
}