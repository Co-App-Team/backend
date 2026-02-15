# API Documentation - Feature #6: AI resume builder

## Overview
This document outlines the API endpoints for the AI resume builder feature of CoApp. This feature allows users interact with GenAI to improve their resumes.

## Endpoints

### 1. Prompt

**Path:** `api/resume-ai-builder/`

**Method:** `POST`

**Description**: Take user email and password, then check if password is correct. If yes, return JWT token; return authetication error if the password is incorrect

**Request Body** 

```json
{
    "userPrompt": "question from user",
}
```

**Response 200 OK:**

Response body:
```json
{
  "response":"response from GenAI"
}
```

**Response 429 TOO MANY REQUESTS:**

Response body:
```json
{
  "error":"OVER_LIMIT_CHATBOT_REQUEST",
  "message":"You have reached your usage limit for Co-App chatbot. Please wait before making more requests."
}
```

**Response 400 BAD REQUESTS:**

Response body:
```json
{
  "error":"OVER_LIMIT_PROMPT_CHARACTER",
  "message":"Your prompt is too long. Please shorten it and try again.."
}
```


### 2. Update users' profiles for GenAI context

**Path:** `api/user/about-me`

**Method:** `GET`

**Response 200 OK:**

Response body:
```json
{
    "userId": "user ID",
    "firstName":"user first name",
    "lastName": "user last name",
    "email": "user email",
    "pastExperiences":[ // The most two recent experience
        {
            "experienceId":"ID of the first experience",
            "company":"company name 1",
            "roleDescription": "description of role at company 1"
        },
        {
            "experienceId":"ID of the second experience",
            "company":"company name 2",
            "roleDescription": "description of role at company 2"
        }

    ]
}
```




