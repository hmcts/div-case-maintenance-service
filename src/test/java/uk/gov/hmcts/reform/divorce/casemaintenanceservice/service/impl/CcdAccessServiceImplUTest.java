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
import uk.gov.hmcts.reform.ccd.client.CaseUserApi;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.CaseUser;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CaseState;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CmsConstants;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.exception.CaseNotFoundException;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.exception.InvalidRequestException;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.exception.UnauthorizedException;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.UserService;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static org.junit.rules.ExpectedException.none;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.TestConstants.TEST_CO_RESP_EMAIL;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.TestConstants.TEST_LETTER_HOLDER_ID_CODE;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.TestConstants.TEST_RESP_EMAIL;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.TestConstants.TEST_RESP_SOL_COMPANY;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.TestConstants.TEST_RESP_SOL_NAME;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.TestConstants.TEST_SERVICE_TOKEN;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties.CO_RESP_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties.CO_RESP_LETTER_HOLDER_ID_FIELD;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties.D8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties.D8_RESPONDENT_SOLICITOR_COMPANY;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties.D8_RESPONDENT_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties.RESP_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties.RESP_LETTER_HOLDER_ID_FIELD;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties.RESP_SOLICITOR_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties.RESP_SOL_REPRESENTED;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CmsConstants.YES_VALUE;

@RunWith(MockitoJUnitRunner.class)
public class CcdAccessServiceImplUTest {
    private static final String JURISDICTION_ID = "DIVORCE";
    private static final String CASE_TYPE = "DIVORCE";

    private static final String RESPONDENT_AUTHORISATION = "Bearer RespondentAuthToken";
    private static final String PET_SOLICITOR_AUTHORISATION = "Bearer PetSolicitorAuthorisation";
    private static final String PET_AUTHORISATION = "Bearer PetAuthToken";
    private static final String CASEWORKER_AUTHORISATION = "CaseWorkerAuthToken";
    private static final String CASE_ID = "12345678";
    private static final String LETTER_HOLDER_ID_SOL = "letterholderIdSol";
    private static final String CASEWORKER_USER_ID = "1";
    private static final String RESPONDENT_USER_ID = "2";
    private static final String PET_USER_ID = "3";
    private static final String RESP_UNAUTHORIZED_MESSAGE =
        "Case with caseId [12345678] and letter holder id [test.letter.holder.id] already assigned for [RESPONDENT] "
            + "or Petitioner attempted to link case. Check previous logs for more information.";
    private static final String CO_RESP_UNAUTHORIZED_MESSAGE =
        "Case with caseId [12345678] and letter holder id [test.letter.holder.id] already assigned for [CO_RESPONDENT] "
            + "or Petitioner attempted to link case. Check previous logs for more information.";
    private static final String UNAUTHORIZED_MESSAGE_WRONG_HOLDER_ID =
        "Case with caseId [12345678] and letter holder id [WrongHolderId] mismatch.";
    private static final String INVALID_MESSAGE = "Case details or letter holder data are invalid";
    private static final String NOT_FOUND_MESSAGE = "Case with caseId [12345678] and letter holder id [test.letter.holder.id] not found";

    private static final User CASE_WORKER_USER = new User(
        CASEWORKER_AUTHORISATION,
        UserDetails.builder().id(CASEWORKER_USER_ID).build()
    );

    private static final User RESPONDENT_USER = new User(
        RESPONDENT_AUTHORISATION,
        UserDetails.builder().id(RESPONDENT_USER_ID).email(TEST_USER_EMAIL).build()
    );

    private static final User PETITIONER_USER = new User(
        PET_AUTHORISATION,
        UserDetails.builder().id(PET_USER_ID).email(TEST_USER_EMAIL).build()
    );

    private static final User PET_SOL_USER = new User(
        PET_AUTHORISATION,
        UserDetails.builder().id(PET_USER_ID).email(TEST_USER_EMAIL).build()
    );

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock(name = "uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi")
    private CoreCaseDataApi coreCaseDataApi;

    @Mock
    private UserService userService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private CaseUserApi caseUserApi;

    @InjectMocks
    private CcdAccessServiceImpl classUnderTest;

    @Before
    public void setup() {
        ReflectionTestUtils.setField(classUnderTest, "jurisdictionId", JURISDICTION_ID);
        ReflectionTestUtils.setField(classUnderTest, "caseType", CASE_TYPE);

        when(userService.retrieveAnonymousCaseWorkerDetails()).thenReturn(CASE_WORKER_USER);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);
        when(userService.retrieveUser(RESPONDENT_AUTHORISATION)).thenReturn(RESPONDENT_USER);
    }

    @Test
    public void givenNoCaseFound_whenLinkRespondent_thenThrowCaseNotFoundException() {
        thrown.expect(CaseNotFoundException.class);
        thrown
            .expectMessage(NOT_FOUND_MESSAGE);

        mockCaseDetails(null);

        classUnderTest.linkRespondent(RESPONDENT_AUTHORISATION, CASE_ID, TEST_LETTER_HOLDER_ID_CODE);
    }

    @Test
    public void givenNoCaseDataFound_whenLinkRespondent_thenThrowCaseNotFoundException() {
        thrown.expect(InvalidRequestException.class);
        thrown.expectMessage(INVALID_MESSAGE);
        CaseDetails caseDetails = CaseDetails.builder().build();

        mockCaseDetails(caseDetails);

        classUnderTest.linkRespondent(RESPONDENT_AUTHORISATION, CASE_ID, TEST_LETTER_HOLDER_ID_CODE);
    }

    @Test
    public void givenLetterHolderIdIsNull_whenLinkRespondent_thenThrowInvalidRequestException() {
        thrown.expect(InvalidRequestException.class);
        thrown.expectMessage(INVALID_MESSAGE);
        CaseDetails caseDetails = CaseDetails.builder()
            .state(CaseState.AOS_AWAITING.getValue())
            .data(Collections.singletonMap(
                RESP_LETTER_HOLDER_ID_FIELD, TEST_LETTER_HOLDER_ID_CODE
            )).build();

        mockCaseDetails(caseDetails);

        classUnderTest.linkRespondent(RESPONDENT_AUTHORISATION, CASE_ID, null);
    }

    @Test
    public void givenLetterHolderIdIsBlank_whenLinkRespondent_thenThrowInvalidRequestException() {
        thrown.expect(InvalidRequestException.class);
        thrown.expectMessage(INVALID_MESSAGE);
        CaseDetails caseDetails = CaseDetails.builder()
            .data(Collections.singletonMap(
                RESP_LETTER_HOLDER_ID_FIELD, TEST_LETTER_HOLDER_ID_CODE
            )).build();

        mockCaseDetails(caseDetails);

        classUnderTest.linkRespondent(RESPONDENT_AUTHORISATION, CASE_ID, " ");
    }

    @Test
    public void givenLetterHolderIdInCaseIsNull_whenLinkRespondent_thenThrowUnauthorizedException() {
        thrown.expect(UnauthorizedException.class);
        thrown.expectMessage(UNAUTHORIZED_MESSAGE_WRONG_HOLDER_ID);
        CaseDetails caseDetails = CaseDetails.builder()
            .id(Long.decode(CASE_ID))
            .data(Collections.emptyMap()).build();

        mockCaseDetails(caseDetails);

        classUnderTest.linkRespondent(RESPONDENT_AUTHORISATION, CASE_ID, "WrongHolderId");
    }

    @Test
    public void givenLetterHolderIdsDoNotMatch_whenLinkRespondent_thenThrowUnauthorizedException() {
        thrown.expect(UnauthorizedException.class);
        thrown.expectMessage(UNAUTHORIZED_MESSAGE_WRONG_HOLDER_ID);

        CaseDetails caseDetails = CaseDetails.builder()
            .state(CaseState.AOS_AWAITING.getValue())
            .id(Long.decode(CASE_ID))
            .data(Collections.singletonMap(
                RESP_LETTER_HOLDER_ID_FIELD, TEST_LETTER_HOLDER_ID_CODE
            )).build();

        mockCaseDetails(caseDetails);

        classUnderTest.linkRespondent(
            RESPONDENT_AUTHORISATION, CASE_ID, "WrongHolderId");
    }

    @Test
    public void givenLetterHolderIdsDoNotMatch_whenLinkCoRespondent_thenThrowUnauthorizedException() {
        thrown.expect(UnauthorizedException.class);
        thrown.expectMessage(UNAUTHORIZED_MESSAGE_WRONG_HOLDER_ID);
        CaseDetails caseDetails = CaseDetails.builder()
            .state(CaseState.AOS_AWAITING.getValue())
            .id(Long.decode(CASE_ID))
            .data(Collections.singletonMap(
                CO_RESP_LETTER_HOLDER_ID_FIELD, TEST_LETTER_HOLDER_ID_CODE
            )).build();

        mockCaseDetails(caseDetails);

        classUnderTest.linkRespondent(
            RESPONDENT_AUTHORISATION, CASE_ID, "WrongHolderId");
    }

    @Test
    public void givenCaseAlreadyLinked_whenLinkRespondent_thenThrowUnauthorizedException() {
        thrown.expect(UnauthorizedException.class);
        thrown.expectMessage(RESP_UNAUTHORIZED_MESSAGE);
        CaseDetails caseDetails = CaseDetails.builder()
            .state(CaseState.ISSUED.getValue())
            .id(Long.decode(CASE_ID))
            .data(ImmutableMap.of(
                Objects.requireNonNull(RESP_LETTER_HOLDER_ID_FIELD), TEST_LETTER_HOLDER_ID_CODE,
                Objects.requireNonNull(RESP_EMAIL_ADDRESS), TEST_RESP_EMAIL
            )).build();

        when(coreCaseDataApi.readForCaseWorker(
            CASEWORKER_AUTHORISATION,
            TEST_SERVICE_TOKEN,
            CASEWORKER_USER_ID,
            JURISDICTION_ID,
            CASE_TYPE,
            CASE_ID
        )).thenReturn(caseDetails);

        classUnderTest.linkRespondent(RESPONDENT_AUTHORISATION, CASE_ID, TEST_LETTER_HOLDER_ID_CODE);
    }

    @Test
    public void givenUserIsPetitioner_whenLinkRespondent_thenThrowUnauthorizedException() {
        thrown.expect(UnauthorizedException.class);
        thrown.expectMessage(RESP_UNAUTHORIZED_MESSAGE);
        CaseDetails caseDetails = CaseDetails.builder()
            .state(CaseState.ISSUED.getValue())
            .id(Long.decode(CASE_ID))
            .data(ImmutableMap.of(
                Objects.requireNonNull(RESP_LETTER_HOLDER_ID_FIELD), TEST_LETTER_HOLDER_ID_CODE,
                Objects.requireNonNull(D8_PETITIONER_EMAIL), TEST_USER_EMAIL
            )).build();

        when(coreCaseDataApi.readForCaseWorker(
            CASEWORKER_AUTHORISATION,
            TEST_SERVICE_TOKEN,
            CASEWORKER_USER_ID,
            JURISDICTION_ID,
            CASE_TYPE,
            CASE_ID
        )).thenReturn(caseDetails);
        when(userService.retrieveUser(PET_AUTHORISATION)).thenReturn(PETITIONER_USER);

        classUnderTest.linkRespondent(PET_AUTHORISATION, CASE_ID, TEST_LETTER_HOLDER_ID_CODE);
    }

    @Test
    public void givenUserIsPetitioner_whenLinkCoRespondent_thenThrowUnauthorizedException() {
        thrown.expect(UnauthorizedException.class);
        thrown.expectMessage(CO_RESP_UNAUTHORIZED_MESSAGE);
        CaseDetails caseDetails = CaseDetails.builder()
            .state(CaseState.ISSUED.getValue())
            .id(Long.decode(CASE_ID))
            .data(ImmutableMap.of(
                Objects.requireNonNull(CO_RESP_LETTER_HOLDER_ID_FIELD), TEST_LETTER_HOLDER_ID_CODE,
                Objects.requireNonNull(D8_PETITIONER_EMAIL), TEST_USER_EMAIL
            )).build();

        when(coreCaseDataApi.readForCaseWorker(
            CASEWORKER_AUTHORISATION,
            TEST_SERVICE_TOKEN,
            CASEWORKER_USER_ID,
            JURISDICTION_ID,
            CASE_TYPE,
            CASE_ID
        )).thenReturn(caseDetails);
        when(userService.retrieveUser(PET_AUTHORISATION)).thenReturn(PETITIONER_USER);

        classUnderTest.linkRespondent(PET_AUTHORISATION, CASE_ID, TEST_LETTER_HOLDER_ID_CODE);
    }

    @Test
    public void givenCaseAlreadyLinked_whenLinkCoRespondent_thenThrowUnauthorizedException() {
        thrown.expect(UnauthorizedException.class);
        thrown.expectMessage(CO_RESP_UNAUTHORIZED_MESSAGE);
        CaseDetails caseDetails = CaseDetails.builder()
            .state(CaseState.ISSUED.getValue())
            .id(Long.decode(CASE_ID))
            .data(ImmutableMap.of(
                Objects.requireNonNull(CO_RESP_LETTER_HOLDER_ID_FIELD), TEST_LETTER_HOLDER_ID_CODE,
                Objects.requireNonNull(CO_RESP_EMAIL_ADDRESS), TEST_RESP_EMAIL
            )).build();

        when(coreCaseDataApi.readForCaseWorker(
            CASEWORKER_AUTHORISATION,
            TEST_SERVICE_TOKEN,
            CASEWORKER_USER_ID,
            JURISDICTION_ID,
            CASE_TYPE,
            CASE_ID
        )).thenReturn(caseDetails);

        classUnderTest.linkRespondent(RESPONDENT_AUTHORISATION, CASE_ID, TEST_LETTER_HOLDER_ID_CODE);
    }

    @Test
    public void givenRespondentEmailNotMatch_whenLinkRespondent_thenThrowUnauthorizedException() {
        thrown.expect(UnauthorizedException.class);
        thrown.expectMessage(RESP_UNAUTHORIZED_MESSAGE);

        CaseDetails caseDetails = CaseDetails.builder()
            .id(1000L)
            .state(CaseState.AOS_AWAITING.getValue())
            .data(ImmutableMap.of(
                RESP_LETTER_HOLDER_ID_FIELD, TEST_LETTER_HOLDER_ID_CODE,
                RESP_EMAIL_ADDRESS, TEST_RESP_EMAIL
            )).build();

        mockCaseDetails(caseDetails);

        classUnderTest.linkRespondent(RESPONDENT_AUTHORISATION, CASE_ID, TEST_LETTER_HOLDER_ID_CODE);

        verify(caseUserApi, never()).updateCaseRolesForUser(
            eq(CASEWORKER_AUTHORISATION),
            eq(TEST_SERVICE_TOKEN),
            eq(CASE_ID),
            eq(RESPONDENT_USER_ID),
            any(CaseUser.class)
        );
    }

    @Test
    public void givenRespondentEmailNotMatch_whenLinkCoRespondent_thenThrowUnauthorizedException() {
        thrown.expect(UnauthorizedException.class);
        thrown.expectMessage(CO_RESP_UNAUTHORIZED_MESSAGE);
        CaseDetails caseDetails = CaseDetails.builder()
            .state(CaseState.AOS_AWAITING.getValue())
            .id(Long.decode(CASE_ID))
            .data(ImmutableMap.of(
                CO_RESP_LETTER_HOLDER_ID_FIELD, TEST_LETTER_HOLDER_ID_CODE,
                CO_RESP_EMAIL_ADDRESS, TEST_CO_RESP_EMAIL
            )).build();

        mockCaseDetails(caseDetails);

        classUnderTest.linkRespondent(RESPONDENT_AUTHORISATION, CASE_ID, TEST_LETTER_HOLDER_ID_CODE);

        verify(caseUserApi, never()).updateCaseRolesForUser(
            eq(CASEWORKER_AUTHORISATION),
            eq(TEST_SERVICE_TOKEN),
            eq(CASE_ID),
            eq(RESPONDENT_USER_ID),
            any(CaseUser.class)
        );
    }

    @Test
    public void givenLetterHolderIdMatchesAndEmailNull_whenLinkRespondent_thenGrantUserPermission() {
        CaseDetails caseDetails = CaseDetails.builder()
            .id(Long.decode(CASE_ID))
            .data(Collections.singletonMap(
                RESP_LETTER_HOLDER_ID_FIELD, TEST_LETTER_HOLDER_ID_CODE
            )).build();

        mockCaseDetails(caseDetails);

        classUnderTest.linkRespondent(RESPONDENT_AUTHORISATION, CASE_ID, TEST_LETTER_HOLDER_ID_CODE);

        verify(caseUserApi).updateCaseRolesForUser(
            eq(CASEWORKER_AUTHORISATION),
            eq(TEST_SERVICE_TOKEN),
            eq(CASE_ID),
            eq(RESPONDENT_USER_ID),
            any(CaseUser.class)
        );
    }

    @Test
    public void givenLetterHolderIdMatchesAndEmailEmptyString_whenLinkRespondent_thenGrantUserPermission() {
        CaseDetails caseDetails = CaseDetails.builder()
            .id(Long.decode(CASE_ID))
            .data(ImmutableMap.of(
                RESP_LETTER_HOLDER_ID_FIELD, TEST_LETTER_HOLDER_ID_CODE,
                RESP_EMAIL_ADDRESS, ""
            )).build();

        mockCaseDetails(caseDetails);

        classUnderTest.linkRespondent(RESPONDENT_AUTHORISATION, CASE_ID, TEST_LETTER_HOLDER_ID_CODE);

        verify(caseUserApi).updateCaseRolesForUser(
            eq(CASEWORKER_AUTHORISATION),
            eq(TEST_SERVICE_TOKEN),
            eq(CASE_ID),
            eq(RESPONDENT_USER_ID),
            any(CaseUser.class)
        );
    }

    @Test
    public void givenLetterHolderIdMatchesAndSolicitorEmailEmptyString_whenLinkRespondent_thenGrantUserPermission() {
        CaseDetails caseDetails = CaseDetails.builder()
            .id(Long.decode(CASE_ID))
            .data(ImmutableMap.of(
                RESP_LETTER_HOLDER_ID_FIELD, TEST_LETTER_HOLDER_ID_CODE,
                RESP_EMAIL_ADDRESS, TEST_RESP_EMAIL,
                RESP_SOLICITOR_EMAIL_ADDRESS, "",
                RESP_SOL_REPRESENTED, YES_VALUE
            )).build();

        mockCaseDetails(caseDetails);

        classUnderTest.linkRespondent(RESPONDENT_AUTHORISATION, CASE_ID, TEST_LETTER_HOLDER_ID_CODE);

        verify(caseUserApi).updateCaseRolesForUser(
            eq(CASEWORKER_AUTHORISATION),
            eq(TEST_SERVICE_TOKEN),
            eq(CASE_ID),
            eq(RESPONDENT_USER_ID),
            any(CaseUser.class)
        );
    }

    @Test
    public void givenLetterHolderIdMatchesAndEmailMatched_whenLinkRespondent_thenGrantCorrectUserPermissions() {
        CaseDetails caseDetails = CaseDetails.builder()
            .id(Long.decode(CASE_ID))
            .data(ImmutableMap.of(
                RESP_LETTER_HOLDER_ID_FIELD, TEST_LETTER_HOLDER_ID_CODE,
                RESP_EMAIL_ADDRESS, TEST_USER_EMAIL
            )).build();

        mockCaseDetails(caseDetails);

        Set<String> expectedCaseRoles = new HashSet<>();
        expectedCaseRoles.add(CmsConstants.CREATOR_ROLE);

        CaseUser expectedCaseUser = new CaseUser(RESPONDENT_USER_ID, expectedCaseRoles);

        classUnderTest.linkRespondent(RESPONDENT_AUTHORISATION, CASE_ID, TEST_LETTER_HOLDER_ID_CODE);

        verify(caseUserApi).updateCaseRolesForUser(
            eq(CASEWORKER_AUTHORISATION),
            eq(TEST_SERVICE_TOKEN),
            eq(CASE_ID),
            eq(RESPONDENT_USER_ID),
            eq(expectedCaseUser)
        );
    }

    @Test
    public void givenLetterHolderIdMatchesAndEmailMatchedAndSolicitorRepresentingRespFieldIsYes_whenLinkRespondent_thenGrantCorrectUserPermissions() {
        CaseDetails caseDetails = CaseDetails.builder()
            .id(Long.decode(CASE_ID))
            .data(ImmutableMap.of(
                RESP_LETTER_HOLDER_ID_FIELD, TEST_LETTER_HOLDER_ID_CODE,
                RESP_EMAIL_ADDRESS, TEST_USER_EMAIL,
                RESP_SOL_REPRESENTED, YES_VALUE
            )).build();

        mockCaseDetails(caseDetails);

        Set<String> expectedCaseRoles = new HashSet<>();
        expectedCaseRoles.add(CmsConstants.CREATOR_ROLE);
        expectedCaseRoles.add(CmsConstants.RESP_SOL_ROLE);

        CaseUser expectedCaseUser = new CaseUser(RESPONDENT_USER_ID, expectedCaseRoles);

        classUnderTest.linkRespondent(RESPONDENT_AUTHORISATION, CASE_ID, TEST_LETTER_HOLDER_ID_CODE);

        verify(caseUserApi).updateCaseRolesForUser(
            eq(CASEWORKER_AUTHORISATION),
            eq(TEST_SERVICE_TOKEN),
            eq(CASE_ID),
            eq(RESPONDENT_USER_ID),
            eq(expectedCaseUser)
        );
    }

    // test for temporary fix until we implement setting respondentSolicitorRepresented from CCD for RespSols
    // in all scenarios https://tools.hmcts.net/jira/browse/DIV-5759
    @Test
    public void givenLetterHolderIdMatchesAndEmailMatchedAndSolRepresentingRespWithoutRespSolRepFieldPresent_whenLinkRespondent_thenGrantCorrectUserPermissions() {
        CaseDetails caseDetails = CaseDetails.builder()
            .id(Long.decode(CASE_ID))
            .data(ImmutableMap.of(
                RESP_LETTER_HOLDER_ID_FIELD, TEST_LETTER_HOLDER_ID_CODE,
                RESP_EMAIL_ADDRESS, TEST_USER_EMAIL,
                D8_RESPONDENT_SOLICITOR_NAME, TEST_RESP_SOL_NAME,
                D8_RESPONDENT_SOLICITOR_COMPANY, TEST_RESP_SOL_COMPANY
            )).build();

        mockCaseDetails(caseDetails);

        Set<String> expectedCaseRoles = new HashSet<>();
        expectedCaseRoles.add(CmsConstants.CREATOR_ROLE);
        expectedCaseRoles.add(CmsConstants.RESP_SOL_ROLE);

        CaseUser expectedCaseUser = new CaseUser(RESPONDENT_USER_ID, expectedCaseRoles);

        classUnderTest.linkRespondent(RESPONDENT_AUTHORISATION, CASE_ID, TEST_LETTER_HOLDER_ID_CODE);

        verify(caseUserApi).updateCaseRolesForUser(
            eq(CASEWORKER_AUTHORISATION),
            eq(TEST_SERVICE_TOKEN),
            eq(CASE_ID),
            eq(RESPONDENT_USER_ID),
            eq(expectedCaseUser)
        );
    }

    @Test
    public void givenLetterHolderIdMatchesRespondentLetterAndSolNameAndCompanyNull_whenLinkRespondent_thenRespondentTypeIsRespondent() {
        CaseDetails caseDetails = CaseDetails.builder()
            .id(Long.decode(CASE_ID))
            .data(ImmutableMap.of(
                RESP_LETTER_HOLDER_ID_FIELD, TEST_LETTER_HOLDER_ID_CODE
            )).build();

        mockCaseDetails(caseDetails);

        Set<String> expectedCaseRoles = new HashSet<>();
        expectedCaseRoles.add(CmsConstants.CREATOR_ROLE);

        CaseUser expectedCaseUser = new CaseUser(RESPONDENT_USER_ID, expectedCaseRoles);

        classUnderTest.linkRespondent(RESPONDENT_AUTHORISATION, CASE_ID, TEST_LETTER_HOLDER_ID_CODE);

        verify(caseUserApi).updateCaseRolesForUser(
            eq(CASEWORKER_AUTHORISATION),
            eq(TEST_SERVICE_TOKEN),
            eq(CASE_ID),
            eq(RESPONDENT_USER_ID),
            eq(expectedCaseUser)
        );
    }

    @Test
    public void givenLetterHolderIdMatchesRespondentLetterAndSolNameAndCompanyEmptyString_whenLinkRespondent_thenRespondentTypeIsRespondent() {
        CaseDetails caseDetails = CaseDetails.builder()
            .id(Long.decode(CASE_ID))
            .data(ImmutableMap.of(
                RESP_LETTER_HOLDER_ID_FIELD, TEST_LETTER_HOLDER_ID_CODE,
                D8_RESPONDENT_SOLICITOR_NAME, "",
                D8_RESPONDENT_SOLICITOR_COMPANY, ""
            )).build();

        mockCaseDetails(caseDetails);

        Set<String> expectedCaseRoles = new HashSet<>();
        expectedCaseRoles.add(CmsConstants.CREATOR_ROLE);

        CaseUser expectedCaseUser = new CaseUser(RESPONDENT_USER_ID, expectedCaseRoles);

        classUnderTest.linkRespondent(RESPONDENT_AUTHORISATION, CASE_ID, TEST_LETTER_HOLDER_ID_CODE);

        verify(caseUserApi).updateCaseRolesForUser(
            eq(CASEWORKER_AUTHORISATION),
            eq(TEST_SERVICE_TOKEN),
            eq(CASE_ID),
            eq(RESPONDENT_USER_ID),
            eq(expectedCaseUser)
        );
    }

    @Test
    public void givenLetterHolderIdMatchedAndEmailNull_whenLinkCoRespondent_thenGrantUserPermission() {
        CaseDetails caseDetails = CaseDetails.builder()
            .id(1000L)
            .data(ImmutableMap.of(
                CO_RESP_LETTER_HOLDER_ID_FIELD, TEST_LETTER_HOLDER_ID_CODE
            )).build();

        mockCaseDetails(caseDetails);

        classUnderTest.linkRespondent(RESPONDENT_AUTHORISATION, CASE_ID, TEST_LETTER_HOLDER_ID_CODE);

        verify(caseUserApi).updateCaseRolesForUser(
            eq(CASEWORKER_AUTHORISATION),
            eq(TEST_SERVICE_TOKEN),
            eq(CASE_ID),
            eq(RESPONDENT_USER_ID),
            any(CaseUser.class)
        );
    }

    @Test
    public void givenLetterHolderIdMatchedAndEmailEmptyString_whenLinkCoRespondent_thenGrantUserPermission() {
        CaseDetails caseDetails = CaseDetails.builder()
            .id(1000L)
            .data(ImmutableMap.of(
                CO_RESP_LETTER_HOLDER_ID_FIELD, TEST_LETTER_HOLDER_ID_CODE,
                CO_RESP_EMAIL_ADDRESS, ""
            )).build();

        mockCaseDetails(caseDetails);

        classUnderTest.linkRespondent(RESPONDENT_AUTHORISATION, CASE_ID, TEST_LETTER_HOLDER_ID_CODE);

        verify(caseUserApi).updateCaseRolesForUser(
            eq(CASEWORKER_AUTHORISATION),
            eq(TEST_SERVICE_TOKEN),
            eq(CASE_ID),
            eq(RESPONDENT_USER_ID),
            any(CaseUser.class)
        );
    }

    @Test
    public void givenValidSolicitorCase_whenCreatingCase_thenAssignRole() {
        when(userService.retrieveUser(PET_SOLICITOR_AUTHORISATION)).thenReturn(PET_SOL_USER);
        classUnderTest.addPetitionerSolicitorRole(PET_SOLICITOR_AUTHORISATION, CASE_ID);

        verify(caseUserApi).updateCaseRolesForUser(
            eq(CASEWORKER_AUTHORISATION),
            eq(TEST_SERVICE_TOKEN),
            eq(CASE_ID),
            anyString(),
            any(CaseUser.class)
        );
    }

    @Test
    public void givenLetterHolderIdAndEmailMatches_whenLinkCoRespondent_thenGrantUserPermission() {
        CaseDetails caseDetails = CaseDetails.builder()
            .id(1000L)
            .data(ImmutableMap.of(
                CO_RESP_LETTER_HOLDER_ID_FIELD, TEST_LETTER_HOLDER_ID_CODE,
                CO_RESP_EMAIL_ADDRESS, TEST_USER_EMAIL
            )).build();

        mockCaseDetails(caseDetails);

        classUnderTest.linkRespondent(RESPONDENT_AUTHORISATION, CASE_ID, TEST_LETTER_HOLDER_ID_CODE);

        verify(caseUserApi).updateCaseRolesForUser(
            eq(CASEWORKER_AUTHORISATION),
            eq(TEST_SERVICE_TOKEN),
            eq(CASE_ID),
            eq(RESPONDENT_USER_ID),
            any(CaseUser.class)
        );
    }

    @Test
    public void givenCoRespLinked_whenLinkRespSolicitor_thenGrantUserPermission() {
        CaseDetails caseDetails = CaseDetails.builder()
            .id(1000L)
            .data(ImmutableMap.of(
                CO_RESP_LETTER_HOLDER_ID_FIELD, TEST_LETTER_HOLDER_ID_CODE,
                CO_RESP_EMAIL_ADDRESS, TEST_CO_RESP_EMAIL,
                RESP_EMAIL_ADDRESS, TEST_USER_EMAIL,
                RESP_SOL_REPRESENTED, YES_VALUE,
                RESP_LETTER_HOLDER_ID_FIELD, LETTER_HOLDER_ID_SOL
            )).build();

        mockCaseDetails(caseDetails);

        classUnderTest.linkRespondent(RESPONDENT_AUTHORISATION, CASE_ID, LETTER_HOLDER_ID_SOL);

        verify(caseUserApi).updateCaseRolesForUser(
            eq(CASEWORKER_AUTHORISATION),
            eq(TEST_SERVICE_TOKEN),
            eq(CASE_ID),
            eq(RESPONDENT_USER_ID),
            any(CaseUser.class)
        );
    }

    @Test
    public void givenUserWithCase_whenUnlinkUser_thenCallRemovePermissionAPI() {
        when(userService.retrieveUser(RESPONDENT_AUTHORISATION)).thenReturn(RESPONDENT_USER);
        classUnderTest.unlinkRespondent(RESPONDENT_AUTHORISATION, CASE_ID);

        verify(caseUserApi).updateCaseRolesForUser(
            eq(CASEWORKER_AUTHORISATION),
            eq(TEST_SERVICE_TOKEN),
            eq(CASE_ID),
            eq(RESPONDENT_USER.getUserDetails().getId()),
            any(CaseUser.class)
        );
    }

    private void mockCaseDetails(CaseDetails caseDetails) {
        when(coreCaseDataApi.readForCaseWorker(
            CASEWORKER_AUTHORISATION,
            TEST_SERVICE_TOKEN,
            CASEWORKER_USER_ID,
            JURISDICTION_ID,
            CASE_TYPE,
            CASE_ID
        )).thenReturn(caseDetails);
    }
}
