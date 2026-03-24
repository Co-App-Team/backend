package com.backend.coapp.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WelcomeControllerTest {
  private WelcomeController welcomeController;

  @BeforeEach
  void setUp() {
    this.welcomeController = new WelcomeController();
  }

  @Test
  void welcome_expectWelcomeMessage() {
    assertEquals("CoApp is Running", this.welcomeController.welcome());
  }
}
