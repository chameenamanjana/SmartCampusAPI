/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.dao;

import com.example.model.Room;
import com.example.model.Sensor;
import com.example.model.SensorReading;

/**
 *
 * @author chameen-amanjana
 */
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MockDatabase {

    // ConcurrentHashMap used for thread-safety (addresses the lifecycle report question)
    public static final Map<String, Room> ROOMS = new ConcurrentHashMap<>();
    public static final Map<String, Sensor> SENSORS = new ConcurrentHashMap<>();
    public static final Map<String, List<SensorReading>> SENSOR_READINGS = new ConcurrentHashMap<>();

    static {
        // --- Seed Rooms ---
        Room r1 = new Room("LIB-301", "Library Quiet Study", 50);
        Room r2 = new Room("LAB-101", "Computer Lab", 30);
        Room r3 = new Room("HALL-A", "Main Hall", 200);
        ROOMS.put(r1.getId(), r1);
        ROOMS.put(r2.getId(), r2);
        ROOMS.put(r3.getId(), r3);

        // --- Seed Sensors ---
        Sensor s1 = new Sensor("TEMP-001", "Temperature", "ACTIVE", 22.5, "LIB-301");
        Sensor s2 = new Sensor("CO2-001", "CO2", "ACTIVE", 400.0, "LAB-101");
        Sensor s3 = new Sensor("OCC-001", "Occupancy", "MAINTENANCE", 0.0, "HALL-A");
        SENSORS.put(s1.getId(), s1);
        SENSORS.put(s2.getId(), s2);
        SENSORS.put(s3.getId(), s3);

        // --- Link Sensors to Rooms ---
        r1.getSensorIds().add("TEMP-001");
        r2.getSensorIds().add("CO2-001");
        r3.getSensorIds().add("OCC-001");

        // --- Seed Readings Lists ---
        SENSOR_READINGS.put("TEMP-001", new ArrayList<>());
        SENSOR_READINGS.put("CO2-001", new ArrayList<>());
        SENSOR_READINGS.put("OCC-001", new ArrayList<>());
    }
}
