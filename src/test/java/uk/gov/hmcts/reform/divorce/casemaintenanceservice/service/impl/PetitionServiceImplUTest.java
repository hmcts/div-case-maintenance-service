package uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.exception.DuplicateCaseException;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.CcdRetrievalService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PetitionServiceImplUTest {

    @Mock
    private CcdRetrievalService ccdRetrievalService;

    @InjectMocks
    private PetitionServiceImpl classUnderTest;

    @Test
    public void givenCcdRetrievalServiceReturnsCase_whenRetrievePetition_thenProceedAsExpected() throws DuplicateCaseException {
        final String authorisation = "userToken";
        final CaseDetails caseDetails = CaseDetails.builder().build();

        when(ccdRetrievalService.retrievePetition(authorisation)).thenReturn(caseDetails);

        CaseDetails actual = classUnderTest.retrievePetition(authorisation, true);

        assertEquals(caseDetails, actual);
        verify(ccdRetrievalService).retrievePetition(authorisation);
    }

    @Test(expected = DuplicateCaseException.class)
    public void givenCcdRetrievalServiceThrowException_whenRetrievePetition_thenThrowException() throws DuplicateCaseException {
        final String authorisation = "userToken";
        final DuplicateCaseException duplicateCaseException = new DuplicateCaseException("Duplicate");

        when(ccdRetrievalService.retrievePetition(authorisation)).thenThrow(duplicateCaseException);

        classUnderTest.retrievePetition(authorisation, true);

        verify(ccdRetrievalService).retrievePetition(authorisation);
    }

    @Test
    public void givenCheckCcdFalse_whenRetrievePetition_thenDoNotCheckCcd() throws DuplicateCaseException {
        final String authorisation = "userToken";

        assertNull(classUnderTest.retrievePetition(authorisation, false));

        verifyZeroInteractions(ccdRetrievalService);
    }
}
