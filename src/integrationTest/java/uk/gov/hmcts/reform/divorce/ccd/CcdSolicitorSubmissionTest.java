package uk.gov.hmcts.reform.divorce.ccd;

import io.restassured.response.Response;
import org.junit.Test;
import uk.gov.hmcts.reform.divorce.support.PetitionSupport;

public class CcdSolicitorSubmissionTest extends PetitionSupport {

    private static final String SOLICITOR_PATH = "solicitor/";

    @Test
    public void shouldReturnCaseIdForValidAddressesSessionData() {
        String expectedStatus = "SOTAgreementPayAndSubmitRequired";
        Response caseSubmitted = solicitorSubmitCase(SOLICITOR_PATH + "base-case.json", getSolicitorUser());
        assertOkResponseAndCaseIdIsNotZero(caseSubmitted);
        assertCaseStatus(caseSubmitted, expectedStatus);
    }

    @Test
    public void shouldReturnCaseIdForValidAmendedSessionData() {
        String expectedStatus = "SOTAgreementPayAndSubmitRequired";
        Response caseSubmitted = solicitorSubmitCase(SOLICITOR_PATH + "base-amended-case.json", getSolicitorUser());
        assertOkResponseAndCaseIdIsNotZero(caseSubmitted);
        assertCaseStatus(caseSubmitted, expectedStatus);

    }

    @Test
    public void shouldReturnCaseIdForValidHowNameChangedSessionData() {
        solicitorSubmitAndAssertSuccess(SOLICITOR_PATH + "how-name-changed.json");
    }

    @Test
    public void shouldReturnCaseIdForValidJurisdiction6To12SessionData() {
        solicitorSubmitAndAssertSuccess(SOLICITOR_PATH + "jurisdiction-6-12.json");
    }

    @Test
    public void shouldReturnCaseIdForValidJurisdictionAllSessionData() {
        solicitorSubmitAndAssertSuccess(SOLICITOR_PATH + "jurisdiction-all.json");
    }

    @Test
    public void shouldReturnCaseIdForValidAdulterySessionData() {
        solicitorSubmitAndAssertSuccess(SOLICITOR_PATH + "reason-adultery.json");
    }

    @Test
    public void shouldReturnCaseIdForValidDesertionSessionData() {
        solicitorSubmitAndAssertSuccess(SOLICITOR_PATH + "reason-desertion.json");
    }

    @Test
    public void shouldReturnCaseIdForValidSeparationSessionData() {
        solicitorSubmitAndAssertSuccess(SOLICITOR_PATH + "reason-separation.json");
    }

    @Test
    public void shouldReturnCaseIdForValidUnreasonableBehaviourSessionData() {
        solicitorSubmitAndAssertSuccess(SOLICITOR_PATH + "reason-unreasonable-behaviour.json");
    }

    @Test
    public void shouldReturnCaseIdForValidSameSexSessionData() {
        solicitorSubmitAndAssertSuccess(SOLICITOR_PATH + "same-sex.json");
    }

    @Test
    public void shouldReturnCaseIdForValidD8DocumentSessionData() {
        solicitorSubmitAndAssertSuccess(SOLICITOR_PATH + "d8-document.json");
    }
}
