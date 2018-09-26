package uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@AllArgsConstructor
@Getter
public enum CaseState {
    AWAITING_PAYMENT("AwaitingPayment", ApplicationStatus.AWAITING_PAYMENT),
    AWAITING_HWF_DECISION("AwaitingHWFDecision", ApplicationStatus.PETITION_COMPLETED),
    SUBMITTED("Submitted", ApplicationStatus.PETITION_COMPLETED),
    ISSUED("Issued", ApplicationStatus.PETITION_COMPLETED),
    PENDING_REJECTION("PendingRejection", ApplicationStatus.PETITION_COMPLETED),
    AWAITING_DOCUMENTS("AwaitingDocuments", ApplicationStatus.PETITION_COMPLETED),
    AOS_AWAITING("AosAwaiting", ApplicationStatus.AOS_AWAITING),
    AOS_STARTED("AosStarted", ApplicationStatus.AOS_STARTED),
    AOS_COMPLETED("AosCompleted", ApplicationStatus.AOS_COMPLETED),
    AOS_COMPLETED_AWAITING_ANSWER("AosCompletedAwaitingAnswer", ApplicationStatus.AOS_COMPLETED),
    AWAITING_DECREE_NISI("AwaitingDecreeNisi", ApplicationStatus.DN_AWAITING),
    REJECTED("Rejected", ApplicationStatus.REJECTED),
    UNKNOWN("Unknown", ApplicationStatus.UNKNOWN);

    private final String value;
    private final ApplicationStatus status;

    public static CaseState getState(String state) {
        return Arrays.stream(CaseState.values())
            .filter(caseState -> caseState.value.equalsIgnoreCase(state))
            .findFirst()
            .orElse(UNKNOWN);
    }
}
