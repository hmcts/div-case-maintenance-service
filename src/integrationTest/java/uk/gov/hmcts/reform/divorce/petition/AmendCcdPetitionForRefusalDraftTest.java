package uk.gov.hmcts.reform.divorce.petition;

import io.restassured.response.Response;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.support.PetitionSupport;

import static org.junit.Assert.assertEquals;

public class AmendCcdPetitionForRefusalDraftTest extends PetitionSupport {

    @Test
    public void givenSingleCaseInCcd_whenCcdAmendPetitionDraft_thenReturnTheDraft() throws Exception {
        final UserDetails userDetails = getUserDetails();

        Long caseId = getCaseIdFromCompletedCase(userDetails);

        Response cmsResponse = putCcdAmendedPetitionDraftForRefusal(userDetails.getAuthToken(), caseId);

        assertEquals(HttpStatus.OK.value(), cmsResponse.getStatusCode());
        assertEquals(String.valueOf(caseId), cmsResponse.path("previousCaseId"));
    }
}
