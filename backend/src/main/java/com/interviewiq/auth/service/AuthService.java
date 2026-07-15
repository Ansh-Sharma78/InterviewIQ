package com.interviewiq.auth.service;

import com.interviewiq.auth.dto.AuthResponse;
import com.interviewiq.auth.dto.LoginRequest;
import com.interviewiq.auth.dto.RegisterRequest;
import com.interviewiq.auth.dto.UpdateProfileRequest;
import com.interviewiq.auth.dto.UserProfileResponse;
import com.interviewiq.auth.entity.RefreshToken;
import com.interviewiq.auth.entity.User;
import com.interviewiq.auth.repository.RefreshTokenRepository;
import com.interviewiq.auth.repository.UserRepository;
import com.interviewiq.common.exception.DomainException;
import com.interviewiq.security.jwt.JwtProvider;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final Duration refreshTokenTtl;

    public AuthService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtProvider jwtProvider,
            @Value("${app.jwt.refresh-token-days}") long refreshTokenDays
    ) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
        this.refreshTokenTtl = Duration.ofDays(refreshTokenDays);
    }

    @Transactional
    public TokenPair register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new DomainException(HttpStatus.CONFLICT, "EMAIL_ALREADY_REGISTERED", "An account already exists for this email");
        }
        User user = userRepository.save(new User(email, passwordEncoder.encode(request.password()), request.fullName().trim()));
        return issueTokens(user);
    }

    @Transactional
    public TokenPair login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(normalizeEmail(request.email()))
                .filter(User::isActive)
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }
        return issueTokens(user);
    }

    @Transactional
    public TokenPair refresh(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw new DomainException(HttpStatus.UNAUTHORIZED, "REFRESH_TOKEN_MISSING", "Refresh token is missing");
        }
        String oldHash = sha256(rawRefreshToken);
        RefreshToken oldToken = refreshTokenRepository.findByTokenHash(oldHash)
                .orElseThrow(() -> new DomainException(HttpStatus.UNAUTHORIZED, "REFRESH_TOKEN_INVALID", "Refresh token is invalid"));

        User user = oldToken.getUser();
        if (!oldToken.isUsable()) {
            refreshTokenRepository.revokeAllActiveByUserId(user.getId());
            throw new DomainException(HttpStatus.UNAUTHORIZED, "REFRESH_TOKEN_REUSED", "Refresh token is no longer valid");
        }

        String newRawRefreshToken = generateRefreshToken();
        String newHash = sha256(newRawRefreshToken);
        oldToken.revoke(newHash);
        refreshTokenRepository.save(new RefreshToken(user, newHash, Instant.now().plus(refreshTokenTtl)));
        return new TokenPair(new AuthResponse(jwtProvider.createAccessToken(user), toProfile(user)), newRawRefreshToken);
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            return;
        }
        refreshTokenRepository.findByTokenHash(sha256(rawRefreshToken))
                .ifPresent(token -> token.revoke(null));
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(Long userId) {
        return userRepository.findById(userId)
                .map(this::toProfile)
                .orElseThrow(() -> new DomainException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "User not found"));
    }

    @Transactional
    public UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DomainException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "User not found"));
        user.updateProfile(
                request.fullName().trim(),
                blankToNull(request.targetRole()),
                blankToNull(request.experienceLevel()),
                blankToNull(request.targetCompanies())
        );
        return toProfile(user);
    }

    private TokenPair issueTokens(User user) {
        String rawRefreshToken = generateRefreshToken();
        refreshTokenRepository.save(new RefreshToken(user, sha256(rawRefreshToken), Instant.now().plus(refreshTokenTtl)));
        return new TokenPair(new AuthResponse(jwtProvider.createAccessToken(user), toProfile(user)), rawRefreshToken);
    }

    private UserProfileResponse toProfile(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getTargetRole(),
                user.getExperienceLevel(),
                user.getTargetCompanies(),
                user.getRole(),
                user.getCreatedAt()
        );
    }

    private String generateRefreshToken() {
        return UUID.randomUUID() + "." + UUID.randomUUID();
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }

    public record TokenPair(AuthResponse response, String refreshToken) {
    }
}

