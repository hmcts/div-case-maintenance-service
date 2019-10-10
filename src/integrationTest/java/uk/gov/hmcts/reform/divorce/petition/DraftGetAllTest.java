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

public class DraftGetAllTest extends PetitionSupport {

    @Test
    public void givenNoDraft_whenSaveDraft_thenReturnEmpty() {
        final String userToken = getUserToken();

        Response draftsResponse = getAllDraft(userToken);

        assertEquals(0, ((List)draftsResponse.getBody().path("data")).size());
    }

    @Test
    public void givenDraftsExist_whenSaveDraft_thenReturnDrafts() throws Exception {
        final String userToken = getUserToken();
        final String divorceFormatDraftFileUri = DIVORCE_FORMAT_DRAFT_CONTEXT_PATH + "base-case-divorce-session.json";

        saveDraft(userToken, divorceFormatDraftFileUri,
            Collections.singletonMap(DIVORCE_FORMAT_KEY, true));

        Response draftsResponse = getAllDraft(userToken);

        assertEquals(1, ((List)draftsResponse.getBody().path("data")).size());

        assertEquals(draftsResponse.getBody().path("data[0].document"),
            ResourceLoader.loadJsonToObject(divorceFormatDraftFileUri, Map.class));

        deleteDraft(userToken);
    }
}
