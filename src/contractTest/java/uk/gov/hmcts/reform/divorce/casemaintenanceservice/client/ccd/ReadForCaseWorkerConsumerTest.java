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

import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.client.util.AssertionHelper.assertCaseDetails;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.client.util.PactDslBuilderForCaseDetailsList.buildCaseDetailsDsl;

public class ReadForCaseWorkerConsumerTest extends CcdConsumerTestBase {

    @Pact(provider = "ccdDataStoreAPI_Cases", consumer = "divorce_caseMaintenanceService")
    public RequestResponsePact readForCaseDetails(PactDslWithProvider builder) throws JSONException {
        return builder
            .given("A Read for a Caseworker is requested", setUpStateMapForProviderWithCaseData(caseDataContent))
            .uponReceiving("A Read For a Caseworker")
            .path(buildPath())
            .method("GET")
            .headers(HttpHeaders.AUTHORIZATION, SOME_AUTHORIZATION_TOKEN,
                SERVICE_AUTHORIZATION, SOME_SERVICE_AUTHORIZATION_TOKEN)
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .willRespondWith()
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .status(200)
            .body(buildCaseDetailsDsl(CASE_ID))
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "readForCaseDetails")
    public void verifyReadForCaseDetails() throws JSONException {

        CaseDetails caseDetailsReponse =
            coreCaseDataApi.readForCaseWorker(
                SOME_AUTHORIZATION_TOKEN,
                SOME_SERVICE_AUTHORIZATION_TOKEN,
                USER_ID,
                jurisdictionId,
                caseType,
                CASE_ID.toString());
        assertCaseDetails(caseDetailsReponse);
    }

    private String buildPath() {
        return new StringBuilder()
            .append("/caseworkers/")
            .append(USER_ID)
            .append("/jurisdictions/")
            .append(jurisdictionId)
            .append("/case-types/")
            .append(caseType)
            .append("/cases/")
            .append(CASE_ID).toString();
    }
}
