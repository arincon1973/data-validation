package com.example.datavalidator;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String requestId = UUID.randomUUID().toString();
        long start = System.currentTimeMillis();
        // Available to any other log statement made while handling this request.
        MDC.put("requestId", requestId);
        log.info("Request {} started: {} {}", requestId, request.getMethod(), request.getRequestURI());
        try {
            chain.doFilter(request, response);
        } finally {
            log.info("Request {} finished: status {} in {} ms",
                    requestId, response.getStatus(), System.currentTimeMillis() - start);
            MDC.remove("requestId");
        }
    }
}
