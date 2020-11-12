package uk.gov.hmcts.reform.divorce.casemaintenanceservice.client;

import static java.lang.String.valueOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.client.util.ObjectMapperTestUtil.convertObjectToJsonString;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.client.util.PactDslBuilderForCaseDetailsList.buildCaseDetailsDsl;

import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.client.util.ResourceLoader;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(PactConsumerTestExt.class)
@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@PactTestFor(providerName = "ccd", port = "8891")
@SpringBootTest({
    "core_case_data.api.url : localhost:8891"
})
public class DivorceCaseMaintenance_SubmitEventForCaseWorker {

    public static final String SOME_AUTHORIZATION_TOKEN = "Bearer UserAuthToken";
    public static final String SOME_SERVICE_AUTHORIZATION_TOKEN = "ServiceToken";

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

    private static final String USER_ID = "123456";
    private static final String CASE_ID = "2000";
    private static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";
    private CaseDataContent caseDataContent;
    private CaseDetails caseDetails;
    private static final String ALPHABETIC_REGEX = "[/^[A-Za-z]+$/]+";
    private static final String VALID_PAYLOAD_PATH = "json/base-case.json";


    @Before
    public void setUp() throws Exception {

    }

    @BeforeEach
    public void setUpEachTest() throws InterruptedException {
        Thread.sleep(2000);
    }

    @Pact(provider = "ccd", consumer = "divorce_caseMaintenanceService")
    RequestResponsePact submitEventForCaseWorker(PactDslWithProvider builder) throws Exception {
        // @formatter:off
        return builder
            .given("A Submit event for Caseworkder  starts ")
            .uponReceiving("When a Submit Event For Caseworker is triggered.")
            .path("/caseworkers/"+ USER_ID
                + "/jurisdictions/" + jurisdictionId
                + "/case-types/"    + caseType
                + "/cases/"         + CASE_ID
                + "/events"
            )
            .query("ignore-warning=true")
            .method("POST")
            .headers(HttpHeaders.AUTHORIZATION, SOME_AUTHORIZATION_TOKEN, SERVICE_AUTHORIZATION, SOME_SERVICE_AUTHORIZATION_TOKEN)
            .body(convertObjectToJsonString(getCaseDataContent()))
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .willRespondWith()
            .status(200)
            .body(buildCaseDetailsDsl(100L, "someemailaddress.com", false, false))
            .matchHeader(HttpHeaders.CONTENT_TYPE, "\\w+\\/[-+.\\w]+;charset=(utf|UTF)-8")
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "submitEventForCaseWorker")
    public void verifyStartEventForCaseworker() throws Exception {

        caseDataContent = getCaseDataContent();

        final CaseDetails caseDetails = coreCaseDataApi.submitEventForCaseWorker(SOME_AUTHORIZATION_TOKEN,
            SOME_SERVICE_AUTHORIZATION_TOKEN, USER_ID, jurisdictionId, caseType, CASE_ID,true,caseDataContent);

        assertThat(caseDetails.getId(), is(100L));
        assertThat(caseDetails.getJurisdiction(), is("DIVORCE"));
        assertThat(caseDetails.getState(), is("CaseCreated"));

        assertThat(caseDetails.getData().get("applicationType"), is("Personal"));
        assertThat(caseDetails.getData().get("primaryApplicantForenames"), is("Jon"));
        assertThat(caseDetails.getData().get("primaryApplicantSurname"), is("Snow"));

    }

    private CaseDataContent getCaseDataContent() throws Exception {

        final String caseData = ResourceLoader.loadJson(VALID_PAYLOAD_PATH);

        final StartEventResponse startEventResponse = StartEventResponse.builder()
            .eventId(createEventId)
            .token(SOME_AUTHORIZATION_TOKEN)
            .build();

        final CaseDataContent caseDataContent = CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(
                Event.builder()
                    .id(startEventResponse.getEventId())
                    .summary("divSummary")
                    .description("div")
                    .build()
            ).data(convertObjectToJsonString(caseData))
            .build();

        return caseDataContent;
    }
}
