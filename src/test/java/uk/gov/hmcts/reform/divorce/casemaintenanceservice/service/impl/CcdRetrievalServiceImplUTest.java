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
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CaseState;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.domain.model.CitizenCaseState;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.exception.DuplicateCaseException;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.UserService;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.TestConstants.TEST_AUTHORISATION;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.TestConstants.TEST_BEARER_AUTHORISATION;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.TestConstants.TEST_CASE_TYPE;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.TestConstants.TEST_JURISDICTION_ID;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.TestConstants.TEST_SERVICE_TOKEN;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CaseRetrievalStateMap.PETITIONER_CASE_STATE_GROUPING;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties.CO_RESP_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties.D8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties.RESP_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CmsConstants.CASEWORKER_ROLE;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CmsConstants.CITIZEN_ROLE;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.DivCaseRole.PETITIONER;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.DivCaseRole.RESPONDENT;

@RunWith(MockitoJUnitRunner.class)
public class CcdRetrievalServiceImplUTest {

    private static final String AWAITING_PAYMENT_STATE = CitizenCaseState.AWAITING_PAYMENT.getValue();
    private static final String USER_ID = "someUserId";
    private static final UserDetails USER_DETAILS = UserDetails.builder()
        .id(USER_ID)
        .email(TEST_USER_EMAIL)
        .build();
    private static final User USER = new User(TEST_BEARER_AUTHORISATION, USER_DETAILS);
    private static final Long CASE_ID_1 = 1L;

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
        ReflectionTestUtils.setField(classUnderTest, "jurisdictionId", TEST_JURISDICTION_ID);
        ReflectionTestUtils.setField(classUnderTest, "caseType", TEST_CASE_TYPE);
    }

    @Test
    public void givenNoCaseInCcd_whenRetrieveCase_thenReturnNull() {
        when(userService.retrieveUser(TEST_BEARER_AUTHORISATION)).thenReturn(USER);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);
        when(coreCaseDataApi
            .searchForCitizen(TEST_BEARER_AUTHORISATION, TEST_SERVICE_TOKEN, USER_ID, TEST_JURISDICTION_ID, TEST_CASE_TYPE,
                Collections.emptyMap())).thenReturn(null);

        CaseDetails actual = classUnderTest.retrieveCase(TEST_AUTHORISATION, PETITIONER_CASE_STATE_GROUPING, PETITIONER);

        assertNull(actual);

        verify(userService).retrieveUser(TEST_BEARER_AUTHORISATION);
        verify(authTokenGenerator).generate();
        verify(coreCaseDataApi)
            .searchForCitizen(TEST_BEARER_AUTHORISATION, TEST_SERVICE_TOKEN, USER_ID, TEST_JURISDICTION_ID, TEST_CASE_TYPE,
                Collections.emptyMap());
    }

    @Test
    public void givenOneCompleteCaseInCcd_whenRetrieveCase_thenReturnTheCase() {
        final Long caseId = 1L;
        final CaseDetails caseDetails = createCaseDetails(caseId, CaseState.SUBMITTED.getValue());

        when(userService.retrieveUser(TEST_BEARER_AUTHORISATION)).thenReturn(USER);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);
        when(coreCaseDataApi
            .searchForCitizen(TEST_BEARER_AUTHORISATION, TEST_SERVICE_TOKEN, USER_ID, TEST_JURISDICTION_ID, TEST_CASE_TYPE,
                Collections.emptyMap())).thenReturn(Collections.singletonList(caseDetails));

        CaseDetails actual = classUnderTest.retrieveCase(TEST_AUTHORISATION, PETITIONER_CASE_STATE_GROUPING, PETITIONER);

        assertEquals(caseDetails, actual);

        verify(userService).retrieveUser(TEST_BEARER_AUTHORISATION);
        verify(authTokenGenerator).generate();
        verify(coreCaseDataApi)
            .searchForCitizen(TEST_BEARER_AUTHORISATION, TEST_SERVICE_TOKEN, USER_ID, TEST_JURISDICTION_ID, TEST_CASE_TYPE,
                Collections.emptyMap());
    }

    @Test
    public void givenMultipleCompletedCaseInCcd_whenRetrieveCase_thenReturnTheFirstCase() {
        final Long caseId1 = 1L;
        final Long caseId2 = 2L;
        final Long caseId3 = 3L;
        final Long caseId4 = 4L;

        final CaseDetails caseDetails1 = createCaseDetails(caseId1, CaseState.SUBMITTED.getValue());
        final CaseDetails caseDetails2 = createCaseDetails(caseId2, CaseState.ISSUED.getValue());
        final CaseDetails caseDetails3 = createCaseDetails(caseId3, CaseState.PENDING_REJECTION.getValue());
        final CaseDetails caseDetails4 = createCaseDetails(caseId4, CaseState.AWAITING_DOCUMENTS.getValue());


        when(userService.retrieveUser(TEST_BEARER_AUTHORISATION)).thenReturn(USER);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);
        when(coreCaseDataApi
            .searchForCitizen(TEST_BEARER_AUTHORISATION, TEST_SERVICE_TOKEN, USER_ID, TEST_JURISDICTION_ID, TEST_CASE_TYPE,
                Collections.emptyMap()))
            .thenReturn(Arrays.asList(caseDetails1, caseDetails2, caseDetails3, caseDetails4));

        CaseDetails actual = classUnderTest.retrieveCase(TEST_AUTHORISATION, PETITIONER_CASE_STATE_GROUPING, PETITIONER);

        assertEquals(caseDetails1, actual);

        verify(userService).retrieveUser(TEST_BEARER_AUTHORISATION);
        verify(authTokenGenerator).generate();
        verify(coreCaseDataApi)
            .searchForCitizen(TEST_BEARER_AUTHORISATION, TEST_SERVICE_TOKEN, USER_ID, TEST_JURISDICTION_ID, TEST_CASE_TYPE,
                Collections.emptyMap());
    }

    @Test
    public void givenMultipleCompletedAndOtherCaseInCcd_whenRetrieveCase_thenReturnFirstCompleteCase() {
        final Long caseId2 = 2L;
        final Long caseId3 = 3L;

        final CaseDetails caseDetails1 = createCaseDetails(CASE_ID_1, CitizenCaseState.ISSUED.getValue());
        final CaseDetails caseDetails2 = createCaseDetails(caseId2, CaseState.SUBMITTED.getValue());
        final CaseDetails caseDetails3 = createCaseDetails(caseId3, CaseState.AWAITING_PAYMENT.getValue());

        when(userService.retrieveUser(TEST_BEARER_AUTHORISATION)).thenReturn(USER);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);

        when(coreCaseDataApi
            .searchForCitizen(TEST_BEARER_AUTHORISATION, TEST_SERVICE_TOKEN, USER_ID, TEST_JURISDICTION_ID, TEST_CASE_TYPE,
                Collections.emptyMap())).thenReturn(Arrays.asList(caseDetails1, caseDetails2, caseDetails3));

        CaseDetails actual = classUnderTest.retrieveCase(TEST_AUTHORISATION, PETITIONER_CASE_STATE_GROUPING, PETITIONER);

        assertEquals(caseDetails1, actual);

        verify(userService).retrieveUser(TEST_BEARER_AUTHORISATION);
        verify(authTokenGenerator).generate();
        verify(coreCaseDataApi)
            .searchForCitizen(TEST_BEARER_AUTHORISATION, TEST_SERVICE_TOKEN, USER_ID, TEST_JURISDICTION_ID, TEST_CASE_TYPE,
                Collections.emptyMap());
    }

    @Test
    public void givenCaseInAwaitingDecreeNisiState_whenRetrievePetition_thenReturnTheCase() {
        final CaseDetails expectedCaseDetails = createCaseDetails(CASE_ID_1,
            CitizenCaseState.AWAITING_DECREE_NISI.getValue());

        when(userService.retrieveUser(TEST_BEARER_AUTHORISATION)).thenReturn(USER);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);
        when(coreCaseDataApi
            .searchForCitizen(TEST_BEARER_AUTHORISATION, TEST_SERVICE_TOKEN, USER_ID, TEST_JURISDICTION_ID, TEST_CASE_TYPE,
                Collections.emptyMap())).thenReturn(Collections.singletonList(expectedCaseDetails));

        CaseDetails caseDetails = classUnderTest.retrieveCase(TEST_AUTHORISATION,
            PETITIONER_CASE_STATE_GROUPING, PETITIONER);

        assertEquals(expectedCaseDetails, caseDetails);
    }

    @Test
    public void givenOneInCompleteCaseInCcd_whenRetrieveCase_thenReturnTheCase() {
        final Long caseId = 1L;

        final CaseDetails caseDetails = createCaseDetails(caseId, CaseState.AWAITING_PAYMENT.getValue());

        when(userService.retrieveUser(TEST_BEARER_AUTHORISATION)).thenReturn(USER);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);
        when(coreCaseDataApi
            .searchForCitizen(TEST_BEARER_AUTHORISATION, TEST_SERVICE_TOKEN, USER_ID, TEST_JURISDICTION_ID, TEST_CASE_TYPE,
                Collections.emptyMap())).thenReturn(Collections.singletonList(caseDetails));

        CaseDetails actual = classUnderTest.retrieveCase(TEST_AUTHORISATION, PETITIONER_CASE_STATE_GROUPING, PETITIONER);

        assertEquals(caseDetails, actual);

        verify(userService).retrieveUser(TEST_BEARER_AUTHORISATION);
        verify(authTokenGenerator).generate();
        verify(coreCaseDataApi)
            .searchForCitizen(TEST_BEARER_AUTHORISATION, TEST_SERVICE_TOKEN, USER_ID, TEST_JURISDICTION_ID, TEST_CASE_TYPE,
                Collections.emptyMap());
    }

    @Test
    public void givenOneInCompleteAndOtherNonSubmittedCaseInCcd_whenRetrieveCase_thenReturnInCompleteCase() {
        final Long caseId1 = 1L;
        final Long caseId2 = 2L;
        final Long caseId3 = 3L;

        final CaseDetails caseDetails1 = createCaseDetails(caseId1, CaseState.AWAITING_PAYMENT.getValue());
        final CaseDetails caseDetails2 = createCaseDetails(caseId2, "state1");
        final CaseDetails caseDetails3 = createCaseDetails(caseId3, "state2");

        when(userService.retrieveUser(TEST_BEARER_AUTHORISATION)).thenReturn(USER);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);
        when(coreCaseDataApi
            .searchForCitizen(TEST_BEARER_AUTHORISATION, TEST_SERVICE_TOKEN, USER_ID, TEST_JURISDICTION_ID, TEST_CASE_TYPE,
                Collections.emptyMap())).thenReturn(Arrays.asList(caseDetails1, caseDetails2, caseDetails3));

        CaseDetails actual = classUnderTest.retrieveCase(TEST_AUTHORISATION, PETITIONER_CASE_STATE_GROUPING, PETITIONER);

        assertEquals(caseDetails1, actual);

        verify(userService).retrieveUser(TEST_BEARER_AUTHORISATION);
        verify(authTokenGenerator).generate();
        verify(coreCaseDataApi)
            .searchForCitizen(TEST_BEARER_AUTHORISATION, TEST_SERVICE_TOKEN, USER_ID, TEST_JURISDICTION_ID, TEST_CASE_TYPE,
                Collections.emptyMap());
    }

    @Test(expected = DuplicateCaseException.class)
    public void givenMultipleInCompleteAndOtherNonCompletedCaseInCcd_whenRetrieveCase_thenThrowException() {

        final Long caseId1 = 1L;
        final Long caseId2 = 2L;
        final Long caseId3 = 3L;

        final CaseDetails caseDetails1 = createCaseDetails(caseId1, AWAITING_PAYMENT_STATE);
        final CaseDetails caseDetails2 = createCaseDetails(caseId2, CitizenCaseState.AWAITING_HWF_DECISION.getValue());
        final CaseDetails caseDetails3 = createCaseDetails(caseId3, "state2");

        when(userService.retrieveUser(TEST_BEARER_AUTHORISATION)).thenReturn(USER);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);
        when(coreCaseDataApi
            .searchForCitizen(TEST_BEARER_AUTHORISATION, TEST_SERVICE_TOKEN, USER_ID, TEST_JURISDICTION_ID, TEST_CASE_TYPE,
                Collections.emptyMap())).thenReturn(Arrays.asList(caseDetails1, caseDetails2, caseDetails3));

        classUnderTest.retrieveCase(TEST_AUTHORISATION, PETITIONER_CASE_STATE_GROUPING, PETITIONER);

        verify(userService).retrieveUser(TEST_BEARER_AUTHORISATION);
        verify(authTokenGenerator).generate();
        verify(coreCaseDataApi)
            .searchForCitizen(TEST_BEARER_AUTHORISATION, TEST_SERVICE_TOKEN, USER_ID, TEST_JURISDICTION_ID, TEST_CASE_TYPE,
                Collections.emptyMap());
    }

    @Test
    public void givenCasesInNonNonCompleteAndNonCompleteCaseInCcd_whenRetrieveCase_thenReturnNull() {
        final Long caseId1 = 1L;
        final Long caseId2 = 2L;
        final Long caseId3 = 3L;

        final CaseDetails caseDetails1 = createCaseDetails(caseId1, "state1");
        final CaseDetails caseDetails2 = createCaseDetails(caseId2, "state2");
        final CaseDetails caseDetails3 = createCaseDetails(caseId3, "state3");

        when(userService.retrieveUser(TEST_BEARER_AUTHORISATION)).thenReturn(USER);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);
        when(coreCaseDataApi
            .searchForCitizen(TEST_BEARER_AUTHORISATION, TEST_SERVICE_TOKEN, USER_ID, TEST_JURISDICTION_ID, TEST_CASE_TYPE,
                Collections.emptyMap())).thenReturn(Arrays.asList(caseDetails1, caseDetails2, caseDetails3));

        CaseDetails actual = classUnderTest.retrieveCase(TEST_AUTHORISATION, PETITIONER_CASE_STATE_GROUPING, PETITIONER);

        assertNull(actual);

        verify(userService).retrieveUser(TEST_BEARER_AUTHORISATION);
        verify(authTokenGenerator).generate();
        verify(coreCaseDataApi)
            .searchForCitizen(TEST_BEARER_AUTHORISATION, TEST_SERVICE_TOKEN, USER_ID, TEST_JURISDICTION_ID, TEST_CASE_TYPE,
                Collections.emptyMap());
    }


    @Test
    public void givenSingleAmendCaseInCcd_whenRetrieveCase_thenReturnTheCase() {
        CaseDetails caseDetails = createCaseDetails(1L, CaseState.AMEND_PETITION.getValue());
        List<CaseDetails> caseDetailsList = Collections.singletonList(caseDetails);

        when(userService.retrieveUser(TEST_BEARER_AUTHORISATION)).thenReturn(USER);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);
        when(coreCaseDataApi
            .searchForCitizen(TEST_BEARER_AUTHORISATION, TEST_SERVICE_TOKEN, USER_ID, TEST_JURISDICTION_ID, TEST_CASE_TYPE,
                Collections.emptyMap())).thenReturn(caseDetailsList);

        assertEquals(caseDetails, classUnderTest.retrieveCase(TEST_AUTHORISATION,
            PETITIONER_CASE_STATE_GROUPING, PETITIONER));

        verify(userService).retrieveUser(TEST_BEARER_AUTHORISATION);
        verify(authTokenGenerator).generate();
        verify(coreCaseDataApi)
            .searchForCitizen(TEST_BEARER_AUTHORISATION, TEST_SERVICE_TOKEN, USER_ID, TEST_JURISDICTION_ID, TEST_CASE_TYPE,
                Collections.emptyMap());
    }

    @Test
    public void givenSingleAmendCaseInCcdWithSubmittedCase_whenRetrieveCase_thenReturnTheCase() {
        CaseDetails caseDetails = createCaseDetails(1L, CaseState.AMEND_PETITION.getValue());
        CaseDetails caseDetailsSubmitted = createCaseDetails(2L, CaseState.SUBMITTED.getValue());
        List<CaseDetails> caseDetailsList = Arrays.asList(caseDetails, caseDetailsSubmitted);

        when(userService.retrieveUser(TEST_BEARER_AUTHORISATION)).thenReturn(USER);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);
        when(coreCaseDataApi
            .searchForCitizen(TEST_BEARER_AUTHORISATION, TEST_SERVICE_TOKEN, USER_ID, TEST_JURISDICTION_ID, TEST_CASE_TYPE,
                Collections.emptyMap())).thenReturn(caseDetailsList);

        assertEquals(caseDetailsSubmitted, classUnderTest.retrieveCase(TEST_AUTHORISATION,
            PETITIONER_CASE_STATE_GROUPING, PETITIONER));

        verify(userService).retrieveUser(TEST_BEARER_AUTHORISATION);
        verify(authTokenGenerator).generate();
        verify(coreCaseDataApi)
            .searchForCitizen(TEST_BEARER_AUTHORISATION, TEST_SERVICE_TOKEN, USER_ID, TEST_JURISDICTION_ID, TEST_CASE_TYPE,
                Collections.emptyMap());
    }

    @Test
    public void givenSingleAmendCaseInCcdWithAwaitingPaymentCase_whenRetrieveCase_thenReturnTheCase() {
        CaseDetails caseDetails = createCaseDetails(1L, CaseState.AMEND_PETITION.getValue());
        CaseDetails caseDetailsAwaiting = createCaseDetails(2L, CaseState.AWAITING_PAYMENT.getValue());
        List<CaseDetails> caseDetailsList = Arrays.asList(caseDetails, caseDetailsAwaiting);

        when(userService.retrieveUser(TEST_BEARER_AUTHORISATION)).thenReturn(USER);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);
        when(coreCaseDataApi
            .searchForCitizen(TEST_BEARER_AUTHORISATION, TEST_SERVICE_TOKEN, USER_ID, TEST_JURISDICTION_ID, TEST_CASE_TYPE,
                Collections.emptyMap())).thenReturn(caseDetailsList);

        assertEquals(caseDetailsAwaiting, classUnderTest.retrieveCase(TEST_AUTHORISATION,
            PETITIONER_CASE_STATE_GROUPING, PETITIONER));

        verify(userService).retrieveUser(TEST_BEARER_AUTHORISATION);
        verify(authTokenGenerator).generate();
        verify(coreCaseDataApi)
            .searchForCitizen(TEST_BEARER_AUTHORISATION, TEST_SERVICE_TOKEN, USER_ID, TEST_JURISDICTION_ID, TEST_CASE_TYPE,
                Collections.emptyMap());
    }

    @Test
    public void givenMultipleAmendCaseInCcd_whenRetrieveCaseWithToken_thenReturnTheLatestCreatedCase() {
        CaseDetails caseDetails = createCaseDetails(1L, CaseState.AMEND_PETITION.getValue(),
            LocalDateTime.now().minusDays(5L));

        CaseDetails caseDetailsNew = createCaseDetails(2L, CaseState.AMEND_PETITION.getValue(),
            LocalDateTime.now().minusDays(3L));

        CaseDetails caseDetailsNewest = createCaseDetails(3L, CaseState.AMEND_PETITION.getValue(),
            LocalDateTime.now().minusDays(1L));

        List<CaseDetails> caseDetailsList = Arrays.asList(caseDetails, caseDetailsNewest, caseDetailsNew);

        when(userService.retrieveUser(TEST_BEARER_AUTHORISATION)).thenReturn(USER);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);
        when(coreCaseDataApi
            .searchForCitizen(TEST_BEARER_AUTHORISATION, TEST_SERVICE_TOKEN, USER_ID, TEST_JURISDICTION_ID, TEST_CASE_TYPE,
                Collections.emptyMap())).thenReturn(caseDetailsList);

        assertEquals(caseDetailsNewest, classUnderTest.retrieveCase(TEST_AUTHORISATION,
            PETITIONER_CASE_STATE_GROUPING, PETITIONER));

        verify(userService).retrieveUser(TEST_BEARER_AUTHORISATION);
        verify(authTokenGenerator).generate();
        verify(coreCaseDataApi)
            .searchForCitizen(TEST_BEARER_AUTHORISATION, TEST_SERVICE_TOKEN, USER_ID, TEST_JURISDICTION_ID, TEST_CASE_TYPE,
                Collections.emptyMap());
    }

    @Test
    public void givenNoCaseInCcd_whenRetrieveCaseWithToken_thenReturnNull() {
        when(userService.retrieveUser(TEST_BEARER_AUTHORISATION)).thenReturn(USER);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);
        when(coreCaseDataApi
            .searchForCitizen(TEST_BEARER_AUTHORISATION, TEST_SERVICE_TOKEN, USER_ID, TEST_JURISDICTION_ID, TEST_CASE_TYPE,
                Collections.emptyMap())).thenReturn(null);

        CaseDetails actual = classUnderTest.retrieveCase(TEST_AUTHORISATION, PETITIONER);

        assertNull(actual);

        verify(userService).retrieveUser(TEST_BEARER_AUTHORISATION);
        verify(authTokenGenerator).generate();
        verify(coreCaseDataApi)
            .searchForCitizen(TEST_BEARER_AUTHORISATION, TEST_SERVICE_TOKEN, USER_ID, TEST_JURISDICTION_ID, TEST_CASE_TYPE,
                Collections.emptyMap());
    }

    @Test(expected = DuplicateCaseException.class)
    public void givenMultipleCaseInCcd_whenRetrieveCaseWithToken_thenReturnThrowDuplicateException() {
        List<CaseDetails> caseDetailsList = Arrays.asList(
            createCaseDetails(1L, CaseState.SUBMITTED.getValue()),
            createCaseDetails(2L, CaseState.SUBMITTED.getValue()));

        when(userService.retrieveUser(TEST_BEARER_AUTHORISATION)).thenReturn(USER);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);
        when(coreCaseDataApi
            .searchForCitizen(TEST_BEARER_AUTHORISATION, TEST_SERVICE_TOKEN, USER_ID, TEST_JURISDICTION_ID, TEST_CASE_TYPE,
                Collections.emptyMap())).thenReturn(caseDetailsList);

        classUnderTest.retrieveCase(TEST_AUTHORISATION, PETITIONER);

        verify(userService).retrieveUser(TEST_BEARER_AUTHORISATION);
        verify(authTokenGenerator).generate();
        verify(coreCaseDataApi)
            .searchForCitizen(TEST_BEARER_AUTHORISATION, TEST_SERVICE_TOKEN, USER_ID, TEST_JURISDICTION_ID, TEST_CASE_TYPE,
                Collections.emptyMap());
    }

    @Test
    public void givenMultipleCaseInCcd_whenRespondentRetrieveCase_thenReturnCaseWithValidRole() {
        CaseDetails expectedCase = createCaseDetails(2L, CaseState.SUBMITTED.getValue(),
            ImmutableMap.of(RESP_EMAIL_ADDRESS, TEST_USER_EMAIL));
        List<CaseDetails> caseDetailsList = Arrays.asList(
            createCaseDetails(1L, CaseState.SUBMITTED.getValue()),
            expectedCase);

        when(userService.retrieveUser(TEST_BEARER_AUTHORISATION)).thenReturn(USER);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);
        when(coreCaseDataApi
            .searchForCitizen(TEST_BEARER_AUTHORISATION, TEST_SERVICE_TOKEN, USER_ID, TEST_JURISDICTION_ID, TEST_CASE_TYPE,
                Collections.emptyMap())).thenReturn(caseDetailsList);

        assertThat(expectedCase, equalTo(classUnderTest.retrieveCase(TEST_AUTHORISATION, RESPONDENT)));
    }

    @Test
    public void givenMultipleCaseInCcd_whenCoRespondentRetrieveCase_thenReturnCaseWithValidRole() {
        CaseDetails expectedCase = createCaseDetails(2L, CaseState.SUBMITTED.getValue(),
            ImmutableMap.of(CO_RESP_EMAIL_ADDRESS, TEST_USER_EMAIL));
        List<CaseDetails> caseDetailsList = Arrays.asList(
            createCaseDetails(1L, CaseState.SUBMITTED.getValue()),
            expectedCase);

        when(userService.retrieveUser(TEST_BEARER_AUTHORISATION)).thenReturn(USER);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);
        when(coreCaseDataApi
            .searchForCitizen(TEST_BEARER_AUTHORISATION, TEST_SERVICE_TOKEN, USER_ID, TEST_JURISDICTION_ID, TEST_CASE_TYPE,
                Collections.emptyMap())).thenReturn(caseDetailsList);

        assertThat(expectedCase, equalTo(classUnderTest.retrieveCase(TEST_AUTHORISATION, RESPONDENT)));
    }

    @Test
    public void givenOnlyAmendCase_whenRetrieveCase_thenReturnNull() {
        List<CaseDetails> caseDetailsList = Arrays.asList(
            createCaseDetails(1L, CaseState.AMEND_PETITION.getValue()));

        when(userService.retrieveUser(TEST_BEARER_AUTHORISATION)).thenReturn(USER);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);
        when(coreCaseDataApi
            .searchForCitizen(TEST_BEARER_AUTHORISATION, TEST_SERVICE_TOKEN, USER_ID, TEST_JURISDICTION_ID, TEST_CASE_TYPE,
                Collections.emptyMap())).thenReturn(caseDetailsList);

        assertNull(classUnderTest.retrieveCase(TEST_AUTHORISATION, PETITIONER));
    }

    @Test
    public void givenNoDivRole_whenRetrieveCase_thenReturnNull() {
        List<CaseDetails> caseDetailsList = Arrays.asList(
            createCaseDetails(1L, CaseState.SUBMITTED.getValue()));

        when(userService.retrieveUser(TEST_BEARER_AUTHORISATION)).thenReturn(USER);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);
        when(coreCaseDataApi
            .searchForCitizen(TEST_BEARER_AUTHORISATION, TEST_SERVICE_TOKEN, USER_ID, TEST_JURISDICTION_ID, TEST_CASE_TYPE,
                Collections.emptyMap())).thenReturn(caseDetailsList);

        assertNull(classUnderTest.retrieveCase(TEST_AUTHORISATION, null));
    }

    @Test
    public void givenSingleCaseInCcd_whenRetrieveCaseWithToken_thenReturnTheCase() {
        CaseDetails caseDetails = createCaseDetails(1L, CaseState.SUBMITTED.getValue());
        List<CaseDetails> caseDetailsList = Collections.singletonList(caseDetails);

        when(userService.retrieveUser(TEST_BEARER_AUTHORISATION)).thenReturn(USER);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);
        when(coreCaseDataApi
            .searchForCitizen(TEST_BEARER_AUTHORISATION, TEST_SERVICE_TOKEN, USER_ID, TEST_JURISDICTION_ID, TEST_CASE_TYPE,
                Collections.emptyMap())).thenReturn(caseDetailsList);

        assertEquals(caseDetails, classUnderTest.retrieveCase(TEST_AUTHORISATION, PETITIONER));

        verify(userService).retrieveUser(TEST_BEARER_AUTHORISATION);
        verify(authTokenGenerator).generate();
        verify(coreCaseDataApi)
            .searchForCitizen(TEST_BEARER_AUTHORISATION, TEST_SERVICE_TOKEN, USER_ID, TEST_JURISDICTION_ID, TEST_CASE_TYPE,
                Collections.emptyMap());
    }

    @Test
    public void givenCaseId_whenRetrieveCaseById_thenReturnTheCase() {
        String testCaseId = String.valueOf(CASE_ID_1);
        CaseDetails caseDetails = CaseDetails.builder().build();

        when(userService.retrieveUser(TEST_BEARER_AUTHORISATION)).thenReturn(USER);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);
        when(coreCaseDataApi
            .readForCitizen(TEST_BEARER_AUTHORISATION, TEST_SERVICE_TOKEN, USER_ID, TEST_JURISDICTION_ID, TEST_CASE_TYPE,
                testCaseId)).thenReturn(caseDetails);

        assertEquals(caseDetails, classUnderTest.retrieveCaseById(TEST_AUTHORISATION, testCaseId));

        verify(userService).retrieveUser(TEST_BEARER_AUTHORISATION);
        verify(authTokenGenerator).generate();
        verify(coreCaseDataApi)
            .readForCitizen(TEST_BEARER_AUTHORISATION, TEST_SERVICE_TOKEN, USER_ID, TEST_JURISDICTION_ID, TEST_CASE_TYPE,
                testCaseId);
    }

    @Test
    public void givenCaseId_whenRetrieveCaseByIdWithCaseworker_thenReturnTheCase() {
        String testCaseId = String.valueOf(CASE_ID_1);
        CaseDetails caseDetails = CaseDetails.builder().build();

        final User userDetails = new User(
            TEST_AUTHORISATION,
            UserDetails.builder().id(USER_ID).roles(Collections.singletonList(CASEWORKER_ROLE)).build()
        );

        when(userService.retrieveUser(TEST_BEARER_AUTHORISATION)).thenReturn(userDetails);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);
        when(coreCaseDataApi
            .readForCaseWorker(TEST_BEARER_AUTHORISATION, TEST_SERVICE_TOKEN, USER_ID, TEST_JURISDICTION_ID, TEST_CASE_TYPE,
                testCaseId)).thenReturn(caseDetails);

        assertEquals(caseDetails, classUnderTest.retrieveCaseById(TEST_AUTHORISATION, testCaseId));

        verify(userService).retrieveUser(TEST_BEARER_AUTHORISATION);
        verify(authTokenGenerator).generate();
        verify(coreCaseDataApi)
            .readForCaseWorker(TEST_BEARER_AUTHORISATION, TEST_SERVICE_TOKEN, USER_ID, TEST_JURISDICTION_ID, TEST_CASE_TYPE,
                testCaseId);
    }

    @Test
    public void givenCaseId_whenRetrieveCaseByIdWithCaseworkerCitizen_thenReturnTheCase() {
        String testCaseId = String.valueOf(CASE_ID_1);
        CaseDetails caseDetails = CaseDetails.builder().build();
        List<String> userRoles = Arrays.asList(CASEWORKER_ROLE, CITIZEN_ROLE);

        final User userDetails = new User(
            TEST_AUTHORISATION,
            UserDetails.builder().id(USER_ID).roles(userRoles).build()
        );

        when(userService.retrieveUser(TEST_BEARER_AUTHORISATION)).thenReturn(userDetails);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);
        when(coreCaseDataApi
            .readForCitizen(TEST_BEARER_AUTHORISATION, TEST_SERVICE_TOKEN, USER_ID, TEST_JURISDICTION_ID, TEST_CASE_TYPE,
                testCaseId)).thenReturn(caseDetails);

        assertEquals(caseDetails, classUnderTest.retrieveCaseById(TEST_AUTHORISATION, testCaseId));

        verify(userService).retrieveUser(TEST_BEARER_AUTHORISATION);
        verify(authTokenGenerator).generate();
        verify(coreCaseDataApi)
            .readForCitizen(TEST_BEARER_AUTHORISATION, TEST_SERVICE_TOKEN, USER_ID, TEST_JURISDICTION_ID, TEST_CASE_TYPE,
                testCaseId);
    }

    @Test
    public void whenSearchCases_theReturnCcdResponse() {
        String query = "QueryToTest";
        SearchResult expectedResult = SearchResult.builder().build();

        final UserDetails userDetails = UserDetails.builder()
            .id(USER_ID).roles(Arrays.asList(CASEWORKER_ROLE)).build();
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);

        when(coreCaseDataApi.searchCases(TEST_BEARER_AUTHORISATION, TEST_SERVICE_TOKEN, TEST_CASE_TYPE, query))
            .thenReturn(expectedResult);

        SearchResult result = classUnderTest.searchCase(TEST_AUTHORISATION, query);

        assertEquals(expectedResult, result);
    }

    private CaseDetails createCaseDetails(Long id, String state) {
        return createCaseDetails(id, state, LocalDateTime.now());
    }

    private CaseDetails createCaseDetails(Long id, String state, LocalDateTime createdTime) {
        return CaseDetails.builder()
            .id(id)
            .state(state)
            .createdDate(createdTime)
            .data(ImmutableMap.of(D8_PETITIONER_EMAIL, TEST_USER_EMAIL))
            .build();
    }

    private CaseDetails createCaseDetails(Long id, String state, Map<String, Object> caseData) {
        return CaseDetails.builder()
            .id(id)
            .state(state)
            .createdDate(LocalDateTime.now())
            .data(caseData)
            .build();
    }
}
