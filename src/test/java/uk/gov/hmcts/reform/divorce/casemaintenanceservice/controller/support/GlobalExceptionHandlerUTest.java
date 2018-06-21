package uk.gov.hmcts.reform.divorce.casemaintenanceservice.controller.support;

import feign.FeignException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GlobalExceptionHandlerUTest {
    private GlobalExceptionHandler classUnderTest = new GlobalExceptionHandler();

    @Test
    public void whenHandleBadRequestException_thenReturnBadRequest() {
        final int statusCode = HttpStatus.BAD_REQUEST.value();
        final String errorMessage = "some error message";

        final FeignException feignException = mock(FeignException.class);

        when(feignException.status()).thenReturn(statusCode);
        when(feignException.getMessage()).thenReturn(errorMessage);

        ResponseEntity<Object> response = classUnderTest.handleBadRequestException(feignException);

        assertEquals(statusCode, response.getStatusCodeValue());
        assertEquals(errorMessage, response.getBody());
    }
}
