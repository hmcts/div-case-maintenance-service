package uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@AllArgsConstructor
@Getter
public enum CaseState {
    AWAITING_PETITIONER("AwaitingPetitioner", ApplicationStatus.AWAITING_PETITIONER),
    AWAITING_PAYMENT("AwaitingPayment", ApplicationStatus.AWAITING_PAYMENT),
    AWAITING_HWF_DECISION("AwaitingHWFDecision", ApplicationStatus.PETITION_COMPLETED),
    SUBMITTED("Submitted", ApplicationStatus.PETITION_COMPLETED),
    ISSUED("Issued", ApplicationStatus.PETITION_COMPLETED),
    PENDING_REJECTION("PendingRejection", ApplicationStatus.PENDING_REJECTION),
    AWAITING_DOCUMENTS("AwaitingDocuments", ApplicationStatus.PETITION_COMPLETED),
    AOS_AWAITING("AosAwaiting", ApplicationStatus.AOS_AWAITING),
    AOS_STARTED("AosStarted", ApplicationStatus.AOS_STARTED),
    AOS_COMPLETED("AosCompleted", ApplicationStatus.AOS_COMPLETED),
    AOS_OVERDUE("AosOverdue", ApplicationStatus.AOS_OVERDUE),
    AWAITING_REISSUE("AwaitingReissue", ApplicationStatus.AWAITING_REISSUE),
    AOS_SUBMITTED_AWAITING_ANSWER("AosSubmittedAwaitingAnswer", ApplicationStatus.AOS_SUBMITTED_AWAITING_ANSWER),
    AWAITING_DECREE_NISI("AwaitingDecreeNisi", ApplicationStatus.DN_AWAITING),
    REJECTED("Rejected", ApplicationStatus.REJECTED),
    AWAITING_LEGAL_ADVISOR_REFERRAL("AwaitingLegalAdvisorReferral", ApplicationStatus.AWAITING_LEGAL_ADVISOR_REFERRAL),
    DEFENDED_DIVORCE("DefendedDivorce", ApplicationStatus.DEFENDED_DIVORCE),
    AWAITING_CONSIDERATION_GENERAL_APPLICATION("AwaitingConsiderationGeneralApplication",
        ApplicationStatus.AWAITING_CONSIDERATION_GENERAL_APPLICATION),
    AWAITING_CONSIDERATION_DN("AwaitingConsiderationDN",
        ApplicationStatus.AWAITING_CONSIDERATION_GENERAL_APPLICATION),
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
