package uk.gov.hmcts.reform.divorce.petition;

import io.restassured.response.Response;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.divorce.support.PetitionSupport;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class DraftDeleteTest extends PetitionSupport {

    @Test
    public void givenNoDraft_whenDeleteDraft_thenDoNothing() {
        Response cmsResponse = deleteDraft(getUserToken());

        assertEquals(HttpStatus.OK.value(), cmsResponse.getStatusCode());
    }

    @Test
    public void givenThereIsADraft_whenDeleteDraft_thenDeleteDraft() throws Exception {
        final String userToken = getUserToken();

        createDraft(userToken, CCD_FORMAT_DRAFT_CONTEXT_PATH + "base-case.json",
            Collections.emptyMap());

        Response draftsResponseBefore = getAllDraft(userToken);

        assertEquals(1, ((List)draftsResponseBefore.getBody().path("data")).size());

        Response cmsResponse = deleteDraft(userToken);

        assertEquals(HttpStatus.OK.value(), cmsResponse.getStatusCode());

        Response draftsResponseAfter = getAllDraft(userToken);

        assertEquals(0, ((List)draftsResponseAfter.getBody().path("data")).size());
    }

    @Test
    public void givenThereAreMultipleDrafts_whenDeleteDraft_thenDeleteAllDraft() throws Exception {
        final String userToken = getUserToken();

        createDraft(userToken, DIVORCE_FORMAT_DRAFT_CONTEXT_PATH + "jurisdiction-6-12.json",
            Collections.singletonMap(DIVORCE_FORMAT_KEY, "true"));
        createDraft(userToken, CCD_FORMAT_DRAFT_CONTEXT_PATH + "base-case.json",
            Collections.emptyMap());

        Response draftsResponseBefore = getAllDraft(userToken);

        assertEquals(2, ((List)draftsResponseBefore.getBody().path("data")).size());

        Response cmsResponse = deleteDraft(userToken);

        assertEquals(HttpStatus.OK.value(), cmsResponse.getStatusCode());

        Response draftsResponseAfter = getAllDraft(userToken);

        assertEquals(0, ((List)draftsResponseAfter.getBody().path("data")).size());
    }
}
