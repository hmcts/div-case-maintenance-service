package uk.gov.hmcts.reform.divorce.casemaintenanceservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.model.DraftList;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.exception.DuplicateCaseException;

public interface PetitionService {
    CaseDetails retrievePetition(String authorisation, boolean checkCcd) throws DuplicateCaseException;

    void saveDraft(String authorisation, JsonNode data);

    DraftList getAllDrafts(String authorisation);

    void deleteDraft(String authorisation);
}
