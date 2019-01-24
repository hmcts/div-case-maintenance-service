package uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@AllArgsConstructor
@Getter
public enum DivorceCaseProperties {
    D8_CASE_REFERENCE("D8caseReference"),
    D8_REASON_FOR_DIVORCE("D8ReasonForDivorce"),
    CASE_REFERENCE("caseReference"),
    CCD_PREVIOUS_CASE_ID("PreviousCaseId"),
    PREVIOUS_CASE_ID("previousCaseId"),
    REASON_FOR_DIVORCE("reasonForDivorce"),
    PREVIOUS_REASONS_FOR_DIVORCE("previousReasonsForDivorce"),
    CCD_PREVIOUS_REASONS_FOR_DIVORCE("PreviousReasonsForDivorce"),
    UNKNOWN("Unknown");


    private final String value;

    public static DivorceCaseProperties getState(String state) {
        return Arrays.stream(DivorceCaseProperties.values())
            .filter(caseState -> caseState.value.equalsIgnoreCase(state))
            .findFirst()
            .orElse(UNKNOWN);
    }

}
