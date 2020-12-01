package uk.gov.hmcts.reform.divorce.casemaintenanceservice.client;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.apache.http.client.fluent.Executor;
import org.json.JSONException;
import org.junit.After;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.client.util.DivorceCaseMaintenancePact;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.client.util.PactDslFixtureHelper;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.client.util.AssertionHelper.assertCaseDetails;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.client.util.ObjectMapperTestUtil.convertObjectToJsonString;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.client.util.PactDslBuilderForCaseDetailsList.buildCaseDetailsDsl;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.client.util.PactDslFixtureHelper.getCaseDataContent;

public class DivorceCaseMaintenanceSubmitEventForCaseWorker extends DivorceCaseMaintenancePact {

    public static final String HWF_APPLICATION_ACCEPTED = "hwfApplicationAccepted";
    private Map<String, Object> caseDetailsMap;
    private CaseDataContent caseDataContent;

    @BeforeAll
    public void setUp() throws Exception {

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

    @Pact(provider = "ccdDataStoreAPI_CaseController", consumer = "divorce_caseMaintenanceService")
    RequestResponsePact submitEventForCaseWorker(PactDslWithProvider builder) throws Exception {
        // @formatter:off
        return builder
            .given("A SubmitEvent for Caseworker is triggered", getCaseDataContentAsMap(caseDataContent))
            .uponReceiving("A SubmitEvent For Caseworker is received.")
            .path("/caseworkers/" + USER_ID
                + "/jurisdictions/" + jurisdictionId
                + "/case-types/" + caseType
                + "/cases/" + CASE_ID
                + "/events"
            )
            .query("ignore-warning=true")
            .method("POST")
            .headers(HttpHeaders.AUTHORIZATION, SOME_AUTHORIZATION_TOKEN, SERVICE_AUTHORIZATION, SOME_SERVICE_AUTHORIZATION_TOKEN)
            .body(convertObjectToJsonString(getCaseDataContent(HWF_APPLICATION_ACCEPTED)))
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .willRespondWith()
            .status(201)
            .body(buildCaseDetailsDsl(CASE_ID))
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "submitEventForCaseWorker")
    public void verifySubmitEventForCaseworker() throws Exception {

        caseDataContent = PactDslFixtureHelper.getCaseDataContent(HWF_APPLICATION_ACCEPTED);

        final CaseDetails caseDetails = coreCaseDataApi.submitEventForCaseWorker(SOME_AUTHORIZATION_TOKEN,
            SOME_SERVICE_AUTHORIZATION_TOKEN, USER_ID, jurisdictionId, caseType, CASE_ID.toString(), true, caseDataContent);

        assertThat(caseDetails.getId(), is(CASE_ID));
        assertThat(caseDetails.getJurisdiction(), is("DIVORCE"));
        assertCaseDetails(caseDetails);
    }

    @Override
    protected Map<String, Object> getCaseDataContentAsMap(CaseDataContent caseDataContent) throws JSONException {
        Map<String, Object> caseDataContentMap = super.getCaseDataContentAsMap(caseDataContent);
        caseDataContentMap.put(EVENT_ID, HWF_APPLICATION_ACCEPTED);
        return caseDataContentMap;
    }

    @After
    public void teardown() {
        Executor.closeIdleConnections();
    }

}
