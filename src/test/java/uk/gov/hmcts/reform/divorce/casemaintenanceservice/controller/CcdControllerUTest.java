package uk.gov.hmcts.reform.divorce.casemaintenanceservice.controller;

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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CcdControllerUTest {
    private static final CaseDetails CASE_DETAILS = CaseDetails.builder().build();
    private static final Map<String, Object> CASE_DATA_CONTENT = new HashMap<>();
    private static final String JWT_TOKEN = "token";

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
        when(ccdSubmissionService.submitCase(CASE_DATA_CONTENT, JWT_TOKEN)).thenReturn(CASE_DETAILS);

        ResponseEntity<CaseDetails> responseEntity = classUnderTest.submitCase(CASE_DATA_CONTENT, JWT_TOKEN);

        assertEquals(responseEntity.getBody(), CASE_DETAILS);
        assertEquals(responseEntity.getStatusCodeValue(), HttpStatus.OK.value());

        verify(ccdSubmissionService).submitCase(CASE_DATA_CONTENT, JWT_TOKEN);
    }

    @Test
    public void whenUpdateCase_thenProceedAsExpected() {
        final String caseId = "caseId";
        final String eventId = "eventId";

        when(ccdUpdateService.update(caseId, CASE_DATA_CONTENT,  eventId, JWT_TOKEN)).thenReturn(CASE_DETAILS);

        ResponseEntity<CaseDetails> responseEntity =
            classUnderTest.updateCase(caseId, CASE_DATA_CONTENT, eventId, JWT_TOKEN);

        assertEquals(CASE_DETAILS, responseEntity.getBody());
        assertEquals(HttpStatus.OK.value(), responseEntity.getStatusCodeValue());

        verify(ccdUpdateService).update(caseId, CASE_DATA_CONTENT, eventId, JWT_TOKEN);
    }

    @Test
    public void whenLinkRespondent_thenProceedAsExpected() {
        final String caseId = "caseId";
        final String letterHolderId = "letterHolderId";

        doNothing().when(ccdAccessService).linkRespondent(JWT_TOKEN, caseId, letterHolderId);

        ResponseEntity responseEntity =
            classUnderTest.linkRespondent(JWT_TOKEN, caseId, letterHolderId);

        assertEquals(responseEntity.getStatusCodeValue(), HttpStatus.OK.value());

        verify(ccdAccessService).linkRespondent(JWT_TOKEN, caseId, letterHolderId);
    }

    @Test
    public void whenUnlinkRespondent_thenProceedAsExpected() {
        final String caseId = "caseId";

        doNothing().when(ccdAccessService).unlinkRespondent(JWT_TOKEN, caseId);

        ResponseEntity responseEntity = classUnderTest.unlinkRespondent(JWT_TOKEN, caseId);

        assertEquals(responseEntity.getStatusCodeValue(), HttpStatus.OK.value());

        verify(ccdAccessService).unlinkRespondent(JWT_TOKEN, caseId);
    }

    @Test
    public void  whenSearchCases_thenProceedAsExpected() {
        String query = "anyQuery";

        SearchResult expectedResult = SearchResult
            .builder()
            .build();
        when(ccdRetrievalService.searchCase(JWT_TOKEN, query)).thenReturn(expectedResult);

        ResponseEntity<SearchResult> caseResult = classUnderTest.search(JWT_TOKEN, query);

        assertEquals(HttpStatus.OK, caseResult.getStatusCode());
        assertEquals(expectedResult, caseResult.getBody());
    }

}
