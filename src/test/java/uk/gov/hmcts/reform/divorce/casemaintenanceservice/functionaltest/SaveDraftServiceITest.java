package uk.gov.hmcts.reform.divorce.casemaintenanceservice.functionaltest;

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
import org.springframework.cloud.contract.wiremock.WireMockSpring;
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
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.CaseMaintenanceServiceApplication;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.client.DraftStoreClient;
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
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class SaveDraftServiceITest extends AuthIdamMockSupport {
    private static final String API_URL = "/casemaintenance/version/1/drafts";
    private static final String DRAFTS_CONTEXT_PATH = "/drafts";
    private static final String DRAFT_DOCUMENT_TYPE_CCD_FORMAT = "divorcedraftccdformat";
    private static final String DRAFT_DOCUMENT_TYPE_DIVORCE_FORMAT = "divorcedraft";
    private static final String DRAFT_ID = "1";
    private static final Draft DRAFT = new Draft(DRAFT_ID, null, DRAFT_DOCUMENT_TYPE_CCD_FORMAT);
    private static final String DATA_TO_SAVE = "{}";
    private static final String DIVORCE_FORMAT_KEY = "divorceFormat";

    @Value("${draft.store.api.max.age}")
    private int maxAge;

    @Autowired
    private MockMvc webClient;

    @ClassRule
    public static WireMockClassRule draftStoreServer = new WireMockClassRule(WireMockSpring.options().port(4601));

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

        when(serviceTokenGenerator.generate()).thenThrow(new HttpClientErrorException(HttpStatus.SERVICE_UNAVAILABLE));

        stubUserDetailsEndpoint(HttpStatus.OK, new EqualToPattern(USER_TOKEN), message);

        webClient.perform(MockMvcRequestBuilders.put(API_URL)
            .content(DATA_TO_SAVE)
            .header(HttpHeaders.AUTHORIZATION, USER_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isServiceUnavailable());
    }

    @Test
    public void givenThereIsNoDraft_whenSaveDraft_thenSaveDraft() throws Exception {
        final String message = getUserDetails();
        final String serviceToken = "serviceToken";

        final CreateDraft createDraft = new CreateDraft(Collections.emptyMap(), DRAFT_DOCUMENT_TYPE_CCD_FORMAT, maxAge);

        when(serviceTokenGenerator.generate()).thenReturn(serviceToken);

        stubGetDraftEndpoint(new EqualToPattern(USER_TOKEN), new EqualToPattern(serviceToken), "");

        stubUserDetailsEndpoint(HttpStatus.OK, new EqualToPattern(USER_TOKEN), message);
        stubSaveDraftEndpoint(new EqualToPattern(serviceToken), createDraft);

        webClient.perform(MockMvcRequestBuilders.put(API_URL)
            .content(DATA_TO_SAVE)
            .param(DIVORCE_FORMAT_KEY, "false")
            .header(HttpHeaders.AUTHORIZATION, USER_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    public void givenThereIsNoDraftAndDivorceFormatTrue_whenSaveDraft_thenSaveDraft() throws Exception {
        final String message = getUserDetails();
        final String serviceToken = "serviceToken";

        final CreateDraft createDraft = new CreateDraft(Collections.emptyMap(),
            DRAFT_DOCUMENT_TYPE_DIVORCE_FORMAT, maxAge);

        when(serviceTokenGenerator.generate()).thenReturn(serviceToken);

        stubGetDraftEndpoint(new EqualToPattern(USER_TOKEN), new EqualToPattern(serviceToken), "");

        stubUserDetailsEndpoint(HttpStatus.OK, new EqualToPattern(USER_TOKEN), message);
        stubSaveDraftEndpoint(new EqualToPattern(serviceToken), createDraft);

        webClient.perform(MockMvcRequestBuilders.put(API_URL)
            .content(DATA_TO_SAVE)
            .param(DIVORCE_FORMAT_KEY, "true")
            .header(HttpHeaders.AUTHORIZATION, USER_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    public void givenThereIsAlreadyADraft_whenSaveDraft_thenUpdateDraft() throws Exception {
        final String message = getUserDetails();
        final String serviceToken = "serviceToken";

        final DraftList draftList = new DraftList(Collections.singletonList(DRAFT), null);

        final UpdateDraft updateDraft = new UpdateDraft(Collections.emptyMap(), DRAFT_DOCUMENT_TYPE_CCD_FORMAT);

        when(serviceTokenGenerator.generate()).thenReturn(serviceToken);

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
}
