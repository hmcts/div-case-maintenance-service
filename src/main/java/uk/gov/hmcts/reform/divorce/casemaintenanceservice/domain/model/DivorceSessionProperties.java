package uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DivorceSessionProperties {
    public static final String PREVIOUS_CASE_ID = "previousCaseId";
    public static final String PREVIOUS_REASONS_FOR_DIVORCE = "previousReasonsForDivorce";
    public static final String PREVIOUS_REASONS_FOR_DIVORCE_REFUSAL = "previousReasonsForDivorceRefusal";

    public static final String LEGAL_PROCEEDINGS = "legalProceedings";
    public static final String DIVORCE_WHO = "divorceWho";
    public static final String SCREEN_HAS_MARRIAGE_BROKEN = "screenHasMarriageBroken";
    public static final String CREATED_DATE = "createdDate";
    public static final String COURTS = "courts";
    public static final String PETITIONER_EMAIL = "petitionerEmail";
}
