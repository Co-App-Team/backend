package com.backend.coapp.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

import com.backend.coapp.dto.response.UserResponse;
import com.backend.coapp.service.EmailService;
import com.backend.coapp.service.UserService;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(UserController.class)
public class UserControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private UserService userService;

  @Autowired private UserController userController;


  @Test
  public void constructor_expectSameInitInstance() {
    assertEquals(this.userController.getUserService(), this.userService);
  }

  @Test
  public void getDummyUser_expect200AndDummyUser() throws Exception {
    UserResponse dummyUser = new UserResponse("Dummy Firstname", "Dummy Lastname", "foo@mail.com");
    when(this.userService.getDummyUser()).thenReturn(dummyUser);

    mockMvc.perform(
            get("/api/user/dummyUser").contentType(MediaType.APPLICATION_JSON)

    ).andExpect(status().isOk())
            .andExpect(jsonPath("$.firstName").value(dummyUser.getFirstName()))
            .andExpect(jsonPath("$.lastName").value(dummyUser.getLastName()))
            .andExpect(jsonPath("$.email").value(dummyUser.getEmail()));
  }
}
