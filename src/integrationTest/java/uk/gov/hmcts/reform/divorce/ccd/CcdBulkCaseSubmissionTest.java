package uk.gov.hmcts.reform.divorce.ccd;

import io.restassured.response.Response;
import org.junit.Test;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.support.PetitionSupport;

public class CcdBulkCaseSubmissionTest extends PetitionSupport {

    private static final String CASE_PAYLOAD_PATH = "bulk-case.json";

    @Test
    public void shouldReturnCaseId() {
        UserDetails caseWorkerUser = getCaseWorkerUser();

        String expectedStatus = "ScheduledForCreate";
        Response caseSubmitted = submitBulkCase(CASE_PAYLOAD_PATH, caseWorkerUser);
        assertOkResponseAndCaseIdIsNotZero(caseSubmitted);
        assertCaseStatus(caseSubmitted, expectedStatus);
    }
}
