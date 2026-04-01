/*
Load testing script

Written with the help of Gemini Flash 3.0

Requirements:
- k6 must be installed globally
- BASE_URL is a valid backend on the specified url must be running on render (paid tier)
- TEST_COMPANY_ID is a valid companyId (Test company)
- 20 accounts in the database with testuser1@test.com up to testuser20@test.com with passwords 123qwe

Performance target:
The system must be able to concurrently handle at least 20 users generating a total of 200 requests per minute.


How to run:
k6 run src/test/java/com/backend/coapp/_performance/loadtest.js

═══════════════════════════════════════════════════════════════════
PERFORMANCE ANALYSIS - TARGET MET AND EXCEEDED
═══════════════════════════════════════════════════════════════════

CONCURRENT USER TARGET: 20 users
────────────────────────────────
✓ MET: The test ran with exactly 20 virtual users (VUs) concurrently
  at peak load, simulating 20 independent authenticated sessions.
  All 20 VUs completed their iterations without interruption
  (227 complete, 0 interrupted).

REQUEST RATE TARGET: 200 requests per minute
─────────────────────────────────────────────
✓ EXCEEDED: The system handled 28.23 requests/second, which equals
  approximately 1,694 requests per minute — over 8x the required target.

  Target  :   200 req/min  (  3.33 req/s )
  Achieved: 1,694 req/min  ( 28.23 req/s )
  Excess  : +1,494 req/min ( +747% above target )

  3,405 total HTTP requests were completed successfully across the
  2-minute test window, with 0 failures recorded.

RELIABILITY
───────────
✓ PERFECT: 100% of all checks passed (3,178 out of 3,178).
  Every endpoint returned the expected HTTP status code on every
  single request across all 14 tested operations:

  - Authentication  : login (200)
  - User profile    : get profile (200)
  - Applications    : post (201), delete (200), search (200), filter (200)
  - Companies       : get (200)
  - Reviews         : post (201), delete (200)
  - Interviews      : get (200), get filtered (200)
  - AI quota        : get (200)
  - Experience      : post (200), delete (200)

  http_req_failed: 0.00% — zero failed HTTP requests out of 3,405.
  This confirms the backend is both highly available and correct
  under concurrent load.

RESPONSE TIME
─────────────
✓ ACCEPTABLE: Average response time was 546ms, with a median of 594ms.
  Given that the backend is hosted on Render (a cloud provider with
  cold-start and network latency characteristics), and that each
  request involves authenticated REST calls over the internet,
  these figures are reasonable and consistent.

  avg : 546ms
  med : 594ms
  p(90):  1.0s  — 90% of requests completed within 1 second
  p(95):  1.03s — 95% of requests completed within 1.03 seconds
  max :   1.72s — worst-case response, still within acceptable bounds

  The tight gap between p(90) and p(95) (only 30ms) indicates
  highly consistent and predictable performance under load, with
  no significant outliers or tail latency spikes.

CONCLUSION
──────────
The system comfortably meets and substantially exceeds the stated
performance requirements. Under a realistic multi-user workload
covering 14 distinct API operations — including authenticated CRUD
actions across applications, reviews, interviews, and experience —
the backend sustained over 1,694 requests per minute with 20
concurrent users, a 0% failure rate, and sub-second response times
at the 90th percentile. No bottlenecks, timeouts, or degradation
were observed during the test window.

═══════════════════════════════════════════════════════════════════
SAMPLE k6 OUTPUT (from a successful run):
═══════════════════════════════════════════════════════════════════

k6 run src/test/java/com/backend/coapp/_performance/loadtest.js

         /\      Grafana   /‾‾/
    /\  /  \     |\  __   /  /
   /  \/    \    | |/ /  /   ‾‾\
  /          \   |   (  |  (‾)  |
 / __________ \  |_|\_\  \_____/


     execution: local
        script: src/test/java/com/backend/coapp/_performance/loadtest.js
        output: -

     scenarios: (100.00%) 1 scenario, 20 max VUs, 2m30s max duration (incl. graceful stop):
              * default: Up to 20 looping VUs for 2m0s over 3 stages (gracefulRampDown: 30s, gracefulStop: 30s)



  █ TOTAL RESULTS

    checks_total.......: 3178    26.351949/s
    checks_succeeded...: 100.00% 3178 out of 3178
    checks_failed......: 0.00%   0 out of 3178

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
    http_req_duration..............: avg=546.26ms min=96.75ms med=594.21ms max=1.72s  p(90)=1s     p(95)=1.03s
      { expected_response:true }...: avg=546.26ms min=96.75ms med=594.21ms max=1.72s  p(90)=1s     p(95)=1.03s
    http_req_failed................: 0.00%  0 out of 3405
    http_reqs......................: 3405   28.234231/s

    EXECUTION
    iteration_duration.............: avg=8.2s     min=2s      med=9.12s    max=13.01s p(90)=10.93s p(95)=11.02s
    iterations.....................: 227    1.882282/s
    vus............................: 2      min=1         max=20
    vus_max........................: 20     min=20        max=20

    NETWORK
    data_received..................: 1.3 MB 11 kB/s
    data_sent......................: 377 kB 3.1 kB/s




running (2m00.6s), 00/20 VUs, 227 complete and 0 interrupted iterations
default ✓ [======================================] 00/20 VUs  2m0s

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

    // sleep(6); // so as to not have TOO many requests since this is already way too overkill for the minimum
}
