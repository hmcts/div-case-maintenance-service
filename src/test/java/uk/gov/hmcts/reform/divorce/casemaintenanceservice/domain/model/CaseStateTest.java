package uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class CaseStateTest {

    private final String ccdState;

    private final String expectedApplicationStatus;

    public CaseStateTest(String ccdState, String expectedApplicationStatus) {
        this.ccdState = ccdState;
        this.expectedApplicationStatus = expectedApplicationStatus;
    }

    @Parameters(name = "{index}: caseSate: {0}, expectedApplicationStatus:{1}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"AwaitingPetitioner", "AwaitingPetitioner"},
                {"AwaitingPayment", "AwaitingPayment"},
                {"AwaitingHWFDecision", "PetitionCompleted"},
                {"Submitted", "PetitionCompleted"},
                {"Issued", "PetitionCompleted"},
                {"PendingRejection", "PendingRejection"},
                {"AwaitingDocuments", "PetitionCompleted"},
                {"AosAwaiting", "AosAwaiting"},
                {"AosStarted", "AosStarted"},
                {"AosSubmittedAwaitingAnswer", "AosSubmittedAwaitingAnswer"},
                {"AwaitingDecreeNisi", "AwaitingDecreeNisi"},
                {"Rejected", "Rejected"},
                {"AwaitingLegalAdvisorReferral", "AwaitingLegalAdvisorReferral"},
                {"DefendedDivorce", "DefendedDivorce"},
                {"AmendPetition", "AmendPetition"},
                {"AwaitingClarification", "AwaitingClarification"},
                {"AwaitingConsideration", "AwaitingConsideration"},
                {"DNPronounced", "DNPronounced"},
                {"AwaitingDecreeAbsolute", "AwaitingDecreeAbsolute"},
                {"AwaitingPronouncement", "AwaitingPronouncement"},
                {"DefendedDivorce", "DefendedDivorce"},
                {"DivorceGranted", "DivorceGranted"},
                {"DNisRefused", "DNisRefused"},
                {"DNDrafted", "DNDrafted"},
                {"Unknown", "DNCompleted"},
                {"DaRequested", "DARequested"},
                {"ClarificationSubmitted", "ClarificationSubmitted"},
                {"AwaitingAdminClarification", "AwaitingAdminClarification"}
        });
    }

    @Test
    public void testCCDStatesMatchExpectedApplicationStatus() {
        ApplicationStatus applicationStatus = CaseState.getState(ccdState).getStatus();

        assertThat(applicationStatus.getValue(), equalTo(expectedApplicationStatus));
    }
}
