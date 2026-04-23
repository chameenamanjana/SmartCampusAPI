/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.exception;

import com.example.model.ErrorMessage;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 *
 * @author chameen-amanjana
 */
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {
    @Override
    public Response toResponse(Throwable exception) {
        // Pass WebApplicationExceptions through (e.g. 404 NotFoundException)
        // so they return their own status codes correctly
        if (exception instanceof WebApplicationException) {
            return ((WebApplicationException) exception).getResponse();
        }
        // All other unexpected errors → 500 with no stack trace exposed
        ErrorMessage error = new ErrorMessage(
            "An unexpected internal server error occurred. Please contact support.",
            500,
            "https://smartcampus.edu/api/docs/errors#internal"
        );
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
    }
}
