package uk.gov.hmcts.reform.divorce.casemaintenanceservice.functionaltest;

import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import feign.FeignException;
import feign.Request;
import feign.Response;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.reform.ccd.client.CaseUserApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseUser;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.CaseMaintenanceServiceApplication;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.TestConstants.TEST_SERVICE_TOKEN;

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
public class UnlinkUserITest  extends MockSupport {

    private static final String CASE_ID = "caseId";

    private static final String API_URL = String.format("/casemaintenance/version/1/link-respondent/%s", CASE_ID);
    private static final int NOT_FOUND = 404;

    @Value("${ccd.jurisdictionid}")
    private String jurisdictionId;

    @Value("${ccd.casetype}")
    private String caseType;

    @MockBean(name = "uk.gov.hmcts.reform.ccd.client.CaseUserApi")
    private CaseUserApi caseUserApi;

    @Autowired
    private MockMvc webClient;

    @Test
    public void givenAuthTokenIsNull_whenUnlinkUser_thenReturnBadRequest() throws Exception {
        webClient.perform(MockMvcRequestBuilders.delete(API_URL))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void givenAllGoesWell_whenUnlinkRespondent_thenReturn200Response() throws Exception {
        final String message = getUserDetails();

        stubUserDetailsEndpoint(HttpStatus.OK, new EqualToPattern(USER_TOKEN), message);
        stubCaseWorkerAuthentication(HttpStatus.OK);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);

        doNothing()
            .when(caseUserApi)
            .updateCaseRolesForUser(
                eq(BEARER_CASE_WORKER_TOKEN),
                eq(TEST_SERVICE_TOKEN),
                eq(CASE_ID),
                eq(USER_ID),
                any(CaseUser.class)
            );

        webClient.perform(MockMvcRequestBuilders.delete(API_URL)
            .header(HttpHeaders.AUTHORIZATION, USER_TOKEN))
            .andExpect(status().isOk());
    }

    @Test
    public void givenCaseNotFound_whenUnlinkRespondent_thenReturnNotFoundResponse() throws Exception {
        final String message = getUserDetails();

        stubUserDetailsEndpoint(HttpStatus.OK, new EqualToPattern(USER_TOKEN), message);
        stubCaseWorkerAuthentication(HttpStatus.OK);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);

        Response mockResponse = Response.builder()
            .request(Request.create(Request.HttpMethod.GET, "http//example.com", Collections.emptyMap(), null))
            .status(NOT_FOUND)
            .headers(Collections.emptyMap())
            .build();
        doThrow(FeignException.errorStatus("CCD exception", mockResponse))
            .when(caseUserApi)
            .updateCaseRolesForUser(
                eq(BEARER_CASE_WORKER_TOKEN),
                eq(TEST_SERVICE_TOKEN),
                eq(CASE_ID),
                eq(USER_ID),
                any(CaseUser.class)
            );

        webClient.perform(MockMvcRequestBuilders.delete(API_URL)
            .header(HttpHeaders.AUTHORIZATION, USER_TOKEN))
            .andExpect(status().is4xxClientError());
    }
}
