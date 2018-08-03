package uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CitizenCaseState;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CitizenCaseStateType;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.UserDetails;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.exception.DuplicateCaseException;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.IdamUserService;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CcdRetrievalServiceImplUTest {

    private static final String JURISDICTION_ID = "someJurisdictionId";
    private static final String CASE_TYPE = "someCaseType";
    private static final String AWAITING_PAYMENT_STATE = CitizenCaseState.AWAITING_PAYMENT.getValue();
    private static final String SUBMITTED_PAYMENT_STATE = CitizenCaseState.SUBMITTED.getValue();
    private static final String AUTHORISATION = "authorisation";
    private static final String BEARER_AUTHORISATION = "Bearer authorisation";
    private static final String SERVICE_TOKEN = "serviceToken";
    private static final String USER_ID = "someUserId";
    private static final UserDetails USER_DETAILS = UserDetails.builder().id(USER_ID).build();
    private static final Long CASE_ID_1 = 1L;

    @Mock
    private CoreCaseDataApi coreCaseDataApi;

    @Mock
    private IdamUserService idamUserService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @InjectMocks
    private CcdRetrievalServiceImpl classUnderTest;

    @Before
    public void setup() {
        ReflectionTestUtils.setField(classUnderTest, "jurisdictionId", JURISDICTION_ID);
        ReflectionTestUtils.setField(classUnderTest, "caseType", CASE_TYPE);
    }

    @Test
    public void givenNoCaseInCcd_whenRetrievePetition_thenReturnNull() throws Exception {
        final String userId = "someUserId";
        final String bearerAuthorisation = "Bearer authorisation";
        final String serviceToken = "serviceToken";

        final UserDetails userDetails = UserDetails.builder().id(userId).build();

        when(idamUserService.retrieveUserDetails(bearerAuthorisation)).thenReturn(userDetails);
        when(authTokenGenerator.generate()).thenReturn(serviceToken);
        when(coreCaseDataApi
            .searchForCitizen(bearerAuthorisation, serviceToken, userId, JURISDICTION_ID, CASE_TYPE,
                Collections.emptyMap())).thenReturn(null);

        CaseDetails actual = classUnderTest.retrievePetition(AUTHORISATION);

        assertNull(actual);

        verify(idamUserService).retrieveUserDetails(bearerAuthorisation);
        verify(authTokenGenerator).generate();
        verify(coreCaseDataApi).searchForCitizen(bearerAuthorisation, serviceToken, userId, JURISDICTION_ID, CASE_TYPE,
            Collections.emptyMap());
    }

    @Test
    public void givenOneSubmittedCaseInCcd_whenRetrievePetition_thenReturnTheCase() throws Exception {
        final String userId = "someUserId";
        final String bearerAuthorisation = "Bearer authorisation";
        final String serviceToken = "serviceToken";

        final Long caseId = 1L;

        final CaseDetails caseDetails = createCaseDetails(caseId, SUBMITTED_PAYMENT_STATE);

        final UserDetails userDetails = UserDetails.builder().id(userId).build();

        when(idamUserService.retrieveUserDetails(bearerAuthorisation)).thenReturn(userDetails);
        when(authTokenGenerator.generate()).thenReturn(serviceToken);
        when(coreCaseDataApi
            .searchForCitizen(bearerAuthorisation, serviceToken, userId, JURISDICTION_ID, CASE_TYPE,
                Collections.emptyMap())).thenReturn(Collections.singletonList(caseDetails));

        CaseDetails actual = classUnderTest.retrievePetition(AUTHORISATION);

        assertEquals(caseDetails, actual);

        verify(idamUserService).retrieveUserDetails(bearerAuthorisation);
        verify(authTokenGenerator).generate();
        verify(coreCaseDataApi).searchForCitizen(bearerAuthorisation, serviceToken, userId, JURISDICTION_ID, CASE_TYPE,
            Collections.emptyMap());
    }

    @Test
    public void givenMultipleSubmittedCaseInCcd_whenRetrievePetition_thenReturnTheFirstCase() throws Exception {
        final String userId = "someUserId";
        final String bearerAuthorisation = "Bearer authorisation";
        final String serviceToken = "serviceToken";

        final Long caseId1 = 1L;
        final Long caseId2 = 2L;
        final Long caseId3 = 3L;
        final Long caseId4 = 4L;

        final CaseDetails caseDetails1 = createCaseDetails(caseId1, SUBMITTED_PAYMENT_STATE);
        final CaseDetails caseDetails2 = createCaseDetails(caseId2, CitizenCaseState.ISSUED.getValue());
        final CaseDetails caseDetails3 = createCaseDetails(caseId3, CitizenCaseState.PENDING_REJECTION.getValue());
        final CaseDetails caseDetails4 = createCaseDetails(caseId4, CitizenCaseState.AWAITING_DOCUMENTS.getValue());

        final UserDetails userDetails = UserDetails.builder().id(userId).build();

        when(idamUserService.retrieveUserDetails(bearerAuthorisation)).thenReturn(userDetails);
        when(authTokenGenerator.generate()).thenReturn(serviceToken);
        when(coreCaseDataApi
            .searchForCitizen(bearerAuthorisation, serviceToken, userId, JURISDICTION_ID, CASE_TYPE,
                Collections.emptyMap()))
            .thenReturn(Arrays.asList(caseDetails1, caseDetails2, caseDetails3, caseDetails4));

        CaseDetails actual = classUnderTest.retrievePetition(AUTHORISATION);

        assertEquals(caseDetails1, actual);

        verify(idamUserService).retrieveUserDetails(bearerAuthorisation);
        verify(authTokenGenerator).generate();
        verify(coreCaseDataApi).searchForCitizen(bearerAuthorisation, serviceToken, userId, JURISDICTION_ID, CASE_TYPE,
            Collections.emptyMap());
    }

    @Test
    public void givenMultipleSubmittedAndOtherCaseInCcd_whenRetrievePetition_thenReturnFirstSubmittedCase()
        throws Exception {
        final Long caseId2 = 2L;
        final Long caseId3 = 3L;

        final CaseDetails caseDetails1 = createCaseDetails(CASE_ID_1, CitizenCaseState.ISSUED.getValue());
        final CaseDetails caseDetails2 = createCaseDetails(caseId2, SUBMITTED_PAYMENT_STATE);
        final CaseDetails caseDetails3 = createCaseDetails(caseId3, AWAITING_PAYMENT_STATE);

        when(idamUserService.retrieveUserDetails(BEARER_AUTHORISATION)).thenReturn(USER_DETAILS);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
        when(coreCaseDataApi
            .searchForCitizen(BEARER_AUTHORISATION, SERVICE_TOKEN, USER_ID, JURISDICTION_ID, CASE_TYPE,
                Collections.emptyMap())).thenReturn(Arrays.asList(caseDetails1, caseDetails2, caseDetails3));

        CaseDetails actual = classUnderTest.retrievePetition(AUTHORISATION);

        assertEquals(caseDetails1, actual);

        verify(idamUserService).retrieveUserDetails(BEARER_AUTHORISATION);
        verify(authTokenGenerator).generate();
        verify(coreCaseDataApi)
            .searchForCitizen(BEARER_AUTHORISATION, SERVICE_TOKEN, USER_ID, JURISDICTION_ID, CASE_TYPE,
            Collections.emptyMap());
    }

    @Test
    public void givenCaseInAwaitingDecreeNisiState_whenRetrievePetition_thenReturnTheCase() throws Exception {

        // given
        final CaseDetails expectedCaseDetails = createCaseDetails(CASE_ID_1,
            CitizenCaseState.AWAITING_DECREE_NISI.getValue());

        when(idamUserService.retrieveUserDetails(BEARER_AUTHORISATION)).thenReturn(USER_DETAILS);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
        when(coreCaseDataApi
            .searchForCitizen(BEARER_AUTHORISATION, SERVICE_TOKEN, USER_ID, JURISDICTION_ID, CASE_TYPE,
                Collections.emptyMap())).thenReturn(Arrays.asList(expectedCaseDetails));

        // when
        CaseDetails caseDetails = classUnderTest.retrievePetition(AUTHORISATION);

        // then
        assertEquals(expectedCaseDetails, caseDetails);
    }

    @Test
    public void givenOneAwaitingPaymentCaseInCcd_whenRetrievePetition_thenReturnTheCase() throws Exception {
        final String userId = "someUserId";
        final String bearerAuthorisation = "Bearer authorisation";
        final String serviceToken = "serviceToken";

        final Long caseId = 1L;

        final CaseDetails caseDetails = createCaseDetails(caseId, AWAITING_PAYMENT_STATE);

        final UserDetails userDetails = UserDetails.builder().id(userId).build();

        when(idamUserService.retrieveUserDetails(bearerAuthorisation)).thenReturn(userDetails);
        when(authTokenGenerator.generate()).thenReturn(serviceToken);
        when(coreCaseDataApi
            .searchForCitizen(bearerAuthorisation, serviceToken, userId, JURISDICTION_ID, CASE_TYPE,
                Collections.emptyMap())).thenReturn(Collections.singletonList(caseDetails));

        CaseDetails actual = classUnderTest.retrievePetition(AUTHORISATION);

        assertEquals(caseDetails, actual);

        verify(idamUserService).retrieveUserDetails(bearerAuthorisation);
        verify(authTokenGenerator).generate();
        verify(coreCaseDataApi).searchForCitizen(bearerAuthorisation, serviceToken, userId, JURISDICTION_ID, CASE_TYPE,
            Collections.emptyMap());
    }

    @Test
    public void givenOneAwaitingPaymentAndOtherNonSubmittedCaseInCcd_whenRetrievePetition_thenReturnAwaitingPaymentCase() throws Exception {
        final String userId = "someUserId";
        final String bearerAuthorisation = "Bearer authorisation";
        final String serviceToken = "serviceToken";

        final Long caseId1 = 1L;
        final Long caseId2 = 2L;
        final Long caseId3 = 3L;

        final CaseDetails caseDetails1 = createCaseDetails(caseId1, AWAITING_PAYMENT_STATE);
        final CaseDetails caseDetails2 = createCaseDetails(caseId2, "state1");
        final CaseDetails caseDetails3 = createCaseDetails(caseId3, "state2");

        final UserDetails userDetails = UserDetails.builder().id(userId).build();

        when(idamUserService.retrieveUserDetails(bearerAuthorisation)).thenReturn(userDetails);
        when(authTokenGenerator.generate()).thenReturn(serviceToken);
        when(coreCaseDataApi
            .searchForCitizen(bearerAuthorisation, serviceToken, userId, JURISDICTION_ID, CASE_TYPE,
                Collections.emptyMap())).thenReturn(Arrays.asList(caseDetails1, caseDetails2, caseDetails3));

        CaseDetails actual = classUnderTest.retrievePetition(AUTHORISATION);

        assertEquals(caseDetails1, actual);

        verify(idamUserService).retrieveUserDetails(bearerAuthorisation);
        verify(authTokenGenerator).generate();
        verify(coreCaseDataApi).searchForCitizen(bearerAuthorisation, serviceToken, userId, JURISDICTION_ID, CASE_TYPE,
            Collections.emptyMap());
    }

    @Test(expected = DuplicateCaseException.class)
    public void givenMultipleAwaitingPaymentAndOtherNonSubmittedCaseInCcd_whenRetrievePetition_thenThrowException()
        throws Exception {
        final String userId = "someUserId";
        final String bearerAuthorisation = "Bearer authorisation";
        final String serviceToken = "serviceToken";

        final Long caseId1 = 1L;
        final Long caseId2 = 2L;
        final Long caseId3 = 3L;

        final CaseDetails caseDetails1 = createCaseDetails(caseId1, AWAITING_PAYMENT_STATE);
        final CaseDetails caseDetails2 = createCaseDetails(caseId2, CitizenCaseState.AWAITING_HWF_DECISION.getValue());
        final CaseDetails caseDetails3 = createCaseDetails(caseId3, "state2");

        final UserDetails userDetails = UserDetails.builder().id(userId).build();

        when(idamUserService.retrieveUserDetails(bearerAuthorisation)).thenReturn(userDetails);
        when(authTokenGenerator.generate()).thenReturn(serviceToken);
        when(coreCaseDataApi
            .searchForCitizen(bearerAuthorisation, serviceToken, userId, JURISDICTION_ID, CASE_TYPE,
                Collections.emptyMap())).thenReturn(Arrays.asList(caseDetails1, caseDetails2, caseDetails3));

        classUnderTest.retrievePetition(AUTHORISATION);

        verify(idamUserService).retrieveUserDetails(bearerAuthorisation);
        verify(authTokenGenerator).generate();
        verify(coreCaseDataApi).searchForCitizen(bearerAuthorisation, serviceToken, userId, JURISDICTION_ID, CASE_TYPE,
            Collections.emptyMap());
    }

    @Test
    public void givenCasesInNotAwaitingPaymentAndNonSubmittedCaseInCcd_whenRetrievePetition_thenReturnNull() throws Exception {
        final String userId = "someUserId";
        final String bearerAuthorisation = "Bearer authorisation";
        final String serviceToken = "serviceToken";

        final Long caseId1 = 1L;
        final Long caseId2 = 2L;
        final Long caseId3 = 3L;

        final CaseDetails caseDetails1 = createCaseDetails(caseId1, "state1");
        final CaseDetails caseDetails2 = createCaseDetails(caseId2, "state2");
        final CaseDetails caseDetails3 = createCaseDetails(caseId3, "state3");

        final UserDetails userDetails = UserDetails.builder().id(userId).build();

        when(idamUserService.retrieveUserDetails(bearerAuthorisation)).thenReturn(userDetails);
        when(authTokenGenerator.generate()).thenReturn(serviceToken);
        when(coreCaseDataApi
            .searchForCitizen(bearerAuthorisation, serviceToken, userId, JURISDICTION_ID, CASE_TYPE,
                Collections.emptyMap())).thenReturn(Arrays.asList(caseDetails1, caseDetails2, caseDetails3));

        CaseDetails actual = classUnderTest.retrievePetition(AUTHORISATION);

        assertNull(actual);

        verify(idamUserService).retrieveUserDetails(bearerAuthorisation);
        verify(authTokenGenerator).generate();
        verify(coreCaseDataApi).searchForCitizen(bearerAuthorisation, serviceToken, userId, JURISDICTION_ID, CASE_TYPE,
            Collections.emptyMap());
    }

    private CaseDetails createCaseDetails(Long id, String state) {
        return CaseDetails.builder().id(id).state(state).build();
    }

}
