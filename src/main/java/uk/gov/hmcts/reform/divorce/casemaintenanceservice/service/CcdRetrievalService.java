package uk.gov.hmcts.reform.divorce.casemaintenanceservice.service;

import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CaseState;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CaseStateGrouping;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.DivCaseRole;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.exception.DuplicateCaseException;

import java.util.List;
import java.util.Map;

public interface CcdRetrievalService {
    CaseDetails retrieveCase(String authorisation, Map<CaseStateGrouping, List<CaseState>> caseStateGrouping,
                             DivCaseRole role)
        throws DuplicateCaseException;

    CaseDetails retrieveCase(String authorisation, DivCaseRole role) throws DuplicateCaseException;

    CaseDetails retrieveCaseById(String authorisation, String caseId);

    SearchResult searchCase(String authorisation, String query);
}
