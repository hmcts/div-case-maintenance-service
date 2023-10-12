package uk.gov.hmcts.reform.divorce.casemaintenanceservice.client.ccd;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.client.util.CcdConsumerTestBase;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.Assert.assertNotNull;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.client.util.PactDslBuilderForCaseDetailsList.buildNewListOfCaseDetailsDsl;

public class SearchForCitizenConsumerTest extends CcdConsumerTestBase {

    private static final String USER_ID = "123456";
    private static final Long CASE_ID = 20000000L;

    Map<String, Object> params = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    @Pact(provider = "ccdDataStoreAPI_Cases", consumer = "divorce_caseMaintenanceService")
    public RequestResponsePact searchForCitizen(PactDslWithProvider builder) throws JSONException {
        params = Collections.emptyMap();

        return builder
            .given("A Search cases for a Citizen is requested", setUpStateMapForProviderWithCaseData(caseDataContent))
            .uponReceiving("A Search Cases for a Citizen")
            .path(buildPath())
            .method("GET")
            .headers(HttpHeaders.AUTHORIZATION, SOME_AUTHORIZATION_TOKEN,
                SERVICE_AUTHORIZATION, SOME_SERVICE_AUTHORIZATION_TOKEN)
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .willRespondWith()
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .status(200)
            .body(
                buildNewListOfCaseDetailsDsl(CASE_ID))
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "searchForCitizen")
    public void searchForCitizen() throws JSONException {

        final Map<String, String> searchCriteria = Collections.EMPTY_MAP;
        List<CaseDetails> caseDetailsList = coreCaseDataApi.searchForCitizen(SOME_AUTHORIZATION_TOKEN,
            SOME_SERVICE_AUTHORIZATION_TOKEN, USER_ID, jurisdictionId,
            caseType, searchCriteria);
        assertNotNull(caseDetailsList);
    }

    private String buildPath() {
        return new StringBuilder()
            .append("/citizens/")
            .append(USER_ID)
            .append("/jurisdictions/")
            .append(jurisdictionId)
            .append("/case-types/")
            .append(caseType)
            .append("/cases").toString();
    }
}
