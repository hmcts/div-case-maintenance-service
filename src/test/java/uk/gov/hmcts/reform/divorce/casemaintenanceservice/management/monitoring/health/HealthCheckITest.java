package uk.gov.hmcts.reform.divorce.casemaintenanceservice.management.monitoring.health;

import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import com.jayway.jsonpath.JsonPath;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.CaseMaintenanceServiceApplication;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(
        classes = {CaseMaintenanceServiceApplication.class})
@PropertySource(value = "classpath:application.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class HealthCheckITest {

    private static final String HEALTH_UP_RESPONSE = "{ \"status\": \"UP\"}";
    private static final String HEALTH_DOWN_RESPONSE = "{ \"status\": \"DOWN\"}";

    @LocalServerPort
    private int port;

    @Value("${idam.s2s-auth.url}/health")
    private String serviceAuthHealthUrl;

    @Value("${case.formatter.service.api.baseurl}/health")
    private String caseFormatterServiceHealthUrl;

    @Value("${draft.store.api.baseurl}/health")
    private String draftStoreHealthUrl;

    @Value("${ccd.server.health.context-path}")
    private String ccdHealthContextPath;

    @Value("${idam.api.url}/health")
    private String idamApiHealthUrl;

    @Autowired
    @Qualifier("healthCheckRestTemplate")
    private RestTemplate restTemplate;

    @ClassRule
    public static WireMockClassRule ccdServer = new WireMockClassRule(4452);

    private String healthUrl;
    private MockRestServiceServer mockRestServiceServer;
    private ClientHttpRequestFactory originalRequestFactory;
    private final HttpClient httpClient = HttpClients.createMinimal();

    private HttpResponse getHealth() throws Exception {
        final HttpGet request = new HttpGet(healthUrl);
        request.addHeader("Accept", "application/json;charset=UTF-8");

        return httpClient.execute(request);
    }

    @Before
    public void setUp() {
        healthUrl = "http://localhost:" + port + "/health";
        originalRequestFactory = restTemplate.getRequestFactory();
        mockRestServiceServer = MockRestServiceServer.createServer(restTemplate);
    }

    @After
    public void tearDown() {
        restTemplate.setRequestFactory(originalRequestFactory);
    }

    @Test
    public void givenAllDependenciesAreUp_whenCheckHealth_thenReturnStatusUp() throws Exception {
        mockEndpointAndResponse(caseFormatterServiceHealthUrl, true);
        mockEndpointAndResponse(draftStoreHealthUrl, true);
        mockEndpointAndResponse(idamApiHealthUrl, true);
        mockEndpointAndResponse(serviceAuthHealthUrl, true);
        mockServiceCcdHealthCheck(true);

        HttpResponse response = getHealth();
        String body = EntityUtils.toString(response.getEntity());

        assertThat(response.getStatusLine().getStatusCode(), equalTo(200));
        assertThat(JsonPath.read(body, "$.status").toString(), equalTo("UP"));
        assertThat(JsonPath.read(body, "$.components.idamApiHealthCheck.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.components.serviceAuthProviderHealthCheck.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.components.draftStoreHealthCheck.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.components.caseFormatterServiceHealthCheck.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.components.coreCaseData.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.components.diskSpace.status").toString(), equalTo("UP"));
    }

    @Test
    public void givenAllDependenciesAreDown_whenCheckHealth_thenReturnStatusDown() throws Exception {
        mockEndpointAndResponse(caseFormatterServiceHealthUrl, false);
        mockEndpointAndResponse(draftStoreHealthUrl, false);
        mockEndpointAndResponse(idamApiHealthUrl, false);
        mockEndpointAndResponse(serviceAuthHealthUrl, false);
        mockServiceCcdHealthCheck(false);

        HttpResponse response = getHealth();
        String body = EntityUtils.toString(response.getEntity());

        assertThat(response.getStatusLine().getStatusCode(), equalTo(503));
        assertThat(JsonPath.read(body, "$.status").toString(), equalTo("DOWN"));
        assertThat(JsonPath.read(body, "$.components.idamApiHealthCheck.status").toString(),
            equalTo("DOWN"));
        assertThat(JsonPath.read(body, "$.components.serviceAuthProviderHealthCheck.status").toString(),
            equalTo("DOWN"));
        assertThat(JsonPath.read(body, "$.components.draftStoreHealthCheck.status").toString(),
            equalTo("DOWN"));
        assertThat(JsonPath.read(body, "$.components.caseFormatterServiceHealthCheck.status").toString(),
            equalTo("DOWN"));
        assertThat(JsonPath.read(body, "$.components.coreCaseData.status").toString(),
            equalTo("DOWN"));
        assertThat(JsonPath.read(body, "$.components.diskSpace.status").toString(), equalTo("UP"));
    }

    @Test
    public void givenIdamApiIsDown_whenCheckHealth_thenReturnStatusDown() throws Exception {
        mockEndpointAndResponse(caseFormatterServiceHealthUrl, true);
        mockEndpointAndResponse(draftStoreHealthUrl, true);
        mockEndpointAndResponse(idamApiHealthUrl, false);
        mockEndpointAndResponse(serviceAuthHealthUrl, true);
        mockServiceCcdHealthCheck(true);

        HttpResponse response = getHealth();
        String body = EntityUtils.toString(response.getEntity());

        assertThat(response.getStatusLine().getStatusCode(), equalTo(503));
        assertThat(JsonPath.read(body, "$.status").toString(), equalTo("DOWN"));
        assertThat(JsonPath.read(body, "$.components.idamApiHealthCheck.status").toString(),
            equalTo("DOWN"));
        assertThat(JsonPath.read(body, "$.components.serviceAuthProviderHealthCheck.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.components.draftStoreHealthCheck.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.components.caseFormatterServiceHealthCheck.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.components.coreCaseData.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.components.diskSpace.status").toString(), equalTo("UP"));
    }

    @Test
    public void givenAuthServiceIsDown_whenCheckHealth_thenReturnStatusDown() throws Exception {
        mockEndpointAndResponse(caseFormatterServiceHealthUrl, true);
        mockEndpointAndResponse(draftStoreHealthUrl, true);
        mockEndpointAndResponse(idamApiHealthUrl, true);
        mockEndpointAndResponse(serviceAuthHealthUrl, false);
        mockServiceCcdHealthCheck(true);

        HttpResponse response = getHealth();
        String body = EntityUtils.toString(response.getEntity());

        assertThat(response.getStatusLine().getStatusCode(), equalTo(503));
        assertThat(JsonPath.read(body, "$.status").toString(), equalTo("DOWN"));
        assertThat(JsonPath.read(body, "$.components.idamApiHealthCheck.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.components.serviceAuthProviderHealthCheck.status").toString(),
            equalTo("DOWN"));
        assertThat(JsonPath.read(body, "$.components.draftStoreHealthCheck.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.components.caseFormatterServiceHealthCheck.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.components.coreCaseData.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.components.diskSpace.status").toString(), equalTo("UP"));
    }

    @Test
    public void givenCcdIsDown_whenCheckHealth_thenReturnStatusDown() throws Exception {
        mockEndpointAndResponse(caseFormatterServiceHealthUrl, true);
        mockEndpointAndResponse(draftStoreHealthUrl, true);
        mockEndpointAndResponse(idamApiHealthUrl, true);
        mockEndpointAndResponse(serviceAuthHealthUrl, true);
        mockServiceCcdHealthCheck(false);

        HttpResponse response = getHealth();
        String body = EntityUtils.toString(response.getEntity());

        assertThat(response.getStatusLine().getStatusCode(), equalTo(503));
        assertThat(JsonPath.read(body, "$.status").toString(), equalTo("DOWN"));
        assertThat(JsonPath.read(body, "$.components.idamApiHealthCheck.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.components.serviceAuthProviderHealthCheck.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.components.draftStoreHealthCheck.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.components.caseFormatterServiceHealthCheck.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.components.coreCaseData.status").toString(),
            equalTo("DOWN"));
        assertThat(JsonPath.read(body, "$.components.diskSpace.status").toString(), equalTo("UP"));
    }

    @Test
    public void givenCaseFormatterServiceIsDown_whenCheckHealth_thenReturnStatusDown() throws Exception {
        mockEndpointAndResponse(caseFormatterServiceHealthUrl, false);
        mockEndpointAndResponse(draftStoreHealthUrl, true);
        mockEndpointAndResponse(idamApiHealthUrl, true);
        mockEndpointAndResponse(serviceAuthHealthUrl, true);
        mockServiceCcdHealthCheck(true);

        HttpResponse response = getHealth();
        String body = EntityUtils.toString(response.getEntity());

        assertThat(response.getStatusLine().getStatusCode(), equalTo(503));
        assertThat(JsonPath.read(body, "$.status").toString(), equalTo("DOWN"));
        assertThat(JsonPath.read(body, "$.components.idamApiHealthCheck.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.components.serviceAuthProviderHealthCheck.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.components.draftStoreHealthCheck.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.components.caseFormatterServiceHealthCheck.status").toString(),
            equalTo("DOWN"));
        assertThat(JsonPath.read(body, "$.components.coreCaseData.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.components.diskSpace.status").toString(), equalTo("UP"));
    }

    @Test
    public void givenDraftStoreIsDown_whenCheckHealth_thenReturnStatusDown() throws Exception {
        mockEndpointAndResponse(caseFormatterServiceHealthUrl, true);
        mockEndpointAndResponse(draftStoreHealthUrl, false);
        mockEndpointAndResponse(idamApiHealthUrl, true);
        mockEndpointAndResponse(serviceAuthHealthUrl, true);
        mockServiceCcdHealthCheck(true);

        HttpResponse response = getHealth();
        String body = EntityUtils.toString(response.getEntity());

        assertThat(response.getStatusLine().getStatusCode(), equalTo(503));
        assertThat(JsonPath.read(body, "$.status").toString(), equalTo("DOWN"));
        assertThat(JsonPath.read(body, "$.components.idamApiHealthCheck.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.components.serviceAuthProviderHealthCheck.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.components.draftStoreHealthCheck.status").toString(),
            equalTo("DOWN"));
        assertThat(JsonPath.read(body, "$.components.caseFormatterServiceHealthCheck.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.components.coreCaseData.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.components.diskSpace.status").toString(), equalTo("UP"));
    }

    private void mockEndpointAndResponse(String requestUrl, boolean serviceUp) {
        mockRestServiceServer.expect(once(), requestTo(requestUrl)).andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(serviceUp ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE)
                        .body(serviceUp ? HEALTH_UP_RESPONSE : HEALTH_DOWN_RESPONSE)
                        .contentType(APPLICATION_JSON_UTF8));
    }

    private void mockServiceCcdHealthCheck(boolean serviceUp) {
        ccdServer.stubFor(get(ccdHealthContextPath)
            .willReturn(aResponse()
                .withStatus(serviceUp ? HttpStatus.OK.value() : HttpStatus.SERVICE_UNAVAILABLE.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                .withBody(serviceUp ? HEALTH_UP_RESPONSE : HEALTH_DOWN_RESPONSE)));
    }
}
