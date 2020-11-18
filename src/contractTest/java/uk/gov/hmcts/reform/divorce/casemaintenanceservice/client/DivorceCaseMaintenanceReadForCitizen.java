package uk.gov.hmcts.reform.divorce.casemaintenanceservice.client;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.apache.http.client.fluent.Executor;
import org.hamcrest.core.Is;
import org.json.JSONException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.client.util.DivorceCaseMaintenancePact;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.client.util.PactDslBuilderForCaseDetailsList.buildCaseDetailsDsl;


public class DivorceCaseMaintenanceReadForCitizen extends DivorceCaseMaintenancePact {

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

    private static final String USER_ID = "123456";
    private static final Long CASE_ID = 2000L;
    private static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";

    @BeforeEach
    public void setUp() throws Exception {

    }

    @AfterEach
    void teardown() {
        Executor.closeIdleConnections();
    }

    @Pact(provider = "ccdDataStoreAPI_CaseController", consumer = "divorce_caseMaintenanceService")
    RequestResponsePact readForCitizen(PactDslWithProvider builder) {
        // @formatter:off
        return builder
            .given("A Read For Citizen is  requested")
            .uponReceiving("A Read For Citizen is requested")
            .path("/citizens/"
                + USER_ID
                + "/jurisdictions/"
                + jurisdictionId
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
            .body(buildCaseDetailsDsl(CASE_ID, "emailAddress@email.com",false, false))
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "readForCitizen")
    public void verifyReadForCitizen() throws IOException, JSONException {

        CaseDetails caseDetailsReponse = coreCaseDataApi.readForCitizen(SOME_AUTHORIZATION_TOKEN,
            SOME_SERVICE_AUTHORIZATION_TOKEN, USER_ID, jurisdictionId,
            caseType,String.valueOf(CASE_ID));

        assertThat(caseDetailsReponse.getId(), Is.is(2000L));
        assertThat(caseDetailsReponse.getJurisdiction(), Is.is("DIVORCE"));

        assertThat(caseDetailsReponse.getData().get("applicationType"), equalTo("Personal"));
        assertThat(caseDetailsReponse.getData().get("primaryApplicantForenames"), equalTo("Jon"));
        assertThat(caseDetailsReponse.getData().get("primaryApplicantSurname"), equalTo("Snow"));
    }
}
