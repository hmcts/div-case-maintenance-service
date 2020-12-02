package uk.gov.hmcts.reform.divorce.casemaintenanceservice.client;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.client.util.DivorceCaseMaintenancePact;

import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.client.util.AssertionHelper.assertCaseDetails;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.client.util.PactDslBuilderForCaseDetailsList.buildStartEventReponse;

public class DivorceCaseMaintenanceStartEventForCitizen extends DivorceCaseMaintenancePact {

    public static final String PAYMENT_REFERENCE_GENERATED = "paymentReferenceGenerated";

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


    @Pact(provider = "ccdDataStoreAPI_Cases", consumer = "divorce_caseMaintenanceService")
    RequestResponsePact startEventForCitizen(PactDslWithProvider builder) {
        // @formatter:off
        return builder
            .given("A Start Event for a Citizen is requested", getCaseDataContentAsMap(caseDataContent))
            .uponReceiving("A Start Event a Citizen")
            .path("/citizens/" + USER_ID + "/jurisdictions/"
                + jurisdictionId + "/case-types/"
                + caseType
                + "/cases/" + CASE_ID
                + "/event-triggers/"
                + PAYMENT_REFERENCE_GENERATED
                + "/token")
            .method("GET")
            .headers(HttpHeaders.AUTHORIZATION, SOME_AUTHORIZATION_TOKEN, SERVICE_AUTHORIZATION,
                SOME_SERVICE_AUTHORIZATION_TOKEN)
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .willRespondWith()
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .status(200)
            .body(buildStartEventReponse(PAYMENT_REFERENCE_GENERATED))
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "startEventForCitizen")
    public void verifyStartEventForCitizen() throws JSONException {

        StartEventResponse startEventResponse = coreCaseDataApi.startEventForCitizen(SOME_AUTHORIZATION_TOKEN,
            SOME_SERVICE_AUTHORIZATION_TOKEN, USER_ID.toString(), jurisdictionId,
            caseType, CASE_ID.toString(), PAYMENT_REFERENCE_GENERATED);

        assertThat(startEventResponse.getEventId(), equalTo(PAYMENT_REFERENCE_GENERATED));
        assertThat(startEventResponse.getCaseDetails().getId(), is(2000L));

        assertCaseDetails(startEventResponse.getCaseDetails());

    }

    @Override
    protected Map<String, Object> getCaseDataContentAsMap(CaseDataContent caseDataContent) throws JSONException {
        Map<String, Object> caseDataContentMap = super.getCaseDataContentAsMap(caseDataContent);
        caseDataContentMap.put(EVENT_ID, PAYMENT_REFERENCE_GENERATED);
        return caseDataContentMap;
    }

}
