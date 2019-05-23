package uk.gov.hmcts.reform.divorce.ccd;

import io.restassured.response.Response;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.support.PetitionSupport;

import static org.junit.Assert.assertEquals;

public class CcdBulkCaseUpdateTest extends PetitionSupport {

    private static final String CASE_PAYLOAD_PATH = "bulk-case.json";

    @Test
    public void shouldUpdateBulkCase() {
        UserDetails caseWorkerUser = getCaseWorkerUser();

        Long caseId = submitBulkCase(CASE_PAYLOAD_PATH, caseWorkerUser).getBody().path("caseId");

        Response cmsResponse = updateBulkCase(CASE_PAYLOAD_PATH,
            caseId,
            BULK_CASE_SCHEDULE_EVENT_ID,
            caseWorkerUser.getAuthToken());

        assertEquals(HttpStatus.OK.value(), cmsResponse.getStatusCode());
        assertEquals(caseId, cmsResponse.getBody().path("id"));
    }
}
