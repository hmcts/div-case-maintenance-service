package uk.gov.hmcts.reform.divorce.casemaintenanceservice.client.util;

import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.Event;

import java.util.Map;


public final class PactDslFixtureHelper {

    public static final String SOME_AUTHORIZATION_TOKEN = "Bearer UserAuthToken";
    public static final String SOME_SERVICE_AUTHORIZATION_TOKEN = "ServiceToken";
    private static final String VALID_PAYLOAD_PATH = "json/base-case.json";

    static final String DIVORCE_CASE_SUBMISSION_EVENT_SUMMARY = "Divorce case submission event";
    static final String DIVORCE_CASE_SUBMISSION_EVENT_DESCRIPTION = "Submitting Divorce Case";

    public static CaseDataContent getCaseDataContent(String eventId) throws Exception {

        return PactDslFixtureHelper.getCaseDataContentWithPath(eventId, VALID_PAYLOAD_PATH);

    }

    public static CaseDataContent getCaseDataContentWithPath(String eventId, String payloadPath) throws Exception {

        final String caseData = ResourceLoader.loadJson(payloadPath);

        final CaseDataContent caseDataContent = CaseDataContent.builder()
            .eventToken(SOME_AUTHORIZATION_TOKEN)
            .event(
                Event.builder()
                    .id(eventId)
                    .summary(DIVORCE_CASE_SUBMISSION_EVENT_SUMMARY)
                    .description(DIVORCE_CASE_SUBMISSION_EVENT_DESCRIPTION)
                    .build()
            ).data(ObjectMapperTestUtil.convertStringToObject(caseData, Map.class))
            .build();
        return caseDataContent;
    }
}
