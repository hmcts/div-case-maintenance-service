package uk.gov.hmcts.reform.divorce.ccd;

import io.restassured.response.Response;
import org.junit.Test;
import uk.gov.hmcts.reform.divorce.support.PetitionSupport;

public class CcdSolicitorSubmissionTest extends PetitionSupport {

    private static final String SOLICITOR_PATH = "solicitor/";

    @Test
    public void shouldReturnCaseIdForValidAddressesSessionData() {
        String expectedStatus = "SOTAgreementPayAndSubmitRequired";
        Response caseSubmitted = solicitorSubmitCase(buildSolicitorPath("base-case.json"), getSolicitorUser());
        assertOkResponseAndCaseIdIsNotZero(caseSubmitted);
        assertCaseStatus(caseSubmitted, expectedStatus);
    }

    @Test
    public void shouldReturnCaseIdForValidAmendedSessionData() {
        String expectedStatus = "SOTAgreementPayAndSubmitRequired";
        Response caseSubmitted = solicitorSubmitCase(buildSolicitorPath("base-amended-case.json"), getSolicitorUser());
        assertOkResponseAndCaseIdIsNotZero(caseSubmitted);
        assertCaseStatus(caseSubmitted, expectedStatus);

    }

    @Test
    public void shouldReturnCaseIdForValidHowNameChangedSessionData() {
        solicitorSubmitAndAssertSuccess(buildSolicitorPath("how-name-changed.json"));
    }

    @Test
    public void shouldReturnCaseIdForValidJurisdiction6To12SessionData() {
        solicitorSubmitAndAssertSuccess(buildSolicitorPath("jurisdiction-6-12.json"));
    }

    @Test
    public void shouldReturnCaseIdForValidJurisdictionAllSessionData() {
        solicitorSubmitAndAssertSuccess(buildSolicitorPath("jurisdiction-all.json"));
    }

    @Test
    public void shouldReturnCaseIdForValidAdulterySessionData() {
        solicitorSubmitAndAssertSuccess(buildSolicitorPath("reason-adultery.json"));
    }

    @Test
    public void shouldReturnCaseIdForValidDesertionSessionData() {
        solicitorSubmitAndAssertSuccess(buildSolicitorPath("reason-desertion.json"));
    }

    @Test
    public void shouldReturnCaseIdForValidSeparationSessionData() {
        solicitorSubmitAndAssertSuccess(buildSolicitorPath("reason-separation.json"));
    }

    @Test
    public void shouldReturnCaseIdForValidUnreasonableBehaviourSessionData() {
        solicitorSubmitAndAssertSuccess(buildSolicitorPath("reason-unreasonable-behaviour.json"));
    }

    @Test
    public void shouldReturnCaseIdForValidSameSexSessionData() {
        solicitorSubmitAndAssertSuccess(buildSolicitorPath("same-sex.json"));
    }

    @Test
    public void shouldReturnCaseIdForValidD8DocumentSessionData() {
        solicitorSubmitAndAssertSuccess(buildSolicitorPath("d8-document.json"));
    }

    private String buildSolicitorPath(String fileName) {
        return SOLICITOR_PATH + fileName;
    }
}
