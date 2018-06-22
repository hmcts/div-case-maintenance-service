package uk.gov.hmcts.reform.divorce.casemaintenanceservice.service;

import java.util.Map;

public interface CcdRetrievalService {
    Map<String, Object> retrievePetition(String authorisation);
}
