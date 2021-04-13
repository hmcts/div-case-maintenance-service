package uk.gov.hmcts.reform.divorce.ccd;

import io.restassured.response.Response;
import org.junit.Ignore;
import org.junit.Test;
import uk.gov.hmcts.reform.divorce.support.PetitionSupport;

@Ignore
public class CcdSolicitorSubmissionTest extends PetitionSupport {

    @Test
    public void shouldReturnCaseIdForValidAddressesSessionData() {
        String expectedStatus = "SOTAgreementPayAndSubmitRequired";
        Response caseSubmitted = solicitorSubmitCase("base-case.json", getSolicitorUser());
        assertOkResponseAndCaseIdIsNotZero(caseSubmitted);
        assertCaseStatus(caseSubmitted, expectedStatus);

    }

    @Test
    public void shouldReturnCaseIdForValidAmendedSessionData() {
        String expectedStatus = "SOTAgreementPayAndSubmitRequired";
        Response caseSubmitted = solicitorSubmitCase("base-amended-case.json", getSolicitorUser());
        assertOkResponseAndCaseIdIsNotZero(caseSubmitted);
        assertCaseStatus(caseSubmitted, expectedStatus);

    }

    @Test
    public void shouldReturnCaseIdForValidHowNameChangedSessionData() {
        solicitorSubmitAndAssertSuccess("how-name-changed.json");
    }

    @Test
    public void shouldReturnCaseIdForValidJurisdiction6To12SessionData() {
        solicitorSubmitAndAssertSuccess("jurisdiction-6-12.json");
    }

    @Test
    public void shouldReturnCaseIdForValidJurisdictionAllSessionData() {
        solicitorSubmitAndAssertSuccess("jurisdiction-all.json");
    }

    @Test
    public void shouldReturnCaseIdForValidAdulterySessionData() {
        solicitorSubmitAndAssertSuccess("reason-adultery.json");
    }

    @Test
    public void shouldReturnCaseIdForValidDesertionSessionData() {
        solicitorSubmitAndAssertSuccess("reason-desertion.json");
    }

    @Test
    public void shouldReturnCaseIdForValidSeparationSessionData() {
        solicitorSubmitAndAssertSuccess("reason-separation.json");
    }

    @Test
    public void shouldReturnCaseIdForValidUnreasonableBehaviourSessionData() {
        solicitorSubmitAndAssertSuccess("reason-unreasonable-behaviour.json");
    }

    @Test
    public void shouldReturnCaseIdForValidSameSexSessionData() {
        solicitorSubmitAndAssertSuccess("same-sex.json");
    }

    @Test
    public void shouldReturnCaseIdForValidD8DocumentSessionData() {
        solicitorSubmitAndAssertSuccess("d8-document.json");
    }
}
