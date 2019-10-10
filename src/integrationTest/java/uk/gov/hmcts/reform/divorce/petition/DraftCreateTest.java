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

public class DraftCreateTest extends PetitionSupport {

    @Test
    public void givenSingleDivorceFormatDraft_whenCreateDraft_thenCreateNewDraft() throws Exception {
        final String userToken = getUserToken();

        final String filePath = DIVORCE_FORMAT_DRAFT_CONTEXT_PATH + "base-case-divorce-session.json";

        Response draftsResponseBefore = getAllDraft(userToken);

        assertEquals(0, ((List)draftsResponseBefore.getBody().path("data")).size());

        Response cmsResponse = createDraft(userToken, filePath, Collections.singletonMap(DIVORCE_FORMAT_KEY, "true"));

        assertEquals(HttpStatus.OK.value(), cmsResponse.getStatusCode());

        Response draftsResponseAfter = getAllDraft(userToken);

        assertEquals(1, ((List)draftsResponseAfter.getBody().path("data")).size());

        assertEquals(draftsResponseAfter.getBody().path("data[0].document"),
            ResourceLoader.loadJsonToObject(filePath, Map.class));

        deleteDraft(userToken);
    }

    @Test
    public void givenDraftAlreadyExist_whenCreateDraft_thenAddNewNewDraft() throws Exception {
        final String userToken = getUserToken();
        final String filePath1 = DIVORCE_FORMAT_DRAFT_CONTEXT_PATH + "base-case-divorce-session.json";
        final String filePath2 = CCD_FORMAT_DRAFT_CONTEXT_PATH + "base-case.json";

        Response draftsResponseBefore = getAllDraft(userToken);

        assertEquals(0, ((List)draftsResponseBefore.getBody().path("data")).size());

        Response cmsResponse1 = createDraft(userToken, filePath1,
            Collections.singletonMap(DIVORCE_FORMAT_KEY, "true"));

        assertEquals(HttpStatus.OK.value(), cmsResponse1.getStatusCode());

        Response cmsResponse2 = createDraft(userToken, filePath2, Collections.emptyMap());

        assertEquals(HttpStatus.OK.value(), cmsResponse2.getStatusCode());

        Response draftsResponseAfter = getAllDraft(userToken);

        assertEquals(2, ((List)draftsResponseAfter.getBody().path("data")).size());

        assertEquals(draftsResponseAfter.getBody().path("data[0].document"),
            ResourceLoader.loadJsonToObject(filePath1, Map.class));
        assertEquals(draftsResponseAfter.getBody().path("data[1].document"),
            ResourceLoader.loadJsonToObject(filePath2, Map.class));

        //delete removes only the first draft. So delete needs to be called twice here
        deleteDraft(userToken);
        deleteDraft(userToken);
    }

}
