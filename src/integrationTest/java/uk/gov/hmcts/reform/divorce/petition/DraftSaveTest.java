package uk.gov.hmcts.reform.divorce.petition;

import io.restassured.response.Response;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.divorce.support.PetitionSupport;
import uk.gov.hmcts.reform.divorce.util.ResourceLoader;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class DraftSaveTest extends PetitionSupport {
    private static final String INVALID_USER_TOKEN = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJqdGkiOiIwOTg3NjU0M"
        + "yIsInN1YiI6IjEwMCIsImlhdCI6MTUwODk0MDU3MywiZXhwIjoxNTE5MzAzNDI3LCJkYXRhIjoiY2l0aXplbiIsInR5cGUiOiJBQ0NFU1MiL"
        + "CJpZCI6IjEwMCIsImZvcmVuYW1lIjoiSm9obiIsInN1cm5hbWUiOiJEb2UiLCJkZWZhdWx0LXNlcnZpY2UiOiJEaXZvcmNlIiwibG9hIjoxL"
        + "CJkZWZhdWx0LXVybCI6Imh0dHBzOi8vd3d3Lmdvdi51ayIsImdyb3VwIjoiZGl2b3JjZSJ9.lkNr1vpAP5_Gu97TQa0cRtHu8I-QESzu8kMX"
        + "CJOQrVU";

    @Test
    public void givenJWTTokenIsNull_whenSaveDraft_thenReturnBadRequest() throws Exception {
        Response cmsResponse = saveDraft(null, CCD_FORMAT_DRAFT_CONTEXT_PATH + "addresscase.json",
            Collections.emptyMap());

        assertEquals(HttpStatus.BAD_REQUEST.value(), cmsResponse.getStatusCode());
    }

    @Test
    public void givenBodyIsNull_whenSaveDraft_thenReturnBadRequest() throws Exception {
        Response cmsResponse = saveDraft(getUserToken(), null, Collections.emptyMap());

        assertEquals(HttpStatus.BAD_REQUEST.value(), cmsResponse.getStatusCode());
    }

    @Test
    public void givenInvalidUserToken_whenSaveDraft_thenReturnForbiddenError() throws Exception {
        Response cmsResponse = saveDraft(INVALID_USER_TOKEN, CCD_FORMAT_DRAFT_CONTEXT_PATH + "addresscase.json",
            Collections.emptyMap());

        assertEquals(HttpStatus.FORBIDDEN.value(), cmsResponse.getStatusCode());
    }

    @Test
    public void givenNoDraft_whenSaveDraft_thenCreateNewDraft() throws Exception {
        final String userToken = getUserToken();

        Response draftsResponseBefore = getAllDraft(userToken);

        assertEquals(0, ((List)draftsResponseBefore.getBody().path("data")).size());

        Response cmsResponse = saveDraft(userToken, CCD_FORMAT_DRAFT_CONTEXT_PATH + "addresscase.json",
            Collections.emptyMap());

        assertEquals(HttpStatus.OK.value(), cmsResponse.getStatusCode());

        Response draftsResponseAfter = getAllDraft(userToken);

        assertEquals(1, ((List)draftsResponseAfter.getBody().path("data")).size());

        deleteDraft(userToken);
    }

    @Test
    public void givenDraftAlreadyExists_whenSaveDraft_thenUpdateExistingDraft() throws Exception {
        final String userToken = getUserToken();
        final String divorceFormatDraftFileUri = DIVORCE_FORMAT_DRAFT_CONTEXT_PATH + "addresses.json";

        saveDraft(userToken, CCD_FORMAT_DRAFT_CONTEXT_PATH + "addresscase.json", Collections.emptyMap());

        Response draftsResponseBefore = getAllDraft(userToken);

        assertEquals(1, ((List)draftsResponseBefore.getBody().path("data")).size());

        Response cmsResponse = saveDraft(userToken, divorceFormatDraftFileUri,
            Collections.singletonMap(DIVORCE_FORMAT_KEY, true));

        assertEquals(HttpStatus.OK.value(), cmsResponse.getStatusCode());

        Response draftsResponseAfter = getAllDraft(userToken);

        assertEquals(1, ((List)draftsResponseAfter.getBody().path("data")).size());

        assertEquals(draftsResponseAfter.getBody().path("data[0].document"),
            ResourceLoader.loadJsonToObject(divorceFormatDraftFileUri, Map.class));

        deleteDraft(userToken);
    }
}
