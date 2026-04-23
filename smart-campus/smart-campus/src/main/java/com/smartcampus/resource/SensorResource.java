package com.smartcampus.resource;

import com.smartcampus.data.DataStore;
import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.stream.Collectors;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    private static final Set<String> VALID_STATUSES = Set.of("ACTIVE", "MAINTENANCE", "OFFLINE");
    private final DataStore store = DataStore.getInstance();

    @GET
    public Response getAllSensors(@QueryParam("type") String type) {
        Collection<Sensor> all = store.getSensors().values();
        List<Sensor> result;
        if (type != null && !type.isBlank()) {
            result = all.stream()
                    .filter(s -> s.getType().equalsIgnoreCase(type.trim()))
                    .collect(Collectors.toList());
        } else {
            result = new ArrayList<>(all);
        }
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("count", result.size());
        if (type != null && !type.isBlank()) response.put("filter", Map.of("type", type));
        response.put("sensors", result);
        response.put("_links", Map.of(
            "self",   linkObj("http://localhost:8080/api/v1/sensors", "GET"),
            "create", linkObj("http://localhost:8080/api/v1/sensors", "POST")
        ));
        return Response.ok(response).build();
    }

    @POST
    public Response createSensor(Sensor sensor) {
        if (sensor == null || sensor.getId() == null || sensor.getId().isBlank())
            return errorResponse(400, "MISSING_FIELD", "Sensor 'id' is required.");
        if (sensor.getType() == null || sensor.getType().isBlank())
            return errorResponse(400, "MISSING_FIELD", "Sensor 'type' is required.");
        if (sensor.getStatus() == null || sensor.getStatus().isBlank())
            return errorResponse(400, "MISSING_FIELD", "Sensor 'status' is required.");
        if (!VALID_STATUSES.contains(sensor.getStatus().toUpperCase()))
            return errorResponse(400, "INVALID_FIELD",
                "Sensor 'status' must be one of: ACTIVE, MAINTENANCE, OFFLINE.");
        if (sensor.getRoomId() == null || sensor.getRoomId().isBlank())
            return errorResponse(400, "MISSING_FIELD", "Sensor 'roomId' is required.");
        if (store.getSensors().containsKey(sensor.getId()))
            return errorResponse(409, "DUPLICATE_ID",
                "A sensor with ID '" + sensor.getId() + "' already exists.");

        // Referential integrity check — throws LinkedResourceNotFoundException → 422
        Room room = store.getRooms().get(sensor.getRoomId());
        if (room == null) {
            throw new LinkedResourceNotFoundException("Room", sensor.getRoomId());
        }

        sensor.setStatus(sensor.getStatus().toUpperCase());
        store.getSensors().put(sensor.getId(), sensor);
        room.getSensorIds().add(sensor.getId());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Sensor registered successfully.");
        response.put("sensor", sensor);
        response.put("_links", Map.of(
            "self",   linkObj("http://localhost:8080/api/v1/sensors/" + sensor.getId(), "GET"),
            "room",   linkObj("http://localhost:8080/api/v1/rooms/" + sensor.getRoomId(), "GET"),
            "all",    linkObj("http://localhost:8080/api/v1/sensors", "GET")
        ));
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @GET
    @Path("/{sensorId}")
    public Response getSensorById(@PathParam("sensorId") String sensorId) {
        Sensor sensor = store.getSensors().get(sensorId);
        if (sensor == null)
            return errorResponse(404, "SENSOR_NOT_FOUND",
                "No sensor found with ID '" + sensorId + "'.");

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("sensor", sensor);
        response.put("_links", Map.of(
            "self",     linkObj("http://localhost:8080/api/v1/sensors/" + sensorId, "GET"),
            "room",     linkObj("http://localhost:8080/api/v1/rooms/" + sensor.getRoomId(), "GET"),
            "readings", linkObj("http://localhost:8080/api/v1/sensors/" + sensorId + "/readings", "GET"),
            "all",      linkObj("http://localhost:8080/api/v1/sensors", "GET")
        ));
        return Response.ok(response).build();
    }

    // Sub-resource locator — no HTTP method annotation
    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingResource(@PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId);
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
