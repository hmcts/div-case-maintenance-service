package uk.gov.hmcts.reform.divorce.casemaintenanceservice.exception;

import org.springframework.http.HttpStatus;

public class InvalidRequestException extends BaseException {
    public InvalidRequestException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
