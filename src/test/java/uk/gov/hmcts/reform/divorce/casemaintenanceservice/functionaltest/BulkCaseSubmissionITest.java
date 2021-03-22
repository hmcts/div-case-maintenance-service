package uk.gov.hmcts.reform.divorce.casemaintenanceservice.functionaltest;

import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import feign.FeignException;
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
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.TestConstants.TEST_SERVICE_TOKEN;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = CaseMaintenanceServiceApplication.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@PropertySource(value = "classpath:application.yml")
@TestPropertySource(properties = {
    "feign.hystrix.enabled=false",
    "eureka.client.enabled=false"
    })
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)

public class BulkCaseSubmissionITest extends MockSupport {

    private static final String API_URL = "/casemaintenance/version/1/bulk/submit";
    private static final String VALID_PAYLOAD_PATH = "ccd-submission-payload/base-case.json";
    private static final String NO_HELP_WITH_FEES_PATH = "ccd-submission-payload/addresses-no-hwf.json";

    private static final String DIVORCE_BULK_CASE_SUBMISSION_EVENT_SUMMARY =
        (String)ReflectionTestUtils.getField(CcdSubmissionServiceImpl.class,
            "DIVORCE_BULK_CASE_SUBMISSION_EVENT_SUMMARY");
    private static final String DIVORCE_BULK_CASE_SUBMISSION_EVENT_DESCRIPTION =
        (String)ReflectionTestUtils.getField(CcdSubmissionServiceImpl.class,
            "DIVORCE_BULK_CASE_SUBMISSION_EVENT_DESCRIPTION");

    @Value("${ccd.jurisdictionid}")
    private String jurisdictionId;

    @Value("${ccd.bulk.casetype}")
    private String caseType;

    @Value("${ccd.bulk.eventid.create}")
    private String createEventId;

    @Autowired
    private MockMvc webClient;

    @MockBean(name = "uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi")
    private CoreCaseDataApi coreCaseDataApi;

    @Test
    public void givenCaseDataIsNull_whenSubmitCase_thenReturnBadRequest() throws Exception {
        webClient.perform(post(API_URL)
            .header(HttpHeaders.AUTHORIZATION, TEST_AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void givenAllGoesWell_whenSubmitBulkCase_thenProceedAsExpected() throws Exception {
        final String caseData = ResourceLoader.loadJson(NO_HELP_WITH_FEES_PATH);
        final String message = getUserDetails();

        final StartEventResponse startEventResponse = StartEventResponse.builder()
            .eventId(createEventId)
            .token(USER_TOKEN)
            .build();

        final CaseDataContent caseDataContent = CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(
                Event.builder()
                    .id(startEventResponse.getEventId())
                    .summary(DIVORCE_BULK_CASE_SUBMISSION_EVENT_SUMMARY)
                    .description(DIVORCE_BULK_CASE_SUBMISSION_EVENT_DESCRIPTION)
                    .build()
            ).data(ObjectMapperTestUtil.convertStringToObject(caseData, Map.class))
            .build();

        final CaseDetails caseDetails = CaseDetails.builder().build();

        stubUserDetailsEndpoint(HttpStatus.OK, new EqualToPattern(USER_TOKEN), message);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);
        when(coreCaseDataApi
            .startForCaseworker(USER_TOKEN, TEST_SERVICE_TOKEN, USER_ID, jurisdictionId, caseType, createEventId))
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

    @Test
    public void givenCcdThrowsFeignExceptionOnStartForCitizen_whenSubmitCase_thenReturnFeignError() throws Exception {
        final String message = getUserDetails();
        final int feignStatusCode = HttpStatus.BAD_REQUEST.value();

        final FeignException feignException = getMockedFeignException(feignStatusCode);

        stubUserDetailsEndpoint(HttpStatus.OK, new EqualToPattern(USER_TOKEN), message);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);
        when(coreCaseDataApi
            .startForCaseworker(USER_TOKEN, TEST_SERVICE_TOKEN, USER_ID, jurisdictionId, caseType, createEventId))
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
    public void givenCcdThrowsFeignExceptionOnSubmitForCitizen_whenSubmitCase_thenReturnFeignError() throws Exception {
        final String caseData = ResourceLoader.loadJson(VALID_PAYLOAD_PATH);
        final String message = getUserDetails();
        final int feignStatusCode = HttpStatus.BAD_REQUEST.value();

        final FeignException feignException = getMockedFeignException(feignStatusCode);

        final StartEventResponse startEventResponse = StartEventResponse.builder()
            .eventId(createEventId)
            .token(USER_TOKEN)
            .build();

        final CaseDataContent caseDataContent = CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(
                Event.builder()
                    .id(startEventResponse.getEventId())
                    .summary(DIVORCE_BULK_CASE_SUBMISSION_EVENT_SUMMARY)
                    .description(DIVORCE_BULK_CASE_SUBMISSION_EVENT_DESCRIPTION)
                    .build()
            ).data(ObjectMapperTestUtil.convertStringToObject(caseData, Map.class))
            .build();

        stubUserDetailsEndpoint(HttpStatus.OK, new EqualToPattern(USER_TOKEN), message);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);
        when(coreCaseDataApi
            .startForCaseworker(USER_TOKEN, TEST_SERVICE_TOKEN, USER_ID, jurisdictionId, caseType, createEventId))
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
}
