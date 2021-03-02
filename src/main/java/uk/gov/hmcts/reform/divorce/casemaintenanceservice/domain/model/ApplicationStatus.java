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
    AOS_OVERDUE("AosOverdue"),
    AOS_COMPLETED("AosCompleted"),
    AOS_DRAFTED("AosDrafted"),
    AWAITING_REISSUE("AwaitingReissue"),
    AWAITING_DECREE_NISI("AwaitingDecreeNisi"),
    DN_DRAFTED("DNDrafted"),
    DN_STARTED("DNStarted"),
    DN_COMPLETED("DNCompleted"),
    REJECTED("Rejected"),
    AWAITING_LEGAL_ADVISOR_REFERRAL("AwaitingLegalAdvisorReferral"),
    AWAITING_CONSIDERATION_GENERAL_APPLICATION("AwaitingConsiderationGeneralApplication"),
    AWAITING_CONSIDERATION_DN("AwaitingConsiderationDN"),
    DEFENDED_DIVORCE("DefendedDivorce"),
    AOS_SUBMITTED_AWAITING_ANSWER("AosSubmittedAwaitingAnswer"),
    AMEND_PETITION("AmendPetition"),
    AWAITING_AMEND_CASE("AwaitingAmendCase"),
    AWAITING_CLARIFICATION("AwaitingClarification"),
    AWAITING_CONSIDERATION("AwaitingConsideration"),
    DN_PRONOUNCED("DNPronounced"),
    DN_IS_REFUSED("DNisRefused"),
    AWAITING_DECREE_ABSOLUTE("AwaitingDecreeAbsolute"),
    AWAITING_PRONOUNCEMENT("AwaitingPronouncement"),
    DIVORCE_GRANTED("DivorceGranted"),
    DECREE_ABSOLUTE_REQUESTED("DARequested"),
    AOS_AWAITING_SOL("AosAwaitingSol"),
    CLARIFICATION_SUBMITTED("ClarificationSubmitted"),
    AWAITING_ADMIN_CLARIFICATION("AwaitingAdminClarification"),
    SERVICE_APPLICATION_NOT_APPROVED("ServiceApplicationNotApproved"),
    AWAITING_ALTERNATIVE_SERVICE("AwaitingAlternativeService"),
    AWAITING_PROCESS_SERVER_SERVICE("AwaitingProcessServerService"),
    AWAITING_DWP_RESPONSE("AwaitingDWPResponse"),
    AWAITING_GENERAL_REFERRAL_PAYMENT("AwaitingGeneralReferralPayment"),
    GENERAL_CONSIDERATION_COMPLETE("GeneralConsiderationComplete"),
    UNKNOWN("DNCompleted");

    private final String value;
}
