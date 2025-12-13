package com.bakery.service;

import com.bakery.entity.AuthToken;
import com.bakery.repository.AuthTokenRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthTokenService {
    
    private final AuthTokenRepository authTokenRepository;
    
    public AuthTokenService(AuthTokenRepository authTokenRepository) {
        this.authTokenRepository = authTokenRepository;
    }
    
    public AuthToken createToken(UUID userId, String token) {
        AuthToken authToken = new AuthToken();
        authToken.setUserId(userId);
        authToken.setToken(token);
        authToken.setCreatedAt(LocalDateTime.now());
        return authTokenRepository.save(authToken);
    }
    
    public Optional<AuthToken> findByToken(String token) {
        return authTokenRepository.findByToken(token);
    }
    
    public void deleteTokenByUserId(UUID userId) {
        authTokenRepository.deleteByUserId(userId);
    }
    
    public void deleteToken(String token) {
        authTokenRepository.findByToken(token).ifPresent(authTokenRepository::delete);
    }
    
    public boolean isValidToken(String token) {
        Optional<AuthToken> authToken = authTokenRepository.findByToken(token);
        if (authToken.isPresent()) {
            // Check if token is not expired (e.g., 24 hours)
            LocalDateTime createdAt = authToken.get().getCreatedAt();
            LocalDateTime expiryTime = createdAt.plusHours(24);
            return LocalDateTime.now().isBefore(expiryTime);
        }
        return false;
    }
}