package com.backend.coapp.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.backend.coapp.config.TestSecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

@Import(TestSecurityConfig.class)
public class WelcomeControllerTest {
  private WelcomeController welcomeController;

  @BeforeEach
  public void setUp() {
    this.welcomeController = new WelcomeController();
  }

  @Test
  public void welcome_expectWelcomeMessage() {
    assertEquals("CoApp is Running", this.welcomeController.welcome());
  }
}
