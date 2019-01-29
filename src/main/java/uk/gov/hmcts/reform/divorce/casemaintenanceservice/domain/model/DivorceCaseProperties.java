package uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DivorceCaseProperties {
    public static final String D8_CASE_REFERENCE = "D8caseReference";
    public static final String D8_REASON_FOR_DIVORCE = "D8ReasonForDivorce";
    public static final String CASE_REFERENCE = "caseReference";
    public static final String PREVIOUS_CASE_ID = "previousCaseId";
    public static final String HWF_NEED_HELP = "helpWithFeesNeedHelp";
    public static final String HWF_APPLIED_FOR_FEES = "helpWithFeesAppliedForFees";
    public static final String HWF_REFERENCE = "helpWithFeesReferenceNumber";
    public static final String REASON_FOR_DIVORCE = "reasonForDivorce";
    public static final String PREVIOUS_REASONS_FOR_DIVORCE = "previousReasonsForDivorce";
    public static final String CCD_PREVIOUS_REASONS_FOR_DIVORCE = "PreviousReasonsForDivorce";
}
