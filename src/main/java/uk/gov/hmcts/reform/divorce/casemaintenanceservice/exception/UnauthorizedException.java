package uk.gov.hmcts.reform.divorce.casemaintenanceservice.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends BaseException {
    public UnauthorizedException(String message) {
        super(message);
        this.setStatus(HttpStatus.UNAUTHORIZED);
    }
}
