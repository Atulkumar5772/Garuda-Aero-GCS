package com.drone.simulation.dto;

public class DashboardStatsDto {
    private long totalDrones;
    private long activeDrones;
    private long totalMissions;
    private long completedMissions;
    private long activeAlerts;
    private double totalDistanceSimulatedMeters;

    public DashboardStatsDto() {}

    public long getTotalDrones() { return totalDrones; }
    public void setTotalDrones(long totalDrones) { this.totalDrones = totalDrones; }

    public long getActiveDrones() { return activeDrones; }
    public void setActiveDrones(long activeDrones) { this.activeDrones = activeDrones; }

    public long getTotalMissions() { return totalMissions; }
    public void setTotalMissions(long totalMissions) { this.totalMissions = totalMissions; }

    public long getCompletedMissions() { return completedMissions; }
    public void setCompletedMissions(long completedMissions) { this.completedMissions = completedMissions; }

    public long getActiveAlerts() { return activeAlerts; }
    public void setActiveAlerts(long activeAlerts) { this.activeAlerts = activeAlerts; }

    public double getTotalDistanceSimulatedMeters() { return totalDistanceSimulatedMeters; }
    public void setTotalDistanceSimulatedMeters(double totalDistanceSimulatedMeters) { this.totalDistanceSimulatedMeters = totalDistanceSimulatedMeters; }
}