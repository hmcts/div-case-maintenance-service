package uk.gov.hmcts.reform.divorce.petition;

import io.restassured.response.Response;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.support.PetitionSupport;

import static org.junit.Assert.assertEquals;

public class AmendPetitionDraftTest extends PetitionSupport {

    @Test
    public void givenNoCaseInCcd_whenAmendPetitionDraft_thenReturn404() {
        Response cmsResponse = putAmendedPetitionDraft(getUserToken());

        assertEquals(HttpStatus.NOT_FOUND.value(), cmsResponse.getStatusCode());
    }

    //    @Test
    //    public void givenSingleCaseInCcd_whenAmendPetitionDraft_thenReturnTheDraft() throws Exception {
    //        final UserDetails userDetails = getUserDetails();
    //
    //        Long caseId = getCaseIdFromCompletedCase(userDetails);
    //
    //        Response cmsResponse = putAmendedPetitionDraft(userDetails.getAuthToken());
    //
    //        assertEquals(HttpStatus.OK.value(), cmsResponse.getStatusCode());
    //        assertEquals(String.valueOf(caseId), cmsResponse.path("previousCaseId"));
    //    }

    @Test
    public void givenCaseNotProgressed_whenAmendPetitionDraft_thenReturn404() throws Exception {
        final UserDetails userDetails = getUserDetails();

        getCaseIdFromSubmittingANewCase(userDetails);

        Response cmsResponse = putAmendedPetitionDraft(userDetails.getAuthToken());

        assertEquals(HttpStatus.NOT_FOUND.value(), cmsResponse.getStatusCode());
    }

    //    @Test
    //    public void givenMultipleSubmittedCaseInCcd_whenAmendPetitionDraft_thenReturn300() throws Exception {
    //        final UserDetails userDetails = getUserDetails();
    //
    //        getCaseIdFromCompletedCase(userDetails);
    //        getCaseIdFromCompletedCase(userDetails);
    //
    //        Response cmsResponse = putAmendedPetitionDraft(userDetails.getAuthToken());
    //
    //        assertEquals(HttpStatus.MULTIPLE_CHOICES.value(), cmsResponse.getStatusCode());
    //    }
}
