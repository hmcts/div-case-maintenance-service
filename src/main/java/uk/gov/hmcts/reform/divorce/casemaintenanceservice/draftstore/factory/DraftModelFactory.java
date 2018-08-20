package uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.factory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.model.CreateDraft;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.model.Draft;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.model.UpdateDraft;

import java.util.Map;

@Component
public class DraftModelFactory {
    @Value("${draft.store.api.document.type.divorceFormat}")
    private String documentTypeDivorceFormat;

    @Value("${draft.store.api.document.type.ccdFormat}")
    private String documentTypeCcdFormat;

    @Value("${draft.store.api.max.age}")
    private int maxAge;

    public CreateDraft createDraft(Map<String, Object> data, boolean divorceFormat) {
        return new CreateDraft(data, getDocumentType(divorceFormat), maxAge);
    }

    public UpdateDraft updateDraft(Map<String, Object> data, boolean divorceFormat) {
        return new UpdateDraft(data, getDocumentType(divorceFormat));
    }

    public boolean isDivorceDraft(Draft draft) {
        return draft.getType().equals(documentTypeDivorceFormat) || isDivorceDraftInCcdFormat(draft);
    }

    public boolean isDivorceDraftInCcdFormat(Draft draft) {
        return draft.getType().equals(documentTypeCcdFormat);
    }

    private String getDocumentType(boolean divorceFormat) {
        return divorceFormat ? documentTypeDivorceFormat : documentTypeCcdFormat;
    }
}
