package uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.client.FormatterServiceClient;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.model.Draft;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.event.ccd.submission.CaseSubmittedEvent;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.exception.DuplicateCaseException;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.CcdRetrievalService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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

    @Mock
    private CcdRetrievalService ccdRetrievalService;

    @Mock
    private DraftServiceImpl draftService;

    @Mock
    private FormatterServiceClient formatterServiceClient;

    @InjectMocks
    private PetitionServiceImpl classUnderTest;

    @Value("${draft.store.api.document.type.divorceFormat}")
    private String documentTypeDivorceFormat;

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
        final HashMap<String, Object> caseData = new HashMap<>();
        caseData.put("D8caseReference", "caseRefVal");
        caseData.put("D8ReasonForDivorce", "unreasonable-behaviour");
        caseData.put("PreviousReasonsForDivorce", new ArrayList<>());

        final CaseDetails caseDetails = CaseDetails.builder().data(caseData).build();
        final HashMap<String, Object> draftData = new HashMap<>();
        final ArrayList<String> previousReasons = new ArrayList<>();

        previousReasons.add("unreasonable-behaviour");
        draftData.put("caseReference", null);
        draftData.put("reasonForDivorce", null);
        draftData.put("previousCaseId", "caseRefVal");
        draftData.put("previousReasonsForDivorce", previousReasons);

        when(ccdRetrievalService.retrieveCase(AUTHORISATION)).thenReturn(caseDetails);
        when(formatterServiceClient.transformToDivorceFormat(draftData, AUTHORISATION)).thenReturn(draftData);

        assertEquals(draftData, classUnderTest.createAmendedPetitionDraft(AUTHORISATION));

        verify(ccdRetrievalService).retrieveCase(AUTHORISATION);
        verify(draftService).createDraft(AUTHORISATION, draftData, true);
    }

    @Test
    public void whenCreateAmendedPetitionDraft_whenCaseHasNoPreviousReasonsProperty_thenProceedAsExpected()
        throws DuplicateCaseException {

        final HashMap<String, Object> caseData = new HashMap<>();
        caseData.put("D8caseReference", "caseRefVal");
        caseData.put("D8ReasonForDivorce", "unreasonable-behaviour");

        final CaseDetails caseDetails = CaseDetails.builder().data(caseData).build();
        final HashMap<String, Object> draftData = new HashMap<>();
        final ArrayList<String> previousReasons = new ArrayList<>();

        previousReasons.add("unreasonable-behaviour");
        draftData.put("caseReference", null);
        draftData.put("reasonForDivorce", null);
        draftData.put("previousCaseId", "caseRefVal");
        draftData.put("previousReasonsForDivorce", previousReasons);

        when(ccdRetrievalService.retrieveCase(AUTHORISATION)).thenReturn(caseDetails);
        when(formatterServiceClient.transformToDivorceFormat(draftData, AUTHORISATION)).thenReturn(draftData);

        assertEquals(draftData, classUnderTest.createAmendedPetitionDraft(AUTHORISATION));

        verify(ccdRetrievalService).retrieveCase(AUTHORISATION);
        verify(draftService).createDraft(AUTHORISATION, draftData, true);
    }

    @Test
    public void whenCreateAmendedPetitionDraft_whenPetitionNotFound_thenProceedAsExpected()
        throws DuplicateCaseException {

        when(ccdRetrievalService.retrieveCase(AUTHORISATION)).thenReturn(null);

        assertNull(classUnderTest.createAmendedPetitionDraft(AUTHORISATION));

        verify(ccdRetrievalService).retrieveCase(AUTHORISATION);
    }
}
