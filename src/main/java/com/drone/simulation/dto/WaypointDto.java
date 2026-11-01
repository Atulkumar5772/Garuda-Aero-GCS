package com.drone.simulation.dto;

public class WaypointDto {
    private Integer sequenceOrder;
    private Double latitude;
    private Double longitude;
    private Double targetAltitudeMeters = 30.0;
    private Double speedMps = 10.0;

    public WaypointDto() {}

    public WaypointDto(Integer sequenceOrder, Double latitude, Double longitude, Double targetAltitudeMeters, Double speedMps) {
        this.sequenceOrder = sequenceOrder;
        this.latitude = latitude;
        this.longitude = longitude;
        this.targetAltitudeMeters = targetAltitudeMeters;
        this.speedMps = speedMps;
    }

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