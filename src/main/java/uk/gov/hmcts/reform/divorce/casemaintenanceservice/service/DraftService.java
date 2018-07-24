package uk.gov.hmcts.reform.divorce.casemaintenanceservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.model.Draft;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.model.DraftList;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.model.DraftsResponse;

public interface DraftService {
    DraftList getAllDrafts(String userToken);

    void saveDraft(String userToken, JsonNode data);

    Draft getDraft(String userToken);
}
