package com.backend.coapp.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.backend.coapp.dto.response.UserResponse;
import com.backend.coapp.service.JwtService;
import com.backend.coapp.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private UserService userService;

  @MockitoBean private JwtService jwtService;

  @Autowired private UserController userController;

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
}
