package uk.gov.hmcts.reform.divorce.support;

import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.util.RestUtil;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.util.ResourceLoader.objectToJson;

public abstract class CcdUpdateSupport extends CcdSubmissionSupport {
    private static final String PAYLOAD_CONTEXT_PATH = "ccd-update-payload/";

    protected static final String EVENT_ID = "paymentMade";
    protected static final String BULK_CASE_SCHEDULE_EVENT_ID = "scheduleForListing";

    @Value("${case.maintenance.update.context-path}")
    private String contextPath;

    @Value("${case.maintenance.bulk.update.context-path}")
    private String contextBulkPath;

    protected Response updateCase(String fileName, Long caseId, String eventId, String userToken) {
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

    protected Response updateBulkCase(Map<String, Object> data, Long caseId, String eventId, String userToken) {
        return
            RestUtil.postToRestService(
                getBulkCaseRequestUrl(caseId, eventId),
                getHeaders(userToken),
                data == null ? "{}" : objectToJson(data)
            );
    }

    private String getRequestUrl(Long caseId, String eventId) {
        return serverUrl + contextPath + "/" + caseId + "/" + eventId;
    }

    private String getBulkCaseRequestUrl(Long caseId, String eventId) {
        return serverUrl + contextBulkPath + "/" + caseId + "/" + eventId;
    }

    protected Long getCaseIdFromSubmittingANewCase(UserDetails userDetails) throws Exception {
        Response cmsResponse = submitCase("addresses-no-hwf.json", userDetails);

        return cmsResponse.path("id");
    }

    protected Long getCaseIdFromCompletedCase(UserDetails userDetails) throws Exception {
        Response cmsResponse = submitCase("completed-case-submitted.json", userDetails);

        return cmsResponse.path("id");
    }
}
