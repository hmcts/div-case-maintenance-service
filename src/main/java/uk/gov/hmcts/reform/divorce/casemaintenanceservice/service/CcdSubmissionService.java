package uk.gov.hmcts.reform.divorce.casemaintenanceservice.service;

import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.Map;

public interface CcdSubmissionService {
    CaseDetails submitCase(Map<String, Object> data, String authorisation);
}
