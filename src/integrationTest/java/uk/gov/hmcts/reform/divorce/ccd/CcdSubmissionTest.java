package uk.gov.hmcts.reform.divorce.ccd;

import io.restassured.response.Response;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.divorce.support.PetitionSupport;
import uk.gov.hmcts.reform.divorce.util.RestUtil;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class CcdSubmissionTest extends PetitionSupport {
    private static final String INVALID_USER_TOKEN = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJqdGkiOiIwOTg3NjU0M"
        + "yIsInN1YiI6IjEwMCIsImlhdCI6MTUwODk0MDU3MywiZXhwIjoxNTE5MzAzNDI3LCJkYXRhIjoiY2l0aXplbiIsInR5cGUiOiJBQ0NFU1MiL"
        + "CJpZCI6IjEwMCIsImZvcmVuYW1lIjoiSm9obiIsInN1cm5hbWUiOiJEb2UiLCJkZWZhdWx0LXNlcnZpY2UiOiJEaXZvcmNlIiwibG9hIjoxL"
        + "CJkZWZhdWx0LXVybCI6Imh0dHBzOi8vd3d3Lmdvdi51ayIsImdyb3VwIjoiZGl2b3JjZSJ9.lkNr1vpAP5_Gu97TQa0cRtHu8I-QESzu8kMX"
        + "CJOQrVU";
    private static final String  UNAUTHORISED_JWT_EXCEPTION = "status 403 reading "
        + "IdamApiClient#retrieveUserDetails(String); content:\n";
    private static final String REQUEST_BODY_NOT_FOUND = "Required request body is missing: public org.springframework."
        + "http.ResponseEntity<uk.gov.hmcts.reform.ccd.client.model.CaseDetails> uk.gov.hmcts.reform.divorce.casemainte"
        + "nanceservice.controller.CcdController.submitCase(java.lang.Object,java.lang.String)";


    @Test
    public void shouldReturnCaseIdForValidAddressesSessionData() throws Exception {
        String expectedStatus = "AwaitingHWFDecision";
        Response caseSubmitted = submitCase("addresses.json", getUserToken());
        assertOkResponseAndCaseIdIsNotZero(caseSubmitted);
        assertCaseStatus(caseSubmitted, expectedStatus);

    }

    @Test
    public void shouldReturnCaseIdForValidAddressesSessionDatas() throws Exception {
        String expectedStatus = "AwaitingPayment";
        Response caseSubmitted = submitCase("addresses-no-hwf.json", getUserToken());
        assertOkResponseAndCaseIdIsNotZero(caseSubmitted);
        assertCaseStatus(caseSubmitted, expectedStatus);
    }

    @Test
    public void shouldReturnCaseIdForValidHowNameChangedSessionData() throws Exception {
        submitAndAssertSuccess("how-name-changed.json");
    }

    @Test
    public void shouldReturnCaseIdForValidJurisdiction6To12SessionData() throws Exception {
        submitAndAssertSuccess("jurisdiction-6-12.json");
    }

    @Test
    public void shouldReturnCaseIdForValidJurisdictionAllSessionData() throws Exception {
        submitAndAssertSuccess("jurisdiction-all.json");
    }

    @Test
    public void shouldReturnCaseIdForValidAdulterySessionData() throws Exception {
        submitAndAssertSuccess("reason-adultery.json");
    }

    @Test
    public void shouldReturnCaseIdForValidDesertionSessionData() throws Exception {
        submitAndAssertSuccess("reason-desertion.json");
    }

    @Test
    public void shouldReturnCaseIdForValidSeparationSessionData() throws Exception {
        submitAndAssertSuccess("reason-separation.json");
    }

    @Test
    public void shouldReturnCaseIdForValidUnreasonableBehaviourSessionData() throws Exception {
        submitAndAssertSuccess("reason-unreasonable-behaviour.json");
    }

    @Test
    public void shouldReturnCaseIdForValidSameSexSessionData() throws Exception {
        submitAndAssertSuccess("same-sex.json");
    }

    @Test
    public void shouldReturnCaseIdForValidD8DocumentSessionData() throws Exception {
        submitAndAssertSuccess("d8-document.json");
    }

    @Test
    public void shouldReturnCaseIdForValidAddressesSessionDataAndDeleteDraft() throws Exception {
        final String userToken = getUserToken();

        saveDraft(userToken, CCD_FORMAT_DRAFT_CONTEXT_PATH + "addresscase.json", Collections.emptyMap());

        Response draftsResponseBefore = getAllDraft(userToken);

        assertEquals(1, ((List)draftsResponseBefore.getBody().path("data")).size());

        Response cmsResponse = submitCase("addresses.json", userToken);

        assertOkResponseAndCaseIdIsNotZero(cmsResponse);

        //allow enough time for the async delete to process
        Thread.sleep(20000);

        Response draftsResponseAfter = getAllDraft(userToken);

        assertEquals(0, ((List)draftsResponseAfter.getBody().path("data")).size());
    }

    @Test
    public void shouldReturnErrorForInvalidUserJwtToken() throws Exception {
        Response cmsResponse = submitCase("addresses.json", INVALID_USER_TOKEN);

        assertEquals(HttpStatus.FORBIDDEN.value(), cmsResponse.getStatusCode());
        assertEquals(UNAUTHORISED_JWT_EXCEPTION, cmsResponse.asString());
    }

    @Test
    public void shouldReturnBadRequestForNoRequestBody() {
        Response cmsResponse = RestUtil.postToRestService(
            getSubmissionRequestUrl(),
            getHeaders(),
            null
        );

        assertEquals(HttpStatus.BAD_REQUEST.value(), cmsResponse.getStatusCode());
        assertEquals(REQUEST_BODY_NOT_FOUND, cmsResponse.path("message"));
    }
}
