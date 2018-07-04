package uk.gov.hmcts.reform.divorce;

import io.restassured.response.Response;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class CcdPetitionServiceTest extends CcdUpdateSupport {
    private static final String CHECK_CCD = "checkCcd";

    private static final String INVALID_USER_TOKEN = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJqdGkiOiIwOTg3NjU0M"
        + "yIsInN1YiI6IjEwMCIsImlhdCI6MTUwODk0MDU3MywiZXhwIjoxNTE5MzAzNDI3LCJkYXRhIjoiY2l0aXplbiIsInR5cGUiOiJBQ0NFU1MiL"
        + "CJpZCI6IjEwMCIsImZvcmVuYW1lIjoiSm9obiIsInN1cm5hbWUiOiJEb2UiLCJkZWZhdWx0LXNlcnZpY2UiOiJEaXZvcmNlIiwibG9hIjoxL"
        + "CJkZWZhdWx0LXVybCI6Imh0dHBzOi8vd3d3Lmdvdi51ayIsImdyb3VwIjoiZGl2b3JjZSJ9.lkNr1vpAP5_Gu97TQa0cRtHu8I-QESzu8kMX"
        + "CJOQrVU";

    @Value("${case.maintenance.petition.context-path}")
    private String contextPath;

    @Test
    public void givenJWTTokenIsNull_whenRetrievePetition_thenReturnBadRequest() {
        Response cmsResponse = getPetition(null, null);

        assertEquals(HttpStatus.BAD_REQUEST.value(), cmsResponse.getStatusCode());
    }

    @Test
    public void givenInvalidUserToken_whenRetrievePetition_thenReturnForbiddenError() {
        Response cmsResponse = getPetition(INVALID_USER_TOKEN, true);

        assertEquals(HttpStatus.FORBIDDEN.value(), cmsResponse.getStatusCode());
    }

    @Test
    public void givenNoCaseInCcd_whenRetrievePetition_thenReturnNull() {
        Response cmsResponse = getPetition(getUserToken(), true);

        assertEquals(HttpStatus.OK.value(), cmsResponse.getStatusCode());
        assertEquals(cmsResponse.asString(), "");
    }

    @Test
    public void givenOneSubmittedCaseInCcd_whenRetrievePetition_thenReturnTheCase() throws Exception {
        String userToken = getUserToken();

        Response createCaseResponse = createACaseMakePaymentAndReturnTheCase("addresses.json", userToken);

        Response cmsResponse = getPetition(userToken, true);

        assertEquals(HttpStatus.OK.value(), cmsResponse.getStatusCode());
        assertEquals((Long)createCaseResponse.path("id"), cmsResponse.path("id"));
    }

    @Test
    public void givenMultipleSubmittedCaseInCcd_whenRetrievePetition_thenReturnTheFirstCase() throws Exception {
        String userToken = getUserToken();

        Response createCaseResponse = createACaseMakePaymentAndReturnTheCase("addresses.json", userToken);

        createACaseMakePaymentAndReturnTheCase("addresses.json", userToken);

        Response cmsResponse = getPetition(userToken, true);

        assertEquals(HttpStatus.OK.value(), cmsResponse.getStatusCode());
        assertEquals((Long)createCaseResponse.path("id"), cmsResponse.path("id"));
    }

    @Test
    public void givenMultipleSubmittedAndOtherCaseInCcd_whenRetrievePetition_thenReturnFirstSubmittedCase()
        throws Exception {
        String userToken = getUserToken();

        getCaseIdFromSubmittingANewCase("addresses.json", userToken);

        Response createCaseResponse = createACaseMakePaymentAndReturnTheCase("addresses.json", userToken);

        createACaseMakePaymentAndReturnTheCase("addresses.json", userToken);

        Response cmsResponse = getPetition(userToken, true);

        assertEquals(HttpStatus.OK.value(), cmsResponse.getStatusCode());
        assertEquals((Long)createCaseResponse.path("id"), cmsResponse.path("id"));
    }

    @Test
    public void givenOneAwaitingPaymentCaseInCcd_whenRetrievePetition_thenReturnTheCase() throws Exception {
        String userToken = getUserToken();

        Long caseId = getCaseIdFromSubmittingANewCase("addresses.json", userToken);

        Response cmsResponse = getPetition(userToken, true);

        assertEquals(HttpStatus.OK.value(), cmsResponse.getStatusCode());
        assertEquals(caseId, cmsResponse.path("id"));
    }

    @Test
    public void givenMultipleAwaitingPaymentAndNoSubmittedCaseInCcd_whenRetrievePetition_thenReturnMultipleChoice()
        throws Exception {
        String userToken = getUserToken();

        getCaseIdFromSubmittingANewCase("addresses.json", userToken);
        getCaseIdFromSubmittingANewCase("addresses.json", userToken);

        Response cmsResponse = getPetition(userToken, true);

        assertEquals(HttpStatus.MULTIPLE_CHOICES.value(), cmsResponse.getStatusCode());
    }

    @Test
    public void givenCasesInNotAwaitingPaymentOrNonSubmittedCaseInCcd_whenRetrievePetition_thenReturnNull() {
        String userToken = getUserToken();

        Response cmsResponse = getPetition(userToken, true);

        assertEquals(HttpStatus.OK.value(), cmsResponse.getStatusCode());
        assertEquals(cmsResponse.asString(), "");
    }

    private Response getPetition(String userToken, Boolean checkCcd) {
        return
            RestUtil.getFromRestService(
                getRequestUrl(),
                getHeaders(userToken),
                checkCcd == null ? null : Collections.singletonMap(CHECK_CCD, checkCcd)
            );
    }

    private String getRequestUrl() {
        return serverUrl + contextPath;
    }

    private Response createACaseMakePaymentAndReturnTheCase(String filePath, String userToken) throws Exception {
        Long caseId = getCaseIdFromSubmittingANewCase(filePath, userToken);

        return updateCase("payment-made.json", caseId, EVENT_ID, userToken);
    }
}
