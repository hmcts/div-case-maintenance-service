package uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.model;

import lombok.Data;

import java.util.Map;

@Data
public class UpdateDraft {
    private final Map<String, Object> document;
    private final String type;
}
