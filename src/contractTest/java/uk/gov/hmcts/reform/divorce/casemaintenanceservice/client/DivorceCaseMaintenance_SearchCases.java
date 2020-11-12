package uk.gov.hmcts.reform.divorce.casemaintenanceservice.client;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.notNull;

import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.domain.model.CitizenCaseState;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.apache.http.client.fluent.Executor;
import org.hamcrest.Matchers;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.jupiter.api.BeforeAll;
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
public class DivorceCaseMaintenance_SearchCases {

    public static final String SOME_AUTHORIZATION_TOKEN = "Bearer UserAuthToken";
    public static final String SOME_SERVICE_AUTHORIZATION_TOKEN = "ServiceToken";
    private static final String ACCESS_TOKEN = "someAccessToken";
    public static final String REGEX_DATE = "^((19|2[0-9])[0-9]{2})-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])$";
    private static final String TOKEN = "someToken";
    private static final String DIVORCE_CASE_SUBMISSION_EVENT_SUMMARY = "DIVORCE_CASE_SUBMISSION_EVENT_SUMMARY";
    private static final String DIVORCE_CASE_SUBMISSION_EVENT_DESCRIPTION = "DIVORCE_CASE_SUBMISSION_EVENT_DESCRIPTION";
    public static final String D8_PETITIONER_EMAIL = "D8PetitionerEmail";
    public static final String D8_REASON_FOR_DIVORCE = "D8ReasonForDivorce";
    public static final String TEST_USER_EMAIL = "test@email.com";


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
    private static final String CASE_ID = "654321";
    private static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";
    private CaseDataContent caseDataContent;
    private CaseDetails caseDetails;


    @BeforeAll
    public void setUp() throws Exception {
        caseDetails = getCaseDetails("base-case.json");
        StartEventResponse startEventResponse = StartEventResponse.builder()
            .token(TOKEN)
            .caseDetails(caseDetails)
            .eventId(createEventId)
            .build();
        caseDataContent = buildCaseDataContent(startEventResponse);
    }


    //  search Cases for Citizen
    @Pact(provider = "ccd", consumer = "divorce_caseMaintenanceService")
    public RequestResponsePact searchCasesForCitizen(PactDslWithProvider builder) throws Exception {
        // @formatter:off
        return builder
            .given("Search Cases for Citizen is requested")
            .uponReceiving("a request for a valid search case for citizen")
            .path("/searchCases")
            .query("ctid=DIVORCE")
            .method("POST")
            .headers(HttpHeaders.AUTHORIZATION,SERVICE_AUTHORIZATION)
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body("searchString")
            .willRespondWith()
            //.matchHeader(HttpHeaders.CONTENT_TYPE, "\\w+\\/[-+.\\w]+;charset=(utf|UTF)-8")
            .status(200)
            //.body(createJsonObject(getSearchResult()))
            .body(createJsonObject(getSampleJson()))
            .toPact();
//            .body(newJsonBody((o) -> {
//                    o.stringValue("total", "5")
//                   .array("caseDetails", (cd) -> {
//                       newJsonBody( cc -> {
//                           cc.stringValue("id", "100");
//                           cc.stringValue("jurisdiction", "UK");
//                           cc.stringValue("caseTypeId", "DIVORCE");
//                       });
//                    });
//            }).build())

    }

    @Test
    @PactTestFor(pactMethod = "searchCasesForCitizen")
    public void verifySearchCasesForCitizen() throws IOException, JSONException {

        SearchResult searchResult = coreCaseDataApi.searchCases(SOME_AUTHORIZATION_TOKEN,
            SOME_SERVICE_AUTHORIZATION_TOKEN, "DIVORCE", "searchString");

        assertThat(searchResult , notNull());
        assertThat(searchResult.getTotal() , Matchers.is(2));

    }

    private File getFile(String fileName) throws FileNotFoundException {
        return org.springframework.util.ResourceUtils.getFile(this.getClass().getResource("/json/" + fileName));
    }

    private CaseDataContent createCaseDataContent(String eventId, StartEventResponse startEventResponse) {
        return CaseDataContent.builder()
            .event(createEvent(eventId))
            .eventToken(startEventResponse.getToken())
            .data(createEventId)
            .build();
    }

    private Event createEvent(String eventId) {
        return Event.builder()
            .id(this.createEventId)
            .description("Divorce Application")
            .summary("Divorce Application")
            .build();
    }

    private CaseDataContent buildCaseDataContent(StartEventResponse startEventResponse) {

        // TODO EMpty for now , may need to Fill it up ?/ TBD

        final Map<String, Object> CASE_DATA_CONTENT = new HashMap<>();
        CASE_DATA_CONTENT.put("eventToken", "token");
        CASE_DATA_CONTENT.put("caseReference","case_reference");

        return CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(
                Event.builder()
                    .id(startEventResponse.getEventId())
                    .summary(DIVORCE_CASE_SUBMISSION_EVENT_SUMMARY)
                    .description(DIVORCE_CASE_SUBMISSION_EVENT_DESCRIPTION)
                    .build()
            ).data(CASE_DATA_CONTENT)
            .build();
    }


    protected JSONObject createJsonObject(Object obj) throws JSONException, IOException {
        String json = objectMapper.writeValueAsString(obj);
        return new JSONObject(json);
    }

    protected CaseDetails getCaseDetails(String fileName) throws JSONException, IOException {
        File file = getFile(fileName);
        CaseDetails caseDetails = objectMapper.readValue(file, CaseDetails.class);
        return caseDetails;
    }

    protected SearchResult getSearchResultsSample(String fileName) throws JSONException, IOException {
        File file = getFile(fileName);
        SearchResult searchResult = objectMapper.readValue(file, SearchResult.class);
        return searchResult;
    }
    protected SearchResult getSearchResult() throws IOException{
        CaseDetails caseDetail1 = createCaseDetails(1L, CitizenCaseState.ISSUED.getValue());
        CaseDetails caseDetail2 = createCaseDetails(2L, CitizenCaseState.AWAITING_DECREE_NISI.getValue());

        List<CaseDetails> caseDetailsList = new ArrayList<CaseDetails>();
        caseDetailsList.add(caseDetail1);
        caseDetailsList.add(caseDetail2);

        return  SearchResult.builder().total(0).cases(null).build();
    }



    private CaseDetails createCaseDetails(Long id, String state) {
        return CaseDetails.builder()
            .id(id)
            .state(state)
            .data(ImmutableMap.of(D8_PETITIONER_EMAIL, TEST_USER_EMAIL))
            .build();
    }

    private  SearchResult getSampleJson() throws Exception {
        File file = getFile("sample.json");
        SearchResult searchResult = objectMapper.readValue(file, SearchResult.class);
        return searchResult;
    }

    @After
    void teardown() {
        Executor.closeIdleConnections();
    }
}