package uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Deprecated
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class TokenExchangeResponse {
    @JsonProperty("access_token")
    private String accessToken;
}
