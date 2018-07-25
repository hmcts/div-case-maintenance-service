package uk.gov.hmcts.reform.divorce.casemaintenanceservice.event.ccd.submission;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

@Getter
public class CaseSubmittedEvent extends ApplicationEvent {
    private final CaseDetails caseDetails;
    private final String authToken;

    CaseSubmittedEvent(Object source, CaseDetails caseDetails, String authToken) {
        super(source);

        this.authToken = authToken;
        this.caseDetails = caseDetails;
    }
}
