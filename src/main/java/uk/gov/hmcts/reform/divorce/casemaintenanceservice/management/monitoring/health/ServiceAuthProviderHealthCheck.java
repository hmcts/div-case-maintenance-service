package uk.gov.hmcts.reform.divorce.casemaintenanceservice.management.monitoring.health;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ServiceAuthProviderHealthCheck extends WebServiceHealthCheck {
    @Autowired
    public ServiceAuthProviderHealthCheck(HttpEntityFactory httpEntityFactory, RestTemplate restTemplate,
                                          @Value("${idam.s2s-auth.url}/health") String uri) {
        super(httpEntityFactory, restTemplate, uri);
    }
}
