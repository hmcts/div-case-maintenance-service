package uk.gov.hmcts.reform.divorce.petition;

import io.restassured.response.Response;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.divorce.support.PetitionSupport;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class DraftDeleteTest extends PetitionSupport {
    private static final String INVALID_USER_TOKEN = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJqdGkiOiIwOTg3NjU0M"
        + "yIsInN1YiI6IjEwMCIsImlhdCI6MTUwODk0MDU3MywiZXhwIjoxNTE5MzAzNDI3LCJkYXRhIjoiY2l0aXplbiIsInR5cGUiOiJBQ0NFU1MiL"
        + "CJpZCI6IjEwMCIsImZvcmVuYW1lIjoiSm9obiIsInN1cm5hbWUiOiJEb2UiLCJkZWZhdWx0LXNlcnZpY2UiOiJEaXZvcmNlIiwibG9hIjoxL"
        + "CJkZWZhdWx0LXVybCI6Imh0dHBzOi8vd3d3Lmdvdi51ayIsImdyb3VwIjoiZGl2b3JjZSJ9.lkNr1vpAP5_Gu97TQa0cRtHu8I-QESzu8kMX"
        + "CJOQrVU";

    @Test
    public void givenJWTTokenIsNull_whenDeleteDraft_thenReturnBadRequest() {
        Response cmsResponse = deleteDraft(null);

        assertEquals(HttpStatus.BAD_REQUEST.value(), cmsResponse.getStatusCode());
    }

    @Test
    public void givenInvalidUserToken_whenDeleteDraft_thenReturnForbiddenError() {
        Response cmsResponse = deleteDraft(INVALID_USER_TOKEN);

        assertEquals(HttpStatus.FORBIDDEN.value(), cmsResponse.getStatusCode());
    }

    @Test
    public void givenNoDraft_whenDeleteDraft_thenDoNothing() {
        Response cmsResponse = deleteDraft(getUserToken());

        assertEquals(HttpStatus.OK.value(), cmsResponse.getStatusCode());
    }

    @Test
    public void givenThereIsADraft_whenDeleteDraft_thenDeleteDraft() throws Exception {
        final String userToken = getUserToken();

        saveDraft(userToken, CCD_FORMAT_DRAFT_CONTEXT_PATH + "addresscase.json",
            Collections.emptyMap());

        Response draftsResponseBefore = getAllDraft(userToken);

        assertEquals(1, ((List)draftsResponseBefore.getBody().path("data")).size());

        Response cmsResponse = deleteDraft(userToken);

        assertEquals(HttpStatus.OK.value(), cmsResponse.getStatusCode());

        Response draftsResponseAfter = getAllDraft(userToken);

        assertEquals(0, ((List)draftsResponseAfter.getBody().path("data")).size());
    }
}
