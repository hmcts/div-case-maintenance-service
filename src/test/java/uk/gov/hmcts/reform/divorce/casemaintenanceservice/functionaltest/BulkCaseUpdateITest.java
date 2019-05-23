package uk.gov.hmcts.reform.divorce.casemaintenanceservice.functionaltest;

import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.CaseMaintenanceServiceApplication;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.impl.CcdSubmissionServiceImpl;

import java.util.Map;

import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = CaseMaintenanceServiceApplication.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@PropertySource(value = "classpath:application.yml")
@TestPropertySource(properties = {
    "feign.hystrix.enabled=false",
    "eureka.client.enabled=false"
    })
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class BulkCaseUpdateITest extends MockSupport {
    private static final String API_URL = "/casemaintenance/version/1/bulk/updateCase";
    private static final String EVENT_ID = "payment";
    private static final String CASE_ID = "2";
    private static final String VALID_PAYLOAD_PATH = "ccd-submission-payload/addresses.json";

    private static final String DIVORCE_CASE_SUBMISSION_EVENT_SUMMARY =
        (String)ReflectionTestUtils.getField(CcdSubmissionServiceImpl.class,
            "DIVORCE_CASE_SUBMISSION_EVENT_SUMMARY");
    private static final String DIVORCE_CASE_SUBMISSION_EVENT_DESCRIPTION =
        (String)ReflectionTestUtils.getField(CcdSubmissionServiceImpl.class,
            "DIVORCE_CASE_SUBMISSION_EVENT_DESCRIPTION");

    @Value("${ccd.jurisdictionid}")
    private String jurisdictionId;

    @Value("${ccd.bulk.casetype}")
    private String caseType;

    @Autowired
    private MockMvc webClient;

    @MockBean
    private CoreCaseDataApi coreCaseDataApi;

    @Test
    public void givenCaseDataIsNull_whenUpdateCase_thenReturnBadRequest() throws Exception {
        webClient.perform(post(getApiUrl())
            .header(HttpHeaders.AUTHORIZATION, "Some JWT Token")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void givenJWTTokenIsNull_whenUpdateCase_thenReturnBadRequest() throws Exception {
        webClient.perform(post(getApiUrl())
            .content(ResourceLoader.loadJson(VALID_PAYLOAD_PATH))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void givenInvalidUserToken_whenUpdateCase_thenReturnForbiddenError() throws Exception {
        final String message = "some message";
        stubUserDetailsEndpoint(HttpStatus.FORBIDDEN, new EqualToPattern(USER_TOKEN), message);

        webClient.perform(post(getApiUrl())
            .content(ResourceLoader.loadJson(VALID_PAYLOAD_PATH))
            .header(HttpHeaders.AUTHORIZATION, USER_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden())
            .andExpect(content().string(containsString(message)));
    }

    @Test
    public void givenCouldNotConnectToAuthService_whenUpdateCase_thenReturnHttp503() throws Exception {
        final String message = getUserDetails();
        stubUserDetailsEndpoint(HttpStatus.OK, new EqualToPattern(USER_TOKEN), message);

        when(serviceTokenGenerator.generate()).thenThrow(new HttpClientErrorException(HttpStatus.SERVICE_UNAVAILABLE));

        webClient.perform(post(getApiUrl())
            .content(ResourceLoader.loadJson(VALID_PAYLOAD_PATH))
            .header(HttpHeaders.AUTHORIZATION, USER_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isServiceUnavailable());
    }

    @Test
    public void givenAllGoesWell_whenUpdateCaseWithCaseworker_thenProceedAsExpected() throws Exception {
        final String caseData = ResourceLoader.loadJson(VALID_PAYLOAD_PATH);
        final String message = getCaseWorkerUserDetails();
        final String serviceAuthToken = "serviceAuthToken";

        final String eventId = "eventId";
        final String token = "token";
        final StartEventResponse startEventResponse = StartEventResponse.builder()
            .eventId(eventId)
            .token(token)
            .build();

        final CaseDataContent caseDataContent = CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(
                Event.builder()
                    .id(startEventResponse.getEventId())
                    .summary(DIVORCE_CASE_SUBMISSION_EVENT_SUMMARY)
                    .description(DIVORCE_CASE_SUBMISSION_EVENT_DESCRIPTION)
                    .build()
            ).data(ObjectMapperTestUtil.convertStringToObject(caseData, Map.class))
            .build();

        final CaseDetails caseDetails = CaseDetails.builder().build();

        stubUserDetailsEndpoint(HttpStatus.OK, new EqualToPattern(BEARER_CASE_WORKER_TOKEN), message);

        when(serviceTokenGenerator.generate()).thenReturn(serviceAuthToken);
        when(coreCaseDataApi
            .startEventForCaseWorker(BEARER_CASE_WORKER_TOKEN, serviceAuthToken, CASE_WORKER_USER_ID,
                jurisdictionId, caseType, CASE_ID, EVENT_ID))
            .thenReturn(startEventResponse);
        when(coreCaseDataApi
            .submitEventForCaseWorker(BEARER_CASE_WORKER_TOKEN, serviceAuthToken, CASE_WORKER_USER_ID,
                jurisdictionId, caseType, CASE_ID, true, caseDataContent))
            .thenReturn(caseDetails);

        webClient.perform(post(getApiUrl())
            .content(caseData)
            .header(HttpHeaders.AUTHORIZATION, BEARER_CASE_WORKER_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString(ObjectMapperTestUtil.convertObjectToJsonString(caseDetails))));
    }

    private String getApiUrl() {
        return API_URL + "/" + CASE_ID + "/" + EVENT_ID;
    }
}
