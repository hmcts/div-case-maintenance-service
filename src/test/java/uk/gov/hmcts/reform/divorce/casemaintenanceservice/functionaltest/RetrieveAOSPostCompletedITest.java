package uk.gov.hmcts.reform.divorce.casemaintenanceservice.functionaltest;

import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
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
import org.springframework.test.context.TestContextManager;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.CaseMaintenanceServiceApplication;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.client.DraftStoreClient;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties.RESP_EMAIL_ADDRESS;

@RunWith(Parameterized.class)
@ContextConfiguration(classes = CaseMaintenanceServiceApplication.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@PropertySource(value = "classpath:application.yml")
@TestPropertySource(properties = {
    "feign.hystrix.enabled=false",
    "eureka.client.enabled=false"
    })
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class RetrieveAOSPostCompletedITest  extends MockSupport {
    private static final String API_URL = "/casemaintenance/version/1/retrieveAosCase";

    @Value("${ccd.jurisdictionid}")
    private String jurisdictionId;

    @Value("${ccd.casetype}")
    private String caseType;

    @MockBean
    private CoreCaseDataApi coreCaseDataApi;

    @MockBean
    private DraftStoreClient draftStoreClient;

    @Autowired
    private MockMvc webClient;

    private String caseState;

    private TestContextManager testContextManager;

    @Before
    public void setUpContext() throws Exception {
        this.testContextManager = new TestContextManager(getClass());
        this.testContextManager.prepareTestInstance(this);
    }


    @Parameterized.Parameters
    public static Collection<String> testData() {
        return Arrays.asList(
            "AosStarted",
            "Issued",
            "PendingRejection",
            "Submitted",
            "AwaitingLegalAdvisorReferral",
            "AwaitingConsiderationGeneralApplication",
            "AosStarted",
            "AosAwaiting",
            "AosSubmittedAwaitingAnswer",
            "AwaitingDecreeNisi",
            "AwaitingConsiderationDN",
            "AwaitingDocuments",
            "AwaitingClarification",
            "AwaitingConsideration",
            "AwaitingPronouncement",
            "AwaitingDecreeAbsolute",
            "DivorceGranted");
    }

    public RetrieveAOSPostCompletedITest(String caseState) {
        this.caseState = caseState;
    }

    @Test
    public void givenCompletedCaseInCcd_whenRetrieveAosCase_thenReturnTheCase() throws Exception {
        final String message = getUserDetails();
        final String serviceToken = "serviceToken";

        stubUserDetailsEndpoint(HttpStatus.OK, new EqualToPattern(USER_TOKEN), message);

        final Long caseId = 1L;
        final CaseDetails caseDetails = createCaseDetails(caseId, caseState);

        when(serviceTokenGenerator.generate()).thenReturn(serviceToken);
        when(coreCaseDataApi
            .searchForCitizen(USER_TOKEN, serviceToken, USER_ID, jurisdictionId, caseType, Collections.emptyMap()))
            .thenReturn(Collections.singletonList(caseDetails));

        webClient.perform(MockMvcRequestBuilders.get(API_URL)
            .header(HttpHeaders.AUTHORIZATION, USER_TOKEN)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content()
                .json(ObjectMapperTestUtil
                    .convertObjectToJsonString(caseDetails)));
    }

    private CaseDetails createCaseDetails(Long id, String state) {
        return CaseDetails.builder()
            .id(id)
            .state(state)
            .data(ImmutableMap.of(RESP_EMAIL_ADDRESS, USER_EMAIL))
            .build();
    }

}
