package uk.gov.hmcts.reform.divorce.casemaintenanceservice.client;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.client.util.DivorceCaseMaintenancePact;

import java.io.IOException;
import java.util.Map;

import static junit.framework.TestCase.assertNotNull;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.client.util.AssertionHelper.assertCaseDetails;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.client.util.PactDslBuilderForCaseDetailsList.buildStartEventReponse;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.client.util.PactDslBuilderForCaseDetailsList.buildStartEventResponseWithEmptyCaseDetails;

public class DivorceCaseMaintenanceStartForCaseWorker extends DivorceCaseMaintenancePact {

    public static final String EVENT_ID = "eventId";

    private Map<String, Object> caseDetailsMap;
    private CaseDataContent caseDataContent;


    @BeforeAll
    public void setUp() throws Exception {
        Thread.sleep(2000);
        caseDetailsMap = getCaseDetailsAsMap("divorce-map.json");
        caseDataContent = CaseDataContent.builder()
            .eventToken("someEventToken")
            .event(
                Event.builder()
                    .id(createEventId)
                    .summary(DIVORCE_CASE_SUBMISSION_EVENT_SUMMARY)
                    .description(DIVORCE_CASE_SUBMISSION_EVENT_DESCRIPTION)
                    .build()
            ).data(caseDetailsMap.get("case_data"))
            .build();
    }

    @BeforeEach
    public void setUpEachTest() throws InterruptedException {
        Thread.sleep(2000);
    }

    @Pact(provider = "ccdDataStoreAPI_Cases", consumer = "divorce_caseMaintenanceService")
    RequestResponsePact startForCaseWorker(PactDslWithProvider builder) {
        // @formatter:off
        return builder
            .given("A Start for a Caseworker is requested", getCaseDataContentAsMap(caseDataContent))
            .uponReceiving("A Start for a Caseworker")
            .path("/caseworkers/" + USER_ID + "/jurisdictions/"
                + jurisdictionId + "/case-types/"
                + caseType
                + "/event-triggers/"
                + createEventId
                + "/token")
            .method("GET")
            .headers(HttpHeaders.AUTHORIZATION, SOME_AUTHORIZATION_TOKEN, SERVICE_AUTHORIZATION,
                SOME_SERVICE_AUTHORIZATION_TOKEN)
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .willRespondWith()
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .status(200)
            .body(buildStartEventResponseWithEmptyCaseDetails(createEventId))
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "startForCaseWorker")
    public void verifyStartEventForCaseworker() throws IOException, JSONException {

        StartEventResponse startEventResponse = coreCaseDataApi.startForCaseworker(SOME_AUTHORIZATION_TOKEN,
            SOME_SERVICE_AUTHORIZATION_TOKEN, USER_ID, jurisdictionId,
            caseType, createEventId);

        assertThat(startEventResponse.getEventId(), equalTo(createEventId));

        assertNotNull(startEventResponse.getCaseDetails());

    }

    @Override
    protected Map<String, Object> getCaseDataContentAsMap(CaseDataContent caseDataContent) throws JSONException {
        Map<String, Object> caseDataContentMap = super.getCaseDataContentAsMap(caseDataContent);
        caseDataContentMap.put(EVENT_ID, createEventId);
        return caseDataContentMap;
    }

}
