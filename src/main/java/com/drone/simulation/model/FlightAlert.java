package com.drone.simulation.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "flight_alerts")
public class FlightAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long droneId;

    private Long missionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertType alertType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertSeverity severity;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    private Boolean acknowledged = false;

    public FlightAlert() {}

    public FlightAlert(Long droneId, Long missionId, AlertType alertType, AlertSeverity severity, String message) {
        this.droneId = droneId;
        this.missionId = missionId;
        this.alertType = alertType;
        this.severity = severity;
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.acknowledged = false;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getDroneId() { return droneId; }
    public void setDroneId(Long droneId) { this.droneId = droneId; }

    public Long getMissionId() { return missionId; }
    public void setMissionId(Long missionId) { this.missionId = missionId; }

    public AlertType getAlertType() { return alertType; }
    public void setAlertType(AlertType alertType) { this.alertType = alertType; }

    public AlertSeverity getSeverity() { return severity; }
    public void setSeverity(AlertSeverity severity) { this.severity = severity; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public Boolean getAcknowledged() { return acknowledged; }
    public void setAcknowledged(Boolean acknowledged) { this.acknowledged = acknowledged; }
}