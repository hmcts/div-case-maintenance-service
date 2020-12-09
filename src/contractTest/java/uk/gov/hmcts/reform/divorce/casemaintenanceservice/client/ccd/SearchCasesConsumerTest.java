package uk.gov.hmcts.reform.divorce.casemaintenanceservice.client.ccd;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.client.util.CcdConsumerTestBase;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.client.util.ResourceLoader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.client.util.AssertionHelper.assertCaseDetails;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.client.util.PactDslBuilderForCaseDetailsList.buildSearchResultDsl;

public class SearchCasesConsumerTest extends CcdConsumerTestBase {

    private static final String VALID_QUERY = "json/esQuery.json";
    private String queryString;

    @BeforeEach
    public void setUpEachTest() throws Exception {
        Thread.sleep(2000);
        queryString = ResourceLoader.loadJson(VALID_QUERY);
    }

    @Pact(provider = "ccdDataStoreAPI_Cases", consumer = "divorce_caseMaintenanceService")
    public RequestResponsePact searchCases(PactDslWithProvider builder) throws Exception {
        // @formatter:off
        return builder
            .given("A Search for cases is requested", setUpStateMapForProviderWithCaseData(caseDataContent))
            .uponReceiving("A Search Cases request")
            .path("/searchCases")
            .query("ctid=DIVORCE")
            .method("POST")
            .body(queryString)
            .headers(HttpHeaders.AUTHORIZATION, SOME_AUTHORIZATION_TOKEN, SERVICE_AUTHORIZATION,
                SOME_SERVICE_AUTHORIZATION_TOKEN)
            .headers(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .willRespondWith()
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .status(200)
            .body(buildSearchResultDsl())
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "searchCases")
    public void verifySearchCases() throws JSONException {

        SearchResult searchResult = coreCaseDataApi.searchCases(SOME_AUTHORIZATION_TOKEN,
            SOME_SERVICE_AUTHORIZATION_TOKEN, "DIVORCE", queryString);

        assertEquals(searchResult.getTotal(), 123);
        assertEquals(searchResult.getCases().size(), 1);

        assertCaseDetails(searchResult.getCases().get(0)); // CaseDetail-1
    }

}
