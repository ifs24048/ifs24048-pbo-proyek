package com.bakery;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BakeryApplicationTest {

    @Test
    void contextLoads() {
        // Test jika konteks Spring Boot berhasil dimuat
        assertTrue(true);
    }

    @Test
    void mainMethodStartsApplication() {
        // Test method main
        BakeryApplication.main(new String[] {});
        assertTrue(true);
    }
}