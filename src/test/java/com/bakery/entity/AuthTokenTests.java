package com.bakery.entity;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AuthTokenTest {

    @Test
    void testAuthTokenCreation() {
        AuthToken authToken = new AuthToken();
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        
        authToken.setId(id);
        authToken.setToken("test-token-123");
        authToken.setUserId(userId);
        
        assertEquals(id, authToken.getId());
        assertEquals("test-token-123", authToken.getToken());
        assertEquals(userId, authToken.getUserId());
        assertNull(authToken.getCreatedAt());
    }

    @Test
    void testPrePersist() {
        AuthToken authToken = new AuthToken();
        authToken.onCreate();
        
        assertNotNull(authToken.getCreatedAt());
        assertTrue(authToken.getCreatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }
}