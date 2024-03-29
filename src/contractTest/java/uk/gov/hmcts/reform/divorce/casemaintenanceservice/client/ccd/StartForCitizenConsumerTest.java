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
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.client.util.CcdConsumerTestBase;

import java.io.IOException;
import java.util.Map;

import static junit.framework.TestCase.assertNotNull;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.client.util.PactDslBuilderForCaseDetailsList.buildStartEventResponseWithEmptyCaseDetails;

public class StartForCitizenConsumerTest extends CcdConsumerTestBase {

    @Pact(provider = "ccdDataStoreAPI_Cases", consumer = "divorce_caseMaintenanceService")
    public RequestResponsePact startForCitizen(PactDslWithProvider builder) {
        return builder
            .given("A Start for a Citizen is requested", setUpStateMapForProviderWithoutCaseData())
            .uponReceiving("A Start for a Citizen")
            .path(buildPath())
            .method("GET")
            .headers(HttpHeaders.AUTHORIZATION, SOME_AUTHORIZATION_TOKEN,
                SERVICE_AUTHORIZATION, SOME_SERVICE_AUTHORIZATION_TOKEN)
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .willRespondWith()
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .status(200)
            .body(buildStartEventResponseWithEmptyCaseDetails(createEventId))
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "startForCitizen")
    public void verifyStartForCitizen() throws IOException, JSONException {

        StartEventResponse startEventResponse = coreCaseDataApi.startForCitizen(SOME_AUTHORIZATION_TOKEN,
            SOME_SERVICE_AUTHORIZATION_TOKEN, USER_ID, jurisdictionId,
            caseType, createEventId);

        assertThat(startEventResponse.getEventId(), equalTo(createEventId));
        CaseDetails caseDetails = startEventResponse.getCaseDetails();
        assertNotNull(startEventResponse.getCaseDetails());
    }

    @Override
    protected Map<String, Object> setUpStateMapForProviderWithoutCaseData() {
        Map<String, Object> caseDataContentMap = super.setUpStateMapForProviderWithoutCaseData();
        caseDataContentMap.put(EVENT_ID, createEventId);
        return caseDataContentMap;
    }

    private String buildPath() {
        return new StringBuilder()
            .append("/citizens/")
            .append(USER_ID)
            .append("/jurisdictions/")
            .append(jurisdictionId)
            .append("/case-types/")
            .append(caseType)
            .append("/event-triggers/")
            .append(createEventId)
            .append("/token")
            .toString();
    }
}
