package com.backend.coapp.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.backend.coapp.dto.request.UpdatePasswordWithOldPasswordRequest;
import com.backend.coapp.dto.response.UserResponse;
import com.backend.coapp.exception.auth.AuthBadCredentialException;
import com.backend.coapp.exception.auth.UserInvalidPasswordChangeException;
import com.backend.coapp.exception.auth.UserServiceFailException;
import com.backend.coapp.exception.global.UserNotFoundException;
import com.backend.coapp.model.document.UserModel;
import com.backend.coapp.model.enumeration.AuthErrorCode;
import com.backend.coapp.model.enumeration.SystemErrorCode;
import com.backend.coapp.model.enumeration.UserErrorCode;
import com.backend.coapp.service.JwtService;
import com.backend.coapp.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private UserService userService;

  @MockitoBean private JwtService jwtService;

  @Autowired private UserController userController;

  private UpdatePasswordWithOldPasswordRequest dummyRequest;

  @BeforeEach
  public void setUp() {
    this.dummyRequest = new UpdatePasswordWithOldPasswordRequest("oldPassword", "newPassword");
  }

  @Test
  public void constructor_expectSameInitInstance() {
    assertEquals(this.userController.getUserService(), this.userService);
  }

  @Test
  public void getDummyUser_expect200AndDummyUser() throws Exception {
    UserResponse dummyUser = new UserResponse("Dummy Firstname", "Dummy Lastname", "foo@mail.com");
    when(this.userService.getDummyUser()).thenReturn(dummyUser);

    mockMvc
        .perform(get("/api/user/dummyUser").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.firstName").value(dummyUser.getFirstName()))
        .andExpect(jsonPath("$.lastName").value(dummyUser.getLastName()))
        .andExpect(jsonPath("$.email").value(dummyUser.getEmail()));
  }

  @Test
  @WithMockUser(username = "testUserID")
  public void updatePassword_whenSuccess_expectNoException() throws Exception {
    doNothing().when(this.userService).updateUserPassword(anyString(), anyString(), anyString());
    mockMvc
        .perform(
            patch("/api/user/update-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(this.dummyRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").isNotEmpty());

    verify(userService, times(1)).updateUserPassword("testUserID", "oldPassword", "newPassword");
  }

  @Test
  @WithMockUser(username = "testUserID")
  public void updatePassword_whenIncorrectOldPassword_expect401() throws Exception {
    doThrow(new AuthBadCredentialException())
        .when(this.userService)
        .updateUserPassword(anyString(), anyString(), anyString());
    mockMvc
        .perform(
            patch("/api/user/update-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(this.dummyRequest)))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.message").isNotEmpty())
        .andExpect(jsonPath("$.error").value(AuthErrorCode.INVALID_EMAIL_OR_PASSWORD.name()));
    ;

    verify(userService, times(1)).updateUserPassword("testUserID", "oldPassword", "newPassword");
  }

  @Test
  @WithMockUser(username = "testUserID")
  public void updatePassword_whenNewPasswordSameWithOldPassword_expect400() throws Exception {
    doThrow(new UserInvalidPasswordChangeException())
        .when(this.userService)
        .updateUserPassword(anyString(), anyString(), anyString());
    mockMvc
        .perform(
            patch("/api/user/update-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(this.dummyRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").isNotEmpty())
        .andExpect(
            jsonPath("$.error").value(UserErrorCode.NEW_PASSWORD_SAME_WITH_OLD_PASSWORD.name()));
    ;
  }

  @Test
  @WithMockUser(username = "testUserID")
  public void updatePassword_whenUserServiceFail_expect500() throws Exception {
    doThrow(new UserServiceFailException("foo"))
        .when(this.userService)
        .updateUserPassword(anyString(), anyString(), anyString());
    mockMvc
        .perform(
            patch("/api/user/update-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(this.dummyRequest)))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.message").isNotEmpty())
        .andExpect(jsonPath("$.error").value(SystemErrorCode.INTERNAL_ERROR.name()));

    verify(userService, times(1)).updateUserPassword("testUserID", "oldPassword", "newPassword");
  }

  @Test
  @WithMockUser(username = "testUserID")
  public void aboutMe_whenSuccess_expectNoException() throws Exception {
    UserModel dummyUser =
        new UserModel("123", "foo@mail.com", "dummyPassword", "foo", "woof", true, 123);
    doReturn(dummyUser).when(this.userService).getUserInformationFromUserID(anyString());
    mockMvc
        .perform(get("/api/user/about-me").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value("foo@mail.com"))
        .andExpect(jsonPath("$.firstName").value("foo"))
        .andExpect(jsonPath("$.lastName").value("woof"));

    verify(userService, times(1)).getUserInformationFromUserID("testUserID");
  }

  @Test
  @WithMockUser(username = "testUserID")
  public void aboutMe_whenNoUserExist_expectException() throws Exception {
    doThrow(new UserNotFoundException())
        .when(this.userService)
        .getUserInformationFromUserID(anyString());
    mockMvc
        .perform(get("/api/user/about-me").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").isNotEmpty())
        .andExpect(jsonPath("$.error").value(UserErrorCode.USER_NOT_EXIST.name()));

    verify(userService, times(1)).getUserInformationFromUserID("testUserID");
  }

  @Test
  @WithMockUser(username = "testUserID")
  public void aboutMe_whenUserServiceFail_expectException() throws Exception {
    doThrow(new UserServiceFailException("foo"))
        .when(this.userService)
        .getUserInformationFromUserID(anyString());
    mockMvc
        .perform(get("/api/user/about-me").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.message").isNotEmpty())
        .andExpect(jsonPath("$.error").value(SystemErrorCode.INTERNAL_ERROR.name()));

    verify(userService, times(1)).getUserInformationFromUserID("testUserID");
  }
}
