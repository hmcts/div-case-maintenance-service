package uk.gov.hmcts.reform.divorce.petition;

import io.restassured.response.Response;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.divorce.support.PetitionSupport;

import static org.junit.Assert.assertEquals;

public class CcdRetrieveAosCaseTest extends PetitionSupport {
    private static final String INVALID_USER_TOKEN = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJqdGkiOiIwOTg3NjU0M"
        + "yIsInN1YiI6IjEwMCIsImlhdCI6MTUwODk0MDU3MywiZXhwIjoxNTE5MzAzNDI3LCJkYXRhIjoiY2l0aXplbiIsInR5cGUiOiJBQ0NFU1MiL"
        + "CJpZCI6IjEwMCIsImZvcmVuYW1lIjoiSm9obiIsInN1cm5hbWUiOiJEb2UiLCJkZWZhdWx0LXNlcnZpY2UiOiJEaXZvcmNlIiwibG9hIjoxL"
        + "CJkZWZhdWx0LXVybCI6Imh0dHBzOi8vd3d3Lmdvdi51ayIsImdyb3VwIjoiZGl2b3JjZSJ9.lkNr1vpAP5_Gu97TQa0cRtHu8I-QESzu8kMX"
        + "CJOQrVU";

    private static final String TEST_AOS_RESPONDED_EVENT = "testAosStarted";
    private static final String TEST_AOS_COMPLETED_EVENT = "testAosCompleted";

    @Value("${case.maintenance.aos-case.context-path}")
    private String retrieveAosCaseContextPath;

    @Test
    public void givenJWTTokenIsNull_whenRetrieveAosCase_thenReturnBadRequest() {
        Response cmsResponse = getCase(null, null);

        assertEquals(HttpStatus.BAD_REQUEST.value(), cmsResponse.getStatusCode());
    }

    @Test
    public void givenInvalidUserToken_whenRetrieveAosCase_thenReturnForbiddenError() {
        Response cmsResponse = getCase(INVALID_USER_TOKEN, true);

        assertEquals(HttpStatus.FORBIDDEN.value(), cmsResponse.getStatusCode());
    }

    @Test
    public void givenNoCaseInCcd_whenRetrieveAosCase_thenReturnNull() {
        Response cmsResponse = getCase(getUserToken(), true);

        assertEquals(HttpStatus.NO_CONTENT.value(), cmsResponse.getStatusCode());
        assertEquals(cmsResponse.asString(), "");
    }

    @Test
    public void givenOneAosRespondedCaseInCcd_whenRetrieveAosCase_thenReturnTheCase() throws Exception {
        final String userToken = getUserToken();

        Response createCaseResponse = createACaseUpdateStateAndReturnTheCase(userToken, TEST_AOS_COMPLETED_EVENT);

        Response cmsResponse = getCase(userToken, true);

        assertEquals(HttpStatus.OK.value(), cmsResponse.getStatusCode());
        assertEquals((Long)createCaseResponse.path("id"), cmsResponse.path("id"));
    }

    @Test
    public void givenAosCompletedCaseInCcd_whenRetrieveAosCase_thenReturnTheFirstCase() throws Exception {
        final String userToken = getUserToken();

        Response createCaseResponse = createACaseUpdateStateAndReturnTheCase(userToken, TEST_AOS_COMPLETED_EVENT);

        createACaseUpdateStateAndReturnTheCase(userToken, TEST_AOS_COMPLETED_EVENT);

        Response cmsResponse = getCase(userToken, true);

        assertEquals(HttpStatus.OK.value(), cmsResponse.getStatusCode());
        assertEquals((Long)createCaseResponse.path("id"), cmsResponse.path("id"));
    }

    @Test
    public void givenMultipleCompletedAndOtherCaseInCcd_whenRetrieveAosCase_thenReturnFirstCompletedCase()
        throws Exception {
        final String userToken = getUserToken();

        getCaseIdFromSubmittingANewCase(userToken);

        Response createCaseResponse = createACaseUpdateStateAndReturnTheCase(userToken, TEST_AOS_COMPLETED_EVENT);

        createACaseUpdateStateAndReturnTheCase(userToken, TEST_AOS_COMPLETED_EVENT);

        Response cmsResponse = getCase(userToken, true);

        assertEquals(HttpStatus.OK.value(), cmsResponse.getStatusCode());
        assertEquals((Long)createCaseResponse.path("id"), cmsResponse.path("id"));
    }

    @Test
    public void givenAosStartedCaseInCcd_whenRetrieveAosCase_thenReturnTheCase() throws Exception {
        final String userToken = getUserToken();

        final Long caseId = createACaseUpdateStateAndReturnTheCase(userToken, TEST_AOS_RESPONDED_EVENT).path("id");

        Response cmsResponse = getCase(userToken, true);

        assertEquals(HttpStatus.OK.value(), cmsResponse.getStatusCode());
        assertEquals(caseId, cmsResponse.path("id"));
    }

    @Test
    public void givenMultipleAosStartedAndNoAosCompletedCaseInCcd_whenRetrieveAosCase_thenReturnMultipleChoice()
        throws Exception {
        final String userToken = getUserToken();

        createACaseUpdateStateAndReturnTheCase(userToken, TEST_AOS_RESPONDED_EVENT).path("id");
        createACaseUpdateStateAndReturnTheCase(userToken, TEST_AOS_RESPONDED_EVENT).path("id");

        Response cmsResponse = getCase(userToken, true);

        assertEquals(HttpStatus.MULTIPLE_CHOICES.value(), cmsResponse.getStatusCode());
    }

    @Test
    public void givenCasesInNotAwaitingPaymentOrAosCompletedCaseInCcd_whenRetrieveAosCase_thenReturnNull() throws Exception {
        final String userToken = getUserToken();

        getCaseIdFromSubmittingANewCase(userToken);

        Response cmsResponse = getCase(userToken, true);

        assertEquals(HttpStatus.NO_CONTENT.value(), cmsResponse.getStatusCode());
        assertEquals(cmsResponse.asString(), "");
    }

    private Response createACaseUpdateStateAndReturnTheCase(String userToken, String eventName) throws Exception {
        Long caseId = getCaseIdFromSubmittingANewCase(userToken);

        return updateCase((String)null, caseId, eventName, userToken);
    }

    @Override
    protected String getRequestUrl() {
        return serverUrl + retrieveAosCaseContextPath;
    }
}
