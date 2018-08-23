package uk.gov.hmcts.reform.divorce.casemaintenanceservice.service;

import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CaseState;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CaseStateGrouping;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.exception.DuplicateCaseException;

import java.util.List;
import java.util.Map;

public interface CcdRetrievalService {
    CaseDetails retrieveCase(String authorisation, Map<CaseStateGrouping, List<CaseState>> caseStateGrouping)
        throws DuplicateCaseException;
}
