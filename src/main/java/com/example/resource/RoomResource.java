/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.resource;

import com.example.dao.MockDatabase;
import com.example.exception.RoomNotEmptyException;
import com.example.model.Room;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author chameen-amanjana
 */
@Path("/rooms")
public class RoomResource {

    // GET /api/v1/rooms — list all rooms
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Room> getAllRooms() {
        return new ArrayList<>(MockDatabase.ROOMS.values());
    }

    // POST /api/v1/rooms — create a new room
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createRoom(Room room) {
        if (room.getId() == null || room.getId().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Room ID is required.\"}")
                    .build();
        }
        MockDatabase.ROOMS.put(room.getId(), room);
        URI location = UriBuilder.fromPath("/api/v1/rooms/{id}").build(room.getId());
        return Response.created(location).entity(room).build();
    }

    // GET /api/v1/rooms/{roomId} — get one room by ID
    @GET
    @Path("/{roomId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Room getRoomById(@PathParam("roomId") String roomId) {
        Room room = MockDatabase.ROOMS.get(roomId);
        if (room == null) {
            throw new NotFoundException("Room with ID '" + roomId + "' was not found.");
        }
        return room;
    }

    // DELETE /api/v1/rooms/{roomId} — delete a room (blocked if sensors present)
    @DELETE
    @Path("/{roomId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = MockDatabase.ROOMS.get(roomId);
        if (room == null) {
            // Idempotent behaviour: already deleted, return success
            return Response.noContent().build();
        }
        if (!room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException(
                "Room '" + roomId + "' cannot be deleted. It still has " +
                room.getSensorIds().size() + " active sensor(s) assigned to it."
            );
        }
        MockDatabase.ROOMS.remove(roomId);
        return Response.noContent().build();
    }
}
