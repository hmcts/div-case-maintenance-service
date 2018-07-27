package uk.gov.hmcts.reform.divorce.casemaintenanceservice.service;

import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.model.DraftList;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.exception.DuplicateCaseException;

import java.util.Map;

public interface PetitionService {
    CaseDetails retrievePetition(String authorisation, boolean checkCcd) throws DuplicateCaseException;

    void saveDraft(String authorisation, Map<String, Object> data);

    DraftList getAllDrafts(String authorisation);

    void deleteDraft(String authorisation);
}
