package uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@AllArgsConstructor
@Getter
public enum CitizenCaseState {
    INCOMPLETE(Arrays.asList("AwaitingPayment", "AwaitingHWFDecision")),
    COMPLETE(Arrays.asList("Submitted", "Issued", "PendingRejection", "AwaitingDocuments")),
    UNKNOWN(Collections.EMPTY_LIST);

    List<String> states;

    public static CitizenCaseState getState(String state) {
        return Arrays.stream(CitizenCaseState.values())
            .filter(citizenCaseState -> citizenCaseState.states.contains(state))
            .findFirst()
            .orElse(UNKNOWN);
    }
}
