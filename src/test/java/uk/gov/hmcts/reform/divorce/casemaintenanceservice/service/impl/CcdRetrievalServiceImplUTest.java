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
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CaseState;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.UserDetails;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.exception.DuplicateCaseException;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.UserService;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CaseRetrievalStateMap.PETITIONER_CASE_STATE_GROUPING;

@RunWith(MockitoJUnitRunner.class)
public class CcdRetrievalServiceImplUTest {
    private static final String JURISDICTION_ID = "someJurisdictionId";
    private static final String CASE_TYPE = "someCaseType";

    @Mock
    private CoreCaseDataApi coreCaseDataApi;

    @Mock
    private UserService userService;

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
    public void givenNoCaseInCcd_whenRetrieveCase_thenReturnNull() throws Exception {
        final String userId = "someUserId";
        final String authorisation = "authorisation";
        final String bearerAuthorisation = "Bearer authorisation";
        final String serviceToken = "serviceToken";

        final UserDetails userDetails = UserDetails.builder().id(userId).build();

        when(userService.retrieveUserDetails(bearerAuthorisation)).thenReturn(userDetails);
        when(authTokenGenerator.generate()).thenReturn(serviceToken);
        when(coreCaseDataApi
            .searchForCitizen(bearerAuthorisation, serviceToken, userId, JURISDICTION_ID, CASE_TYPE,
                Collections.emptyMap())).thenReturn(null);

        CaseDetails actual = classUnderTest.retrieveCase(authorisation, PETITIONER_CASE_STATE_GROUPING);

        assertNull(actual);

        verify(userService).retrieveUserDetails(bearerAuthorisation);
        verify(authTokenGenerator).generate();
        verify(coreCaseDataApi).searchForCitizen(bearerAuthorisation, serviceToken, userId, JURISDICTION_ID, CASE_TYPE,
            Collections.emptyMap());
    }

    @Test
    public void givenOneCompleteCaseInCcd_whenRetrieveCase_thenReturnTheCase() throws Exception {
        final String userId = "someUserId";
        final String authorisation = "authorisation";
        final String bearerAuthorisation = "Bearer authorisation";
        final String serviceToken = "serviceToken";

        final Long caseId = 1L;

        final CaseDetails caseDetails = createCaseDetails(caseId, CaseState.SUBMITTED.getValue());

        final UserDetails userDetails = UserDetails.builder().id(userId).build();

        when(userService.retrieveUserDetails(bearerAuthorisation)).thenReturn(userDetails);
        when(authTokenGenerator.generate()).thenReturn(serviceToken);
        when(coreCaseDataApi
            .searchForCitizen(bearerAuthorisation, serviceToken, userId, JURISDICTION_ID, CASE_TYPE,
                Collections.emptyMap())).thenReturn(Collections.singletonList(caseDetails));

        CaseDetails actual = classUnderTest.retrieveCase(authorisation, PETITIONER_CASE_STATE_GROUPING);

        assertEquals(caseDetails, actual);

        verify(userService).retrieveUserDetails(bearerAuthorisation);
        verify(authTokenGenerator).generate();
        verify(coreCaseDataApi).searchForCitizen(bearerAuthorisation, serviceToken, userId, JURISDICTION_ID, CASE_TYPE,
            Collections.emptyMap());
    }

    @Test
    public void givenMultipleCompletedCaseInCcd_whenRetrieveCase_thenReturnTheFirstCase() throws Exception {
        final String userId = "someUserId";
        final String authorisation = "authorisation";
        final String bearerAuthorisation = "Bearer authorisation";
        final String serviceToken = "serviceToken";

        final Long caseId1 = 1L;
        final Long caseId2 = 2L;
        final Long caseId3 = 3L;
        final Long caseId4 = 4L;

        final CaseDetails caseDetails1 = createCaseDetails(caseId1, CaseState.SUBMITTED.getValue());
        final CaseDetails caseDetails2 = createCaseDetails(caseId2, CaseState.ISSUED.getValue());
        final CaseDetails caseDetails3 = createCaseDetails(caseId3, CaseState.PENDING_REJECTION.getValue());
        final CaseDetails caseDetails4 = createCaseDetails(caseId4, CaseState.AWAITING_DOCUMENTS.getValue());

        final UserDetails userDetails = UserDetails.builder().id(userId).build();

        when(userService.retrieveUserDetails(bearerAuthorisation)).thenReturn(userDetails);
        when(authTokenGenerator.generate()).thenReturn(serviceToken);
        when(coreCaseDataApi
            .searchForCitizen(bearerAuthorisation, serviceToken, userId, JURISDICTION_ID, CASE_TYPE,
                Collections.emptyMap()))
            .thenReturn(Arrays.asList(caseDetails1, caseDetails2, caseDetails3, caseDetails4));

        CaseDetails actual = classUnderTest.retrieveCase(authorisation, PETITIONER_CASE_STATE_GROUPING);

        assertEquals(caseDetails1, actual);

        verify(userService).retrieveUserDetails(bearerAuthorisation);
        verify(authTokenGenerator).generate();
        verify(coreCaseDataApi).searchForCitizen(bearerAuthorisation, serviceToken, userId, JURISDICTION_ID, CASE_TYPE,
            Collections.emptyMap());
    }

    @Test
    public void givenMultipleCompletedAndOtherCaseInCcd_whenRetrieveCase_thenReturnFirstCompleteCase()
        throws Exception {
        final String userId = "someUserId";
        final String authorisation = "authorisation";
        final String bearerAuthorisation = "Bearer authorisation";
        final String serviceToken = "serviceToken";

        final Long caseId1 = 1L;
        final Long caseId2 = 2L;
        final Long caseId3 = 3L;

        final CaseDetails caseDetails1 = createCaseDetails(caseId1, CaseState.ISSUED.getValue());
        final CaseDetails caseDetails2 = createCaseDetails(caseId2, CaseState.SUBMITTED.getValue());
        final CaseDetails caseDetails3 = createCaseDetails(caseId3, CaseState.AWAITING_PAYMENT.getValue());

        final UserDetails userDetails = UserDetails.builder().id(userId).build();

        when(userService.retrieveUserDetails(bearerAuthorisation)).thenReturn(userDetails);
        when(authTokenGenerator.generate()).thenReturn(serviceToken);
        when(coreCaseDataApi
            .searchForCitizen(bearerAuthorisation, serviceToken, userId, JURISDICTION_ID, CASE_TYPE,
                Collections.emptyMap())).thenReturn(Arrays.asList(caseDetails1, caseDetails2, caseDetails3));

        CaseDetails actual = classUnderTest.retrieveCase(authorisation, PETITIONER_CASE_STATE_GROUPING);

        assertEquals(caseDetails1, actual);

        verify(userService).retrieveUserDetails(bearerAuthorisation);
        verify(authTokenGenerator).generate();
        verify(coreCaseDataApi).searchForCitizen(bearerAuthorisation, serviceToken, userId, JURISDICTION_ID, CASE_TYPE,
            Collections.emptyMap());
    }

    @Test
    public void givenOneInCompleteCaseInCcd_whenRetrieveCase_thenReturnTheCase() throws Exception {
        final String userId = "someUserId";
        final String authorisation = "authorisation";
        final String bearerAuthorisation = "Bearer authorisation";
        final String serviceToken = "serviceToken";

        final Long caseId = 1L;

        final CaseDetails caseDetails = createCaseDetails(caseId, CaseState.AWAITING_PAYMENT.getValue());

        final UserDetails userDetails = UserDetails.builder().id(userId).build();

        when(userService.retrieveUserDetails(bearerAuthorisation)).thenReturn(userDetails);
        when(authTokenGenerator.generate()).thenReturn(serviceToken);
        when(coreCaseDataApi
            .searchForCitizen(bearerAuthorisation, serviceToken, userId, JURISDICTION_ID, CASE_TYPE,
                Collections.emptyMap())).thenReturn(Collections.singletonList(caseDetails));

        CaseDetails actual = classUnderTest.retrieveCase(authorisation, PETITIONER_CASE_STATE_GROUPING);

        assertEquals(caseDetails, actual);

        verify(userService).retrieveUserDetails(bearerAuthorisation);
        verify(authTokenGenerator).generate();
        verify(coreCaseDataApi).searchForCitizen(bearerAuthorisation, serviceToken, userId, JURISDICTION_ID, CASE_TYPE,
            Collections.emptyMap());
    }

    @Test
    public void givenOneInCompleteAndOtherNonSubmittedCaseInCcd_whenRetrieveCase_thenReturnInCompleteCase()
        throws Exception {
        final String userId = "someUserId";
        final String authorisation = "authorisation";
        final String bearerAuthorisation = "Bearer authorisation";
        final String serviceToken = "serviceToken";

        final Long caseId1 = 1L;
        final Long caseId2 = 2L;
        final Long caseId3 = 3L;

        final CaseDetails caseDetails1 = createCaseDetails(caseId1, CaseState.AWAITING_PAYMENT.getValue());
        final CaseDetails caseDetails2 = createCaseDetails(caseId2, "state1");
        final CaseDetails caseDetails3 = createCaseDetails(caseId3, "state2");

        final UserDetails userDetails = UserDetails.builder().id(userId).build();

        when(userService.retrieveUserDetails(bearerAuthorisation)).thenReturn(userDetails);
        when(authTokenGenerator.generate()).thenReturn(serviceToken);
        when(coreCaseDataApi
            .searchForCitizen(bearerAuthorisation, serviceToken, userId, JURISDICTION_ID, CASE_TYPE,
                Collections.emptyMap())).thenReturn(Arrays.asList(caseDetails1, caseDetails2, caseDetails3));

        CaseDetails actual = classUnderTest.retrieveCase(authorisation, PETITIONER_CASE_STATE_GROUPING);

        assertEquals(caseDetails1, actual);

        verify(userService).retrieveUserDetails(bearerAuthorisation);
        verify(authTokenGenerator).generate();
        verify(coreCaseDataApi).searchForCitizen(bearerAuthorisation, serviceToken, userId, JURISDICTION_ID, CASE_TYPE,
            Collections.emptyMap());
    }

    @Test(expected = DuplicateCaseException.class)
    public void givenMultipleInCompleteAndOtherNonCompletedCaseInCcd_whenRetrieveCase_thenThrowException()
        throws Exception {
        final String userId = "someUserId";
        final String authorisation = "authorisation";
        final String bearerAuthorisation = "Bearer authorisation";
        final String serviceToken = "serviceToken";

        final Long caseId1 = 1L;
        final Long caseId2 = 2L;
        final Long caseId3 = 3L;

        final CaseDetails caseDetails1 = createCaseDetails(caseId1, CaseState.AWAITING_PAYMENT.getValue());
        final CaseDetails caseDetails2 = createCaseDetails(caseId2, CaseState.AWAITING_HWF_DECISION.getValue());
        final CaseDetails caseDetails3 = createCaseDetails(caseId3, "state2");

        final UserDetails userDetails = UserDetails.builder().id(userId).build();

        when(userService.retrieveUserDetails(bearerAuthorisation)).thenReturn(userDetails);
        when(authTokenGenerator.generate()).thenReturn(serviceToken);
        when(coreCaseDataApi
            .searchForCitizen(bearerAuthorisation, serviceToken, userId, JURISDICTION_ID, CASE_TYPE,
                Collections.emptyMap())).thenReturn(Arrays.asList(caseDetails1, caseDetails2, caseDetails3));

        classUnderTest.retrieveCase(authorisation, PETITIONER_CASE_STATE_GROUPING);

        verify(userService).retrieveUserDetails(bearerAuthorisation);
        verify(authTokenGenerator).generate();
        verify(coreCaseDataApi).searchForCitizen(bearerAuthorisation, serviceToken, userId, JURISDICTION_ID, CASE_TYPE,
            Collections.emptyMap());
    }

    @Test
    public void givenCasesInNonNonCompleteAndNonCompleteCaseInCcd_whenRetrieveCase_thenReturnNull() throws Exception {
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

        when(userService.retrieveUserDetails(bearerAuthorisation)).thenReturn(userDetails);
        when(authTokenGenerator.generate()).thenReturn(serviceToken);
        when(coreCaseDataApi
            .searchForCitizen(bearerAuthorisation, serviceToken, userId, JURISDICTION_ID, CASE_TYPE,
                Collections.emptyMap())).thenReturn(Arrays.asList(caseDetails1, caseDetails2, caseDetails3));

        CaseDetails actual = classUnderTest.retrieveCase(authorisation, PETITIONER_CASE_STATE_GROUPING);

        assertNull(actual);

        verify(userService).retrieveUserDetails(bearerAuthorisation);
        verify(authTokenGenerator).generate();
        verify(coreCaseDataApi).searchForCitizen(bearerAuthorisation, serviceToken, userId, JURISDICTION_ID, CASE_TYPE,
            Collections.emptyMap());
    }

    private CaseDetails createCaseDetails(Long id, String state) {
        return CaseDetails.builder().id(id).state(state).build();
    }

}
