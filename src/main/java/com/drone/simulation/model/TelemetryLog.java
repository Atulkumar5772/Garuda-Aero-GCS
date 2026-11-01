package com.drone.simulation.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "telemetry_logs")
public class TelemetryLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long droneId;

    private Long missionId;

    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    private Double altitudeMeters;
    private Double speedMps;
    private Double batteryPercentage;
    private Double headingDegrees;
    private Double pitchDegrees = 0.0;
    private Double rollDegrees = 0.0;

    public TelemetryLog() {}

    public TelemetryLog(Long droneId, Long missionId, Double latitude, Double longitude, Double altitudeMeters, Double speedMps, Double batteryPercentage, Double headingDegrees) {
        this.droneId = droneId;
        this.missionId = missionId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitudeMeters = altitudeMeters;
        this.speedMps = speedMps;
        this.batteryPercentage = batteryPercentage;
        this.headingDegrees = headingDegrees;
        this.timestamp = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getDroneId() { return droneId; }
    public void setDroneId(Long droneId) { this.droneId = droneId; }

    public Long getMissionId() { return missionId; }
    public void setMissionId(Long missionId) { this.missionId = missionId; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public Double getAltitudeMeters() { return altitudeMeters; }
    public void setAltitudeMeters(Double altitudeMeters) { this.altitudeMeters = altitudeMeters; }

    public Double getSpeedMps() { return speedMps; }
    public void setSpeedMps(Double speedMps) { this.speedMps = speedMps; }

    public Double getBatteryPercentage() { return batteryPercentage; }
    public void setBatteryPercentage(Double batteryPercentage) { this.batteryPercentage = batteryPercentage; }

    public Double getHeadingDegrees() { return headingDegrees; }
    public void setHeadingDegrees(Double headingDegrees) { this.headingDegrees = headingDegrees; }

    public Double getPitchDegrees() { return pitchDegrees; }
    public void setPitchDegrees(Double pitchDegrees) { this.pitchDegrees = pitchDegrees; }

    public Double getRollDegrees() { return rollDegrees; }
    public void setRollDegrees(Double rollDegrees) { this.rollDegrees = rollDegrees; }
}