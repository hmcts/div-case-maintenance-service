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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.client.util.AssertionHelper.assertCaseDetails;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.client.util.ObjectMapperTestUtil.convertObjectToJsonString;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.client.util.PactDslBuilderForCaseDetailsList.buildCaseDetailsDsl;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.client.util.PactDslFixtureHelper.getCaseDataContent;

public class DivorceCaseMaintenanceSubmitEventForCaseWorker  extends DivorceCaseMaintenancePact  {

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

    private static final String CASE_ID = "2000";
    private CaseDataContent caseDataContent;
    private static final String USER_ID = "123456";
    private static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";


    @BeforeEach
    public void setUpEachTest() throws InterruptedException {
        Thread.sleep(2000);
    }

    @After
    public void teardown() {
        Executor.closeIdleConnections();
    }


    @Pact(provider = "ccd", consumer = "divorce_caseMaintenanceService_caseworker")
    RequestResponsePact submitEventForCaseWorker(PactDslWithProvider builder) throws Exception {
        // @formatter:off
        return builder
            .given("A SubmitEvent for Caseworker is triggered")
            .uponReceiving("A SubmitEvent For Caseworker is received.")
            .path("/caseworkers/"   + USER_ID
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

        caseDataContent = PactDslFixtureHelper.getCaseDataContent();

        final CaseDetails caseDetails = coreCaseDataApi.submitEventForCaseWorker(SOME_AUTHORIZATION_TOKEN,
            SOME_SERVICE_AUTHORIZATION_TOKEN, USER_ID, jurisdictionId, caseType, CASE_ID,true,caseDataContent);

        assertThat(caseDetails.getId(), is(100L));
        assertThat(caseDetails.getJurisdiction(), is("DIVORCE"));
        assertCaseDetails(caseDetails);
    }
}
