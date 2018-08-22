package uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model;


import com.google.common.collect.ImmutableMap;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@SuppressWarnings("squid:S1118")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CaseRetrievalStateMap {

    public static final Map<CaseStateGrouping, List<CaseState>> PETITIONER_CASE_STATE_GROUPING =
        ImmutableMap.of(
            CaseStateGrouping.INCOMPLETE,
            Arrays.asList(
                CaseState.AWAITING_PAYMENT,
                CaseState.AWAITING_HWF_DECISION),
            CaseStateGrouping.COMPLETE,
            Arrays.asList(
                CaseState.SUBMITTED,
                CaseState.ISSUED,
                CaseState.PENDING_REJECTION,
                CaseState.AWAITING_DOCUMENTS)
        );

    public static final Map<CaseStateGrouping, List<CaseState>> RESPONDENT_CASE_STATE_GROUPING =
        ImmutableMap.of(
            CaseStateGrouping.INCOMPLETE,
            Collections.singletonList(
                CaseState.AOS_RESPONDED),
            CaseStateGrouping.COMPLETE,
            Collections.singletonList(
                CaseState.AOS_COMPLETED
            )
        );
}
