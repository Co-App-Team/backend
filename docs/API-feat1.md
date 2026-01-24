
# API Documentation - Feature #1: User authentication

We will use Jason Web Token (JWT) for managing users authentication and users sessions. For further information about JWT token, please see [Preamble on JWT](#preamble-on-jwt).



## Endpoints

### 1. Login

**Path:** `api/v1/auth/login`

**Method:** `POST`

**Description**: Take `userEmail` and `password`, then check if password is correct. If yes, return JWT token; return authetication error if the password is incorrect

**Request Body** 

```json
{
    "email": "email",
    "password": "password"
}
```

**Response 200 OK:**
Response body:

```json
{
  "accessToken": "<JWT token>",
  "tokenType": "Bearer",
  "expiresIn": ...
}

```

**Response 400 BAD REQUEST:**

Response body:
```json
{
  "error":"ACCOUNT_NOT_ACTIVATE",
  "message":"The account has not yet activated."
}
```

*Note: When get this error, we should re-direct user to the page to confirm confirmation code.*

**Response 401 Unauthorized:**
Response body:

```json
{
  "error": "INVALID_EMAIL_OR_PASSWORD",
  "message": "Invalid email or password"
}
```


### 2. Log out

To log user out, client will clean up token cache in web browser

### 3. Create account

Each account on Co-App is associated with a email. To create an new account on `Co-App`, user need to follow the process:
1. Create a new account and provide the user's email
2. User need to provide the confirmation code, which we send to them through email, to activate their account
3. User can request to resend a new confirmation code.

If the user doesn't activate the account, the account can't be used.

3.1. **Path:** `api/v1/auth/register`

**Method:** `POST`

**Description**: Take user information, including firstname, lastname, email and password to create a new account on Co-App.

**Request Body** 

```json
{
  "firstName" : "user firstname",
  "lastName" : "user lastname",
  "email" : "email",
  "password" : "password",
}
```

**Response 200 OK:**

Response body:
```json
{
  "message":"An confirmation code will be sent to your email. Please provide the confirmation to activate your account."
}
```


**Response 409 CONFLICT:**

Response body:
```json
{
  "error":"EXIST_ACCOUNT_WITH_EMAIL",
  "message":"Email already exists."
}
```
3.2. **Path:** `api/v1/auth/verify-email`

**Method:** `UPDATE`

**Description**: Check user confirmation code. If it matches, activate their account.

**Request Body** 

```json
{
  "email" : "email",
  "verifyCode" : "code",
}
```

**Response 200 OK:**

Response body:
```json
{
  "message":"Your account is verified. Account is created successfully. Please log in."
}
```

**Response 400 BAD REQUEST:**

Response body:
```json
{
  "error":"INVALID_CONFIRMATION_CODE",
  "message":"Invalid confirmation code."
}
```

3.3 **Path:** `api/v1/auth/reset-confirmation-code`

**Description**: Reset confirmation code and send to user via email.

**Request Body** 

```json
{
  "email" : "email",
}
```

**Response 200 OK:**

Response body:
```json
{
  "message":"New confirmation code is sent. Check your mail."
}
```

**Response 400 BAD REQUEST:**

Response body:
```json
{
  "error":"ACCOUNT_NOT_EXIST",
  "message":"Account with provided email not exists."
}
```

### 4. Change password

To change password, we will follow the process:
1. The user provides the email that he/she used to register for the account
2. The user provides the confirmation code that we send the user via email

4.1. **Path:** `api/v1/auth/forgot-password`

**Description**: Take the email, and check if an account associated with the email exist and send confirmation code to the email.

**Method:** `GET`

**Request Body** 

```json
{
  "email" : "email",
}
```

**Response 200 OK:**

Response body:
```json
{
  "message":"An confirmation code will be sent to your email. Please provide the confirmation to reset your password."
}
```

**Response 400 BAD REQUEST:**

Response body:
```json
{
  "error":"ACCOUNT_NOT_EXIST",
  "message":"No account with the provided email."
}
```

**Response 400 BAD REQUEST:**

Response body:
```json
{
  "error":"ACCOUNT_NOT_ACTIVATE",
  "message":"The account has not yet activated."
}
```

*Note: When get this error, we should re-direct user to the page to confirm confirmation code.*

4.2 **Path:** `/api/v1/auth/update-password`


**Description**: Check the confirmation code is correct, we let user to update the password

**Method:** `POST`

**Request Body** 

```json
{
  "email" : "email",
  "verifyCode" : "code",
  "newPassword":"newPassword"
}
```

**Response 200 OK:**

Response body:
```json
{
  "message":"Password was updated successfully."
}
```

**Response 400 BAD REQUEST:**

Response body:
```json
{
  "error":"INVALID_CONFIRMATION_CODE",
  "message":"Invalid confirmation code."
}
```


For every API, we can potentially return this response (if something goes wrong) and the message indicating the internal failure

**Response 500 INTERNAL SERVER ERROR:**

Response body:
```json
{
  "error":"INTERNAL_ERROR",
  "message":"<this message will indicate a specific failure>"
}
```

---

### Preamble on JWT

*If you want to read more about JWT go to: [jwt.io](https://www.jwt.io/introduction#what-is-json-web-token)*

JWT (JSON Web Token) can be used for authorization, consists of 3 parts:
- Header
- Payload
- Signature

Typically looking like:
```
xxxx.yyyy.zzzz
```
where `xxxx` is the header encoded in base-64, same for `yyyy`. Then `zzzz` is `xxxx.yyyy` encrypted with a key on the backend.

Anything you want can go into the payload, but there are some common standards like:
- `iss`: Issuer
- `exp`: Expiration time
- `sub`: Subject (like a user ID)

*A common and recommended practice is to store a JWT in a cookie, specifically an `HttpOnly` cookie with the `Secure` and `SameSite` flags.*

### Auth flow

1. (Client) `POST /api/login` body: `{"username": "...", "password": "..."}
2. (Server) Response with JWT token, which will be used for any follow up API calls from client (until the token expire). 