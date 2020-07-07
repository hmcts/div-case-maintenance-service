package uk.gov.hmcts.reform.divorce.petition;

import io.restassured.response.Response;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.support.PetitionSupport;

import static org.junit.Assert.assertEquals;

public class AmendPetitionForRefusalDraftFromCaseIdTest extends PetitionSupport {

    @Test
    public void givenSingleCaseInCcd_whenAmendPetitionDraftFromCaseId_thenReturnTheDraft() throws Exception {
        final UserDetails userDetails = getUserDetails();

        Long caseId = getCaseIdFromCompletedCase(userDetails);

        Response cmsResponse = putAmendedPetitionDraftForRefusalFromCaseId(userDetails.getAuthToken(), caseId);

        assertEquals(HttpStatus.OK.value(), cmsResponse.getStatusCode());
        assertEquals(String.valueOf(caseId), cmsResponse.path("previousCaseId"));
    }
}
