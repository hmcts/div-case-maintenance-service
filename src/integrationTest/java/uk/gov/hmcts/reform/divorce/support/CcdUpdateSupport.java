package uk.gov.hmcts.reform.divorce.support;

import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.DivorceCaseProperties;
import uk.gov.hmcts.reform.divorce.util.RestUtil;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.util.ResourceLoader.objectToJson;

public abstract class CcdUpdateSupport extends CcdSubmissionSupport {
    private static final String PAYLOAD_CONTEXT_PATH = "ccd-update-payload/";

    protected static final String EVENT_ID = "paymentMade";

    @Value("${case.maintenance.update.context-path}")
    private String contextPath;

    protected Response updateCase(String fileName, Long caseId, String eventId, String userToken) throws Exception {
        return
            RestUtil.postToRestService(
                getRequestUrl(caseId, eventId),
                getHeaders(userToken),
                fileName == null ? "{}" : loadJson(fileName, PAYLOAD_CONTEXT_PATH)
            );
    }

    protected Response updateCase(Map<String, Object> data, Long caseId, String eventId, String userToken) {
        return
            RestUtil.postToRestService(
                getRequestUrl(caseId, eventId),
                getHeaders(userToken),
                data == null ? "{}" : objectToJson(data)
            );
    }

    private String getRequestUrl(Long caseId, String eventId) {
        return serverUrl + contextPath + "/" + caseId + "/" + eventId;
    }

    protected Long getCaseIdFromSubmittingANewCase(String userToken) throws Exception {
        Response cmsResponse = submitCase("addresses-no-hwf.json", userToken);

        return cmsResponse.path("id");
    }

    protected String getCaseRefFromCompletedCase(String userToken) throws Exception {
        Response cmsResponse = submitCase("completed-case-submitted.json", userToken);

        return cmsResponse.path("case_data.D8caseReference");
    }
}
