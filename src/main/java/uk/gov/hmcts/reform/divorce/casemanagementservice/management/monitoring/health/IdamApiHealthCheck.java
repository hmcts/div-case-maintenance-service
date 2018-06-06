package uk.gov.hmcts.reform.divorce.casemanagementservice.management.monitoring.health;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class IdamApiHealthCheck extends WebServiceHealthCheck {
    @Autowired
    public IdamApiHealthCheck(HttpEntityFactory httpEntityFactory, RestTemplate restTemplate,
                              @Value("${idam.api.url}") String uri) {
        super(httpEntityFactory, restTemplate, uri);
    }
}
