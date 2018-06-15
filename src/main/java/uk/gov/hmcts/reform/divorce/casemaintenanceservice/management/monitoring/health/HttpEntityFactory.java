package uk.gov.hmcts.reform.divorce.casemaintenanceservice.management.monitoring.health;

import org.springframework.http.HttpEntity;

public interface HttpEntityFactory {

    HttpEntity<Object> createRequestEntityForHealthCheck();
}
