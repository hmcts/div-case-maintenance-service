package uk.gov.hmcts.reform.divorce.casemaintenanceservice.controller;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.CcdAccessService;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.CcdRetrievalService;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.CcdSubmissionService;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.CcdUpdateService;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.TestConstants.TEST_AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.TestConstants.TEST_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.TestConstants.TEST_LETTER_HOLDER_ID_CODE;

@RunWith(MockitoJUnitRunner.class)
public class CcdControllerUTest {
    private static final CaseDetails CASE_DETAILS = CaseDetails.builder().build();
    private static final Map<String, Object> CASE_DATA_CONTENT = new HashMap<>();

    @Mock
    private CcdSubmissionService ccdSubmissionService;

    @Mock
    private CcdUpdateService ccdUpdateService;

    @Mock
    private CcdAccessService ccdAccessService;

    @Mock
    private CcdRetrievalService ccdRetrievalService;

    @InjectMocks
    private CcdController classUnderTest;

    @Test
    public void whenSubmitCase_thenProceedAsExpected() {
        when(ccdSubmissionService.submitCase(CASE_DATA_CONTENT, TEST_AUTH_TOKEN)).thenReturn(CASE_DETAILS);

        ResponseEntity<CaseDetails> responseEntity = classUnderTest.submitCase(CASE_DATA_CONTENT, TEST_AUTH_TOKEN);

        assertEquals(CASE_DETAILS, responseEntity.getBody());
        assertEquals(HttpStatus.OK.value(), responseEntity.getStatusCodeValue());

        verify(ccdSubmissionService).submitCase(CASE_DATA_CONTENT, TEST_AUTH_TOKEN);
    }

    @Test
    public void whenSubmitCaseForSolicitor_thenProceedAsExpected() {
        when(ccdSubmissionService.submitCaseForSolicitor(CASE_DATA_CONTENT, TEST_AUTH_TOKEN)).thenReturn(CASE_DETAILS);

        ResponseEntity<CaseDetails> responseEntity =
            classUnderTest.submitCaseForSolicitor(CASE_DATA_CONTENT, TEST_AUTH_TOKEN);

        assertEquals(CASE_DETAILS, responseEntity.getBody());
        assertEquals(HttpStatus.OK.value(), responseEntity.getStatusCodeValue());

        verify(ccdSubmissionService).submitCaseForSolicitor(CASE_DATA_CONTENT, TEST_AUTH_TOKEN);
    }

    @Test
    public void whenUpdateCase_thenProceedAsExpected() {
        when(ccdUpdateService.update(TEST_CASE_ID, CASE_DATA_CONTENT,  TEST_EVENT_ID, TEST_AUTH_TOKEN)).thenReturn(CASE_DETAILS);

        ResponseEntity<CaseDetails> responseEntity =
            classUnderTest.updateCase(TEST_CASE_ID, CASE_DATA_CONTENT, TEST_EVENT_ID, TEST_AUTH_TOKEN);

        assertEquals(CASE_DETAILS, responseEntity.getBody());
        assertEquals(HttpStatus.OK.value(), responseEntity.getStatusCodeValue());

        verify(ccdUpdateService).update(TEST_CASE_ID, CASE_DATA_CONTENT, TEST_EVENT_ID, TEST_AUTH_TOKEN);
    }

    @Test
    public void whenLinkRespondent_thenProceedAsExpected() {
        doNothing().when(ccdAccessService).linkRespondent(TEST_AUTH_TOKEN, TEST_CASE_ID, TEST_LETTER_HOLDER_ID_CODE);

        ResponseEntity responseEntity =
            classUnderTest.linkRespondent(TEST_AUTH_TOKEN, TEST_CASE_ID, TEST_LETTER_HOLDER_ID_CODE);

        assertEquals(responseEntity.getStatusCodeValue(), HttpStatus.OK.value());

        verify(ccdAccessService).linkRespondent(TEST_AUTH_TOKEN, TEST_CASE_ID, TEST_LETTER_HOLDER_ID_CODE);
    }

    @Test
    public void whenAssignPetitionerSolicitorRole_thenProceedAsExpected() {
        doNothing().when(ccdAccessService).addPetitionerSolicitorRole(TEST_AUTH_TOKEN, TEST_CASE_ID);

        ResponseEntity responseEntity =
            classUnderTest.addPetitionerSolicitorRole(TEST_AUTH_TOKEN, TEST_CASE_ID);

        assertThat(responseEntity.getStatusCodeValue(), is(HttpStatus.OK.value()));

        verify(ccdAccessService).addPetitionerSolicitorRole(TEST_AUTH_TOKEN, TEST_CASE_ID);
    }

    @Test
    public void whenUnlinkRespondent_thenProceedAsExpected() {
        doNothing().when(ccdAccessService).unlinkRespondent(TEST_AUTH_TOKEN, TEST_CASE_ID);

        ResponseEntity responseEntity = classUnderTest.unlinkRespondent(TEST_AUTH_TOKEN, TEST_CASE_ID);

        assertEquals(responseEntity.getStatusCodeValue(), HttpStatus.OK.value());

        verify(ccdAccessService).unlinkRespondent(TEST_AUTH_TOKEN, TEST_CASE_ID);
    }

    @Test
    public void whenSearchCases_thenProceedAsExpected() {
        String query = "anyQuery";

        SearchResult expectedResult = SearchResult
            .builder()
            .build();
        when(ccdRetrievalService.searchCase(TEST_AUTH_TOKEN, query)).thenReturn(expectedResult);

        ResponseEntity<SearchResult> caseResult = classUnderTest.search(TEST_AUTH_TOKEN, query);

        assertEquals(HttpStatus.OK, caseResult.getStatusCode());
        assertEquals(expectedResult, caseResult.getBody());
    }

    @Test
    public void whenSubmitBulkCase_thenProceedAsExpected() {
        Map<String, Object> inputData = ImmutableMap.of("key", "value");

        CaseDetails expectedResult = CaseDetails.builder().build();
        when(ccdSubmissionService.submitBulkCase(inputData, TEST_AUTH_TOKEN)).thenReturn(expectedResult);

        ResponseEntity<CaseDetails> caseResult = classUnderTest.submitBulkCase(inputData, TEST_AUTH_TOKEN);

        assertEquals(HttpStatus.OK, caseResult.getStatusCode());
        assertEquals(expectedResult, caseResult.getBody());
    }

    @Test
    public void whenUpdateBulkCase_thenProceedAsExpected() {
        when(ccdUpdateService.updateBulkCase(TEST_CASE_ID, CASE_DATA_CONTENT,  TEST_EVENT_ID, TEST_AUTH_TOKEN)).thenReturn(CASE_DETAILS);

        ResponseEntity<CaseDetails> responseEntity =
            classUnderTest.updateBulkCase(TEST_CASE_ID, CASE_DATA_CONTENT, TEST_EVENT_ID, TEST_AUTH_TOKEN);

        assertEquals(CASE_DETAILS, responseEntity.getBody());
        assertEquals(HttpStatus.OK.value(), responseEntity.getStatusCodeValue());

        verify(ccdUpdateService).updateBulkCase(TEST_CASE_ID, CASE_DATA_CONTENT, TEST_EVENT_ID, TEST_AUTH_TOKEN);
    }
}
