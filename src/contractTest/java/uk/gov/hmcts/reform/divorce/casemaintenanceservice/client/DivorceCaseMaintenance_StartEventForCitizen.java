package uk.gov.hmcts.reform.divorce.casemaintenanceservice.client;

import static io.pactfoundation.consumer.dsl.LambdaDsl.newJsonBody;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.apache.http.client.fluent.Executor;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.junit.jupiter.SpringExtension;

//import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties.D8_PETITIONER_EMAIL;
//import au.com.dius.pact.core.model.RequestResponsePact;
//import au.com.dius.pact.core.model.annotations.Pact;

@ExtendWith(PactConsumerTestExt.class)
@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@PactTestFor(providerName = "ccd", port = "8891")
@SpringBootTest({
    "core_case_data.api.url : localhost:8891"
})
public class DivorceCaseMaintenance_StartEventForCitizen {

    public static final String SOME_AUTHORIZATION_TOKEN = "Bearer UserAuthToken";
    public static final String SOME_SERVICE_AUTHORIZATION_TOKEN = "ServiceToken";
    private static final String ACCESS_TOKEN = "someAccessToken";
    public static final String REGEX_DATE = "^((19|2[0-9])[0-9]{2})-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])$";
    private static final String TOKEN = "someToken";
    private static final String DIVORCE_CASE_SUBMISSION_EVENT_SUMMARY = "DIVORCE_CASE_SUBMISSION_EVENT_SUMMARY";
    private static final String DIVORCE_CASE_SUBMISSION_EVENT_DESCRIPTION = "DIVORCE_CASE_SUBMISSION_EVENT_DESCRIPTION";
    public static final String D8_PETITIONER_EMAIL = "D8PetitionerEmail";
    public static final String D8_REASON_FOR_DIVORCE = "D8ReasonForDivorce";
    public static final String TEST_USER_EMAIL = "test@email.com";


    @Autowired
    private CoreCaseDataApi coreCaseDataApi;

    @Autowired
    ObjectMapper objectMapper;

    @Value("${ccd.jurisdictionid}")
    String jurisdictionId;

    @Value("${ccd.casetype}")
    String caseType;

    @Value("${ccd.eventid.create}")
    String createEventId;

    private static final String USER_ID ="123456";
    private static final String CASE_ID = "654321";
    private static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";
    private CaseDataContent caseDataContent;
    private CaseDetails caseDetails;
    private static final String  ALPHABETIC_REGEX = "[/^[A-Za-z]+$/]+";

    @Before
    public void setUp() throws Exception {

    }

    @Pact(provider = "ccd", consumer = "divorce_caseMaintenanceService")
    RequestResponsePact startEventForCitizen(PactDslWithProvider builder) {
        // @formatter:off
        return builder
            .given("A start request for citizen is requested")
            .uponReceiving("a request for a valid start event")
            .path("/citizens/" + USER_ID + "/jurisdictions/"
                + jurisdictionId + "/case-types/"
                + caseType
                + "/cases/" + CASE_ID
                + "/event-triggers/"
                + createEventId
                + "/token")
            .method("GET")
            .headers(HttpHeaders.AUTHORIZATION, SOME_AUTHORIZATION_TOKEN, SERVICE_AUTHORIZATION,
                SOME_SERVICE_AUTHORIZATION_TOKEN)
            .willRespondWith()
            .matchHeader(HttpHeaders.CONTENT_TYPE, "\\w+\\/[-+.\\w]+;charset=(utf|UTF)-8")
            .status(200)
            .body(newJsonBody((o) -> {
                o.stringValue("event_id", createEventId)
                    .stringType("token", "123234543456")
                    .object("case_details", (cd) ->{
                        cd.numberValue("id", 2000L);
                        cd.stringMatcher("jurisdiction",  ALPHABETIC_REGEX,"DIVORCE");
                        cd.stringMatcher("callback_response_status", ALPHABETIC_REGEX,  "DONE");
                        cd.stringMatcher("case_type", ALPHABETIC_REGEX,  "DIVORCE");
                        // TODO build the Map<Object,Object> data in CaseDetails
                    });

                ;
            }).build())
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "startEventForCitizen")
    public void verifyStartEventForCitizen() throws IOException, JSONException {

        StartEventResponse startEventResponse = coreCaseDataApi.startEventForCitizen(SOME_AUTHORIZATION_TOKEN,
            SOME_SERVICE_AUTHORIZATION_TOKEN, USER_ID.toString(), jurisdictionId,
            caseType, CASE_ID.toString(), createEventId);
        assertThat(startEventResponse.getEventId(), equalTo(createEventId));
        assertThat(startEventResponse.getCaseDetails().getId(), is(2000L));
        assertThat(startEventResponse.getCaseDetails().getJurisdiction(), is("DIVORCE"));
        assertThat(startEventResponse.getCaseDetails().getCallbackResponseStatus(), is("DONE"));
        assertThat(startEventResponse.getCaseDetails().getCaseTypeId(), is("DIVORCE"));

    }

    @After
    void teardown() {
            Executor.closeIdleConnections();
    }
}
