package com.capg.gateway.filter;

import com.capg.gateway.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthFilter implements GlobalFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String path = exchange.getRequest().getURI().getPath();
        String method = exchange.getRequest().getMethod().name();

        // PUBLIC ENDPOINTS
        if (path.startsWith("/api/users/login") ||
            path.startsWith("/api/users/register") ||
            path.startsWith("/swagger-ui") ||
            path.startsWith("/v3/api-docs") ||
            path.contains("/v3/api-docs") ||
            path.startsWith("/webjars")) {
            return chain.filter(exchange);
        }

        //  AUTH HEADER VALIDATION
        String authHeader = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);

        if (!jwtUtil.validateToken(token)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        //  EXTRACT USER DETAILS
        String email = jwtUtil.extractEmail(token);
        String role = jwtUtil.extractRole(token);

        //  ROLE-BASED ACCESS CONTROL

        //  RECRUITER → can CREATE jobs only
        if (path.equals("/api/jobs") && method.equals("POST")
                && !role.equals("RECRUITER")) {
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            return exchange.getResponse().setComplete();
        }

        // 👤 JOB SEEKER → can APPLY only
        if (path.equals("/api/applications") && method.equals("POST")
                && !role.equals("JOB_SEEKER")) {
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            return exchange.getResponse().setComplete();
        }

        //  RECRUITER → view applicants
        if (path.startsWith("/api/applications/job") && !role.equals("RECRUITER")) {
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            return exchange.getResponse().setComplete();
        }

        //  JOB SEEKER → view own applications
        if (path.equals("/api/applications/me") && !role.equals("JOB_SEEKER")) {
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            return exchange.getResponse().setComplete();
        }

        //  ADMIN only endpoints (optional future use)
        if (path.startsWith("/api/admin") && !role.equals("ADMIN")) {
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            return exchange.getResponse().setComplete();
        }

        //  ADD HEADERS TO DOWNSTREAM
        ServerWebExchange modifiedExchange = exchange.mutate()
                .request(r -> r
                        .header("X-User-Email", email)
                        .header("X-User-Role", role)
                )
                .build();

        return chain.filter(modifiedExchange);
    }
}