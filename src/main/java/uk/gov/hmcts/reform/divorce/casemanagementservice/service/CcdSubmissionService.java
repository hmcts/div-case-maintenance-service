package uk.gov.hmcts.reform.divorce.casemanagementservice.service;

import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

public interface CcdSubmissionService {
    CaseDetails submitCase(Object data, String authorisation);
}
