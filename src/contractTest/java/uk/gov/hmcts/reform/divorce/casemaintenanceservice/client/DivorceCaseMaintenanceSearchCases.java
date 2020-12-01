package uk.gov.hmcts.reform.divorce.casemaintenanceservice.client;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.fluent.Executor;
import org.json.JSONException;
import org.junit.After;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.client.util.DivorceCaseMaintenancePact;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.client.util.PactDslFixtureHelper;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.client.util.ResourceLoader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.client.util.AssertionHelper.assertCaseDetails;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.client.util.ObjectMapperTestUtil.convertObjectToJsonString;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.client.util.PactDslBuilderForCaseDetailsList.buildSearchResultDsl;

public class DivorceCaseMaintenanceSearchCases extends DivorceCaseMaintenancePact {


    private Map<String, Object> caseDetailsMap;
    private CaseDataContent caseDataContent;
    private static final String VALID_QUERY = "json/esQuery.json";
    private String queryString;


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
    public void setUpEachTest() throws Exception {
        Thread.sleep(2000);
         queryString = ResourceLoader.loadJson(VALID_QUERY);
    }

    @Pact(provider = "ccdDataStoreAPI_CaseController", consumer = "divorce_caseMaintenanceService")
    public RequestResponsePact searchCasesForCitizen(PactDslWithProvider builder) throws Exception {
        // @formatter:off
        return builder
            .given("SearchCases for Citizen is requested", getCaseDataContentAsMap(caseDataContent))
            .uponReceiving("Search Cases Request is requested for citizen")
            .path("/searchCases")
            .query("ctid=DIVORCE")
            .method("POST")
            .body( queryString)
            .headers(HttpHeaders.AUTHORIZATION, SOME_AUTHORIZATION_TOKEN, SERVICE_AUTHORIZATION,
                SOME_SERVICE_AUTHORIZATION_TOKEN)
            .headers(HttpHeaders.CONTENT_TYPE,MediaType.APPLICATION_JSON_VALUE)
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .willRespondWith()
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .status(200)
            .body(buildSearchResultDsl())
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "searchCasesForCitizen")
    public void verifySearchCasesForCitizen() throws JSONException {

        SearchResult searchResult = coreCaseDataApi.searchCases(SOME_AUTHORIZATION_TOKEN,
            SOME_SERVICE_AUTHORIZATION_TOKEN, "DIVORCE", queryString);

        assertEquals(searchResult.getTotal() , 123);
        assertEquals(searchResult.getCases().size() ,1) ;

        assertCaseDetails(searchResult.getCases().get(0)); // CaseDetail-1
    }


    @After
    void teardown() {
        Executor.closeIdleConnections();
    }
}
