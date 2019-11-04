package uk.gov.hmcts.reform.divorce.casemaintenanceservice.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CmsConstants;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.DivorceSessionProperties;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.model.DraftList;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.exception.DuplicateCaseException;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.PetitionService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.TestConstants.TEST_AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.TestConstants.TEST_REASON_UNREASONABLE_BEHAVIOUR;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CaseRetrievalStateMap.PETITIONER_CASE_STATE_GROUPING;

@RunWith(MockitoJUnitRunner.class)
public class PetitionControllerUTest {

    @Mock
    private PetitionService petitionService;

    @InjectMocks
    private PetitionController classUnderTest;

    @Test
    public void givenCaseFound_whenRetrievePetition_thenReturnCaseDetails() throws DuplicateCaseException {

        final CaseDetails caseDetails = CaseDetails.builder().build();

        when(petitionService.retrievePetition(TEST_AUTH_TOKEN, PETITIONER_CASE_STATE_GROUPING))
                .thenReturn(caseDetails);

        ResponseEntity<CaseDetails> actual = classUnderTest.retrievePetition(TEST_AUTH_TOKEN);

        assertEquals(HttpStatus.OK, actual.getStatusCode());
        assertEquals(caseDetails, actual.getBody());

        verify(petitionService).retrievePetition(TEST_AUTH_TOKEN, PETITIONER_CASE_STATE_GROUPING);
    }

    @Test
    public void givenCaseFound_whenRetrieveCaseForRespondent_thenReturnCaseDetails() throws DuplicateCaseException {

        final CaseDetails caseDetails = CaseDetails.builder().build();

        when(petitionService.retrievePetitionForAos(TEST_AUTH_TOKEN)).thenReturn(caseDetails);

        ResponseEntity<CaseDetails> actual = classUnderTest.retrieveCaseForRespondent(TEST_AUTH_TOKEN);

        assertEquals(HttpStatus.OK, actual.getStatusCode());
        assertEquals(caseDetails, actual.getBody());

        verify(petitionService).retrievePetitionForAos(TEST_AUTH_TOKEN);
    }

    @Test
    public void givenCaseFound_whenRetrieveCase_thenReturnCaseDetails() throws DuplicateCaseException {

        final CaseDetails caseDetails = CaseDetails.builder().build();

        when(petitionService.retrievePetition(TEST_AUTH_TOKEN, PETITIONER_CASE_STATE_GROUPING))
                .thenReturn(caseDetails);

        ResponseEntity<CaseDetails> actual = classUnderTest.retrievePetition(TEST_AUTH_TOKEN);

        assertEquals(HttpStatus.OK, actual.getStatusCode());
        assertEquals(caseDetails, actual.getBody());

        verify(petitionService).retrievePetition(TEST_AUTH_TOKEN, PETITIONER_CASE_STATE_GROUPING);
    }

    @Test
    public void givenNoCaseFound_whenRetrieveCase_thenReturn204() throws DuplicateCaseException {

        when(petitionService.retrievePetition(TEST_AUTH_TOKEN, PETITIONER_CASE_STATE_GROUPING))
                .thenReturn(null);

        ResponseEntity<CaseDetails> actual = classUnderTest.retrievePetition(TEST_AUTH_TOKEN);

        assertEquals(HttpStatus.NO_CONTENT, actual.getStatusCode());
        assertNull(actual.getBody());

        verify(petitionService).retrievePetition(TEST_AUTH_TOKEN, PETITIONER_CASE_STATE_GROUPING);
    }

    @Test
    public void givenDuplicateCase_whenRetrieveCase_thenReturnHttpStatus300() throws DuplicateCaseException {
        when(petitionService.retrievePetition(TEST_AUTH_TOKEN, PETITIONER_CASE_STATE_GROUPING))
                .thenThrow(new DuplicateCaseException("Duplicate"));

        ResponseEntity<CaseDetails> actual = classUnderTest.retrievePetition(TEST_AUTH_TOKEN);

        assertEquals(HttpStatus.MULTIPLE_CHOICES, actual.getStatusCode());

        verify(petitionService).retrievePetition(TEST_AUTH_TOKEN, PETITIONER_CASE_STATE_GROUPING);
    }

    @Test
    public void givenNoCaseFound_whenRetrieveCaseWithToken_thenReturn404() throws DuplicateCaseException {

        when(petitionService.retrievePetition(TEST_AUTH_TOKEN)).thenReturn(null);

        ResponseEntity<CaseDetails> actual = classUnderTest.retrieveCase(TEST_AUTH_TOKEN);

        assertEquals(HttpStatus.NOT_FOUND, actual.getStatusCode());
        assertNull(actual.getBody());

        verify(petitionService).retrievePetition(TEST_AUTH_TOKEN);
    }

    @Test
    public void givenMultipleCaseCaseFound_whenRetrieveCaseWithToken_thenReturn300() throws DuplicateCaseException {

        when(petitionService.retrievePetition(TEST_AUTH_TOKEN)).thenThrow(new DuplicateCaseException("Some Error"));

        ResponseEntity<CaseDetails> actual = classUnderTest.retrieveCase(TEST_AUTH_TOKEN);

        assertEquals(HttpStatus.MULTIPLE_CHOICES, actual.getStatusCode());
        assertNull(actual.getBody());

        verify(petitionService).retrievePetition(TEST_AUTH_TOKEN);
    }

    @Test
    public void givenSingleCaseCaseFound_whenRetrieveCaseWithToken_thenReturnCase() throws DuplicateCaseException {
        final CaseDetails caseDetails = CaseDetails.builder().build();

        when(petitionService.retrievePetition(TEST_AUTH_TOKEN)).thenReturn(caseDetails);

        ResponseEntity<CaseDetails> actual = classUnderTest.retrieveCase(TEST_AUTH_TOKEN);

        assertEquals(HttpStatus.OK, actual.getStatusCode());
        assertEquals(caseDetails, actual.getBody());

        verify(petitionService).retrievePetition(TEST_AUTH_TOKEN);
    }

    @Test
    public void givenCaseFound_whenRetrieveCaseById_thenReturnCase() throws DuplicateCaseException {
        final CaseDetails caseDetails = CaseDetails.builder().id(123456789L).build();

        when(petitionService.retrievePetitionByCaseId(TEST_AUTH_TOKEN, TEST_CASE_ID)).thenReturn(caseDetails);

        ResponseEntity<CaseDetails> actual = classUnderTest.retrieveCaseById(TEST_AUTH_TOKEN, TEST_CASE_ID);

        assertEquals(HttpStatus.OK, actual.getStatusCode());
        assertEquals(caseDetails, actual.getBody());

        verify(petitionService).retrievePetitionByCaseId(TEST_AUTH_TOKEN, TEST_CASE_ID);
    }

    @Test
    public void givenCaseNotFound_whenRetrieveCaseById_thenNotFound() throws DuplicateCaseException {
        when(petitionService.retrievePetitionByCaseId(TEST_AUTH_TOKEN, TEST_CASE_ID)).thenReturn(null);

        ResponseEntity<CaseDetails> actual = classUnderTest.retrieveCaseById(TEST_AUTH_TOKEN, TEST_CASE_ID);

        assertEquals(HttpStatus.NOT_FOUND, actual.getStatusCode());

        verify(petitionService).retrievePetitionByCaseId(TEST_AUTH_TOKEN, TEST_CASE_ID);
    }

    @Test
    public void givenDivorceFormatIsNull_whenSaveDraft_thenProceedAsExpected() {
        final Map<String, Object> data = Collections.emptyMap();

        ResponseEntity<Void> response = classUnderTest.saveDraft(TEST_AUTH_TOKEN, data, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(petitionService).saveDraft(TEST_AUTH_TOKEN, data, false);
    }

    @Test
    public void givenDivorceFormatIsFalse_whenSaveDraft_thenProceedAsExpected() {
        final Map<String, Object> data = Collections.emptyMap();

        ResponseEntity<Void> response = classUnderTest.saveDraft(TEST_AUTH_TOKEN, data, false);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(petitionService).saveDraft(TEST_AUTH_TOKEN, data, false);
    }

    @Test
    public void givenDivorceFormatIsTrue_whenSaveDraft_thenProceedAsExpected() {
        final Map<String, Object> data = Collections.emptyMap();

        ResponseEntity<Void> response = classUnderTest.saveDraft(TEST_AUTH_TOKEN, data, true);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(petitionService).saveDraft(TEST_AUTH_TOKEN, data, true);
    }

    @Test
    public void givenDivorceFormatIsNull_whenCreateDraft_thenProceedAsExpected() {
        final Map<String, Object> data = Collections.emptyMap();

        ResponseEntity<Void> response = classUnderTest.createDraft(TEST_AUTH_TOKEN, data, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(petitionService).createDraft(TEST_AUTH_TOKEN, data, false);
    }

    @Test
    public void givenDivorceFormatIsFalse_whenCreateDraft_thenProceedAsExpected() {
        final Map<String, Object> data = Collections.emptyMap();

        ResponseEntity<Void> response = classUnderTest.createDraft(TEST_AUTH_TOKEN, data, false);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(petitionService).createDraft(TEST_AUTH_TOKEN, data, false);
    }

    @Test
    public void givenDivorceFormatIsTrue_whenCreateDraft_thenProceedAsExpected() {
        final Map<String, Object> data = Collections.emptyMap();

        ResponseEntity<Void> response = classUnderTest.createDraft(TEST_AUTH_TOKEN, data, true);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(petitionService).createDraft(TEST_AUTH_TOKEN, data, true);
    }

    @Test
    public void whenDeleteDraft_thenProceedAsExpected() {
        ResponseEntity<Void> response = classUnderTest.deleteDraft(TEST_AUTH_TOKEN);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        verify(petitionService).deleteDraft(TEST_AUTH_TOKEN);
    }

    @Test
    public void whenRetrieveAllDrafts_thenProceedAsExpected() {
        final DraftList draftList = mock(DraftList.class);

        when(petitionService.getAllDrafts(TEST_AUTH_TOKEN)).thenReturn(draftList);

        ResponseEntity<DraftList> response = classUnderTest.retrieveAllDrafts(TEST_AUTH_TOKEN);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(draftList, response.getBody());

        verify(petitionService).getAllDrafts(TEST_AUTH_TOKEN);
    }

    @Test
    public void givenNoCaseFound_whenAmendToDraftPetition_thenReturn404() throws DuplicateCaseException {

        when(petitionService.createAmendedPetitionDraft(TEST_AUTH_TOKEN))
            .thenReturn(null);

        ResponseEntity<Map<String, Object>> actual = classUnderTest.createAmendedPetitionDraft(TEST_AUTH_TOKEN);

        assertEquals(HttpStatus.NOT_FOUND, actual.getStatusCode());
        assertNull(actual.getBody());

        verify(petitionService).createAmendedPetitionDraft(TEST_AUTH_TOKEN);
    }

    @Test
    public void givenCaseFound_whenAmendToDraftPetition_thenReturnDraftData() throws DuplicateCaseException {

        final Map<String, Object> draftData = new HashMap<>();

        when(petitionService.createAmendedPetitionDraft(TEST_AUTH_TOKEN))
            .thenReturn(draftData);

        ResponseEntity<Map<String, Object>> actual = classUnderTest.createAmendedPetitionDraft(TEST_AUTH_TOKEN);

        assertEquals(HttpStatus.OK, actual.getStatusCode());
        assertEquals(draftData, actual.getBody());

        verify(petitionService).createAmendedPetitionDraft(TEST_AUTH_TOKEN);
    }

    @Test
    public void givenCaseFound_whenAmendToDraftPetition_thenSetDraftDataFromCase() throws DuplicateCaseException {
        final Map<String, Object> draftData = new HashMap<>();
        final List<String> previousReasons = new ArrayList<>();
        final SimpleDateFormat createdDate = new SimpleDateFormat(CmsConstants.YEAR_DATE_FORMAT);

        previousReasons.add(TEST_REASON_UNREASONABLE_BEHAVIOUR);
        draftData.put(DivorceSessionProperties.PREVIOUS_CASE_ID, TEST_CASE_ID);
        draftData.put(DivorceSessionProperties.PREVIOUS_REASONS_FOR_DIVORCE, previousReasons);
        draftData.put(DivorceSessionProperties.CREATED_DATE, createdDate.toPattern());
        draftData.put(DivorceSessionProperties.COURTS, CmsConstants.CTSC_SERVICE_CENTRE);

        when(petitionService.createAmendedPetitionDraft(TEST_AUTH_TOKEN))
            .thenReturn(draftData);

        ResponseEntity<Map<String, Object>> actual = classUnderTest.createAmendedPetitionDraft(TEST_AUTH_TOKEN);

        assertEquals(HttpStatus.OK, actual.getStatusCode());
        assertEquals(draftData, actual.getBody());

        verify(petitionService).createAmendedPetitionDraft(TEST_AUTH_TOKEN);
    }

    @Test
    public void givenNoCaseFound_whenAmendToDraftPetitionForRefusal_thenReturn404() throws DuplicateCaseException {

        when(petitionService.createAmendedPetitionDraftRefusal(TEST_AUTH_TOKEN))
            .thenReturn(null);

        ResponseEntity<Map<String, Object>> actual = classUnderTest.createAmendedPetitionDraftRefusal(TEST_AUTH_TOKEN);

        assertEquals(HttpStatus.NOT_FOUND, actual.getStatusCode());
        assertNull(actual.getBody());

        verify(petitionService).createAmendedPetitionDraftRefusal(TEST_AUTH_TOKEN);
    }

    @Test
    public void givenCaseFound_whenAmendToDraftPetitionForRefusal_thenReturnDraftData() throws DuplicateCaseException {

        final Map<String, Object> draftData = new HashMap<>();

        when(petitionService.createAmendedPetitionDraftRefusal(TEST_AUTH_TOKEN))
            .thenReturn(draftData);

        ResponseEntity<Map<String, Object>> actual = classUnderTest.createAmendedPetitionDraftRefusal(TEST_AUTH_TOKEN);

        assertEquals(HttpStatus.OK, actual.getStatusCode());
        assertEquals(draftData, actual.getBody());

        verify(petitionService).createAmendedPetitionDraftRefusal(TEST_AUTH_TOKEN);
    }

    @Test
    public void givenCaseFound_whenAmendToDraftPetitionForRefusal_thenSetDraftDataFromCase() throws DuplicateCaseException {
        final Map<String, Object> draftData = new HashMap<>();
        final SimpleDateFormat createdDate = new SimpleDateFormat(CmsConstants.YEAR_DATE_FORMAT);

        draftData.put(DivorceSessionProperties.PREVIOUS_CASE_ID, TEST_CASE_ID);
        draftData.put(DivorceSessionProperties.CREATED_DATE, createdDate.toPattern());
        draftData.put(DivorceSessionProperties.COURTS, CmsConstants.CTSC_SERVICE_CENTRE);

        when(petitionService.createAmendedPetitionDraftRefusal(TEST_AUTH_TOKEN))
            .thenReturn(draftData);

        ResponseEntity<Map<String, Object>> actual = classUnderTest.createAmendedPetitionDraftRefusal(TEST_AUTH_TOKEN);

        assertEquals(HttpStatus.OK, actual.getStatusCode());
        assertEquals(draftData, actual.getBody());

        verify(petitionService).createAmendedPetitionDraftRefusal(TEST_AUTH_TOKEN);
    }
}
