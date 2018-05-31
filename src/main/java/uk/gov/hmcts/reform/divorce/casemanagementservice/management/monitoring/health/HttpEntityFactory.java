package uk.gov.hmcts.reform.divorce.casemanagementservice.management.monitoring.health;

import org.springframework.http.HttpEntity;

public interface HttpEntityFactory {

    HttpEntity<Object> createRequestEntityForHealthCheck();
}
