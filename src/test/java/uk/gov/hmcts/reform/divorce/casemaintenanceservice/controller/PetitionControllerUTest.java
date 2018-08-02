package uk.gov.hmcts.reform.divorce.casemaintenanceservice.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.exception.DuplicateCaseException;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.PetitionService;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PetitionControllerUTest {

    @Mock
    private PetitionService petitionService;

    @InjectMocks
    private PetitionController classUnderTest;

    @Test
    public void givenCaseFound_whenRetrievePetition_thenReturnCaseDetails() throws DuplicateCaseException {
        final String authorisation = "user";
        final boolean checkCcd = true;

        final CaseDetails caseDetails = CaseDetails.builder().build();

        when(petitionService.retrievePetition(authorisation, checkCcd)).thenReturn(caseDetails);

        ResponseEntity<Object> actual = classUnderTest.retrievePetition(authorisation, checkCcd);

        assertEquals(HttpStatus.OK, actual.getStatusCode());
        assertEquals(caseDetails, actual.getBody());

        verify(petitionService).retrievePetition(authorisation, checkCcd);
    }

    @Test
    public void givenDuplicateCase_whenRetrievePetition_thenReturnHttpStatus300() throws DuplicateCaseException {
        final String authorisation = "user";
        final boolean checkCcd = true;

        when(petitionService.retrievePetition(authorisation, checkCcd))
            .thenThrow(new DuplicateCaseException("Duplicate"));

        ResponseEntity<Object> actual = classUnderTest.retrievePetition(authorisation, checkCcd);

        assertEquals(HttpStatus.MULTIPLE_CHOICES, actual.getStatusCode());

        verify(petitionService).retrievePetition(authorisation, checkCcd);
    }
}
