package com.example.aquascape.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final AuthRepository authRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    /**
     * Register a new user
     */
    public AuthDto.AuthResponse signUp(AuthDto.SignUpRequest request) {

        // Check if email already exists
        if (authRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already registered");
        }

        // Check if full name already exists
        if (authRepository.existsByUsername(request.getFullName())) {
            throw new IllegalArgumentException("Full name is already taken");
        }

        // Create new user
        Auth user = Auth.builder()
                .email(request.getEmail())
                .username(request.getFullName())
                .password(passwordEncoder.encode(request.getPassword()))
                .isActive(true)
                .build();

        // Save user to database
        Auth savedUser = authRepository.save(user);

        // Generate JWT token
        String token = generateJwtToken(savedUser.getId(), savedUser.getEmail());

        return AuthDto.AuthResponse.builder()
                .id(savedUser.getId())
                .email(savedUser.getEmail())
                .username(savedUser.getUsername())
                .token(token)
                .createdAt(savedUser.getCreatedAt())
                .isActive(savedUser.getIsActive())
                .message("User registered successfully")
                .build();
    }

    /**
     * Login user with email/username and password
     */
    public AuthDto.AuthResponse login(AuthDto.LoginRequest request) {
        // Find user by email or full name
        Optional<Auth> userOptional = authRepository.findByEmail(
                request.getEmail());
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("Email/full name or password is incorrect");
        }

        Auth user = userOptional.get();

        // Check if account is active
        if (!user.getIsActive()) {
            throw new IllegalArgumentException("Your account has been deactivated");
        }

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Email/username or password is incorrect");
        }

        // Generate JWT token
        String token = generateJwtToken(user.getId(), user.getEmail());

        return AuthDto.AuthResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .token(token)
                .createdAt(user.getCreatedAt())
                .isActive(user.getIsActive())
                .message("Login successful")
                .build();
    }

    /**
     * Generate JWT token
     */
    private String generateJwtToken(Long userId, String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("email", email)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(
                        Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)),
                        SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * Validate JWT token
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get user ID from token
     */
    public Long getUserIdFromToken(String token) {
        return Long.valueOf(Jwts.parser()
                .setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject());
    }
}
