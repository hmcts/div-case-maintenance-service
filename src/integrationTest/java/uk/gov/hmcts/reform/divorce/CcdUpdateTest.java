package uk.gov.hmcts.reform.divorce;

import io.restassured.response.Response;
import org.junit.Test;
import org.springframework.http.HttpStatus;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class CcdUpdateTest extends CcdUpdateSupport {
    private static final String INVALID_USER_TOKEN = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJqdGkiOiIwOTg3NjU0M"
        + "yIsInN1YiI6IjEwMCIsImlhdCI6MTUwODk0MDU3MywiZXhwIjoxNTE5MzAzNDI3LCJkYXRhIjoiY2l0aXplbiIsInR5cGUiOiJBQ0NFU1MiL"
        + "CJpZCI6IjEwMCIsImZvcmVuYW1lIjoiSm9obiIsInN1cm5hbWUiOiJEb2UiLCJkZWZhdWx0LXNlcnZpY2UiOiJEaXZvcmNlIiwibG9hIjoxL"
        + "CJkZWZhdWx0LXVybCI6Imh0dHBzOi8vd3d3Lmdvdi51ayIsImdyb3VwIjoiZGl2b3JjZSJ9.lkNr1vpAP5_Gu97TQa0cRtHu8I-QESzu8kMX"
        + "CJOQrVU";

    private static final String UNAUTHORISED_JWT_EXCEPTION = "status 403 reading "
        + "IdamUserService#retrieveUserDetails(String); content:\n";

    @Test
    public void shouldReturnCaseIdWhenUpdatingDataAfterInitialSubmit() throws Exception {
        String userToken = getUserToken();

        Long caseId = getCaseIdFromSubmittingANewCase("addresses.json", userToken);

        Response cmsResponse = updateCase("update-addresses.json", caseId, EVENT_ID, userToken);

        assertEquals(HttpStatus.OK.value(), cmsResponse.getStatusCode());
        assertEquals(caseId, cmsResponse.getBody().path("id"));
    }

    @Test
    public void shouldReturnCaseIdWhenUpdatingPaymentAfterUpdatingWithPaymentReference() throws Exception {
        String userToken = getUserToken();

        Long caseId = getCaseIdFromSubmittingANewCase("addresses.json", userToken);

        Response cmsResponse = updateCase("payment-made.json", caseId, EVENT_ID, userToken);

        assertEquals(HttpStatus.OK.value(), cmsResponse.getStatusCode());
        assertEquals(caseId, cmsResponse.getBody().path("id"));
    }

    @Test
    public void shouldReturnErrorForNonExistingCaseId() throws Exception {
        String userToken = getUserToken();

        Response cmsResponse = updateCase("payment-made.json", -1L, EVENT_ID, userToken);

        assertEquals(HttpStatus.BAD_REQUEST.value(), cmsResponse.getStatusCode());
        assertThat(cmsResponse.asString(), containsString("Case reference is not valid"));
    }

    @Test
    public void shouldReturnErrorForInvalidEventId() throws Exception {
        String userToken = getUserToken();

        Long caseId = getCaseIdFromSubmittingANewCase("addresses.json", userToken);

        Response cmsResponse = updateCase("payment-made.json", caseId, "InvalidEvenId", userToken);

        assertEquals(HttpStatus.NOT_FOUND.value(), cmsResponse.getStatusCode());
        assertThat(cmsResponse.asString(),
            containsString("Cannot findCaseEvent event InvalidEvenId for case type DIVORCE"));
    }

    @Test
    public void shouldReturnErrorUpdatingWithCaseSameEventId() throws Exception {
        String userToken = getUserToken();

        Long caseId = getCaseIdFromSubmittingANewCase("addresses.json", userToken);

        updateCase("payment-made.json", caseId, EVENT_ID, userToken);

        Response cmsResponse = updateCase("payment-made.json", caseId, EVENT_ID, userToken);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), cmsResponse.getStatusCode());
        assertThat(cmsResponse.asString(),
            containsString("The case status did not qualify for the event"));
    }

    @Test
    public void shouldReturnErrorForInvalidUserJwtToken() throws Exception {
        String userToken = getUserToken();

        Long caseId = getCaseIdFromSubmittingANewCase("addresses.json", userToken);

        Response cmsResponse = updateCase("payment-made.json", caseId, EVENT_ID, INVALID_USER_TOKEN);

        assertEquals(HttpStatus.FORBIDDEN.value(), cmsResponse.getStatusCode());
        assertEquals(UNAUTHORISED_JWT_EXCEPTION, cmsResponse.asString());
    }
}
