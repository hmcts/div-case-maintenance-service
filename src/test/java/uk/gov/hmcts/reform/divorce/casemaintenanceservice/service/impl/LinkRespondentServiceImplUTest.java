package uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.impl;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAccessApi;
import uk.gov.hmcts.reform.ccd.client.CaseUserApi;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.CaseUser;
import uk.gov.hmcts.reform.ccd.client.model.UserId;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CaseState;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.UserDetails;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.exception.CaseNotFoundException;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.exception.InvalidRequestException;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.exception.UnauthorizedException;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.UserService;

import java.util.Collections;
import java.util.Objects;

import static org.junit.rules.ExpectedException.none;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties.CO_RESP_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties.CO_RESP_LETTER_HOLDER_ID_FIELD;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties.RESP_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties.RESP_LETTER_HOLDER_ID_FIELD;

@RunWith(MockitoJUnitRunner.class)
public class LinkRespondentServiceImplUTest {
    private static final String JURISDICTION_ID = "DIVORCE";
    private static final String CASE_TYPE = "DIVORCE";

    private static final String RESPONDENT_AUTHORISATION = "Bearer RespondentAuthToken";
    private static final String CASEWORKER_AUTHORISATION = "CaseWorkerAuthToken";
    private static final String CASE_ID = "12345678";
    private static final String LETTER_HOLDER_ID = "letterholderId";
    private static final String CASEWORKER_USER_ID = "1";
    private static final String RESPONDENT_USER_ID = "2";
    private static final String USER_EMAIL = "user@email.com";
    private static final String SERVICE_TOKEN = "ServiceToken";
    private static final String RESPONDENT_EMAIL = "aos@respondent.com";
    private static final String UNAUTHORIZED_MESSAGE =
        "Case with caseId [12345678] and letter holder id [letterholderId] already assigned or letter holder mismatch";
    private static final String UNAUTHORIZED_MESSAGE_WRONG_HOLDER_ID =
        "Case with caseId [12345678] and letter holder id [WrongHolderId] already assigned or letter holder mismatch";
    private static final String INVALID_MESSAGE = "Case details or letter holder data are invalid";
    private static final String NOT_FOUND_MESSAGE = "Case with caseId [12345678] and letter holder id [letterholderId] not found";

    private static final UserDetails CASE_WORKER_USER = UserDetails.builder()
        .authToken(CASEWORKER_AUTHORISATION)
        .id(CASEWORKER_USER_ID)
        .build();

    private static final UserDetails RESPONDENT_USER = UserDetails.builder()
        .authToken(RESPONDENT_AUTHORISATION)
        .id(RESPONDENT_USER_ID)
        .email(USER_EMAIL)
        .build();

    @Rule
    public ExpectedException expectedException = none();

    @Mock
    private CoreCaseDataApi coreCaseDataApi;

    @Mock
    private UserService userService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private CaseAccessApi caseAccessApi;

    @Mock
    private CaseUserApi caseUserApi;

    @InjectMocks
    private CcdAccessServiceImpl classUnderTest;

    @Before
    public void setup() {
        ReflectionTestUtils.setField(classUnderTest, "jurisdictionId", JURISDICTION_ID);
        ReflectionTestUtils.setField(classUnderTest, "caseType", CASE_TYPE);

        when(userService.retrieveUserDetails(RESPONDENT_AUTHORISATION)).thenReturn(RESPONDENT_USER);
        when(userService.retrieveAnonymousCaseWorkerDetails()).thenReturn(CASE_WORKER_USER);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
    }

    @Test
    public void givenNoCaseFound_whenLinkRespondent_thenThrowCaseNotFoundException() {
        expectedException.expect(CaseNotFoundException.class);
        expectedException
            .expectMessage(NOT_FOUND_MESSAGE);

        mockCaseDetails(null);

        classUnderTest.linkRespondent(RESPONDENT_AUTHORISATION, CASE_ID, LETTER_HOLDER_ID);
    }

    @Test
    public void givenNoCaseCaseDataFound_whenLinkRespondent_thenThrowCaseNotFoundException() {
        expectedException.expect(InvalidRequestException.class);
        expectedException
            .expectMessage(INVALID_MESSAGE);
        CaseDetails caseDetails = CaseDetails.builder().build();

        mockCaseDetails(caseDetails);

        classUnderTest.linkRespondent(RESPONDENT_AUTHORISATION, CASE_ID, LETTER_HOLDER_ID);
    }

    @Test
    public void givenLetterHolderIdIsNull_whenLinkRespondent_thenThrowInvalidRequestException() {
        expectedException.expect(InvalidRequestException.class);
        expectedException
            .expectMessage(INVALID_MESSAGE);
        CaseDetails caseDetails = CaseDetails.builder()
            .state(CaseState.AOS_AWAITING.getValue())
            .data(Collections.singletonMap(
                RESP_LETTER_HOLDER_ID_FIELD, LETTER_HOLDER_ID
            )).build();

        mockCaseDetails(caseDetails);

        classUnderTest.linkRespondent(RESPONDENT_AUTHORISATION, CASE_ID, null);
    }

    @Test
    public void givenLetterHolderIdIsBlank_whenLinkRespondent_thenThrowInvalidRequestException() {
        expectedException.expect(InvalidRequestException.class);
        expectedException
            .expectMessage(INVALID_MESSAGE);
        CaseDetails caseDetails = CaseDetails.builder()
            .data(Collections.singletonMap(
                RESP_LETTER_HOLDER_ID_FIELD, LETTER_HOLDER_ID
            )).build();

        mockCaseDetails(caseDetails);

        classUnderTest.linkRespondent(RESPONDENT_AUTHORISATION, CASE_ID, " ");
    }

    @Test
    public void givenLetterHolderIdInCaseIsNull_whenLinkRespondent_thenThrowUnauthorizedException() {
        expectedException.expect(UnauthorizedException.class);
        expectedException
            .expectMessage(UNAUTHORIZED_MESSAGE);
        CaseDetails caseDetails = CaseDetails.builder()
            .data(Collections.emptyMap()).build();

        mockCaseDetails(caseDetails);

        classUnderTest.linkRespondent(RESPONDENT_AUTHORISATION, CASE_ID, LETTER_HOLDER_ID);
    }

    @Test
    public void givenLetterHolderIdsDoNotMatch_whenLinkRespondent_thenThrowUnauthorizedException() {
        expectedException.expect(UnauthorizedException.class);
        expectedException
            .expectMessage(UNAUTHORIZED_MESSAGE_WRONG_HOLDER_ID);

        CaseDetails caseDetails = CaseDetails.builder()
            .state(CaseState.AOS_AWAITING.getValue())
            .id(Long.decode(CASE_ID))
            .data(Collections.singletonMap(
                RESP_LETTER_HOLDER_ID_FIELD, LETTER_HOLDER_ID
            )).build();

        mockCaseDetails(caseDetails);

        classUnderTest.linkRespondent(
            RESPONDENT_AUTHORISATION, CASE_ID, "WrongHolderId");
    }

    @Test
    public void givenLetterHolderIdsDoNotMatch_whenLinkCoRespondent_thenThrowUnauthorizedException() {
        expectedException.expect(UnauthorizedException.class);
        expectedException
            .expectMessage(UNAUTHORIZED_MESSAGE_WRONG_HOLDER_ID);
        CaseDetails caseDetails = CaseDetails.builder()
            .state(CaseState.AOS_AWAITING.getValue())
            .id(Long.decode(CASE_ID))
            .data(Collections.singletonMap(
                CO_RESP_LETTER_HOLDER_ID_FIELD, LETTER_HOLDER_ID
            )).build();

        mockCaseDetails(caseDetails);

        classUnderTest.linkRespondent(
            RESPONDENT_AUTHORISATION, CASE_ID, "WrongHolderId");
    }

    @Test
    public void givenCaseAlreadyLinked_whenLinkRespondent_thenThrowUnauthorizedException() {
        expectedException.expect(UnauthorizedException.class);
        expectedException
            .expectMessage(UNAUTHORIZED_MESSAGE);
        CaseDetails caseDetails = CaseDetails.builder()
            .state(CaseState.ISSUED.getValue())
            .id(Long.decode(CASE_ID))
            .data(ImmutableMap.of(
                Objects.requireNonNull(RESP_LETTER_HOLDER_ID_FIELD), LETTER_HOLDER_ID,
                Objects.requireNonNull(RESP_EMAIL_ADDRESS), RESPONDENT_EMAIL
            )).build();

        when(coreCaseDataApi.readForCaseWorker(
            CASEWORKER_AUTHORISATION,
            SERVICE_TOKEN,
            CASEWORKER_USER_ID,
            JURISDICTION_ID,
            CASE_TYPE,
            CASE_ID
        )).thenReturn(caseDetails);

        classUnderTest.linkRespondent(RESPONDENT_AUTHORISATION, CASE_ID, LETTER_HOLDER_ID);
    }

    @Test
    public void givenCaseAlreadyLinked_whenLinkCoRespondent_thenThrowUnauthorizedException() {
        expectedException.expect(UnauthorizedException.class);
        expectedException
            .expectMessage(UNAUTHORIZED_MESSAGE);
        CaseDetails caseDetails = CaseDetails.builder()
            .state(CaseState.ISSUED.getValue())
            .id(Long.decode(CASE_ID))
            .data(ImmutableMap.of(
                Objects.requireNonNull(CO_RESP_LETTER_HOLDER_ID_FIELD), LETTER_HOLDER_ID,
                Objects.requireNonNull(CO_RESP_EMAIL_ADDRESS), RESPONDENT_EMAIL
            )).build();

        when(coreCaseDataApi.readForCaseWorker(
            CASEWORKER_AUTHORISATION,
            SERVICE_TOKEN,
            CASEWORKER_USER_ID,
            JURISDICTION_ID,
            CASE_TYPE,
            CASE_ID
        )).thenReturn(caseDetails);

        classUnderTest.linkRespondent(RESPONDENT_AUTHORISATION, CASE_ID, LETTER_HOLDER_ID);
    }

    @Test
    public void givenRespondentEmailNotMatch_whenLinkRespondent_thenThrowUnauthorizedException() {
        expectedException.expect(UnauthorizedException.class);
        expectedException
            .expectMessage(UNAUTHORIZED_MESSAGE);

        CaseDetails caseDetails = CaseDetails.builder()
            .state(CaseState.AOS_AWAITING.getValue())
            .data(ImmutableMap.of(
                RESP_LETTER_HOLDER_ID_FIELD, LETTER_HOLDER_ID,
                RESP_EMAIL_ADDRESS, "RandomEmail@email.com"
            )).build();

        mockCaseDetails(caseDetails);

        classUnderTest.linkRespondent(RESPONDENT_AUTHORISATION, CASE_ID, LETTER_HOLDER_ID);

        verify(caseUserApi, never()).updateCaseRolesForUser(
            eq(CASEWORKER_AUTHORISATION),
            eq(SERVICE_TOKEN),
            eq(CASE_ID),
            anyString(),
            any(CaseUser.class)
        );
    }

    @Test
    public void givenRespondentEmailNotMatch_whenLinkCoRespondent_thenThrowUnauthorizedException() {
        expectedException.expect(UnauthorizedException.class);
        expectedException
            .expectMessage(UNAUTHORIZED_MESSAGE);
        CaseDetails caseDetails = CaseDetails.builder()
            .state(CaseState.AOS_AWAITING.getValue())
            .data(ImmutableMap.of(
                CO_RESP_LETTER_HOLDER_ID_FIELD, LETTER_HOLDER_ID,
                CO_RESP_EMAIL_ADDRESS, "RandomEmail@email.com"
            )).build();

        mockCaseDetails(caseDetails);

        classUnderTest.linkRespondent(RESPONDENT_AUTHORISATION, CASE_ID, LETTER_HOLDER_ID);
 
        verify(caseUserApi, never()).updateCaseRolesForUser(
            eq(CASEWORKER_AUTHORISATION),
            eq(SERVICE_TOKEN),
            eq(CASE_ID),
            anyString(),
            any(CaseUser.class)
        );
    }

    @Test
    public void givenLetterHolderIdMatchesAndEmailNull_whenLinkRespondent_thenGrantUserPermission() {
        CaseDetails caseDetails = CaseDetails.builder()
            .id(Long.decode(CASE_ID))
            .data(Collections.singletonMap(
                RESP_LETTER_HOLDER_ID_FIELD, LETTER_HOLDER_ID
            )).build();

        mockCaseDetails(caseDetails);

        classUnderTest.linkRespondent(RESPONDENT_AUTHORISATION, CASE_ID, LETTER_HOLDER_ID);

        verify(caseUserApi).updateCaseRolesForUser(
            eq(CASEWORKER_AUTHORISATION),
            eq(SERVICE_TOKEN),
            eq(CASE_ID),
            anyString(),
            any(CaseUser.class)
        );
    }

    @Test
    public void givenLetterHolderIdMatchesAndEmailMatched_whenLinkRespondent_thenGrantUserPermission() {
        CaseDetails caseDetails = CaseDetails.builder()
            .id(Long.decode(CASE_ID))
            .data(ImmutableMap.of(
                RESP_LETTER_HOLDER_ID_FIELD, LETTER_HOLDER_ID,
                RESP_EMAIL_ADDRESS, USER_EMAIL
            )).build();

        mockCaseDetails(caseDetails);

        classUnderTest.linkRespondent(RESPONDENT_AUTHORISATION, CASE_ID, LETTER_HOLDER_ID);

        verify(caseUserApi).updateCaseRolesForUser(
            eq(CASEWORKER_AUTHORISATION),
            eq(SERVICE_TOKEN),
            eq(CASE_ID),
            anyString(),
            any(CaseUser.class)
        );
    }

    @Test
    public void givenLetterHolderIdMatchedAndEmailNull_whenLinkCoRespondent_thenGrantUserPermission() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(ImmutableMap.of(
                CO_RESP_LETTER_HOLDER_ID_FIELD, LETTER_HOLDER_ID
            )).build();

        mockCaseDetails(caseDetails);

        classUnderTest.linkRespondent(RESPONDENT_AUTHORISATION, CASE_ID, LETTER_HOLDER_ID);

        verify(caseUserApi).updateCaseRolesForUser(
            eq(CASEWORKER_AUTHORISATION),
            eq(SERVICE_TOKEN),
            eq(CASE_ID),
            anyString(),
            any(CaseUser.class)
        );
    }

    @Test
    public void givenLetterHolderIdAndEmailMatches_whenLinkCoRespondent_thenGrantUserPermission() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(ImmutableMap.of(
                CO_RESP_LETTER_HOLDER_ID_FIELD, LETTER_HOLDER_ID,
                CO_RESP_EMAIL_ADDRESS, USER_EMAIL
            )).build();

        mockCaseDetails(caseDetails);

        classUnderTest.linkRespondent(RESPONDENT_AUTHORISATION, CASE_ID, LETTER_HOLDER_ID);

        verify(caseUserApi).updateCaseRolesForUser(
            eq(CASEWORKER_AUTHORISATION),
            eq(SERVICE_TOKEN),
            eq(CASE_ID),
            anyString(),
            any(CaseUser.class)
        );
    }

    @Test
    public void givenUserWithCase_whenUnlinkUser_thenCallRemovePermissionAPI() {
        classUnderTest.unlinkRespondent(RESPONDENT_AUTHORISATION, CASE_ID);

        verify(caseAccessApi).revokeAccessToCase(
            eq(CASEWORKER_AUTHORISATION),
            eq(SERVICE_TOKEN),
            eq(CASEWORKER_USER_ID),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE),
            eq(CASE_ID),
            eq(RESPONDENT_USER.getId())
        );
    }

    private void mockCaseDetails(CaseDetails caseDetails) {
        when(coreCaseDataApi.readForCaseWorker(
            CASEWORKER_AUTHORISATION,
            SERVICE_TOKEN,
            CASEWORKER_USER_ID,
            JURISDICTION_ID,
            CASE_TYPE,
            CASE_ID
        )).thenReturn(caseDetails);
    }
}
