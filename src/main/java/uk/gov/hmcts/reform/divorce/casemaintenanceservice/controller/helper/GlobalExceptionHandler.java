package uk.gov.hmcts.reform.divorce.casemaintenanceservice.controller.helper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler({IllegalArgumentException.class, NullPointerException.class})
    public ResponseEntity<Object> handleBadRequestException(Exception exception) {
        log.warn(exception.getMessage(), exception);
        return ResponseEntity.badRequest().build();
    }


}
