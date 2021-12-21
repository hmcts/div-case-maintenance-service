package uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@AllArgsConstructor
@Getter
public enum CaseState {
    AMEND_PETITION("AmendPetition", ApplicationStatus.AMEND_PETITION),
    AOS_AWAITING("AosAwaiting", ApplicationStatus.AOS_AWAITING),
    AOS_AWAITING_SOL("AosAwaitingSol", ApplicationStatus.AOS_AWAITING_SOL),
    AOS_COMPLETED("AosCompleted", ApplicationStatus.AOS_COMPLETED),
    AOS_DRAFTED("AosDrafted", ApplicationStatus.AOS_DRAFTED),
    AOS_OVERDUE("AosOverdue", ApplicationStatus.AOS_OVERDUE),
    AOS_STARTED("AosStarted", ApplicationStatus.AOS_STARTED),
    AOS_SUBMITTED_AWAITING_ANSWER("AosSubmittedAwaitingAnswer", ApplicationStatus.AOS_SUBMITTED_AWAITING_ANSWER),
    AWAITING_ADMIN_CLARIFICATION("AwaitingAdminClarification", ApplicationStatus.AWAITING_ADMIN_CLARIFICATION),
    AWAITING_BAILIFF_SERVICE("AwaitingBailiffService", ApplicationStatus.AWAITING_BAILIFF_SERVICE),
    AWAITING_BAILIFF_REFERRAL("AwaitingBailiffReferral", ApplicationStatus.AWAITING_BAILIFF_REFERRAL),
    AWAITING_ALTERNATIVE_SERVICE("AwaitingAlternativeService", ApplicationStatus.AWAITING_ALTERNATIVE_SERVICE),
    AWAITING_AMEND_CASE("AwaitingAmendCase", ApplicationStatus.AWAITING_AMEND_CASE),
    AWAITING_CLARIFICATION("AwaitingClarification", ApplicationStatus.AWAITING_CLARIFICATION),
    AWAITING_CONSIDERATION("AwaitingConsideration", ApplicationStatus.AWAITING_CONSIDERATION),
    AWAITING_CONSIDERATION_DN("AwaitingConsiderationDN", ApplicationStatus.AWAITING_CONSIDERATION_GENERAL_APPLICATION),
    AWAITING_CONSIDERATION_GENERAL_APPLICATION("AwaitingConsiderationGeneralApplication",
        ApplicationStatus.AWAITING_CONSIDERATION_GENERAL_APPLICATION),
    AWAITING_DECREE_ABSOLUTE("AwaitingDecreeAbsolute", ApplicationStatus.AWAITING_DECREE_ABSOLUTE),
    AWAITING_DECREE_NISI("AwaitingDecreeNisi", ApplicationStatus.AWAITING_DECREE_NISI),
    AWAITING_DOCUMENTS("AwaitingDocuments", ApplicationStatus.PETITION_COMPLETED),
    AWAITING_DWP_RESPONSE("AwaitingDWPResponse", ApplicationStatus.AWAITING_DWP_RESPONSE),
    AWAITING_GENERAL_REFERRAL_PAYMENT("AwaitingGeneralReferralPayment", ApplicationStatus.AWAITING_GENERAL_REFERRAL_PAYMENT),
    AWAITING_HWF_DECISION("AwaitingHWFDecision", ApplicationStatus.PETITION_COMPLETED),
    AWAITING_LEGAL_ADVISOR_REFERRAL("AwaitingLegalAdvisorReferral", ApplicationStatus.AWAITING_LEGAL_ADVISOR_REFERRAL),
    AWAITING_PAYMENT("AwaitingPayment", ApplicationStatus.AWAITING_PAYMENT),
    AWAITING_PETITIONER("AwaitingPetitioner", ApplicationStatus.AWAITING_PETITIONER),
    AWAITING_PROCESS_SERVER_SERVICE("AwaitingProcessServerService", ApplicationStatus.AWAITING_PROCESS_SERVER_SERVICE),
    AWAITING_PRONOUNCEMENT("AwaitingPronouncement", ApplicationStatus.AWAITING_PRONOUNCEMENT),
    AWAITING_REISSUE("AwaitingReissue", ApplicationStatus.AWAITING_REISSUE),
    CLARIFICATION_SUBMITTED("ClarificationSubmitted", ApplicationStatus.CLARIFICATION_SUBMITTED),
    DECREE_ABSOLUTE_REQUESTED("DaRequested", ApplicationStatus.DECREE_ABSOLUTE_REQUESTED),
    DEFENDED_DIVORCE("DefendedDivorce", ApplicationStatus.DEFENDED_DIVORCE),
    DIVORCE_GRANTED("DivorceGranted", ApplicationStatus.DIVORCE_GRANTED),
    DN_DRAFTED("DNDrafted", ApplicationStatus.DN_DRAFTED),
    DN_IS_REFUSED("DNisRefused", ApplicationStatus.DN_IS_REFUSED),
    DN_PRONOUNCED("DNPronounced", ApplicationStatus.DN_PRONOUNCED),
    GENERAL_CONSIDERATION_COMPLETE("GeneralConsiderationComplete", ApplicationStatus.GENERAL_CONSIDERATION_COMPLETE),
    ISSUED_TO_BAILIFF("IssuedToBailiff", ApplicationStatus.ISSUED_TO_BAILIFF),
    ISSUED("Issued", ApplicationStatus.PETITION_COMPLETED),
    PENDING_REJECTION("PendingRejection", ApplicationStatus.PENDING_REJECTION),
    REJECTED("Rejected", ApplicationStatus.REJECTED),
    SERVICE_APPLICATION_NOT_APPROVED("ServiceApplicationNotApproved", ApplicationStatus.SERVICE_APPLICATION_NOT_APPROVED),
    SUBMITTED("Submitted", ApplicationStatus.PETITION_COMPLETED),
    WELSH_RESPONSE_AWAITING_REVIEW("WelshResponseAwaitingReview", ApplicationStatus.WELSH_RESPONSE_AWAITING_REVIEW),
    WELSH_LA_DECISION("WelshLADecision", ApplicationStatus.WELSH_LA_DECISION),
    WELSH_DN_RECEIVED("WelshDNReceived", ApplicationStatus.WELSH_DN_RECEIVED),
    WELSH_BO_TRANSLATION_REQUESTED("BOTranslationRequested", ApplicationStatus.WELSH_BO_TRANSLATION_REQUESTED),
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
