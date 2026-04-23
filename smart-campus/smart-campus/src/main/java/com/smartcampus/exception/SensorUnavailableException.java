package com.smartcampus.exception;

/**
 * Thrown when a POST reading is attempted on a Sensor whose status is
 * "MAINTENANCE" or "OFFLINE"  the device is physically disconnected
 * and cannot accept new readings.
 *
 * Mapped to HTTP 403 Forbidden by SensorUnavailableExceptionMapper.
 */
public class SensorUnavailableException extends RuntimeException {

    private final String sensorId;
    private final String currentStatus;

    public SensorUnavailableException(String sensorId, String currentStatus) {
        super("Sensor '" + sensorId + "' is currently " + currentStatus
                + " and cannot accept new readings.");
        this.sensorId      = sensorId;
        this.currentStatus = currentStatus;
    }

    public String getSensorId()      { return sensorId; }
    public String getCurrentStatus() { return currentStatus; }
}
