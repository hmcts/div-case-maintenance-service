package uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CaseStateGrouping {
    COMPLETE,
    INCOMPLETE,
    UNKNOWN
}
