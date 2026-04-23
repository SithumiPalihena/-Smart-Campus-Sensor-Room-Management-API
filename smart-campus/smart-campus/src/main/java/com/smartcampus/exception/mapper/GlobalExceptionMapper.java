package com.smartcampus.exception.mapper;

import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Global "safety net" ExceptionMapper catches ALL unhandled Throwables.
 *
 * This ensures the API NEVER leaks a raw Java stack trace or a default
 * server error page to external consumers. Any unexpected runtime error
 * (NullPointerException, IndexOutOfBoundsException, etc.) is intercepted
 * here and returned as a clean, generic HTTP 500 JSON response.
 *
 * The full stack trace IS logged server-side (using java.util.logging) so
 * engineers can diagnose issues without exposing internals to clients.
 *
 * SECURITY NOTE (see report for full discussion):
 * Exposing stack traces reveals class names, package structure, library
 * versions, and file paths all of which aid an attacker in crafting
 * targeted exploits. This mapper prevents that information leakage entirely.
 */
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOGGER =
            Logger.getLogger(GlobalExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable ex) {

        // Log the full details SERVER-SIDE only — never sent to the client
        LOGGER.log(Level.SEVERE,
                "Unhandled exception caught by GlobalExceptionMapper: "
                + ex.getMessage(), ex);

        // Return a safe, generic error body to the client
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status",  500);
        body.put("error",   "INTERNAL_SERVER_ERROR");
        body.put("message", "An unexpected error occurred on the server. "
                + "The issue has been logged. Please contact the API administrator.");

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON)
                .entity(body)
                .build();
    }
}
