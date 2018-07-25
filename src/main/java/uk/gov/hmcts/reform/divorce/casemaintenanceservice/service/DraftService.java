package uk.gov.hmcts.reform.divorce.casemaintenanceservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.model.Draft;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.model.DraftList;

public interface DraftService {
    DraftList getAllDrafts(String userToken);

    void saveDraft(String userToken, JsonNode data);

    Draft getDraft(String userToken);

    void deleteDraft(String authorisation);
}
