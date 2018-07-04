package uk.gov.hmcts.reform.divorce;

import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Value;

public abstract class CcdUpdateSupport extends CcdSubmissionSupport {
    private static final String PAYLOAD_CONTEXT_PATH = "ccd-update-payload/";

    static final String EVENT_ID = "paymentMade";

    @Value("${case.maintenance.update.context-path}")
    private String contextPath;

    Response updateCase(String fileName, Long caseId, String eventId, String userToken) throws Exception {
        return
            RestUtil.postToRestService(
                getRequestUrl(caseId, eventId),
                getHeaders(userToken),
                loadJson(fileName, PAYLOAD_CONTEXT_PATH)
            );
    }

    private String getRequestUrl(Long caseId, String eventId) {
        return serverUrl + contextPath + "/" + caseId + "/" + eventId;
    }

    Long getCaseIdFromSubmittingANewCase(String filePath, String userToken) throws Exception {
        Response cmsResponse = submitCase(filePath, userToken);

        return cmsResponse.path("id");
    }
}
