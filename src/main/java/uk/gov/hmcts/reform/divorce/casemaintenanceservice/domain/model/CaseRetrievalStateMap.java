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
            CaseStateGrouping.INCOMPLETE, Arrays.asList(
                CaseState.AWAITING_PAYMENT,
                CaseState.AWAITING_HWF_DECISION,
                CaseState.AWAITING_DECREE_NISI,
                CaseState.AWAITING_PETITIONER
            ),
            CaseStateGrouping.COMPLETE, Arrays.asList(
                CaseState.SUBMITTED,
                CaseState.ISSUED,
                CaseState.PENDING_REJECTION,
                CaseState.AWAITING_CONSIDERATION_GENERAL_APPLICATION,
                CaseState.AWAITING_DOCUMENTS,
                CaseState.AOS_AWAITING,
                CaseState.AOS_STARTED,
                CaseState.AOS_COMPLETED,
                CaseState.AOS_OVERDUE,
                CaseState.AOS_SUBMITTED_AWAITING_ANSWER,
                CaseState.AOS_COMPLETED,
                CaseState.AWAITING_DECREE_NISI,
                CaseState.AWAITING_LEGAL_ADVISOR_REFERRAL,
                CaseState.AWAITING_CLARIFICATION,
                CaseState.AWAITING_CONSIDERATION_DN,
                CaseState.DN_PRONOUNCED,
                CaseState.AWAITING_DECREE_ABSOLUTE,
                CaseState.AWAITING_CONSIDERATION,
                CaseState.AWAITING_PRONOUNCEMENT,
                CaseState.DEFENDED_DIVORCE,
                CaseState.DIVORCE_GRANTED,
                CaseState.DN_IS_REFUSED,
                CaseState.DECREE_ABSOLUTE_REQUESTED,
                CaseState.AOS_AWAITING_SOL,
                CaseState.CLARIFICATION_SUBMITTED,
                CaseState.AWAITING_ADMIN_CLARIFICATION
            ),
            CaseStateGrouping.AMEND, Arrays.asList(
                CaseState.AMEND_PETITION
            )
        );

    public static final Map<CaseStateGrouping, List<CaseState>> RESPONDENT_CASE_STATE_GROUPING =
        ImmutableMap.of(
            CaseStateGrouping.INCOMPLETE, Collections.singletonList(CaseState.AOS_STARTED),
            CaseStateGrouping.COMPLETE, Arrays.asList(
                CaseState.SUBMITTED,
                CaseState.ISSUED,
                CaseState.PENDING_REJECTION,
                CaseState.AWAITING_CONSIDERATION_GENERAL_APPLICATION,
                CaseState.AWAITING_DOCUMENTS,
                CaseState.AOS_AWAITING,
                CaseState.AOS_STARTED,
                CaseState.AOS_OVERDUE,
                CaseState.AWAITING_REISSUE,
                CaseState.AOS_SUBMITTED_AWAITING_ANSWER,
                CaseState.AOS_COMPLETED,
                CaseState.AWAITING_CONSIDERATION_DN,
                CaseState.AWAITING_DECREE_NISI,
                CaseState.AWAITING_LEGAL_ADVISOR_REFERRAL,
                CaseState.AWAITING_CLARIFICATION,
                CaseState.AWAITING_CONSIDERATION,
                CaseState.DN_PRONOUNCED,
                CaseState.AWAITING_DECREE_ABSOLUTE,
                CaseState.AWAITING_PRONOUNCEMENT,
                CaseState.DEFENDED_DIVORCE,
                CaseState.DN_DRAFTED,
                CaseState.DIVORCE_GRANTED,
                CaseState.DN_IS_REFUSED,
                CaseState.DECREE_ABSOLUTE_REQUESTED,
                CaseState.AOS_AWAITING_SOL,
                CaseState.CLARIFICATION_SUBMITTED,
                CaseState.AWAITING_ADMIN_CLARIFICATION)
        );

}
