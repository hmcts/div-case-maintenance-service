package uk.gov.hmcts.reform.divorce.casemaintenanceservice.exception;

public class CaseNotFoundException extends RuntimeException {
    public CaseNotFoundException(String message) {
        super(message);
    }
}
