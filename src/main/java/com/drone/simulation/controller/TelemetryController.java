package com.drone.simulation.controller;

import com.drone.simulation.dto.DroneTelemetryResponse;
import com.drone.simulation.model.TelemetryLog;
import com.drone.simulation.repository.TelemetryLogRepository;
import com.drone.simulation.service.TelemetrySimulatorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/telemetry")
@CrossOrigin(origins = "*")
public class TelemetryController {

    private final TelemetrySimulatorService telemetrySimulatorService;
    private final TelemetryLogRepository telemetryLogRepository;

    public TelemetryController(TelemetrySimulatorService telemetrySimulatorService,
                               TelemetryLogRepository telemetryLogRepository) {
        this.telemetrySimulatorService = telemetrySimulatorService;
        this.telemetryLogRepository = telemetryLogRepository;
    }

    @GetMapping("/live")
    public ResponseEntity<List<DroneTelemetryResponse>> getLiveTelemetry() {
        return ResponseEntity.ok(telemetrySimulatorService.getAllLiveTelemetry());
    }

    @GetMapping("/history/{droneId}")
    public ResponseEntity<List<TelemetryLog>> getTelemetryHistory(@PathVariable("droneId") Long droneId) {
        return ResponseEntity.ok(telemetryLogRepository.findByDroneIdOrderByTimestampDesc(droneId));
    }

    @GetMapping("/mission/{missionId}")
    public ResponseEntity<List<TelemetryLog>> getMissionTelemetry(@PathVariable("missionId") Long missionId) {
        return ResponseEntity.ok(telemetryLogRepository.findByMissionIdOrderByTimestampAsc(missionId));
    }
}