package uk.gov.hmcts.reform.divorce.casemaintenanceservice.functionaltest;

import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import feign.FeignException;
import org.junit.Ignore;
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
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.TestConstants.TEST_AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.TestConstants.TEST_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.TestConstants.TEST_SERVICE_TOKEN;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.TestConstants.TEST_TOKEN;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = CaseMaintenanceServiceApplication.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@PropertySource(value = "classpath:application.yml")
@TestPropertySource(properties = {
    "feign.hystrix.enabled=false",
    "eureka.client.enabled=false",
    "draft.delete.async=false"
    })
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Ignore
public class CcdSolicitorSubmissionITest extends MockSupport {
    private static final String API_URL = "/casemaintenance/version/1/solicitor-submit";
    private static final String VALID_PAYLOAD_PATH = "ccd-submission-payload/base-amended-case.json";

    private static final String DIVORCE_CASE_SUBMISSION_EVENT_SUMMARY =
        (String)ReflectionTestUtils.getField(CcdSubmissionServiceImpl.class,
            "DIVORCE_CASE_SUBMISSION_EVENT_SUMMARY");
    private static final String DIVORCE_CASE_SUBMISSION_EVENT_DESCRIPTION =
        (String)ReflectionTestUtils.getField(CcdSubmissionServiceImpl.class,
            "DIVORCE_CASE_SUBMISSION_EVENT_DESCRIPTION");

    @Value("${ccd.jurisdictionid}")
    private String jurisdictionId;

    @Value("${ccd.casetype}")
    private String caseType;

    @Value("${ccd.eventid.solicitorCreate}")
    private String solicitorCreateEventId;

    @Autowired
    private MockMvc webClient;


    @MockBean(name = "uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi")
    private CoreCaseDataApi coreCaseDataApi;

    @Test
    public void givenCaseDataIsNull_whenSolicitorSubmitCase_thenReturnBadRequest() throws Exception {
        webClient.perform(post(API_URL)
            .header(HttpHeaders.AUTHORIZATION, TEST_AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void givenJWTTokenIsNull_whenSolicitorSubmitCase_thenReturnBadRequest() throws Exception {
        webClient.perform(post(API_URL)
            .content(ResourceLoader.loadJson(VALID_PAYLOAD_PATH))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void givenInvalidUserToken_whenSolicitorSubmitCase_thenReturnForbiddenError() throws Exception {
        final String message = "some message";
        stubUserDetailsEndpoint(HttpStatus.FORBIDDEN, new EqualToPattern(USER_TOKEN), message);

        webClient.perform(post(API_URL)
            .content(ResourceLoader.loadJson(VALID_PAYLOAD_PATH))
            .header(HttpHeaders.AUTHORIZATION, USER_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden())
            .andExpect(content().string(containsString(message)));
    }

    @Test
    public void givenCouldNotConnectToAuthService_whenSolicitorSubmitCase_thenReturnHttp503() throws Exception {
        final String message = getUserDetails();
        stubUserDetailsEndpoint(HttpStatus.OK, new EqualToPattern(USER_TOKEN), message);

        when(serviceTokenGenerator.generate()).thenThrow(new HttpClientErrorException(HttpStatus.SERVICE_UNAVAILABLE));

        webClient.perform(post(API_URL)
            .content(ResourceLoader.loadJson(VALID_PAYLOAD_PATH))
            .header(HttpHeaders.AUTHORIZATION, USER_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isServiceUnavailable());
    }

    @Test
    public void givenCcdThrowsFeignExceptionOnStartForCaseworker_whenSolicitorSubmitCase_thenReturnFeignError() throws Exception {
        final String message = getUserDetails();
        final int feignStatusCode = HttpStatus.BAD_REQUEST.value();

        final FeignException feignException = getMockedFeignException(feignStatusCode);

        stubUserDetailsEndpoint(HttpStatus.OK, new EqualToPattern(USER_TOKEN), message);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);
        when(coreCaseDataApi
            .startForCaseworker(USER_TOKEN, TEST_SERVICE_TOKEN, USER_ID, jurisdictionId, caseType, solicitorCreateEventId))
            .thenThrow(feignException);

        webClient.perform(post(API_URL)
            .content(ResourceLoader.loadJson(VALID_PAYLOAD_PATH))
            .header(HttpHeaders.AUTHORIZATION, USER_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().is(feignStatusCode))
            .andExpect(content().string(containsString(FEIGN_ERROR)));
    }

    @Test
    public void givenCcdThrowsFeignExceptionOnSubmitForCaseworker_whenSolicitorSubmitCase_thenReturnFeignError() throws Exception {
        final String caseData = ResourceLoader.loadJson(VALID_PAYLOAD_PATH);
        final String message = getUserDetails();
        final int feignStatusCode = HttpStatus.BAD_REQUEST.value();

        final FeignException feignException = getMockedFeignException(feignStatusCode);

        final StartEventResponse startEventResponse = StartEventResponse.builder()
            .eventId(TEST_EVENT_ID)
            .token(TEST_TOKEN)
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

        stubUserDetailsEndpoint(HttpStatus.OK, new EqualToPattern(USER_TOKEN), message);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);
        when(coreCaseDataApi
            .startForCaseworker(USER_TOKEN, TEST_SERVICE_TOKEN, USER_ID, jurisdictionId, caseType, solicitorCreateEventId))
            .thenReturn(startEventResponse);
        when(coreCaseDataApi
            .submitForCaseworker(USER_TOKEN, TEST_SERVICE_TOKEN, USER_ID, jurisdictionId, caseType,
            true, caseDataContent))
            .thenThrow(feignException);

        webClient.perform(post(API_URL)
            .content(caseData)
            .header(HttpHeaders.AUTHORIZATION, USER_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().is(feignStatusCode))
            .andExpect(content().string(containsString(FEIGN_ERROR)));
    }

    @Test
    public void givenAllGoesWell_whenSolicitorSubmitCase_thenProceedAsExpected() throws Exception {
        final String caseData = ResourceLoader.loadJson(VALID_PAYLOAD_PATH);
        final String message = getUserDetails();

        final StartEventResponse startEventResponse = StartEventResponse.builder()
            .eventId(solicitorCreateEventId)
            .token(USER_TOKEN)
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

        stubUserDetailsEndpoint(HttpStatus.OK, new EqualToPattern(USER_TOKEN), message);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);
        when(coreCaseDataApi
            .startForCaseworker(USER_TOKEN, TEST_SERVICE_TOKEN, USER_ID, jurisdictionId, caseType, solicitorCreateEventId))
            .thenReturn(startEventResponse);
        when(coreCaseDataApi
            .submitForCaseworker(USER_TOKEN, TEST_SERVICE_TOKEN, USER_ID, jurisdictionId, caseType,
                true, caseDataContent))
            .thenReturn(caseDetails);

        webClient.perform(post(API_URL)
            .content(caseData)
            .header(HttpHeaders.AUTHORIZATION, USER_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString(ObjectMapperTestUtil.convertObjectToJsonString(caseDetails))));
    }
}
