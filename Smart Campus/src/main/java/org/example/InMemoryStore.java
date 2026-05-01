package org.example;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

final class InMemoryStore {
    static final Map<String, Room> ROOMS = new ConcurrentHashMap<>();
    static final Map<String, Sensor> SENSORS = new ConcurrentHashMap<>();
    static final Map<String, List<org.example.SensorReading>> SENSOR_READINGS = new ConcurrentHashMap<>();

    private InMemoryStore() {
    }
}

