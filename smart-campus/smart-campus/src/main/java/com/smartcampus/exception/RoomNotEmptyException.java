package com.smartcampus.exception;

import java.util.List;

/**
 * Thrown when a DELETE is attempted on a Room that still has sensors assigned.
 * Mapped to HTTP 409 Conflict by RoomNotEmptyExceptionMapper.
 */
public class RoomNotEmptyException extends RuntimeException {

    private final String roomId;
    private final List<String> sensorIds;

    public RoomNotEmptyException(String roomId, List<String> sensorIds) {
        super("Room '" + roomId + "' cannot be deleted: it still has "
                + sensorIds.size() + " sensor(s) assigned.");
        this.roomId    = roomId;
        this.sensorIds = sensorIds;
    }

    public String getRoomId()          { return roomId; }
    public List<String> getSensorIds() { return sensorIds; }
}
