package uk.gov.hmcts.reform.divorce.casemaintenanceservice.service;

public interface CcdAccessService {
    void linkRespondent(String authorisation, String caseId, String letterHolderId);

    void unlinkRespondent(String authorisation, String caseId);
}
