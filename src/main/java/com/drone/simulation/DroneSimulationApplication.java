package com.drone.simulation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DroneSimulationApplication {
    public static void main(String[] args) {
        SpringApplication.run(DroneSimulationApplication.class, args);
        System.out.println("===============================================================");
        System.out.println(" Drone Mission Planning & Telemetry Simulation Platform Live!");
        System.out.println(" Open Dashboard at: http://localhost:8080");
        System.out.println("===============================================================");
    }
}