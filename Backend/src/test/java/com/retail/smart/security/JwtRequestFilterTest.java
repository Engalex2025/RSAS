package com.retail.smart.security;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockFilterChain;

import javax.crypto.SecretKey;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtRequestFilterTest {

    private JwtRequestFilter jwtRequestFilter;
    private AuthenticationJwt jwtUtil;

    private SecretKey testSecretKey;

    @BeforeEach
    void setUp() {
        // Generate a secure key for testing
        testSecretKey = Keys.secretKeyFor(SignatureAlgorithm.HS512);

        jwtUtil = Mockito.spy(new AuthenticationJwt());
        jwtUtil.setSecretKey(testSecretKey); // Inject secure test key

        jwtRequestFilter = new JwtRequestFilter();
        jwtRequestFilter.setJwtUtil(jwtUtil); // Inject mocked JWT util
    }

    @Test
    void testAllowsPublicEndpoint() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/auth/login");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        jwtRequestFilter.doFilterInternal(request, response, chain);

        assertEquals(200, response.getStatus());
    }

    @Test
    void testBlocksMissingAuthorizationHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/protected");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        jwtRequestFilter.doFilterInternal(request, response, chain);

        assertEquals(401, response.getStatus());
    }

    @Test
    void testBlocksInvalidToken() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/protected");

        // Structurally valid token (must contain two dots)
        String fakeToken = "aaa.bbb.ccc";
        request.addHeader("Authorization", "Bearer " + fakeToken);

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        // Simulate parsed username and force validation to fail
        Mockito.doReturn("user").when(jwtUtil).getUsernameFromToken(fakeToken);
        Mockito.doReturn(false).when(jwtUtil).validateToken(fakeToken, "user");

        jwtRequestFilter.doFilterInternal(request, response, chain);

        assertEquals(401, response.getStatus());
    }

    @Test
    void testAllowsValidToken() throws Exception {
        String username = "alex";
        List<String> roles = List.of("ROLE_ADMIN");

        // Generate a valid token with secure key
        jwtUtil.setSecretKey(testSecretKey);
        String token = jwtUtil.generateToken(username, roles);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/protected");
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        jwtRequestFilter.doFilterInternal(request, response, chain);

        assertEquals(200, response.getStatus());
    }
}
