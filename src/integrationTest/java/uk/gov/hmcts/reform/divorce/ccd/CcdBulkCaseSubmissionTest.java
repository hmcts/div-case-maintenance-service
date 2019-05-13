package uk.gov.hmcts.reform.divorce.ccd;

import io.restassured.response.Response;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.support.PetitionSupport;
import uk.gov.hmcts.reform.divorce.util.RestUtil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CcdBulkCaseSubmissionTest extends PetitionSupport {

    private static final String INVALID_USER_TOKEN = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJqdGkiOiIwOTg3NjU0M"
        + "yIsInN1YiI6IjEwMCIsImlhdCI6MTUwODk0MDU3MywiZXhwIjoxNTE5MzAzNDI3LCJkYXRhIjoiY2l0aXplbiIsInR5cGUiOiJBQ0NFU1MiL"
        + "CJpZCI6IjEwMCIsImZvcmVuYW1lIjoiSm9obiIsInN1cm5hbWUiOiJEb2UiLCJkZWZhdWx0LXNlcnZpY2UiOiJEaXZvcmNlIiwibG9hIjoxL"
        + "CJkZWZhdWx0LXVybCI6Imh0dHBzOi8vd3d3Lmdvdi51ayIsImdyb3VwIjoiZGl2b3JjZSJ9.lkNr1vpAP5_Gu97TQa0cRtHu8I-QESzu8kMX"
        + "CJOQrVU";
    private static final String  UNAUTHORISED_JWT_EXCEPTION = "status 403 reading "
        + "IdamApiClient#retrieveUserDetails(String) - ";
    private static final String REQUEST_BODY_NOT_FOUND = "Required request body is missing";

    private static final String USER_EMAIL = "test@test.com";

    private static final String CASE_PAYLOAD_PATH = "bulk-case.json";
    @Test
    public void shouldReturnCaseId() {
        UserDetails caseWorkerUser = getCaseWorkerUser();

        String expectedStatus = "Created";
        Response caseSubmitted = submitBulkCase(CASE_PAYLOAD_PATH, caseWorkerUser);
        assertOkResponseAndCaseIdIsNotZero(caseSubmitted);
        assertCaseStatus(caseSubmitted, expectedStatus);
    }

    @Test
    public void shouldReturnErrorForInvalidUserJwtToken() {
        Response cmsResponse = submitBulkCase(CASE_PAYLOAD_PATH, UserDetails.builder()
            .authToken(INVALID_USER_TOKEN)
            .emailAddress(USER_EMAIL)
            .build());

        assertEquals(HttpStatus.FORBIDDEN.value(), cmsResponse.getStatusCode());
        assertEquals(UNAUTHORISED_JWT_EXCEPTION, cmsResponse.asString());
    }

    @Test
    public void shouldReturnBadRequestForNoRequestBody() {
        Response cmsResponse = RestUtil.postToRestService(
            getBulkCaseSubmissionRequestUrl(),
            getHeaders(),
            null
        );

        assertEquals(HttpStatus.BAD_REQUEST.value(), cmsResponse.getStatusCode());
        assertTrue(cmsResponse.getBody().asString().contains(REQUEST_BODY_NOT_FOUND));
    }

}
