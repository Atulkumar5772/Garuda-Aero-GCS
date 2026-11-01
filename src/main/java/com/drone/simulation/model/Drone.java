package com.drone.simulation.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "drones")
public class Drone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String serialNumber;

    @Column(nullable = false)
    private String modelName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DroneStatus status = DroneStatus.IDLE;

    @Column(nullable = false)
    private Double batteryPercentage = 100.0;

    @Column(nullable = false)
    private Double maxSpeedMps = 15.0; // max speed in meters/second

    @Column(nullable = false)
    private Double maxAltitudeMeters = 120.0; // safe ceiling in meters

    private Double currentLatitude;
    private Double currentLongitude;
    private Double currentAltitude = 0.0;
    private Double currentSpeed = 0.0;
    private Double currentHeading = 0.0; // 0-360 degrees

    private LocalDateTime lastTelemetryUpdate;

    public Drone() {}

    public Drone(String serialNumber, String modelName, Double maxSpeedMps, Double maxAltitudeMeters, Double initialLat, Double initialLng) {
        this.serialNumber = serialNumber;
        this.modelName = modelName;
        this.maxSpeedMps = maxSpeedMps;
        this.maxAltitudeMeters = maxAltitudeMeters;
        this.currentLatitude = initialLat;
        this.currentLongitude = initialLng;
        this.status = DroneStatus.IDLE;
        this.batteryPercentage = 100.0;
        this.lastTelemetryUpdate = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }

    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }

    public DroneStatus getStatus() { return status; }
    public void setStatus(DroneStatus status) { this.status = status; }

    public Double getBatteryPercentage() { return batteryPercentage; }
    public void setBatteryPercentage(Double batteryPercentage) { this.batteryPercentage = batteryPercentage; }

    public Double getMaxSpeedMps() { return maxSpeedMps; }
    public void setMaxSpeedMps(Double maxSpeedMps) { this.maxSpeedMps = maxSpeedMps; }

    public Double getMaxAltitudeMeters() { return maxAltitudeMeters; }
    public void setMaxAltitudeMeters(Double maxAltitudeMeters) { this.maxAltitudeMeters = maxAltitudeMeters; }

    public Double getCurrentLatitude() { return currentLatitude; }
    public void setCurrentLatitude(Double currentLatitude) { this.currentLatitude = currentLatitude; }

    public Double getCurrentLongitude() { return currentLongitude; }
    public void setCurrentLongitude(Double currentLongitude) { this.currentLongitude = currentLongitude; }

    public Double getCurrentAltitude() { return currentAltitude; }
    public void setCurrentAltitude(Double currentAltitude) { this.currentAltitude = currentAltitude; }

    public Double getCurrentSpeed() { return currentSpeed; }
    public void setCurrentSpeed(Double currentSpeed) { this.currentSpeed = currentSpeed; }

    public Double getCurrentHeading() { return currentHeading; }
    public void setCurrentHeading(Double currentHeading) { this.currentHeading = currentHeading; }

    public LocalDateTime getLastTelemetryUpdate() { return lastTelemetryUpdate; }
    public void setLastTelemetryUpdate(LocalDateTime lastTelemetryUpdate) { this.lastTelemetryUpdate = lastTelemetryUpdate; }
}