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
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.domain.model.CitizenCaseState;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.exception.DuplicateCaseException;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.UserService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CaseRetrievalStateMap.PETITIONER_CASE_STATE_GROUPING;

@RunWith(MockitoJUnitRunner.class)
public class CcdRetrievalServiceImplUTest {

    private static final String JURISDICTION_ID = "someJurisdictionId";
    private static final String CASE_TYPE = "someCaseType";
    private static final String AWAITING_PAYMENT_STATE = CitizenCaseState.AWAITING_PAYMENT.getValue();
    private static final String AUTHORISATION = "authorisation";
    private static final String BEARER_AUTHORISATION = "Bearer authorisation";
    private static final String SERVICE_TOKEN = "serviceToken";
    private static final String USER_ID = "someUserId";
    private static final UserDetails USER_DETAILS = UserDetails.builder().id(USER_ID).build();
    private static final Long CASE_ID_1 = 1L;
    private static final String CASEWORKER_ROLE = "caseworker";
    private static final String CITIZEN_ROLE = "citizen";

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

        final UserDetails userDetails = UserDetails.builder().id(USER_ID).build();

        when(userService.retrieveUserDetails(BEARER_AUTHORISATION)).thenReturn(userDetails);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
        when(coreCaseDataApi
            .searchForCitizen(BEARER_AUTHORISATION, SERVICE_TOKEN, USER_ID, JURISDICTION_ID, CASE_TYPE,
                Collections.emptyMap())).thenReturn(null);

        CaseDetails actual = classUnderTest.retrieveCase(AUTHORISATION, PETITIONER_CASE_STATE_GROUPING);

        assertNull(actual);

        verify(userService).retrieveUserDetails(BEARER_AUTHORISATION);
        verify(authTokenGenerator).generate();
        verify(coreCaseDataApi)
            .searchForCitizen(BEARER_AUTHORISATION, SERVICE_TOKEN, USER_ID, JURISDICTION_ID, CASE_TYPE,
                Collections.emptyMap());
    }

    @Test
    public void givenOneCompleteCaseInCcd_whenRetrieveCase_thenReturnTheCase() throws Exception {

        final Long caseId = 1L;

        final CaseDetails caseDetails = createCaseDetails(caseId, CaseState.SUBMITTED.getValue());

        final UserDetails userDetails = UserDetails.builder().id(USER_ID).build();

        when(userService.retrieveUserDetails(BEARER_AUTHORISATION)).thenReturn(userDetails);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
        when(coreCaseDataApi
            .searchForCitizen(BEARER_AUTHORISATION, SERVICE_TOKEN, USER_ID, JURISDICTION_ID, CASE_TYPE,
                Collections.emptyMap())).thenReturn(Collections.singletonList(caseDetails));

        CaseDetails actual = classUnderTest.retrieveCase(AUTHORISATION, PETITIONER_CASE_STATE_GROUPING);

        assertEquals(caseDetails, actual);

        verify(userService).retrieveUserDetails(BEARER_AUTHORISATION);
        verify(authTokenGenerator).generate();
        verify(coreCaseDataApi)
            .searchForCitizen(BEARER_AUTHORISATION, SERVICE_TOKEN, USER_ID, JURISDICTION_ID, CASE_TYPE,
                Collections.emptyMap());
    }

    @Test
    public void givenMultipleCompletedCaseInCcd_whenRetrieveCase_thenReturnTheFirstCase() throws Exception {

        final Long caseId1 = 1L;
        final Long caseId2 = 2L;
        final Long caseId3 = 3L;
        final Long caseId4 = 4L;

        final CaseDetails caseDetails1 = createCaseDetails(caseId1, CaseState.SUBMITTED.getValue());
        final CaseDetails caseDetails2 = createCaseDetails(caseId2, CaseState.ISSUED.getValue());
        final CaseDetails caseDetails3 = createCaseDetails(caseId3, CaseState.PENDING_REJECTION.getValue());
        final CaseDetails caseDetails4 = createCaseDetails(caseId4, CaseState.AWAITING_DOCUMENTS.getValue());

        final UserDetails userDetails = UserDetails.builder().id(USER_ID).build();

        when(userService.retrieveUserDetails(BEARER_AUTHORISATION)).thenReturn(userDetails);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
        when(coreCaseDataApi
            .searchForCitizen(BEARER_AUTHORISATION, SERVICE_TOKEN, USER_ID, JURISDICTION_ID, CASE_TYPE,
                Collections.emptyMap()))
            .thenReturn(Arrays.asList(caseDetails1, caseDetails2, caseDetails3, caseDetails4));

        CaseDetails actual = classUnderTest.retrieveCase(AUTHORISATION, PETITIONER_CASE_STATE_GROUPING);

        assertEquals(caseDetails1, actual);

        verify(userService).retrieveUserDetails(BEARER_AUTHORISATION);
        verify(authTokenGenerator).generate();
        verify(coreCaseDataApi)
            .searchForCitizen(BEARER_AUTHORISATION, SERVICE_TOKEN, USER_ID, JURISDICTION_ID, CASE_TYPE,
                Collections.emptyMap());
    }

    @Test
    public void givenMultipleCompletedAndOtherCaseInCcd_whenRetrieveCase_thenReturnFirstCompleteCase()
        throws Exception {

        final Long caseId2 = 2L;
        final Long caseId3 = 3L;

        final CaseDetails caseDetails1 = createCaseDetails(CASE_ID_1, CitizenCaseState.ISSUED.getValue());
        final CaseDetails caseDetails2 = createCaseDetails(caseId2, CaseState.SUBMITTED.getValue());
        final CaseDetails caseDetails3 = createCaseDetails(caseId3, CaseState.AWAITING_PAYMENT.getValue());

        final UserDetails userDetails = UserDetails.builder().id(USER_ID).build();

        when(userService.retrieveUserDetails(BEARER_AUTHORISATION)).thenReturn(userDetails);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);

        when(coreCaseDataApi
            .searchForCitizen(BEARER_AUTHORISATION, SERVICE_TOKEN, USER_ID, JURISDICTION_ID, CASE_TYPE,
                Collections.emptyMap())).thenReturn(Arrays.asList(caseDetails1, caseDetails2, caseDetails3));

        CaseDetails actual = classUnderTest.retrieveCase(AUTHORISATION, PETITIONER_CASE_STATE_GROUPING);

        assertEquals(caseDetails1, actual);

        verify(userService).retrieveUserDetails(BEARER_AUTHORISATION);
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

        when(userService.retrieveUserDetails(BEARER_AUTHORISATION)).thenReturn(USER_DETAILS);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
        when(coreCaseDataApi
            .searchForCitizen(BEARER_AUTHORISATION, SERVICE_TOKEN, USER_ID, JURISDICTION_ID, CASE_TYPE,
                Collections.emptyMap())).thenReturn(Collections.singletonList(expectedCaseDetails));

        // when
        CaseDetails caseDetails = classUnderTest.retrieveCase(AUTHORISATION, PETITIONER_CASE_STATE_GROUPING);

        // then
        assertEquals(expectedCaseDetails, caseDetails);
    }

    @Test
    public void givenOneInCompleteCaseInCcd_whenRetrieveCase_thenReturnTheCase() throws Exception {

        final Long caseId = 1L;

        final CaseDetails caseDetails = createCaseDetails(caseId, CaseState.AWAITING_PAYMENT.getValue());

        final UserDetails userDetails = UserDetails.builder().id(USER_ID).build();

        when(userService.retrieveUserDetails(BEARER_AUTHORISATION)).thenReturn(userDetails);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
        when(coreCaseDataApi
            .searchForCitizen(BEARER_AUTHORISATION, SERVICE_TOKEN, USER_ID, JURISDICTION_ID, CASE_TYPE,
                Collections.emptyMap())).thenReturn(Collections.singletonList(caseDetails));

        CaseDetails actual = classUnderTest.retrieveCase(AUTHORISATION, PETITIONER_CASE_STATE_GROUPING);

        assertEquals(caseDetails, actual);

        verify(userService).retrieveUserDetails(BEARER_AUTHORISATION);
        verify(authTokenGenerator).generate();
        verify(coreCaseDataApi)
            .searchForCitizen(BEARER_AUTHORISATION, SERVICE_TOKEN, USER_ID, JURISDICTION_ID, CASE_TYPE,
                Collections.emptyMap());
    }

    @Test
    public void givenOneInCompleteAndOtherNonSubmittedCaseInCcd_whenRetrieveCase_thenReturnInCompleteCase()
        throws Exception {

        final Long caseId1 = 1L;
        final Long caseId2 = 2L;
        final Long caseId3 = 3L;

        final CaseDetails caseDetails1 = createCaseDetails(caseId1, CaseState.AWAITING_PAYMENT.getValue());
        final CaseDetails caseDetails2 = createCaseDetails(caseId2, "state1");
        final CaseDetails caseDetails3 = createCaseDetails(caseId3, "state2");

        final UserDetails userDetails = UserDetails.builder().id(USER_ID).build();

        when(userService.retrieveUserDetails(BEARER_AUTHORISATION)).thenReturn(userDetails);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
        when(coreCaseDataApi
            .searchForCitizen(BEARER_AUTHORISATION, SERVICE_TOKEN, USER_ID, JURISDICTION_ID, CASE_TYPE,
                Collections.emptyMap())).thenReturn(Arrays.asList(caseDetails1, caseDetails2, caseDetails3));

        CaseDetails actual = classUnderTest.retrieveCase(AUTHORISATION, PETITIONER_CASE_STATE_GROUPING);

        assertEquals(caseDetails1, actual);

        verify(userService).retrieveUserDetails(BEARER_AUTHORISATION);
        verify(authTokenGenerator).generate();
        verify(coreCaseDataApi)
            .searchForCitizen(BEARER_AUTHORISATION, SERVICE_TOKEN, USER_ID, JURISDICTION_ID, CASE_TYPE,
                Collections.emptyMap());
    }

    @Test(expected = DuplicateCaseException.class)
    public void givenMultipleInCompleteAndOtherNonCompletedCaseInCcd_whenRetrieveCase_thenThrowException()
        throws Exception {

        final Long caseId1 = 1L;
        final Long caseId2 = 2L;
        final Long caseId3 = 3L;

        final CaseDetails caseDetails1 = createCaseDetails(caseId1, AWAITING_PAYMENT_STATE);
        final CaseDetails caseDetails2 = createCaseDetails(caseId2, CitizenCaseState.AWAITING_HWF_DECISION.getValue());
        final CaseDetails caseDetails3 = createCaseDetails(caseId3, "state2");

        final UserDetails userDetails = UserDetails.builder().id(USER_ID).build();

        when(userService.retrieveUserDetails(BEARER_AUTHORISATION)).thenReturn(userDetails);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
        when(coreCaseDataApi
            .searchForCitizen(BEARER_AUTHORISATION, SERVICE_TOKEN, USER_ID, JURISDICTION_ID, CASE_TYPE,
                Collections.emptyMap())).thenReturn(Arrays.asList(caseDetails1, caseDetails2, caseDetails3));

        classUnderTest.retrieveCase(AUTHORISATION, PETITIONER_CASE_STATE_GROUPING);

        verify(userService).retrieveUserDetails(BEARER_AUTHORISATION);
        verify(authTokenGenerator).generate();
        verify(coreCaseDataApi)
            .searchForCitizen(BEARER_AUTHORISATION, SERVICE_TOKEN, USER_ID, JURISDICTION_ID, CASE_TYPE,
                Collections.emptyMap());
    }

    @Test
    public void givenCasesInNonNonCompleteAndNonCompleteCaseInCcd_whenRetrieveCase_thenReturnNull() throws Exception {
        final Long caseId1 = 1L;
        final Long caseId2 = 2L;
        final Long caseId3 = 3L;

        final CaseDetails caseDetails1 = createCaseDetails(caseId1, "state1");
        final CaseDetails caseDetails2 = createCaseDetails(caseId2, "state2");
        final CaseDetails caseDetails3 = createCaseDetails(caseId3, "state3");

        final UserDetails userDetails = UserDetails.builder().id(USER_ID).build();

        when(userService.retrieveUserDetails(BEARER_AUTHORISATION)).thenReturn(userDetails);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
        when(coreCaseDataApi
            .searchForCitizen(BEARER_AUTHORISATION, SERVICE_TOKEN, USER_ID, JURISDICTION_ID, CASE_TYPE,
                Collections.emptyMap())).thenReturn(Arrays.asList(caseDetails1, caseDetails2, caseDetails3));

        CaseDetails actual = classUnderTest.retrieveCase(AUTHORISATION, PETITIONER_CASE_STATE_GROUPING);

        assertNull(actual);

        verify(userService).retrieveUserDetails(BEARER_AUTHORISATION);
        verify(authTokenGenerator).generate();
        verify(coreCaseDataApi)
            .searchForCitizen(BEARER_AUTHORISATION, SERVICE_TOKEN, USER_ID, JURISDICTION_ID, CASE_TYPE,
                Collections.emptyMap());
    }

    @Test
    public void givenNoCaseInCcd_whenRetrieveCaseWithToken_thenReturnNull() throws Exception {

        final UserDetails userDetails = UserDetails.builder().id(USER_ID).build();

        when(userService.retrieveUserDetails(BEARER_AUTHORISATION)).thenReturn(userDetails);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
        when(coreCaseDataApi
            .searchForCitizen(BEARER_AUTHORISATION, SERVICE_TOKEN, USER_ID, JURISDICTION_ID, CASE_TYPE,
                Collections.emptyMap())).thenReturn(null);

        CaseDetails actual = classUnderTest.retrieveCase(AUTHORISATION);

        assertNull(actual);

        verify(userService).retrieveUserDetails(BEARER_AUTHORISATION);
        verify(authTokenGenerator).generate();
        verify(coreCaseDataApi)
            .searchForCitizen(BEARER_AUTHORISATION, SERVICE_TOKEN, USER_ID, JURISDICTION_ID, CASE_TYPE,
                Collections.emptyMap());
    }

    @Test(expected = DuplicateCaseException.class)
    public void givenMultipleCaseInCcd_whenRetrieveCaseWithToken_thenReturnThrowDuplicateException() throws Exception {
        List<CaseDetails> caseDetailsList = Arrays.asList(CaseDetails.builder().build(), CaseDetails.builder().build());

        final UserDetails userDetails = UserDetails.builder().id(USER_ID).build();

        when(userService.retrieveUserDetails(BEARER_AUTHORISATION)).thenReturn(userDetails);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
        when(coreCaseDataApi
            .searchForCitizen(BEARER_AUTHORISATION, SERVICE_TOKEN, USER_ID, JURISDICTION_ID, CASE_TYPE,
                Collections.emptyMap())).thenReturn(caseDetailsList);

        classUnderTest.retrieveCase(AUTHORISATION);

        verify(userService).retrieveUserDetails(BEARER_AUTHORISATION);
        verify(authTokenGenerator).generate();
        verify(coreCaseDataApi)
            .searchForCitizen(BEARER_AUTHORISATION, SERVICE_TOKEN, USER_ID, JURISDICTION_ID, CASE_TYPE,
                Collections.emptyMap());
    }

    @Test
    public void givenSingleCaseInCcd_whenRetrieveCaseWithToken_thenReturnTheCase() throws Exception {
        CaseDetails caseDetails = CaseDetails.builder().build();
        List<CaseDetails> caseDetailsList = Collections.singletonList(caseDetails);

        final UserDetails userDetails = UserDetails.builder().id(USER_ID).build();

        when(userService.retrieveUserDetails(BEARER_AUTHORISATION)).thenReturn(userDetails);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
        when(coreCaseDataApi
            .searchForCitizen(BEARER_AUTHORISATION, SERVICE_TOKEN, USER_ID, JURISDICTION_ID, CASE_TYPE,
                Collections.emptyMap())).thenReturn(caseDetailsList);

        assertEquals(caseDetails, classUnderTest.retrieveCase(AUTHORISATION));

        verify(userService).retrieveUserDetails(BEARER_AUTHORISATION);
        verify(authTokenGenerator).generate();
        verify(coreCaseDataApi)
            .searchForCitizen(BEARER_AUTHORISATION, SERVICE_TOKEN, USER_ID, JURISDICTION_ID, CASE_TYPE,
                Collections.emptyMap());
    }

    @Test
    public void givenCaseId_whenRetrieveCaseById_thenReturnTheCase() throws Exception {
        String testCaseId = String.valueOf(CASE_ID_1);
        CaseDetails caseDetails = CaseDetails.builder().build();

        final UserDetails userDetails = UserDetails.builder().id(USER_ID).build();

        when(userService.retrieveUserDetails(BEARER_AUTHORISATION)).thenReturn(userDetails);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
        when(coreCaseDataApi
            .readForCitizen(BEARER_AUTHORISATION, SERVICE_TOKEN, USER_ID, JURISDICTION_ID, CASE_TYPE,
                testCaseId)).thenReturn(caseDetails);

        assertEquals(caseDetails, classUnderTest.retrieveCaseById(AUTHORISATION, testCaseId));

        verify(userService).retrieveUserDetails(BEARER_AUTHORISATION);
        verify(authTokenGenerator).generate();
        verify(coreCaseDataApi)
            .readForCitizen(BEARER_AUTHORISATION, SERVICE_TOKEN, USER_ID, JURISDICTION_ID, CASE_TYPE,
                testCaseId);
    }

    @Test
    public void givenCaseId_whenRetrieveCaseByIdWithCaseworker_thenReturnTheCase() throws Exception {
        String testCaseId = String.valueOf(CASE_ID_1);
        CaseDetails caseDetails = CaseDetails.builder().build();

        final UserDetails userDetails = UserDetails.builder()
            .id(USER_ID).roles(Collections.singletonList(CASEWORKER_ROLE)).build();

        when(userService.retrieveUserDetails(BEARER_AUTHORISATION)).thenReturn(userDetails);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
        when(coreCaseDataApi
            .readForCaseWorker(BEARER_AUTHORISATION, SERVICE_TOKEN, USER_ID, JURISDICTION_ID, CASE_TYPE,
                testCaseId)).thenReturn(caseDetails);

        assertEquals(caseDetails, classUnderTest.retrieveCaseById(AUTHORISATION, testCaseId));

        verify(userService).retrieveUserDetails(BEARER_AUTHORISATION);
        verify(authTokenGenerator).generate();
        verify(coreCaseDataApi)
            .readForCaseWorker(BEARER_AUTHORISATION, SERVICE_TOKEN, USER_ID, JURISDICTION_ID, CASE_TYPE,
                testCaseId);
    }

    @Test
    public void givenCaseId_whenRetrieveCaseByIdWithCaseworkerCitizen_thenReturnTheCase() throws Exception {
        String testCaseId = String.valueOf(CASE_ID_1);
        CaseDetails caseDetails = CaseDetails.builder().build();
        List<String> userRoles = Arrays.asList(CASEWORKER_ROLE, CITIZEN_ROLE);

        final UserDetails userDetails = UserDetails.builder()
            .id(USER_ID).roles(userRoles).build();

        when(userService.retrieveUserDetails(BEARER_AUTHORISATION)).thenReturn(userDetails);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
        when(coreCaseDataApi
            .readForCitizen(BEARER_AUTHORISATION, SERVICE_TOKEN, USER_ID, JURISDICTION_ID, CASE_TYPE,
                testCaseId)).thenReturn(caseDetails);

        assertEquals(caseDetails, classUnderTest.retrieveCaseById(AUTHORISATION, testCaseId));

        verify(userService).retrieveUserDetails(BEARER_AUTHORISATION);
        verify(authTokenGenerator).generate();
        verify(coreCaseDataApi)
            .readForCitizen(BEARER_AUTHORISATION, SERVICE_TOKEN, USER_ID, JURISDICTION_ID, CASE_TYPE,
                testCaseId);
    }

    private CaseDetails createCaseDetails(Long id, String state) {
        return CaseDetails.builder().id(id).state(state).build();
    }

}
