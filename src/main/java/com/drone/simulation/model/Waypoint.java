package com.drone.simulation.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "waypoints")
public class Waypoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_id", nullable = false)
    @JsonIgnore
    private Mission mission;

    @Column(nullable = false)
    private Integer sequenceOrder;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(nullable = false)
    private Double targetAltitudeMeters = 30.0;

    @Column(nullable = false)
    private Double speedMps = 10.0;

    public Waypoint() {}

    public Waypoint(Integer sequenceOrder, Double latitude, Double longitude, Double targetAltitudeMeters, Double speedMps) {
        this.sequenceOrder = sequenceOrder;
        this.latitude = latitude;
        this.longitude = longitude;
        this.targetAltitudeMeters = targetAltitudeMeters;
        this.speedMps = speedMps;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Mission getMission() { return mission; }
    public void setMission(Mission mission) { this.mission = mission; }

    public Integer getSequenceOrder() { return sequenceOrder; }
    public void setSequenceOrder(Integer sequenceOrder) { this.sequenceOrder = sequenceOrder; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public Double getTargetAltitudeMeters() { return targetAltitudeMeters; }
    public void setTargetAltitudeMeters(Double targetAltitudeMeters) { this.targetAltitudeMeters = targetAltitudeMeters; }

    public Double getSpeedMps() { return speedMps; }
    public void setSpeedMps(Double speedMps) { this.speedMps = speedMps; }
}