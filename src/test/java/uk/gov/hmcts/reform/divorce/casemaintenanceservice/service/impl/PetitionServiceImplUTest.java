package uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.model.Draft;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.event.ccd.submission.CaseSubmittedEvent;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.exception.DuplicateCaseException;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.formatterservice.FormatterServiceClient;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.CcdRetrievalService;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PetitionServiceImplUTest {
    private static final String AUTHORISATION = "userToken";
    private static final boolean DIVORCE_FORMAT = false;

    @Mock
    private CcdRetrievalService ccdRetrievalService;

    @Mock
    private DraftServiceImpl draftService;

    @Mock
    private FormatterServiceClient formatterServiceClient;

    @InjectMocks
    private PetitionServiceImpl classUnderTest;

    @Test
    public void givenCcdRetrievalServiceReturnsCase_whenRetrievePetition_thenProceedAsExpected() throws DuplicateCaseException {
        final CaseDetails caseDetails = CaseDetails.builder().build();

        when(ccdRetrievalService.retrievePetition(AUTHORISATION)).thenReturn(caseDetails);

        CaseDetails actual = classUnderTest.retrievePetition(AUTHORISATION, true);

        assertEquals(caseDetails, actual);
        verify(ccdRetrievalService).retrievePetition(AUTHORISATION);
    }

    @Test(expected = DuplicateCaseException.class)
    public void givenCcdRetrievalServiceThrowException_whenRetrievePetition_thenThrowException()
        throws DuplicateCaseException {
        final DuplicateCaseException duplicateCaseException = new DuplicateCaseException("Duplicate");

        when(ccdRetrievalService.retrievePetition(AUTHORISATION)).thenThrow(duplicateCaseException);

        classUnderTest.retrievePetition(AUTHORISATION, true);

        verify(ccdRetrievalService).retrievePetition(AUTHORISATION);
    }

    @Test
    public void givenCheckCcdTrueNoDataInCcdOrDraft_whenRetrievePetition_thenReturnNull()
        throws DuplicateCaseException {

        when(ccdRetrievalService.retrievePetition(AUTHORISATION)).thenReturn(null);
        when(draftService.getDraft(AUTHORISATION)).thenReturn(null);

        assertNull(classUnderTest.retrievePetition(AUTHORISATION, true));

        verify(ccdRetrievalService).retrievePetition(AUTHORISATION);
        verify(draftService).getDraft(AUTHORISATION);
    }

    @Test
    public void givenCheckCcdFalseAndNoDraft_whenRetrievePetition_thenReturnNull() throws DuplicateCaseException {
        when(draftService.getDraft(AUTHORISATION)).thenReturn(null);

        assertNull(classUnderTest.retrievePetition(AUTHORISATION, false));

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
        when(draftService.isInCcdFormat(draft)).thenReturn(true);

        CaseDetails actual = classUnderTest.retrievePetition(AUTHORISATION, false);

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

        final Map<String, Object> caseData = new HashMap<>();
        final CaseDetails caseDetails = CaseDetails.builder().data(caseData).build();

        when(draftService.getDraft(AUTHORISATION)).thenReturn(draft);
        when(draftService.isInCcdFormat(draft)).thenReturn(false);
        when(formatterServiceClient.transformToCCDFormat(document, AUTHORISATION)).thenReturn(caseData);

        CaseDetails actual = classUnderTest.retrievePetition(AUTHORISATION, false);

        assertEquals(caseDetails, actual);

        verifyZeroInteractions(ccdRetrievalService);
        verify(draftService).getDraft(AUTHORISATION);
        verify(formatterServiceClient).transformToCCDFormat(document, AUTHORISATION);
    }

    @Test
    public void whenSaveDraft_thenProceedAsExpected() {
        final Map<String, Object> data = Collections.emptyMap();

        classUnderTest.saveDraft(AUTHORISATION, data, DIVORCE_FORMAT);

        verify(draftService).saveDraft(AUTHORISATION, data, DIVORCE_FORMAT);
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

}
