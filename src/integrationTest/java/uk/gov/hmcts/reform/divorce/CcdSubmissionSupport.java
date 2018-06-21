package uk.gov.hmcts.reform.divorce;

import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public abstract class CcdSubmissionSupport extends IntegrationTest {
    private static final String PAYLOAD_CONTEXT_PATH = "ccd-submission-payload/";

    @Value("${case.maintenance.submission.context-path}")
    private String contextPath;

    void submitAndAssertSuccess(String fileName) throws Exception {
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

    Response submitCase(String fileName, String userToken) throws Exception {
        return
            RestUtil.postToRestService(
                getSubmissionRequestUrl(),
                getHeaders(userToken),
                loadJson(fileName)
            );
    }

    private String loadJson(String fileName) throws Exception{
        return loadJson(fileName, PAYLOAD_CONTEXT_PATH);
    }

    String loadJson(String fileName, String contextPath) throws Exception{
        return ResourceLoader.loadJson(contextPath + fileName);
    }

    String getSubmissionRequestUrl(){
        return serverUrl + contextPath;
    }

    Map<String, Object> getHeaders(){
        return getHeaders(getUserToken());
    }

    Map<String, Object> getHeaders(String userToken){
        Map<String, Object> headers = new HashMap<>();
        headers.put("Content-type", MediaType.APPLICATION_JSON_UTF8_VALUE);
        headers.put("Authorization", userToken);

        return headers;
    }

    private void assertOkResponseAndCaseIdIsNotZero(Response cmsResponse) {
        assertEquals(HttpStatus.OK.value(), cmsResponse.getStatusCode());
        assertNotEquals((Long)0L, cmsResponse.getBody().path("caseId"));
    }
}
