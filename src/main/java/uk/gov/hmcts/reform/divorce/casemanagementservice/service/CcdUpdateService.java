package uk.gov.hmcts.reform.divorce.casemanagementservice.service;

import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

public interface CcdUpdateService {
    CaseDetails update(String caseId, CaseDataContent caseDataContent, String authorisation);
}
