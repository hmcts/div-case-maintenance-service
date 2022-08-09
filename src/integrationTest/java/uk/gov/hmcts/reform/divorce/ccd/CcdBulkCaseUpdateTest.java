package uk.gov.hmcts.reform.divorce.ccd;

import io.restassured.response.Response;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.support.PetitionSupport;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@Ignore
public class CcdBulkCaseUpdateTest extends PetitionSupport {

    private static final String CASE_PAYLOAD_PATH = "bulk-case.json";
    private static final String COURT_HEARING_DATETIME = "hearingDate";

    @Test
    public void shouldUpdateBulkCase() {
        UserDetails caseWorkerUser = getCaseWorkerUser();

        Long caseId = submitBulkCase(CASE_PAYLOAD_PATH, caseWorkerUser).getBody().path("id");
        Response bulkCreateResponse = updateBulkCase(null,
            caseId,
            BULK_CASE_CREATED_EVENT_ID,
            caseWorkerUser.getAuthToken());

        assertEquals(HttpStatus.OK.value(), bulkCreateResponse.getStatusCode());

        // Hearing Date is a mandatory field that must be set in the future
        Map<String, Object> updateData = Collections.singletonMap(COURT_HEARING_DATETIME, LocalDateTime.now().plusMonths(3).toString());

        Response cmsResponse = updateBulkCase(updateData,
            caseId,
            BULK_CASE_SCHEDULE_EVENT_ID,
            caseWorkerUser.getAuthToken());

        assertEquals(HttpStatus.OK.value(), cmsResponse.getStatusCode());
        assertEquals(caseId, cmsResponse.getBody().path("id"));
    }
}
