package com.retail.smart.gateway;

import com.retail.smart.dto.AuthRequest;
import com.retail.smart.security.AuthenticationJwt;
import com.retail.smart.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationJwt jwtUtil;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
public ResponseEntity<?> login(@RequestBody AuthRequest authRequest) {
    System.out.println("Trying login with: " + authRequest.getUsername() + " / " + authRequest.getPassword());

    UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.getUsername());

    System.out.println("User found: " + userDetails.getUsername());
    System.out.println("Encoded password in DB: " + userDetails.getPassword());
    System.out.println("Password matches: " + passwordEncoder.matches(authRequest.getPassword(), userDetails.getPassword()));

    if (!passwordEncoder.matches(authRequest.getPassword(), userDetails.getPassword())) {
        System.out.println("Password mismatch");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
    }

    List<String> roles = userDetails.getAuthorities()
            .stream()
            .map(auth -> auth.getAuthority())
            .collect(Collectors.toList());

    String token = jwtUtil.generateToken(userDetails.getUsername(), roles);
    return ResponseEntity.ok(Collections.singletonMap("token", token));
}

}