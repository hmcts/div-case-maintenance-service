package uk.gov.hmcts.reform.divorce.support;

import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.divorce.context.IntegrationTest;
import uk.gov.hmcts.reform.divorce.util.ResourceLoader;
import uk.gov.hmcts.reform.divorce.util.RestUtil;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public abstract class CcdSubmissionSupport extends IntegrationTest {
    private static final String PAYLOAD_CONTEXT_PATH = "ccd-submission-payload/";

    @Value("${case.maintenance.submission.context-path}")
    private String contextPath;

    protected void submitAndAssertSuccess(String fileName) throws Exception {
        Response cmsResponse = submitCase(fileName);
        assertOkResponseAndCaseIdIsNotZero(cmsResponse);
    }

    private Response submitCase(String fileName) throws Exception {
        return
            RestUtil.postToRestService(
                getSubmissionRequestUrl(),
                getHeaders(),
                loadJson(fileName)
            );
    }

    protected Response submitCase(String fileName, String userToken) throws Exception {
        return submitCaseJson(loadJson(fileName), userToken);
    }

    protected Response submitCaseJson(String jsonCase, String userToken) {
        return
            RestUtil.postToRestService(
                getSubmissionRequestUrl(),
                getHeaders(userToken),
                jsonCase
            );
    }

    private String loadJson(String fileName) throws Exception {
        return loadJson(fileName, PAYLOAD_CONTEXT_PATH);
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
        assertEquals(HttpStatus.OK.value(), cmsResponse.getStatusCode());
        assertNotEquals((Long)0L, cmsResponse.getBody().path("caseId"));
    }

    protected void assertCaseStatus(Response cmsResponse, String caseStatus) {
        assertTrue(String.format("Expected [%s] status not found", caseStatus) ,
            cmsResponse.getBody().asString().contains(caseStatus));
    }
}
