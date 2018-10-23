package uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model;


import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CaseRetrievalStateMap.PETITIONER_CASE_STATE_GROUPING;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CaseRetrievalStateMap.RESPONDENT_CASE_STATE_GROUPING;

public class CaseRetrievalStateMapTest {

    @Test
    public void petitionerCaseStateGroupingStateTest() {
        assertThat(PETITIONER_CASE_STATE_GROUPING.get(CaseStateGrouping.INCOMPLETE))
            .contains(
                CaseState.AWAITING_PAYMENT,
                CaseState.AWAITING_HWF_DECISION,
                CaseState.AWAITING_DECREE_NISI
            );

        assertThat(PETITIONER_CASE_STATE_GROUPING.get(CaseStateGrouping.COMPLETE))
            .contains(
                CaseState.SUBMITTED,
                CaseState.ISSUED,
                CaseState.PENDING_REJECTION,
                CaseState.AWAITING_DOCUMENTS
            );
    }

    @Test
    public void respondantCaseStateGroupingStateTest() {
        assertThat(RESPONDENT_CASE_STATE_GROUPING.get(CaseStateGrouping.INCOMPLETE))
            .contains(
                CaseState.AOS_STARTED
            );

        assertThat(RESPONDENT_CASE_STATE_GROUPING.get(CaseStateGrouping.COMPLETE))
            .contains(
                CaseState.AOS_COMPLETED,
                CaseState.AOS_SUBMITTED_AWAITING_ANSWER,
                CaseState.AWAITING_DECREE_NISI,
                CaseState.AWAITING_LEGAL_ADVISOR_REFERRAL,
                CaseState.AWAITING_CONSIDERATION_DN,
                CaseState.AWAITING_CLARIFICATION,
                CaseState.AWAITING_LISTING,
                CaseState.AWAITING_PRONOUNCEMENT
            );
    }

}