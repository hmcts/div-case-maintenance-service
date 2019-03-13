package uk.gov.hmcts.reform.divorce.casemaintenanceservice.exception;

import org.springframework.http.HttpStatus;

public class DuplicateCaseException extends BaseException {
    public DuplicateCaseException(String message) {
        super(message);
        this.setStatus(HttpStatus.MULTIPLE_CHOICES);
    }
}
