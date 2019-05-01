package uk.gov.hmcts.reform.divorce.support;

import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.divorce.util.ResourceLoader;
import uk.gov.hmcts.reform.divorce.util.RestUtil;

import java.util.Collections;
import java.util.Map;

public abstract class PetitionSupport extends CcdUpdateSupport {
    protected static final String CCD_FORMAT_DRAFT_CONTEXT_PATH = "ccd-format-draft/";
    protected static final String DIVORCE_FORMAT_DRAFT_CONTEXT_PATH = "divorce-format-draft/";
    protected static final String DIVORCE_FORMAT_KEY = "divorceFormat";

    @Value("${case.maintenance.draft.context-path}")
    private String draftContextPath;

    @Value("${case.maintenance.petition.context-path}")
    private String petitionContextPath;

    @Value("${case.maintenance.get-case.context-path}")
    private String getCaseContextPath;

    @Value("${case.maintenance.amend-petition-draft.context-path}")
    private String amendPetitionContextPath;

    private String searchContextPath = "/casemaintenance/version/1//search";

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

    protected Response retrieveCase(String userToken) {
        return
            RestUtil.getFromRestService(
                getRetrieveCaseRequestUrl(),
                getHeaders(userToken)
            );
    }

    protected Response retrieveCaseById(String userToken, String caseId) {
        return
            RestUtil.getFromRestService(
                getCaseRequestUrl() + "/" + caseId,
                getHeaders(userToken)
            );
    }

    protected Response searchCases(String userToken, String query) {
        return
            RestUtil.postToRestService(
                getSearchRequestUrl(),

                getHeaders(userToken),
                query
            );
    }

    protected Response getCase(String userToken) {
        return
            RestUtil.getFromRestService(
                getCaseRequestUrl(),
                getHeaders(userToken)
            );
    }

    protected Response getAllDraft(String userToken) {
        return
            RestUtil.getFromRestService(
                draftsRequestUrl(),
                getHeaders(userToken)
            );
    }

    protected Response putAmendedPetitionDraft(String userToken) {
        return
            RestUtil.putToRestService(
                getGetAmendPetitionContextPath(),
                getHeaders(userToken),
                "",
                Collections.emptyMap()
            );
    }

    private String getGetAmendPetitionContextPath() {
        return serverUrl + amendPetitionContextPath;
    }

    protected String getRetrieveCaseRequestUrl() {
        return serverUrl + petitionContextPath;
    }

    private String getCaseRequestUrl() {
        return serverUrl + getCaseContextPath;
    }

    private String getSearchRequestUrl() {
        return serverUrl + searchContextPath;
    }

    private String draftsRequestUrl() {
        return serverUrl + draftContextPath;
    }
}
