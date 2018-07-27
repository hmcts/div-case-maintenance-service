package uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.factory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.model.CreateDraft;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.model.Draft;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.model.UpdateDraft;

import java.util.Map;

@Component
public class DraftModelFactory {

    @Value("${draft.store.api.document.type}")
    private String documentType;

    @Value("${draft.store.api.max.age}")
    private int maxAge;

    public CreateDraft createDraft(Map<String, Object> data) {
        return new CreateDraft(data, documentType, maxAge, true);
    }

    public UpdateDraft updateDraft(Map<String, Object> data) {
        return new UpdateDraft(data, documentType, true);
    }

    public boolean isDivorceDraft(Draft draft) {
        return draft.getType().equalsIgnoreCase(documentType);
    }

}
