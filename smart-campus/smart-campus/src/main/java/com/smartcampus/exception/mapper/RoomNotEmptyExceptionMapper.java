package com.smartcampus.exception.mapper;

import com.smartcampus.exception.RoomNotEmptyException;

import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Maps RoomNotEmptyException → HTTP 409 Conflict.
 *
 * @Provider registers this mapper with the JAX-RS runtime automatically
 * (Jersey discovers it via package scanning). No manual registration needed.
 */
@Provider
public class RoomNotEmptyExceptionMapper implements ExceptionMapper<RoomNotEmptyException> {

    @Override
    public Response toResponse(RoomNotEmptyException ex) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status",  409);
        body.put("error",   "ROOM_HAS_SENSORS");
        body.put("message", "Room '" + ex.getRoomId() + "' cannot be decommissioned. "
                + "It is currently occupied by active hardware. "
                + "Reassign or remove all sensors before deleting this room.");
        body.put("roomId",         ex.getRoomId());
        body.put("assignedSensors", ex.getSensorIds());
        body.put("hint", "DELETE each sensor first via DELETE /api/v1/sensors/{sensorId}");

        return Response.status(Response.Status.CONFLICT)
                .type(MediaType.APPLICATION_JSON)
                .entity(body)
                .build();
    }
}
