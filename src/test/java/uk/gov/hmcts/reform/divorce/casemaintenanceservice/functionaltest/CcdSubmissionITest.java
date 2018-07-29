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
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.DraftStoreClient;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.domain.model.UserDetails;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.model.Draft;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.model.DraftList;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.impl.CcdSubmissionServiceImpl;

import java.util.Collections;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
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
    "eureka.client.enabled=false",
    "draft.delete.async=false"
    })
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class CcdSubmissionITest {
    private static final String API_URL = "/casemaintenance/version/1/submit";
    private static final String VALID_PAYLOAD_PATH = "ccd-submission-payload/addresses.json";
    private static final String IDAM_USER_DETAILS_CONTEXT_PATH = "/details";
    private static final String DRAFTS_CONTEXT_PATH = "/drafts";
    private static final String DRAFT_DOCUMENT_TYPE = "divorcedraft";
    private static final String USER_ID = "1";
    private static final String ENCRYPTED_USER_ID = "OVZRS2hJRDg2MUFkeFdXdjF6bElfMQ==";
    private static final String DRAFT_ID = "1";
    private static final Draft DRAFT = new Draft(DRAFT_ID, null, DRAFT_DOCUMENT_TYPE);


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

    @ClassRule
    public static WireMockClassRule draftStoreServer = new WireMockClassRule(4601);

    @Value("${ccd.jurisdictionid}")
    private String jurisdictionId;

    @Value("${ccd.casetype}")
    private String caseType;

    @Value("${ccd.eventid.create}")
    private String createEventId;

    @Autowired
    private MockMvc webClient;

    @MockBean
    private AuthTokenGenerator serviceTokenGenerator;

    @MockBean
    private CoreCaseDataApi coreCaseDataApi;

    @Test
    public void givenCaseDataIsNull_whenSubmitCase_thenReturnBadRequest() throws Exception {
        webClient.perform(post(API_URL)
            .header(HttpHeaders.AUTHORIZATION, "Some JWT Token")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void givenJWTTokenIsNull_whenSubmitCase_thenReturnBadRequest() throws Exception {
        webClient.perform(post(API_URL)
            .content(ResourceLoader.loadJson(VALID_PAYLOAD_PATH))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void givenInvalidUserToken_whenSubmitCase_thenReturnForbiddenError() throws Exception {
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
    public void givenCouldNotConnectToAuthService_whenSubmitCase_thenReturnHttp503() throws Exception {
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
    public void givenCcdThrowsFeignExceptionOnStartForCitizen_whenSubmitCase_thenReturnFeignError() throws Exception {
        final String message = getUserDetails();
        final String serviceAuthToken = "serviceAuthToken";
        final int feignStatusCode = HttpStatus.BAD_REQUEST.value();
        final String feignErrorMessage = "some error message";

        final FeignException feignException = getMockedFeignException(feignStatusCode, feignErrorMessage);

        stubUserDetailsEndpoint(HttpStatus.OK, new EqualToPattern(USER_TOKEN), message);

        when(serviceTokenGenerator.generate()).thenReturn(serviceAuthToken);
        when(coreCaseDataApi
            .startForCitizen(USER_TOKEN, serviceAuthToken, USER_ID, jurisdictionId, caseType, createEventId))
            .thenThrow(feignException);

        webClient.perform(post(API_URL)
            .content(ResourceLoader.loadJson(VALID_PAYLOAD_PATH))
            .header(HttpHeaders.AUTHORIZATION, USER_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().is(feignStatusCode))
            .andExpect(content().string(containsString(feignErrorMessage)));
    }

    @Test
    public void givenCcdThrowsFeignExceptionOnSubmitForCitizen_whenSubmitCase_thenReturnFeignError() throws Exception {
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
            .startForCitizen(USER_TOKEN, serviceAuthToken, USER_ID, jurisdictionId, caseType, createEventId))
            .thenReturn(startEventResponse);
        when(coreCaseDataApi
            .submitForCitizen(USER_TOKEN, serviceAuthToken, USER_ID, jurisdictionId, caseType,
            true, caseDataContent))
            .thenThrow(feignException);

        webClient.perform(post(API_URL)
            .content(caseData)
            .header(HttpHeaders.AUTHORIZATION, USER_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().is(feignStatusCode))
            .andExpect(content().string(containsString(feignErrorMessage)));
    }

    @Test
    public void givenAllGoesWell_whenSubmitCase_thenProceedAsExpected() throws Exception {
        final String caseData = ResourceLoader.loadJson(VALID_PAYLOAD_PATH);
        final String message = getUserDetails();
        final String serviceAuthToken = "serviceAuthToken";
        final DraftList draftList = new DraftList(Collections.singletonList(DRAFT), null);

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

        stubGetDraftEndpoint(new EqualToPattern(USER_TOKEN), new EqualToPattern(serviceAuthToken),
            ObjectMapperTestUtil.convertObjectToJsonString(draftList));

        stubDeleteDraftEndpoint(new EqualToPattern(USER_TOKEN), new EqualToPattern(serviceAuthToken));
        stubUserDetailsEndpoint(HttpStatus.OK, new EqualToPattern(USER_TOKEN), message);

        when(serviceTokenGenerator.generate()).thenReturn(serviceAuthToken);
        when(coreCaseDataApi
            .startForCitizen(USER_TOKEN, serviceAuthToken, USER_ID, jurisdictionId, caseType, createEventId))
            .thenReturn(startEventResponse);
        when(coreCaseDataApi
            .submitForCitizen(USER_TOKEN, serviceAuthToken, USER_ID, jurisdictionId, caseType,
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

    private void stubGetDraftEndpoint(StringValuePattern authHeader, StringValuePattern serviceToken, String message) {
        draftStoreServer.stubFor(get(DRAFTS_CONTEXT_PATH)
            .withHeader(HttpHeaders.AUTHORIZATION, authHeader)
            .withHeader(DraftStoreClient.SERVICE_AUTHORIZATION_HEADER_NAME, serviceToken)
            .withHeader(DraftStoreClient.SECRET_HEADER_NAME, new EqualToPattern(ENCRYPTED_USER_ID))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                .withBody(message)));
    }

    private void stubDeleteDraftEndpoint(StringValuePattern authHeader, StringValuePattern serviceToken) {
        draftStoreServer.stubFor(delete(DRAFTS_CONTEXT_PATH + "/" + DRAFT_ID)
            .withHeader(HttpHeaders.AUTHORIZATION, authHeader)
            .withHeader(DraftStoreClient.SERVICE_AUTHORIZATION_HEADER_NAME, serviceToken)
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())));
    }
}
