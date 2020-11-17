package uk.gov.hmcts.reform.divorce.casemaintenanceservice.client;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.client.util.PactDslBuilderForCaseDetailsList.buildStartEventReponse;

import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;

import java.io.IOException;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.json.JSONException;
import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.client.util.DivorceCaseMaintenancePact;

public class DivorceCaseMaintenanceStartEventForCaseWorker extends DivorceCaseMaintenancePact {

    public static final String SOME_AUTHORIZATION_TOKEN = "Bearer UserAuthToken";
    public static final String SOME_SERVICE_AUTHORIZATION_TOKEN = "ServiceToken";

    @Autowired
    private CoreCaseDataApi coreCaseDataApi;

    @Value("${ccd.jurisdictionid}")
    String jurisdictionId;

    @Value("${ccd.casetype}")
    String caseType;

    @Value("${ccd.eventid.create}")
    String createEventId;

    private static final String USER_ID ="123456";
    private static final String CASE_ID = "2000";
    private static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";

    @Before
    public void setUp() throws Exception {

    }

    @BeforeEach
    public void setUpEachTest() throws InterruptedException {
        Thread.sleep(2000);
    }

    @Pact(provider = "ccd", consumer = "divorce_caseMaintenanceService_caseworker")
    RequestResponsePact startEventForCaseWorker(PactDslWithProvider builder) {
        // @formatter:off
        return builder
            .given("A StartEvent for Caseworkder is  requested")
            .uponReceiving("A StartEvent for a caseworker is received.")
            .path("/caseworkers/" + USER_ID + "/jurisdictions/"
                + jurisdictionId + "/case-types/"
                + caseType
                + "/cases/"
                +  CASE_ID
                + "/event-triggers/"
                + createEventId
                + "/token")
            .method("GET")
            .headers(HttpHeaders.AUTHORIZATION, SOME_AUTHORIZATION_TOKEN, SERVICE_AUTHORIZATION, SOME_SERVICE_AUTHORIZATION_TOKEN)
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .willRespondWith()
            .matchHeader(HttpHeaders.CONTENT_TYPE, "\\w+\\/[-+.\\w]+;charset=(utf|UTF)-8")
            .status(200)
            .body(buildStartEventReponse("100", "testServiceToken" , "emailAddress@email.com", true, true))
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "startEventForCaseWorker")
    public void verifyStartEventForCaseworker() throws IOException, JSONException {

        final StartEventResponse startEventResponse = coreCaseDataApi.startEventForCaseWorker(SOME_AUTHORIZATION_TOKEN,
            SOME_SERVICE_AUTHORIZATION_TOKEN, USER_ID, jurisdictionId,caseType,CASE_ID,createEventId);

        assertThat(startEventResponse.getEventId(), is("100"));
        assertThat(startEventResponse.getToken(), is("testServiceToken"));

        assertThat(startEventResponse.getCaseDetails().getId(), is((2000L)));
        assertThat(startEventResponse.getCaseDetails().getJurisdiction(), is("DIVORCE"));
        assertThat(startEventResponse.getCaseDetails().getCallbackResponseStatus(), is("DONE"));
        assertThat(startEventResponse.getCaseDetails().getCaseTypeId(), is("GRANT_OF_REPRESENTATION"));
    }
}
