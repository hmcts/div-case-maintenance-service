package uk.gov.hmcts.reform.divorce.petition;

import io.restassured.response.Response;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.support.PetitionSupport;
import uk.gov.hmcts.reform.divorce.util.ResourceLoader;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class CcdRetrieveCaseTest extends PetitionSupport {
    private static final String CASE_DATA_JSON_PATH = "case_data";

    private static final String INVALID_USER_TOKEN = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJqdGkiOiIwOTg3NjU0M"
        + "yIsInN1YiI6IjEwMCIsImlhdCI6MTUwODk0MDU3MywiZXhwIjoxNTE5MzAzNDI3LCJkYXRhIjoiY2l0aXplbiIsInR5cGUiOiJBQ0NFU1MiL"
        + "CJpZCI6IjEwMCIsImZvcmVuYW1lIjoiSm9obiIsInN1cm5hbWUiOiJEb2UiLCJkZWZhdWx0LXNlcnZpY2UiOiJEaXZvcmNlIiwibG9hIjoxL"
        + "CJkZWZhdWx0LXVybCI6Imh0dHBzOi8vd3d3Lmdvdi51ayIsImdyb3VwIjoiZGl2b3JjZSJ9.lkNr1vpAP5_Gu97TQa0cRtHu8I-QESzu8kMX"
        + "CJOQrVU";

    @Test
    public void givenJWTTokenIsNull_whenRetrieveCase_thenReturnBadRequest() {
        Response cmsResponse = getCase(null, null);

        assertEquals(HttpStatus.BAD_REQUEST.value(), cmsResponse.getStatusCode());
    }

    @Test
    public void givenInvalidUserToken_whenRetrieveCase_thenReturnForbiddenError() {
        Response cmsResponse = getCase(INVALID_USER_TOKEN, true);

        assertEquals(HttpStatus.FORBIDDEN.value(), cmsResponse.getStatusCode());
    }

    @Test
    public void givenNoCaseInCcdOrDraftStore_whenRetrieveCase_thenReturnNull() {
        Response cmsResponse = getCase(getUserToken(), true);

        assertEquals(HttpStatus.NO_CONTENT.value(), cmsResponse.getStatusCode());
        assertEquals(cmsResponse.asString(), "");
    }

    @Test
    public void givenOneSubmittedCaseInCcd_whenRetrieveCase_thenReturnTheCase() throws Exception {
        final String userToken = getUserToken();

        Response createCaseResponse = createACaseMakePaymentAndReturnTheCase(userToken);

        Response cmsResponse = getCase(userToken, true);

        assertEquals(HttpStatus.OK.value(), cmsResponse.getStatusCode());
        assertEquals((Long)createCaseResponse.path("id"), cmsResponse.path("id"));
    }

    @Test
    public void givenMultipleSubmittedCaseInCcd_whenRetrieveCase_thenReturnTheFirstCase() throws Exception {
        final String userToken = getUserToken();

        Response createCaseResponse = createACaseMakePaymentAndReturnTheCase(userToken);

        createACaseMakePaymentAndReturnTheCase(userToken);

        Response cmsResponse = getCase(userToken, true);

        assertEquals(HttpStatus.OK.value(), cmsResponse.getStatusCode());
        assertEquals((Long)createCaseResponse.path("id"), cmsResponse.path("id"));
    }

    @Test
    public void givenMultipleSubmittedAndOtherCaseInCcd_whenRetrieveCase_thenReturnFirstSubmittedCase()
        throws Exception {
        final String userToken = getUserToken();

        getCaseIdFromSubmittingANewCase(userToken);

        Response createCaseResponse = createACaseMakePaymentAndReturnTheCase(userToken);

        createACaseMakePaymentAndReturnTheCase(userToken);

        Response cmsResponse = getCase(userToken, true);

        assertEquals(HttpStatus.OK.value(), cmsResponse.getStatusCode());
        assertEquals((Long)createCaseResponse.path("id"), cmsResponse.path("id"));
    }

    @Test
    public void givenOneAwaitingPaymentCaseInCcd_whenRetrieveCase_thenReturnTheCase() throws Exception {
        final String userToken = getUserToken();

        final Long caseId = getCaseIdFromSubmittingANewCase(userToken);

        Response cmsResponse = getCase(userToken, true);

        assertEquals(HttpStatus.OK.value(), cmsResponse.getStatusCode());
        assertEquals(caseId, cmsResponse.path("id"));
    }

    @Test
    public void givenMultipleAwaitingPaymentAndNoSubmittedCaseInCcd_whenRetrieveCase_thenReturnMultipleChoice()
        throws Exception {
        final String userToken = getUserToken();

        getCaseIdFromSubmittingANewCase(userToken);
        getCaseIdFromSubmittingANewCase(userToken);

        Response cmsResponse = getCase(userToken, true);

        assertEquals(HttpStatus.MULTIPLE_CHOICES.value(), cmsResponse.getStatusCode());
    }

    @Test
    public void givenCasesInNotAwaitingPaymentOrNonSubmittedCaseInCcd_whenRetrieveCase_thenReturnNull() {
        final String userToken = getUserToken();

        Response cmsResponse = getCase(userToken, true);

        assertEquals(HttpStatus.NO_CONTENT.value(), cmsResponse.getStatusCode());
        assertEquals(cmsResponse.asString(), "");
    }

    @Test
    public void givenDoNotCheckCcdAndOnePetitionInDivorceFormat_whenRetrieveCase_thenReturnPetition() throws Exception {
        final String userToken = getUserToken();

        final String caseInDivorceFormatFileName = DIVORCE_FORMAT_DRAFT_CONTEXT_PATH + "addresses.json";

        createDraft(userToken, caseInDivorceFormatFileName,
            Collections.singletonMap(DIVORCE_FORMAT_KEY, true));

        Response cmsResponse = getCase(userToken, false);

        assertEquals(HttpStatus.OK.value(), cmsResponse.getStatusCode());
        
        assertEquals( ResourceLoader.loadJsonToObject(caseInDivorceFormatFileName, Map.class),
            cmsResponse.getBody().path(CASE_DATA_JSON_PATH));

        deleteDraft(userToken);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void givenDoNotCheckCcdAndMultiplePetitionsInDivorceFormat_whenRetrieveCase_thenReturnLatestPetition()
        throws Exception {
        final UserDetails userDetails = getUserDetails();

        final String caseInDivorceFormatFileName1 = DIVORCE_FORMAT_DRAFT_CONTEXT_PATH + "addresses.json";
        final String caseInCcdFormatFileName2 = DIVORCE_FORMAT_DRAFT_CONTEXT_PATH + "jurisdiction-6-12.json";

        createDraft(userDetails.getAuthToken(), caseInDivorceFormatFileName1,
            Collections.singletonMap(DIVORCE_FORMAT_KEY, true));
        createDraft(userDetails.getAuthToken(), caseInCcdFormatFileName2,
            Collections.singletonMap(DIVORCE_FORMAT_KEY, true));

        Response cmsResponse = getCase(userDetails.getAuthToken(), false);

        Map<String, Object> expected =
            ResourceLoader.loadJsonToObject(caseInDivorceFormatFileName1, Map.class);

        assertEquals(HttpStatus.OK.value(), cmsResponse.getStatusCode());

        assertEquals(cmsResponse.getBody().path(CASE_DATA_JSON_PATH), expected);

        //delete removes only the first draft. So delete needs to be called twice here
        deleteDraft(userDetails.getAuthToken());
        deleteDraft(userDetails.getAuthToken());
    }

    private Response createACaseMakePaymentAndReturnTheCase(String userToken) throws Exception {
        Long caseId = getCaseIdFromSubmittingANewCase(userToken);

        return updateCase("payment-made.json", caseId, EVENT_ID, userToken);
    }
}
