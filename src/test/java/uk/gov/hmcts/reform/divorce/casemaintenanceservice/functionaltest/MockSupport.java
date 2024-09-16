package uk.gov.hmcts.reform.divorce.casemaintenanceservice.functionaltest;

import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import feign.FeignException;
import org.junit.ClassRule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.contract.wiremock.WireMockSpring;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.idam.AuthenticateUserResponse;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.idam.TokenExchangeResponse;
import uk.gov.hmcts.reform.idam.client.IdamClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CmsConstants.CASEWORKER_ROLE;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CmsConstants.CITIZEN_ROLE;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.functionaltest.ObjectMapperTestUtil.convertObjectToJsonString;

public abstract class MockSupport {
    private static final String IDAM_USER_DETAILS_CONTEXT_PATH = "/details";
    private static final String IDAM_EXCHANGE_CODE_CONTEXT_PATH = "/oauth2/token";
    public static final String TEST_CODE = "test.code";
    private static final String IDAM_AUTHORIZE_CONTEXT_PATH = "/oauth2/authorize";
    private static final AuthenticateUserResponse AUTHENTICATE_USER_RESPONSE =
        AuthenticateUserResponse.builder().code(TEST_CODE).build();

    static final String USER_ID = "1";
    static final String CASE_WORKER_USER_ID = "2";
    static final String ENCRYPTED_USER_ID = "OVZRS2hJRDg2MUFkeFdXdjF6bElfMQ==";
    static final String FEIGN_ERROR = "some error message";

    private static final String BEARER = "Bearer";

    private static final String CASEWORKER_BASIC_AUTH_HEADER =
        "Basic ZHVtbXljYXNld29ya2VyQHRlc3QuY29tOmR1bW15";

    static final String USER_TOKEN = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJqdGkiOiIwOTg3NjU0M"
        + "yIsInN1YiI6IjEwMCIsImlhdCI6MTUwODk0MDU3MywiZXhwIjoxNTE5MzAzNDI3LCJkYXRhIjoiY2l0aXplbiIsInR5cGUiOiJBQ0NFU1MiL"
        + "CJpZCI6IjEwMCIsImZvcmVuYW1lIjoiSm9obiIsInN1cm5hbWUiOiJEb2UiLCJkZWZhdWx0LXNlcnZpY2UiOiJEaXZvcmNlIiwibG9hIjoxL"
        + "CJkZWZhdWx0LXVybCI6Imh0dHBzOi8vd3d3Lmdvdi51ayIsImdyb3VwIjoiZGl2b3JjZSJ9.lkNr1vpAP5_Gu97TQa0cRtHu8I-QESzu8kMX"
        + "CJOQrVU";

    private static final String CASE_WORKER_TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJqdGkiOiIwOTg3NjU0M"
        + "yIsInN1YiI6IjEwMCIsImlhdCI6MTUwODk0MDU3MywiZXhwIjoxNTE5MzAzNDI3LCJkYXRhIjoiY2l0aXplbiIsInR5cGUiOiJBQ0NFU1MiL"
        + "CJpZCI6IjEwMCIsImZvcmVuYW1lIjoiSm9obiIsInN1cm5hbWUiOiJEb2UiLCJkZWZhdWx0LXNlcnZpY2UiOiJEaXZvcmNlIiwibG9hIjoxL"
        + "CJkZWZhdWx0LXVybCI6Imh0dHBzOi8vd3d3Lmdvdi51ayIsImdyb3VwIjoiZGl2b3JjZSJ9.lkNr1vpAP5_Gu97TQa0cRtHu8I-QESzu8kMX"
        + "CJOQrMM";

    private static final TokenExchangeResponse TOKEN_EXCHANGE_RESPONSE =
            TokenExchangeResponse.builder()
                    .accessToken(CASE_WORKER_TOKEN)
                    .build();

    static final String BEARER_CASE_WORKER_TOKEN = BEARER + " " + CASE_WORKER_TOKEN;

    private static final String CASE_WORKER_AUTH_CODE = "AuthCode";
    private static final String SOLICITOR_ROLE = "caseworker-divorce-solicitor";

    @Value("${idam.client.redirect_uri}")
    private String authRedirectUrl;

    @Value("${idam.client.id}")
    private String authClientId;

    @Value("${idam.client.secret}")
    private String authClientSecret;

    @ClassRule
    public static WireMockClassRule idamUserDetailsServer = new WireMockClassRule(
        WireMockSpring
            .options()
            .port(4503)
            .jettyStopTimeout(20L)
            .extensions(new ConnectionCloseExtension())
    );

    @ClassRule
    public static WireMockClassRule draftStoreServer = new WireMockClassRule(
        WireMockSpring
            .options()
            .port(4601)
            .extensions(new ConnectionCloseExtension())
    );

    @ClassRule
    public static WireMockClassRule caseFormatterServer = new WireMockClassRule(
        WireMockSpring
            .options()
            .port(4011)
            .extensions(new ConnectionCloseExtension())
    );

    @MockBean
    AuthTokenGenerator serviceTokenGenerator;

    void stubUserDetailsEndpoint(HttpStatus status, StringValuePattern authHeader, String message) {
        idamUserDetailsServer.stubFor(get(IDAM_USER_DETAILS_CONTEXT_PATH)
            .withHeader(AUTHORIZATION, authHeader)
            .willReturn(aResponse()
                .withStatus(status.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(message)));
    }

    private static final String APP_FORM_DATA_UTF8_HEADER = MediaType.APPLICATION_FORM_URLENCODED_VALUE + "; charset=UTF-8";
    void stubCaseWorkerAuthentication(HttpStatus status) {
        stubSignInForCaseworker(status);

        stubUserDetailsEndpoint(HttpStatus.OK, new EqualToPattern(BEARER_CASE_WORKER_TOKEN),
            getCaseWorkerUserDetails());
    }

    void stubCaseworkerAuthoriseEndpoint(HttpStatus status, String responseBody) {
        idamUserDetailsServer.stubFor(post(IDAM_AUTHORIZE_CONTEXT_PATH)
                .withHeader(AUTHORIZATION, new EqualToPattern(CASEWORKER_BASIC_AUTH_HEADER))
                .withHeader(CONTENT_TYPE, new EqualToPattern(APP_FORM_DATA_UTF8_HEADER))
                .willReturn(aResponse()
                    .withStatus(status.value())
                    .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                    .withBody(responseBody)));
    }

    private void stubTokenExchangeEndpoint(String responseBody) {
        idamUserDetailsServer.stubFor(post(IDAM_EXCHANGE_CODE_CONTEXT_PATH)
                .withHeader(CONTENT_TYPE, new EqualToPattern(APP_FORM_DATA_UTF8_HEADER))
                .willReturn(aResponse()
                    .withStatus(HttpStatus.OK.value())
                    .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                    .withBody(responseBody)));
    }

    void stubSignInForCaseworker(HttpStatus status) {
        try {
            stubCaseworkerAuthoriseEndpoint(status, convertObjectToJsonString(AUTHENTICATE_USER_RESPONSE));
            stubTokenExchangeEndpoint(convertObjectToJsonString(TOKEN_EXCHANGE_RESPONSE));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        stubUserDetailsEndpoint(HttpStatus.OK, new EqualToPattern(BEARER_CASE_WORKER_TOKEN),
                getCaseWorkerUserDetails());
    }

    String getCaseWorkerUserDetails() {
        return getUserDetailsForRole(CASE_WORKER_USER_ID, CASEWORKER_ROLE);
    }

    String getUserDetails() {
        return getUserDetailsForRole(USER_ID, CITIZEN_ROLE);
    }

    String getSolicitorUserDetails() {
        return getUserDetailsForRole(USER_ID, SOLICITOR_ROLE);
    }

    private String getUserDetailsForRole(String userId, String role) {
        return "{\"id\":\"" + userId
            + "\",\"email\":\"" + TEST_USER_EMAIL
            + "\",\"forename\":\"forename\",\"surname\":\"Surname\",\"roles\":[\"" + role + "\"]}";
    }

    private String tokenBody() {
        return "code=" + CASE_WORKER_AUTH_CODE
            + "&grant_type=" + IdamClient.GRANT_TYPE
            + "&redirect_uri=" + getRedirectUri()
            + "&client_secret=" + authClientSecret
            + "&client_id=" + authClientId;
    }

    private String getRedirectUri() {
        try {
            return URLEncoder.encode(authRedirectUrl, StandardCharsets.UTF_8.name());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    FeignException getMockedFeignException(int statusCode) {
        final FeignException feignException = mock(FeignException.class);

        when(feignException.status()).thenReturn(statusCode);
        when(feignException.getMessage()).thenReturn(FEIGN_ERROR);

        return feignException;
    }
}
