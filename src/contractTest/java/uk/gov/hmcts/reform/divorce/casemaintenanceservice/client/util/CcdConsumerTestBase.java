package uk.gov.hmcts.reform.divorce.casemaintenanceservice.client.util;

import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.PactSpecVersion;
import au.com.dius.pact.core.model.annotations.PactFolder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.fluent.Executor;
import org.json.JSONException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@ExtendWith(PactConsumerTestExt.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@PactTestFor(providerName = "ccdDataStoreAPI_Cases", port = "8891", pactVersion = PactSpecVersion.V3)
@PactFolder("pacts")
@SpringBootTest({
    "core_case_data.api.url : localhost:8891"
})
public abstract class CcdConsumerTestBase {

    public static final String JURISDICTION = "jurisdictionId";
    public static final String CASE_TYPE = "caseType";
    public static final String CASEWORKER_USERNAME = "caseworkerUsername";
    public static final String CASEWORKER_PASSWORD = "caseworkerPassword";
    public static final String CASE_DATA_CONTENT = "caseDataContent";
    public static final String EVENT_ID = "eventId";
    public static final String HWF_APPLICATION_ACCEPTED = "hwfApplicationAccepted";
    public static final int SLEEP_TIME = 2000;

    @Autowired
    protected CoreCaseDataApi coreCaseDataApi;

    @Value("${ccd.jurisdictionid}")
    protected String jurisdictionId;

    @Value("${ccd.casetype}")
    protected String caseType;

    @Value("${ccd.eventid.create}")
    protected String createEventId;

    @Value("${idam.caseworker.username}")
    protected String caseworkerUsername;

    @Value("${idam.caseworker.password}")
    protected String caseworkerPwd;

    protected Map<String, Object> caseDetailsMap;
    protected CaseDataContent caseDataContent;

    @Autowired
    private ObjectMapper objectMapper;

    protected static final String USER_ID = "123456";
    protected static final Long CASE_ID = 1593694526480034L;
    protected static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";

    protected static final String SOME_AUTHORIZATION_TOKEN = "Bearer UserAuthToken";
    protected static final String SOME_SERVICE_AUTHORIZATION_TOKEN = "ServiceToken";

    protected static final String DIVORCE_CASE_SUBMISSION_EVENT_SUMMARY = "Divorce case submission event";
    protected static final String DIVORCE_CASE_SUBMISSION_EVENT_DESCRIPTION = "Submitting Divorce Case";


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
    public void prepareTest() throws Exception {
        Thread.sleep(SLEEP_TIME);
    }

    @AfterEach
    void teardown() {
        Executor.closeIdleConnections();
    }

    protected Map<String, Object> getCaseDetailsAsMap(String fileName) throws JSONException, IOException {
        File file = getFile(fileName);
        CaseDetails caseDetails = objectMapper.readValue(file, CaseDetails.class);
        Map<String, Object> map = objectMapper.convertValue(caseDetails, Map.class);
        return map;
    }

    protected Map<String, Object> setUpStateMapForProviderWithCaseData(CaseDataContent caseDataContent) throws JSONException {
        Map<String, Object> map = this.setUpStateMapForProviderWithoutCaseData();
        Map<String, Object> caseDataContentMap = objectMapper.convertValue(caseDataContent, Map.class);
        map.put(CASE_DATA_CONTENT, caseDataContentMap);
        return map;
    }

    protected Map<String, Object> setUpStateMapForProviderWithoutCaseData() {
        Map<String, Object> map = new HashMap<>();
        map.put(JURISDICTION, jurisdictionId);
        map.put(CASE_TYPE, caseType);
        map.put(CASEWORKER_USERNAME, caseworkerUsername);
        map.put(CASEWORKER_PASSWORD, caseworkerPwd);
        return map;
    }

    private File getFile(String fileName) throws FileNotFoundException {
        return org.springframework.util.ResourceUtils.getFile(this.getClass().getResource("/json/" + fileName));
    }

}
