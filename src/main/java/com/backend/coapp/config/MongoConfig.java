package com.backend.coapp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@Configuration
@EnableMongoAuditing  // to enable mongo auditing
public class MongoConfig {
}