package uk.gov.hmcts.reform.divorce.casemaintenanceservice.functionaltest;

import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import joptsimple.internal.Strings;
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
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.CaseMaintenanceServiceApplication;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.client.DraftStoreClient;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CaseState;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.model.Draft;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.model.DraftList;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
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
public class RetrieveAosCaseITest extends AuthIdamMockSupport {
    private static final String API_URL = "/casemaintenance/version/1/retrieveAosCase";
    private static final String CHECK_CCD_PARAM = "checkCcd";
    private static final String DRAFTS_CONTEXT_PATH = "/drafts";
    private static final String TRANSFORM_TO_DIVORCE_CONTEXT_PATH = "/caseformatter/version/1/to-divorce-format";
    private static final String DRAFT_DOCUMENT_TYPE_CCD_FORMAT = "divorcedraftccdformat";
    private static final String DRAFT_DOCUMENT_TYPE_DIVORCE_FORMAT = "divorcedraft";

    @ClassRule
    public static WireMockClassRule draftStoreServer = new WireMockClassRule(4601);

    @ClassRule
    public static WireMockClassRule caseFormatterServer = new WireMockClassRule(4011);

    @Value("${ccd.jurisdictionid}")
    private String jurisdictionId;

    @Value("${ccd.casetype}")
    private String caseType;

    @MockBean
    private CoreCaseDataApi coreCaseDataApi;

    @Autowired
    private MockMvc webClient;

    @Test
    public void givenJWTTokenIsNull_whenRetrieveAosCase_thenReturnBadRequest() throws Exception {
        webClient.perform(MockMvcRequestBuilders.get(API_URL)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void givenInvalidUserToken_whenRetrieveAosCase_thenReturnForbiddenError() throws Exception {
        final String message = "some message";
        stubUserDetailsEndpoint(HttpStatus.FORBIDDEN, new EqualToPattern(USER_TOKEN), message);

        webClient.perform(MockMvcRequestBuilders.get(API_URL)
            .header(HttpHeaders.AUTHORIZATION, USER_TOKEN)
            .param(CHECK_CCD_PARAM, "true")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden())
            .andExpect(content().string(containsString(message)));
    }

    @Test
    public void givenCouldNotConnectToAuthService_whenRetrieveAosCase_thenReturnHttp503() throws Exception {
        final String message = getUserDetails();
        stubUserDetailsEndpoint(HttpStatus.OK, new EqualToPattern(USER_TOKEN), message);

        when(serviceTokenGenerator.generate()).thenThrow(new HttpClientErrorException(HttpStatus.SERVICE_UNAVAILABLE));

        webClient.perform(MockMvcRequestBuilders.get(API_URL)
            .header(HttpHeaders.AUTHORIZATION, USER_TOKEN)
            .param(CHECK_CCD_PARAM, "true")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isServiceUnavailable());
    }

    @Test
    public void givenNoCaseInCcdOrDraftStore_whenRetrieveAosCase_thenReturnNull() throws Exception {
        final String message = getUserDetails();
        final String serviceToken = "serviceToken";

        stubUserDetailsEndpoint(HttpStatus.OK, new EqualToPattern(USER_TOKEN), message);
        stubGetDraftEndpoint(new EqualToPattern(USER_TOKEN), new EqualToPattern(serviceToken), "");

        when(serviceTokenGenerator.generate()).thenReturn(serviceToken);

        when(coreCaseDataApi
            .searchForCitizen(USER_TOKEN, serviceToken, USER_ID, jurisdictionId, caseType, Collections.emptyMap()))
            .thenReturn(null);

        webClient.perform(MockMvcRequestBuilders.get(API_URL)
            .header(HttpHeaders.AUTHORIZATION, USER_TOKEN)
            .param(CHECK_CCD_PARAM, "true")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent())
            .andExpect(content().string(""));
    }

    @Test
    public void givenDoNotCheckCcdNoCaseInDraftStore_whenRetrieveAosCase_thenReturnNull() throws Exception {
        final String message = getUserDetails();
        final String serviceToken = "serviceToken";

        stubUserDetailsEndpoint(HttpStatus.OK, new EqualToPattern(USER_TOKEN), message);
        stubGetDraftEndpoint(new EqualToPattern(USER_TOKEN), new EqualToPattern(serviceToken), "");

        when(serviceTokenGenerator.generate()).thenReturn(serviceToken);

        webClient.perform(MockMvcRequestBuilders.get(API_URL)
            .header(HttpHeaders.AUTHORIZATION, USER_TOKEN)
            .param(CHECK_CCD_PARAM, "false")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent())
            .andExpect(content().string(""));
    }

    @Test
    public void givenCompletedCaseInCcd_whenRetrieveAosCase_thenReturnTheCase() throws Exception {
        final String message = getUserDetails();
        final String serviceToken = "serviceToken";

        stubUserDetailsEndpoint(HttpStatus.OK, new EqualToPattern(USER_TOKEN), message);

        final Long caseId = 1L;
        final CaseDetails caseDetails = createCaseDetails(caseId, CaseState.AWAITING_DECREE_NISI.getValue());

        when(serviceTokenGenerator.generate()).thenReturn(serviceToken);
        when(coreCaseDataApi
            .searchForCitizen(USER_TOKEN, serviceToken, USER_ID, jurisdictionId, caseType, Collections.emptyMap()))
            .thenReturn(Collections.singletonList(caseDetails));

        webClient.perform(MockMvcRequestBuilders.get(API_URL)
            .header(HttpHeaders.AUTHORIZATION, USER_TOKEN)
            .param(CHECK_CCD_PARAM, "true")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content()
                .json(ObjectMapperTestUtil
                    .convertObjectToJsonString(caseDetails)));
    }

    @Test
    public void givenMultipleCompletedCaseInCcd_whenRetrieveAosCase_thenReturnTheFirstCase() throws Exception {
        final String message = getUserDetails();
        final String serviceToken = "serviceToken";
        stubUserDetailsEndpoint(HttpStatus.OK, new EqualToPattern(USER_TOKEN), message);

        final Long caseId1 = 1L;
        final Long caseId2 = 2L;

        final CaseDetails caseDetails1 = createCaseDetails(caseId1, CaseState.AWAITING_DECREE_NISI.getValue());
        final CaseDetails caseDetails2 = createCaseDetails(caseId2, CaseState.AWAITING_DECREE_NISI.getValue());

        when(serviceTokenGenerator.generate()).thenReturn(serviceToken);
        when(coreCaseDataApi
            .searchForCitizen(USER_TOKEN, serviceToken, USER_ID, jurisdictionId, caseType, Collections.emptyMap()))
            .thenReturn(Arrays.asList(caseDetails1, caseDetails2));

        stubGetDraftEndpoint(new EqualToPattern(USER_TOKEN), new EqualToPattern(serviceToken),
            Strings.EMPTY);

        webClient.perform(MockMvcRequestBuilders.get(API_URL)
            .header(HttpHeaders.AUTHORIZATION, USER_TOKEN)
            .param(CHECK_CCD_PARAM, "true")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content()
                .json(ObjectMapperTestUtil
                    .convertObjectToJsonString(caseDetails1)));
    }

    @Test
    public void givenMultipleCompletedAndOtherCaseInCcd_whenRetrieveAosCase_thenReturnFirstCompletedCase()
        throws Exception {
        final String message = getUserDetails();
        final String serviceToken = "serviceToken";
        stubUserDetailsEndpoint(HttpStatus.OK, new EqualToPattern(USER_TOKEN), message);

        final Long caseId1 = 1L;
        final Long caseId2 = 2L;
        final Long caseId3 = 3L;

        final CaseDetails caseDetails1 = createCaseDetails(caseId1, CaseState.AWAITING_DECREE_NISI.getValue());
        final CaseDetails caseDetails2 = createCaseDetails(caseId2, CaseState.AWAITING_DECREE_NISI.getValue());
        final CaseDetails caseDetails3 = createCaseDetails(caseId3, CaseState.AOS_AWAITING.getValue());

        when(serviceTokenGenerator.generate()).thenReturn(serviceToken);
        when(coreCaseDataApi
            .searchForCitizen(USER_TOKEN, serviceToken, USER_ID, jurisdictionId, caseType, Collections.emptyMap()))
            .thenReturn(Arrays.asList(caseDetails1, caseDetails2, caseDetails3));

        webClient.perform(MockMvcRequestBuilders.get(API_URL)
            .header(HttpHeaders.AUTHORIZATION, USER_TOKEN)
            .param(CHECK_CCD_PARAM, "true")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content()
                .json(ObjectMapperTestUtil
                    .convertObjectToJsonString(caseDetails1)));
    }

    @Test
    public void givenOneInCompleteCaseInCcd_whenRetrieveAosCase_thenReturnTheCase() throws Exception {
        final String message = getUserDetails();
        final String serviceToken = "serviceToken";
        stubUserDetailsEndpoint(HttpStatus.OK, new EqualToPattern(USER_TOKEN), message);

        final Long caseId = 1L;
        final CaseDetails caseDetails = createCaseDetails(caseId, CaseState.AOS_STARTED.getValue());

        when(serviceTokenGenerator.generate()).thenReturn(serviceToken);
        when(coreCaseDataApi
            .searchForCitizen(USER_TOKEN, serviceToken, USER_ID, jurisdictionId, caseType, Collections.emptyMap()))
            .thenReturn(Collections.singletonList(caseDetails));

        webClient.perform(MockMvcRequestBuilders.get(API_URL)
            .header(HttpHeaders.AUTHORIZATION, USER_TOKEN)
            .param(CHECK_CCD_PARAM, "true")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content()
                .json(ObjectMapperTestUtil
                    .convertObjectToJsonString(caseDetails)));
    }

    @Test
    public void givenOneInCompleteAndOtherNonCompleteCaseInCcd_whenRetrieveAosCase_thenReturnInComplete()
        throws Exception {
        final String message = getUserDetails();
        final String serviceToken = "serviceToken";
        stubUserDetailsEndpoint(HttpStatus.OK, new EqualToPattern(USER_TOKEN), message);

        final Long caseId1 = 1L;
        final Long caseId2 = 2L;
        final Long caseId3 = 3L;

        final CaseDetails caseDetails1 = createCaseDetails(caseId1, CaseState.AOS_STARTED.getValue());
        final CaseDetails caseDetails2 = createCaseDetails(caseId2, "state1");
        final CaseDetails caseDetails3 = createCaseDetails(caseId3, "state2");

        when(serviceTokenGenerator.generate()).thenReturn(serviceToken);
        when(coreCaseDataApi
            .searchForCitizen(USER_TOKEN, serviceToken, USER_ID, jurisdictionId, caseType, Collections.emptyMap()))
            .thenReturn(Arrays.asList(caseDetails1, caseDetails2, caseDetails3));

        webClient.perform(MockMvcRequestBuilders.get(API_URL)
            .header(HttpHeaders.AUTHORIZATION, USER_TOKEN)
            .param(CHECK_CCD_PARAM, "true")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content()
                .json(ObjectMapperTestUtil
                    .convertObjectToJsonString(caseDetails1)));
    }

    @Test
    public void givenMultipleInCompleteAndOtherNonCompleteCaseInCcd_whenRetrieveAosCase_thenReturnError()
        throws Exception {
        final String message = getUserDetails();
        final String serviceToken = "serviceToken";
        stubUserDetailsEndpoint(HttpStatus.OK, new EqualToPattern(USER_TOKEN), message);

        final Long caseId1 = 1L;
        final Long caseId2 = 2L;
        final Long caseId3 = 3L;

        final CaseDetails caseDetails1 = createCaseDetails(caseId1, CaseState.AOS_STARTED.getValue());
        final CaseDetails caseDetails2 = createCaseDetails(caseId2, CaseState.AOS_STARTED.getValue());
        final CaseDetails caseDetails3 = createCaseDetails(caseId3, "state2");

        when(serviceTokenGenerator.generate()).thenReturn(serviceToken);
        when(coreCaseDataApi
            .searchForCitizen(USER_TOKEN, serviceToken, USER_ID, jurisdictionId, caseType, Collections.emptyMap()))
            .thenReturn(Arrays.asList(caseDetails1, caseDetails2, caseDetails3));

        webClient.perform(MockMvcRequestBuilders.get(API_URL)
            .header(HttpHeaders.AUTHORIZATION, USER_TOKEN)
            .param(CHECK_CCD_PARAM, "true")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isMultipleChoices());
    }

    @Test
    public void givenCasesInNotInCompleteOrNonCompleteCaseInCcdOrNoDraft_whenRetrieveAosCase_thenReturnNull()
        throws Exception {
        final String message = getUserDetails();
        final String serviceToken = "serviceToken";

        stubUserDetailsEndpoint(HttpStatus.OK, new EqualToPattern(USER_TOKEN), message);
        stubGetDraftEndpoint(new EqualToPattern(USER_TOKEN), new EqualToPattern(serviceToken), "");

        final Long caseId1 = 1L;
        final Long caseId2 = 2L;
        final Long caseId3 = 3L;

        final CaseDetails caseDetails1 = createCaseDetails(caseId1, "state1");
        final CaseDetails caseDetails2 = createCaseDetails(caseId2, "state2");
        final CaseDetails caseDetails3 = createCaseDetails(caseId3, "state3");

        when(serviceTokenGenerator.generate()).thenReturn(serviceToken);
        when(coreCaseDataApi
            .searchForCitizen(USER_TOKEN, serviceToken, USER_ID, jurisdictionId, caseType, Collections.emptyMap()))
            .thenReturn(Arrays.asList(caseDetails1, caseDetails2, caseDetails3));

        webClient.perform(MockMvcRequestBuilders.get(API_URL)
            .header(HttpHeaders.AUTHORIZATION, USER_TOKEN)
            .param(CHECK_CCD_PARAM, "true")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent())
            .andExpect(content().string(""));
    }

    @Test
    public void givenNoCaseInCcdAndOneDraftInStore_whenRetrieveAosCase_thenReturnFormattedDraft() throws Exception {
        final String message = getUserDetails();
        final String serviceToken = "serviceToken";

        final DraftList draftList = new DraftList(Collections.singletonList(
            createDraft("1", DRAFT_DOCUMENT_TYPE_DIVORCE_FORMAT)), null);

        stubUserDetailsEndpoint(HttpStatus.OK, new EqualToPattern(USER_TOKEN), message);

        when(serviceTokenGenerator.generate()).thenReturn(serviceToken);

        stubGetDraftEndpoint(new EqualToPattern(USER_TOKEN), new EqualToPattern(serviceToken),
            ObjectMapperTestUtil.convertObjectToJsonString(draftList));
        when(coreCaseDataApi
            .searchForCitizen(USER_TOKEN, serviceToken, USER_ID, jurisdictionId, caseType, Collections.emptyMap()))
            .thenReturn(null);

        webClient.perform(MockMvcRequestBuilders.get(API_URL)
            .header(HttpHeaders.AUTHORIZATION, USER_TOKEN)
            .param(CHECK_CCD_PARAM, "true")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(ObjectMapperTestUtil.convertObjectToJsonString(CaseDetails
                .builder()
                .data(new HashMap<>())
                .build())));
    }

    @Test
    public void givenNoCaseInCcdAndOneDraftInStoreInDivorceFormat_whenRetrieveAosCase_thenReturnFormattedDraft()
        throws Exception {
        final String message = getUserDetails();
        final String serviceToken = "serviceToken";

        final Map<String, Object> divorceSessionData = Collections.emptyMap();
        final Map<String, Object> caseData = Collections.emptyMap();

        final DraftList draftList = new DraftList(Collections.singletonList(
            new Draft("1", divorceSessionData, DRAFT_DOCUMENT_TYPE_DIVORCE_FORMAT)),
            null);

        final CaseDetails caseDetails = CaseDetails.builder().data(caseData).build();

        stubUserDetailsEndpoint(HttpStatus.OK, new EqualToPattern(USER_TOKEN), message);

        when(serviceTokenGenerator.generate()).thenReturn(serviceToken);

        stubGetDraftEndpoint(new EqualToPattern(USER_TOKEN), new EqualToPattern(serviceToken),
            ObjectMapperTestUtil.convertObjectToJsonString(draftList));
        stubToDivorceFormatEndpoint(divorceSessionData,
            ObjectMapperTestUtil.convertObjectToJsonString(caseData));

        when(coreCaseDataApi
            .searchForCitizen(USER_TOKEN, serviceToken, USER_ID, jurisdictionId, caseType, Collections.emptyMap()))
            .thenReturn(null);

        webClient.perform(MockMvcRequestBuilders.get(API_URL)
            .header(HttpHeaders.AUTHORIZATION, USER_TOKEN)
            .param(CHECK_CCD_PARAM, "true")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(ObjectMapperTestUtil.convertObjectToJsonString(caseDetails)));
    }

    @Test
    public void givenDoNotCheckCcdAndMultipleDraftInStore_whenRetrieveAosCase_thenReturnFormattedDraft() throws Exception {
        final String message = getUserDetails();
        final String serviceToken = "serviceToken";

        final DraftList draftList = new DraftList(Arrays.asList(
            createDraft("1", DRAFT_DOCUMENT_TYPE_DIVORCE_FORMAT),
            createDraft("2", DRAFT_DOCUMENT_TYPE_CCD_FORMAT)),
            null);

        stubUserDetailsEndpoint(HttpStatus.OK, new EqualToPattern(USER_TOKEN), message);

        when(serviceTokenGenerator.generate()).thenReturn(serviceToken);

        stubGetDraftEndpoint(new EqualToPattern(USER_TOKEN), new EqualToPattern(serviceToken),
            ObjectMapperTestUtil.convertObjectToJsonString(draftList));

        webClient.perform(MockMvcRequestBuilders.get(API_URL)
            .header(HttpHeaders.AUTHORIZATION, USER_TOKEN)
            .param(CHECK_CCD_PARAM, "false")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(ObjectMapperTestUtil.convertObjectToJsonString(CaseDetails
                .builder()
                .data(new HashMap<>())
                .build())));
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

    private void stubToDivorceFormatEndpoint(Object request, String response) {
        caseFormatterServer.stubFor(post(TRANSFORM_TO_DIVORCE_CONTEXT_PATH)
            .withRequestBody(equalToJson(ObjectMapperTestUtil.convertObjectToJsonString(request)))
            .withHeader(HttpHeaders.AUTHORIZATION, new EqualToPattern(RetrieveAosCaseITest.USER_TOKEN))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                .withBody(response)));
    }

    private CaseDetails createCaseDetails(Long id, String state) {
        return CaseDetails.builder().id(id).state(state).build();
    }

    private Draft createDraft(String id, String documentType) {
        return new Draft(id, new HashMap<>(), documentType);
    }
}
