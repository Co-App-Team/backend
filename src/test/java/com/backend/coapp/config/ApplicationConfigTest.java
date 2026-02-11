package com.backend.coapp.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backend.coapp.exception.AuthEmailNotRegisteredException;
import com.backend.coapp.model.document.UserModel;
import com.backend.coapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

@ExtendWith(MockitoExtension.class)
class ApplicationConfigTest {

  @Mock private UserRepository userRepository;

  private ApplicationConfig applicationConfig;
  private UserDetailsService userDetailsService;

  @BeforeEach
  void setUp() {
    applicationConfig = new ApplicationConfig(userRepository);
    userDetailsService = applicationConfig.userDetailsService();
  }

  @Test
  void userDetailsService_whenUserExist_expectNoException() {
    String userIdentity = "123";
    UserModel expectedUser =
        new UserModel(userIdentity, "foo@mail.com", "password123", "foo", "woof", false, 123);

    when(userRepository.findUserModelById(userIdentity)).thenReturn(expectedUser);

    UserDetails result = userDetailsService.loadUserByUsername(userIdentity);

    assertThat(result).isEqualTo(expectedUser);
    verify(userRepository).findUserModelById(userIdentity);
  }

  @Test
  void userDetailsService_whenUserNotExist_expectException() {
    String userIdentity = "nonexistent@example.com";
    when(userRepository.findUserModelById(userIdentity)).thenReturn(null);

    assertThatThrownBy(() -> userDetailsService.loadUserByUsername(userIdentity))
        .isInstanceOf(AuthEmailNotRegisteredException.class);

    verify(userRepository).findUserModelById(userIdentity);
  }
}
