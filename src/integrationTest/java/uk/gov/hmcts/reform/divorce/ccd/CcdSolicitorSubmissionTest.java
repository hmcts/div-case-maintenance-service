package uk.gov.hmcts.reform.divorce.ccd;

import io.restassured.response.Response;
import org.junit.Test;
import uk.gov.hmcts.reform.divorce.support.PetitionSupport;

public class CcdSolicitorSubmissionTest extends PetitionSupport {

    @Test
    public void shouldReturnCaseIdForValidAddressesSessionData() {
        String expectedStatus = "SOTAgreementPayAndSubmitRequired";
        Response caseSubmitted = solicitorSubmitCase("solicitor/base-case.json", getSolicitorUser());
        assertOkResponseAndCaseIdIsNotZero(caseSubmitted);
        assertCaseStatus(caseSubmitted, expectedStatus);

    }

    @Test
    public void shouldReturnCaseIdForValidAmendedSessionData() {
        String expectedStatus = "SOTAgreementPayAndSubmitRequired";
        Response caseSubmitted = solicitorSubmitCase("solicitor/base-amended-case.json", getSolicitorUser());
        assertOkResponseAndCaseIdIsNotZero(caseSubmitted);
        assertCaseStatus(caseSubmitted, expectedStatus);

    }

    @Test
    public void shouldReturnCaseIdForValidHowNameChangedSessionData() {
        solicitorSubmitAndAssertSuccess("solicitor/how-name-changed.json");
    }

    @Test
    public void shouldReturnCaseIdForValidJurisdiction6To12SessionData() {
        solicitorSubmitAndAssertSuccess("solicitor/jurisdiction-6-12.json");
    }

    @Test
    public void shouldReturnCaseIdForValidJurisdictionAllSessionData() {
        solicitorSubmitAndAssertSuccess("solicitor/jurisdiction-all.json");
    }

    @Test
    public void shouldReturnCaseIdForValidAdulterySessionData() {
        solicitorSubmitAndAssertSuccess("solicitor/reason-adultery.json");
    }

    @Test
    public void shouldReturnCaseIdForValidDesertionSessionData() {
        solicitorSubmitAndAssertSuccess("solicitor/reason-desertion.json");
    }

    @Test
    public void shouldReturnCaseIdForValidSeparationSessionData() {
        solicitorSubmitAndAssertSuccess("solicitor/reason-separation.json");
    }

    @Test
    public void shouldReturnCaseIdForValidUnreasonableBehaviourSessionData() {
        solicitorSubmitAndAssertSuccess("solicitor/reason-unreasonable-behaviour.json");
    }

    @Test
    public void shouldReturnCaseIdForValidSameSexSessionData() {
        solicitorSubmitAndAssertSuccess("solicitor/same-sex.json");
    }

    @Test
    public void shouldReturnCaseIdForValidD8DocumentSessionData() {
        solicitorSubmitAndAssertSuccess("solicitor/d8-document.json");
    }
}
