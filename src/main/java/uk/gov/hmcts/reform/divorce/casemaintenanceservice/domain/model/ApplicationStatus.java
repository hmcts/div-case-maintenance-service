package uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ApplicationStatus {
    AMEND_PETITION("AmendPetition"),
    AOS_AWAITING("AosAwaiting"),
    AOS_AWAITING_SOL("AosAwaitingSol"),
    AOS_COMPLETED("AosCompleted"),
    AOS_DRAFTED("AosDrafted"),
    AOS_OVERDUE("AosOverdue"),
    AOS_STARTED("AosStarted"),
    AOS_SUBMITTED_AWAITING_ANSWER("AosSubmittedAwaitingAnswer"),
    AWAITING_ADMIN_CLARIFICATION("AwaitingAdminClarification"),
    AWAITING_ALTERNATIVE_SERVICE("AwaitingAlternativeService"),
    AWAITING_AMEND_CASE("AwaitingAmendCase"),
    AWAITING_CLARIFICATION("AwaitingClarification"),
    AWAITING_CONSIDERATION("AwaitingConsideration"),
    AWAITING_CONSIDERATION_DN("AwaitingConsiderationDN"),
    AWAITING_CONSIDERATION_GENERAL_APPLICATION("AwaitingConsiderationGeneralApplication"),
    AWAITING_DECREE_ABSOLUTE("AwaitingDecreeAbsolute"),
    AWAITING_DECREE_NISI("AwaitingDecreeNisi"),
    AWAITING_DWP_RESPONSE("AwaitingDWPResponse"),
    AWAITING_GENERAL_REFERRAL_PAYMENT("AwaitingGeneralReferralPayment"),
    AWAITING_LEGAL_ADVISOR_REFERRAL("AwaitingLegalAdvisorReferral"),
    AWAITING_PAYMENT("AwaitingPayment"),
    AWAITING_PETITIONER("AwaitingPetitioner"),
    AWAITING_PROCESS_SERVER_SERVICE("AwaitingProcessServerService"),
    AWAITING_PRONOUNCEMENT("AwaitingPronouncement"),
    AWAITING_REISSUE("AwaitingReissue"),
    CLARIFICATION_SUBMITTED("ClarificationSubmitted"),
    DECREE_ABSOLUTE_REQUESTED("DARequested"),
    DEFENDED_DIVORCE("DefendedDivorce"),
    DIVORCE_GRANTED("DivorceGranted"),
    DN_COMPLETED("DNCompleted"),
    DN_DRAFTED("DNDrafted"),
    DN_IS_REFUSED("DNisRefused"),
    DN_PRONOUNCED("DNPronounced"),
    DN_STARTED("DNStarted"),
    GENERAL_CONSIDERATION_COMPLETE("GeneralConsiderationComplete"),
    PENDING_REJECTION("PendingRejection"),
    PETITION_COMPLETED("PetitionCompleted"),
    REJECTED("Rejected"),
    SERVICE_APPLICATION_NOT_APPROVED("ServiceApplicationNotApproved"),
    UNKNOWN("DNCompleted");

    private final String value;
}
