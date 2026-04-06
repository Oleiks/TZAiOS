package org.example.library.dto.auth;

public record AuthResponse(String accessToken, String refreshToken, UserDto user) {}
