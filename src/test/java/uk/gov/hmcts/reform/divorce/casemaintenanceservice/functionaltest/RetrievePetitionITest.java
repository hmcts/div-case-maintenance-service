package uk.gov.hmcts.reform.divorce.casemaintenanceservice.functionaltest;

import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.google.common.collect.ImmutableMap;
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
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.DivorceSessionProperties;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.domain.model.CitizenCaseState;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.model.Draft;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.model.DraftList;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.TestConstants.TEST_DRAFT_DOC_TYPE_DIVORCE_FORMAT;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.TestConstants.TEST_SERVICE_TOKEN;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties.D8_PETITIONER_EMAIL;

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
public class RetrievePetitionITest extends MockSupport {
    private static final String API_URL = "/casemaintenance/version/1/retrieveCase";
    private static final String DRAFTS_CONTEXT_PATH = "/drafts";

    private static final String AWAITING_PAYMENT_STATE = CitizenCaseState.AWAITING_PAYMENT.getValue();
    private static final String SUBMITTED_PAYMENT_STATE = CitizenCaseState.SUBMITTED.getValue();

    @Value("${ccd.jurisdictionid}")
    private String jurisdictionId;

    @Value("${ccd.casetype}")
    private String caseType;

    @MockBean
    private CoreCaseDataApi coreCaseDataApi;

    @Autowired
    private MockMvc webClient;

    @Test
    public void givenJWTTokenIsNull_whenRetrievePetition_thenReturnBadRequest() throws Exception {
        webClient.perform(MockMvcRequestBuilders.get(API_URL)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void givenInvalidUserToken_whenRetrievePetition_thenReturnForbiddenError() throws Exception {
        final String message = "some message";
        stubUserDetailsEndpoint(HttpStatus.FORBIDDEN, new EqualToPattern(USER_TOKEN), message);

        webClient.perform(MockMvcRequestBuilders.get(API_URL)
            .header(HttpHeaders.AUTHORIZATION, USER_TOKEN)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden())
            .andExpect(content().string(containsString(message)));
    }

    @Test
    public void givenCouldNotConnectToAuthService_whenRetrievePetition_thenReturnHttp503() throws Exception {
        final String message = getUserDetails();
        stubUserDetailsEndpoint(HttpStatus.OK, new EqualToPattern(USER_TOKEN), message);

        when(serviceTokenGenerator.generate()).thenThrow(new HttpClientErrorException(HttpStatus.SERVICE_UNAVAILABLE));

        webClient.perform(MockMvcRequestBuilders.get(API_URL)
            .header(HttpHeaders.AUTHORIZATION, USER_TOKEN)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isServiceUnavailable());
    }

    @Test
    public void givenNoCaseInCcdOrDraftStore_whenRetrievePetition_thenReturnNull() throws Exception {
        final String message = getUserDetails();

        stubUserDetailsEndpoint(HttpStatus.OK, new EqualToPattern(USER_TOKEN), message);
        stubGetDraftEndpoint(new EqualToPattern(USER_TOKEN), new EqualToPattern(TEST_SERVICE_TOKEN), "");

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);

        when(coreCaseDataApi
            .searchForCitizen(USER_TOKEN, TEST_SERVICE_TOKEN, USER_ID, jurisdictionId, caseType, Collections.emptyMap()))
            .thenReturn(null);

        webClient.perform(MockMvcRequestBuilders.get(API_URL)
            .header(HttpHeaders.AUTHORIZATION, USER_TOKEN)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent())
            .andExpect(content().string(""));
    }

    public void givenCompletedCaseInCcd_whenRetrievePetition_thenReturnTheCase() throws Exception {
        final String message = getUserDetails();

        stubUserDetailsEndpoint(HttpStatus.OK, new EqualToPattern(USER_TOKEN), message);
        stubGetDraftEndpoint(new EqualToPattern(USER_TOKEN), new EqualToPattern(TEST_SERVICE_TOKEN), "");

        final Long caseId = 1L;
        final CaseDetails caseDetails = createCaseDetails(caseId, CaseState.SUBMITTED.getValue());

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);
        when(coreCaseDataApi
            .searchForCitizen(USER_TOKEN, TEST_SERVICE_TOKEN, USER_ID, jurisdictionId, caseType, Collections.emptyMap()))
            .thenReturn(Collections.singletonList(caseDetails));

        webClient.perform(MockMvcRequestBuilders.get(API_URL)
            .header(HttpHeaders.AUTHORIZATION, USER_TOKEN)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content()
                .json(ObjectMapperTestUtil
                    .convertObjectToJsonString(caseDetails)));
    }

    @Test
    public void givenMultipleCompletedCaseInCcd_whenRetrievePetition_thenReturnTheFirstCase() throws Exception {
        final String message = getUserDetails();
        stubUserDetailsEndpoint(HttpStatus.OK, new EqualToPattern(USER_TOKEN), message);

        final Long caseId1 = 1L;
        final Long caseId2 = 2L;
        final Long caseId3 = 3L;
        final Long caseId4 = 4L;

        final CaseDetails caseDetails1 = createCaseDetails(caseId1, SUBMITTED_PAYMENT_STATE);
        final CaseDetails caseDetails2 = createCaseDetails(caseId2, CitizenCaseState.ISSUED.getValue());
        final CaseDetails caseDetails3 = createCaseDetails(caseId3, CitizenCaseState.PENDING_REJECTION.getValue());
        final CaseDetails caseDetails4 = createCaseDetails(caseId4, CitizenCaseState.PENDING_REJECTION.getValue());

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);
        when(coreCaseDataApi
            .searchForCitizen(USER_TOKEN, TEST_SERVICE_TOKEN, USER_ID, jurisdictionId, caseType, Collections.emptyMap()))
            .thenReturn(Arrays.asList(caseDetails1, caseDetails2, caseDetails3, caseDetails4));

        webClient.perform(MockMvcRequestBuilders.get(API_URL)
            .header(HttpHeaders.AUTHORIZATION, USER_TOKEN)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content()
                .json(ObjectMapperTestUtil
                    .convertObjectToJsonString(caseDetails1)));
    }

    @Test
    public void givenMultipleCompletedAndOtherCaseInCcd_whenRetrievePetition_thenReturnFirstCompletedCase()
        throws Exception {
        final String message = getUserDetails();
        stubUserDetailsEndpoint(HttpStatus.OK, new EqualToPattern(USER_TOKEN), message);

        final Long caseId1 = 1L;
        final Long caseId2 = 2L;
        final Long caseId3 = 3L;

        final CaseDetails caseDetails1 = createCaseDetails(caseId1, CitizenCaseState.PENDING_REJECTION.getValue());
        final CaseDetails caseDetails2 = createCaseDetails(caseId2, SUBMITTED_PAYMENT_STATE);
        final CaseDetails caseDetails3 = createCaseDetails(caseId3, AWAITING_PAYMENT_STATE);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);
        when(coreCaseDataApi
            .searchForCitizen(USER_TOKEN, TEST_SERVICE_TOKEN, USER_ID, jurisdictionId, caseType, Collections.emptyMap()))
            .thenReturn(Arrays.asList(caseDetails1, caseDetails2, caseDetails3));

        webClient.perform(MockMvcRequestBuilders.get(API_URL)
            .header(HttpHeaders.AUTHORIZATION, USER_TOKEN)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content()
                .json(ObjectMapperTestUtil
                    .convertObjectToJsonString(caseDetails1)));
    }

    @Test
    public void givenOneInCompleteCaseInCcd_whenRetrievePetition_thenReturnTheCase() throws Exception {
        final String message = getUserDetails();
        stubUserDetailsEndpoint(HttpStatus.OK, new EqualToPattern(USER_TOKEN), message);

        final Long caseId = 1L;
        final CaseDetails caseDetails = createCaseDetails(caseId, CaseState.AWAITING_PAYMENT.getValue());

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);
        when(coreCaseDataApi
            .searchForCitizen(USER_TOKEN, TEST_SERVICE_TOKEN, USER_ID, jurisdictionId, caseType, Collections.emptyMap()))
            .thenReturn(Collections.singletonList(caseDetails));

        webClient.perform(MockMvcRequestBuilders.get(API_URL)
            .header(HttpHeaders.AUTHORIZATION, USER_TOKEN)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content()
                .json(ObjectMapperTestUtil
                    .convertObjectToJsonString(caseDetails)));
    }

    @Test
    public void givenOneInCompleteAndOtherNonCompleteCaseInCcd_whenRetrievePetition_thenReturnInComplete()
        throws Exception {
        final String message = getUserDetails();
        stubUserDetailsEndpoint(HttpStatus.OK, new EqualToPattern(USER_TOKEN), message);

        final Long caseId1 = 1L;
        final Long caseId2 = 2L;
        final Long caseId3 = 3L;

        final CaseDetails caseDetails1 = createCaseDetails(caseId1, CaseState.AWAITING_PAYMENT.getValue());
        final CaseDetails caseDetails2 = createCaseDetails(caseId2, "state1");
        final CaseDetails caseDetails3 = createCaseDetails(caseId3, "state2");

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);
        when(coreCaseDataApi
            .searchForCitizen(USER_TOKEN, TEST_SERVICE_TOKEN, USER_ID, jurisdictionId, caseType, Collections.emptyMap()))
            .thenReturn(Arrays.asList(caseDetails1, caseDetails2, caseDetails3));

        webClient.perform(MockMvcRequestBuilders.get(API_URL)
            .header(HttpHeaders.AUTHORIZATION, USER_TOKEN)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content()
                .json(ObjectMapperTestUtil
                    .convertObjectToJsonString(caseDetails1)));
    }

    @Test
    public void givenMultipleInCompleteAndOtherNonCompleteCaseInCcd_whenRetrievePetition_thenReturnError()
        throws Exception {
        final String message = getUserDetails();
        stubUserDetailsEndpoint(HttpStatus.OK, new EqualToPattern(USER_TOKEN), message);
        stubGetDraftEndpoint(new EqualToPattern(USER_TOKEN), new EqualToPattern(TEST_SERVICE_TOKEN), "");

        final Long caseId1 = 1L;
        final Long caseId2 = 2L;
        final Long caseId3 = 3L;

        final CaseDetails caseDetails1 = createCaseDetails(caseId1, AWAITING_PAYMENT_STATE);
        final CaseDetails caseDetails2 = createCaseDetails(caseId2, CitizenCaseState.AWAITING_HWF_DECISION.getValue());
        final CaseDetails caseDetails3 = createCaseDetails(caseId3, "state2");

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);
        when(coreCaseDataApi
            .searchForCitizen(USER_TOKEN, TEST_SERVICE_TOKEN, USER_ID, jurisdictionId, caseType, Collections.emptyMap()))
            .thenReturn(Arrays.asList(caseDetails1, caseDetails2, caseDetails3));

        webClient.perform(MockMvcRequestBuilders.get(API_URL)
            .header(HttpHeaders.AUTHORIZATION, USER_TOKEN)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isMultipleChoices());
    }

    @Test
    public void givenCasesInNotInCompleteOrNonCompleteCaseInCcdOrNoDraft_whenRetrievePetition_thenReturnNull()
        throws Exception {
        final String message = getUserDetails();

        stubUserDetailsEndpoint(HttpStatus.OK, new EqualToPattern(USER_TOKEN), message);
        stubGetDraftEndpoint(new EqualToPattern(USER_TOKEN), new EqualToPattern(TEST_SERVICE_TOKEN), "");

        final Long caseId1 = 1L;
        final Long caseId2 = 2L;
        final Long caseId3 = 3L;

        final CaseDetails caseDetails1 = createCaseDetails(caseId1, "state1");
        final CaseDetails caseDetails2 = createCaseDetails(caseId2, "state2");
        final CaseDetails caseDetails3 = createCaseDetails(caseId3, "state3");

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);
        when(coreCaseDataApi
            .searchForCitizen(USER_TOKEN, TEST_SERVICE_TOKEN, USER_ID, jurisdictionId, caseType, Collections.emptyMap()))
            .thenReturn(Arrays.asList(caseDetails1, caseDetails2, caseDetails3));

        webClient.perform(MockMvcRequestBuilders.get(API_URL)
            .header(HttpHeaders.AUTHORIZATION, USER_TOKEN)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent())
            .andExpect(content().string(""));
    }

    @Test
    public void givenNoCaseInCcdAndOneDraftInStore_whenRetrievePetition_thenReturnFormattedDraft() throws Exception {
        final String message = getUserDetails();

        final DraftList draftList = new DraftList(Collections.singletonList(
            createDraft("1", TEST_DRAFT_DOC_TYPE_DIVORCE_FORMAT)), null);

        stubUserDetailsEndpoint(HttpStatus.OK, new EqualToPattern(USER_TOKEN), message);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);

        stubGetDraftEndpoint(new EqualToPattern(USER_TOKEN), new EqualToPattern(TEST_SERVICE_TOKEN),
            ObjectMapperTestUtil.convertObjectToJsonString(draftList));
        when(coreCaseDataApi
            .searchForCitizen(USER_TOKEN, TEST_SERVICE_TOKEN, USER_ID, jurisdictionId, caseType, Collections.emptyMap()))
            .thenReturn(null);

        webClient.perform(MockMvcRequestBuilders.get(API_URL)
            .header(HttpHeaders.AUTHORIZATION, USER_TOKEN)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(ObjectMapperTestUtil.convertObjectToJsonString(CaseDetails
                .builder()
                .data(new HashMap<>())
                .build())));
    }

    @Test
    public void givenNoCaseInCcdAndOneDraftInStoreInDivorceFormat_whenRetrievePetition_thenReturnFormattedDraft()
        throws Exception {
        final String message = getUserDetails();

        final Map<String, Object> divorceSessionData = Collections.emptyMap();
        final Map<String, Object> caseData = Collections.emptyMap();

        final DraftList draftList = new DraftList(Collections.singletonList(
            new Draft("1", divorceSessionData, TEST_DRAFT_DOC_TYPE_DIVORCE_FORMAT)),
            null);

        final CaseDetails caseDetails = CaseDetails.builder().data(caseData).build();

        stubUserDetailsEndpoint(HttpStatus.OK, new EqualToPattern(USER_TOKEN), message);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);

        stubGetDraftEndpoint(new EqualToPattern(USER_TOKEN), new EqualToPattern(TEST_SERVICE_TOKEN),
            ObjectMapperTestUtil.convertObjectToJsonString(draftList));

        when(coreCaseDataApi
            .searchForCitizen(USER_TOKEN, TEST_SERVICE_TOKEN, USER_ID, jurisdictionId, caseType, Collections.emptyMap()))
            .thenReturn(null);

        webClient.perform(MockMvcRequestBuilders.get(API_URL)
            .header(HttpHeaders.AUTHORIZATION, USER_TOKEN)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(ObjectMapperTestUtil.convertObjectToJsonString(caseDetails)));
    }

    @Test
    public void givenAmendPetitionDraft_whenRetrievePetition_thenReturnFormattedDraft() throws Exception {
        final String message = getUserDetails();

        final DraftList draftList = new DraftList(Arrays.asList(
            createDraft("1", ImmutableMap.of(DivorceSessionProperties.PREVIOUS_CASE_ID, "1"),
                TEST_DRAFT_DOC_TYPE_DIVORCE_FORMAT)),
            null);

        stubUserDetailsEndpoint(HttpStatus.OK, new EqualToPattern(USER_TOKEN), message);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);

        stubGetDraftEndpoint(new EqualToPattern(USER_TOKEN), new EqualToPattern(TEST_SERVICE_TOKEN),
            ObjectMapperTestUtil.convertObjectToJsonString(draftList));

        webClient.perform(MockMvcRequestBuilders.get(API_URL)
            .header(HttpHeaders.AUTHORIZATION, USER_TOKEN)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(ObjectMapperTestUtil.convertObjectToJsonString(CaseDetails
                .builder()
                .data(ImmutableMap.of(DivorceSessionProperties.PREVIOUS_CASE_ID, "1"))
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

    private CaseDetails createCaseDetails(Long id, String state) {
        return CaseDetails.builder()
            .id(id)
            .state(state)
            .data(ImmutableMap.of(D8_PETITIONER_EMAIL, TEST_USER_EMAIL))
            .build();
    }

    private Draft createDraft(String id, String documentType) {
        return createDraft(id, new HashMap<>(), documentType);
    }

    private Draft createDraft(String id, Map<String, Object> document, String documentType) {
        return new Draft(id, document, documentType);
    }
}
