package uk.gov.hmcts.reform.divorce.support;

import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.divorce.util.ResourceLoader;
import uk.gov.hmcts.reform.divorce.util.RestUtil;

import java.util.Collections;
import java.util.Map;

public abstract class PetitionSupport extends CcdUpdateSupport {
    private static final String CHECK_CCD = "checkCcd";
    protected static final String CCD_FORMAT_DRAFT_CONTEXT_PATH = "ccd-format-draft/";
    protected static final String DIVORCE_FORMAT_DRAFT_CONTEXT_PATH = "divorce-format-draft/";
    protected static final String DIVORCE_FORMAT_KEY = "divorceFormat";

    @Value("${case.maintenance.draft.context-path}")
    private String draftContextPath;

    @Value("${case.maintenance.petition.context-path}")
    private String petitionContextPath;

    @Value("${case.maintenance.get-case.context-path}")
    private String getCaseContextPath;


    protected Response saveDraft(String userToken, String fileName, Map<String, Object> params) throws Exception {
        return
            RestUtil.putToRestService(
                draftsRequestUrl(),
                getHeaders(userToken),
                fileName == null ? null : ResourceLoader.loadJson(fileName),
                params
            );
    }

    protected Response createDraft(String userToken, String fileName, Map<String, Object> params) throws Exception {
        return
            RestUtil.postToRestService(
                draftsRequestUrl(),
                getHeaders(userToken),
                fileName == null ? null : ResourceLoader.loadJson(fileName),
                params
            );
    }

    protected Response deleteDraft(String userToken) {
        return
            RestUtil.deleteOnRestService(
                draftsRequestUrl(),
                getHeaders(userToken)
            );
    }

    protected Response retrieveCase(String userToken, Boolean checkCcd) {
        return
            RestUtil.getFromRestService(
                getRetrieveCaseRequestUrl(),
                getHeaders(userToken),
                checkCcd == null ? null : Collections.singletonMap(CHECK_CCD, checkCcd)
            );
    }

    protected Response getCase(String userToken) {
        return
            RestUtil.getFromRestService(
                getCaseRequestUrl(),
                getHeaders(userToken),
                null
            );
    }

    protected Response getAllDraft(String userToken) {
        return
            RestUtil.getFromRestService(
                draftsRequestUrl(),
                getHeaders(userToken),
                Collections.emptyMap()
            );
    }

    protected String getRetrieveCaseRequestUrl() {
        return serverUrl + petitionContextPath;
    }

    private String getCaseRequestUrl() {
        return serverUrl + getCaseContextPath;
    }

    private String draftsRequestUrl() {
        return serverUrl + draftContextPath;
    }
}
