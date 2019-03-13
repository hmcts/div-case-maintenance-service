package uk.gov.hmcts.reform.divorce.casemaintenanceservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public abstract class BaseException extends RuntimeException {
    public HttpStatus status;

    BaseException(String message) {
        super(message);
    }

    public ResponseEntity<Object> getResponse() {
        return ResponseEntity.status(status).body(this.getMessage());
    }
}
