package com.smartcampus.exception.mapper;

import com.smartcampus.exception.SensorUnavailableException;

import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Maps SensorUnavailableException → HTTP 403 Forbidden.
 *
 * 403 is appropriate here because the server understood the request perfectly,
 * but it is REFUSING to act on it due to the sensor's current operational state.
 * The client is not permitted to post readings to an offline/maintenance device.
 */
@Provider
public class SensorUnavailableExceptionMapper
        implements ExceptionMapper<SensorUnavailableException> {

    @Override
    public Response toResponse(SensorUnavailableException ex) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status",        403);
        body.put("error",         "SENSOR_UNAVAILABLE");
        body.put("message",       ex.getMessage());
        body.put("sensorId",      ex.getSensorId());
        body.put("currentStatus", ex.getCurrentStatus());
        body.put("hint",          "Update the sensor status to ACTIVE via PUT /api/v1/sensors/"
                + ex.getSensorId() + " before submitting readings.");

        return Response.status(Response.Status.FORBIDDEN)
                .type(MediaType.APPLICATION_JSON)
                .entity(body)
                .build();
    }
}
