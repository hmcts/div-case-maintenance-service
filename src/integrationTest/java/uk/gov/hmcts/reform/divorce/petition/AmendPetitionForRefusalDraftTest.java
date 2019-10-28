package uk.gov.hmcts.reform.divorce.petition;

import io.restassured.response.Response;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.support.PetitionSupport;

import static org.junit.Assert.assertEquals;

public class AmendPetitionForRefusalDraftTest extends PetitionSupport {

    @Test
    public void givenSingleCaseInCcd_whenAmendPetitionDraft_thenReturnTheDraft() throws Exception {
        final UserDetails userDetails = getUserDetails();

        Long caseId = getCaseIdFromCompletedCase(userDetails);

        Response cmsResponse = putAmendedPetitionDraft(userDetails.getAuthToken());

        assertEquals(HttpStatus.OK.value(), cmsResponse.getStatusCode());
        assertEquals(String.valueOf(caseId), cmsResponse.path("previousCaseId"));
    }
}
