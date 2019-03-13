package uk.gov.hmcts.reform.divorce.casemaintenanceservice.exception;

import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public abstract class BaseException extends RuntimeException {

    @Setter
    private HttpStatus status;

    BaseException(String message) {
        super(message);
    }

    public ResponseEntity<Object> getResponse() {
        return ResponseEntity.status(status).body(this.getMessage());
    }
}
