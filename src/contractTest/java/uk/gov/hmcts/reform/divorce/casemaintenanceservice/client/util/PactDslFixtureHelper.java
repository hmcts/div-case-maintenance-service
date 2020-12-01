package uk.gov.hmcts.reform.divorce.casemaintenanceservice.client.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;


public final  class PactDslFixtureHelper {


    public static final String CREATE_EVENT = "create";
    @Autowired
    private ObjectMapper objectMapper;

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

        final StartEventResponse startEventResponse = StartEventResponse.builder()
            .eventId(eventId)
            .token(SOME_AUTHORIZATION_TOKEN)
            .build();

        final CaseDataContent caseDataContent = CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(
                Event.builder()
                    .id(startEventResponse.getEventId())
                    .summary(DIVORCE_CASE_SUBMISSION_EVENT_SUMMARY)
                    .description(DIVORCE_CASE_SUBMISSION_EVENT_DESCRIPTION)
                    .build()
            ).data(ObjectMapperTestUtil.convertStringToObject(caseData, Map.class))
            .build();
        return caseDataContent;
    }

    private File getFile(String fileName) throws FileNotFoundException {
        return org.springframework.util.ResourceUtils.getFile(this.getClass().getResource("/json/" + fileName));
    }

    protected CaseDetails getCaseDetails(String fileName) throws JSONException, IOException {
        File file = getFile(fileName);
        return  objectMapper.readValue(file, CaseDetails.class);
    }

}
