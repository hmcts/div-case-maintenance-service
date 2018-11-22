package uk.gov.hmcts.reform.divorce.casemaintenanceservice.management.monitoring.health;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class CaseFormatterServiceHealthCheck extends WebServiceHealthCheck {
    @Autowired
    public CaseFormatterServiceHealthCheck(HttpEntityFactory httpEntityFactory, RestTemplate restTemplate,
                                           @Value("${case.formatter.service.api.baseurl}/health") String uri) {
        super(httpEntityFactory, restTemplate, uri);
    }
}
