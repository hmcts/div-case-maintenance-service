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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.CaseMaintenanceServiceApplication;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CaseState;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CmsConstants;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.DivorceSessionProperties;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.impl.CcdRetrievalServiceImpl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.TestConstants.TEST_CASE_REF;
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
public class AmendedPetitionForRefusalDraftFromCaseIdServiceITest extends MockSupport {
    private static final String API_URL = "/casemaintenance/version/1/amended-petition-draft-refusal";
    private static final String TEST_CASE_ID = "1234567891234567";
    private static final String TRANSFORM_TO_DIVORCE_CONTEXT_PATH = "/caseformatter/version/1/to-divorce-format";

    @Autowired
    private MockMvc webClient;

    @Autowired
    private CcdRetrievalServiceImpl ccdRetrievalService;

    @MockBean(name = "uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi")
    private CoreCaseDataApi coreCaseDataApi;

    @Value("${ccd.jurisdictionid}")
    private String jurisdictionId;

    @Value("${ccd.casetype}")
    private String caseType;

    @Test
    public void givenJWTTokenIsNull_whenAmendedPetitionDraftForRefusalFromCaseId_thenReturnBadRequest()
        throws Exception {
        webClient.perform(MockMvcRequestBuilders.put(API_URL + "/" + TEST_CASE_ID)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void givenInvalidUserToken_whenAmendedPetitionDraftForRefusalFromCaseId_thenReturnForbiddenError()
        throws Exception {
        final String message = "some message";
        stubUserDetailsEndpoint(HttpStatus.FORBIDDEN, new EqualToPattern(USER_TOKEN), message);
        stubCaseWorkerAuthentication(HttpStatus.OK);

        webClient.perform(MockMvcRequestBuilders.put(API_URL + "/" + TEST_CASE_ID)
                .header(HttpHeaders.AUTHORIZATION, USER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden())
            .andExpect(content().string(containsString(message)));
    }

    @Test
    public void givenCouldNotConnectToAuthService_whenAmendedPetitionDraftForRefusalFromCaseId_thenReturnHttp503()
        throws Exception {
        final String solicitorUserDetails = getSolicitorUserDetails();

        when(serviceTokenGenerator.generate()).thenThrow(new HttpClientErrorException(HttpStatus.SERVICE_UNAVAILABLE));

        stubUserDetailsEndpoint(HttpStatus.OK, new EqualToPattern(USER_TOKEN), solicitorUserDetails);
        stubCaseWorkerAuthentication(HttpStatus.OK);

        webClient.perform(MockMvcRequestBuilders.put(API_URL + "/" + TEST_CASE_ID)
                .header(HttpHeaders.AUTHORIZATION, USER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isServiceUnavailable());
    }

    @Test
    public void givenValidRequestToAmend_whenAmendedPetitionDraftForRefusalFromCaseId_thenCreateAmendedPetitionDraft()
        throws Exception {
        final String solicitorUserDetails = getSolicitorUserDetails();

        final Map<String, Object> caseData = new HashMap<>();
        caseData.put(CcdCaseProperties.D8_CASE_REFERENCE, TEST_CASE_REF);
        caseData.put(CcdCaseProperties.D8_LEGAL_PROCEEDINGS, YES_VALUE);
        caseData.put(CcdCaseProperties.D8_DIVORCE_WHO, TEST_RELATIONSHIP);
        caseData.put(CcdCaseProperties.D8_SCREEN_HAS_MARRIAGE_BROKEN, YES_VALUE);
        caseData.put(CcdCaseProperties.D8_PETITIONER_EMAIL, TEST_USER_EMAIL);
        caseData.put(CcdCaseProperties.D8_REASON_FOR_DIVORCE, TEST_REASON_ADULTERY);
        caseData.put(REFUSAL_ORDER_REJECTION_REASONS, List.of(
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
        caseDataFormatRequest.put(REFUSAL_ORDER_REJECTION_REASONS, List.of(
            REJECTION_NO_JURISDICTION, REJECTION_NO_CRITERIA, REJECTION_INSUFFICIENT_DETAILS
        ));

        final Map<String, Object> draftData = new HashMap<>();

        draftData.put(DivorceSessionProperties.PREVIOUS_CASE_ID, TEST_CASE_ID);
        draftData.put(CcdCaseProperties.D8_DIVORCE_WHO, TEST_RELATIONSHIP);
        draftData.put(CcdCaseProperties.D8_SCREEN_HAS_MARRIAGE_BROKEN, YES_VALUE);
        draftData.put(CcdCaseProperties.D8_DIVORCE_UNIT, CmsConstants.CTSC_SERVICE_CENTRE);
        draftData.put(DivorceSessionProperties.PREVIOUS_REASONS_FOR_DIVORCE_REFUSAL, Collections
            .singletonList(TEST_REASON_ADULTERY));

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);
        String searchQuery = ccdRetrievalService.buildQuery(TEST_CASE_ID, "reference");
        when(coreCaseDataApi
            .searchCases(BEARER_CASE_WORKER_TOKEN, TEST_SERVICE_TOKEN, caseType, searchQuery)).thenReturn(
            SearchResult.builder().cases(Collections.singletonList(oldCase)).build());

        stubUserDetailsEndpoint(HttpStatus.OK, new EqualToPattern(USER_TOKEN), solicitorUserDetails);
        stubCaseWorkerAuthentication(HttpStatus.OK);
        stubToDivorceFormatEndpoint(caseDataFormatRequest, draftData);

        webClient.perform(MockMvcRequestBuilders.put(API_URL + "/" + TEST_CASE_ID)
                .header(HttpHeaders.AUTHORIZATION, USER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content()
                .json(ObjectMapperTestUtil
                    .convertObjectToJsonString(draftData)));
    }

    @Test
    public void givenInvalidRequestToAmend_whenAmendedPetitionDraftForRefusalFromCaseId_thenReturn404()
        throws Exception {
        final String solicitorUserDetails = getSolicitorUserDetails();
        final Map<String, Object> caseData = new HashMap<>();
        caseData.put(REFUSAL_ORDER_REJECTION_REASONS, Collections.singletonList("other"));

        final Long caseId = 1L;
        final CaseDetails caseDetails = CaseDetails.builder()
            .data(caseData).id(caseId).state(CaseState.SUBMITTED.getValue()).build();

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);

        String searchQuery = ccdRetrievalService.buildQuery(TEST_CASE_ID, "reference");
        when(coreCaseDataApi
            .searchCases(BEARER_CASE_WORKER_TOKEN, TEST_SERVICE_TOKEN, caseType, searchQuery)).thenReturn(
            SearchResult.builder().cases(Collections.singletonList(caseDetails)).build());


        stubUserDetailsEndpoint(HttpStatus.OK, new EqualToPattern(USER_TOKEN), solicitorUserDetails);
        stubCaseWorkerAuthentication(HttpStatus.OK);

        webClient.perform(MockMvcRequestBuilders.put(API_URL + "/" + TEST_CASE_ID)
                .header(HttpHeaders.AUTHORIZATION, USER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    public void givenNoCaseToAmend_whenAmendedPetitionDraftForRefusal_thenReturn404() throws Exception {
        final String solicitorUserDetails = getSolicitorUserDetails();

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);

        String searchQuery = ccdRetrievalService.buildQuery(TEST_CASE_ID, "reference");
        when(coreCaseDataApi
            .searchCases(BEARER_CASE_WORKER_TOKEN, TEST_SERVICE_TOKEN, caseType, searchQuery)).thenReturn(
            SearchResult.builder().cases(null).build());

        stubUserDetailsEndpoint(HttpStatus.OK, new EqualToPattern(USER_TOKEN), solicitorUserDetails);
        stubCaseWorkerAuthentication(HttpStatus.OK);

        webClient.perform(MockMvcRequestBuilders.put(API_URL + "/" + TEST_CASE_ID)
                .header(HttpHeaders.AUTHORIZATION, USER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    private void stubToDivorceFormatEndpoint(Object request, Object response) {
        caseFormatterServer.stubFor(post(TRANSFORM_TO_DIVORCE_CONTEXT_PATH)
            .withRequestBody(equalToJson(ObjectMapperTestUtil.convertObjectToJsonString(request)))
            .withHeader(HttpHeaders.AUTHORIZATION, new EqualToPattern(USER_TOKEN))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(ObjectMapperTestUtil.convertObjectToJsonString(response))));
    }
}
