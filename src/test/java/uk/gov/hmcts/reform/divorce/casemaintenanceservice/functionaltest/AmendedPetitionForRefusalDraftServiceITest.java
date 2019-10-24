package uk.gov.hmcts.reform.divorce.casemaintenanceservice.functionaltest;

import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.google.common.collect.ImmutableList;
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
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CmsConstants;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.DivorceSessionProperties;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.model.CreateDraft;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.TestConstants.TEST_CASE_REF;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.TestConstants.TEST_DRAFT_DOC_TYPE_DIVORCE_FORMAT;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.TestConstants.TEST_REASON_ADULTERY;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.TestConstants.TEST_RELATIONSHIP;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.TestConstants.TEST_SERVICE_TOKEN;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties.REFUSAL_ORDER_REJECTION_REASONS;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties.REJECTION_INSUFFICIENT_DETAILS;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties.REJECTION_NO_CRITERIA;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties.REJECTION_NO_JURISDICTION;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CmsConstants.YES_VALUE;

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
public class AmendedPetitionForRefusalDraftServiceITest extends MockSupport {
    private static final String API_URL = "/casemaintenance/version/1/amended-petition-draft-refusal";
    private static final String DRAFTS_CONTEXT_PATH = "/drafts";
    private static final String TEST_CASE_ID = "1234567891234567";

    @Value("${draft.store.api.max.age}")
    private int maxAge;

    @Autowired
    private MockMvc webClient;

    @MockBean
    private CoreCaseDataApi coreCaseDataApi;

    @Value("${ccd.jurisdictionid}")
    private String jurisdictionId;

    @Value("${ccd.casetype}")
    private String caseType;

    @Test
    public void givenJWTTokenIsNull_whenAmendedPetitionDraftForRefusal_thenReturnBadRequest() throws Exception {
        webClient.perform(MockMvcRequestBuilders.put(API_URL)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void givenInvalidUserToken_whenAmendedPetitionDraftForRefusal_thenReturnForbiddenError() throws Exception {
        final String message = "some message";
        stubUserDetailsEndpoint(HttpStatus.FORBIDDEN, new EqualToPattern(USER_TOKEN), message);

        webClient.perform(MockMvcRequestBuilders.put(API_URL)
            .header(HttpHeaders.AUTHORIZATION, USER_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden())
            .andExpect(content().string(containsString(message)));
    }

    @Test
    public void givenCouldNotConnectToAuthService_whenAmendedPetitionDraftForRefusal_thenReturnHttp503() throws Exception {
        final String message = getUserDetails();

        when(serviceTokenGenerator.generate()).thenThrow(new HttpClientErrorException(HttpStatus.SERVICE_UNAVAILABLE));

        stubUserDetailsEndpoint(HttpStatus.OK, new EqualToPattern(USER_TOKEN), message);

        webClient.perform(MockMvcRequestBuilders.put(API_URL)
            .header(HttpHeaders.AUTHORIZATION, USER_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isServiceUnavailable());
    }

    @Test
    public void givenValidRequestToAmend_whenAmendedPetitionDraftForRefusal_thenCreateAmendedPetitionDraft() throws Exception {
        final String message = getUserDetails();

        final Map<String, Object> caseData = new HashMap<>();
        caseData.put(CcdCaseProperties.D8_CASE_REFERENCE, TEST_CASE_REF);
        caseData.put(CcdCaseProperties.D8_LEGAL_PROCEEDINGS, YES_VALUE);
        caseData.put(CcdCaseProperties.D8_DIVORCE_WHO, TEST_RELATIONSHIP);
        caseData.put(CcdCaseProperties.D8_SCREEN_HAS_MARRIAGE_BROKEN, YES_VALUE);
        caseData.put(CcdCaseProperties.D8_PETITIONER_EMAIL, TEST_USER_EMAIL);
        caseData.put(CcdCaseProperties.D8_REASON_FOR_DIVORCE, TEST_REASON_ADULTERY);
        caseData.put(REFUSAL_ORDER_REJECTION_REASONS, ImmutableList.of(
            REJECTION_NO_JURISDICTION, REJECTION_NO_CRITERIA, REJECTION_INSUFFICIENT_DETAILS
        ));
        final CaseDetails oldCase = CaseDetails.builder().data(caseData)
            .id(Long.decode(TEST_CASE_ID)).build();

        final Map<String, Object> caseDataFormatRequest = new HashMap<>();
        caseDataFormatRequest.put(CcdCaseProperties.D8_CASE_REFERENCE, TEST_CASE_REF);
        caseDataFormatRequest.put(CcdCaseProperties.D8_DIVORCE_WHO, TEST_RELATIONSHIP);
        caseDataFormatRequest.put(CcdCaseProperties.D8_SCREEN_HAS_MARRIAGE_BROKEN, YES_VALUE);
        caseDataFormatRequest.put(CcdCaseProperties.D8_PETITIONER_EMAIL, TEST_USER_EMAIL);
        caseDataFormatRequest.put(CcdCaseProperties.D8_DIVORCE_UNIT, CmsConstants.CTSC_SERVICE_CENTRE);
        caseDataFormatRequest.put(REFUSAL_ORDER_REJECTION_REASONS, ImmutableList.of(
            REJECTION_NO_JURISDICTION, REJECTION_NO_CRITERIA, REJECTION_INSUFFICIENT_DETAILS
        ));

        final Map<String, Object> draftData = new HashMap<>();

        draftData.put(DivorceSessionProperties.PREVIOUS_CASE_ID, TEST_CASE_ID);
        draftData.put(DivorceSessionProperties.DIVORCE_WHO, TEST_RELATIONSHIP);
        draftData.put(DivorceSessionProperties.SCREEN_HAS_MARRIAGE_BROKEN, YES_VALUE);
        draftData.put(DivorceSessionProperties.COURTS, CmsConstants.CTSC_SERVICE_CENTRE);
        draftData.put(DivorceSessionProperties.PREVIOUS_REASONS_FOR_DIVORCE_REFUSAL, Collections.singletonList(TEST_REASON_ADULTERY));

        final CreateDraft createDraft = new CreateDraft(draftData,
            TEST_DRAFT_DOC_TYPE_DIVORCE_FORMAT, maxAge);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);
        when(coreCaseDataApi
            .searchForCitizen(USER_TOKEN, TEST_SERVICE_TOKEN, USER_ID, jurisdictionId, caseType, Collections.emptyMap()))
            .thenReturn(Collections.singletonList(oldCase));

        stubUserDetailsEndpoint(HttpStatus.OK, new EqualToPattern(USER_TOKEN), message);
        stubDeleteDraftsEndpoint(new EqualToPattern(TEST_SERVICE_TOKEN));
        stubCreateDraftEndpoint(new EqualToPattern(TEST_SERVICE_TOKEN), createDraft);

        webClient.perform(MockMvcRequestBuilders.put(API_URL)
            .header(HttpHeaders.AUTHORIZATION, USER_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content()
                .json(ObjectMapperTestUtil
                    .convertObjectToJsonString(draftData)));
    }

    @Test
    public void givenInvalidRequestToAmend_whenAmendedPetitionDraftForRefusal_thenReturn404() throws Exception {
        final String message = getUserDetails();
        final Map<String, Object> caseData = new HashMap<>();
        caseData.put(REFUSAL_ORDER_REJECTION_REASONS, Collections.singletonList("other"));

        final Long caseId = 1L;
        final CaseDetails caseDetails = CaseDetails.builder()
            .data(caseData).id(caseId).state(CaseState.SUBMITTED.getValue()).build();

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);
        when(coreCaseDataApi
            .searchForCitizen(USER_TOKEN, TEST_SERVICE_TOKEN, USER_ID, jurisdictionId, caseType, Collections.emptyMap()))
            .thenReturn(Collections.singletonList(caseDetails));

        stubUserDetailsEndpoint(HttpStatus.OK, new EqualToPattern(USER_TOKEN), message);

        webClient.perform(MockMvcRequestBuilders.put(API_URL)
            .header(HttpHeaders.AUTHORIZATION, USER_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    public void givenNoCaseToAmend_whenAmendedPetitionDraftForRefusal_thenReturn404() throws Exception {
        final String message = getUserDetails();

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);
        when(coreCaseDataApi
            .searchForCitizen(USER_TOKEN, TEST_SERVICE_TOKEN, USER_ID, jurisdictionId, caseType, Collections.emptyMap()))
            .thenReturn(null);

        stubUserDetailsEndpoint(HttpStatus.OK, new EqualToPattern(USER_TOKEN), message);

        webClient.perform(MockMvcRequestBuilders.put(API_URL)
            .header(HttpHeaders.AUTHORIZATION, USER_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    private void stubCreateDraftEndpoint(StringValuePattern serviceToken, CreateDraft createDraft) {
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

    private void stubDeleteDraftsEndpoint(StringValuePattern serviceToken) {
        draftStoreServer.stubFor(delete(DRAFTS_CONTEXT_PATH)
            .withHeader(HttpHeaders.AUTHORIZATION, new EqualToPattern(USER_TOKEN))
            .withHeader(DraftStoreClient.SERVICE_AUTHORIZATION_HEADER_NAME, serviceToken)
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                .withBody("")));
    }
}
