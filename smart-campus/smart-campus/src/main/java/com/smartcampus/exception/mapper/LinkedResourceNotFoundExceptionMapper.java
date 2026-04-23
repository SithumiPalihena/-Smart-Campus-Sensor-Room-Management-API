package com.smartcampus.exception.mapper;

import com.smartcampus.exception.LinkedResourceNotFoundException;

import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.LinkedHashMap;
import java.util.Map;


@Provider
public class LinkedResourceNotFoundExceptionMapper
        implements ExceptionMapper<LinkedResourceNotFoundException> {

    @Override
    public Response toResponse(LinkedResourceNotFoundException ex) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status",       422);
        body.put("error",        "LINKED_RESOURCE_NOT_FOUND");
        body.put("message",      ex.getMessage());
        body.put("resourceType", ex.getResourceType());
        body.put("resourceId",   ex.getResourceId());
        body.put("hint",         "Ensure the referenced " + ex.getResourceType()
                + " exists before creating a resource that depends on it.");

        // 422 Unprocessable Entity — not directly in JAX-RS 2.x Status enum,
        // so we supply the numeric code manually.
        return Response.status(422)
                .type(MediaType.APPLICATION_JSON)
                .entity(body)
                .build();
    }
}
