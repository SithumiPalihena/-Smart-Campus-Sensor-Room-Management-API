package com.smartcampus.resource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.LinkedHashMap;
import java.util.Map;
@Path("/")           // Mapped to the root of @ApplicationPath("/api/v1")
@Produces(MediaType.APPLICATION_JSON)
public class DiscoveryResource {
    private static final String BASE = "http://localhost:8080/api/v1";
    @GET
    public Response discover() {
        // ── Version info ──────────────────────────────────────────────────────
        Map<String, Object> version = new LinkedHashMap<>();
        version.put("major", 1);
        version.put("minor", 0);
        version.put("patch", 0);
        version.put("releaseDate", "2025-01-01");
        // ── Admin contact ─────────────────────────────────────────────────────
        Map<String, String> contact = new LinkedHashMap<>();
        contact.put("team",  "Smart Campus Infrastructure Team");
        contact.put("email", "smartcampus-api@university.ac.uk");
        contact.put("docs",  "https://campus-docs.university.ac.uk/api/v1");
        // ── HATEOAS links — primary resource collections ──────────────────────
        // Each entry gives clients a self-describing path they can call directly.
        Map<String, Object> links = new LinkedHashMap<>();
        links.put("self",    buildLink(BASE,               "GET", "API root / discovery"));
        links.put("rooms",   buildLink(BASE + "/rooms",    "GET", "List all campus rooms"));
        links.put("sensors", buildLink(BASE + "/sensors",  "GET", "List all sensors"));
        // ── Assemble full response body ───────────────────────────────────────
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("api",         "Smart Campus Sensor & Room Management API");
        body.put("description", "RESTful API for managing campus rooms and IoT sensor devices.");
        body.put("version",     version);
        body.put("contact",     contact);
        body.put("_links",      links);   // _links is the conventional HATEOAS key
        return Response.ok(body).build();
        
        
    }
    // ── Helper: build a HATEOAS link object ───────────────────────────────────
    private Map<String, String> buildLink(String href, String method, String description) {
        Map<String, String> link = new LinkedHashMap<>();
        link.put("href",        href);
        link.put("method",      method);
        link.put("description", description);
        return link;
    }
}
  
