package org.example;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ApiSmokeTest extends JerseyTest {

    @Override
    protected Application configure() {
        return new ResourceConfig()
                .register(DiscoveryResource.class)
                .register(RoomResource.class)
                .register(SensorResource.class)
                .register(LoggingFilter.class)
                .register(RoomNotEmptyExceptionMapper.class)
                .register(LinkedResourceNotFoundExceptionMapper.class);
    }

    @Test
    public void discoveryEndpointReturnsVersionAndLinks() {
        try (Response res = target("/").request(MediaType.APPLICATION_JSON).get()) {
            assertEquals(200, res.getStatus());
            String body = res.readEntity(String.class);
            assertTrue(body.contains("\"version\""));
            assertTrue(body.contains("\"rooms\""));
            assertTrue(body.contains("\"sensors\""));
        }
    }

    @Test
    public void createRoomThenListRooms() {
        Room room = new Room();
        room.setName("Lab A");
        room.setCapacity(12);

        try (Response created = target("/rooms")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(room, MediaType.APPLICATION_JSON))) {
            assertEquals(201, created.getStatus());
        }

        try (Response list = target("/rooms").request(MediaType.APPLICATION_JSON).get()) {
            assertEquals(200, list.getStatus());
            String body = list.readEntity(String.class);
            assertTrue(body.contains("\"name\":\"Lab A\""));
        }
    }

    @Test
    public void registeringSensorForMissingRoomReturns422() {
        Sensor sensor = new Sensor();
        sensor.setRoomId("does-not-exist");
        sensor.setType("temp");

        try (Response res = target("/sensors")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(sensor, MediaType.APPLICATION_JSON))) {
            assertEquals(422, res.getStatus());
            assertEquals("The specified roomId does not exist.", res.readEntity(String.class));
        }
    }

    @Test
    public void deleteRoomWithSensorsReturns409() {
        // create room
        Room room = new Room();
        room.setName("Room With Sensors");
        room.setCapacity(1);
        Room createdRoom = target("/rooms")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(room, MediaType.APPLICATION_JSON), Room.class);

        // register sensor in that room
        Sensor sensor = new Sensor();
        sensor.setRoomId(createdRoom.getId());
        sensor.setType("motion");
        try (Response sensorRes = target("/sensors")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(sensor, MediaType.APPLICATION_JSON))) {
            assertEquals(201, sensorRes.getStatus());
        }

        // delete should conflict
        try (Response del = target("/rooms/" + createdRoom.getId()).request().delete()) {
            assertEquals(409, del.getStatus());
            assertEquals("Room has active sensors and cannot be deleted.", del.readEntity(String.class));
        }
    }

    @Test
    public void postingReadingToMaintenanceSensorReturns403() {
        // create room
        Room room = new Room();
        room.setName("Room For Maintenance Sensor");
        room.setCapacity(1);
        Room createdRoom = target("/rooms")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(room, MediaType.APPLICATION_JSON), Room.class);

        // register sensor in maintenance
        Sensor sensor = new Sensor();
        sensor.setRoomId(createdRoom.getId());
        sensor.setType("temp");
        sensor.setStatus("MAINTENANCE");
        Sensor createdSensor = target("/sensors")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(sensor, MediaType.APPLICATION_JSON), Sensor.class);

        SensorReading reading = new SensorReading();
        reading.setValue(21.5);
        reading.setUnit("C");

        try (Response res = target("/sensors/" + createdSensor.getId() + "/readings")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(reading, MediaType.APPLICATION_JSON))) {
            assertEquals(403, res.getStatus());
        }
    }
}

