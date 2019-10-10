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

    @Test
    public void givenNoDraft_whenSaveDraft_thenCreateNewDraft() throws Exception {
        final String userToken = getUserToken();

        Response draftsResponseBefore = getAllDraft(userToken);

        assertEquals(0, ((List)draftsResponseBefore.getBody().path("data")).size());

        Response cmsResponse = saveDraft(userToken, CCD_FORMAT_DRAFT_CONTEXT_PATH + "base-case.json",
            Collections.emptyMap());

        assertEquals(HttpStatus.OK.value(), cmsResponse.getStatusCode());

        Response draftsResponseAfter = getAllDraft(userToken);

        assertEquals(1, ((List)draftsResponseAfter.getBody().path("data")).size());

        deleteDraft(userToken);
    }

    @Test
    public void givenDraftAlreadyExists_whenSaveDraft_thenUpdateExistingDraft() throws Exception {
        final String userToken = getUserToken();
        final String divorceFormatDraftFileUri = DIVORCE_FORMAT_DRAFT_CONTEXT_PATH + "base-case-divorce-session.json";

        saveDraft(userToken, CCD_FORMAT_DRAFT_CONTEXT_PATH + "base-case.json", Collections.emptyMap());

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
