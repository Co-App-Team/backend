/*
Load testing script

written with the help of gemini flash 3.0

Requirements:
- k6 must be installed globally
- BASE_URL is a valid backend on the specified url must be running on render (paid tier)
- TEST_COMPANY_ID is a valid companyId (Test company)
- 20 accounts in the database with testuser1@test.com up to testuser20@test.com with passwords 123qwe

Performance target:
The system must be able to concurrently handle at least 20 users generating a total of 200 requests per minute.

How to run:
cd src/test/java/com/backend/coapp/_performance
k6 run loadtest.js

SAMPLE k6 OUTPUT & ANALYSIS (from a successful run):
This output demonstrates the test passing all requirements with 100% success rate.

          /\      Grafana   /‾‾/
    /\  /  \     |\  __   /  /
   /  \/    \    | |/ /  /   ‾‾\
  /          \   |   (  |  (‾)  |
 / __________ \  |_|\_\  \_____/

     execution: local
        script: loadtest.js
        output: -

     scenarios: (100.00%) 1 scenario, 20 max VUs, 2m30s max duration (incl. graceful stop):
              * default: Up to 20 looping VUs for 2m0s over 3 stages (gracefulRampDown: 30s, gracefulStop: 30s)

  █ TOTAL RESULTS

    checks_total.......: 2702    21.460896/s  : Total checks run (~15 checks per iteration x 193 iterations)
    checks_succeeded...: 100.00% 2702 out of 2702  : PERFECT: All API assertions passed (status codes 200/201)
    checks_failed......: 0.00%   0 out of 2702  : No failures in business logic checks

    ✓ login status 200
    ✓ get profile status 200
    ✓ post application status 201
    ✓ delete application status 200
    ✓ search applications status 200
    ✓ filter applications status 200
    ✓ get companies status 200
    ✓ post review status 201
    ✓ delete review status 200
    ✓ get interviews status 200
    ✓ get filtered interviews status 200
    ✓ get ai quota status 200
    ✓ post experience status 200
    ✓ delete experience status 200

    HTTP
    http_req_duration..............: avg=258.67ms min=98.93ms med=140.27ms max=1.82s  p(90)=682.56ms p(95)=749.53ms  : Response times: Excellent (avg <300ms, p95 <1s)
      { expected_response:true }...: avg=258.67ms min=98.93ms med=140.27ms max=1.82s  p(90)=682.56ms p(95)=749.53ms
    http_req_failed................: 0.00%  0 out of 2895  ← PERFECT: Zero HTTP errors (no 4xx/5xx)
    http_reqs......................: 2895   22.993817/s  ← ~23 req/s = ~1380 RPM (FAR exceeds 200 RPM requirement)

    EXECUTION
    iteration_duration.............: avg=9.89s    min=7.96s   med=10.1s    max=12.48s p(90)=11.05s   p(95)=11.52s  : Each full iteration ~10s (15 reqs + sleep(6))
    iterations.....................: 193    1.532921/s  : Total loops completed across all VUs
    vus............................: 1      min=1         max=20  : VUs ramped correctly
    vus_max........................: 20     min=20        max=20  : ✓ Met: 20 concurrent users achieved & held for 1m

    NETWORK
    data_received..................: 1.1 MB 9.0 kB/s
    data_sent......................: 327 kB 2.6 kB/s

running (2m05.9s), 00/20 VUs, 193 complete and 0 interrupted iterations
default ✓ [======================================] 00/20 VUs  2m0s

KEY METRICS EXPLAINED & REQUIREMENT PASS:
- CONCURRENT USERS: vus_max=20 → Handled 20 real users (testuser1-20@test.com) simultaneously.
- REQUESTS PER MINUTE: http_reqs=2895 over ~2min = ~1380 RPM (>> 200 RPM required).
- SUCCESS RATE: 100% checks & HTTP → No errors, all features (auth, CRUD, search, AI quota) functional under load.
- LATENCY: avg=259ms, p95=750ms → Responsive (sub-second for 95% of requests).
- STABILITY: Held peak for 1m (steady-state stage), graceful ramp up/down → Realistic traffic simulation.
- CONCLUSION: System PASSES requirements with significant headroom (6x+ RPM capacity demonstrated).

*/


import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    stages: [
        { duration: '30s', target: 20 }, // Gradually increase from 0 to 20 userss over 30 seconds
        { duration: '1m', target: 20 }, // Stay at 20 concurrent users for 1 minute
        // 200 RPM target with sleep(6)
        { duration: '30s', target: 0 }, // go back down to 0 users
    ],
};

const BASE_URL = 'https://coapp-backend-dev.onrender.com';
const TEST_COMPANY_ID = "69a4ddcab0a73ab3e5bd5a8b"

export default function () {

    // VU ID (1 to 20)
    const userEmail = `testuser${__VU}@test.com`;
    const uniqueId = `v${__VU}i${__ITER}`;

    // Feature 1: Authentication and Profile ------------------------------------------------------------------------------------

    // Request 1: Login (Write/Session Creation)
    const loginRes = http.post(`${BASE_URL}/api/auth/login`, JSON.stringify({
        email: userEmail,
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
    check(postReviewRes, { 'post review status 201': (r) => r.status === 201});

    // Request 3: Delete Review (Cleanup/Write)
    const delReviewRes = http.del(`${BASE_URL}/api/companies/${TEST_COMPANY_ID}/reviews`, null, authParams);
    check(delReviewRes, { 'delete review status 200': (r) => r.status === 200 });


    // Feature 5: Interview Applications ------------------------------------------------------------------------------------

    // Request 1: Get All Interviews (Read)
    const getInterviewsRes = http.get(`${BASE_URL}/api/application/interviews`, authParams);
    check(getInterviewsRes, { 'get interviews status 200': (r) => r.status === 200 });

    // Request 2: Get Interviews with Date Filter (Read/Logic Test)
    const dateFilteredRes = http.get(`${BASE_URL}/api/application/interviews?startDate=2024-01-01&endDate=2025-12-31`, authParams);
    check(dateFilteredRes, { 'get filtered interviews status 200': (r) => r.status === 200 });

    // Feature 6: AI Resume Builder and Profile Experience ------------------------------------------------------------------------------------

    // NOTE: For feature 6, we don’t perform load tests on the API that evolve Gemini API call since there is a
    // Gemini usage limit on the free tier version. Instead, we perform load tests on getting AI quota
    // and creating/deleting experiences. We have confirmed this with the instructor.

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
        const delExpRes = http.request("DELETE", `${BASE_URL}/api/user/experience/${expId}`, null, authParams);
        check(delExpRes, { 'delete experience status 200': (r) => r.status === 200 });
    }

    http.get(`${BASE_URL}/api/auth/logout`, authParams);

    sleep(6); // so as to not have TOO many requests since this is already way too overkill for the minimum
}
