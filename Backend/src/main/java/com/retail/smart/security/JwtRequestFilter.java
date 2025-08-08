package com.retail.smart.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private static final String AUTH_HEADER_PREFIX = "Bearer ";

    @Autowired
    private AuthenticationJwt jwtUtil;

    public JwtRequestFilter() {}

    public JwtRequestFilter(AuthenticationJwt jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    void setJwtUtil(AuthenticationJwt jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();
        System.out.println("=== Incoming request to: " + uri);
        System.out.println("Authorization Header: " + request.getHeader("Authorization"));

        // Allow only open endpoints to pass through without token
        if (uri.startsWith("/api/auth") || uri.equals("/ping") || uri.startsWith("/reports/")) {
            System.out.println(" Public route, skipping JWT validation");
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith(AUTH_HEADER_PREFIX)) {
            System.out.println("❌ Missing or invalid Authorization header");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String token = authHeader.substring(AUTH_HEADER_PREFIX.length());

        try {
            var claims = jwtUtil.getClaimsFromToken(token);
            String username = claims.getSubject();
            Date expiration = claims.getExpiration();

            System.out.println("Token for user: " + username);
            System.out.println("Expiration: " + expiration);

            if (username == null || expiration.before(new Date())) {
                System.out.println(" Invalid or expired token");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            List<String> roles = claims.get("roles", List.class);
            System.out.println("Roles from token: " + roles);

            var authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority(
                    role.startsWith("ROLE_") ? role : "ROLE_" + role
                ))
                .collect(Collectors.toList());

            var authentication = new UsernamePasswordAuthenticationToken(username, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            System.out.println(" Authenticated user: " + username);

        } catch (Exception e) {
            System.out.println(" Exception while validating token: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
