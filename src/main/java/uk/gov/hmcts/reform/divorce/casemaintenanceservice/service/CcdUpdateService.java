package uk.gov.hmcts.reform.divorce.casemaintenanceservice.service;

import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

public interface CcdUpdateService {
    CaseDetails update(String caseId, Object data, String eventId, String authorisation);

    CaseDetails updateBulkCase(String caseId, Object data, String eventId, String authorisation);
}
