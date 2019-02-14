package uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.impl;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAccessApi;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.UserId;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CaseState;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.UserDetails;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.exception.CaseNotFoundException;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.UserService;

import java.util.Collections;
import java.util.Objects;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LinkRespondentServiceImplUTest {
    private static final String JURISDICTION_ID = "someJurisdictionId";
    private static final String CASE_TYPE = "someCaseType";

    private static final String LETTER_HOLDER_CASE_FIELD =
        (String)ReflectionTestUtils.getField(CcdCaseProperties.class, "RESP_LETTER_HOLDER_ID_FIELD");
    private static final String RECEIVED_AOS_FIELD =
        (String)ReflectionTestUtils.getField(CcdCaseProperties.class, "RESP_RECEIVED_AOS_FIELD");

    private static final String RESPONDENT_AUTHORISATION = "Bearer RespondentAuthToken";
    private static final String CASEWORKER_AUTHORISATION = "CaseWorkerAuthToken";
    private static final String CASE_ID = "CaseId";
    private static final String LETTER_HOLDER_ID = "letterholderId";
    private static final String CASEWORKER_USER_ID = "1";
    private static final String RESPONDENT_USER_ID = "2";
    private static final String SERVICE_TOKEN = "ServiceToken";
    private static final String RECEIVED_AOS_FIELD_VALUE = "Yes";
    private static final boolean NOT_CO_RESPONDENT = false;

    private static final UserDetails CASE_WORKER_USER = UserDetails.builder()
        .authToken(CASEWORKER_AUTHORISATION)
        .id(CASEWORKER_USER_ID)
        .build();

    private static final UserDetails RESPONDENT_USER = UserDetails.builder()
        .authToken(RESPONDENT_AUTHORISATION)
        .id(RESPONDENT_USER_ID)
        .build();


    @Mock
    private CoreCaseDataApi coreCaseDataApi;

    @Mock
    private UserService userService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private CaseAccessApi caseAccessApi;

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

    @Test(expected = CaseNotFoundException.class)
    public void givenNoCaseFound_whenLinkRespondent_thenThrowCaseNotFoundException() {

        when(coreCaseDataApi.readForCaseWorker(
            CASEWORKER_AUTHORISATION,
            SERVICE_TOKEN,
            CASEWORKER_USER_ID,
            JURISDICTION_ID,
            CASE_TYPE,
            CASE_ID
        )).thenReturn(null);

        classUnderTest.linkRespondent(RESPONDENT_AUTHORISATION, CASE_ID, LETTER_HOLDER_ID, NOT_CO_RESPONDENT);
    }

    @Test(expected = CaseNotFoundException.class)
    public void givenNoCaseCaseDataFound_whenLinkRespondent_thenThrowCaseNotFoundException() {
        CaseDetails caseDetails = CaseDetails.builder().build();

        when(coreCaseDataApi.readForCaseWorker(
            CASEWORKER_AUTHORISATION,
            SERVICE_TOKEN,
            CASEWORKER_USER_ID,
            JURISDICTION_ID,
            CASE_TYPE,
            CASE_ID
        )).thenReturn(caseDetails);

        classUnderTest.linkRespondent(RESPONDENT_AUTHORISATION, CASE_ID, LETTER_HOLDER_ID, NOT_CO_RESPONDENT);
    }

    @Test(expected = CaseNotFoundException.class)
    public void givenLetterHolderIdIsNull_whenLinkRespondent_thenThrowCaseNotFoundException() {
        CaseDetails caseDetails = CaseDetails.builder()
            .state(CaseState.AOS_AWAITING.getValue())
            .data(Collections.singletonMap(
                LETTER_HOLDER_CASE_FIELD, LETTER_HOLDER_ID
            )).build();

        when(coreCaseDataApi.readForCaseWorker(
            CASEWORKER_AUTHORISATION,
            SERVICE_TOKEN,
            CASEWORKER_USER_ID,
            JURISDICTION_ID,
            CASE_TYPE,
            CASE_ID
        )).thenReturn(caseDetails);

        classUnderTest.linkRespondent(RESPONDENT_AUTHORISATION, CASE_ID, null, NOT_CO_RESPONDENT);
    }

    @Test(expected = CaseNotFoundException.class)
    public void givenLetterHolderIdIsBlank_whenLinkRespondent_thenThrowCaseNotFoundException() {
        CaseDetails caseDetails = CaseDetails.builder()
            .state(CaseState.AOS_AWAITING.getValue())
            .data(Collections.singletonMap(
                LETTER_HOLDER_CASE_FIELD, LETTER_HOLDER_ID
            )).build();

        when(coreCaseDataApi.readForCaseWorker(
            CASEWORKER_AUTHORISATION,
            SERVICE_TOKEN,
            CASEWORKER_USER_ID,
            JURISDICTION_ID,
            CASE_TYPE,
            CASE_ID
        )).thenReturn(caseDetails);

        classUnderTest.linkRespondent(RESPONDENT_AUTHORISATION, CASE_ID, " ", NOT_CO_RESPONDENT);
    }

    @Test(expected = CaseNotFoundException.class)
    public void givenLetterHolderIdInCaseIsNull_whenLinkRespondent_thenThrowCaseNotFoundException() {
        CaseDetails caseDetails = CaseDetails.builder()
            .state(CaseState.AOS_AWAITING.getValue())
            .data(Collections.emptyMap()).build();

        when(coreCaseDataApi.readForCaseWorker(
            CASEWORKER_AUTHORISATION,
            SERVICE_TOKEN,
            CASEWORKER_USER_ID,
            JURISDICTION_ID,
            CASE_TYPE,
            CASE_ID
        )).thenReturn(caseDetails);

        classUnderTest.linkRespondent(RESPONDENT_AUTHORISATION, CASE_ID, LETTER_HOLDER_ID, NOT_CO_RESPONDENT);
    }

    @Test(expected = CaseNotFoundException.class)
    public void givenLetterHolderIdsDoNotMatch_whenLinkRespondent_thenThrowCaseNotFoundException() {
        CaseDetails caseDetails = CaseDetails.builder()
            .state(CaseState.AOS_AWAITING.getValue())
            .data(Collections.singletonMap(
                LETTER_HOLDER_CASE_FIELD, LETTER_HOLDER_ID
            )).build();

        when(coreCaseDataApi.readForCaseWorker(
            CASEWORKER_AUTHORISATION,
            SERVICE_TOKEN,
            CASEWORKER_USER_ID,
            JURISDICTION_ID,
            CASE_TYPE,
            CASE_ID
        )).thenReturn(caseDetails);

        classUnderTest.linkRespondent(
            RESPONDENT_AUTHORISATION, CASE_ID, "Letter holder id no match", NOT_CO_RESPONDENT);
    }

    @Test(expected = CaseNotFoundException.class)
    public void givenCaseAlreadyLinked_whenLinkRespondent_thenThrowCaseNotFoundException() {
        CaseDetails caseDetails = CaseDetails.builder()
            .state(CaseState.ISSUED.getValue())
            .data(ImmutableMap.of(
                Objects.requireNonNull(LETTER_HOLDER_CASE_FIELD), LETTER_HOLDER_ID,
                Objects.requireNonNull(RECEIVED_AOS_FIELD), RECEIVED_AOS_FIELD_VALUE
            )).build();

        when(coreCaseDataApi.readForCaseWorker(
            CASEWORKER_AUTHORISATION,
            SERVICE_TOKEN,
            CASEWORKER_USER_ID,
            JURISDICTION_ID,
            CASE_TYPE,
            CASE_ID
        )).thenReturn(caseDetails);

        classUnderTest.linkRespondent(RESPONDENT_AUTHORISATION, CASE_ID, LETTER_HOLDER_ID, NOT_CO_RESPONDENT);
    }

    @Test
    public void givenLetterHolderIdAndCaseStateMatches_whenLinkRespondent_thenProceedAsExpected() {
        CaseDetails caseDetails = CaseDetails.builder()
            .state(CaseState.AOS_AWAITING.getValue())
            .data(Collections.singletonMap(
                LETTER_HOLDER_CASE_FIELD, LETTER_HOLDER_ID
            )).build();

        when(coreCaseDataApi.readForCaseWorker(
            CASEWORKER_AUTHORISATION,
            SERVICE_TOKEN,
            CASEWORKER_USER_ID,
            JURISDICTION_ID,
            CASE_TYPE,
            CASE_ID
        )).thenReturn(caseDetails);

        doNothing().when(caseAccessApi).grantAccessToCase(
            eq(CASEWORKER_AUTHORISATION),
            eq(SERVICE_TOKEN),
            eq(CASEWORKER_USER_ID),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE),
            eq(CASE_ID),
            any(UserId.class)
        );

        classUnderTest.linkRespondent(RESPONDENT_AUTHORISATION, CASE_ID, LETTER_HOLDER_ID, NOT_CO_RESPONDENT);

        verify(caseAccessApi).grantAccessToCase(
            eq(CASEWORKER_AUTHORISATION),
            eq(SERVICE_TOKEN),
            eq(CASEWORKER_USER_ID),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE),
            eq(CASE_ID),
            any(UserId.class)
        );
    }

    @Test
    public void givenUserWithCase_whenUnlinkUser_thenCallRemovePermissionAPI() {

        doNothing().when(caseAccessApi).revokeAccessToCase(
            eq(CASEWORKER_AUTHORISATION),
            eq(SERVICE_TOKEN),
            eq(CASEWORKER_USER_ID),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE),
            eq(CASE_ID),
            eq(RESPONDENT_USER.getId())
        );

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
}
