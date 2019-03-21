package uk.gov.hmcts.reform.divorce.casemaintenanceservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public abstract class BaseException extends RuntimeException {

    private final HttpStatus status;

    BaseException(String message, HttpStatus httpStatus) {
        super(message);
        this.status = httpStatus;
    }

    public ResponseEntity<Object> getResponse() {
        return ResponseEntity.status(status).body(this.getMessage());
    }
}
