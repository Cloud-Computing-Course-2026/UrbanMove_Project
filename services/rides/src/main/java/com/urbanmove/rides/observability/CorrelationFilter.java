package com.urbanmove.rides.observability;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class CorrelationFilter extends OncePerRequestFilter {
    private static final Logger LOG = LoggerFactory.getLogger(CorrelationFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String id = request.getHeader("X-Correlation-ID");
        if (id == null || !id.matches("[A-Za-z0-9._:-]{1,128}")) {
            id = UUID.randomUUID().toString();
        }
        request.setAttribute("correlationId", id);
        response.setHeader("X-Correlation-ID", id);
        MDC.put("correlationId", id);
        try {
            chain.doFilter(request, response);
        } finally {
            LOG.atInfo().addKeyValue("method", request.getMethod())
                    .addKeyValue("status", response.getStatus()).log("http_request");
            MDC.remove("correlationId");
        }
    }
}
