package uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@AllArgsConstructor
@Getter
public enum CaseState {
    AWAITING_PAYMENT("AwaitingPayment"),
    AWAITING_HWF_DECISION("AwaitingHWFDecision"),
    SUBMITTED("Submitted"),
    ISSUED("Issued"),
    PENDING_REJECTION("PendingRejection"),
    AWAITING_DOCUMENTS("AwaitingDocuments"),
    AOS_AWAITING("AosAwaiting"),
    AOS_RESPONDED("AosResponded"),
    AOS_COMPLETED("AosCompleted"),
    AWAITING_DECREE_NISI("AwaitingDecreeNisi"),
    UNKNOWN("Unknown");

    private final String value;

    public static CaseState getState(String state) {
        return Arrays.stream(CaseState.values())
            .filter(caseState -> caseState.value.equalsIgnoreCase(state))
            .findFirst()
            .orElse(UNKNOWN);
    }
}
