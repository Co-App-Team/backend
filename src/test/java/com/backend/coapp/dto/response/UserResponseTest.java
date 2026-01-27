package com.backend.coapp.dto.response;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class UserResponseTest {
    @Test
    public void getterMethod_expectInitValues(){
        UserResponse response = new UserResponse("foo","woof","foo@mail.com");

        assertEquals("foo",response.getFirstName());
        assertEquals("woof",response.getLastName());
        assertEquals("foo@mail.com", response.getEmail());

    }

    @Test
    public void toMap_expectMapWithInitValues(){
        UserResponse response = new UserResponse("foo","woof","foo@mail.com");
        Map<String,Object> expectMap = Map.of("firstName","foo","lastName","woof","email","foo@mail.com");

        assertEquals(expectMap,response.toMap());
    }
}
