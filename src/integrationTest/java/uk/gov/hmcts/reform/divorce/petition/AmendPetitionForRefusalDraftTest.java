package uk.gov.hmcts.reform.divorce.petition;

import com.google.common.collect.ImmutableMap;
import io.restassured.response.Response;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.support.PetitionSupport;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties.REFUSAL_ORDER_REJECTION_REASONS;

public class AmendPetitionForRefusalDraftTest extends PetitionSupport {
    private static final String AMEND_CASE_EVENT_ID = "amendCase";

    @Ignore
    @Test
    public void givenSingleCaseInCcd_whenAmendPetitionDraft_thenReturnTheDraft() throws Exception {
        final UserDetails userDetails = getUserDetails();

        Long caseId = getCaseIdFromCompletedCase(userDetails);

        updateCase(ImmutableMap.of(REFUSAL_ORDER_REJECTION_REASONS, Collections.singletonList("other")),
            caseId, AMEND_CASE_EVENT_ID, getCaseWorkerUser().getAuthToken());

        Response cmsResponse = putAmendedPetitionDraftForRefusal(userDetails.getAuthToken());

        assertEquals(HttpStatus.OK.value(), cmsResponse.getStatusCode());
        assertEquals(String.valueOf(caseId), cmsResponse.path("previousCaseId"));
    }
}
