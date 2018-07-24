package uk.gov.hmcts.reform.divorce.casemaintenanceservice.functionaltest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import feign.FeignException;
import org.junit.ClassRule;
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
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.CaseMaintenanceServiceApplication;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.domain.model.UserDetails;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.impl.CcdSubmissionServiceImpl;

import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
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
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class CcdUpdateITest {
    private static final String API_URL = "/casemaintenance/version/1/updateCase";
    private static final String EVENT_ID = "payment";
    private static final String CASE_ID = "2";
    private static final String VALID_PAYLOAD_PATH = "ccd-submission-payload/addresses.json";
    private static final String IDAM_USER_DETAILS_CONTEXT_PATH = "/details";
    private static final String USER_ID = "1";

    private static final String DIVORCE_CASE_SUBMISSION_EVENT_SUMMARY =
        (String)ReflectionTestUtils.getField(CcdSubmissionServiceImpl.class,
            "DIVORCE_CASE_SUBMISSION_EVENT_SUMMARY");
    private static final String DIVORCE_CASE_SUBMISSION_EVENT_DESCRIPTION =
        (String)ReflectionTestUtils.getField(CcdSubmissionServiceImpl.class,
            "DIVORCE_CASE_SUBMISSION_EVENT_DESCRIPTION");

    private static final String USER_TOKEN = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJqdGkiOiIwOTg3NjU0M"
        + "yIsInN1YiI6IjEwMCIsImlhdCI6MTUwODk0MDU3MywiZXhwIjoxNTE5MzAzNDI3LCJkYXRhIjoiY2l0aXplbiIsInR5cGUiOiJBQ0NFU1MiL"
        + "CJpZCI6IjEwMCIsImZvcmVuYW1lIjoiSm9obiIsInN1cm5hbWUiOiJEb2UiLCJkZWZhdWx0LXNlcnZpY2UiOiJEaXZvcmNlIiwibG9hIjoxL"
        + "CJkZWZhdWx0LXVybCI6Imh0dHBzOi8vd3d3Lmdvdi51ayIsImdyb3VwIjoiZGl2b3JjZSJ9.lkNr1vpAP5_Gu97TQa0cRtHu8I-QESzu8kMX"
        + "CJOQrVU";

    @ClassRule
    public static WireMockClassRule idamUserDetailsServer = new WireMockClassRule(4503);

    @Value("${ccd.jurisdictionid}")
    private String jurisdictionId;

    @Value("${ccd.casetype}")
    private String caseType;

    @Autowired
    private MockMvc webClient;

    @MockBean
    private AuthTokenGenerator serviceTokenGenerator;

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
    public void givenCcdThrowsFeignExceptionOnStartEventForCitizen_whenUpdateCase_thenReturnFeignError()
        throws Exception {

        final String message = getUserDetails();
        final String serviceAuthToken = "serviceAuthToken";
        final int feignStatusCode = HttpStatus.BAD_REQUEST.value();
        final String feignErrorMessage = "some error message";

        final FeignException feignException = getMockedFeignException(feignStatusCode, feignErrorMessage);

        stubUserDetailsEndpoint(HttpStatus.OK, new EqualToPattern(USER_TOKEN), message);

        when(serviceTokenGenerator.generate()).thenReturn(serviceAuthToken);
        when(coreCaseDataApi
            .startEventForCitizen(USER_TOKEN, serviceAuthToken, USER_ID, jurisdictionId, caseType, CASE_ID, EVENT_ID))
            .thenThrow(feignException);

        webClient.perform(post(getApiUrl())
            .content(ResourceLoader.loadJson(VALID_PAYLOAD_PATH))
            .header(HttpHeaders.AUTHORIZATION, USER_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().is(feignStatusCode))
            .andExpect(content().string(containsString(feignErrorMessage)));
    }

    @Test
    public void givenCcdThrowsFeignExceptionOnSubmitForCitizen_whenUpdateCase_thenReturnFeignError() throws Exception {
        final String caseData = ResourceLoader.loadJson(VALID_PAYLOAD_PATH);
        final String message = getUserDetails();
        final String serviceAuthToken = "serviceAuthToken";
        final int feignStatusCode = HttpStatus.BAD_REQUEST.value();
        final String feignErrorMessage = "some error message";

        final FeignException feignException = getMockedFeignException(feignStatusCode, feignErrorMessage);

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

        stubUserDetailsEndpoint(HttpStatus.OK, new EqualToPattern(USER_TOKEN), message);

        when(serviceTokenGenerator.generate()).thenReturn(serviceAuthToken);
        when(coreCaseDataApi
            .startEventForCitizen(USER_TOKEN, serviceAuthToken, USER_ID, jurisdictionId, caseType, CASE_ID, EVENT_ID))
            .thenReturn(startEventResponse);
        when(coreCaseDataApi
            .submitEventForCitizen(USER_TOKEN, serviceAuthToken, USER_ID, jurisdictionId, caseType, CASE_ID,
                true, caseDataContent))
            .thenThrow(feignException);

        webClient.perform(post(getApiUrl())
            .content(caseData)
            .header(HttpHeaders.AUTHORIZATION, USER_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().is(feignStatusCode))
            .andExpect(content().string(containsString(feignErrorMessage)));
    }

    @Test
    public void givenAllGoesWell_whenUpdateCase_thenProceedAsExpected() throws Exception {
        final String caseData = ResourceLoader.loadJson(VALID_PAYLOAD_PATH);
        final String message = getUserDetails();
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

        stubUserDetailsEndpoint(HttpStatus.OK, new EqualToPattern(USER_TOKEN), message);

        when(serviceTokenGenerator.generate()).thenReturn(serviceAuthToken);
        when(coreCaseDataApi
            .startEventForCitizen(USER_TOKEN, serviceAuthToken, USER_ID, jurisdictionId, caseType, CASE_ID, EVENT_ID))
            .thenReturn(startEventResponse);
        when(coreCaseDataApi
            .submitEventForCitizen(USER_TOKEN, serviceAuthToken, USER_ID, jurisdictionId, caseType, CASE_ID,
                true, caseDataContent))
            .thenReturn(caseDetails);

        webClient.perform(post(getApiUrl())
            .content(caseData)
            .header(HttpHeaders.AUTHORIZATION, USER_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString(ObjectMapperTestUtil.convertObjectToJsonString(caseDetails))));
    }

    private String getApiUrl() {
        return API_URL + "/" + CASE_ID + "/" + EVENT_ID;
    }

    private void stubUserDetailsEndpoint(HttpStatus status, StringValuePattern authHeader, String message) {
        idamUserDetailsServer.stubFor(get(IDAM_USER_DETAILS_CONTEXT_PATH)
            .withHeader(HttpHeaders.AUTHORIZATION, authHeader)
            .willReturn(aResponse()
                .withStatus(status.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                .withBody(message)));
    }

    private String getUserDetails() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(
            UserDetails.builder()
                .id(USER_ID)
                .email("test@test.com")
                .forename("forename")
                .surname("surname")
                .build());
    }

    private FeignException getMockedFeignException(int statusCode, String errorMessage) {
        final FeignException feignException = mock(FeignException.class);

        when(feignException.status()).thenReturn(statusCode);
        when(feignException.getMessage()).thenReturn(errorMessage);

        return feignException;
    }
}
