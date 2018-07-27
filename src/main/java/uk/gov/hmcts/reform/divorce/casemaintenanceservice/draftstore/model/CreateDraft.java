package uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
public class CreateDraft {
    private final Map<String, Object> document;
    private final String type;
    @JsonProperty("max_age")
    private final int maxAge;
    private final boolean ccdFormat;
}
