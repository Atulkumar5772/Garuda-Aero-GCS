package com.drone.simulation.dto;

import com.drone.simulation.model.DroneStatus;
import java.time.LocalDateTime;

public class DroneTelemetryResponse {
    private Long droneId;
    private String serialNumber;
    private String modelName;
    private DroneStatus status;
    private Double batteryPercentage;
    private Double latitude;
    private Double longitude;
    private Double altitude;
    private Double speed;
    private Double heading;
    private Double pitch;
    private Double roll;
    private Long activeMissionId;
    private String activeMissionName;
    private Double missionProgress;
    private LocalDateTime timestamp;

    public DroneTelemetryResponse() {}

    public Long getDroneId() { return droneId; }
    public void setDroneId(Long droneId) { this.droneId = droneId; }

    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }

    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }

    public DroneStatus getStatus() { return status; }
    public void setStatus(DroneStatus status) { this.status = status; }

    public Double getBatteryPercentage() { return batteryPercentage; }
    public void setBatteryPercentage(Double batteryPercentage) { this.batteryPercentage = batteryPercentage; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public Double getAltitude() { return altitude; }
    public void setAltitude(Double altitude) { this.altitude = altitude; }

    public Double getSpeed() { return speed; }
    public void setSpeed(Double speed) { this.speed = speed; }

    public Double getHeading() { return heading; }
    public void setHeading(Double heading) { this.heading = heading; }

    public Double getPitch() { return pitch; }
    public void setPitch(Double pitch) { this.pitch = pitch; }

    public Double getRoll() { return roll; }
    public void setRoll(Double roll) { this.roll = roll; }

    public Long getActiveMissionId() { return activeMissionId; }
    public void setActiveMissionId(Long activeMissionId) { this.activeMissionId = activeMissionId; }

    public String getActiveMissionName() { return activeMissionName; }
    public void setActiveMissionName(String activeMissionName) { this.activeMissionName = activeMissionName; }

    public Double getMissionProgress() { return missionProgress; }
    public void setMissionProgress(Double missionProgress) { this.missionProgress = missionProgress; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}