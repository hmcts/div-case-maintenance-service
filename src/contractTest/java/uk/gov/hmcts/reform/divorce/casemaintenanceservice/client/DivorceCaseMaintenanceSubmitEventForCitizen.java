package uk.gov.hmcts.reform.divorce.casemaintenanceservice.client;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.apache.http.client.fluent.Executor;
import org.junit.After;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.client.util.DivorceCaseMaintenancePact;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.client.util.PactDslFixtureHelper;

import java.util.Map;
import java.util.TreeMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.client.util.PactDslBuilderForCaseDetailsList.buildCaseDetailsDsl;

public class DivorceCaseMaintenanceSubmitEventForCitizen extends DivorceCaseMaintenancePact {


    public static final String SOME_AUTHORIZATION_TOKEN = "Bearer UserAuthToken";
    public static final String SOME_SERVICE_AUTHORIZATION_TOKEN = "ServiceToken";
    private static final Long CASE_ID = 2000L;

    @Autowired
    private CoreCaseDataApi coreCaseDataApi;

    @Value("${ccd.jurisdictionid}")
    String jurisdictionId;

    @Value("${ccd.casetype}")
    String caseType;

    CaseDataContent caseDataContent;

    @Value("${ccd.bulk.eventid.create}")
    private String createEventId;

    private static final String USER_ID = "123456";
    private static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";

    Map<String, Object> params = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);


    @BeforeEach
    public void setUpEachTest() throws InterruptedException {
        Thread.sleep(2000);
    }

    @After
    public void teardown() {
        Executor.closeIdleConnections();
    }

    @Pact(provider = "ccd", consumer = "divorce_caseMaintenanceService_citizen")
    RequestResponsePact submitEventForCitizen(PactDslWithProvider builder) {
        // @formatter:off
        return builder
            .given("A SubmitEvent for a Citizen is triggered")
            .uponReceiving("A SubmitEvent for a Citizen is triggered")
            .path("/citizens/"
                + USER_ID
                + "/jurisdictions/"
                + jurisdictionId
                + "/case-types/"
                + caseType
                + "/cases/"
                +  CASE_ID
                + "/events")
            .query("ignore-warning=true")
            .method("POST")
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
    @PactTestFor(pactMethod = "submitEventForCitizen")
    public void verifySubmitEventForCitizen() throws Exception {

        caseDataContent = PactDslFixtureHelper.getCaseDataContent();

        CaseDetails caseDetailsReponse = coreCaseDataApi.submitEventForCitizen(SOME_AUTHORIZATION_TOKEN,
            SOME_SERVICE_AUTHORIZATION_TOKEN, USER_ID, jurisdictionId,
            caseType,CASE_ID.toString(),true,caseDataContent);
        Map<String,Object> dataMap = caseDetailsReponse.getData() ;
        assertThat(dataMap.get("outsideUKGrantCopies"), is(6));
        assertThat(dataMap.get("primaryApplicantForenames"), is("Jon"));
    }

}
