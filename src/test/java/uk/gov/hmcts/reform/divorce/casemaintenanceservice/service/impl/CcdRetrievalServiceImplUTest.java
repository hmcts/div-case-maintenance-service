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
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.domain.model.CitizenCaseState;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.domain.model.UserDetails;
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
    private static final String AWAITING_PAYMENT_STATE = CitizenCaseState.INCOMPLETE.getStates().get(0);
    private static final String SUBMITTED_PAYMENT_STATE = CitizenCaseState.COMPLETE.getStates().get(0);

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
        final String authorisation = "authorisation";
        final String bearerAuthorisation = "Bearer authorisation";
        final String serviceToken = "serviceToken";

        final UserDetails userDetails = UserDetails.builder().id(userId).build();

        when(idamUserService.retrieveUserDetails(bearerAuthorisation)).thenReturn(userDetails);
        when(authTokenGenerator.generate()).thenReturn(serviceToken);
        when(coreCaseDataApi
            .searchForCitizen(bearerAuthorisation, serviceToken, userId, JURISDICTION_ID, CASE_TYPE,
                Collections.emptyMap())).thenReturn(null);

        CaseDetails actual = classUnderTest.retrievePetition(authorisation);

        assertNull(actual);

        verify(idamUserService).retrieveUserDetails(bearerAuthorisation);
        verify(authTokenGenerator).generate();
        verify(coreCaseDataApi).searchForCitizen(bearerAuthorisation, serviceToken, userId, JURISDICTION_ID, CASE_TYPE,
            Collections.emptyMap());
    }

    @Test
    public void givenOneSubmittedCaseInCcd_whenRetrievePetition_thenReturnTheCase() throws Exception {
        final String userId = "someUserId";
        final String authorisation = "authorisation";
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

        CaseDetails actual = classUnderTest.retrievePetition(authorisation);

        assertEquals(caseDetails, actual);

        verify(idamUserService).retrieveUserDetails(bearerAuthorisation);
        verify(authTokenGenerator).generate();
        verify(coreCaseDataApi).searchForCitizen(bearerAuthorisation, serviceToken, userId, JURISDICTION_ID, CASE_TYPE,
            Collections.emptyMap());
    }

    @Test
    public void givenMultipleSubmittedCaseInCcd_whenRetrievePetition_thenReturnTheFirstCase() throws Exception {
        final String userId = "someUserId";
        final String authorisation = "authorisation";
        final String bearerAuthorisation = "Bearer authorisation";
        final String serviceToken = "serviceToken";

        final Long caseId1 = 1L;
        final Long caseId2 = 2L;
        final Long caseId3 = 3L;
        final Long caseId4 = 4L;

        final CaseDetails caseDetails1 = createCaseDetails(caseId1, SUBMITTED_PAYMENT_STATE);
        final CaseDetails caseDetails2 = createCaseDetails(caseId2, CitizenCaseState.COMPLETE.getStates().get(1));
        final CaseDetails caseDetails3 = createCaseDetails(caseId3, CitizenCaseState.COMPLETE.getStates().get(2));
        final CaseDetails caseDetails4 = createCaseDetails(caseId4, CitizenCaseState.COMPLETE.getStates().get(3));

        final UserDetails userDetails = UserDetails.builder().id(userId).build();

        when(idamUserService.retrieveUserDetails(bearerAuthorisation)).thenReturn(userDetails);
        when(authTokenGenerator.generate()).thenReturn(serviceToken);
        when(coreCaseDataApi
            .searchForCitizen(bearerAuthorisation, serviceToken, userId, JURISDICTION_ID, CASE_TYPE,
                Collections.emptyMap()))
            .thenReturn(Arrays.asList(caseDetails1, caseDetails2, caseDetails3, caseDetails4));

        CaseDetails actual = classUnderTest.retrievePetition(authorisation);

        assertEquals(caseDetails1, actual);

        verify(idamUserService).retrieveUserDetails(bearerAuthorisation);
        verify(authTokenGenerator).generate();
        verify(coreCaseDataApi).searchForCitizen(bearerAuthorisation, serviceToken, userId, JURISDICTION_ID, CASE_TYPE,
            Collections.emptyMap());
    }

    @Test
    public void givenMultipleSubmittedAndOtherCaseInCcd_whenRetrievePetition_thenReturnFirstSubmittedCase() throws Exception {
        final String userId = "someUserId";
        final String authorisation = "authorisation";
        final String bearerAuthorisation = "Bearer authorisation";
        final String serviceToken = "serviceToken";

        final Long caseId1 = 1L;
        final Long caseId2 = 2L;
        final Long caseId3 = 3L;

        final CaseDetails caseDetails1 = createCaseDetails(caseId1, CitizenCaseState.COMPLETE.getStates().get(1));
        final CaseDetails caseDetails2 = createCaseDetails(caseId2, SUBMITTED_PAYMENT_STATE);
        final CaseDetails caseDetails3 = createCaseDetails(caseId3, AWAITING_PAYMENT_STATE);

        final UserDetails userDetails = UserDetails.builder().id(userId).build();

        when(idamUserService.retrieveUserDetails(bearerAuthorisation)).thenReturn(userDetails);
        when(authTokenGenerator.generate()).thenReturn(serviceToken);
        when(coreCaseDataApi
            .searchForCitizen(bearerAuthorisation, serviceToken, userId, JURISDICTION_ID, CASE_TYPE,
                Collections.emptyMap())).thenReturn(Arrays.asList(caseDetails1, caseDetails2, caseDetails3));

        CaseDetails actual = classUnderTest.retrievePetition(authorisation);

        assertEquals(caseDetails1, actual);

        verify(idamUserService).retrieveUserDetails(bearerAuthorisation);
        verify(authTokenGenerator).generate();
        verify(coreCaseDataApi).searchForCitizen(bearerAuthorisation, serviceToken, userId, JURISDICTION_ID, CASE_TYPE,
            Collections.emptyMap());
    }

    @Test
    public void givenOneAwaitingPaymentCaseInCcd_whenRetrievePetition_thenReturnTheCase() throws Exception {
        final String userId = "someUserId";
        final String authorisation = "authorisation";
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

        CaseDetails actual = classUnderTest.retrievePetition(authorisation);

        assertEquals(caseDetails, actual);

        verify(idamUserService).retrieveUserDetails(bearerAuthorisation);
        verify(authTokenGenerator).generate();
        verify(coreCaseDataApi).searchForCitizen(bearerAuthorisation, serviceToken, userId, JURISDICTION_ID, CASE_TYPE,
            Collections.emptyMap());
    }

    @Test
    public void givenOneAwaitingPaymentAndOtherNonSubmittedCaseInCcd_whenRetrievePetition_thenReturnAwaitingPaymentCase() throws Exception {
        final String userId = "someUserId";
        final String authorisation = "authorisation";
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

        CaseDetails actual = classUnderTest.retrievePetition(authorisation);

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
        final String authorisation = "authorisation";
        final String bearerAuthorisation = "Bearer authorisation";
        final String serviceToken = "serviceToken";

        final Long caseId1 = 1L;
        final Long caseId2 = 2L;
        final Long caseId3 = 3L;

        final CaseDetails caseDetails1 = createCaseDetails(caseId1, AWAITING_PAYMENT_STATE);
        final CaseDetails caseDetails2 = createCaseDetails(caseId2, CitizenCaseState.INCOMPLETE.getStates().get(1));
        final CaseDetails caseDetails3 = createCaseDetails(caseId3, "state2");

        final UserDetails userDetails = UserDetails.builder().id(userId).build();

        when(idamUserService.retrieveUserDetails(bearerAuthorisation)).thenReturn(userDetails);
        when(authTokenGenerator.generate()).thenReturn(serviceToken);
        when(coreCaseDataApi
            .searchForCitizen(bearerAuthorisation, serviceToken, userId, JURISDICTION_ID, CASE_TYPE,
                Collections.emptyMap())).thenReturn(Arrays.asList(caseDetails1, caseDetails2, caseDetails3));

        classUnderTest.retrievePetition(authorisation);

        verify(idamUserService).retrieveUserDetails(bearerAuthorisation);
        verify(authTokenGenerator).generate();
        verify(coreCaseDataApi).searchForCitizen(bearerAuthorisation, serviceToken, userId, JURISDICTION_ID, CASE_TYPE,
            Collections.emptyMap());
    }

    @Test
    public void givenCasesInNotAwaitingPaymentAndNonSubmittedCaseInCcd_whenRetrievePetition_thenReturnNull() throws Exception {
        final String userId = "someUserId";
        final String authorisation = "authorisation";
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

        CaseDetails actual = classUnderTest.retrievePetition(authorisation);

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
