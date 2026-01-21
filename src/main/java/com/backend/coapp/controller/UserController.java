package com.backend.coapp.controller;

import com.backend.coapp.dto.UserDTO;
import com.backend.coapp.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * User controllers
 *
 * User related REST APIs
 */

@RestController
@Slf4j
@RequestMapping("/api/v1/user")
public class UserController {
    private final UserService userService; // Singleton

    /**
     * Constructor
     */
    public UserController(UserService userService){
        this.userService = userService;
    }

    /**
     * This API endpoint is a proof of concept.
     * This just return a dummy user that doesn't exist in the database.
     *
     * @return ResponseEntity
     */
    @GetMapping("/dummyUser")
    public ResponseEntity<Map<String, Object>> getDummyUser(){
        UserDTO dummyUser = this.userService.getDummyUser();
        log.info("INFO: GET dummyUser API is called.");
        return ResponseEntity.ok().body(dummyUser.toMap());
    }

    /**
     * Get userService instance
     *
     * For testing only
     * @return UserService
     */
    public UserService getUserService(){
        return this.userService;
    }

}
