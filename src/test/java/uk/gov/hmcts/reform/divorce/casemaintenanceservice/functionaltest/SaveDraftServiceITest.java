package uk.gov.hmcts.reform.divorce.casemaintenanceservice.functionaltest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.CaseMaintenanceServiceApplication;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.DraftStoreClient;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.domain.model.UserDetails;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.model.CreateDraft;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.model.Draft;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.model.DraftList;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.model.UpdateDraft;

import java.util.Collections;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
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
public class SaveDraftServiceITest {
    private static final String API_URL = "/casemaintenance/version/1/drafts";
    private static final String IDAM_USER_DETAILS_CONTEXT_PATH = "/details";
    private static final String DRAFTS_CONTEXT_PATH = "/drafts";
    private static final String USER_ID = "1";
    private static final String ENCRYPTED_USER_ID = "OVZRS2hJRDg2MUFkeFdXdjF6bElfMQ==";
    private static final String DRAFT_DOCUMENT_TYPE = "divorcedraft";
    private static final String DRAFT_ID = "1";
    private static final Draft DRAFT = new Draft(DRAFT_ID, null, DRAFT_DOCUMENT_TYPE, true);
    private static final String DATA_TO_SAVE = "{}";

    private static final String USER_TOKEN = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJqdGkiOiIwOTg3NjU0M"
        + "yIsInN1YiI6IjEwMCIsImlhdCI6MTUwODk0MDU3MywiZXhwIjoxNTE5MzAzNDI3LCJkYXRhIjoiY2l0aXplbiIsInR5cGUiOiJBQ0NFU1MiL"
        + "CJpZCI6IjEwMCIsImZvcmVuYW1lIjoiSm9obiIsInN1cm5hbWUiOiJEb2UiLCJkZWZhdWx0LXNlcnZpY2UiOiJEaXZvcmNlIiwibG9hIjoxL"
        + "CJkZWZhdWx0LXVybCI6Imh0dHBzOi8vd3d3Lmdvdi51ayIsImdyb3VwIjoiZGl2b3JjZSJ9.lkNr1vpAP5_Gu97TQa0cRtHu8I-QESzu8kMX"
        + "CJOQrVU";

    @Value("${draft.store.api.max.age}")
    private int maxAge;

    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private MockMvc webClient;

    @ClassRule
    public static WireMockClassRule idamUserDetailsServer = new WireMockClassRule(4503);

    @ClassRule
    public static WireMockClassRule draftStoreServer = new WireMockClassRule(4601);

    @Test
    public void givenJWTTokenIsNull_whenSaveDraft_thenReturnBadRequest() throws Exception {
        webClient.perform(MockMvcRequestBuilders.put(API_URL)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void givenDataIsNull_whenSaveDraft_thenReturnBadRequest() throws Exception {
        webClient.perform(MockMvcRequestBuilders.put(API_URL)
            .header(HttpHeaders.AUTHORIZATION, USER_TOKEN)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void givenInvalidUserToken_whenSaveDraft_thenReturnForbiddenError() throws Exception {
        final String message = "some message";
        stubUserDetailsEndpoint(HttpStatus.FORBIDDEN, new EqualToPattern(USER_TOKEN), message);

        webClient.perform(MockMvcRequestBuilders.put(API_URL)
            .content(DATA_TO_SAVE)
            .header(HttpHeaders.AUTHORIZATION, USER_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden())
            .andExpect(content().string(containsString(message)));
    }

    @Test
    public void givenCouldNotConnectToAuthService_whenSaveDraft_thenReturnHttp503() throws Exception {
        final String message = getUserDetails();

        when(authTokenGenerator.generate()).thenThrow(new HttpClientErrorException(HttpStatus.SERVICE_UNAVAILABLE));

        stubUserDetailsEndpoint(HttpStatus.OK, new EqualToPattern(USER_TOKEN), message);

        webClient.perform(MockMvcRequestBuilders.put(API_URL)
            .content(DATA_TO_SAVE)
            .header(HttpHeaders.AUTHORIZATION, USER_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isServiceUnavailable());
    }

    @Test
    public void givenThereIsNoDraft_whenSaveDraft_thenReturnSaveDraft() throws Exception {
        final String message = getUserDetails();
        final String serviceToken = "serviceToken";

        final CreateDraft createDraft = new CreateDraft(Collections.emptyMap(), DRAFT_DOCUMENT_TYPE, maxAge,true);

        when(authTokenGenerator.generate()).thenReturn(serviceToken);

        stubGetDraftEndpoint(new EqualToPattern(USER_TOKEN), new EqualToPattern(serviceToken), "");

        stubUserDetailsEndpoint(HttpStatus.OK, new EqualToPattern(USER_TOKEN), message);
        stubSaveDraftEndpoint(new EqualToPattern(serviceToken), createDraft);

        webClient.perform(MockMvcRequestBuilders.put(API_URL)
            .content(DATA_TO_SAVE)
            .header(HttpHeaders.AUTHORIZATION, USER_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    public void givenThereIsAlreadyADraft_whenSaveDraft_thenReturnUpdateDraft() throws Exception {
        final String message = getUserDetails();
        final String serviceToken = "serviceToken";

        final DraftList draftList = new DraftList(Collections.singletonList(DRAFT), null);

        final UpdateDraft updateDraft = new UpdateDraft(Collections.emptyMap(), DRAFT_DOCUMENT_TYPE, true);

        when(authTokenGenerator.generate()).thenReturn(serviceToken);

        stubGetDraftEndpoint(new EqualToPattern(USER_TOKEN), new EqualToPattern(serviceToken),
            ObjectMapperTestUtil.convertObjectToJsonString(draftList));

        stubUserDetailsEndpoint(HttpStatus.OK, new EqualToPattern(USER_TOKEN), message);
        stubUpdateDraftEndpoint(new EqualToPattern(serviceToken), updateDraft);

        webClient.perform(MockMvcRequestBuilders.put(API_URL)
            .content(DATA_TO_SAVE)
            .header(HttpHeaders.AUTHORIZATION, USER_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    private void stubUserDetailsEndpoint(HttpStatus status, StringValuePattern authHeader, String message) {
        idamUserDetailsServer.stubFor(get(IDAM_USER_DETAILS_CONTEXT_PATH)
            .withHeader(HttpHeaders.AUTHORIZATION, authHeader)
            .willReturn(aResponse()
                .withStatus(status.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                .withBody(message)));
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

    private void stubUpdateDraftEndpoint(StringValuePattern serviceToken, UpdateDraft updateDraft) {
        draftStoreServer.stubFor(put(DRAFTS_CONTEXT_PATH + "/" + DRAFT_ID)
            .withRequestBody(equalToJson(ObjectMapperTestUtil.convertObjectToJsonString(updateDraft)))
            .withHeader(HttpHeaders.AUTHORIZATION, new EqualToPattern(USER_TOKEN))
            .withHeader(DraftStoreClient.SERVICE_AUTHORIZATION_HEADER_NAME, serviceToken)
            .withHeader(DraftStoreClient.SECRET_HEADER_NAME, new EqualToPattern(ENCRYPTED_USER_ID))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                .withBody("{}")));
    }

    private void stubSaveDraftEndpoint(StringValuePattern serviceToken, CreateDraft createDraft) {
        draftStoreServer.stubFor(post(DRAFTS_CONTEXT_PATH)
            .withRequestBody(equalToJson(ObjectMapperTestUtil.convertObjectToJsonString(createDraft)))
            .withHeader(HttpHeaders.AUTHORIZATION, new EqualToPattern(USER_TOKEN))
            .withHeader(DraftStoreClient.SERVICE_AUTHORIZATION_HEADER_NAME, serviceToken)
            .withHeader(DraftStoreClient.SECRET_HEADER_NAME, new EqualToPattern(ENCRYPTED_USER_ID))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                .withBody("{}")));
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
}
