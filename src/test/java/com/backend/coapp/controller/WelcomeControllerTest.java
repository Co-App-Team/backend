package com.backend.coapp.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WelcomeControllerTest {
    private WelcomeController welcomeController;

    @BeforeEach
    public void setUp(){
        this.welcomeController = new WelcomeController();
    }

    @Test
    public void testSpinUp(){
        assertEquals("CoApp is Running",this.welcomeController.welcome());
    }

}
