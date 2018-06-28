package uk.gov.hmcts.reform.divorce.casemaintenanceservice.controller.support;

import feign.FeignException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GlobalExceptionHandlerUTest {
    private final GlobalExceptionHandler classUnderTest = new GlobalExceptionHandler();

    @Test
    public void whenHandleBadRequestException_thenReturnUnderLyingError() {
        final int statusCode = HttpStatus.BAD_REQUEST.value();
        final String errorMessage = "some error message";

        final FeignException feignException = mock(FeignException.class);

        when(feignException.status()).thenReturn(statusCode);
        when(feignException.getMessage()).thenReturn(errorMessage);

        ResponseEntity<Object> response = classUnderTest.handleBadRequestException(feignException);

        assertEquals(statusCode, response.getStatusCodeValue());
        assertEquals(errorMessage, response.getBody());
    }

    @Test
    public void whenHandleServiceAuthErrorException_thenReturnUnderLyingError() {
        final HttpStatus statusCode = HttpStatus.BAD_REQUEST;

        final HttpClientErrorException exception = new HttpClientErrorException(statusCode);

        ResponseEntity<Object> response = classUnderTest.handleServiceAuthErrorException(exception);

        assertEquals(statusCode, response.getStatusCode());
    }
}
