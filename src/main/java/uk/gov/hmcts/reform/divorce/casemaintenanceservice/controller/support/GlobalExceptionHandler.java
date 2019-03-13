package uk.gov.hmcts.reform.divorce.casemaintenanceservice.controller.support;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.exception.BaseException;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.exception.CaseNotFoundException;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.exception.InvalidRequestException;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.exception.UnauthorizedException;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(FeignException.class)
    ResponseEntity<Object> handleBadRequestException(FeignException exception) {
        log.warn(exception.getMessage(), exception);

        return ResponseEntity.status(exception.status()).body(exception.getMessage());
    }

    @ExceptionHandler({CaseNotFoundException.class, InvalidRequestException.class, UnauthorizedException.class})
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
