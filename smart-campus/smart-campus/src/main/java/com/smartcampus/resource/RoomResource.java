package com.smartcampus.resource;

import com.smartcampus.data.DataStore;
import com.smartcampus.exception.RoomNotEmptyException;
import com.smartcampus.model.Room;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

/**
 * Room Resource — manages the /api/v1/rooms path.
 *
 * Endpoints:
 *   GET    /api/v1/rooms          — list all rooms
 *   POST   /api/v1/rooms          — create a new room
 *   GET    /api/v1/rooms/{roomId} — get a specific room
 *   DELETE /api/v1/rooms/{roomId} — delete a room (throws RoomNotEmptyException if sensors exist)
 */
@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    private final DataStore store = DataStore.getInstance();

    @GET
    public Response getAllRooms() {
        Collection<Room> rooms = store.getRooms().values();
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("count", rooms.size());
        response.put("rooms", rooms);
        response.put("_links", Map.of(
            "self",   linkObj("http://localhost:8080/api/v1/rooms", "GET"),
            "create", linkObj("http://localhost:8080/api/v1/rooms", "POST")
        ));
        return Response.ok(response).build();
    }

    @POST
    public Response createRoom(Room room) {
        if (room == null || room.getId() == null || room.getId().isBlank())
            return errorResponse(400, "MISSING_FIELD", "Room 'id' is required.");
        if (room.getName() == null || room.getName().isBlank())
            return errorResponse(400, "MISSING_FIELD", "Room 'name' is required.");
        if (room.getCapacity() <= 0)
            return errorResponse(400, "INVALID_FIELD", "Room 'capacity' must be a positive integer.");
        if (store.getRooms().containsKey(room.getId()))
            return errorResponse(409, "DUPLICATE_ID",
                "A room with ID '" + room.getId() + "' already exists.");
        if (room.getSensorIds() == null) room.setSensorIds(new ArrayList<>());

        store.getRooms().put(room.getId(), room);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Room created successfully.");
        response.put("room", room);
        response.put("_links", Map.of(
            "self",   linkObj("http://localhost:8080/api/v1/rooms/" + room.getId(), "GET"),
            "delete", linkObj("http://localhost:8080/api/v1/rooms/" + room.getId(), "DELETE"),
            "all",    linkObj("http://localhost:8080/api/v1/rooms", "GET")
        ));
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @GET
    @Path("/{roomId}")
    public Response getRoomById(@PathParam("roomId") String roomId) {
        Room room = store.getRooms().get(roomId);
        if (room == null)
            return errorResponse(404, "ROOM_NOT_FOUND", "No room found with ID '" + roomId + "'.");

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("room", room);
        response.put("_links", Map.of(
            "self",    linkObj("http://localhost:8080/api/v1/rooms/" + roomId, "GET"),
            "sensors", linkObj("http://localhost:8080/api/v1/rooms/" + roomId + "/sensors", "GET"),
            "delete",  linkObj("http://localhost:8080/api/v1/rooms/" + roomId, "DELETE"),
            "all",     linkObj("http://localhost:8080/api/v1/rooms", "GET")
        ));
        return Response.ok(response).build();
    }

    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = store.getRooms().get(roomId);
        if (room == null)
            return errorResponse(404, "ROOM_NOT_FOUND",
                "No room found with ID '" + roomId + "'. It may have already been deleted.");

        // Collect sensors still assigned to this room
        List<String> activeSensors = new ArrayList<>();
        for (String sensorId : room.getSensorIds()) {
            if (store.getSensors().containsKey(sensorId)) activeSensors.add(sensorId);
        }

        // Throw custom exception — handled by RoomNotEmptyExceptionMapper → 409
        if (!activeSensors.isEmpty()) {
            throw new RoomNotEmptyException(roomId, activeSensors);
        }

        store.getRooms().remove(roomId);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Room '" + roomId + "' has been successfully deleted.");
        response.put("_links", Map.of(
            "all", linkObj("http://localhost:8080/api/v1/rooms", "GET")
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
