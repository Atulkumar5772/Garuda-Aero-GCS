package com.drone.simulation.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "missions")
public class Mission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "drone_id", nullable = false)
    private Drone drone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MissionStatus status = MissionStatus.DRAFT;

    @OneToMany(mappedBy = "mission", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("sequenceOrder ASC")
    private List<Waypoint> waypoints = new ArrayList<>();

    private Integer currentWaypointIndex = 0;
    private Double totalDistanceMeters = 0.0;
    private Double progressPercentage = 0.0;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    public Mission() {}

    public Mission(String name, Drone drone) {
        this.name = name;
        this.drone = drone;
        this.status = MissionStatus.DRAFT;
        this.createdAt = LocalDateTime.now();
    }

    public void addWaypoint(Waypoint wp) {
        waypoints.add(wp);
        wp.setMission(this);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Drone getDrone() { return drone; }
    public void setDrone(Drone drone) { this.drone = drone; }

    public MissionStatus getStatus() { return status; }
    public void setStatus(MissionStatus status) { this.status = status; }

    public List<Waypoint> getWaypoints() { return waypoints; }
    public void setWaypoints(List<Waypoint> waypoints) { this.waypoints = waypoints; }

    public Integer getCurrentWaypointIndex() { return currentWaypointIndex; }
    public void setCurrentWaypointIndex(Integer currentWaypointIndex) { this.currentWaypointIndex = currentWaypointIndex; }

    public Double getTotalDistanceMeters() { return totalDistanceMeters; }
    public void setTotalDistanceMeters(Double totalDistanceMeters) { this.totalDistanceMeters = totalDistanceMeters; }

    public Double getProgressPercentage() { return progressPercentage; }
    public void setProgressPercentage(Double progressPercentage) { this.progressPercentage = progressPercentage; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}