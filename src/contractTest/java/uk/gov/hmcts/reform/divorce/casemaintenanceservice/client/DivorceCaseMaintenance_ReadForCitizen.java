package uk.gov.hmcts.reform.divorce.casemaintenanceservice.client;

import static io.pactfoundation.consumer.dsl.LambdaDsl.newJsonBody;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;

import java.io.IOException;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.json.JSONException;
import org.junit.Before;
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
public class DivorceCaseMaintenance_ReadForCitizen {

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

    private static final String USER_ID ="123456";
    private static final String CASE_ID = "2000";
    private static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";
    private CaseDataContent caseDataContent;
    private CaseDetails caseDetails;
    private static final String  ALPHABETIC_REGEX = "[/^[A-Za-z]+$/]+";

    @Before
    public void setUp() throws Exception {

    }

    @Pact(provider = "ccd", consumer = "divorce_caseMaintenanceService")
    RequestResponsePact readForCitizen(PactDslWithProvider builder) {
        // @formatter:off
        return builder
            .given("A Read For Citizen is  requested")
            .uponReceiving("A Read For Citizen is requested")
            .path("/citizens/"
                + USER_ID +
                "/jurisdictions/" + jurisdictionId
                + "/case-types/"
                + caseType
                + "/cases/"
                +  CASE_ID)
            .method("GET")
            .headers(HttpHeaders.AUTHORIZATION, SOME_AUTHORIZATION_TOKEN, SERVICE_AUTHORIZATION,
                SOME_SERVICE_AUTHORIZATION_TOKEN)
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .willRespondWith()
            .matchHeader(HttpHeaders.CONTENT_TYPE, "\\w+\\/[-+.\\w]+;charset=(utf|UTF)-8")
            .status(200)
            .body(newJsonBody((caseDetailObject) -> {
                caseDetailObject.stringMatcher("id", "[0-9]+", "2000");
                caseDetailObject.stringType("jurisdiction",  "DIVORCE");
                caseDetailObject.stringMatcher("case_type", ALPHABETIC_REGEX, "Caveat");
                     // TODO build the Map<Object,Object> data in CaseDetails
            }).build())
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "readForCitizen")
    public void verifyReadForCitizen() throws IOException, JSONException {

        CaseDetails caseDetailsReponse = coreCaseDataApi.readForCitizen(SOME_AUTHORIZATION_TOKEN,
            SOME_SERVICE_AUTHORIZATION_TOKEN, USER_ID, jurisdictionId,
            caseType,CASE_ID);
        assertThat(caseDetailsReponse.getId(), equalTo(2000L));
        assertThat(caseDetailsReponse.getJurisdiction(), is("DIVORCE"));
        assertThat(caseDetailsReponse.getCaseTypeId(), is("Caveat"));

    }
}