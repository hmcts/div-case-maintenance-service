package uk.gov.hmcts.reform.divorce.casemaintenanceservice.exception;

public class InvalidRequestException extends RuntimeException {
    public InvalidRequestException(String message) {
        super(message);
    }
}
