package com.interviewiq.auth.controller;

import com.interviewiq.auth.dto.AuthResponse;
import com.interviewiq.auth.dto.LoginRequest;
import com.interviewiq.auth.dto.RegisterRequest;
import com.interviewiq.auth.dto.UpdateProfileRequest;
import com.interviewiq.auth.dto.UserProfileResponse;
import com.interviewiq.auth.service.AuthService;
import com.interviewiq.auth.service.AuthService.TokenPair;
import com.interviewiq.security.userdetails.AuthenticatedUser;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private static final String REFRESH_COOKIE = "interviewiq_refresh";

    private final AuthService authService;
    private final boolean secureCookie;
    private final long refreshTokenDays;

    public AuthController(
            AuthService authService,
            @Value("${app.cookies.secure}") boolean secureCookie,
            @Value("${app.jwt.refresh-token-days}") long refreshTokenDays
    ) {
        this.authService = authService;
        this.secureCookie = secureCookie;
        this.refreshTokenDays = refreshTokenDays;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request, HttpServletResponse response) {
        TokenPair tokenPair = authService.register(request);
        addRefreshCookie(response, tokenPair.refreshToken());
        return ResponseEntity.status(201).body(tokenPair.response());
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        TokenPair tokenPair = authService.login(request);
        addRefreshCookie(response, tokenPair.refreshToken());
        return tokenPair.response();
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(
            @CookieValue(name = REFRESH_COOKIE, required = false) String refreshToken,
            HttpServletResponse response
    ) {
        TokenPair tokenPair = authService.refresh(refreshToken);
        addRefreshCookie(response, tokenPair.refreshToken());
        return tokenPair.response();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = REFRESH_COOKIE, required = false) String refreshToken,
            HttpServletResponse response
    ) {
        authService.logout(refreshToken);
        clearRefreshCookie(response);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public UserProfileResponse me(@AuthenticationPrincipal AuthenticatedUser user) {
        return authService.getProfile(user.id());
    }

    @PatchMapping("/me")
    public UserProfileResponse updateMe(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        return authService.updateProfile(user.id(), request);
    }

    private void addRefreshCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_COOKIE, refreshToken)
                .httpOnly(true)
                .secure(secureCookie)
                .sameSite("Strict")
                .path("/api/v1/auth")
                .maxAge(Duration.ofDays(refreshTokenDays))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearRefreshCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_COOKIE, "")
                .httpOnly(true)
                .secure(secureCookie)
                .sameSite("Strict")
                .path("/api/v1/auth")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}

