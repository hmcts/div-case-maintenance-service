package uk.gov.hmcts.reform.divorce.casemaintenanceservice.controller.support;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.exception.CaseNotFoundException;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(FeignException.class)
    ResponseEntity<Object> handleBadRequestException(FeignException exception) {
        log.warn(exception.getMessage(), exception);

        return ResponseEntity.status(exception.status()).body(exception.getMessage());
    }

    @ExceptionHandler(CaseNotFoundException.class)
    ResponseEntity<Object> handleCaseNotFoundException(CaseNotFoundException exception) {
        log.warn(exception.getMessage(), exception);

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
    }

    @ExceptionHandler(HttpClientErrorException.class)
    ResponseEntity<Object> handleServiceAuthErrorException(HttpClientErrorException exception) {
        log.warn(exception.getMessage(), exception);

        return ResponseEntity.status(exception.getStatusCode()).build();
    }
}