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

    private String ccdState;

    private String expectedApplicationStatus;

    public CaseStateTest(String ccdState, String expectedApplicationStatus) {
        this.ccdState = ccdState;
        this.expectedApplicationStatus = expectedApplicationStatus;
    }

    @Parameters
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
                {"AosCompleted", "AosCompleted"},
                {"AosSubmittedAwaitingAnswer", "AosCompleted"},
                {"AwaitingDecreeNisi", "DNAwaiting"},
                {"Rejected", "Rejected"},
                {"AwaitingLegalAdvisorReferral", "AwaitingLegalAdvisorReferral"},
                {"AwaitingConsiderationDN", "AwaitingConsiderationDN"},
                {"AwaitingClarification", "AwaitingClarification"},
                {"AwaitingListing", "AwaitingListing"},
                {"AwaitingPronouncement", "AwaitingPronouncement"},
                {"Unknown", "DNCompleted"}
        });
    }

    @Test
    public void testCCDStatesMatchExpectedApplicationStatus() {
        ApplicationStatus applicationStatus = CaseState.getState(ccdState).getStatus();

        assertThat(applicationStatus.getValue(), equalTo(expectedApplicationStatus));
    }

}