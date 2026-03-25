/*
How to run (you must have k6 installed):

```
cd src/test/java/com/backend/coapp/_performance
k6 run loadtest.js
```

Note: if your environment is on the free tier of render,
you must make sure the backend is running
and will not have to spin up before running this load test

 */
import http from 'k6/http';
import { check, sleep } from 'k6';


export const options = {
    stages: [
        { duration: '30s', target: 20 },
        { duration: '1m', target: 20 },
        { duration: '30s', target: 0 },
    ],
};

const BASE_URL = 'https://coapp-backend-dev.onrender.com';
let i = 1;

export default function () {

    i += 1

    // Feature 1: log in and get token
    const loginRes = http.post(`${BASE_URL}/api/auth/login`, JSON.stringify({
        email: 'geetloomba79@gmail.com',
        password: '123qwe',
    }), { headers: { 'Content-Type': 'application/json' } });

    // Extract token to be used for the remaining requests
    const authToken = loginRes.json('token');
    check(loginRes, { 'logged in': (r) => r.status === 200 });
    const authParams = {
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${authToken}`,
        },
    };

    // Feature 2: GET a list of applications for the user
    const getApplicationRes = http.get(`${BASE_URL}/api/application`, authParams);

    check(getApplicationRes, { 'Get Application status 200': (r) => r.status === 200 });

    // Feature 2: POST a new application
    const payload = JSON.stringify(
        {"companyId":"69a4ddcab0a73ab3e5bd5a8b","jobTitle":`test${i}`,"numPositions":"1","status":"NOT_APPLIED","applicationDeadline":"2100-03-01","jobDescription":"","sourceLink":""}
    );
    const postApplicationRes = http.post(`${BASE_URL}/api/application`, payload, authParams);
    const applicationId = postApplicationRes.json().applicationId;
    check(postApplicationRes, { 'Post Application status 200': (r) => r.status === 201 });

    // Feature 2: DELETE application (for test cleanup)
    const deleteApplicationRes = http.request('DELETE', `${BASE_URL}/api/application/${applicationId}`, null, authParams);
    check(deleteApplicationRes, { 'Delete Application status 200': (r) => r.status === 200 });

    // Feature 1: GET user information
    const res2 = http.get(`${BASE_URL}/api/user/about-me`, authParams);

    check(res2, { 'GET user information status 200': (r) => r.status === 200 });

    // Feature 1: log out
    const res3 = http.get(`${BASE_URL}/api/auth/logout`, authParams);

    check(res3, { 'GET log out status 200': (r) => r.status === 200 });


    sleep(6);


}
