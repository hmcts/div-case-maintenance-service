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
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.CaseMaintenanceServiceApplication;

import static org.mockito.Mockito.when;

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
public class RetrievePetitionByCaseIdITest extends MockSupport {
    private static final String API_URL = "/casemaintenance/version/1/case";

    @Value("${ccd.jurisdictionid}")
    private String jurisdictionId;

    @Value("${ccd.casetype}")
    private String caseType;

    @MockBean
    private CoreCaseDataApi coreCaseDataApi;

    @Autowired
    private MockMvc webClient;

    @Test
    public void givenCaseExistsWithId_whenRetrievePetitionById_thenReturnCaseDetails()
        throws Exception {
        final String message = getUserDetails();
        final String serviceToken = "serviceToken";
        stubUserDetailsEndpoint(HttpStatus.OK, new EqualToPattern(USER_TOKEN), message);

        final Long caseId = 1L;
        final String testCaseId = String.valueOf(caseId);

        final CaseDetails caseDetails = createCaseDetails(caseId, "state");

        when(serviceTokenGenerator.generate()).thenReturn(serviceToken);
        when(coreCaseDataApi
            .readForCitizen(USER_TOKEN, serviceToken, USER_ID, jurisdictionId, caseType, testCaseId))
            .thenReturn(caseDetails);

        webClient.perform(MockMvcRequestBuilders.get(API_URL + "/" + testCaseId)
            .header(HttpHeaders.AUTHORIZATION, USER_TOKEN)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content()
                .json(ObjectMapperTestUtil
                    .convertObjectToJsonString(caseDetails)));
    }

    @Test
    public void givenCaseExistsWithId_whenRetrievePetitionByIdWithCaseworker_thenReturnCaseDetails()
        throws Exception {
        final String message = getCaseWorkerUserDetails();
        final String serviceToken = "serviceToken";
        stubUserDetailsEndpoint(HttpStatus.OK, new EqualToPattern(BEARER_CASE_WORKER_TOKEN), message);

        final Long caseId = 1L;
        final String testCaseId = String.valueOf(caseId);

        final CaseDetails caseDetails = createCaseDetails(caseId, "state");

        when(serviceTokenGenerator.generate()).thenReturn(serviceToken);
        when(coreCaseDataApi
            .readForCaseWorker(BEARER_CASE_WORKER_TOKEN, serviceToken, CASE_WORKER_USER_ID,
                jurisdictionId, caseType, testCaseId))
            .thenReturn(caseDetails);

        webClient.perform(MockMvcRequestBuilders.get(API_URL + "/" + testCaseId)
            .header(HttpHeaders.AUTHORIZATION, BEARER_CASE_WORKER_TOKEN)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content()
                .json(ObjectMapperTestUtil
                    .convertObjectToJsonString(caseDetails)));
    }

    @Test
    public void givenCaseDoesNotExistWithId_whenRetrievePetitionById_thenReturnNotFound()
        throws Exception {
        final String message = getUserDetails();
        final String serviceToken = "serviceToken";
        stubUserDetailsEndpoint(HttpStatus.OK, new EqualToPattern(USER_TOKEN), message);

        final Long caseId = 1L;
        final String testCaseId = String.valueOf(caseId);

        when(serviceTokenGenerator.generate()).thenReturn(serviceToken);
        when(coreCaseDataApi
            .readForCitizen(USER_TOKEN, serviceToken, USER_ID, jurisdictionId, caseType, testCaseId))
            .thenReturn(null);

        webClient.perform(MockMvcRequestBuilders.get(API_URL + "/" + testCaseId)
            .header(HttpHeaders.AUTHORIZATION, USER_TOKEN)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    private CaseDetails createCaseDetails(Long id, String state) {
        return CaseDetails.builder().id(id).state(state).build();
    }
}
