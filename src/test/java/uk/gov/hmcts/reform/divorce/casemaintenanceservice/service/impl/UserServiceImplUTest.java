package uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.TestConstants.TEST_AUTH_TOKEN;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceImplUTest {
    private static final String CASEWORKER_USER_NAME = "caseworker user name";
    private static final String CASEWORKER_PASSWORD = "caseworker password";

    @Mock
    private IdamClient idamClient;

    @InjectMocks
    private UserServiceImpl classUnderTest;

    @Before
    public void setup() {
        ReflectionTestUtils.setField(classUnderTest, "caseworkerUserName", CASEWORKER_USER_NAME);
        ReflectionTestUtils.setField(classUnderTest, "caseworkerPassword", CASEWORKER_PASSWORD);
    }

    @Test
    public void givenUserExists_whenRetrieveUserDetails_thenReturnUserDetails() {
        UserDetails userDetails = UserDetails.builder().build();
        final User expected = new User(TEST_AUTH_TOKEN, userDetails);

        when(idamClient.getUserDetails(TEST_AUTH_TOKEN)).thenReturn(userDetails);

        User actual = classUnderTest.retrieveUser(TEST_AUTH_TOKEN);

        assertEquals(expected.getUserDetails(), actual.getUserDetails());
        assertEquals(TEST_AUTH_TOKEN, actual.getAuthToken());

        verify(idamClient).getUserDetails(TEST_AUTH_TOKEN);
    }

    @Test
    public void givenCorrectCredentials_whenRetrieveAnonymousCaseWorkerDetails_thenReturnReturnCaseWorkerDetails() {
        final String bearerAuthToken = IdamClient.BEARER_AUTH_TYPE + " " + TEST_AUTH_TOKEN;

        final UserDetails userDetails = UserDetails.builder().id("2").build();
        final User expected = new User(bearerAuthToken, userDetails);

        when(idamClient.authenticateUser(CASEWORKER_USER_NAME, CASEWORKER_PASSWORD)).thenReturn(bearerAuthToken);
        when(idamClient.getUserDetails(bearerAuthToken)).thenReturn(userDetails);

        User actual = classUnderTest.retrieveAnonymousCaseWorkerDetails();

        assertEquals(expected.getUserDetails(), actual.getUserDetails());
        assertEquals(bearerAuthToken, actual.getAuthToken());

        verify(idamClient).getUserDetails(bearerAuthToken);
        verify(idamClient).authenticateUser(CASEWORKER_USER_NAME, CASEWORKER_PASSWORD);
    }
}
