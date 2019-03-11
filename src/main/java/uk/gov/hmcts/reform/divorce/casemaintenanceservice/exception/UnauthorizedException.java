package uk.gov.hmcts.reform.divorce.casemaintenanceservice.exception;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
