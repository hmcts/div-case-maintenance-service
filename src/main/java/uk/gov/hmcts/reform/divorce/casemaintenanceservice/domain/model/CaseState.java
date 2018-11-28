package uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@AllArgsConstructor
@Getter
public enum CaseState {
    AMEND_PETITION("AmendPetition", ApplicationStatus.AWAITING_REISSUE),

    AOS_AWAITING("AosAwaiting", ApplicationStatus.AOS_AWAITING),
    AOS_STARTED("AosStarted", ApplicationStatus.AOS_STARTED),
    AOS_COMPLETED("AosCompleted", ApplicationStatus.AOS_COMPLETED),
    AOS_SUBMITTED_AWAITING_ANSWER("AosSubmittedAwaitingAnswer", ApplicationStatus.AOS_SUBMITTED_AWAITING_ANSWER),
    AOS_OVERDUE("AosOverdue", ApplicationStatus.AOS_OVERDUE),

    AWAITING_CONSIDERATION_DN("AwaitingConsiderationDN",
        ApplicationStatus.AWAITING_CONSIDERATION_DN),
    AWAITING_CONSIDERATION_DA("AwaitingConsiderationDA",
        ApplicationStatus.AWAITING_CONSIDERATION_DA),
    AWAITING_CLARIFICATION("AwaitingClarification",
        ApplicationStatus.AWAITING_CLARIFICATION),
    AWAITING_CONSIDERATION_GENERAL_APPLICATION("AwaitingConsiderationGeneralApplication",
        ApplicationStatus.AWAITING_CONSIDERATION_GENERAL_APPLICATION),
    AWAITING_DECREE_ABSOLUTE("AwaitingDecreeAbsolute", ApplicationStatus.AWAITING_DECREE_ABSOLUTE),
    AWAITING_DECREE_ABSOLUTE_PETITIONER("AwaitingDecreeAbsolutePetitioner",
        ApplicationStatus.AWAITING_DECREE_ABSOLUTE_PETITIONER),
    AWAITING_DA_APPLICATION_DUE_DATE("AwaitingDAApplicationDueDate", ApplicationStatus.AWAITING_DA_APPLICATION_DUE_DATE),
    AWAITING_DECREE_NISI("AwaitingDecreeNisi", ApplicationStatus.DN_AWAITING),
    AWAITING_DOCUMENTS("AwaitingDocuments", ApplicationStatus.PETITION_COMPLETED),
    AWAITING_HWF_DECISION("AwaitingHWFDecision", ApplicationStatus.PETITION_COMPLETED),
    AWAITING_LEGAL_ADVISOR_REFERRAL("AwaitingLegalAdvisorReferral", ApplicationStatus.AWAITING_LEGAL_ADVISOR_REFERRAL),
    AWAITING_LISTING("AwaitingListing", ApplicationStatus.AWAITING_LISTING),
    AWAITING_PAYMENT("AwaitingPayment", ApplicationStatus.AWAITING_PAYMENT),
    AWAITING_PETITIONER("AwaitingPetitioner", ApplicationStatus.AWAITING_PETITIONER),
    AWAITING_PRONOUNCEMENT("AwaitingPronouncement", ApplicationStatus.AWAITING_PRONOUNCEMENT),
    AWAITING_REISSUE("AwaitingReissue", ApplicationStatus.AWAITING_REISSUE),

    DEFENDED_DIVORCE("DefendedDivorce", ApplicationStatus.DEFENDED_DIVORCE),
    DA_APPLICATION_OVERDUE("DAApplicationOverdue", ApplicationStatus.DA_APPLICATION_OVERDUE),
    DA_APPLICATION_RECEIVED("DAApplicationReceived", ApplicationStatus.DA_APPLICATION_RECEIVED),
    DIVORCE_GRANTED("DivorceGranted", ApplicationStatus.DIVORCE_GRANTED),
    ISSUED("Issued", ApplicationStatus.PETITION_COMPLETED),
    PENDING_REJECTION("PendingRejection", ApplicationStatus.PENDING_REJECTION),
    REJECTED("Rejected", ApplicationStatus.REJECTED),
    SUBMITTED("Submitted", ApplicationStatus.PETITION_COMPLETED),
    WITHDRAWN("Withdrawn", ApplicationStatus.WITHDRAWN),
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
