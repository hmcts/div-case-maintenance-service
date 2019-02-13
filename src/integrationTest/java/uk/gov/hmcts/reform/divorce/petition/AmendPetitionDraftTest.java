package uk.gov.hmcts.reform.divorce.petition;

import io.restassured.response.Response;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.divorce.support.PetitionSupport;

import static org.junit.Assert.assertEquals;

public class AmendPetitionDraftTest extends PetitionSupport {
    private static final String INVALID_USER_TOKEN = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJqdGkiOiIwOTg3NjU0M"
        + "yIsInN1YiI6IjEwMCIsImlhdCI6MTUwODk0MDU3MywiZXhwIjoxNTE5MzAzNDI3LCJkYXRhIjoiY2l0aXplbiIsInR5cGUiOiJBQ0NFU1MiL"
        + "CJpZCI6IjEwMCIsImZvcmVuYW1lIjoiSm9obiIsInN1cm5hbWUiOiJEb2UiLCJkZWZhdWx0LXNlcnZpY2UiOiJEaXZvcmNlIiwibG9hIjoxL"
        + "CJkZWZhdWx0LXVybCI6Imh0dHBzOi8vd3d3Lmdvdi51ayIsImdyb3VwIjoiZGl2b3JjZSJ9.lkNr1vpAP5_Gu97TQa0cRtHu8I-QESzu8kMX"
        + "CJOQrVU";

    @Test
    public void givenJWTTokenIsNull_whenAmendPetitionDraft_thenReturnBadRequest() {
        Response cmsResponse = putAmendedPetitionDraft(null);

        assertEquals(HttpStatus.BAD_REQUEST.value(), cmsResponse.getStatusCode());
    }

    @Test
    public void givenInvalidUserToken_whenAmendPetitionDraft_thenReturnForbiddenError() {
        Response cmsResponse = putAmendedPetitionDraft(INVALID_USER_TOKEN);

        assertEquals(HttpStatus.FORBIDDEN.value(), cmsResponse.getStatusCode());
    }

    @Test
    public void givenNoCaseInCcd_whenAmendPetitionDraft_thenReturn404() {
        Response cmsResponse = putAmendedPetitionDraft(getUserToken());

        assertEquals(HttpStatus.NOT_FOUND.value(), cmsResponse.getStatusCode());
    }

    @Test
    public void givenSingleCaseInCcd_whenAmendPetitionDraft_thenReturnTheDraft() throws Exception {
        final String userToken = getUserToken();

        Long caseRef = getCaseIdFromCompletedCase(userToken);

        Response cmsResponse = putAmendedPetitionDraft(userToken);

        assertEquals(HttpStatus.OK.value(), cmsResponse.getStatusCode());
        assertEquals(caseRef, cmsResponse.path("previousCaseId"));
    }

    @Test
    public void givenCaseNotProgressed_whenAmendPetitionDraft_thenReturn404() throws Exception {
        final String userToken = getUserToken();

        getCaseIdFromSubmittingANewCase(userToken);

        Response cmsResponse = putAmendedPetitionDraft(userToken);

        assertEquals(HttpStatus.NOT_FOUND.value(), cmsResponse.getStatusCode());
    }

    @Test
    public void givenMultipleSubmittedCaseInCcd_whenAmendPetitionDraft_thenReturn300() throws Exception {
        final String userToken = getUserToken();

        getCaseIdFromCompletedCase(userToken);
        getCaseIdFromCompletedCase(userToken);

        Response cmsResponse = putAmendedPetitionDraft(userToken);

        assertEquals(HttpStatus.MULTIPLE_CHOICES.value(), cmsResponse.getStatusCode());
    }
}
