package uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CcdCaseProperties {
    public static final String D8_CASE_REFERENCE = "D8caseReference";
    public static final String PREVIOUS_REASONS_DIVORCE = "PreviousReasonsForDivorce";
    public static final String D8_REASON_FOR_DIVORCE = "D8ReasonForDivorce";
    public static final String D8_DIVORCE_WHO = "D8DivorceWho";
    public static final String D8_LEGAL_PROCEEDINGS = "D8LegalProceedings";
    public static final String D8_SCREEN_HAS_MARRIAGE_BROKEN = "D8ScreenHasMarriageBroken";
    public static final String D8_DIVORCE_UNIT = "D8DivorceUnit";
    public static final String CO_RESP_LETTER_HOLDER_ID_FIELD = "CoRespLetterHolderId";
    public static final String CO_RESP_RECEIVED_AOS_FIELD = "ReceivedAosFromCoResp";
    public static final String RESP_LETTER_HOLDER_ID_FIELD = "AosLetterHolderId";
    public static final String RESP_RECEIVED_AOS_FIELD = "ReceivedAOSfromResp";
}
