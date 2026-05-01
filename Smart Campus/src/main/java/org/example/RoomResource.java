package org.example;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Path("/rooms")
public class RoomResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllRooms() {
        List<Room> rooms = new ArrayList<>(InMemoryStore.ROOMS.values());
        return Response.ok(rooms).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createRoom(Room room) {
        if (room == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Room payload is required\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        String id = (room.getId() == null || room.getId().isBlank())
                ? UUID.randomUUID().toString()
                : room.getId();
        room.setId(id);

        InMemoryStore.ROOMS.put(id, room);

        return Response.created(URI.create("/api/v1/rooms/" + id))
                .entity(room)
                .build();
    }

    @GET
    @Path("/{roomId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRoomById(@PathParam("roomId") String roomId) {
        Room room = InMemoryStore.ROOMS.get(roomId);
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Room not found\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
        return Response.ok(room).build();
    }

    @DELETE
    @Path("/{roomId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = InMemoryStore.ROOMS.get(roomId);
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Room not found\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        if (roomHasSensors(roomId)) {
            throw new RoomNotEmptyException();
        }

        InMemoryStore.ROOMS.remove(roomId);
        return Response.ok().build();
    }

    private boolean roomHasSensors(String roomId) {
        Room room = InMemoryStore.ROOMS.get(roomId);
        return room != null
                && room.getSensorIds() != null
                && !room.getSensorIds().isEmpty();
    }
}

