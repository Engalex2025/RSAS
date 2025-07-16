package com.retail.smart.gateway;

import com.retail.smart.dto.AuthRequest;
import com.retail.smart.dto.RegisterRequestDTO;
import com.retail.smart.model.User;
import com.retail.smart.repository.UserRepository;
import com.retail.smart.security.AuthenticationJwt;
import com.retail.smart.service.UserDetailsServiceImpl;

import jakarta.annotation.PostConstruct;

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
    @PostConstruct
public void init() {
    System.out.println("🚀 AuthController initialized!");
}


    @Autowired
    private AuthenticationJwt jwtUtil;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequestDTO request) {
        System.out.println("=== REGISTER ENDPOINT ===");

        if (userRepository.existsByUsername(request.getUsername())) {
            System.out.println("Username already exists");
            return ResponseEntity.badRequest().body("Username already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        String role = request.getRole();
        if (!role.startsWith("ROLE_")) {
            role = "ROLE_" + role;
        }
        user.setRole(role);

        userRepository.save(user);

        System.out.println("User saved: " + user.getUsername());
        return ResponseEntity.ok("User registered successfully");
    }



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