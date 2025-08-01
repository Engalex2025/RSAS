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
        if (uri.startsWith("/api/auth") || uri.equals("/ping")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith(AUTH_HEADER_PREFIX)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String token = authHeader.substring(AUTH_HEADER_PREFIX.length());

        try {
            var claims = jwtUtil.getClaimsFromToken(token);
            String username = claims.getSubject();
            Date expiration = claims.getExpiration();

            if (username == null || expiration.before(new Date())) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            List<String> roles = claims.get("roles", List.class);
            var authorities = roles.stream()
                .map(SimpleGrantedAuthority::new) 
                .collect(Collectors.toList());

            var authentication = new UsernamePasswordAuthenticationToken(username, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
