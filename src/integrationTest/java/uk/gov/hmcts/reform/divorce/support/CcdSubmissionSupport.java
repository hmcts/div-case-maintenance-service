package uk.gov.hmcts.reform.divorce.support;

import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.divorce.context.IntegrationTest;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.util.ResourceLoader;
import uk.gov.hmcts.reform.divorce.util.RestUtil;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public abstract class CcdSubmissionSupport extends IntegrationTest {
    private static final String PAYLOAD_CONTEXT_PATH = "ccd-submission-payload/";
    private static final String PETITIONER_DEFAULT_EMAIL = "simulate-delivered@notifications.service.gov.uk";
    @Value("${case.maintenance.submission.context-path}")
    private String contextPath;

    @Value("${env}")
    private String testEnvironment;

    protected void submitAndAssertSuccess(String fileName) throws Exception {
        Response cmsResponse = submitCase(fileName);
        assertOkResponseAndCaseIdIsNotZero(cmsResponse);
    }

    private Response submitCase(String fileName) throws Exception {
        return submitCase(fileName, getUserDetails());
    }

    protected Response submitCase(String fileName, UserDetails userDetails) throws Exception {
        return submitCaseJson(loadJson(fileName, userDetails), userDetails.getAuthToken());
    }

    protected Response submitCaseJson(String jsonCase, String userToken) {
        return
            RestUtil.postToRestService(
                getSubmissionRequestUrl(),
                getHeaders(userToken),
                jsonCase
            );
    }

    String loadJson(String fileName, UserDetails userDetails) throws Exception {
        // Update document links in the Json String to be current environment
        String payload = loadJson(fileName, PAYLOAD_CONTEXT_PATH);
        if (!testEnvironment.equals("local")) {
            payload = payload.replaceAll("-aat", "-".concat(testEnvironment));
        }
        return payload
            .replaceAll(PETITIONER_DEFAULT_EMAIL, userDetails.getEmailAddress());
    }

    String loadJson(String fileName, String contextPath) throws Exception {
        return ResourceLoader.loadJson(contextPath + fileName);
    }

    protected String getSubmissionRequestUrl() {
        return serverUrl + contextPath;
    }

    protected Map<String, Object> getHeaders() {
        return getHeaders(getUserToken());
    }

    Map<String, Object> getHeaders(String userToken) {
        Map<String, Object> headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE);

        if (userToken != null) {
            headers.put(HttpHeaders.AUTHORIZATION, userToken);
        }

        return headers;
    }

    protected void assertOkResponseAndCaseIdIsNotZero(Response cmsResponse) {
        assertEquals(cmsResponse.getBody().asString(), HttpStatus.OK.value(), cmsResponse.getStatusCode());
        assertNotEquals((Long)0L, cmsResponse.getBody().path("caseId"));
    }

    protected void assertCaseStatus(Response cmsResponse, String caseStatus) {
        assertTrue(String.format("Expected [%s] status not found", caseStatus) ,
            cmsResponse.getBody().asString().contains(caseStatus));
    }
}
