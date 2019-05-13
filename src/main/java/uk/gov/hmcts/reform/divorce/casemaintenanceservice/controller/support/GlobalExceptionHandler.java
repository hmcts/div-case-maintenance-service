package uk.gov.hmcts.reform.divorce.casemaintenanceservice.controller.support;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.exception.BaseException;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private static final String CASE_REFERENCE_IS_NOT_VALID = "Case reference is not valid";

    @ExceptionHandler(FeignException.class)
    ResponseEntity<Object> handleBadRequestException(FeignException exception) {

        int statusCode = exception.status();

        String contentUTF8 = exception.contentUTF8();

        log.warn(contentUTF8, exception);

        if (contentUTF8 != null && contentUTF8.contains(CASE_REFERENCE_IS_NOT_VALID)) {
            //This happens when the case is not found in CCD
            statusCode = HttpStatus.UNAUTHORIZED.value();
        }
        return ResponseEntity.status(statusCode).body(
            String.format("%s - %s", exception.getMessage(), contentUTF8)
        );
    }

    @ExceptionHandler(BaseException.class)
    ResponseEntity<Object> handleApplicationException(BaseException exception) {
        log.warn(exception.getMessage(), exception);

        return exception.getResponse();
    }

    @ExceptionHandler(HttpClientErrorException.class)
    ResponseEntity<Object> handleServiceAuthErrorException(HttpClientErrorException exception) {
        log.warn(exception.getMessage(), exception);

        return ResponseEntity.status(exception.getStatusCode()).build();
    }
}
