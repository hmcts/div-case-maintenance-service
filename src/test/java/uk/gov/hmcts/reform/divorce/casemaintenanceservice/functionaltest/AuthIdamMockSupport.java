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
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.AuthenticateUserResponse;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.TokenExchangeResponse;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.UserDetails;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.impl.UserServiceImpl;

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
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

public class AuthIdamMockSupport {
    private static final String IDAM_USER_DETAILS_CONTEXT_PATH = "/details";
    private static final String IDAM_USER_AUTHENTICATE_CONTEXT_PATH = "/oauth2/authorize";
    private static final String IDAM_USER_AUTH_TOKEN_CONTEXT_PATH = "/oauth2/token";

    static final String USER_ID = "1";
    static final String CASE_WORKER_USER_ID = "2";
    static final String ENCRYPTED_USER_ID = "OVZRS2hJRDg2MUFkeFdXdjF6bElfMQ==";
    static final String FEIGN_ERROR = "some error message";

    private static final String BEARER = (String)ReflectionTestUtils.getField(UserServiceImpl.class, "BEARER");
    private static final String AUTHORIZATION_CODE =
        (String)ReflectionTestUtils.getField(UserServiceImpl.class, "AUTHORIZATION_CODE");
    private static final String CODE = (String)ReflectionTestUtils.getField(UserServiceImpl.class, "CODE");

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

    static final String BEARER_CASE_WORKER_TOKEN = BEARER + CASE_WORKER_TOKEN;

    private static final String CASE_WORKER_AUTH_CODE = "AuthCode";
    private static final String CITIZEN_ROLE = "citizen";
    private static final String CASEWORKER_ROLE = "caseworker";

    private final AuthenticateUserResponse authenticateUserResponse =
        getAuthenticateUserResponse();
    private final TokenExchangeResponse tokenExchangeResponse = getTokenExchangeResponse();


    @Value("${idam.api.redirect-url}")
    private String authRedirectUrl;

    @Value("${auth2.client.id}")
    private String authClientId;

    @Value("${auth2.client.secret}")
    private String authClientSecret;

    @ClassRule
    public static WireMockClassRule idamUserDetailsServer = new WireMockClassRule(WireMockSpring.options().port(4503));

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
        try {
            idamUserDetailsServer.stubFor(post(IDAM_USER_AUTHENTICATE_CONTEXT_PATH
                + "?response_type=" + CODE
                + "&client_id=" + authClientId
                + "&redirect_uri=" + URLEncoder.encode(authRedirectUrl, StandardCharsets.UTF_8.name()))
                .withHeader(AUTHORIZATION, new EqualToPattern(CASEWORKER_BASIC_AUTH_HEADER))
                .withHeader(CONTENT_TYPE, new EqualToPattern(MediaType.APPLICATION_JSON_VALUE))
                .willReturn(aResponse()
                    .withStatus(status.value())
                    .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                    .withBody(ObjectMapperTestUtil.convertObjectToJsonString(authenticateUserResponse))));

            idamUserDetailsServer.stubFor(post(IDAM_USER_AUTH_TOKEN_CONTEXT_PATH
                + "?code=" + CASE_WORKER_AUTH_CODE
                + "&grant_type=" + AUTHORIZATION_CODE
                + "&redirect_uri=" + URLEncoder.encode(authRedirectUrl, StandardCharsets.UTF_8.name())
                + "&client_id=" + authClientId
                + "&client_secret=" + authClientSecret)
                .willReturn(aResponse()
                    .withStatus(HttpStatus.OK.value())
                    .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                    .withBody(ObjectMapperTestUtil.convertObjectToJsonString(tokenExchangeResponse))));

        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        stubUserDetailsEndpoint(HttpStatus.OK, new EqualToPattern(BEARER_CASE_WORKER_TOKEN),
            getCaseWorkerUserDetails());
    }

    String getCaseWorkerUserDetails() {
        return getUserDetails(CASE_WORKER_USER_ID, CASE_WORKER_TOKEN, CASEWORKER_ROLE);
    }

    String getUserDetails() {
        return getUserDetails(USER_ID, USER_TOKEN, CITIZEN_ROLE);
    }

    private String getUserDetails(String userId, String authToken, String role) {
        try {
            return new ObjectMapper().writeValueAsString(
                UserDetails.builder()
                    .id(userId)
                    .authToken(authToken)
                    .email("test@test.com")
                    .forename("forename")
                    .surname("surname")
                    .roles(Collections.singletonList(role))
                    .build());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    FeignException getMockedFeignException(int statusCode) {
        final FeignException feignException = mock(FeignException.class);

        when(feignException.status()).thenReturn(statusCode);
        when(feignException.getMessage()).thenReturn(FEIGN_ERROR);

        return feignException;
    }

    private AuthenticateUserResponse getAuthenticateUserResponse() {
        AuthenticateUserResponse authenticateUserResponse = new AuthenticateUserResponse();
        authenticateUserResponse.setCode(CASE_WORKER_AUTH_CODE);

        return  authenticateUserResponse;
    }

    private TokenExchangeResponse getTokenExchangeResponse() {
        TokenExchangeResponse tokenExchangeResponse = new TokenExchangeResponse();
        tokenExchangeResponse.setAccessToken(CASE_WORKER_TOKEN);

        return tokenExchangeResponse;
    }
}
