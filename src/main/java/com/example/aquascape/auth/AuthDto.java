package com.example.aquascape.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

public class AuthDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SignUpRequest {
        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        private String email;

        @NotBlank(message = "Full name is required")
        @Size(min = 3, max = 50, message = "Full name must be between 3 and 50 characters")
        private String fullName;

        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        private String password;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequest {
        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        private String email;

        @NotBlank(message = "Password is required")
        private String password;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthResponse {
        private Long id;
        private String email;
        private String username;
        private String token;
        private LocalDateTime createdAt;
        private Boolean isActive;
        private String message;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorResponse {
        private String message;
        private Integer statusCode;
        private LocalDateTime timestamp;
    }
}
