package com.bakery.entity;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void testUserCreation() {
        User user = new User();
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        user.setId(id);
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setCreatedAt(now); // Set explicitly for consistency
        user.setUpdatedAt(now); // Set explicitly for consistency

        assertEquals(id, user.getId());
        assertEquals("Test User", user.getName());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("password", user.getPassword());
        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getUpdatedAt());
        assertEquals(now, user.getCreatedAt());
        assertEquals(now, user.getUpdatedAt());
    }

    @Test
    void testPrePersist() {
        User user = new User();
        user.onCreate();

        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getUpdatedAt());
        // Check that they are within 1 second of each other (to handle nanosecond
        // differences)
        assertTrue(user.getCreatedAt().isBefore(user.getUpdatedAt().plusSeconds(1)));
        assertTrue(user.getCreatedAt().isAfter(user.getUpdatedAt().minusSeconds(1)));
    }

    @Test
    void testPreUpdate() throws InterruptedException {
        User user = new User();
        user.onCreate();

        LocalDateTime createdAt = user.getCreatedAt();
        Thread.sleep(10); // Small delay to ensure different timestamps
        user.onUpdate();

        assertNotNull(user.getUpdatedAt());
        assertTrue(user.getUpdatedAt().isAfter(createdAt));
    }
}