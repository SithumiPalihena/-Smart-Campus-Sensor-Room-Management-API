package com.smartcampus.filter;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * API Observability Filter.
 *
 * Implements BOTH ContainerRequestFilter and ContainerResponseFilter in a
 * single class so that every HTTP interaction is logged in one place.
 *
 * Logged on every REQUEST:
 *   [REQUEST]  POST  /api/v1/sensors
 *
 * Logged on every RESPONSE:
 *   [RESPONSE] POST  /api/v1/sensors  →  201
 *
 * @Provider ensures Jersey discovers and registers this filter automatically
 * via package scanning — no manual wiring required.
 */
@Provider
public class ApiLoggingFilter
        implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOGGER =
            Logger.getLogger(ApiLoggingFilter.class.getName());

    // ── Incoming request ──────────────────────────────────────────────────────

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String method = requestContext.getMethod();
        String uri    = requestContext.getUriInfo().getRequestUri().toString();

        LOGGER.info(String.format("[REQUEST]  %-7s %s", method, uri));
    }

    // ── Outgoing response ─────────────────────────────────────────────────────

    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) throws IOException {

        String method     = requestContext.getMethod();
        String uri        = requestContext.getUriInfo().getRequestUri().toString();
        int    statusCode = responseContext.getStatus();

        // Choose log level based on status: WARN for 4xx/5xx, INFO for 2xx/3xx
        String level = (statusCode >= 400) ? "WARN" : "INFO";

        String logLine = String.format("[RESPONSE] %-7s %s  →  %d", method, uri, statusCode);

        if (statusCode >= 400) {
            LOGGER.warning(logLine);
        } else {
            LOGGER.info(logLine);
        }
    }
}
