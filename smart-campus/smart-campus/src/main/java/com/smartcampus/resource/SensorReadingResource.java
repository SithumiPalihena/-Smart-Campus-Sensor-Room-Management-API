package com.smartcampus.resource;

import com.smartcampus.data.DataStore;
import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final String sensorId;
    private final DataStore store = DataStore.getInstance();

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    @GET
    public Response getReadings() {
        Sensor sensor = store.getSensors().get(sensorId);
        if (sensor == null)
            return errorResponse(404, "SENSOR_NOT_FOUND",
                "No sensor found with ID '" + sensorId + "'.");

        List<SensorReading> readings = store.getReadingsForSensor(sensorId);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("sensorId", sensorId);
        response.put("count", readings.size());
        response.put("readings", readings);
        response.put("_links", Map.of(
            "self",   linkObj("http://localhost:8080/api/v1/sensors/" + sensorId + "/readings", "GET"),
            "add",    linkObj("http://localhost:8080/api/v1/sensors/" + sensorId + "/readings", "POST"),
            "sensor", linkObj("http://localhost:8080/api/v1/sensors/" + sensorId, "GET")
        ));
        return Response.ok(response).build();
    }

    @POST
    public Response addReading(SensorReading reading) {
        Sensor sensor = store.getSensors().get(sensorId);
        if (sensor == null)
            return errorResponse(404, "SENSOR_NOT_FOUND",
                "No sensor found with ID '" + sensorId + "'.");

        // State constraint: throw SensorUnavailableException → 403 Forbidden
        if (!"ACTIVE".equals(sensor.getStatus())) {
            throw new SensorUnavailableException(sensorId, sensor.getStatus());
        }

        if (reading == null)
            return errorResponse(400, "MISSING_BODY", "Request body is required.");
        if (reading.getTimestamp() <= 0)
            return errorResponse(400, "MISSING_FIELD",
                "Reading 'timestamp' is required and must be a positive epoch value (ms).");

        if (reading.getId() == null || reading.getId().isBlank())
            reading.setId(UUID.randomUUID().toString());

        store.getReadingsForSensor(sensorId).add(reading);

        // Side effect: update parent sensor's currentValue
        sensor.setCurrentValue(reading.getValue());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Reading recorded successfully.");
        response.put("reading", reading);
        response.put("sensorCurrentValue", sensor.getCurrentValue());
        response.put("_links", Map.of(
            "self",    linkObj("http://localhost:8080/api/v1/sensors/" + sensorId + "/readings/" + reading.getId(), "GET"),
            "history", linkObj("http://localhost:8080/api/v1/sensors/" + sensorId + "/readings", "GET"),
            "sensor",  linkObj("http://localhost:8080/api/v1/sensors/" + sensorId, "GET")
        ));
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @GET
    @Path("/{readingId}")
    public Response getReadingById(@PathParam("readingId") String readingId) {
        Sensor sensor = store.getSensors().get(sensorId);
        if (sensor == null)
            return errorResponse(404, "SENSOR_NOT_FOUND",
                "No sensor found with ID '" + sensorId + "'.");

        SensorReading found = store.getReadingsForSensor(sensorId).stream()
                .filter(r -> r.getId().equals(readingId))
                .findFirst().orElse(null);

        if (found == null)
            return errorResponse(404, "READING_NOT_FOUND",
                "No reading found with ID '" + readingId + "' for sensor '" + sensorId + "'.");

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("reading", found);
        response.put("_links", Map.of(
            "self",    linkObj("http://localhost:8080/api/v1/sensors/" + sensorId + "/readings/" + readingId, "GET"),
            "history", linkObj("http://localhost:8080/api/v1/sensors/" + sensorId + "/readings", "GET"),
            "sensor",  linkObj("http://localhost:8080/api/v1/sensors/" + sensorId, "GET")
        ));
        return Response.ok(response).build();
    }

    private Map<String, String> linkObj(String href, String method) {
        Map<String, String> link = new LinkedHashMap<>();
        link.put("href", href); link.put("method", method);
        return link;
    }

    private Response errorResponse(int statusCode, String error, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", statusCode); body.put("error", error); body.put("message", message);
        return Response.status(statusCode).entity(body).build();
    }
}
