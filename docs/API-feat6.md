# API Documentation - Feature #6: AI resume builder

## Overview
This document outlines the API endpoints for the AI resume builder feature of CoApp. This feature allows users interact with GenAI to improve their resumes.

## Endpoints

### 1. Prompt

**Path:** `api/resume-ai-advisor/`

**Method:** `POST`

**Description**: Prompt to chatbot 

**Request Body** 

```json
{
    "userPrompt": "question from user",
    "applicationId" : "applicationID"
}
```

*Note: `applicationId` is optional context.*

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

**Path:** `api/resume-ai-advisor/remaining-quota`

**Method:** `GET`

**Description**: Get remaining GenAI usage quota

**Response 200 OK:**

Response body:
```json
{
  "remainingQuota":number of requests left(int)
}
```

**Example response:**
```json
{
 "response": "Here's a review of your provided experience with suggestions for improvement, tailored for a CEO co-op application:\n\n## Section 1: Key Feedback\n\n*   **Clarity of Impact:** While the specific achievements are strong (10x and 41% speedup), the connection to the \"CEO\" role needs to be emphasized. Think about how these technical achievements translate to business outcomes like efficiency, scalability, or cost savings.\n*   **Action Verbs:** The current phrasing is good, but consider stronger, more results-oriented action verbs where appropriate.\n*   **Contextualization:** Briefly mentioning the *purpose* or *application* of the deep learning models could add more weight. What problem were you solving?\n*   **Remote Work Emphasis:** The job description specifically mentions the ability to work independently in a remote environment. While not explicitly detailed in your experience, consider how your contributions demonstrate this.\n*   **\"CEO\" Role Relevance:** For a CEO role, it's important to show not just technical execution but also strategic thinking, leadership, and understanding of broader business implications. Your current description focuses heavily on the technical implementation.\n\n## Section 2: Improved Version\n\n**Original Text:**\n\"Implementing various deep learning models along with integration tests to ensure the correctness of the models. Vectorized neighbor encode algorithms in a state-of-the-art model using Torch tensors, achieving a 10× algorithm speedup and 41% overall model speedup, enabling efficient training on larger-scale graph datasets\"\n\n**Improved Version:**\n\n*   **Spearheaded the development and rigorous testing of multiple deep learning models, ensuring robust functionality and accuracy through comprehensive integration testing.**\n*   **Optimized a state-of-the-art model by vectorizing neighbor encoding algorithms with Torch tensors, resulting in a 10x acceleration of the encoding process and a 41% improvement in overall model training speed. This enhancement facilitated more efficient and scalable training on large-scale graph datasets.**\n\n**Additional Suggestions for a CEO Application (to be integrated into your resume/cover letter):**\n\n*   **For the \"Foo job\" experience:** If \"Doing foo things\" involved any strategic decisions, project management, collaboration, or problem-solving that had a quantifiable outcome (even if qualitative), try to rephrase it to highlight those aspects. For example, if \"foo things\" involved improving a process, state that.\n*   **For the \"Software Engineer\" experience:** Consider adding a bullet point that speaks to the *impact* of these microservices. Did they improve system performance, user experience, or operational efficiency?\n*   **Addressing Remote Work:** In your cover letter or a dedicated \"Skills\" section, you could explicitly state: \"Proven ability to thrive in and contribute effectively to remote work environments, demonstrating strong self-management, communication, and independent problem-solving skills.\"\n*   **Connecting to Business Acumen:** In your cover letter, try to bridge the gap between your technical achievements and business objectives. For instance, you could say something like: \"My experience in optimizing model performance, as evidenced by a 41% speedup in training large-scale datasets, directly translates to reducing operational costs and accelerating product development cycles, key considerations for a CEO.\""
}

```

### 2. Update users' profiles for GenAI context

#### 2.1 View user information


This is an updated of the same endpoint from `API-feat1.md`

**Path:** `api/user/experience`

**Method:** `GET`

**Response 200 OK:**

Response body:
```json
{
    "experience":[
      {
        "experienceId": "experienceId1",
        "companyId": "companyId1",
        "roleTitle": "Software Engineer",
        "roleDescription": "Built and maintained microservices using Spring Boot",
        "startDate": "2023-01-01",
        "endDate": "2024-01-01"
      },
      {
       "experienceId": "experienceId2",
        "companyId": "companyId2",
        "roleTitle": "Software Engineer",
        "roleDescription": "Built and maintained microservices using Spring Boot",
        "startDate": "2023-01-01",
        "endDate": "2024-01-01"
      },
      ...
    ]
}
```

#### 2.2 Add/update/delete experience

**Note: `endDate` can be null to indicate current job.**

**Path:** `api/user/experience`

**Method:** `POST`

**Description**: Create a new experience for the user

**Request Body**

```json
{
  "companyId": "companyId",
  "roleTitle": "Software Engineer",
  "roleDescription": "Built and maintained microservices using Spring Boot",
  "startDate": "2023-01-01",
  "endDate": "2024-01-01" (optional)
}
```

**Response 200 OK:**

Response body:
```json
{
  "experienceId":"ID of the new experience"
}
```

**Response 400 BAD REQUEST:**

 *The backend will return 400 if any field is invalid. 
 This includes null/emptty fields or role title/description is over character limit (We will specify exact number of character limit in the response's message).*

**Response 404 NOT FOUND:**

Response body:
```json
{
  "error":"COMPANY_NOT_FOUND",
  "message":"Company with this companyId does not exist."
}
```
**Path:** `api/user/experience/{experienceId}`

**Method:** `PATCH`

**Description**: Update the existing experience for the user

**Request Body**

```json
{
   "companyId": "companyId",
   "roleTitle": "Software Engineer",
   "roleDescription": "Built and maintained microservices using Spring Boot",
   "startDate": "2023-01-01",
   "endDate": "2024-01-01" (optional)
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

*The backend will return 400 if any field is invalid.
This includes null/emptty fields or role title/description is over character limit (We will specify exact number of character limit in the response's message).*


**Response 404 NOT FOUND:**

Response body:
```json
{
  "error":"EXPERIENCE_NOT_FOUND",
  "message":"The experience does not exist or not belong to the user."
}
```

**Response 403 FORBIDDEN:**

Response body:
```json
{
  "error":"EXPERIENCE_NOT_OWN",
  "message":"The experience doesn't belong to the user. Can NOT delete."
}
```

**Response 404 NOT FOUND:**

Response body:
```json
{
  "error":"COMPANY_NOT_FOUND",
  "message":"Company with this companyId does not exist."
}
```

**Path:** `api/user/experience/{experienceId}`

**Method:** `DELETE`

**Description**: Delete the existing experience for the user

**Request Body**

*N/A*

**Response 200 OK:**

Response body:
```json
{
  "response":"Deleted successfully."
}
```

**Response 404 NOT FOUND:**

Response body:
```json
{
  "error":"EXPERIENCE_NOT_FOUND",
  "message":"The experience does not exist or not belong to the user."
}
```

**Response 403 FORBIDDEN:**

Response body:
```json
{
  "error":"EXPERIENCE_NOT_OWN",
  "message":"The experience doesn't belong to the user. Can NOT delete."
}
```







