package com.backend.coapp.service;

import com.backend.coapp.repository.ApplicationRepository;
import com.backend.coapp.repository.UserExperienceRepository;
import com.backend.coapp.service.genAI.GeminiGenAIService;
import com.backend.coapp.service.genAI.GenAIService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Testcontainers
public class GenAIResumeAdvisorServiceTest {

    @Container
    @ServiceConnection
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0");

    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private UserExperienceRepository userExperienceRepository;


    private GenAIResumeAdvisorService genAIResumeAdvisorService;
    private GenAIUsageManagementService genAIUsageManagementService;
    private GeminiGenAIService geminiGenAIService;

    @BeforeEach
    public void setUp(){
        this.genAIUsageManagementService = Mockito.mock(GenAIUsageManagementService.class);
        this.geminiGenAIService = Mockito.mock(GeminiGenAIService.class);
        this.genAIResumeAdvisorService = new GenAIResumeAdvisorService(
                this.geminiGenAIService,
                this.genAIUsageManagementService,
                this.applicationRepository,
                this.userExperienceRepository
        );
    }

    @Test
    public void constructor_expectInitCorrectInstance(){
        assertEquals(geminiGenAIService,genAIResumeAdvisorService.getGenAIService());
        assertEquals(genAIUsageManagementService,genAIResumeAdvisorService.getGenAIUsageManagementService());
        assertEquals(applicationRepository,genAIResumeAdvisorService.getApplicationRepository());
        assertEquals(userExperienceRepository,genAIResumeAdvisorService.getUserExperienceRepository());

    }
}
