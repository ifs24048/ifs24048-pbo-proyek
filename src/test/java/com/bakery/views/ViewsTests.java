package com.bakery.views;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ViewsTests {

    @Test
    void testAuthViewInstantiations() {
        assertNotNull(new AuthView());

        AuthView.LoginView loginView = new AuthView.LoginView();
        assertNotNull(loginView);
        assertEquals("Login - Mimi's Bakery", AuthView.LoginView.PAGE_TITLE);

        AuthView.RegisterView registerView = new AuthView.RegisterView();
        assertNotNull(registerView);
        assertEquals("Register - Mimi's Bakery", AuthView.RegisterView.PAGE_TITLE);
    }

    @Test
    void testHomeViewInstantiations() {
        assertNotNull(new HomeView());
        assertNotNull(new HomeView.DashboardView());
        assertNotNull(new HomeView.IndexView());
    }

    @Test
    void testMenuViewInstantiations() {
        assertNotNull(new MenuView());
        assertNotNull(new MenuView.Navigation());
        assertNotNull(new MenuView.Breadcrumb());
    }

    @Test
    void testProductViewInstantiations() {
        assertNotNull(new ProductView());
        assertNotNull(new ProductView.ListView());
        assertNotNull(new ProductView.CreateView());
        assertNotNull(new ProductView.EditView());
        assertNotNull(new ProductView.DetailView());
    }
}
