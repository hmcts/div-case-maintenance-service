package uk.gov.hmcts.reform.divorce.casemaintenanceservice.client;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.client.util.PactDslBuilderForCaseDetailsList.buildListOfCaseDetailsDsl;

import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.CoreMatchers;
import org.json.JSONException;
import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.client.util.DivorceCaseMaintenancePact;


public class DivorceCaseMaintenanceSearchForCitizen extends DivorceCaseMaintenancePact {
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

    private static final String USER_ID = "123456";
    private static final String CASE_ID = "2000";
    private static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";
    private CaseDataContent caseDataContent;
    private CaseDetails caseDetails;
    private static final String ALPHABETIC_REGEX = "[/^[A-Za-z]+$/]+";
    Map<String, Object> params = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    @Before
    public void setUp() throws Exception {

    }

    @BeforeEach
    public void setUpEachTest() throws InterruptedException {
        Thread.sleep(2000);
    }

    @Pact(provider = "ccd", consumer = "divorce_caseMaintenanceService_citizen")
    RequestResponsePact searchForCitizen(PactDslWithProvider builder) {
        params = Collections.emptyMap();

        // @formatter:off
        return builder
            .given("A Search For Citizen requested", params)
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
            .body(buildListOfCaseDetailsDsl(Long.valueOf(CASE_ID), "somemailaddress@gmail.com", false, false))
            .toPact();

    }

    @Test
    @PactTestFor(pactMethod = "searchForCitizen")
    public void searchForCitizen() throws IOException, JSONException {

        final Map<String, String> searchCriteria = Collections.EMPTY_MAP;

        List<CaseDetails> caseDetailsList = coreCaseDataApi.searchForCitizen(SOME_AUTHORIZATION_TOKEN,
            SOME_SERVICE_AUTHORIZATION_TOKEN, USER_ID, jurisdictionId,
            caseType, searchCriteria);

        assertThat(caseDetailsList.size(), is(2));

        Map<String, Object> data1 = caseDetailsList.get(0).getData();
        Map<String, Object> data2 = caseDetailsList.get(1).getData();

        assertThat(data1.get("applicationType"), CoreMatchers.equalTo("Personal"));
        assertThat(data1.get("primaryApplicantForenames"), CoreMatchers.equalTo("Jon"));

        assertThat(data2.get("applicationType"), CoreMatchers.equalTo("Personal"));
        assertThat(data2.get("primaryApplicantForenames"), CoreMatchers.equalTo("Jon"));
    }
}
