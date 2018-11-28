package uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ApplicationStatus {
    AMEND_PETITION("AmendPetition"),

    AOS_AWAITING("AosAwaiting"),
    AOS_STARTED("AosStarted"),
    AOS_COMPLETED("AosCompleted"),
    AOS_OVERDUE("AosOverdue"),
    AOS_SUBMITTED_AWAITING_ANSWER("AosSubmittedAwaitingAnswer"),

    AWAITING_DA_APPLICATION_DUE_DATE("AwaitingDAApplicationDueDate"),
    AWAITING_CLARIFICATION("AwaitingClarification"),
    AWAITING_CONSIDERATION_DN("AwaitingConsiderationDN"),
    AWAITING_CONSIDERATION_DA("AwaitingConsiderationDA"),
    AWAITING_CONSIDERATION_GENERAL_APPLICATION("AwaitingConsiderationGeneralApplication"),
    AWAITING_DECREE_ABSOLUTE("AwaitingDecreeAbsolute"),
    AWAITING_DECREE_ABSOLUTE_PETITIONER("AwaitingDecreeAbsolutePetitioner"),
    AWAITING_LEGAL_ADVISOR_REFERRAL("AwaitingLegalAdvisorReferral"),
    AWAITING_LISTING("AwaitingListing"),
    AWAITING_PAYMENT("AwaitingPayment"),
    AWAITING_PETITIONER("AwaitingPetitioner"),
    AWAITING_PRONOUNCEMENT("AwaitingPronouncement"),
    AWAITING_REISSUE("AwaitingReissue"),

    DA_APPLICATION_OVERDUE("DAApplicationReceived"),
    DA_APPLICATION_RECEIVED("DAApplicationOverdue"),
    DEFENDED_DIVORCE("DefendedDivorce"),
    DIVORCE_GRANTED("DivorceGranted"),

    DN_AWAITING("DNAwaiting"),
    DN_COMPLETED("DNCompleted"),
    DN_STARTED("DNStarted"),

    PENDING_REJECTION("PendingRejection"),
    PETITION_COMPLETED("PetitionCompleted"),

    REJECTED("Rejected"),
    UNKNOWN("DNCompleted"),
    WITHDRAWN("Withdrawn");

    private final String value;
}
