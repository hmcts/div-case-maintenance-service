package uk.gov.hmcts.reform.divorce.casemaintenanceservice.exception;

public class DuplicateCaseException extends Exception {
    public DuplicateCaseException(String message) {
        super(message);
    }
}
