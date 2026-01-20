package com.backend.coapp;

import com.backend.coapp.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class CoAppApplicationTests {

  @MockitoBean private MongoTemplate mongoTemplate;

  @MockitoBean private GridFsTemplate gridFsTemplate;

  @MockitoBean private UserRepository userRepository;

  @Test
  void contextLoads() {}
}
