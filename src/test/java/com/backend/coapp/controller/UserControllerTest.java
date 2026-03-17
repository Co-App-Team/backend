package com.backend.coapp.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.backend.coapp.dto.request.UpdatePasswordWithOldPasswordRequest;
import com.backend.coapp.dto.request.UserExperienceRequest;
import com.backend.coapp.dto.response.UserResponse;
import com.backend.coapp.exception.auth.AuthBadCredentialException;
import com.backend.coapp.exception.auth.UserInvalidPasswordChangeException;
import com.backend.coapp.exception.auth.UserServiceFailException;
import com.backend.coapp.exception.company.CompanyNotFoundException;
import com.backend.coapp.exception.genai.ExperienceNotFoundException;
import com.backend.coapp.exception.genai.ExperienceNotOwnedException;
import com.backend.coapp.exception.global.UserNotFoundException;
import com.backend.coapp.model.document.UserExperienceModel;
import com.backend.coapp.model.document.UserModel;
import com.backend.coapp.model.enumeration.*;
import com.backend.coapp.service.JwtService;
import com.backend.coapp.service.UserService;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private UserService userService;

  @MockitoBean private JwtService jwtService;

  @MockitoBean private Authentication authentication;

  @Autowired private UserController userController;

  private UpdatePasswordWithOldPasswordRequest DUMMY_UPDATE_PASSWORD_REQUEST;
  private UserExperienceRequest DUMMY_USER_EXPERIENCE_REQUEST;
  private UserExperienceModel DUMMY_USER_EXPERIENCE_MODEL;
  private UserModel mockUser;

  @BeforeEach
  void setUp() {
    mockUser = mock(UserModel.class);
    when(mockUser.getId()).thenReturn("testUserID");
    when(mockUser.getFirstName()).thenReturn("Foo");
    when(mockUser.getLastName()).thenReturn("User");

    this.DUMMY_UPDATE_PASSWORD_REQUEST =
        new UpdatePasswordWithOldPasswordRequest("oldPassword", "newPassword");
    this.DUMMY_USER_EXPERIENCE_REQUEST =
        UserExperienceRequest.builder()
            .companyId("fooCompanyId")
            .roleTitle("fooRole")
            .roleDescription("Doing foo tasks")
            .startDate(LocalDate.of(2023, 1, 1))
            .endDate(LocalDate.of(2024, 1, 1))
            .build();
    this.DUMMY_USER_EXPERIENCE_MODEL =
        new UserExperienceModel(
            mockUser.getId(),
            "fooCompanyId",
            "fooRole",
            "Doing foo tasks",
            LocalDate.of(2023, 1, 1),
            LocalDate.of(2024, 1, 1));
    this.DUMMY_USER_EXPERIENCE_MODEL.setId("fooExperienceId");

    when(this.authentication.getPrincipal()).thenReturn(mockUser);
  }

  @Test
  void constructor_expectSameInitInstance() {
    assertEquals(this.userController.getUserService(), this.userService);
  }

  @Test
  void getDummyUser_expect200AndDummyUser() throws Exception {
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
  void updatePassword_whenSuccess_expectNoException() throws Exception {
    doNothing().when(this.userService).updateUserPassword(anyString(), anyString(), anyString());
    mockMvc
        .perform(
            patch("/api/user/update-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(this.DUMMY_UPDATE_PASSWORD_REQUEST))
                .principal(this.authentication))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").isNotEmpty());

    verify(userService, times(1))
        .updateUserPassword(mockUser.getId(), "oldPassword", "newPassword");
  }

  @Test
  void updatePassword_whenIncorrectOldPassword_expect401() throws Exception {
    doThrow(new AuthBadCredentialException())
        .when(this.userService)
        .updateUserPassword(anyString(), anyString(), anyString());
    mockMvc
        .perform(
            patch("/api/user/update-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(this.DUMMY_UPDATE_PASSWORD_REQUEST))
                .principal(this.authentication))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.message").isNotEmpty())
        .andExpect(jsonPath("$.error").value(AuthErrorCode.INVALID_EMAIL_OR_PASSWORD.name()));

    verify(userService, times(1))
        .updateUserPassword(mockUser.getId(), "oldPassword", "newPassword");
  }

  @Test
  void updatePassword_whenNewPasswordSameWithOldPassword_expect400() throws Exception {
    doThrow(new UserInvalidPasswordChangeException())
        .when(this.userService)
        .updateUserPassword(anyString(), anyString(), anyString());
    mockMvc
        .perform(
            patch("/api/user/update-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(this.DUMMY_UPDATE_PASSWORD_REQUEST))
                .principal(this.authentication))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").isNotEmpty())
        .andExpect(
            jsonPath("$.error").value(UserErrorCode.NEW_PASSWORD_SAME_WITH_OLD_PASSWORD.name()));
  }

  @Test
  void updatePassword_whenUserServiceFail_expect500() throws Exception {
    doThrow(new UserServiceFailException("foo"))
        .when(this.userService)
        .updateUserPassword(anyString(), anyString(), anyString());
    mockMvc
        .perform(
            patch("/api/user/update-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(this.DUMMY_UPDATE_PASSWORD_REQUEST))
                .principal(this.authentication))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.message").isNotEmpty())
        .andExpect(jsonPath("$.error").value(SystemErrorCode.INTERNAL_ERROR.name()));

    verify(userService, times(1))
        .updateUserPassword(mockUser.getId(), "oldPassword", "newPassword");
  }

  @Test
  void aboutMe_whenSuccess_expectNoException() throws Exception {
    UserModel dummyUser =
        new UserModel("123", "foo@mail.com", "dummyPassword", "foo", "woof", true, 123);
    doReturn(dummyUser).when(this.userService).getUserInformationFromUserID(anyString());
    mockMvc
        .perform(
            get("/api/user/about-me")
                .contentType(MediaType.APPLICATION_JSON)
                .principal(this.authentication))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value("foo@mail.com"))
        .andExpect(jsonPath("$.firstName").value("foo"))
        .andExpect(jsonPath("$.lastName").value("woof"));

    verify(userService, times(1)).getUserInformationFromUserID(mockUser.getId());
  }

  @Test
  void aboutMe_whenNoUserExist_expectException() throws Exception {
    doThrow(new UserNotFoundException())
        .when(this.userService)
        .getUserInformationFromUserID(anyString());
    mockMvc
        .perform(
            get("/api/user/about-me")
                .contentType(MediaType.APPLICATION_JSON)
                .principal(this.authentication))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").isNotEmpty())
        .andExpect(jsonPath("$.error").value(UserErrorCode.USER_NOT_EXIST.name()));

    verify(userService, times(1)).getUserInformationFromUserID(mockUser.getId());
  }

  @Test
  void aboutMe_whenUserServiceFail_expectException() throws Exception {
    doThrow(new UserServiceFailException("foo"))
        .when(this.userService)
        .getUserInformationFromUserID(anyString());
    mockMvc
        .perform(
            get("/api/user/about-me")
                .contentType(MediaType.APPLICATION_JSON)
                .principal(this.authentication))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.message").isNotEmpty())
        .andExpect(jsonPath("$.error").value(SystemErrorCode.INTERNAL_ERROR.name()));

    verify(userService, times(1)).getUserInformationFromUserID(mockUser.getId());
  }

  @Test
  void getAllUserExperience_whenSuccess_expect200AndExperienceList() throws Exception {
    List<UserExperienceModel> experiences = List.of(this.DUMMY_USER_EXPERIENCE_MODEL);
    when(userService.getAllUserExperience(any())).thenReturn(experiences);

    mockMvc
        .perform(
            get("/api/user/experience")
                .contentType(MediaType.APPLICATION_JSON)
                .principal(this.authentication))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.experience").isArray())
        .andExpect(jsonPath("$.experience.length()").value(1));

    verify(userService, times(1)).getAllUserExperience(mockUser.getId());
  }

  @Test
  void getAllUserExperience_whenNoExperience_expect200AndEmptyList() throws Exception {
    when(userService.getAllUserExperience(any())).thenReturn(Collections.emptyList());

    mockMvc
        .perform(
            get("/api/user/experience")
                .contentType(MediaType.APPLICATION_JSON)
                .principal(this.authentication))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.experience").isArray())
        .andExpect(jsonPath("$.experience.length()").value(0));
    verify(userService, times(1)).getAllUserExperience(mockUser.getId());
  }

  @Test
  void getAllUserExperience_whenServiceThrows_expect500() throws Exception {
    when(userService.getAllUserExperience(any()))
        .thenThrow(new UserServiceFailException("DB failed"));

    mockMvc
        .perform(
            get("/api/user/experience")
                .contentType(MediaType.APPLICATION_JSON)
                .principal(this.authentication))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.error").value(SystemErrorCode.INTERNAL_ERROR.name()));
    verify(userService, times(1)).getAllUserExperience(mockUser.getId());
  }

  @Test
  void createNewUserExperience_whenSuccess_expect200AndExperienceId() throws Exception {
    when(userService.createNewUserExperience(any(), any(), any(), any(), any(), any()))
        .thenReturn(DUMMY_USER_EXPERIENCE_MODEL);

    mockMvc
        .perform(
            post("/api/user/experience")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(DUMMY_USER_EXPERIENCE_REQUEST))
                .principal(this.authentication))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.experienceId").value(DUMMY_USER_EXPERIENCE_MODEL.getId()));

    verify(userService, times(1))
        .createNewUserExperience(
            mockUser.getId(),
            DUMMY_USER_EXPERIENCE_REQUEST.getCompanyId(),
            DUMMY_USER_EXPERIENCE_REQUEST.getRoleTitle(),
            DUMMY_USER_EXPERIENCE_REQUEST.getRoleDescription(),
            DUMMY_USER_EXPERIENCE_REQUEST.getStartDate(),
            DUMMY_USER_EXPERIENCE_REQUEST.getEndDate());
  }

  @Test
  void createNewUserExperience_whenInvalidRequest_expect400() throws Exception {
    UserExperienceRequest invalidRequest = UserExperienceRequest.builder().build();

    mockMvc
        .perform(
            post("/api/user/experience")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))
                .principal(this.authentication))
        .andExpect(status().isBadRequest())
        .andExpect(
            jsonPath("$.error").value(RequestErrorCode.REQUEST_HAS_NULL_OR_EMPTY_FIELD.name()));
    verifyNoInteractions(userService);
  }

  @Test
  void createNewUserExperience_whenCompanyNotFound_expect404() throws Exception {
    when(userService.createNewUserExperience(any(), any(), any(), any(), any(), any()))
        .thenThrow(new CompanyNotFoundException());

    mockMvc
        .perform(
            post("/api/user/experience")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(DUMMY_USER_EXPERIENCE_REQUEST))
                .principal(this.authentication))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error").value(CompanyErrorCode.COMPANY_NOT_FOUND.name()));

    verify(userService, times(1))
        .createNewUserExperience(
            mockUser.getId(),
            DUMMY_USER_EXPERIENCE_REQUEST.getCompanyId(),
            DUMMY_USER_EXPERIENCE_REQUEST.getRoleTitle(),
            DUMMY_USER_EXPERIENCE_REQUEST.getRoleDescription(),
            DUMMY_USER_EXPERIENCE_REQUEST.getStartDate(),
            DUMMY_USER_EXPERIENCE_REQUEST.getEndDate());
  }

  @Test
  void createNewUserExperience_whenServiceFails_expect500() throws Exception {
    when(userService.createNewUserExperience(any(), any(), any(), any(), any(), any()))
        .thenThrow(new UserServiceFailException("DB failed"));

    mockMvc
        .perform(
            post("/api/user/experience")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(DUMMY_USER_EXPERIENCE_REQUEST))
                .principal(this.authentication))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.error").value(SystemErrorCode.INTERNAL_ERROR.name()));
    verify(userService, times(1))
        .createNewUserExperience(
            mockUser.getId(),
            DUMMY_USER_EXPERIENCE_REQUEST.getCompanyId(),
            DUMMY_USER_EXPERIENCE_REQUEST.getRoleTitle(),
            DUMMY_USER_EXPERIENCE_REQUEST.getRoleDescription(),
            DUMMY_USER_EXPERIENCE_REQUEST.getStartDate(),
            DUMMY_USER_EXPERIENCE_REQUEST.getEndDate());
  }

  @Test
  void deleteUserExperience_whenSuccess_expect200() throws Exception {
    doNothing().when(userService).deleteUserExperience(any(), any());

    mockMvc
        .perform(
            delete("/api/user/experience/{experienceId}", DUMMY_USER_EXPERIENCE_MODEL.getId())
                .principal(this.authentication))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("Deleted successfully."));
    verify(userService, times(1))
        .deleteUserExperience(DUMMY_USER_EXPERIENCE_MODEL.getId(), mockUser.getId());
  }

  @Test
  void deleteUserExperience_whenExperienceNotFound_expect404() throws Exception {
    doThrow(new ExperienceNotFoundException()).when(userService).deleteUserExperience(any(), any());

    mockMvc
        .perform(
            delete("/api/user/experience/{experienceId}", DUMMY_USER_EXPERIENCE_MODEL.getId())
                .principal(this.authentication))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error").value(UserExperienceErrorCode.EXPERIENCE_NOT_FOUND.name()));
    verify(userService, times(1))
        .deleteUserExperience(DUMMY_USER_EXPERIENCE_MODEL.getId(), mockUser.getId());
  }

  @Test
  void deleteUserExperience_whenNotOwned_expect403() throws Exception {
    doThrow(new ExperienceNotOwnedException("Can NOT delete."))
        .when(userService)
        .deleteUserExperience(any(), any());

    mockMvc
        .perform(
            delete("/api/user/experience/{experienceId}", DUMMY_USER_EXPERIENCE_MODEL.getId())
                .principal(this.authentication))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.error").value(UserExperienceErrorCode.EXPERIENCE_NOT_OWN.name()));
    verify(userService, times(1))
        .deleteUserExperience(DUMMY_USER_EXPERIENCE_MODEL.getId(), mockUser.getId());
  }

  @Test
  void deleteUserExperience_whenServiceFails_expect500() throws Exception {
    doThrow(new UserServiceFailException("DB failed"))
        .when(userService)
        .deleteUserExperience(any(), any());

    mockMvc
        .perform(
            delete("/api/user/experience/{experienceId}", DUMMY_USER_EXPERIENCE_MODEL.getId())
                .principal(this.authentication))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.error").value(SystemErrorCode.INTERNAL_ERROR.name()));
    verify(userService, times(1))
        .deleteUserExperience(DUMMY_USER_EXPERIENCE_MODEL.getId(), mockUser.getId());
  }

  @Test
  void deleteUserExperience_whenMissingExperienceId_expect400() throws Exception {

    mockMvc
        .perform(delete("/api/user/experience/{experienceId}", "  ").principal(this.authentication))
        .andExpect(status().isBadRequest())
        .andExpect(
            jsonPath("$.error").value(RequestErrorCode.REQUEST_HAS_NULL_OR_EMPTY_FIELD.name()));
    verifyNoInteractions(userService);
  }

  @Test
  void updateUserExperience_whenSuccess_expect200() throws Exception {
    doNothing()
        .when(userService)
        .updateUserExperience(any(), any(), any(), any(), any(), any(), any());

    mockMvc
        .perform(
            patch("/api/user/experience/{experienceId}", DUMMY_USER_EXPERIENCE_MODEL.getId())
                .principal(this.authentication)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(DUMMY_USER_EXPERIENCE_REQUEST)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("Update successfully."));
    verify(userService, times(1))
        .updateUserExperience(
            DUMMY_USER_EXPERIENCE_MODEL.getId(),
            mockUser.getId(),
            DUMMY_USER_EXPERIENCE_REQUEST.getCompanyId(),
            DUMMY_USER_EXPERIENCE_REQUEST.getRoleTitle(),
            DUMMY_USER_EXPERIENCE_REQUEST.getRoleDescription(),
            DUMMY_USER_EXPERIENCE_REQUEST.getStartDate(),
            DUMMY_USER_EXPERIENCE_REQUEST.getEndDate());
  }

  @Test
  void updateUserExperience_whenInvalidRequest_expect400() throws Exception {
    UserExperienceRequest invalidRequest = UserExperienceRequest.builder().build();

    mockMvc
        .perform(
            patch("/api/user/experience/{experienceId}", DUMMY_USER_EXPERIENCE_MODEL.getId())
                .principal(this.authentication)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(
            jsonPath("$.error").value(RequestErrorCode.REQUEST_HAS_NULL_OR_EMPTY_FIELD.name()));
    verifyNoInteractions(userService);
  }

  @Test
  void updateUserExperience_whenExperienceNotFound_expect404() throws Exception {
    doThrow(new ExperienceNotFoundException())
        .when(userService)
        .updateUserExperience(any(), any(), any(), any(), any(), any(), any());

    mockMvc
        .perform(
            patch("/api/user/experience/{experienceId}", DUMMY_USER_EXPERIENCE_MODEL.getId())
                .principal(this.authentication)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(DUMMY_USER_EXPERIENCE_REQUEST)))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error").value(UserExperienceErrorCode.EXPERIENCE_NOT_FOUND.name()));

    verify(userService, times(1))
        .updateUserExperience(
            DUMMY_USER_EXPERIENCE_MODEL.getId(),
            mockUser.getId(),
            DUMMY_USER_EXPERIENCE_REQUEST.getCompanyId(),
            DUMMY_USER_EXPERIENCE_REQUEST.getRoleTitle(),
            DUMMY_USER_EXPERIENCE_REQUEST.getRoleDescription(),
            DUMMY_USER_EXPERIENCE_REQUEST.getStartDate(),
            DUMMY_USER_EXPERIENCE_REQUEST.getEndDate());
  }

  @Test
  void updateUserExperience_whenNotOwned_expect403() throws Exception {
    doThrow(new ExperienceNotOwnedException("Cannot update."))
        .when(userService)
        .updateUserExperience(any(), any(), any(), any(), any(), any(), any());

    mockMvc
        .perform(
            patch("/api/user/experience/{experienceId}", DUMMY_USER_EXPERIENCE_MODEL.getId())
                .principal(this.authentication)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(DUMMY_USER_EXPERIENCE_REQUEST)))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.error").value(UserExperienceErrorCode.EXPERIENCE_NOT_OWN.name()));
    verify(userService, times(1))
        .updateUserExperience(
            DUMMY_USER_EXPERIENCE_MODEL.getId(),
            mockUser.getId(),
            DUMMY_USER_EXPERIENCE_REQUEST.getCompanyId(),
            DUMMY_USER_EXPERIENCE_REQUEST.getRoleTitle(),
            DUMMY_USER_EXPERIENCE_REQUEST.getRoleDescription(),
            DUMMY_USER_EXPERIENCE_REQUEST.getStartDate(),
            DUMMY_USER_EXPERIENCE_REQUEST.getEndDate());
  }

  @Test
  void updateUserExperience_whenCompanyNotFound_expect404() throws Exception {
    doThrow(new CompanyNotFoundException())
        .when(userService)
        .updateUserExperience(any(), any(), any(), any(), any(), any(), any());

    mockMvc
        .perform(
            patch("/api/user/experience/{experienceId}", DUMMY_USER_EXPERIENCE_MODEL.getId())
                .principal(this.authentication)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(DUMMY_USER_EXPERIENCE_REQUEST)))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error").value(CompanyErrorCode.COMPANY_NOT_FOUND.name()));
    verify(userService, times(1))
        .updateUserExperience(
            DUMMY_USER_EXPERIENCE_MODEL.getId(),
            mockUser.getId(),
            DUMMY_USER_EXPERIENCE_REQUEST.getCompanyId(),
            DUMMY_USER_EXPERIENCE_REQUEST.getRoleTitle(),
            DUMMY_USER_EXPERIENCE_REQUEST.getRoleDescription(),
            DUMMY_USER_EXPERIENCE_REQUEST.getStartDate(),
            DUMMY_USER_EXPERIENCE_REQUEST.getEndDate());
  }

  @Test
  void updateUserExperience_whenServiceFails_expect500() throws Exception {
    doThrow(new UserServiceFailException("DB failed"))
        .when(userService)
        .updateUserExperience(any(), any(), any(), any(), any(), any(), any());

    mockMvc
        .perform(
            patch("/api/user/experience/{experienceId}", DUMMY_USER_EXPERIENCE_MODEL.getId())
                .principal(this.authentication)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(DUMMY_USER_EXPERIENCE_REQUEST)))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.error").value(SystemErrorCode.INTERNAL_ERROR.name()));
    verify(userService, times(1))
        .updateUserExperience(
            DUMMY_USER_EXPERIENCE_MODEL.getId(),
            mockUser.getId(),
            DUMMY_USER_EXPERIENCE_REQUEST.getCompanyId(),
            DUMMY_USER_EXPERIENCE_REQUEST.getRoleTitle(),
            DUMMY_USER_EXPERIENCE_REQUEST.getRoleDescription(),
            DUMMY_USER_EXPERIENCE_REQUEST.getStartDate(),
            DUMMY_USER_EXPERIENCE_REQUEST.getEndDate());
  }

  @Test
  void updateUserExperience_whenMissingExperienceId_expect400() throws Exception {

    mockMvc
        .perform(
            patch("/api/user/experience/{experienceId}", " ")
                .principal(this.authentication)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(DUMMY_USER_EXPERIENCE_REQUEST)))
        .andExpect(status().isBadRequest())
        .andExpect(
            jsonPath("$.error").value(RequestErrorCode.REQUEST_HAS_NULL_OR_EMPTY_FIELD.name()));
    verifyNoInteractions(userService);
  }
}
