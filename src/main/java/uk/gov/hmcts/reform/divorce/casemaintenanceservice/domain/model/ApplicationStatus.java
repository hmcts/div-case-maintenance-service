package uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ApplicationStatus {
    AWAITING_PETITIONER("AwaitingPetitioner"),
    AWAITING_PAYMENT("AwaitingPayment"),
    PETITION_COMPLETED("PetitionCompleted"),
    PENDING_REJECTION("PendingRejection"),
    AOS_AWAITING("AosAwaiting"),
    AOS_STARTED("AosStarted"),
    AOS_COMPLETED("AosCompleted"),
    AOS_OVERDUE("AosOverdue"),
    AWAITING_REISSUE("AwaitingReissue"),
    DN_AWAITING("DNAwaiting"),
    DN_STARTED("DNStarted"),
    DN_COMPLETED("DNCompleted"),
    REJECTED("Rejected"),
    AWAITING_LEGAL_ADVISOR_REFERRAL("AwaitingLegalAdvisorReferral"),
    AWAITING_CONSIDERATION_GENERAL_APPLICATION("AwaitingConsiderationGeneralApplication"),
    AWAITING_CONSIDERATION_DN("AwaitingConsiderationDN"),
    DEFENDED_DIVORCE("DefendedDivorce"),
    AOS_SUBMITTED_AWAITING_ANSWER("AosSubmittedAwaitingAnswer"),
    UNKNOWN("DNCompleted");

    private final String value;
}
