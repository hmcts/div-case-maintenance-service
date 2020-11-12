package uk.gov.hmcts.reform.divorce.casemaintenanceservice.client;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.client.util.ObjectMapperTestUtil;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.client.util.ResourceLoader;

import java.io.IOException;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.client.util.PactDslCaseData.buildCaseDetails;

public class DivorceCaseMaintenance_SubmitForCaseWorker {

    public static final String SOME_AUTHORIZATION_TOKEN = "Bearer UserAuthToken";
    public static final String SOME_SERVICE_AUTHORIZATION_TOKEN = "ServiceToken";
    private static final String VALID_PAYLOAD_PATH = "json/base-case.json";



    @Autowired
    private CoreCaseDataApi coreCaseDataApi;

    @Autowired
    ObjectMapper objectMapper;

    CaseDataContent caseDataContent;

    @Value("${ccd.jurisdictionid}")
    String jurisdictionId;

    @Value("${ccd.casetype}")
    String caseType;

    @Value("${ccd.bulk.eventid.create}")
    private String createEventId;

    private static final String USER_ID ="123456";
    private static final Long CASE_ID = 2000l;
    private static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";

    @Pact(provider = "ccd", consumer = "divorce_caseMaintenanceService")
    RequestResponsePact submitCaseWorkerDetails(PactDslWithProvider builder) {
        // @formatter:off
        return builder
            .given("A Caseworker details submission is posted")
            .uponReceiving("A Caseworker details submission is posted")
            .path("/caseworkers/"
                + USER_ID +
                "/jurisdictions/" + jurisdictionId
                + "/case-types/"
                + caseType
                + "/cases/")
            .method("POST")
            .headers(HttpHeaders.AUTHORIZATION, SOME_AUTHORIZATION_TOKEN, SERVICE_AUTHORIZATION,
                SOME_SERVICE_AUTHORIZATION_TOKEN)
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .willRespondWith()
            .matchHeader(HttpHeaders.CONTENT_TYPE, "\\w+\\/[-+.\\w]+;charset=(utf|UTF)-8")
            .status(200)
            .body(buildCaseDetails(CASE_ID, "emailAddress@email.com",false, false))
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "submitCaseWorkerDetails")
    public void submitForCaseWorker() throws IOException, JSONException {


        CaseDetails caseDetailsReponse = coreCaseDataApi.submitForCaseworker(SOME_AUTHORIZATION_TOKEN,
            SOME_SERVICE_AUTHORIZATION_TOKEN, USER_ID, jurisdictionId,
            caseType,false,caseDataContent);
        Map<String,Object> dataMap = caseDetailsReponse.getData() ;
        assertThat(dataMap.get("outsideUKGrantCopies"), is(6));
        assertThat(dataMap.get("primaryApplicantForenames"), is("Jon"));
    }


    private CaseDataContent getCaseDataContent() throws Exception {

        final String caseData = ResourceLoader.loadJson(VALID_PAYLOAD_PATH);

        final StartEventResponse startEventResponse = StartEventResponse.builder()
            .eventId(createEventId)
            .token(SOME_AUTHORIZATION_TOKEN)
            .build();

        final CaseDataContent caseDataContent = CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(
                Event.builder()
                    .id(startEventResponse.getEventId())
                    .summary("divSummary")
                    .description("div")
                    .build()
            ).data(ObjectMapperTestUtil.convertObjectToJsonString(caseData))
            .build();

        System.out.println("xyz"+caseDataContent.getData());
        return caseDataContent;
    }
}
