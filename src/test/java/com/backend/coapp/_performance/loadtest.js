/*
Load testing script

Requirements:
- k6 must be installed globally
- a valid backend on the specified url must be running on render (paid tier)
- 69a4ddcab0a73ab3e5bd5a8b is a valid companyId (Test company)


How to run:
cd src/test/java/com/backend/coapp/_performance
k6 run loadtest.js

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
const TEST_COMPANY_ID = "69a4ddcab0a73ab3e5bd5a8b"

export default function () {

    const uniqueId = `v${__VU}i${__ITER}`;
    
    // Feature 1: Authentication and Profile ------------------------------------------------------------------------------------

    // Request 1: Login (Write/Session Creation)
    const loginRes = http.post(`${BASE_URL}/api/auth/login`, JSON.stringify({
        email: 'geetloomba79@gmail.com',
        password: '123qwe',
    }), { headers: { 'Content-Type': 'application/json' } });

    const authToken = loginRes.json('token');
    check(loginRes, { 'login status 200': (r) => r.status === 200 });
    
    const authParams = {
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${authToken}`,
        },
    };

    // Request 2: Get About Me (Read)
    const aboutMeRes = http.get(`${BASE_URL}/api/user/about-me`, authParams);
    check(aboutMeRes, { 'get profile status 200': (r) => r.status === 200 });

    // Feature 2: Application Management ------------------------------------------------------------------------------------

    // Request 1: Create Application (Write)
    const appPayload = JSON.stringify({
        "companyId": TEST_COMPANY_ID,
        "jobTitle": `Engineer_${uniqueId}`,
        "numPositions": "1",
        "status": "NOT_APPLIED",
        "applicationDeadline": "2100-01-01",
        "jobDescription": "Load testing",
        "sourceLink": "https://test.com"
    });
    const postAppRes = http.post(`${BASE_URL}/api/application`, appPayload, authParams);
    const applicationId = postAppRes.json().applicationId;
    check(postAppRes, { 'post application status 201': (r) => r.status === 201 });

    // Request 2: Delete Application (Cleanup/Write)
    if (applicationId) {
        const delAppRes = http.del(`${BASE_URL}/api/application/${applicationId}`, null, authParams);
        check(delAppRes, { 'delete application status 200': (r) => r.status === 200 });
    }

    // Feature 3: Application Filtering/Search ------------------------------------------------------------------------------------

    // Request 1: Search by Text (Read)
    const searchRes = http.get(`${BASE_URL}/api/application?search=Niche`, authParams);
    check(searchRes, { 'search applications status 200': (r) => r.status === 200 });

    // Request 2: Filter by Multiple Statuses (Read)
    const filterRes = http.get(`${BASE_URL}/api/application?status=APPLIED,INTERVIEWING`, authParams);
    check(filterRes, { 'filter applications status 200': (r) => r.status === 200 });

    // Feature 4: Company Wiki and Reviews ------------------------------------------------------------------------------------

    // Request 1: Get All Companies (Read)
    const getCompaniesRes = http.get(`${BASE_URL}/api/companies?usePagination=true&size=10`, authParams);
    check(getCompaniesRes, { 'get companies status 200': (r) => r.status === 200 });

    // Request 2: Create Review for Company (Write)
    const reviewPayload = JSON.stringify({
        "rating": 5,
        "comment": `Great mentorship`,
        "workTermSeason": "Summer",
        "workTermYear": 2025,
        "jobTitle": "Software Intern"
    });
    const postReviewRes = http.post(`${BASE_URL}/api/companies/${TEST_COMPANY_ID}/reviews`, reviewPayload, authParams);
    const reviewId = postReviewRes.json().reviewId;
    check(postReviewRes, { 'post review status 201': (r) => r.status === 201});

    // Request 3: Delete Review (Cleanup/Write)
    if (reviewId) {
        const delReviewRes = http.del(`${BASE_URL}/api/companies/${targetCompId}/reviews/${reviewId}`, null, authParams);
        check(delReviewRes, { 'delete review status 200': (r) => r.status === 200 });
    }

    // Feature 5: Interview Applications ------------------------------------------------------------------------------------

    // Request 1: Get All Interviews (Read)
    const getInterviewsRes = http.get(`${BASE_URL}/api/application/interviews`, authParams);
    check(getInterviewsRes, { 'get interviews status 200': (r) => r.status === 200 });

    // Request 2: Get Interviews with Date Filter (Read/Logic Test)
    const dateFilteredRes = http.get(`${BASE_URL}/api/application/interviews?startDate=2024-01-01&endDate=2025-12-31`, authParams);
    check(dateFilteredRes, { 'get filtered interviews status 200': (r) => r.status === 200 });

    // Feature 6: AI Resume Builder and Profile Experience ------------------------------------------------------------------------------------

    // Request 1: Get Remaining Quota (Read)
    const quotaRes = http.get(`${BASE_URL}/api/resume-ai-advisor/remaining-quota`, authParams);
    check(quotaRes, { 'get ai quota status 200': (r) => r.status === 200 });

    // Request 2: Create Experience Entry (Write)
    const expPayload = JSON.stringify({
        "companyId": TEST_COMPANY_ID,
        "roleTitle": "Software Developer",
        "roleDescription": `Handled microservices ${uniqueId}`,
        "startDate": "2023-01-01"
    });
    const postExpRes = http.post(`${BASE_URL}/api/user/experience`, expPayload, authParams);
    const expId = postExpRes.json('experienceId');
    check(postExpRes, { 'post experience status 200': (r) => r.status === 200 });

    // Request 3: Delete Experience (Cleanup/Write)
    if (expId) {
        const delExpRes = http.del(`${BASE_URL}/api/user/experience/${expId}`, null, authParams);
        check(delExpRes, { 'delete experience status 200': (r) => r.status === 200 });
    }

    // Logout and final safety sleep
    http.get(`${BASE_URL}/api/auth/logout`, authParams);

    sleep(6); // to maintain the 200 Requests Per Minute target across 20 users
}
