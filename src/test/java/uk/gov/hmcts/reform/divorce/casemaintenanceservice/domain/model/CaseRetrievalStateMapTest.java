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
                CaseState.AWAITING_DECREE_NISI,
                CaseState.AWAITING_PETITIONER
            );

        assertThat(PETITIONER_CASE_STATE_GROUPING.get(CaseStateGrouping.COMPLETE))
            .contains(
                CaseState.SUBMITTED,
                CaseState.ISSUED,
                CaseState.PENDING_REJECTION,
                CaseState.AWAITING_DOCUMENTS,
                CaseState.AWAITING_LEGAL_ADVISOR_REFERRAL,
                CaseState.AWAITING_CONSIDERATION_GENERAL_APPLICATION,
                CaseState.AOS_AWAITING,
                CaseState.AOS_STARTED,
                CaseState.AOS_COMPLETED,
                CaseState.AOS_OVERDUE,
                CaseState.AOS_SUBMITTED_AWAITING_ANSWER,
                CaseState.AWAITING_CONSIDERATION_DN,
                CaseState.AOS_COMPLETED,
                CaseState.AWAITING_DECREE_NISI,
                CaseState.DN_PRONOUNCED,
                CaseState.AWAITING_DECREE_ABSOLUTE,
                CaseState.AWAITING_CLARIFICATION,
                CaseState.AWAITING_CONSIDERATION,
                CaseState.AWAITING_PRONOUNCEMENT,
                CaseState.DEFENDED_DIVORCE,
                CaseState.DIVORCE_GRANTED,
                CaseState.DN_IS_REFUSED
            );

        assertThat(PETITIONER_CASE_STATE_GROUPING.get(CaseStateGrouping.AMEND))
            .contains(
                CaseState.AMEND_PETITION
            );
    }

    @Test
    public void respondentCaseStateGroupingStateTest() {
        assertThat(RESPONDENT_CASE_STATE_GROUPING.get(CaseStateGrouping.INCOMPLETE))
            .contains(
                CaseState.AOS_STARTED
            );

        assertThat(RESPONDENT_CASE_STATE_GROUPING.get(CaseStateGrouping.COMPLETE))
            .contains(
                CaseState.SUBMITTED,
                CaseState.ISSUED,
                CaseState.PENDING_REJECTION,
                CaseState.AWAITING_CONSIDERATION_GENERAL_APPLICATION,
                CaseState.AWAITING_DOCUMENTS,
                CaseState.AOS_AWAITING,
                CaseState.AOS_STARTED,
                CaseState.AOS_COMPLETED,
                CaseState.AWAITING_REISSUE,
                CaseState.AOS_SUBMITTED_AWAITING_ANSWER,
                CaseState.AWAITING_CONSIDERATION_DN,
                CaseState.AWAITING_DECREE_NISI,
                CaseState.AWAITING_CLARIFICATION,
                CaseState.AWAITING_CONSIDERATION,
                CaseState.DN_PRONOUNCED,
                CaseState.DN_IS_REFUSED,
                CaseState.AWAITING_DECREE_ABSOLUTE,
                CaseState.AWAITING_PRONOUNCEMENT,
                CaseState.DEFENDED_DIVORCE,
                CaseState.DIVORCE_GRANTED
            );
    }

}
