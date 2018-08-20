package uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class DraftList {
    private final List<Draft> data;

    @JsonProperty("paging_cursors")
    private final PagingCursors paging;

    @Data
    public static class PagingCursors {
        private final String after;
    }
}
