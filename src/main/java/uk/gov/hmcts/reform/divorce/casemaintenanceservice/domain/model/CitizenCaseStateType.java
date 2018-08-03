package uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.EMPTY_LIST;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CitizenCaseState.AWAITING_DECREE_NISI;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CitizenCaseState.AWAITING_DOCUMENTS;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CitizenCaseState.AWAITING_HWF_DECISION;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CitizenCaseState.AWAITING_PAYMENT;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CitizenCaseState.ISSUED;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CitizenCaseState.PENDING_REJECTION;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CitizenCaseState.SUBMITTED;

@AllArgsConstructor
@Getter
public enum CitizenCaseStateType {

    INCOMPLETE(asList(AWAITING_PAYMENT, AWAITING_HWF_DECISION)),
    COMPLETE(asList(SUBMITTED, ISSUED, PENDING_REJECTION, AWAITING_DOCUMENTS, AWAITING_DECREE_NISI)),
    UNKNOWN(EMPTY_LIST);

    List<CitizenCaseState> states;

    public static CitizenCaseStateType getState(String state) {

        for (CitizenCaseStateType citizenCaseStateType : CitizenCaseStateType.values()) {
            boolean inputStateMatch = citizenCaseStateType.getStates()
                    .stream()
                    .filter(citizenCaseState -> citizenCaseState.getValue().equalsIgnoreCase(state))
                    .findFirst()
                    .isPresent();
            if (inputStateMatch) {
                return citizenCaseStateType;
            }
        }
        return UNKNOWN;
    }
}
