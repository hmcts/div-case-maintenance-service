package uk.gov.hmcts.reform.divorce.petition;

import io.restassured.response.Response;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.support.PetitionSupport;

import static java.util.Collections.singletonMap;
import static org.junit.Assert.assertEquals;

public class CcdRetrieveAosCaseTest extends PetitionSupport {
    private static final String INVALID_USER_TOKEN = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJqdGkiOiIwO"
            + "Tg3NjU0MyIsInN1YiI6IjEwMCIsImlhdCI6MTUwODk0MDU3MywiZXhwIjoxNTE5MzAzNDI3LCJkYXRhIjoiY2l0aXplbiIsInR"
            + "5cGUiOiJBQ0NFU1MiLCJpZCI6IjEwMCIsImZvcmVuYW1lIjoiSm9obiIsInN1cm5hbWUiOiJEb2UiLCJkZWZhdWx0LXNlcnZpY"
            + "2UiOiJEaXZvcmNlIiwibG9hIjoxLCJkZWZhdWx0LXVybCI6Imh0dHBzOi8vd3d3Lmdvdi51ayIsImdyb3VwIjoiZGl2b3JjZSJ"
            + "9.lkNr1vpAP5_Gu97TQa0cRtHu8I-QESzu8kMXCJOQrVU";

    private static final String TEST_AOS_RESPONDED_EVENT = "testAosStarted";
    private static final String TEST_AOS_AWAITING_DN = "testAwaitingDecreeNisi";

    @Value("${case.maintenance.aos-case.context-path}")
    private String retrieveAosCaseContextPath;

    @Test
    public void givenJWTTokenIsNull_whenRetrieveAosCase_thenReturnBadRequest() {
        Response cmsResponse = retrieveCase(null);

        assertEquals(HttpStatus.BAD_REQUEST.value(), cmsResponse.getStatusCode());
    }

    @Test
    public void givenInvalidUserToken_whenRetrieveAosCase_thenReturnForbiddenError() {
        Response cmsResponse = retrieveCase(INVALID_USER_TOKEN);

        assertEquals(HttpStatus.FORBIDDEN.value(), cmsResponse.getStatusCode());
    }

    @Test
    public void givenNoCaseInCcd_whenRetrieveAosCase_thenReturnNull() {
        Response cmsResponse = retrieveCase(getUserToken());

        assertEquals(HttpStatus.NO_CONTENT.value(), cmsResponse.getStatusCode());
        assertEquals(cmsResponse.asString(), "");
    }

    @Test
    public void whenUserAlreadyHasDraftSaved_AndTriesToLogInAsRespondent_ThenCaseIsNotFound() throws Exception {
        //Create a draft
        final String userToken = getUserToken();
        final String filePath = DIVORCE_FORMAT_DRAFT_CONTEXT_PATH + "addresses.json";
        Response draftCreationResponse = createDraft(userToken, filePath, singletonMap(DIVORCE_FORMAT_KEY, "true"));
        assertEquals(HttpStatus.OK.value(), draftCreationResponse.getStatusCode());

        //Query AOS case
        Response cmsResponse = retrieveCase(userToken);

        //Response should be not found
        assertEquals(HttpStatus.NO_CONTENT.value(), cmsResponse.getStatusCode());
        assertEquals(cmsResponse.asString(), "");
    }

    @Test
    public void givenOneAosRespondedCaseInCcd_whenRetrieveAosCase_thenReturnTheCase() throws Exception {
        final UserDetails userDetails = getUserDetails();

        Response createCaseResponse = createACaseUpdateStateAndReturnTheCase(userDetails, TEST_AOS_AWAITING_DN);

        Response cmsResponse = retrieveCase(userDetails.getAuthToken());

        assertEquals(HttpStatus.OK.value(), cmsResponse.getStatusCode());
        assertEquals((Long) createCaseResponse.path("id"), cmsResponse.path("id"));
    }

    @Test
    public void givenAosCompletedCaseInCcd_whenRetrieveAosCase_thenReturnTheFirstCase() throws Exception {
        final UserDetails userDetails = getUserDetails();

        Response createCaseResponse = createACaseUpdateStateAndReturnTheCase(userDetails, TEST_AOS_AWAITING_DN);

        createACaseUpdateStateAndReturnTheCase(userDetails, TEST_AOS_AWAITING_DN);

        Response cmsResponse = retrieveCase(userDetails.getAuthToken());

        assertEquals(HttpStatus.OK.value(), cmsResponse.getStatusCode());
        assertEquals((Long) createCaseResponse.path("id"), cmsResponse.path("id"));
    }

    @Test
    public void givenMultipleCompletedAndOtherCaseInCcd_whenRetrieveAosCase_thenReturnFirstCompletedCase()
            throws Exception {
        final UserDetails userDetails = getUserDetails();

        getCaseIdFromSubmittingANewCase(userDetails);

        Response createCaseResponse = createACaseUpdateStateAndReturnTheCase(userDetails, TEST_AOS_AWAITING_DN);

        createACaseUpdateStateAndReturnTheCase(userDetails, TEST_AOS_AWAITING_DN);

        Response cmsResponse = retrieveCase(userDetails.getAuthToken());

        assertEquals(HttpStatus.OK.value(), cmsResponse.getStatusCode());
        assertEquals((Long) createCaseResponse.path("id"), cmsResponse.path("id"));
    }

    @Test
    public void givenAosStartedCaseInCcd_whenRetrieveAosCase_thenReturnTheCase() throws Exception {
        final UserDetails userDetails = getUserDetails();

        final Long caseId = createACaseUpdateStateAndReturnTheCase(userDetails, TEST_AOS_RESPONDED_EVENT).path("id");

        Response cmsResponse = retrieveCase(userDetails.getAuthToken());

        assertEquals(HttpStatus.OK.value(), cmsResponse.getStatusCode());
        assertEquals(caseId, cmsResponse.path("id"));
    }

    @Test
    public void givenMultipleAosStartedAndNoAosCompletedCaseInCcd_whenRetrieveAosCase_thenReturnMultipleChoice()
            throws Exception {
        final UserDetails userDetails = getUserDetails();

        createACaseUpdateStateAndReturnTheCase(userDetails, TEST_AOS_RESPONDED_EVENT).prettyPrint();
        createACaseUpdateStateAndReturnTheCase(userDetails, TEST_AOS_RESPONDED_EVENT).prettyPrint();

        Response cmsResponse = retrieveCase(userDetails.getAuthToken());

        assertEquals(HttpStatus.MULTIPLE_CHOICES.value(), cmsResponse.getStatusCode());
    }

    @Test
    public void givenCasesInNotAwaitingPaymentOrAosCompletedCaseInCcd_whenRetrieveAosCase_thenReturnNull() throws Exception {
        final UserDetails userDetails = getUserDetails();

        getCaseIdFromSubmittingANewCase(userDetails);

        Response cmsResponse = retrieveCase(userDetails.getAuthToken());

        assertEquals(HttpStatus.NO_CONTENT.value(), cmsResponse.getStatusCode());
        assertEquals(cmsResponse.asString(), "");
    }

    private Response createACaseUpdateStateAndReturnTheCase(UserDetails userDetails, String eventName) throws Exception {
        Long caseId = getCaseIdFromSubmittingANewCase(userDetails);

        return updateCase((String) null, caseId, eventName, userDetails.getAuthToken());
    }

    @Override
    protected String getRetrieveCaseRequestUrl() {
        return serverUrl + retrieveAosCaseContextPath;
    }
}
