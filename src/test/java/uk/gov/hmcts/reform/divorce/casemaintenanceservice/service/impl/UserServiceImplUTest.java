package uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.client.IdamApiClient;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.AuthenticateUserResponse;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.TokenExchangeResponse;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.UserDetails;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceImplUTest {
    private static final String BEARER =
        (String)ReflectionTestUtils.getField(UserServiceImpl.class, "BEARER");
    private static final String AUTHORIZATION_CODE =
        (String)ReflectionTestUtils.getField(UserServiceImpl.class, "AUTHORIZATION_CODE");
    private static final String CODE =
        (String)ReflectionTestUtils.getField(UserServiceImpl.class, "CODE");

    private static final String CITIZEN_AUTHORISATION = "auth token";
    private static final String CASEWORKER_BASIC_AUTH_HEADER =
        "Basic Y2FzZXdvcmtlciB1c2VyIG5hbWU6Y2FzZXdvcmtlciBwYXNzd29yZA==";

    private static final String AUTH_REDIRECT_URL = "Redirect url";
    private static final String AUTH_CLIENT_ID = "auth client id";
    private static final String AUTH_CLIENT_SECRET = "auth client secret";
    private static final String CASEWORKER_USER_NAME = "caseworker user name";
    private static final String CASEWORKER_PASSWORD = "caseworker password";

    @Mock
    private IdamApiClient idamApiClient;

    @InjectMocks
    private UserServiceImpl classUnderTest;

    @Before
    public void setup() {
        ReflectionTestUtils.setField(classUnderTest, "authRedirectUrl", AUTH_REDIRECT_URL);
        ReflectionTestUtils.setField(classUnderTest, "authClientId", AUTH_CLIENT_ID);
        ReflectionTestUtils.setField(classUnderTest, "authClientSecret", AUTH_CLIENT_SECRET);
        ReflectionTestUtils.setField(classUnderTest, "caseworkerUserName", CASEWORKER_USER_NAME);
        ReflectionTestUtils.setField(classUnderTest, "caseworkerPassword", CASEWORKER_PASSWORD);
    }

    @Test
    public void givenUserExists_whenRetrieveUserDetails_thenReturnUserDetails() {
        final UserDetails expected = UserDetails.builder().id("1").build();

        when(idamApiClient.retrieveUserDetails(CITIZEN_AUTHORISATION)).thenReturn(expected);

        UserDetails actual = classUnderTest.retrieveUserDetails(CITIZEN_AUTHORISATION);

        assertEquals(expected, actual);
        assertEquals(CITIZEN_AUTHORISATION, actual.getAuthToken());

        verify(idamApiClient).retrieveUserDetails(CITIZEN_AUTHORISATION);
    }

    @Test
    public void givenCorrectCredentials_whenRetrieveAnonymousCaseWorkerDetails_thenReturnReturnCaseWorkerDetails() {
        final String code = "code";
        final String authToken = "auth token";
        final String bearerAuthToken = BEARER + authToken;
        final AuthenticateUserResponse authenticateUserResponse = new AuthenticateUserResponse();
        authenticateUserResponse.setCode(code);

        final TokenExchangeResponse tokenExchangeResponse = new TokenExchangeResponse();
        tokenExchangeResponse.setAccessToken(authToken);

        final UserDetails expected = UserDetails.builder().id("2").build();

        when(idamApiClient.retrieveUserDetails(bearerAuthToken)).thenReturn(expected);

        when(idamApiClient.authenticateUser(
            CASEWORKER_BASIC_AUTH_HEADER,
            CODE,
            AUTH_CLIENT_ID,
            AUTH_REDIRECT_URL
        )).thenReturn(authenticateUserResponse);

        when(idamApiClient.exchangeCode(
            code,
            AUTHORIZATION_CODE,
            AUTH_REDIRECT_URL,
            AUTH_CLIENT_ID,
            AUTH_CLIENT_SECRET
        )).thenReturn(tokenExchangeResponse);

        UserDetails actual = classUnderTest.retrieveAnonymousCaseWorkerDetails();

        assertEquals(expected, actual);
        assertEquals(bearerAuthToken, actual.getAuthToken());

        verify(idamApiClient).retrieveUserDetails(bearerAuthToken);

        verify(idamApiClient).authenticateUser(
            CASEWORKER_BASIC_AUTH_HEADER,
            CODE,
            AUTH_CLIENT_ID,
            AUTH_REDIRECT_URL
        );

        verify(idamApiClient).exchangeCode(
            code,
            AUTHORIZATION_CODE,
            AUTH_REDIRECT_URL,
            AUTH_CLIENT_ID,
            AUTH_CLIENT_SECRET
        );
    }
}
