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
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

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

    @Value("${ccd.server.health.context-path}")
    private String ccdHealthContextPath;

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
        mockServiceCcdHealthCheck(true);

        HttpResponse response = getHealth();
        String body = EntityUtils.toString(response.getEntity());

        assertThat(response.getStatusLine().getStatusCode(), equalTo(200));
        assertThat(JsonPath.read(body, "$.status").toString(), equalTo("UP"));
        assertThat(JsonPath.read(body, "$.components.coreCaseData.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.components.diskSpace.status").toString(), equalTo("UP"));
    }

    @Test
    public void givenAllDependenciesAreDown_whenCheckHealth_thenReturnStatusDown() throws Exception {
        mockServiceCcdHealthCheck(false);

        HttpResponse response = getHealth();
        String body = EntityUtils.toString(response.getEntity());

        assertThat(response.getStatusLine().getStatusCode(), equalTo(503));
        assertThat(JsonPath.read(body, "$.status").toString(), equalTo("DOWN"));
        assertThat(JsonPath.read(body, "$.components.coreCaseData.status").toString(),
            equalTo("DOWN"));
        assertThat(JsonPath.read(body, "$.components.diskSpace.status").toString(), equalTo("UP"));
    }

    @Test
    public void givenCcdIsDown_whenCheckHealth_thenReturnStatusDown() throws Exception {
        mockServiceCcdHealthCheck(false);

        HttpResponse response = getHealth();
        String body = EntityUtils.toString(response.getEntity());

        assertThat(response.getStatusLine().getStatusCode(), equalTo(503));
        assertThat(JsonPath.read(body, "$.status").toString(), equalTo("DOWN"));
        assertThat(JsonPath.read(body, "$.components.coreCaseData.status").toString(),
            equalTo("DOWN"));
        assertThat(JsonPath.read(body, "$.components.diskSpace.status").toString(), equalTo("UP"));
    }

    private void mockServiceCcdHealthCheck(boolean serviceUp) {
        ccdServer.stubFor(get(ccdHealthContextPath)
            .willReturn(aResponse()
                .withStatus(serviceUp ? HttpStatus.OK.value() : HttpStatus.SERVICE_UNAVAILABLE.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(serviceUp ? HEALTH_UP_RESPONSE : HEALTH_DOWN_RESPONSE)));
    }
}
