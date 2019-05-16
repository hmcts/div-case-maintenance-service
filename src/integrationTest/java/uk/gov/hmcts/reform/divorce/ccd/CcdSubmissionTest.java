package uk.gov.hmcts.reform.divorce.ccd;

import io.restassured.response.Response;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.support.PetitionSupport;
import uk.gov.hmcts.reform.divorce.util.RestUtil;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

public class CcdSubmissionTest extends PetitionSupport {
    private static final String INVALID_USER_TOKEN = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJqdGkiOiIwOTg3NjU0M"
        + "yIsInN1YiI6IjEwMCIsImlhdCI6MTUwODk0MDU3MywiZXhwIjoxNTE5MzAzNDI3LCJkYXRhIjoiY2l0aXplbiIsInR5cGUiOiJBQ0NFU1MiL"
        + "CJpZCI6IjEwMCIsImZvcmVuYW1lIjoiSm9obiIsInN1cm5hbWUiOiJEb2UiLCJkZWZhdWx0LXNlcnZpY2UiOiJEaXZvcmNlIiwibG9hIjoxL"
        + "CJkZWZhdWx0LXVybCI6Imh0dHBzOi8vd3d3Lmdvdi51ayIsImdyb3VwIjoiZGl2b3JjZSJ9.lkNr1vpAP5_Gu97TQa0cRtHu8I-QESzu8kMX"
        + "CJOQrVU";
    private static final String  UNAUTHORISED_JWT_EXCEPTION = "status 403 reading "
        + "IdamApiClient#retrieveUserDetails(String) - ";
    private static final String REQUEST_BODY_NOT_FOUND = "Required request body is missing";

    private static final String USER_EMAIL = "test@test.com";

    @Test
    public void shouldReturnCaseIdForValidAddressesSessionData() throws Exception {
        String expectedStatus = "AwaitingHWFDecision";
        Response caseSubmitted = submitCase("addresses.json", getUserDetails());
        assertOkResponseAndCaseIdIsNotZero(caseSubmitted);
        assertCaseStatus(caseSubmitted, expectedStatus);

    }

    @Test
    public void shouldReturnCaseIdForValidAddressesSessionDatas() throws Exception {
        String expectedStatus = "AwaitingPayment";
        Response caseSubmitted = submitCase("addresses-no-hwf.json", getUserDetails());
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
        final UserDetails userDetails = getUserDetails();

        final String userToken = userDetails.getAuthToken();

        saveDraft(userToken, CCD_FORMAT_DRAFT_CONTEXT_PATH + "addresscase.json", Collections.emptyMap());

        Response draftsResponseBefore = getAllDraft(userToken);

        assertThat(((List)draftsResponseBefore.getBody().path("data")).size()).isOne();

        Response cmsResponse = submitCase("addresses.json", userDetails);

        assertOkResponseAndCaseIdIsNotZero(cmsResponse);

        //allow enough time for the async delete to process
        Thread.sleep(20000);

        Response draftsResponseAfter = getAllDraft(userToken);

        assertThat((List) draftsResponseAfter.getBody().path("data")).isEmpty();
    }

    @Test
    public void shouldReturnErrorForInvalidUserJwtToken() throws Exception {
        Response cmsResponse = submitCase("addresses.json", UserDetails.builder()
            .authToken(INVALID_USER_TOKEN)
            .emailAddress(USER_EMAIL)
            .build());

        assertThat(HttpStatus.FORBIDDEN.value()).isEqualTo(cmsResponse.getStatusCode());
        assertThat(UNAUTHORISED_JWT_EXCEPTION).isEqualTo(cmsResponse.asString());
    }

    @Test
    public void shouldReturnBadRequestForNoRequestBody() {
        Response cmsResponse = RestUtil.postToRestService(
            getSubmissionRequestUrl(),
            getHeaders(),
            null
        );

        assertThat(HttpStatus.BAD_REQUEST.value()).isEqualTo(cmsResponse.getStatusCode());
        assertTrue(cmsResponse.getBody().asString().contains(REQUEST_BODY_NOT_FOUND));
    }
}
