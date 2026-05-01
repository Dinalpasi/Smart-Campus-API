package org.example;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Path("/sensors")
public class SensorResource {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerSensor(Sensor sensor) {
        if (sensor == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Sensor payload is required\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
        if (sensor.getRoomId() == null || sensor.getRoomId().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"roomId is required\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        Room room = InMemoryStore.ROOMS.get(sensor.getRoomId());
        if (room == null) {
            throw new LinkedResourceNotFoundException();
        }

        String id = (sensor.getId() == null || sensor.getId().isBlank())
                ? UUID.randomUUID().toString()
                : sensor.getId();
        sensor.setId(id);
        if (sensor.getStatus() == null || sensor.getStatus().isBlank()) {
            sensor.setStatus("ACTIVE");
        }

        InMemoryStore.SENSORS.put(id, sensor);

        if (room.getSensorIds() != null && !room.getSensorIds().contains(id)) {
            room.getSensorIds().add(id);
        }

        return Response.created(URI.create("/api/v1/sensors/" + id))
                .entity(sensor)
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllSensors(@QueryParam("type") String type) {
        List<Sensor> sensors = new ArrayList<>(InMemoryStore.SENSORS.values());
        if (type == null || type.isBlank()) {
            return Response.ok(sensors).build();
        }

        String normalized = type.trim();
        List<Sensor> filtered = new ArrayList<>();
        for (Sensor sensor : sensors) {
            if (sensor != null && sensor.getType() != null && sensor.getType().equalsIgnoreCase(normalized)) {
                filtered.add(sensor);
            }
        }
        return Response.ok(filtered).build();
    }

    @Path("/{sensorId}/readings")
    public SensorReadingResource getSensorReadings(@PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId);
    }
}

