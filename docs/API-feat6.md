# API Documentation - Feature #6: AI resume builder

## Overview
This document outlines the API endpoints for the AI resume builder feature of CoApp. This feature allows users interact with GenAI to improve their resumes.

## Endpoints

### 1. Prompt

**Path:** `api/resume-ai-builder/`

**Method:** `GET`

**Description**: Prompt to chatbot 

**Request Body** 

```json
{
    "userPrompt": "question from user",
    "applicationId" : "applicationID",
    "experienceId": ["experienceId1","experienceId2"]
}
```

*Note: `applicationId` and `experienceId` are optional context.*

> \[!IMPORTANT\]
> Since we only allow users to select up to 2 experience to stay within context window limit.


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
  "error":"OVER_LIMIT_CHARACTER",
  "message":"Your prompt is too long. Please shorten it and try again..."
}
```

**Response 400 BAD REQUESTS:**

Response body:
```json
{
  "error":"OVER_LIMIT_EXPERIENCE",
  "message":"Only allow to select up to 2 experience. Please try again."
}
```


### 2. Update users' profiles for GenAI context

#### 2.1 View user information


This is an updated of the same endpoint from `API-feat1.md`

**Path:** `api/user/about-me`

**Method:** `GET`

**Response 200 OK:**

Response body:
```json
{
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

**Response 400 BAD REQUEST:**

Response body:
```json
{
  "error": "USER_NOT_EXIST",
  "message": "User does NOT exist."
}
```

#### 2.2 Add/update/delete experience

**Path:** `api/user/past-experience`

**Method:** `POST`

**Description**: Create a new experience for the user

**Request Body**

```json
{
    "company": "Company A", 
    "summary": "Summary of your role"
}
```

**Response 200 OK:**

Response body:
```json
{
  "response":"Added successfully."
}
```

**Response 400 BAD REQUEST:**

Response body:
```json
{
  "error":"OVER_LIMIT_CHARACTER",
  "message":"Your description about your role is too long. Please short it and try again."
}
```

**Method:** `PATCH`

**Description**: Update the existing experience for the user

**Request Body**

```json
{
    "experienceId": "ID of the experience",
    "company": "Company A", 
    "summary": "Summary of your role"
}
```

**Response 200 OK:**

Response body:
```json
{
  "response":"Updated successfully."
}
```

**Response 400 BAD REQUEST:**

Response body:
```json
{
  "error":"OVER_LIMIT_CHARACTER",
  "message":"Your description about your role is too long. Please short it and try again."
}
```

**Response 400 BAD REQUEST:**

Response body:
```json
{
  "error":"EXPERIENCE_NOT_EXIST",
  "message":"The experience doesn't exist or belong to the user."
}
```

**Method:** `DELETE`

**Description**: Delete the existing experience for the user

**Request Body**

```json
{
    "experienceId": "ID of the experience"
}
```

**Response 200 OK:**

Response body:
```json
{
  "response":"Deleted successfully."
}
```

**Response 400 BAD REQUEST:**

Response body:
```json
{
  "error":"EXPERIENCE_NOT_EXIST",
  "message":"The experience does not exist or not belong to the user."
}
```







