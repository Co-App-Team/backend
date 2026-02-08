package com.backend.coapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@EnableMongoAuditing
@SpringBootApplication
public class CoAppApplication {

  public static void main(String[] args) {
    SpringApplication.run(CoAppApplication.class, args);
  }
}
