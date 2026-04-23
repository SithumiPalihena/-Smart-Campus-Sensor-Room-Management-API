package com.smartcampus.data;

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;


public class DataStore {

    // Singleton boilerplate 

    private static final DataStore INSTANCE = new DataStore();

    private DataStore() {
        // No seed data — API starts with empty collections
    }

    public static DataStore getInstance() {
        return INSTANCE;
    }

    //  Storage maps 

    /**
     * Rooms keyed by room ID (e.g. "LIB-301").
     * ConcurrentHashMap guarantees atomic put/get/remove operations.
     */
    private final Map<String, Room> rooms = new ConcurrentHashMap<>();

    /**
     * Sensors keyed by sensor ID (e.g. "TEMP-001").
     */
    private final Map<String, Sensor> sensors = new ConcurrentHashMap<>();

    /**
     * Sensor readings keyed by reading UUID.
     * CopyOnWriteArrayList is used per sensor to allow safe concurrent reads.
     */
    private final Map<String, List<SensorReading>> sensorReadings = new ConcurrentHashMap<>();

    // ── Accessors ─────────────────────────────────────────────────────────────

    public Map<String, Room> getRooms()           { return rooms; }
    public Map<String, Sensor> getSensors()        { return sensors; }

    public List<SensorReading> getReadingsForSensor(String sensorId) {
        return sensorReadings.computeIfAbsent(sensorId, k -> new CopyOnWriteArrayList<>());
    }

    // ── Seed data (makes the API immediately testable) ────────────────────────

    private void seedSampleData() {
        // --- Rooms ---
        Room lib301 = new Room("LIB-301", "Library Quiet Study", 40);
        Room engLab = new Room("ENG-101", "Engineering Computer Lab", 30);
        rooms.put(lib301.getId(), lib301);
        rooms.put(engLab.getId(), engLab);

        // --- Sensors ---
        Sensor temp001 = new Sensor("TEMP-001", "Temperature", "ACTIVE", 22.5, "LIB-301");
        Sensor co2001  = new Sensor("CO2-001",  "CO2",         "ACTIVE", 412.0, "LIB-301");
        Sensor occ001  = new Sensor("OCC-001",  "Occupancy",   "MAINTENANCE", 0.0, "ENG-101");

        sensors.put(temp001.getId(), temp001);
        sensors.put(co2001.getId(),  co2001);
        sensors.put(occ001.getId(),  occ001);

        lib301.getSensorIds().add(temp001.getId());
        lib301.getSensorIds().add(co2001.getId());
        engLab.getSensorIds().add(occ001.getId());
    }
}
