/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author chameen-amanjana
 */
@Path("/")
public class DiscoveryResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> discover(@QueryParam("triggerError") boolean triggerError) {
        // Allows triggering a 500 error in Postman to demonstrate the global exception mapper
        if (triggerError) {
            throw new NullPointerException("Deliberate error to demonstrate global safety net.");
        }

        Map<String, Object> response = new HashMap<>();
        response.put("apiVersion", "v1");
        response.put("description", "Smart Campus Sensor & Room Management API");
        response.put("contact", "admin@smartcampus.edu");
        response.put("status", "operational");

        Map<String, String> resources = new HashMap<>();
        resources.put("rooms", "/api/v1/rooms");
        resources.put("sensors", "/api/v1/sensors");
        response.put("resources", resources);

        return response;
    }
}
