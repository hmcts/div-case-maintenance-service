package uk.gov.hmcts.reform.divorce.casemaintenanceservice.exception;

import org.springframework.http.HttpStatus;

public class CaseNotFoundException extends BaseException {
    public CaseNotFoundException(String message) {
        super(message);
        this.status = HttpStatus.NOT_FOUND;
    }
}
