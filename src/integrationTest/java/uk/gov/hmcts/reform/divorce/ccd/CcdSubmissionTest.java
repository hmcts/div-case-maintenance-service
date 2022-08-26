package uk.gov.hmcts.reform.divorce.ccd;

import io.restassured.response.Response;
import org.junit.Test;
import uk.gov.hmcts.reform.divorce.support.PetitionSupport;

public class CcdSubmissionTest extends PetitionSupport {

    //    @Test
    //    public void shouldReturnCaseIdForValidAddressesSessionData() throws Exception {
    //        String expectedStatus = "AwaitingHWFDecision";
    //        Response caseSubmitted = submitCase("base-case.json", getUserDetails());
    //        assertOkResponseAndCaseIdIsNotZero(caseSubmitted);
    //        assertCaseStatus(caseSubmitted, expectedStatus);
    //
    //    }

    @Test
    public void shouldReturnCaseIdForValidAddressesSessionDatas() throws Exception {
        String expectedStatus = "AwaitingPayment";
        Response caseSubmitted = submitCase("addresses-no-hwf.json", getUserDetails());
        assertOkResponseAndCaseIdIsNotZero(caseSubmitted);
        assertCaseStatus(caseSubmitted, expectedStatus);
    }

    //    @Test
    //    public void shouldReturnCaseIdForValidHowNameChangedSessionData() throws Exception {
    //        submitAndAssertSuccess("how-name-changed.json");
    //    }

    //    @Test
    //    public void shouldReturnCaseIdForValidJurisdiction6To12SessionData() throws Exception {
    //        submitAndAssertSuccess("jurisdiction-6-12.json");
    //    }

    @Test
    public void shouldReturnCaseIdForValidJurisdictionAllSessionData() throws Exception {
        submitAndAssertSuccess("jurisdiction-all.json");
    }

    //    @Test
    //    public void shouldReturnCaseIdForValidAdulterySessionData() throws Exception {
    //        submitAndAssertSuccess("reason-adultery.json");
    //    }

    //    @Test
    //    public void shouldReturnCaseIdForValidDesertionSessionData() throws Exception {
    //        submitAndAssertSuccess("reason-desertion.json");
    //    }

    //    @Test
    //    public void shouldReturnCaseIdForValidSeparationSessionData() throws Exception {
    //        submitAndAssertSuccess("reason-separation.json");
    //    }

    //    @Test
    //    public void shouldReturnCaseIdForValidUnreasonableBehaviourSessionData() throws Exception {
    //        submitAndAssertSuccess("reason-unreasonable-behaviour.json");
    //    }

    @Test
    public void shouldReturnCaseIdForValidSameSexSessionData() throws Exception {
        submitAndAssertSuccess("same-sex.json");
    }

    //    @Test
    //    public void shouldReturnCaseIdForValidD8DocumentSessionData() throws Exception {
    //        submitAndAssertSuccess("d8-document.json");
    //    }

    //    @Test
    //    public void shouldReturnCaseIdForValidAddressesSessionDataAndDeleteDraft() throws Exception {
    //        final UserDetails userDetails = getUserDetails();
    //
    //        final String userToken = userDetails.getAuthToken();
    //
    //        saveDraft(userToken, CCD_FORMAT_DRAFT_CONTEXT_PATH + "base-case.json", Collections.emptyMap());
    //
    //        Response draftsResponseBefore = getAllDraft(userToken);
    //
    //        assertThat(((List)draftsResponseBefore.getBody().path("data")).size()).isOne();
    //
    //        Response cmsResponse = submitCase("base-case.json", userDetails);
    //
    //        assertOkResponseAndCaseIdIsNotZero(cmsResponse);
    //
    //        //allow enough time for the async delete to process
    //        Thread.sleep(20000);
    //
    //        Response draftsResponseAfter = getAllDraft(userToken);
    //
    //        assertThat((List) draftsResponseAfter.getBody().path("data")).isEmpty();
    //    }
}
