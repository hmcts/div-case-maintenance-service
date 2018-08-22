package uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDetails {
    private String id;
    private String email;
    private String forename;
    private String surname;
    private String authToken;
}