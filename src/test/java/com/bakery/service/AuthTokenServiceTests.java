package com.bakery.service;

import com.bakery.entity.AuthToken;
import com.bakery.repository.AuthTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthTokenServiceTest {

    @Mock
    private AuthTokenRepository authTokenRepository;

    private AuthTokenService authTokenService;
    private UUID userId;
    private String token;
    private AuthToken mockAuthToken;

    @BeforeEach
    void setUp() {
        authTokenService = new AuthTokenService(authTokenRepository);
        userId = UUID.randomUUID();
        token = "test-token-123";
        
        mockAuthToken = new AuthToken();
        mockAuthToken.setId(UUID.randomUUID());
        mockAuthToken.setToken(token);
        mockAuthToken.setUserId(userId);
        mockAuthToken.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void testCreateToken() {
        when(authTokenRepository.save(any(AuthToken.class))).thenReturn(mockAuthToken);

        AuthToken result = authTokenService.createToken(userId, token);

        assertNotNull(result);
        assertEquals(token, result.getToken());
        assertEquals(userId, result.getUserId());
        assertNotNull(result.getCreatedAt());
        verify(authTokenRepository).save(any(AuthToken.class));
    }

    @Test
    void testFindByToken_Found() {
        when(authTokenRepository.findByToken(token)).thenReturn(Optional.of(mockAuthToken));

        Optional<AuthToken> result = authTokenService.findByToken(token);

        assertTrue(result.isPresent());
        assertEquals(mockAuthToken, result.get());
        verify(authTokenRepository).findByToken(token);
    }

    @Test
    void testFindByToken_NotFound() {
        when(authTokenRepository.findByToken(token)).thenReturn(Optional.empty());

        Optional<AuthToken> result = authTokenService.findByToken(token);

        assertFalse(result.isPresent());
        verify(authTokenRepository).findByToken(token);
    }

    @Test
    void testDeleteTokenByUserId() {
        doNothing().when(authTokenRepository).deleteByUserId(userId);

        authTokenService.deleteTokenByUserId(userId);

        verify(authTokenRepository).deleteByUserId(userId);
    }

    @Test
    void testDeleteToken_Found() {
        when(authTokenRepository.findByToken(token)).thenReturn(Optional.of(mockAuthToken));
        doNothing().when(authTokenRepository).delete(mockAuthToken);

        authTokenService.deleteToken(token);

        verify(authTokenRepository).findByToken(token);
        verify(authTokenRepository).delete(mockAuthToken);
    }

    @Test
    void testDeleteToken_NotFound() {
        when(authTokenRepository.findByToken(token)).thenReturn(Optional.empty());

        authTokenService.deleteToken(token);

        verify(authTokenRepository).findByToken(token);
        verify(authTokenRepository, never()).delete(any());
    }

    @Test
    void testIsValidToken_Valid() {
        mockAuthToken.setCreatedAt(LocalDateTime.now().minusHours(12)); // 12 hours ago
        
        when(authTokenRepository.findByToken(token)).thenReturn(Optional.of(mockAuthToken));

        boolean result = authTokenService.isValidToken(token);

        assertTrue(result);
        verify(authTokenRepository).findByToken(token);
    }

    @Test
    void testIsValidToken_Expired() {
        mockAuthToken.setCreatedAt(LocalDateTime.now().minusHours(25)); // 25 hours ago
        
        when(authTokenRepository.findByToken(token)).thenReturn(Optional.of(mockAuthToken));

        boolean result = authTokenService.isValidToken(token);

        assertFalse(result);
        verify(authTokenRepository).findByToken(token);
    }

    @Test
    void testIsValidToken_NotFound() {
        when(authTokenRepository.findByToken(token)).thenReturn(Optional.empty());

        boolean result = authTokenService.isValidToken(token);

        assertFalse(result);
        verify(authTokenRepository).findByToken(token);
    }
}