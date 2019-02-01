package uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.client.FormatterServiceClient;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.*;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.model.Draft;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.event.ccd.submission.CaseSubmittedEvent;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.exception.DuplicateCaseException;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.CcdRetrievalService;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.UserService;

import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CaseRetrievalStateMap.PETITIONER_CASE_STATE_GROUPING;

@RunWith(MockitoJUnitRunner.class)
public class PetitionServiceImplUTest {
    private static final String AUTHORISATION = "userToken";
    private static final boolean DIVORCE_FORMAT = false;
    private static final String TEST_CASE_ID = "test.id";
    private static final String USER_FIRST_NAME = "John";
    private static final String ADULTERY = "adultery";
    private static final String TWO_YEAR_SEPARATION = "2yr-separation";

    @Mock
    private CcdRetrievalService ccdRetrievalService;

    @Mock
    private DraftServiceImpl draftService;

    @Mock
    private FormatterServiceClient formatterServiceClient;

    @Mock
    private UserService userService;

    @InjectMocks
    private PetitionServiceImpl classUnderTest;

    @Test
    public void givenCcdRetrievalServiceReturnsCase_whenRetrievePetition_thenProceedAsExpected() throws DuplicateCaseException {
        final CaseDetails caseDetails = CaseDetails.builder().build();

        when(ccdRetrievalService.retrieveCase(AUTHORISATION, PETITIONER_CASE_STATE_GROUPING)).thenReturn(caseDetails);

        CaseDetails actual = classUnderTest.retrievePetition(AUTHORISATION, PETITIONER_CASE_STATE_GROUPING,true);

        assertEquals(caseDetails, actual);
        verify(ccdRetrievalService).retrieveCase(AUTHORISATION, PETITIONER_CASE_STATE_GROUPING);
    }

    @Test(expected = DuplicateCaseException.class)
    public void givenCcdRetrievalServiceThrowException_whenRetrievePetition_thenThrowException()
        throws DuplicateCaseException {
        final DuplicateCaseException duplicateCaseException = new DuplicateCaseException("Duplicate");

        when(ccdRetrievalService.retrieveCase(AUTHORISATION, PETITIONER_CASE_STATE_GROUPING))
            .thenThrow(duplicateCaseException);

        classUnderTest.retrievePetition(AUTHORISATION, PETITIONER_CASE_STATE_GROUPING,true);

        verify(ccdRetrievalService).retrieveCase(AUTHORISATION, PETITIONER_CASE_STATE_GROUPING);
    }

    @Test
    public void givenCheckCcdTrueNoDataInCcdOrDraft_whenRetrievePetition_thenReturnNull()
        throws DuplicateCaseException {

        when(ccdRetrievalService.retrieveCase(AUTHORISATION, PETITIONER_CASE_STATE_GROUPING)).thenReturn(null);
        when(draftService.getDraft(AUTHORISATION)).thenReturn(null);

        assertNull(classUnderTest.retrievePetition(AUTHORISATION, PETITIONER_CASE_STATE_GROUPING,true));

        verify(ccdRetrievalService).retrieveCase(AUTHORISATION, PETITIONER_CASE_STATE_GROUPING);
        verify(draftService).getDraft(AUTHORISATION);
    }

    @Test
    public void givenCheckCcdFalseAndNoDraft_whenRetrievePetition_thenReturnNull() throws DuplicateCaseException {
        when(draftService.getDraft(AUTHORISATION)).thenReturn(null);

        assertNull(classUnderTest.retrievePetition(AUTHORISATION, PETITIONER_CASE_STATE_GROUPING,false));

        verifyZeroInteractions(ccdRetrievalService);
        verify(draftService).getDraft(AUTHORISATION);
    }

    @Test
    public void givenCheckCcdFalseAndDraftExistsInCcdFormat_whenRetrievePetition_thenReturnDataFromDraftStore()
        throws DuplicateCaseException {

        final Map<String, Object> document = Collections.emptyMap();
        final Draft draft = new Draft("1", document, null);

        final Map<String, Object> caseData = new HashMap<>();
        final CaseDetails caseDetails = CaseDetails.builder().data(caseData).build();

        when(draftService.getDraft(AUTHORISATION)).thenReturn(draft);
        when(draftService.isInCcdFormat(draft)).thenReturn(false);

        CaseDetails actual = classUnderTest.retrievePetition(AUTHORISATION, PETITIONER_CASE_STATE_GROUPING,false);

        assertEquals(caseDetails, actual);

        verifyZeroInteractions(ccdRetrievalService);
        verify(draftService).getDraft(AUTHORISATION);
        verifyZeroInteractions(formatterServiceClient);
    }

    @Test
    public void givenCheckCcdFalseAndDraftExists_whenRetrievePetition_thenReturnDataFromDraftStore()
        throws DuplicateCaseException {

        final Map<String, Object> document = Collections.emptyMap();
        final Draft draft = new Draft("1", document, null);

        final Map<String, Object> draftDocument = new HashMap<>();
        final CaseDetails caseDetails = CaseDetails.builder().data(draftDocument).build();

        when(draftService.getDraft(AUTHORISATION)).thenReturn(draft);
        when(draftService.isInCcdFormat(draft)).thenReturn(true);
        when(formatterServiceClient.transformToDivorceFormat(document, AUTHORISATION)).thenReturn(draftDocument);

        CaseDetails actual = classUnderTest.retrievePetition(AUTHORISATION, PETITIONER_CASE_STATE_GROUPING,false);

        assertEquals(caseDetails, actual);

        verifyZeroInteractions(ccdRetrievalService);
        verify(draftService).getDraft(AUTHORISATION);
        verify(formatterServiceClient).transformToDivorceFormat(document, AUTHORISATION);
    }

    @Test
    public void whenRetrievePetition_thenProceedAsExpected() throws DuplicateCaseException {
        final CaseDetails caseDetails = CaseDetails.builder().build();

        when(ccdRetrievalService.retrieveCase(AUTHORISATION)).thenReturn(caseDetails);

        assertEquals(caseDetails, classUnderTest.retrievePetition(AUTHORISATION));

        verify(ccdRetrievalService).retrieveCase(AUTHORISATION);
    }

    @Test
    public void whenRetrievePetitionById_thenProceedAsExpected() throws DuplicateCaseException {
        final CaseDetails caseDetails = CaseDetails.builder().build();

        when(ccdRetrievalService.retrieveCaseById(AUTHORISATION, TEST_CASE_ID)).thenReturn(caseDetails);

        assertEquals(caseDetails, classUnderTest.retrievePetitionByCaseId(AUTHORISATION, TEST_CASE_ID));

        verify(ccdRetrievalService).retrieveCaseById(AUTHORISATION, TEST_CASE_ID);
    }

    @Test
    public void whenSaveDraft_thenProceedAsExpected() {
        final Map<String, Object> data = Collections.emptyMap();

        classUnderTest.saveDraft(AUTHORISATION, data, DIVORCE_FORMAT);

        verify(draftService).saveDraft(AUTHORISATION, data, DIVORCE_FORMAT);
    }

    @Test
    public void whenCreateDraft_thenProceedAsExpected() {
        final Map<String, Object> data = Collections.emptyMap();

        classUnderTest.createDraft(AUTHORISATION, data, DIVORCE_FORMAT);

        verify(draftService).createDraft(AUTHORISATION, data, DIVORCE_FORMAT);
    }

    @Test
    public void whenGetAllDrafts_thenProceedAsExpected() {
        classUnderTest.getAllDrafts(AUTHORISATION);

        verify(draftService).getAllDrafts(AUTHORISATION);
    }

    @Test
    public void whenDeleteDraft_thenProceedAsExpected() {
        classUnderTest.deleteDraft(AUTHORISATION);

        verify(draftService).deleteDraft(AUTHORISATION);
    }

    @Test
    public void whenOnApplicationEvent_thenProceedAsExpected() {
        final CaseSubmittedEvent caseSubmittedEvent = mock(CaseSubmittedEvent.class);

        when(caseSubmittedEvent.getAuthToken()).thenReturn(AUTHORISATION);

        classUnderTest.onApplicationEvent(caseSubmittedEvent);

        verify(draftService).deleteDraft(AUTHORISATION);
    }

    @Test
    public void whenCreateAmendedPetitionDraft_thenProceedAsExpected() throws DuplicateCaseException {
        final Map<String, Object> caseData = new HashMap<>();
        caseData.put(CcdCaseProperties.D8_CASE_REFERENCE, TEST_CASE_ID);
        caseData.put(CcdCaseProperties.D8_REASON_FOR_DIVORCE, ADULTERY);
        caseData.put(CcdCaseProperties.PREVIOUS_REASONS_DIVORCE, new ArrayList<>());

        final CaseDetails caseDetails = CaseDetails.builder().data(caseData).build();
        final Map<String, Object> draftData = new HashMap<>();
        final List<String> previousReasons = new ArrayList<>();
        final SimpleDateFormat createdDate = new SimpleDateFormat(CmsConstants.YEAR_DATE_FORMAT, Locale.ENGLISH);

        previousReasons.add(ADULTERY);
        draftData.put(DivorceSessionProperties.PREVIOUS_CASE_ID, TEST_CASE_ID);
        draftData.put(DivorceSessionProperties.PREVIOUS_REASONS_FOR_DIVORCE, previousReasons);
        draftData.put(DivorceSessionProperties.CREATED_DATE, createdDate.format(new Date()));

        when(ccdRetrievalService.retrieveCase(AUTHORISATION)).thenReturn(caseDetails);

        classUnderTest.createAmendedPetitionDraft(AUTHORISATION);

        verify(ccdRetrievalService).retrieveCase(AUTHORISATION);
        verify(draftService).createDraft(AUTHORISATION, draftData, true);
    }

    @Test
    public void whenCreateAmendedPetitionDraft_whenCaseHasPreviousReasons_thenProceedAsExpected()
        throws DuplicateCaseException {

        final Map<String, Object> caseData = new HashMap<>();
        final List<String> previousReasonsOld = new ArrayList<>();
        previousReasonsOld.add(TWO_YEAR_SEPARATION);
        caseData.put(CcdCaseProperties.D8_CASE_REFERENCE, TEST_CASE_ID);
        caseData.put(CcdCaseProperties.D8_REASON_FOR_DIVORCE, ADULTERY);
        caseData.put(CcdCaseProperties.PREVIOUS_REASONS_DIVORCE, previousReasonsOld);

        final CaseDetails caseDetails = CaseDetails.builder().data(caseData).build();
        final Map<String, Object> draftData = new HashMap<>();
        final List<String> previousReasons = new ArrayList<>();
        final SimpleDateFormat createdDate = new SimpleDateFormat(CmsConstants.YEAR_DATE_FORMAT, Locale.ENGLISH);

        previousReasons.add(TWO_YEAR_SEPARATION);
        previousReasons.add(ADULTERY);
        draftData.put(DivorceSessionProperties.PREVIOUS_CASE_ID, TEST_CASE_ID);
        draftData.put(DivorceSessionProperties.PREVIOUS_REASONS_FOR_DIVORCE, previousReasons);
        draftData.put(DivorceSessionProperties.CREATED_DATE, createdDate.format(new Date()));

        when(ccdRetrievalService.retrieveCase(AUTHORISATION)).thenReturn(caseDetails);

        classUnderTest.createAmendedPetitionDraft(AUTHORISATION);

        verify(ccdRetrievalService).retrieveCase(AUTHORISATION);
        verify(draftService).createDraft(AUTHORISATION, draftData, true);
    }

    @Test
    public void whenCreateAmendedPetitionDraft_whenPetitionNotFound_thenProceedAsExpected()
        throws DuplicateCaseException {
        final UserDetails user = UserDetails.builder().forename(USER_FIRST_NAME).build();

        when(ccdRetrievalService.retrieveCase(AUTHORISATION)).thenReturn(null);
        when(userService.retrieveUserDetails(AUTHORISATION)).thenReturn(user);

        classUnderTest.createAmendedPetitionDraft(AUTHORISATION);

        verify(ccdRetrievalService).retrieveCase(AUTHORISATION);
    }
}
