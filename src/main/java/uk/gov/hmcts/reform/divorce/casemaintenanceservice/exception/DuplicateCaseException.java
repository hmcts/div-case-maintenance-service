package uk.gov.hmcts.reform.divorce.casemaintenanceservice.exception;

import org.springframework.http.HttpStatus;

public class DuplicateCaseException extends BaseException {
    public DuplicateCaseException(String message) {
        super(message, HttpStatus.MULTIPLE_CHOICES);
    }
}
