package com.drone.simulation.dto;

import java.util.List;

public class MissionCreateRequest {
    private String name;
    private Long droneId;
    private List<WaypointDto> waypoints;

    public MissionCreateRequest() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Long getDroneId() { return droneId; }
    public void setDroneId(Long droneId) { this.droneId = droneId; }

    public List<WaypointDto> getWaypoints() { return waypoints; }
    public void setWaypoints(List<WaypointDto> waypoints) { this.waypoints = waypoints; }
}