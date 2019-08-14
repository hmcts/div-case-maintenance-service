package uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Deprecated
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class AuthenticateUserResponse {
    private String code;
}
