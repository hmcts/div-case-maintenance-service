package uk.gov.hmcts.reform.divorce.casemaintenanceservice.client;

import static io.pactfoundation.consumer.dsl.LambdaDsl.newJsonBody;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.client.util.PactDslBuilderForCaseDetailsList.buildCaseDetailList;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.client.util.PactDslBuilderForCaseDetailsList.buildCaseDetails;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.client.util.PactDslBuilderForCaseDetailsList.buildCaseDetails;

import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.client.util.PactDslBuilderForCaseDetailsList;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslJsonArray;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.errorprone.annotations.Immutable;
import io.pactfoundation.consumer.dsl.LambdaDslJsonBody;
import org.json.JSONException;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(PactConsumerTestExt.class)
@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@PactTestFor(providerName = "ccd", port = "8891")
@SpringBootTest({
    "core_case_data.api.url : localhost:8891"
})
public class DivorceCaseMaintenance_searchForCitizen {

    public static final String SOME_AUTHORIZATION_TOKEN = "Bearer UserAuthToken";
    public static final String SOME_SERVICE_AUTHORIZATION_TOKEN = "ServiceToken";


    @Autowired
    private CoreCaseDataApi coreCaseDataApi;

    @Autowired
    ObjectMapper objectMapper;

    @Value("${ccd.jurisdictionid}")
    String jurisdictionId;

    @Value("${ccd.casetype}")
    String caseType;

    @Value("${ccd.eventid.create}")
    String createEventId;

    private static final String USER_ID ="123456";
    private static final String CASE_ID = "2000";
    private static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";
    private CaseDataContent caseDataContent;
    private CaseDetails caseDetails;
    private static final String  ALPHABETIC_REGEX = "[/^[A-Za-z]+$/]+";
    Map<String, Object> params = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    @Before
    public void setUp() throws Exception {

    }

    @Pact(provider = "ccd", consumer = "divorce_caseMaintenanceService")
    RequestResponsePact searchForCitizen(PactDslWithProvider builder) {
        params  = Collections.emptyMap();
        // @formatter:off
        return builder
            .given("A Search For Citizen requested",params)
            .uponReceiving("A request for search For Citizen")
            .path("/citizens/"
                + USER_ID +
                "/jurisdictions/" + jurisdictionId
                + "/case-types/"
                + caseType
                + "/cases")
            .method("GET")
            .query("")
            .headers(HttpHeaders.AUTHORIZATION, SOME_AUTHORIZATION_TOKEN, SERVICE_AUTHORIZATION,
                SOME_SERVICE_AUTHORIZATION_TOKEN)
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .willRespondWith()
            .matchHeader(HttpHeaders.CONTENT_TYPE, "\\w+\\/[-+.\\w]+;charset=(utf|UTF)-8")
            .status(200)
            .body( buildCaseDetails(Long.valueOf(CASE_ID),"somemailaddress@gmail.com",false,false))
            //.body( new PactDslJsonArray())
            .toPact();

    }

    @Test
    @PactTestFor(pactMethod = "searchForCitizen")
    public void searchForCitizen() throws IOException, JSONException {

        final Map<String,String> searchCriteria = Collections.EMPTY_MAP;

        List<CaseDetails> caseDetailsList = coreCaseDataApi.searchForCitizen(SOME_AUTHORIZATION_TOKEN,
            SOME_SERVICE_AUTHORIZATION_TOKEN, USER_ID, jurisdictionId,
            caseType,searchCriteria);

        assertThat(caseDetailsList.get(0).getId(), equalTo(100L));
        assertThat(caseDetailsList.get(0).getCaseTypeId(), equalTo("AwaitingDecreeNisi"));
        assertThat(caseDetailsList.get(0).getState(), equalTo("CaseCreated"));

        Map<String,Object>  dataMap = caseDetailsList.get(0).getData() ;
        assertThat(dataMap.get("outsideUKGrantCopies"), is(6));
        assertThat(dataMap.get("primaryApplicantForenames"), is("Jon"));

    }
}
