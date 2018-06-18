package uk.gov.hmcts.reform.divorce.casemanagementservice.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.controller.CcdController;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.CcdSubmissionService;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.CcdUpdateService;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CcdControllerUTest {
    private static final CaseDetails CASE_DETAILS = CaseDetails.builder().build();
    private static final Object CASE_DATA_CONTENT = new Object();
    private static final String JWT_TOKEN = "token";

    @Mock
    private CcdSubmissionService ccdSubmissionService;

    @Mock
    private CcdUpdateService ccdUpdateService;

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

        assertEquals(responseEntity.getBody(), CASE_DETAILS);
        assertEquals(responseEntity.getStatusCodeValue(), HttpStatus.OK.value());

        verify(ccdUpdateService).update(caseId, CASE_DATA_CONTENT, eventId, JWT_TOKEN);
    }
}
