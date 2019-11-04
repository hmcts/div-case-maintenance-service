package uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CcdCaseProperties {

    public static final String ISSUE_DATE = "IssueDate";

    public static final String D8_CASE_REFERENCE = "D8caseReference";
    public static final String D8_PETITIONER_EMAIL = "D8PetitionerEmail";
    public static final String D8_REASON_FOR_DIVORCE = "D8ReasonForDivorce";
    public static final String D8_DIVORCE_WHO = "D8DivorceWho";
    public static final String D8_LEGAL_PROCEEDINGS = "D8LegalProceedings";
    public static final String D8_SCREEN_HAS_MARRIAGE_BROKEN = "D8ScreenHasMarriageBroken";
    public static final String D8_DIVORCE_UNIT = "D8DivorceUnit";
    public static final String CO_RESP_LETTER_HOLDER_ID_FIELD = "CoRespLetterHolderId";
    public static final String CO_RESP_EMAIL_ADDRESS = "CoRespEmailAddress";

    public static final String RESP_LETTER_HOLDER_ID_FIELD = "AosLetterHolderId";
    public static final String RESP_EMAIL_ADDRESS = "RespEmailAddress";

    public static final String D8_DOCUMENTS_UPLOADED = "D8DocumentsUploaded";
    public static final String D8_REJECT_DOCUMENTS_UPLOADED = "D8RejectDocumentsUploaded";
    public static final String D8_DOCUMENTS_GENERATED = "D8DocumentsGenerated";

    public static final String PREVIOUS_REASONS_DIVORCE = "PreviousReasonsForDivorce";
    public static final String PREVIOUS_REASONS_DIVORCE_REFUSAL = "PreviousReasonsForDivorceRefusal";
    public static final String PREVIOUS_ISSUE_DATE = "PreviousIssueDate";

    public static final String RESP_SOLICITOR_EMAIL_ADDRESS = "D8RespondentSolicitorEmail";
    public static final String RESP_SOL_REPRESENTED = "respondentSolicitorRepresented";
    public static final String D8_RESPONDENT_SOLICITOR_NAME = "D8RespondentSolicitorName";
    public static final String D8_RESPONDENT_SOLICITOR_COMPANY = "D8RespondentSolicitorCompany";

    public static final String REFUSAL_ORDER_REJECTION_REASONS = "RefusalRejectionReason";
    public static final String REJECTION_NO_JURISDICTION = "noJurisdiction";
    public static final String REJECTION_NO_CRITERIA = "noCriteria";
    public static final String REJECTION_INSUFFICIENT_DETAILS = "insufficentDetails";
}
