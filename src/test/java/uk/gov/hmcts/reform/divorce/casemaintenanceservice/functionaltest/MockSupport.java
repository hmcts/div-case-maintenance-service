package uk.gov.hmcts.reform.divorce.casemaintenanceservice.functionaltest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import feign.FeignException;
import org.junit.ClassRule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.contract.wiremock.WireMockSpring;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.AuthenticateUserResponse;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

public abstract class MockSupport {
    private static final String IDAM_USER_DETAILS_CONTEXT_PATH = "/details";
    private static final String IDAM_USER_AUTHENTICATE_CONTEXT_PATH = "/oauth2/authorize";
    private static final String IDAM_USER_AUTH_TOKEN_CONTEXT_PATH = "/oauth2/token";

    static final String USER_ID = "1";
    static final String CASE_WORKER_USER_ID = "2";
    static final String USER_EMAIL = "test@test.com";
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

    static final String BEARER_CASE_WORKER_TOKEN = BEARER + " " + CASE_WORKER_TOKEN;

    private static final String CASE_WORKER_AUTH_CODE = "AuthCode";
    private static final String CITIZEN_ROLE = "citizen";
    private static final String SOLICITOR_ROLE = "caseworker-divorce-solicitor";
    private static final String CASEWORKER_ROLE = "caseworker";

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
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                .withBody(message)));
    }

    void stubCaseWorkerAuthentication(HttpStatus status) {
        idamUserDetailsServer.stubFor(
            post(IDAM_USER_AUTHENTICATE_CONTEXT_PATH)
                .withHeader(AUTHORIZATION, new EqualToPattern(CASEWORKER_BASIC_AUTH_HEADER))
                .withHeader(CONTENT_TYPE, new EqualToPattern("application/x-www-form-urlencoded; charset=UTF-8"))
                .withRequestBody(new EqualToPattern(authorisedBody()))
                .willReturn(aResponse()
                    .withStatus(status.value())
                    .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                    .withBody(ObjectMapperTestUtil.convertObjectToJsonString(new AuthenticateUserResponse(CASE_WORKER_AUTH_CODE)))
                )
        );

        idamUserDetailsServer.stubFor(
            post(IDAM_USER_AUTH_TOKEN_CONTEXT_PATH)
                .withRequestBody(new EqualToPattern(tokenBody()))
                .willReturn(aResponse()
                    .withStatus(HttpStatus.OK.value())
                    .withBody("{ \"access_token\" : \"" + CASE_WORKER_TOKEN + "\" }")));

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
            + "\",\"email\":\"" + USER_EMAIL
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

    private String authorisedBody() {
        return "response_type=code&redirect_uri=" + getRedirectUri() + "&client_id=" + authClientId;
    }

    FeignException getMockedFeignException(int statusCode) {
        final FeignException feignException = mock(FeignException.class);

        when(feignException.status()).thenReturn(statusCode);
        when(feignException.getMessage()).thenReturn(FEIGN_ERROR);

        return feignException;
    }
}
