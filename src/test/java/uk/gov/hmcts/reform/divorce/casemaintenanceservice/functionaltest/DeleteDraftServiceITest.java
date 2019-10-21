package uk.gov.hmcts.reform.divorce.casemaintenanceservice.functionaltest;

import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.CaseMaintenanceServiceApplication;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.client.DraftStoreClient;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
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
public class DeleteDraftServiceITest extends MockSupport {
    private static final String API_URL = "/casemaintenance/version/1/drafts";
    private static final String DRAFTS_CONTEXT_PATH = "/drafts";

    @Autowired
    private MockMvc webClient;

    @Test
    public void givenJWTTokenIsNull_whenDeleteDraft_thenReturnBadRequest() throws Exception {
        webClient.perform(MockMvcRequestBuilders.delete(API_URL))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void givenCouldNotConnectToAuthService_whenDeleteDraft_thenReturnHttp503() throws Exception {
        final String message = getUserDetails();

        when(serviceTokenGenerator.generate()).thenThrow(new HttpClientErrorException(HttpStatus.SERVICE_UNAVAILABLE));

        stubUserDetailsEndpoint(HttpStatus.OK, new EqualToPattern(USER_TOKEN), message);

        webClient.perform(MockMvcRequestBuilders.delete(API_URL)
            .header(HttpHeaders.AUTHORIZATION, USER_TOKEN))
            .andExpect(status().isServiceUnavailable());
    }

    @Test
    public void givenNoDrafts_whenDeleteDraft_thenReturnHttp200() throws Exception {
        final String message = getUserDetails();

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);

        stubUserDetailsEndpoint(HttpStatus.OK, new EqualToPattern(USER_TOKEN), message);

        webClient.perform(MockMvcRequestBuilders.delete(API_URL)
            .header(HttpHeaders.AUTHORIZATION, USER_TOKEN))
            .andExpect(status().isOk());
    }

    @Test
    public void givenThereIsDrafts_whenDeleteDraft_thenReturnHttp200() throws Exception {
        final String message = getUserDetails();

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);

        stubUserDetailsEndpoint(HttpStatus.OK, new EqualToPattern(USER_TOKEN), message);
        stubDeleteDraftEndpoint(new EqualToPattern(USER_TOKEN), new EqualToPattern(TEST_SERVICE_TOKEN));

        webClient.perform(MockMvcRequestBuilders.delete(API_URL)
            .header(HttpHeaders.AUTHORIZATION, USER_TOKEN))
            .andExpect(status().isOk());
    }

    private void stubDeleteDraftEndpoint(StringValuePattern authHeader, StringValuePattern serviceToken) {
        draftStoreServer.stubFor(delete(DRAFTS_CONTEXT_PATH)
            .withHeader(HttpHeaders.AUTHORIZATION, authHeader)
            .withHeader(DraftStoreClient.SERVICE_AUTHORIZATION_HEADER_NAME, serviceToken)
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())));
    }
}
