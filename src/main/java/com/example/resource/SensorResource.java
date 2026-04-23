/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.resource;

import com.example.dao.MockDatabase;
import com.example.exception.LinkedResourceNotFoundException;
import com.example.model.Room;
import com.example.model.Sensor;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author chameen-amanjana
 */
@Path("/sensors")
public class SensorResource {

    // GET /api/v1/sensors or GET /api/v1/sensors?type=CO2
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Sensor> getSensors(@QueryParam("type") String type) {
        List<Sensor> all = new ArrayList<>(MockDatabase.SENSORS.values());
        if (type != null && !type.trim().isEmpty()) {
            return all.stream()
                    .filter(s -> s.getType().equalsIgnoreCase(type))
                    .collect(Collectors.toList());
        }
        return all;
    }

    // GET /api/v1/sensors/{sensorId}
    @GET
    @Path("/{sensorId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Sensor getSensorById(@PathParam("sensorId") String sensorId) {
        Sensor sensor = MockDatabase.SENSORS.get(sensorId);
        if (sensor == null) {
            throw new NotFoundException("Sensor with ID '" + sensorId + "' was not found.");
        }
        return sensor;
    }

    // POST /api/v1/sensors — register a new sensor (validates roomId)
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createSensor(Sensor sensor) {
        if (sensor.getRoomId() == null || sensor.getRoomId().trim().isEmpty()) {
            throw new LinkedResourceNotFoundException("A roomId must be provided when registering a sensor.");
        }
        Room room = MockDatabase.ROOMS.get(sensor.getRoomId());
        if (room == null) {
            throw new LinkedResourceNotFoundException(
                "Room with ID '" + sensor.getRoomId() + "' does not exist. Cannot register sensor."
            );
        }
        MockDatabase.SENSORS.put(sensor.getId(), sensor);
        room.getSensorIds().add(sensor.getId());
        MockDatabase.SENSOR_READINGS.putIfAbsent(sensor.getId(), new ArrayList<>());

        URI location = UriBuilder.fromPath("/api/v1/sensors/{id}").build(sensor.getId());
        return Response.created(location).entity(sensor).build();
    }

    // Sub-Resource Locator — NO HTTP method annotation (this is what makes it a locator)
    @Path("{sensorId}/readings")
    public SensorReadingResource getReadingResource(@PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId);
    }
}
