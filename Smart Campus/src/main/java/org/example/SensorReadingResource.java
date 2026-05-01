package org.example;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Path("/{sensorId}/readings")
public class SensorReadingResource {

    private final String sensorId;

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllReadings() {
        if (!InMemoryStore.SENSORS.containsKey(sensorId)) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Sensor not found\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        List<SensorReading> readings = InMemoryStore.SENSOR_READINGS.get(sensorId);
        if (readings == null) {
            readings = List.of();
        }
        return Response.ok(readings).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addReading(SensorReading reading) {
        Sensor sensor = InMemoryStore.SENSORS.get(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Sensor not found\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
        if (sensor.getStatus() != null && sensor.getStatus().equalsIgnoreCase("MAINTENANCE")) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"error\":\"Sensor is in MAINTENANCE and cannot accept readings.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
        if (reading == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Reading payload is required\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        String id = (reading.getId() == null || reading.getId().isBlank())
                ? UUID.randomUUID().toString()
                : reading.getId();
        reading.setId(id);
        reading.setSensorId(sensorId);
        if (reading.getTimestampEpochMs() == 0L) {
            reading.setTimestampEpochMs(System.currentTimeMillis());
        }

        List<SensorReading> list = InMemoryStore.SENSOR_READINGS.computeIfAbsent(sensorId, ignored -> new ArrayList<>());
        list.add(reading);

        sensor.setCurrentValue(reading.getValue());

        return Response.created(URI.create("/api/v1/sensors/" + sensorId + "/readings/" + id))
                .entity(reading)
                .build();
    }
}

