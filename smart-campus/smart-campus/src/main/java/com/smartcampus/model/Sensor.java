package com.smartcampus.model;

/**
 * Represents a sensor device deployed in a campus room.
 */
public class Sensor {

    private String id;            // Unique identifier, e.g. "TEMP-001"
    private String type;          // Category: "Temperature", "Occupancy", "CO2"
    private String status;        // "ACTIVE", "MAINTENANCE", or "OFFLINE"
    private double currentValue;  // Most recent measurement
    private String roomId;        // Foreign key to the Room this sensor belongs to

    // ── Constructors ──────────────────────────────────────────────────────────

    public Sensor() {}

    public Sensor(String id, String type, String status, double currentValue, String roomId) {
        this.id           = id;
        this.type         = type;
        this.status       = status;
        this.currentValue = currentValue;
        this.roomId       = roomId;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public String getId()                          { return id; }
    public void   setId(String id)                 { this.id = id; }

    public String getType()                        { return type; }
    public void   setType(String type)             { this.type = type; }

    public String getStatus()                      { return status; }
    public void   setStatus(String status)         { this.status = status; }

    public double getCurrentValue()                { return currentValue; }
    public void   setCurrentValue(double v)        { this.currentValue = v; }

    public String getRoomId()                      { return roomId; }
    public void   setRoomId(String roomId)         { this.roomId = roomId; }
}
