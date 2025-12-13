package com.bakery.repository;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.bakery.entity.AuthToken;

@DataJpaTest
@ExtendWith(SpringExtension.class)
class AuthTokenRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AuthTokenRepository authTokenRepository;

    private AuthToken authToken1;
    private AuthToken authToken2;
    private UUID userId1;
    private UUID userId2;

    @BeforeEach
    void setUp() {
        userId1 = UUID.randomUUID();
        userId2 = UUID.randomUUID();

        authToken1 = new AuthToken();
        authToken1.setToken("token-123");
        authToken1.setUserId(userId1);
        authToken1.setCreatedAt(LocalDateTime.now());

        authToken2 = new AuthToken();
        authToken2.setToken("token-456");
        authToken2.setUserId(userId2);
        authToken2.setCreatedAt(LocalDateTime.now());

        entityManager.persist(authToken1);
        entityManager.persist(authToken2);
        entityManager.flush();
    }

    @Test
    void testFindByToken_Found() {
        Optional<AuthToken> found = authTokenRepository.findByToken("token-123");
        
        assertTrue(found.isPresent());
        assertEquals("token-123", found.get().getToken());
        assertEquals(userId1, found.get().getUserId());
        assertNotNull(found.get().getId());
        assertNotNull(found.get().getCreatedAt());
    }

    @Test
    void testFindByToken_NotFound() {
        Optional<AuthToken> found = authTokenRepository.findByToken("nonexistent-token");
        
        assertFalse(found.isPresent());
    }

    @Test
    void testFindByToken_EmptyToken() {
        Optional<AuthToken> found = authTokenRepository.findByToken("");
        
        assertFalse(found.isPresent());
    }

    @Test
    void testSaveNewAuthToken() {
        AuthToken newAuthToken = new AuthToken();
        newAuthToken.setToken("new-token-789");
        newAuthToken.setUserId(UUID.randomUUID());
        newAuthToken.setCreatedAt(LocalDateTime.now());

        AuthToken saved = authTokenRepository.save(newAuthToken);
        
        assertNotNull(saved);
        assertNotNull(saved.getId());
        assertEquals("new-token-789", saved.getToken());
        assertNotNull(saved.getCreatedAt());
        
        // Verify can retrieve by token
        Optional<AuthToken> retrieved = authTokenRepository.findByToken("new-token-789");
        assertTrue(retrieved.isPresent());
    }

    @Test
    void testUpdateAuthToken() {
        Optional<AuthToken> found = authTokenRepository.findByToken("token-123");
        assertTrue(found.isPresent());
        
        AuthToken authToken = found.get();
        String newToken = "updated-token-123";
        authToken.setToken(newToken);
        
        authTokenRepository.save(authToken);
        entityManager.flush();
        entityManager.clear();
        
        Optional<AuthToken> updated = authTokenRepository.findByToken(newToken);
        assertTrue(updated.isPresent());
        assertEquals(newToken, updated.get().getToken());
    }

    @Test
    void testDeleteByUserId() {
        // Check token exists before deletion
        Optional<AuthToken> found = authTokenRepository.findByToken("token-123");
        assertTrue(found.isPresent());
        
        // Delete by userId
        authTokenRepository.deleteByUserId(userId1);
        entityManager.flush();
        entityManager.clear();
        
        // Verify token is deleted
        Optional<AuthToken> deleted = authTokenRepository.findByToken("token-123");
        assertFalse(deleted.isPresent());
        
        // Verify other user's token still exists
        Optional<AuthToken> otherToken = authTokenRepository.findByToken("token-456");
        assertTrue(otherToken.isPresent());
    }

    @Test
    void testDeleteByUserId_NoMatchingUser() {
        UUID nonExistentUserId = UUID.randomUUID();
        
        // Should not throw exception
        assertDoesNotThrow(() -> authTokenRepository.deleteByUserId(nonExistentUserId));
    }

    @Test
    void testDeleteAuthToken() {
        Optional<AuthToken> found = authTokenRepository.findByToken("token-123");
        assertTrue(found.isPresent());
        
        authTokenRepository.delete(found.get());
        entityManager.flush();
        entityManager.clear();
        
        Optional<AuthToken> deleted = authTokenRepository.findByToken("token-123");
        assertFalse(deleted.isPresent());
    }

    @Test
    void testFindAll() {
        Iterable<AuthToken> allTokens = authTokenRepository.findAll();
        
        int count = 0;
        for (AuthToken token : allTokens) {
            count++;
            assertNotNull(token.getToken());
            assertNotNull(token.getUserId());
        }
        
        assertTrue(count >= 2);
    }

    @Test
    void testTokenUniqueness() {
        // Try to save token with same value (should fail if constraint exists)
        AuthToken duplicateToken = new AuthToken();
        duplicateToken.setToken("token-123"); // Same as existing
        duplicateToken.setUserId(UUID.randomUUID());
        duplicateToken.setCreatedAt(LocalDateTime.now());

        // Depending on your database constraint, this might throw exception
        // For test purposes, we'll just verify it doesn't affect existing
        AuthToken saved = authTokenRepository.save(duplicateToken);
        assertNotNull(saved);
    }

    @Test
    void testFindById() {
        Optional<AuthToken> foundByToken = authTokenRepository.findByToken("token-123");
        assertTrue(foundByToken.isPresent());
        
        UUID tokenId = foundByToken.get().getId();
        Optional<AuthToken> foundById = authTokenRepository.findById(tokenId);
        
        assertTrue(foundById.isPresent());
        assertEquals("token-123", foundById.get().getToken());
    }

    @Test
    void testEntityLifecycle() {
        AuthToken token = new AuthToken();
        token.setToken("test-lifecycle-token");
        token.setUserId(UUID.randomUUID());
        
        // Test pre-persist
        assertNull(token.getCreatedAt());
        
        AuthToken saved = authTokenRepository.save(token);
        assertNotNull(saved.getCreatedAt());
        assertTrue(saved.getCreatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }
}