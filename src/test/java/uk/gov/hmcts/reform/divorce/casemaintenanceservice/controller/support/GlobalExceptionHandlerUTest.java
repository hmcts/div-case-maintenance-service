package uk.gov.hmcts.reform.divorce.casemaintenanceservice.controller.support;

import feign.FeignException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.exception.CaseNotFoundException;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.exception.DuplicateCaseException;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.exception.InvalidRequestException;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.exception.UnauthorizedException;

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
        final String errorContent = "some error content";

        final FeignException feignException = mock(FeignException.class);

        when(feignException.status()).thenReturn(statusCode);
        when(feignException.getMessage()).thenReturn(errorMessage);
        when(feignException.contentUTF8()).thenReturn(errorContent);

        ResponseEntity<Object> response = classUnderTest.handleBadRequestException(feignException);

        assertEquals(statusCode, response.getStatusCodeValue());
        assertEquals(errorMessage + " - " + errorContent, response.getBody());
    }

    @Test
    public void givenInvalidCaseIdBadException_whenHandleBadException_thenReturnUnauthorised() {
        final int statusCode = HttpStatus.BAD_REQUEST.value();
        final String errorMessage = "some error message";
        final String errorContent = "Case reference is not valid";

        final FeignException feignException = mock(FeignException.class);

        when(feignException.status()).thenReturn(statusCode);
        when(feignException.getMessage()).thenReturn(errorMessage);
        when(feignException.contentUTF8()).thenReturn(errorContent);

        ResponseEntity<Object> response = classUnderTest.handleBadRequestException(feignException);

        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatusCodeValue());
        assertEquals(errorMessage + " - " + errorContent, response.getBody());
    }

    @Test
    public void whenHandleCaseNotFoundException_thenReturnUnderLyingError() {
        final HttpStatus statusCode = HttpStatus.NOT_FOUND;
        final String errorMessage = "Error Message";

        final CaseNotFoundException exception = new CaseNotFoundException(errorMessage);

        ResponseEntity<Object> response = classUnderTest.handleApplicationException(exception);

        assertEquals(statusCode, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());
    }

    @Test
    public void whenDuplicateCaseException_thenReturnUnderLyingError() {
        final HttpStatus statusCode = HttpStatus.MULTIPLE_CHOICES;
        final String errorMessage = "Error Message";

        final DuplicateCaseException exception = new DuplicateCaseException(errorMessage);

        ResponseEntity<Object> response = classUnderTest.handleApplicationException(exception);

        assertEquals(statusCode, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());
    }

    @Test
    public void whenInvalidRequestException_thenReturnUnderLyingError() {
        final HttpStatus statusCode = HttpStatus.BAD_REQUEST;
        final String errorMessage = "Error Message";

        final InvalidRequestException exception = new InvalidRequestException(errorMessage);

        ResponseEntity<Object> response = classUnderTest.handleApplicationException(exception);

        assertEquals(statusCode, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());
    }

    @Test
    public void whenUnauthorizedException_thenReturnUnderLyingError() {
        final HttpStatus statusCode = HttpStatus.UNAUTHORIZED;
        final String errorMessage = "Error Message";

        final UnauthorizedException exception = new UnauthorizedException(errorMessage);

        ResponseEntity<Object> response = classUnderTest.handleApplicationException(exception);

        assertEquals(statusCode, response.getStatusCode());
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
