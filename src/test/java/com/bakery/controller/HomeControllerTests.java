package com.bakery.controller;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HomeControllerTest {

    @Test
    void testHome() {
        HomeController controller = new HomeController();
        String result = controller.home();
        assertEquals("redirect:/dashboard", result);
    }

    @Test
    void testAbout() {
        HomeController controller = new HomeController();
        String result = controller.about();
        assertEquals("index", result);
    }
}