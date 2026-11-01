package com.drone.simulation.controller;

import com.drone.simulation.dto.MissionCreateRequest;
import com.drone.simulation.model.Mission;
import com.drone.simulation.service.MissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/missions")
@CrossOrigin(origins = "*")
public class MissionController {

    private final MissionService missionService;

    public MissionController(MissionService missionService) {
        this.missionService = missionService;
    }

    @GetMapping
    public ResponseEntity<List<Mission>> getAllMissions() {
        return ResponseEntity.ok(missionService.getAllMissions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Mission> getMissionById(@PathVariable("id") Long id) {
        return missionService.getMissionById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Mission> createMission(@RequestBody MissionCreateRequest request) {
        return ResponseEntity.ok(missionService.createMission(request));
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<Mission> startMission(@PathVariable("id") Long id) {
        return ResponseEntity.ok(missionService.startMission(id));
    }

    @PostMapping("/{id}/pause")
    public ResponseEntity<Mission> pauseMission(@PathVariable("id") Long id) {
        return ResponseEntity.ok(missionService.pauseMission(id));
    }

    @PostMapping("/{id}/resume")
    public ResponseEntity<Mission> resumeMission(@PathVariable("id") Long id) {
        return ResponseEntity.ok(missionService.resumeMission(id));
    }

    @PostMapping("/{id}/abort")
    public ResponseEntity<Mission> abortMission(@PathVariable("id") Long id) {
        return ResponseEntity.ok(missionService.abortMission(id));
    }

    @PostMapping("/{id}/rth")
    public ResponseEntity<Mission> returnToHome(@PathVariable("id") Long id) {
        return ResponseEntity.ok(missionService.triggerReturnToHome(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMission(@PathVariable("id") Long id) {
        missionService.deleteMission(id);
        return ResponseEntity.noContent().build();
    }
}